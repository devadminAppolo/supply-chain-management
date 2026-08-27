package com.supplychain.procurement.asn.infrastructure.mapper;

import com.supplychain.procurement.asn.application.dto.AsnLineRequestDto;
import com.supplychain.procurement.asn.application.dto.AsnLineResponseDto;
import com.supplychain.procurement.asn.application.dto.AsnLineUpdateRequestDto;
import com.supplychain.procurement.asn.domain.model.AdvanceShippingNoticeLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdvanceShippingNoticeLineMapper {

    @Mapping(target = "id",                         ignore = true)
    @Mapping(target = "orgId",                      ignore = true)
    @Mapping(target = "dcId",                       ignore = true)
    @Mapping(target = "status",                     ignore = true)
    @Mapping(target = "remarks",                    ignore = true)
    @Mapping(target = "createdBy",                  ignore = true)
    @Mapping(target = "createdDate",                ignore = true)
    @Mapping(target = "modifiedBy",                 ignore = true)
    @Mapping(target = "modifiedDate",               ignore = true)
    @Mapping(target = "deleted",                    ignore = true)
    @Mapping(target = "advanceShippingNotice",      ignore = true)
    @Mapping(target = "purchaseOrderLine",          ignore = true)
    @Mapping(target = "lineNumber",                 ignore = true)
    @Mapping(target = "itemCode",                   ignore = true)
    @Mapping(target = "itemName",                   ignore = true)
    @Mapping(target = "supplierItemCode",           ignore = true)
    @Mapping(target = "itemGtin",                   ignore = true)
    @Mapping(target = "quantityOrdered",            ignore = true)
    @Mapping(target = "previouslyShippedQuantity",  ignore = true)
    @Mapping(target = "remainingQuantity",          ignore = true)
    @Mapping(target = "quantityUom",                ignore = true)
    @Mapping(target = "unitPrice",                  ignore = true)
    @Mapping(target = "currencyCode",               ignore = true)
    @Mapping(target = "totalMonetaryValue",         ignore = true)
    @Mapping(target = "lineStatus",                 ignore = true)
    AdvanceShippingNoticeLine toEntity(AsnLineRequestDto dto);

    @Mapping(target = "asnId", source = "advanceShippingNotice.id")
    @Mapping(target = "poLineId", source = "purchaseOrderLine.id")
    AsnLineResponseDto toDto(AdvanceShippingNoticeLine entity);

    List<AsnLineResponseDto> toDtoList(List<AdvanceShippingNoticeLine> entities);

    @Mapping(target = "id",                         ignore = true)
    @Mapping(target = "orgId",                      ignore = true)
    @Mapping(target = "dcId",                       ignore = true)
    @Mapping(target = "status",                     ignore = true)
    @Mapping(target = "remarks",                    ignore = true)
    @Mapping(target = "createdBy",                  ignore = true)
    @Mapping(target = "createdDate",                ignore = true)
    @Mapping(target = "modifiedBy",                 ignore = true)
    @Mapping(target = "modifiedDate",               ignore = true)
    @Mapping(target = "deleted",                    ignore = true)
    @Mapping(target = "advanceShippingNotice",      ignore = true)
    @Mapping(target = "purchaseOrderLine",          ignore = true)
    @Mapping(target = "lineNumber",                 ignore = true)
    @Mapping(target = "itemCode",                   ignore = true)
    @Mapping(target = "itemName",                   ignore = true)
    @Mapping(target = "supplierItemCode",           ignore = true)
    @Mapping(target = "itemGtin",                   ignore = true)
    @Mapping(target = "quantityOrdered",            ignore = true)
    @Mapping(target = "previouslyShippedQuantity",  ignore = true)
    @Mapping(target = "remainingQuantity",          ignore = true)
    @Mapping(target = "quantityUom",                ignore = true)
    @Mapping(target = "unitPrice",                  ignore = true)
    @Mapping(target = "currencyCode",               ignore = true)
    @Mapping(target = "totalMonetaryValue",         ignore = true)
    @Mapping(target = "lineStatus",                 ignore = true)
    void updateEntity(AsnLineUpdateRequestDto dto, @MappingTarget AdvanceShippingNoticeLine entity);
}
