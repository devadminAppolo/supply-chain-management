package com.supplychain.wms.location.application.service;

import com.supplychain.wms.location.application.dto.BinDto;
import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
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
