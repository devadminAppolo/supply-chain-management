package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.ZoneDto;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ZoneService {

    ZoneDto save(ZoneDto dto);

    ZoneDto update(Long id, ZoneDto dto);

    ZoneDto findById(Long id);

    PageResponse<ZoneDto> findAll(String search, Long warehouseId, Pageable pageable);

    List<DropdownOptionDto> findDropdown(String warehouseId);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
