package com.supplychain.wms.inventorytransaction.infrastructure.mapper;

import com.supplychain.wms.inventorytransaction.application.dto.InventoryTransactionDto;
import com.supplychain.wms.inventorytransaction.domain.model.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "productId",   ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    @Mapping(target = "fromBinId",   ignore = true)
    @Mapping(target = "toBinId",     ignore = true)
    InventoryTransaction toEntity(InventoryTransactionDto dto);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "productId",   ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    @Mapping(target = "fromBinId",   ignore = true)
    @Mapping(target = "toBinId",     ignore = true)
    InventoryTransactionDto toDto(InventoryTransaction entity);

    List<InventoryTransactionDto> toDtoList(List<InventoryTransaction> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "productId",   ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    @Mapping(target = "fromBinId",   ignore = true)
    @Mapping(target = "toBinId",     ignore = true)
    void updateEntity(InventoryTransactionDto dto, @MappingTarget InventoryTransaction entity);
}
