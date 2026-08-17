package com.supplychain.service;

import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.ProductDto;
import com.supplychain.entity.Category;
import com.supplychain.entity.Product;
import com.supplychain.entity.Uom;
import com.supplychain.mapper.ProductMapper;
import com.supplychain.repository.CategoryRepository;
import com.supplychain.repository.ProductRepository;
import com.supplychain.repository.UomRepository;
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
public class ProductServiceImpl implements ProductService {

    private final ProductRepository  repository;
    private final ProductMapper      mapper;
    private final CategoryRepository categoryRepository;
    private final UomRepository      uomRepository;

    @Override
    @Transactional
    public ProductDto save(ProductDto dto) {
        if (repository.existsByProductCodeAndDeletedFalse(dto.getProductCode())) {
            log.warn("Duplicate product code: {}", dto.getProductCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product code already exists: " + dto.getProductCode());
        }
        if (dto.getBarcode() != null && repository.existsByBarcodeAndDeletedFalse(dto.getBarcode())) {
            log.warn("Duplicate product barcode: {}", dto.getBarcode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product barcode already exists: " + dto.getBarcode());
        }
        Long categoryId = resolveCategoryId(dto.getCategoryId());
        Long uomId      = resolveUomId(dto.getUomId());
        Product entity = mapper.toEntity(dto);
        entity.setCategoryId(categoryId);
        entity.setUomId(uomId);
        entity.setDeleted(false);
        Product saved = repository.save(entity);
        log.info("Created product id={}, code={}", saved.getId(), saved.getProductCode());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product entity = findActiveOrThrow(id);
        if (repository.existsByProductCodeAndIdNotAndDeletedFalse(dto.getProductCode(), id)) {
            log.warn("Duplicate product code {} on update", dto.getProductCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product code already exists: " + dto.getProductCode());
        }
        if (dto.getBarcode() != null && repository.existsByBarcodeAndIdNotAndDeletedFalse(dto.getBarcode(), id)) {
            log.warn("Duplicate product barcode {} on update", dto.getBarcode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Product barcode already exists: " + dto.getBarcode());
        }
        Long categoryId = resolveCategoryId(dto.getCategoryId());
        Long uomId      = resolveUomId(dto.getUomId());
        mapper.updateEntity(dto, entity);
        entity.setCategoryId(categoryId);
        entity.setUomId(uomId);
        Product saved = repository.save(entity);
        log.info("Updated product id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public ProductDto findById(Long id) {
        Product entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<ProductDto> findAll(String search, Long categoryId, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search, categoryId), pageable)
                .map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    public List<DropdownOptionDto> findDropdown() {
        return repository.findAllByDeletedFalseOrderByProductNameAsc()
                .stream()
                .map(p -> new DropdownOptionDto(p.getProductName(), p.getProductName()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted product id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Product> entities = repository.findAllById(ids).stream()
                .filter(p -> !p.isDeleted()).toList();
        entities.forEach(p -> p.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} products", entities.size());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Product findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id));
    }

    private Long resolveCategoryId(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return null;
        return categoryRepository.findByCategoryNameAndDeletedFalse(categoryName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category not found with name: " + categoryName))
                .getId();
    }

    private Long resolveUomId(String uomName) {
        if (uomName == null || uomName.isBlank()) return null;
        return uomRepository.findByUomNameAndDeletedFalse(uomName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "UOM not found with name: " + uomName))
                .getId();
    }

    private ProductDto enrich(ProductDto dto, Product entity) {
        if (entity.getCategoryId() != null) {
            categoryRepository.findById(entity.getCategoryId())
                    .ifPresent(c -> dto.setCategoryId(c.getCategoryName()));
        }
        if (entity.getUomId() != null) {
            uomRepository.findById(entity.getUomId())
                    .ifPresent(u -> dto.setUomId(u.getUomName()));
        }
        return dto;
    }

    private Specification<Product> buildSpec(String search, Long categoryId) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (categoryId != null) pred = cb.and(pred, cb.equal(root.get("categoryId"), categoryId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("productCode")), pattern),
                        cb.like(cb.lower(root.get("productName")), pattern)
                ));
            }
            return pred;
        };
    }
}
