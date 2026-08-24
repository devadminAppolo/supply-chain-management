package com.supplychain.catalog.application.service;

import com.supplychain.catalog.application.dto.UomDto;
import com.supplychain.catalog.domain.model.Uom;
import com.supplychain.catalog.infrastructure.mapper.UomMapper;
import com.supplychain.catalog.infrastructure.repository.UomRepository;
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
public class UomServiceImpl implements UomService {

    private final UomRepository repository;
    private final UomMapper     mapper;

    @Override
    @Transactional
    public UomDto save(UomDto dto) {
        if (repository.existsByUomCodeAndDeletedFalse(dto.getUomCode())) {
            log.warn("Duplicate UOM code: {}", dto.getUomCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "UOM code already exists: " + dto.getUomCode());
        }
        Uom entity = mapper.toEntity(dto);
        entity.setDeleted(false);
        Uom saved = repository.save(entity);
        log.info("Created UOM id={}, code={}", saved.getId(), saved.getUomCode());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public UomDto update(Long id, UomDto dto) {
        Uom entity = findActiveOrThrow(id);
        if (repository.existsByUomCodeAndIdNotAndDeletedFalse(dto.getUomCode(), id)) {
            log.warn("Duplicate UOM code {} on update", dto.getUomCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "UOM code already exists: " + dto.getUomCode());
        }
        mapper.updateEntity(dto, entity);
        Uom saved = repository.save(entity);
        log.info("Updated UOM id={}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    public UomDto findById(Long id) {
        return mapper.toDto(findActiveOrThrow(id));
    }

    @Override
    public PageResponse<UomDto> findAll(String search, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search), pageable).map(mapper::toDto));
    }

    @Override
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByUomNameAsc()
                .stream()
                .map(u -> new DropdownOptionDto(u.getUomName(), u.getUomName()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Uom entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted UOM id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Uom> entities = repository.findAllById(ids).stream()
                .filter(u -> !u.isDeleted()).toList();
        entities.forEach(u -> u.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} UOMs", entities.size());
    }

    private Uom findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "UOM not found with id: " + id));
    }

    private Specification<Uom> buildSpec(String search) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("uomCode")), pattern),
                        cb.like(cb.lower(root.get("uomName")), pattern)
                ));
            }
            return pred;
        };
    }
}
