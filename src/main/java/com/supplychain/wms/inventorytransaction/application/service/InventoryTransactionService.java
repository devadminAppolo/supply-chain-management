package com.supplychain.wms.inventorytransaction.application.service;

import com.supplychain.common.application.dto.PageResponse;
import com.supplychain.common.domain.enums.TransactionType;
import com.supplychain.wms.inventorytransaction.application.dto.InventoryTransactionDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryTransactionService {

    InventoryTransactionDto save(InventoryTransactionDto dto);

    InventoryTransactionDto update(Long id, InventoryTransactionDto dto);

    InventoryTransactionDto findById(Long id);

    PageResponse<InventoryTransactionDto> findAll(String search,
                                                  Long warehouseId,
                                                  Long inventoryId,
                                                  Long skuId,
                                                  TransactionType transactionType,
                                                  Pageable pageable);

    void deleteById(Long id);

    void deleteAllByIds(List<Long> ids);
}
