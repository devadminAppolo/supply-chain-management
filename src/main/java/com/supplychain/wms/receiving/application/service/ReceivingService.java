package com.supplychain.wms.receiving.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.receiving.application.dto.AsnReceivingDetailsDto;
import com.supplychain.wms.receiving.application.dto.ReceivingCreateRequestDto;
import com.supplychain.wms.receiving.application.dto.ReceivingResponseDto;
import com.supplychain.wms.receiving.domain.enums.ReceivingStatus;
import org.springframework.data.domain.Pageable;

public interface ReceivingService {

    AsnReceivingDetailsDto getAsnReceivingDetails(Long asnId);

    ReceivingResponseDto createReceiving(ReceivingCreateRequestDto request);

    ReceivingResponseDto completeReceiving(Long receivingId);

    ReceivingResponseDto findById(Long id);

    PageResponse<ReceivingResponseDto> findAll(String search, ReceivingStatus status, Long warehouseId, Pageable pageable);
}
