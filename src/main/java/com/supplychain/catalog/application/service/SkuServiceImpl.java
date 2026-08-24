package com.supplychain.catalog.application.service;

import com.supplychain.catalog.application.dto.SkuDto;
import com.supplychain.catalog.domain.model.Sku;
import com.supplychain.catalog.infrastructure.mapper.SkuMapper;
import com.supplychain.catalog.infrastructure.repository.ProductRepository;
import com.supplychain.catalog.infrastructure.repository.SkuRepository;
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
public class SkuServiceImpl implements SkuService {

    private final SkuRepository     repository;
    private final SkuMapper         mapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public SkuDto save(SkuDto dto) {
        Long productId = resolveProductId(dto.getProductId());
        if (repository.existsBySkuCodeAndProductIdAndDeletedFalse(dto.getSkuCode(), productId)) {
            log.warn("Duplicate SKU code {} for product {}", dto.getSkuCode(), productId);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "SKU code already exists for this product: " + dto.getSkuCode());
        }
        if (dto.getUpc() != null && repository.existsByUpcAndDeletedFalse(dto.getUpc())) {
            log.warn("Duplicate SKU UPC: {}", dto.getUpc());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "UPC already exists: " + dto.getUpc());
        }
        Sku entity = mapper.toEntity(dto);
        entity.setProductId(productId);
        entity.setDeleted(false);
        Sku saved = repository.save(entity);
        log.info("Created SKU id={}, code={}", saved.getId(), saved.getSkuCode());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    @Transactional
    public SkuDto update(Long id, SkuDto dto) {
        Sku entity   = findActiveOrThrow(id);
        Long productId = resolveProductId(dto.getProductId());
        if (repository.existsBySkuCodeAndProductIdAndIdNotAndDeletedFalse(dto.getSkuCode(), productId, id)) {
            log.warn("Duplicate SKU code {} on update", dto.getSkuCode());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "SKU code already exists for this product: " + dto.getSkuCode());
        }
        if (dto.getUpc() != null && repository.existsByUpcAndIdNotAndDeletedFalse(dto.getUpc(), id)) {
            log.warn("Duplicate SKU UPC {} on update", dto.getUpc());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "UPC already exists: " + dto.getUpc());
        }
        mapper.updateEntity(dto, entity);
        entity.setProductId(productId);
        Sku saved = repository.save(entity);
        log.info("Updated SKU id={}", saved.getId());
        return enrich(mapper.toDto(saved), saved);
    }

    @Override
    public SkuDto findById(Long id) {
        Sku entity = findActiveOrThrow(id);
        return enrich(mapper.toDto(entity), entity);
    }

    @Override
    public PageResponse<SkuDto> findAll(String search, Long productId, Pageable pageable) {
        return PageResponse.of(repository.findAll(buildSpec(search, productId), pageable)
                .map(e -> enrich(mapper.toDto(e), e)));
    }

    @Override
    public List<DropdownOptionDto> findDropdown(Long productId) {
        List<Sku> skus = productId != null
                ? repository.findAllByProductIdAndDeletedFalseOrderBySkuCodeAsc(productId)
                : repository.findAllByDeletedFalseOrderBySkuCodeAsc();
        return skus.stream()
                .map(s -> new DropdownOptionDto(s.getSkuCode(), s.getSkuCode()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Sku entity = findActiveOrThrow(id);
        entity.setDeleted(true);
        repository.save(entity);
        log.info("Soft-deleted SKU id={}", id);
    }

    @Override
    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        List<Sku> entities = repository.findAllById(ids).stream()
                .filter(s -> !s.isDeleted()).toList();
        entities.forEach(s -> s.setDeleted(true));
        repository.saveAll(entities);
        log.info("Soft-deleted {} SKUs", entities.size());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Sku findActiveOrThrow(Long id) {
        return repository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "SKU not found with id: " + id));
    }

    private Long resolveProductId(String productName) {
        return productRepository.findByProductNameAndDeletedFalse(productName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product not found with name: " + productName))
                .getId();
    }

    private SkuDto enrich(SkuDto dto, Sku entity) {
        productRepository.findById(entity.getProductId())
                .ifPresent(p -> dto.setProductId(p.getProductName()));
        return dto;
    }

    private Specification<Sku> buildSpec(String search, Long productId) {
        return (root, query, cb) -> {
            var pred = cb.equal(root.get("deleted"), false);
            if (productId != null) pred = cb.and(pred, cb.equal(root.get("productId"), productId));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                pred = cb.and(pred, cb.or(
                        cb.like(cb.lower(root.get("skuCode")), pattern),
                        cb.like(cb.lower(root.get("upc")), pattern)
                ));
            }
            return pred;
        };
    }
}
