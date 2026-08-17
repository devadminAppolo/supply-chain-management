package com.supplychain.service;

import com.supplychain.dto.InventoryTransactionDto;
import com.supplychain.dto.PageResponse;
import com.supplychain.enums.TransactionType;
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
