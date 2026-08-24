package com.supplychain.wms.bintransfer.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.wms.bintransfer.application.dto.BinTransferDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BinTransferService {

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
