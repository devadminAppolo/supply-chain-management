package com.supplychain.mapper;

import com.supplychain.dto.BinTransferDto;
import com.supplychain.entity.BinTransfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BinTransferMapper {

    // All FK ID fields (Long in entity, String in DTO) are resolved manually
    // in the service layer — mapper ignores them to avoid type-mismatch errors.

    @Mapping(target = "sourceWarehouseId",      ignore = true)
    @Mapping(target = "sourceBinId",            ignore = true)
    @Mapping(target = "destinationWarehouseId", ignore = true)
    @Mapping(target = "destinationBinId",       ignore = true)
    @Mapping(target = "skuId",                  ignore = true)
    @Mapping(target = "uomId",                  ignore = true)
    BinTransfer toEntity(BinTransferDto dto);

    @Mapping(target = "sourceWarehouseId",      ignore = true)
    @Mapping(target = "sourceBinId",            ignore = true)
    @Mapping(target = "destinationWarehouseId", ignore = true)
    @Mapping(target = "destinationBinId",       ignore = true)
    @Mapping(target = "skuId",                  ignore = true)
    @Mapping(target = "uomId",                  ignore = true)
    BinTransferDto toDto(BinTransfer entity);

    List<BinTransferDto> toDtoList(List<BinTransfer> entities);

    @Mapping(target = "id",                     ignore = true)
    @Mapping(target = "createdBy",              ignore = true)
    @Mapping(target = "createdDate",            ignore = true)
    @Mapping(target = "modifiedBy",             ignore = true)
    @Mapping(target = "modifiedDate",           ignore = true)
    @Mapping(target = "deleted",                ignore = true)
    @Mapping(target = "sourceWarehouseId",      ignore = true)
    @Mapping(target = "sourceBinId",            ignore = true)
    @Mapping(target = "destinationWarehouseId", ignore = true)
    @Mapping(target = "destinationBinId",       ignore = true)
    @Mapping(target = "skuId",                  ignore = true)
    @Mapping(target = "uomId",                  ignore = true)
    void updateEntity(BinTransferDto dto, @MappingTarget BinTransfer entity);
}
