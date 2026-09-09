package com.supplychain.wms.receiving.infrastructure.mapper;

import com.supplychain.wms.receiving.application.dto.ReceivingResponseDto;
import com.supplychain.wms.receiving.domain.model.Receiving;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ReceivingPalletMapper.class})
public interface ReceivingMapper {

    @Mapping(target = "asnId", source = "advanceShippingNotice.id")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "pallets", source = "pallets")
    ReceivingResponseDto toDto(Receiving entity);

    List<ReceivingResponseDto> toDtoList(List<Receiving> entities);
}
