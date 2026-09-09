package com.supplychain.wms.pallet.infrastructure.mapper;

import com.supplychain.wms.pallet.application.dto.CreatePalletRequestDto;
import com.supplychain.wms.pallet.application.dto.PalletResponseDto;
import com.supplychain.wms.pallet.application.dto.UpdatePalletRequestDto;
import com.supplychain.wms.pallet.domain.model.Pallet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PalletMapper {

    PalletResponseDto toDto(Pallet entity);

    List<PalletResponseDto> toDtoList(List<Pallet> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "palletNumber", ignore = true)
    @Mapping(target = "lpnNumber", ignore = true)
    @Mapping(target = "palletStatus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Pallet toEntity(CreatePalletRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "palletNumber", ignore = true)
    @Mapping(target = "lpnNumber", ignore = true)
    @Mapping(target = "palletStatus", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UpdatePalletRequestDto dto, @MappingTarget Pallet entity);
}
