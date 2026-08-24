package com.supplychain.catalog.application.service;

import com.supplychain.catalog.application.dto.ProductDto;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductDto save(ProductDto dto);

    ProductDto update(Long id, ProductDto dto);

    ProductDto findById(Long id);

    PageResponse<ProductDto> findAll(String search, Long categoryId, Pageable pageable);

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
