package com.supplychain.service;

import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.dto.RackDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RackService {

    RackDto save(RackDto dto);

    RackDto update(Long id, RackDto dto);

    RackDto findById(Long id);

    PageResponse<RackDto> findAll(String search, Long warehouseId, Long zoneId, Long aisleId, Pageable pageable);

    List<DropdownOptionDto> findDropdown(String aisleId);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
