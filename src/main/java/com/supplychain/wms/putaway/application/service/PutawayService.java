package com.supplychain.wms.putaway.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.putaway.application.dto.AssignStaffRequestDto;
import com.supplychain.wms.putaway.application.dto.CompletePutawayRequestDto;
import com.supplychain.wms.putaway.application.dto.PutawayTaskDto;
import com.supplychain.wms.putaway.domain.enums.PutawayStatus;
import org.springframework.data.domain.Pageable;

public interface PutawayService {

    PageResponse<PutawayTaskDto> findAll(String search, PutawayStatus status, Long warehouseId, String staffId, Pageable pageable);

    PutawayTaskDto findById(Long id);

    PutawayTaskDto assignStaff(Long id, AssignStaffRequestDto request);

    PutawayTaskDto startPutaway(Long id);

    PutawayTaskDto completePutaway(Long id, CompletePutawayRequestDto request);
}
