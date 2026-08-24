package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.BinDto;
import com.supplychain.wms.location.domain.model.Aisle;
import com.supplychain.wms.location.domain.model.Bin;
import com.supplychain.wms.location.domain.model.Rack;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.location.domain.model.Zone;
import com.supplychain.wms.location.infrastructure.mapper.BinMapper;
import com.supplychain.wms.location.infrastructure.repository.AisleRepository;
import com.supplychain.wms.location.infrastructure.repository.BinRepository;
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
public class BinServiceImpl implements BinService {

    private final BinRepository       repository;
    private final BinMapper           mapper;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository      zoneRepository;
    private final AisleRepository     aisleRepository;
    private final RackRepository      rackRepository;

    @Override
    @Transactional
    public BinDto save(BinDto dto) {
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long zoneId      = resolveZoneId(dto.getZoneId(), warehouseId);
        Long aisleId     = resolveAisleId(dto.getAisleId(), zoneId);
        Long rackId      = resolveRackId(dto.getRackId(), aisleId);
        if (repository.existsByBinCodeAndRackIdAndDeletedFalse(dto.getBinCode(), rackId)) {
            log.warn("Duplicate bin code {} in rack {}", dto.getBinCode(), rackId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bin code already exists in this rack: " + dto.getBinCode());
        }
        if (dto.getBarcode() != null && repository.existsByBarcodeAndDeletedFalse(dto.getBarcode())) {
            log.warn("Duplicate bin barcode {}", dto.getBarcode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bin barcode already exists: " + dto.getBarcode());
        }
        Bin entity = mapper.toEntity(dto);
        entity.setWarehouseId(warehouseId);
        entity.setZoneId(zoneId);
        entity.setAisleId(aisleId);
        entity.setRackId(rackId);
        entity.setDeleted(false);
        Bin saved = repository.save(entity);
        log.info("Created bin id={}, code={}", saved.getId(), saved.getBinCode());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    @Transactional
    public BinDto update(Long id, BinDto dto) {
        Bin entity       = findActiveOrThrow(id);
        Long warehouseId = resolveWarehouseId(dto.getWarehouseId());
        Long zoneId      = resolveZoneId(dto.getZoneId(), warehouseId);
        Long aisleId     = resolveAisleId(dto.getAisleId(), zoneId);
        Long rackId      = resolveRackId(dto.getRackId(), aisleId);
        if (repository.existsByBinCodeAndRackIdAndIdNotAndDeletedFalse(dto.getBinCode(), rackId, id)) {
            log.warn("Duplicate bin code {} on update", dto.getBinCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bin code already exists in this rack: " + dto.getBinCode());
        }
        if (dto.getBarcode() != null && repository.existsByBarcodeAndIdNotAndDeletedFalse(dto.getBarcode(), id)) {
            log.warn("Duplicate bin barcode {} on update", dto.getBarcode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bin barcode already exists: " + dto.getBarcode());
        }
        mapper.updateEntity(dto, entity);
        entity.setWarehouseId(warehouseId);
        entity.setZoneId(zoneId);
        entity.setAisleId(aisleId);
        entity.setRackId(rackId);
        Bin saved = repository.save(entity);
        log.info("Updated bin id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public BinDto findById(Long id) {
        Bin entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<BinDto> findAll(String search, Long warehouseId, Long zoneId, Long aisleId, Long rackId, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search, warehouseId, zoneId, aisleId, rackId), pageable)
                .map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    public List<DropdownOptionDto> findDropdown(String rackId) {
        Long resolvedId = rackRepository.findFirstByRackNameAndDeletedFalse(rackId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rack not found with name: " + rackId))
                .getId();
        return repository.findAllByRackIdAndDeletedFalseOrderByBinCodeAsc(resolvedId)
                .stream()
                .map(b -> new DropdownOptionDto(b.getBinCode(), b.getBinCode()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Bin entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted bin id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Bin> entities = repository.findAllById(ids).stream()
                .filter(b -> !b.isDeleted()).toList();
        entities.forEach(b -> b.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} bins", entities.size());
    }

    private Bin findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(b -> !b.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Bin not found with id: " + id));
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

    private Long resolveRackId(String name, Long aisleId) {
        return rackRepository.findByRackNameAndAisleIdAndDeletedFalse(name, aisleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rack not found with name: " + name))
                .getId();
    }

    private BinDto enrich(BinDto dto, Bin entity) {
        dto.setWarehouseId(warehouseRepository.findById(entity.getWarehouseId()).map(Warehouse::getWarehouseName).orElse(null));
        dto.setZoneId(zoneRepository.findById(entity.getZoneId()).map(Zone::getZoneName).orElse(null));
        dto.setAisleId(aisleRepository.findById(entity.getAisleId()).map(Aisle::getAisleName).orElse(null));
        dto.setRackId(rackRepository.findById(entity.getRackId()).map(Rack::getRackName).orElse(null));
        return dto;
    }

    private Specification<Bin> buildSpec(String search, Long warehouseId, Long zoneId, Long aisleId, Long rackId) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (warehouseId != null) pred = cb.and(pred, cb.equal(root.get("warehouseId"), warehouseId));
            if (zoneId != null)      pred = cb.and(pred, cb.equal(root.get("zoneId"), zoneId));
            if (aisleId != null)     pred = cb.and(pred, cb.equal(root.get("aisleId"), aisleId));
            if (rackId != null)      pred = cb.and(pred, cb.equal(root.get("rackId"), rackId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("binCode")), pattern),
                        cb.like(cb.lower(root.get("barcode")), pattern)
                ));
            }
            return pred;
        };
    }
}
