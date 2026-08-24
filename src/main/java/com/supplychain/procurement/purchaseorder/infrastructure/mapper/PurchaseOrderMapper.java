package com.supplychain.procurement.purchaseorder.infrastructure.mapper;

import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderDto;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PurchaseOrderLineItemMapper.class})
public interface PurchaseOrderMapper {

    @Mapping(target = "lineItems", ignore = true)
    PurchaseOrder toEntity(PurchaseOrderDto dto);

    PurchaseOrderDto toDto(PurchaseOrder entity);

    List<PurchaseOrderDto> toDtoList(List<PurchaseOrder> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "lineItems",    ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    void updateEntity(PurchaseOrderDto dto, @MappingTarget PurchaseOrder entity);
}
