package com.supplychain.wms.inventoryadjustment.infrastructure.mapper;

import com.supplychain.wms.inventoryadjustment.application.dto.InventoryAdjustmentDto;
import com.supplychain.wms.inventoryadjustment.domain.model.InventoryAdjustment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryAdjustmentMapper {

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "binId",       ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    InventoryAdjustment toEntity(InventoryAdjustmentDto dto);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "binId",       ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    InventoryAdjustmentDto toDto(InventoryAdjustment entity);

    List<InventoryAdjustmentDto> toDtoList(List<InventoryAdjustment> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "binId",       ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    void updateEntity(InventoryAdjustmentDto dto, @MappingTarget InventoryAdjustment entity);
}
