package com.supplychain.service;

import com.supplychain.dto.BinDto;
import com.supplychain.dto.DropdownOptionDto;
import com.supplychain.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BinService {

    BinDto save(BinDto dto);

    BinDto update(Long id, BinDto dto);

    BinDto findById(Long id);

    PageResponse<BinDto> findAll(String search, Long warehouseId, Long zoneId, Long aisleId, Long rackId, Pageable pageable);

    List<DropdownOptionDto> findDropdown(String rackId);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
