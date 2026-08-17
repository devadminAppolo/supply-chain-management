package com.supplychain.service;

import com.supplychain.dto.AisleDto;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AisleService {

    AisleDto save(AisleDto dto);

    AisleDto update(Long id, AisleDto dto);

    AisleDto findById(Long id);

    PageResponse<AisleDto> findAll(String search, Long warehouseId, Long zoneId, Pageable pageable);

    List<DropdownOptionDto> findDropdown(String zoneId);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
