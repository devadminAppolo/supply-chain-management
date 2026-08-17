package com.supplychain.service;

import com.supplychain.dto.CategoryDto;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.entity.Category;
import com.supplychain.mapper.CategoryMapper;
import com.supplychain.repository.CategoryRepository;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper     mapper;

    @Override
    @Transactional
    public CategoryDto save(CategoryDto dto) {
        if (repository.existsByCategoryCodeAndDeletedFalse(dto.getCategoryCode())) {
            log.warn("Duplicate category code: {}", dto.getCategoryCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Category code already exists: " + dto.getCategoryCode());
        }
        Long parentId = resolveParentCategoryId(dto.getParentCategoryId());
        Category entity = mapper.toEntity(dto);
        entity.setParentCategoryId(parentId);
        entity.setDeleted(false);
        Category saved = repository.save(entity);
        log.info("Created category id={}, code={}", saved.getId(), saved.getCategoryCode());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto dto) {
        Category entity = findActiveOrThrow(id);
        if (repository.existsByCategoryCodeAndIdNotAndDeletedFalse(dto.getCategoryCode(), id)) {
            log.warn("Duplicate category code {} on update", dto.getCategoryCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Category code already exists: " + dto.getCategoryCode());
        }
        if (dto.getParentCategoryId() != null) {
            Long parentId = resolveParentCategoryId(dto.getParentCategoryId());
            if (parentId != null && parentId.equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category cannot be its own parent");
            }
            mapper.updateEntity(dto, entity);
            entity.setParentCategoryId(parentId);
        } else {
            mapper.updateEntity(dto, entity);
            entity.setParentCategoryId(null);
        }
        Category saved = repository.save(entity);
        log.info("Updated category id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public CategoryDto findById(Long id) {
        Category entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<CategoryDto> findAll(String search, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search), pageable)
                .map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByCategoryNameAsc()
                .stream()
                .map(c -> new DropdownOptionDto(c.getCategoryName(), c.getCategoryName()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Category entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted category id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Category> entities = repository.findAllById(ids).stream()
                .filter(c -> !c.isDeleted()).toList();
        entities.forEach(c -> c.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} categories", entities.size());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Category findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Category not found with id: " + id));
    }

    /**
     * Resolves an optional parent category name to its Long ID.
     * Returns null if the input is null/blank (no parent).
     */
    private Long resolveParentCategoryId(String parentName) {
        if (parentName == null || parentName.isBlank()) return null;
        return repository.findByCategoryNameAndDeletedFalse(parentName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Parent category not found with name: " + parentName))
                .getId();
    }

    private CategoryDto enrich(CategoryDto dto, Category entity) {
        if (entity.getParentCategoryId() != null) {
            repository.findById(entity.getParentCategoryId())
                    .ifPresent(p -> dto.setParentCategoryId(p.getCategoryName()));
        }
        return dto;
    }

    private Specification<Category> buildSpec(String search) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("categoryCode")), pattern),
                        cb.like(cb.lower(root.get("categoryName")), pattern)
                ));
            }
            return pred;
        };
    }
}
