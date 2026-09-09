package com.supplychain.wms.receiving.infrastructure.mapper;

import com.supplychain.wms.receiving.application.dto.ReceivingPalletResponseDto;
import com.supplychain.wms.receiving.domain.model.ReceivingPallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReceivingPalletMapper {

    @Mapping(target = "receivingId", source = "receiving.id")
    @Mapping(target = "asnId", source = "advanceShippingNotice.id")
    @Mapping(target = "asnLineId", source = "advanceShippingNoticeLine.id")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "poLineId", source = "purchaseOrderLine.id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "skuId", source = "skuId")
    @Mapping(target = "uomId", source = "uomId")
    @Mapping(target = "stagingLocationId", source = "stagingBinId")
    @Mapping(target = "stagingBinCode", ignore = true)
    @Mapping(target = "currentLocationId", source = "currentBinId")
    @Mapping(target = "currentBinCode", ignore = true)
    ReceivingPalletResponseDto toDto(ReceivingPallet entity);

    List<ReceivingPalletResponseDto> toDtoList(List<ReceivingPallet> entities);
}
