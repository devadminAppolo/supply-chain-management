package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.AisleDto;
import com.supplychain.wms.location.domain.model.Aisle;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.location.domain.model.Zone;
import com.supplychain.wms.location.infrastructure.mapper.AisleMapper;
import com.supplychain.wms.location.infrastructure.repository.AisleRepository;
import com.supplychain.wms.location.infrastructure.repository.WarehouseRepository;
import com.supplychain.wms.location.infrastructure.repository.ZoneRepository;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AisleServiceImpl implements AisleService {

    private final AisleRepository     repository;
    private final AisleMapper         mapper;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository      zoneRepository;

    @Override
    @Transactional
    public AisleDto save(AisleDto dto) {
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long zoneId      = resolveZoneId(dto.getZoneId(), warehouseId);
        if (repository.existsByAisleCodeAndZoneIdAndDeletedFalse(dto.getAisleCode(), zoneId)) {
            log.warn("Duplicate aisle code {} in zone {}", dto.getAisleCode(), zoneId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Aisle code already exists in this zone: " + dto.getAisleCode());
        }
        Aisle entity = mapper.toEntity(dto);
        entity.setWarehouseId(warehouseId);
        entity.setZoneId(zoneId);
        entity.setDeleted(false);
        Aisle saved = repository.save(entity);
        log.info("Created aisle id={}, code={}", saved.getId(), saved.getAisleCode());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    @Transactional
    public AisleDto update(Long id, AisleDto dto) {
        Aisle entity     = findActiveOrThrow(id);
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long zoneId      = resolveZoneId(dto.getZoneId(), warehouseId);
        if (repository.existsByAisleCodeAndZoneIdAndIdNotAndDeletedFalse(dto.getAisleCode(), zoneId, id)) {
            log.warn("Duplicate aisle code {} on update", dto.getAisleCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Aisle code already exists in this zone: " + dto.getAisleCode());
        }
        mapper.updateEntity(dto, entity);
        entity.setWarehouseId(warehouseId);
        entity.setZoneId(zoneId);
        Aisle saved = repository.save(entity);
        log.info("Updated aisle id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public AisleDto findById(Long id) {
        Aisle entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<AisleDto> findAll(String search, Long warehouseId, Long zoneId, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search, warehouseId, zoneId), pageable)
                .map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    public List<DropdownOptionDto> findDropdown(String zoneId) {
        Long resolvedId = zoneRepository.findFirstByZoneNameAndDeletedFalse(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Zone not found with name: " + zoneId))
                .getId();
        return repository.findAllByZoneIdAndDeletedFalseOrderByAisleNameAsc(resolvedId)
                .stream()
                .map(a -> new DropdownOptionDto(a.getAisleName(), a.getAisleName()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Aisle entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted aisle id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Aisle> entities = repository.findAllById(ids).stream()
                .filter(a -> !a.isDeleted()).toList();
        entities.forEach(a -> a.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} aisles", entities.size());
    }

    private Aisle findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aisle not found with id: " + id));
    }

    private Long resolveWarehouseId(String warehouseName) {
        return warehouseRepository.findByWarehouseNameAndDeletedFalse(warehouseName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Warehouse not found with name: " + warehouseName))
                .getId();
    }

    private Long resolveZoneId(String zoneName, Long warehouseId) {
        return zoneRepository.findByZoneNameAndWarehouseIdAndDeletedFalse(zoneName, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Zone not found with name: " + zoneName))
                .getId();
    }

    private AisleDto enrich(AisleDto dto, Aisle entity) {
        dto.setWarehouseId(warehouseRepository.findById(entity.getWarehouseId())
                .map(Warehouse::getWarehouseName).orElse(null));
        dto.setZoneId(zoneRepository.findById(entity.getZoneId())
                .map(Zone::getZoneName).orElse(null));
        return dto;
    }

    private Specification<Aisle> buildSpec(String search, Long warehouseId, Long zoneId) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (warehouseId != null) pred = cb.and(pred, cb.equal(root.get("warehouseId"), warehouseId));
            if (zoneId != null)      pred = cb.and(pred, cb.equal(root.get("zoneId"), zoneId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("aisleCode")), pattern),
                        cb.like(cb.lower(root.get("aisleName")), pattern)
                ));
            }
            return pred;
        };
    }
}
