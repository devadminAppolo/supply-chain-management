package com.supplychain.outbound.packing.infrastructure.mapper;

import com.supplychain.outbound.packing.application.dto.PackingTaskResponseDto;
import com.supplychain.outbound.packing.domain.model.PackingTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackingTaskMapper {

    /**
     * Maps core entity fields. Derived fields (orderNumber, customerCode,
     * customerName, quantityRemaining) are enriched by the service.
     */
    @Mapping(target = "orderNumber",       ignore = true)
    @Mapping(target = "customerCode",      ignore = true)
    @Mapping(target = "customerName",      ignore = true)
    @Mapping(target = "quantityRemaining", ignore = true)
    PackingTaskResponseDto toDto(PackingTask entity);

    List<PackingTaskResponseDto> toDtoList(List<PackingTask> entities);
}
