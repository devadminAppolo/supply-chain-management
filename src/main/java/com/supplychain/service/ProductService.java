package com.supplychain.service;

import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.ProductDto;
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
