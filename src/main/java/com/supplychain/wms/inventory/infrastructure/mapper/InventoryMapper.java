package com.supplychain.wms.inventory.infrastructure.mapper;

import com.supplychain.wms.inventory.application.dto.InventoryDto;
import com.supplychain.wms.inventory.domain.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "binId",       ignore = true)
    @Mapping(target = "productId",   ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    Inventory toEntity(InventoryDto dto);

    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "binId",       ignore = true)
    @Mapping(target = "productId",   ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    InventoryDto toDto(Inventory entity);

    List<InventoryDto> toDtoList(List<Inventory> entities);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "createdBy",   ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy",  ignore = true)
    @Mapping(target = "modifiedDate",ignore = true)
    @Mapping(target = "deleted",     ignore = true)
    @Mapping(target = "warehouseId", ignore = true)
    @Mapping(target = "binId",       ignore = true)
    @Mapping(target = "productId",   ignore = true)
    @Mapping(target = "skuId",       ignore = true)
    @Mapping(target = "uomId",       ignore = true)
    void updateEntity(InventoryDto dto, @MappingTarget Inventory entity);
}
