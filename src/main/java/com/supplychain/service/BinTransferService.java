package com.supplychain.service;

import com.supplychain.dto.BinTransferDto;
import com.supplychain.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BinTransferService {

    /** Validates, executes the transfer, and returns the completed transfer record. */
    BinTransferDto transfer(BinTransferDto dto);

    BinTransferDto findById(Long id);

    PageResponse<BinTransferDto> findAll(String search,
                                         Long sourceWarehouseId,
                                         Long sourceBinId,
                                         Long skuId,
                                         Pageable pageable);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
