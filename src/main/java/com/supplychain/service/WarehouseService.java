package com.supplychain.service;

import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.WarehouseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarehouseService {

    WarehouseDto save(WarehouseDto dto);

    WarehouseDto update(Long id, WarehouseDto dto);

    WarehouseDto findById(Long id);

    PageResponse<WarehouseDto> findAll(String search, Pageable pageable);

    List<DropdownOptionDto> findDropdown();

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
