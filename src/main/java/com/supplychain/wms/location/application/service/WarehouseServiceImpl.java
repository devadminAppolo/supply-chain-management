package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.WarehouseDto;
import com.supplychain.wms.location.domain.model.Warehouse;
import com.supplychain.wms.location.infrastructure.mapper.WarehouseMapper;
import com.supplychain.wms.location.infrastructure.repository.WarehouseRepository;
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
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;
    private final WarehouseMapper     mapper;

    @Override
    @Transactional
    public WarehouseDto save(WarehouseDto dto) {
        if (repository.existsByWarehouseCodeAndDeletedFalse(dto.getWarehouseCode())) {
            log.warn("Duplicate warehouse code: {}", dto.getWarehouseCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Warehouse code already exists: " + dto.getWarehouseCode());
        }
        Warehouse entity = mapper.toEntity(dto);
        entity.setDeleted(false);
        Warehouse saved = repository.save(entity);
        log.info("Created warehouse id={}, code={}", saved.getId(), saved.getWarehouseCode());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public WarehouseDto update(Long id, WarehouseDto dto) {
        Warehouse entity = findActiveOrThrow(id);
        if (repository.existsByWarehouseCodeAndIdNotAndDeletedFalse(dto.getWarehouseCode(), id)) {
            log.warn("Duplicate warehouse code on update: {}", dto.getWarehouseCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Warehouse code already exists: " + dto.getWarehouseCode());
        }
        mapper.updateEntity(dto, entity);
        Warehouse saved = repository.save(entity);
        log.info("Updated warehouse id={}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public WarehouseDto findById(Long id) {
        return mapper.toDto(findActiveOrThrow(id));
    }

    @Override
    public PageResponse<WarehouseDto> findAll(String search, Pageable pageable) {
        Specification<Warehouse> spec = buildSpec(search);
        return PageResponse.of(repository.findAll(spec, pageable).map(mapper::toDto));
    }

    @Override
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByWarehouseNameAsc()
                .stream()
                .map(w -> new DropdownOptionDto(w.getWarehouseName(), w.getWarehouseName()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Warehouse entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted warehouse id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Warehouse> entities = repository.findAllById(ids).stream()
                .filter(w -> !w.isDeleted())
                .toList();
        entities.forEach(w -> w.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} warehouses", entities.size());
    }

    private Warehouse findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(w -> !w.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Warehouse not found with id: " + id));
    }

    private Specification<Warehouse> buildSpec(String search) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                var searchPred = cb.or(
                        cb.like(cb.lower(root.get("warehouseCode")), pattern),
                        cb.like(cb.lower(root.get("warehouseName")), pattern)
                );
                return cb.and(pred, searchPred);
            }
            return pred;
        };
    }
}
