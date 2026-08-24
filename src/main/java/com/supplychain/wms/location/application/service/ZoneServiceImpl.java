package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.ZoneDto;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.location.domain.model.Zone;
import com.supplychain.wms.location.infrastructure.mapper.ZoneMapper;
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
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository      repository;
    private final ZoneMapper          mapper;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public ZoneDto save(ZoneDto dto) {
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        if (repository.existsByZoneCodeAndWarehouseIdAndDeletedFalse(dto.getZoneCode(), warehouseId)) {
            log.warn("Duplicate zone code {} in warehouse {}", dto.getZoneCode(), warehouseId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Zone code already exists in this warehouse: " + dto.getZoneCode());
        }
        Zone entity = mapper.toEntity(dto);
        entity.setWarehouseId(warehouseId);
        entity.setDeleted(false);
        Zone saved = repository.save(entity);
        log.info("Created zone id={}, code={}", saved.getId(), saved.getZoneCode());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    @Transactional
    public ZoneDto update(Long id, ZoneDto dto) {
        Zone entity = findActiveOrThrow(id);
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        if (repository.existsByZoneCodeAndWarehouseIdAndIdNotAndDeletedFalse(dto.getZoneCode(), warehouseId, id)) {
            log.warn("Duplicate zone code {} on update", dto.getZoneCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Zone code already exists in this warehouse: " + dto.getZoneCode());
        }
        mapper.updateEntity(dto, entity);
        entity.setWarehouseId(warehouseId);
        Zone saved = repository.save(entity);
        log.info("Updated zone id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public ZoneDto findById(Long id) {
        Zone entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<ZoneDto> findAll(String search, Long warehouseId, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search, warehouseId), pageable)
                .map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    public List<DropdownOptionDto> findDropdown(String warehouseId) {
        Long resolvedId = resolveWarehouseId(warehouseId);
        return repository.findAllByWarehouseIdAndDeletedFalseOrderByZoneNameAsc(resolvedId)
                .stream()
                .map(z -> new DropdownOptionDto(z.getZoneName(), z.getZoneName()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Zone entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted zone id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Zone> entities = repository.findAllById(ids).stream()
                .filter(z -> !z.isDeleted())
                .toList();
        entities.forEach(z -> z.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} zones", entities.size());
    }

    private Zone findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(z -> !z.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Zone not found with id: " + id));
    }

    private Long resolveWarehouseId(String warehouseName) {
        return warehouseRepository.findByWarehouseNameAndDeletedFalse(warehouseName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Warehouse not found with name: " + warehouseName))
                .getId();
    }

    private ZoneDto enrich(ZoneDto dto, Zone entity) {
        dto.setWarehouseId(warehouseRepository.findById(entity.getWarehouseId())
                .map(Warehouse::getWarehouseName).orElse(null));
        return dto;
    }

    private Specification<Zone> buildSpec(String search, Long warehouseId) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (warehouseId != null) pred = cb.and(pred, cb.equal(root.get("warehouseId"), warehouseId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("zoneCode")), pattern),
                        cb.like(cb.lower(root.get("zoneName")), pattern)
                ));
            }
            return pred;
        };
    }
}
