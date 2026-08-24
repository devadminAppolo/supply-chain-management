package com.supplychain.catalog.application.service;

import com.supplychain.catalog.application.dto.UomDto;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UomService {

    UomDto save(UomDto dto);

    UomDto update(Long id, UomDto dto);

    UomDto findById(Long id);

    PageResponse<UomDto> findAll(String search, Pageable pageable);

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
