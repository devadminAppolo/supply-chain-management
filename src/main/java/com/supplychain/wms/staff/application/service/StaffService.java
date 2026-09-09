package com.supplychain.wms.staff.application.service;

import com.supplychain.common.application.dto.DropdownOptionDto;
import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.staff.application.dto.StaffDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffService {

    StaffDto save(StaffDto dto);

    StaffDto update(Long id, StaffDto dto);

    StaffDto findById(Long id);

    PageResponse<StaffDto> findAll(String search, String status, Long orgId, Long dcId, Pageable pageable);

    List<DropdownOptionDto> findDropdown(Long orgId, Long dcId);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
