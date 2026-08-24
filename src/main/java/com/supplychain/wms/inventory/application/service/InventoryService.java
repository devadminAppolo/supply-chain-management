package com.supplychain.wms.inventory.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.InventoryStatus;
import com.supplychain.wms.inventory.application.dto.InventoryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {

    InventoryDto save(InventoryDto dto);

    InventoryDto update(Long id, InventoryDto dto);

    InventoryDto findById(Long id);

    PageResponse<InventoryDto> findAll(String search,
                                       Long warehouseId,
                                       Long binId,
                                       Long productId,
                                       Long skuId,
                                       InventoryStatus inventoryStatus,
                                       Pageable pageable);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
