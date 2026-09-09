package com.supplychain.wms.putaway.infrastructure.mapper;

import com.supplychain.wms.putaway.application.dto.PutawayTaskDto;
import com.supplychain.wms.putaway.domain.model.PutawayTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PutawayTaskMapper {

    @Mapping(target = "receivingId", source = "receiving.id")
    @Mapping(target = "receivingNumber", source = "receiving.receivingNumber")
    @Mapping(target = "palletId", source = "pallet.id")
    @Mapping(target = "palletNumber", source = "pallet.palletNumber")
    @Mapping(target = "lpnNumber", source = "pallet.lpnNumber")
    @Mapping(target = "asnId", source = "advanceShippingNotice.id")
    @Mapping(target = "asnNumber", source = "advanceShippingNotice.asnNumber")
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "purchaseOrderNumber", source = "purchaseOrder.poNumber")
    @Mapping(target = "warehouseId", source = "warehouse.id")
    @Mapping(target = "warehouseCode", source = "warehouse.warehouseCode")
    @Mapping(target = "warehouseName", source = "warehouse.warehouseName")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "skuId", source = "skuId")
    @Mapping(target = "uomId", source = "uomId")
    @Mapping(target = "stagingLocationId", source = "stagingBinId")
    @Mapping(target = "stagingBinCode", ignore = true)
    @Mapping(target = "putawayLocationId", source = "putawayBinId")
    @Mapping(target = "putawayBinCode", ignore = true)
    PutawayTaskDto toDto(PutawayTask entity);

    List<PutawayTaskDto> toDtoList(List<PutawayTask> entities);
}
