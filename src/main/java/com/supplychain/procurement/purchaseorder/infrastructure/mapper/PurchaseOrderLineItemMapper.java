package com.supplychain.procurement.purchaseorder.infrastructure.mapper;

import com.supplychain.procurement.purchaseorder.application.dto.PurchaseOrderLineItemDto;
import com.supplychain.procurement.purchaseorder.domain.model.PurchaseOrderLineItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseOrderLineItemMapper {

    @Mapping(target = "purchaseOrder", ignore = true)
    PurchaseOrderLineItem toEntity(PurchaseOrderLineItemDto dto);

    PurchaseOrderLineItemDto toDto(PurchaseOrderLineItem entity);

    List<PurchaseOrderLineItemDto> toDtoList(List<PurchaseOrderLineItem> entities);

    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "purchaseOrder",ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    void updateEntity(PurchaseOrderLineItemDto dto, @MappingTarget PurchaseOrderLineItem entity);
}
