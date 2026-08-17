package com.supplychain.service;

import com.supplychain.dto.CategoryDto;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryDto save(CategoryDto dto);

    CategoryDto update(Long id, CategoryDto dto);

    CategoryDto findById(Long id);

    PageResponse<CategoryDto> findAll(String search, Pageable pageable);

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
