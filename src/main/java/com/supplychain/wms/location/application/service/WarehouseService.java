package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.WarehouseDto;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
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
