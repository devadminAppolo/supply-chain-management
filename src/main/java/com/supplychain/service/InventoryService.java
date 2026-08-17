package com.supplychain.service;

import com.supplychain.dto.InventoryDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.enums.InventoryStatus;
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
