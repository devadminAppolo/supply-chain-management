package com.supplychain.outbound.picking.infrastructure.mapper;

import com.supplychain.outbound.picking.application.dto.PickingTaskResponseDto;
import com.supplychain.outbound.picking.domain.model.PickingTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PickingTaskMapper {

    /**
     * Maps core entity fields. Derived fields (orderNumber, customerCode,
     * customerName, lineNumber, quantityRemaining) are enriched by the service.
     */
    @Mapping(target = "orderNumber",      ignore = true)
    @Mapping(target = "customerCode",     ignore = true)
    @Mapping(target = "customerName",     ignore = true)
    @Mapping(target = "lineNumber",       ignore = true)
    @Mapping(target = "quantityRemaining",ignore = true)
    PickingTaskResponseDto toDto(PickingTask entity);

    List<PickingTaskResponseDto> toDtoList(List<PickingTask> entities);
}
