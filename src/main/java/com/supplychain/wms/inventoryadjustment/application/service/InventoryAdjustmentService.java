package com.supplychain.wms.inventoryadjustment.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.AdjustmentType;
import com.supplychain.wms.inventoryadjustment.application.dto.InventoryAdjustmentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryAdjustmentService {

    InventoryAdjustmentDto save(InventoryAdjustmentDto dto);

    InventoryAdjustmentDto update(Long id, InventoryAdjustmentDto dto);

    InventoryAdjustmentDto findById(Long id);

    PageResponse<InventoryAdjustmentDto> findAll(String search,
                                                 Long warehouseId,
                                                 Long skuId,
                                                 Long binId,
                                                 AdjustmentType adjustmentType,
                                                 String status,
                                                 Pageable pageable);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
