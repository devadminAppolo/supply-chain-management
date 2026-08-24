package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.RackDto;
import com.supplychain.wms.location.domain.model.Aisle;
import com.supplychain.wms.location.domain.model.Rack;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.location.domain.model.Zone;
import com.supplychain.wms.location.infrastructure.mapper.RackMapper;
import com.supplychain.wms.location.infrastructure.repository.AisleRepository;
import com.supplychain.wms.location.infrastructure.repository.RackRepository;
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
public class RackServiceImpl implements RackService {

    private final RackRepository      repository;
    private final RackMapper          mapper;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository      zoneRepository;
    private final AisleRepository     aisleRepository;

    @Override
    @Transactional
    public RackDto save(RackDto dto) {
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long zoneId      = resolveZoneId(dto.getZoneId(), warehouseId);
        Long aisleId     = resolveAisleId(dto.getAisleId(), zoneId);
        if (repository.existsByRackCodeAndAisleIdAndDeletedFalse(dto.getRackCode(), aisleId)) {
            log.warn("Duplicate rack code {} in aisle {}", dto.getRackCode(), aisleId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Rack code already exists in this aisle: " + dto.getRackCode());
        }
        Rack entity = mapper.toEntity(dto);
        entity.setWarehouseId(warehouseId);
        entity.setZoneId(zoneId);
        entity.setAisleId(aisleId);
        entity.setDeleted(false);
        Rack saved = repository.save(entity);
        log.info("Created rack id={}, code={}", saved.getId(), saved.getRackCode());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    @Transactional
    public RackDto update(Long id, RackDto dto) {
        Rack entity      = findActiveOrThrow(id);
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long zoneId      = resolveZoneId(dto.getZoneId(), warehouseId);
        Long aisleId     = resolveAisleId(dto.getAisleId(), zoneId);
        if (repository.existsByRackCodeAndAisleIdAndIdNotAndDeletedFalse(dto.getRackCode(), aisleId, id)) {
            log.warn("Duplicate rack code {} on update", dto.getRackCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Rack code already exists in this aisle: " + dto.getRackCode());
        }
        mapper.updateEntity(dto, entity);
        entity.setWarehouseId(warehouseId);
        entity.setZoneId(zoneId);
        entity.setAisleId(aisleId);
        Rack saved = repository.save(entity);
        log.info("Updated rack id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public RackDto findById(Long id) {
        Rack entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<RackDto> findAll(String search, Long warehouseId, Long zoneId, Long aisleId, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search, warehouseId, zoneId, aisleId), pageable)
                .map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    public List<DropdownOptionDto> findDropdown(String aisleId) {
        Long resolvedId = aisleRepository.findFirstByAisleNameAndDeletedFalse(aisleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aisle not found with name: " + aisleId))
                .getId();
        return repository.findAllByAisleIdAndDeletedFalseOrderByRackNameAsc(resolvedId)
                .stream()
                .map(r -> new DropdownOptionDto(r.getRackName(), r.getRackName()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Rack entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted rack id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Rack> entities = repository.findAllById(ids).stream()
                .filter(r -> !r.isDeleted()).toList();
        entities.forEach(r -> r.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} racks", entities.size());
    }

    private Rack findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Rack not found with id: " + id));
    }

    private Long resolveWarehouseId(String name) {
        return warehouseRepository.findByWarehouseNameAndDeletedFalse(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Warehouse not found with name: " + name))
                .getId();
    }

    private Long resolveZoneId(String name, Long warehouseId) {
        return zoneRepository.findByZoneNameAndWarehouseIdAndDeletedFalse(name, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Zone not found with name: " + name))
                .getId();
    }

    private Long resolveAisleId(String name, Long zoneId) {
        return aisleRepository.findByAisleNameAndZoneIdAndDeletedFalse(name, zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aisle not found with name: " + name))
                .getId();
    }

    private RackDto enrich(RackDto dto, Rack entity) {
        dto.setWarehouseId(warehouseRepository.findById(entity.getWarehouseId()).map(Warehouse::getWarehouseName).orElse(null));
        dto.setZoneId(zoneRepository.findById(entity.getZoneId()).map(Zone::getZoneName).orElse(null));
        dto.setAisleId(aisleRepository.findById(entity.getAisleId()).map(Aisle::getAisleName).orElse(null));
        return dto;
    }

    private Specification<Rack> buildSpec(String search, Long warehouseId, Long zoneId, Long aisleId) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (warehouseId != null) pred = cb.and(pred, cb.equal(root.get("warehouseId"), warehouseId));
            if (zoneId != null)      pred = cb.and(pred, cb.equal(root.get("zoneId"), zoneId));
            if (aisleId != null)     pred = cb.and(pred, cb.equal(root.get("aisleId"), aisleId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("rackCode")), pattern),
                        cb.like(cb.lower(root.get("rackName")), pattern)
                ));
            }
            return pred;
        };
    }
}
