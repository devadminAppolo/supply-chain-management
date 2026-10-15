package com.supplychain.customer.infrastructure.mapper;

import com.supplychain.customer.application.dto.CustomerRequestDto;
import com.supplychain.customer.application.dto.CustomerResponseDto;
import com.supplychain.customer.application.dto.CustomerUpdateDto;
import com.supplychain.customer.domain.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * Map create request to a new entity.
     * The {@code status} field on CustomerRequestDto is a {@link com.supplychain.customer.domain.enums.CustomerStatus}
     * enum; the service converts it to a String and sets it on the entity via BaseEntity.setStatus().
     */
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true)   // set by service from enum
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    @Mapping(target = "orgId",        ignore = true)
    @Mapping(target = "dcId",         ignore = true)
    @Mapping(target = "remarks",      ignore = true)
    Customer toEntity(CustomerRequestDto dto);

    CustomerResponseDto toDto(Customer entity);

    List<CustomerResponseDto> toDtoList(List<Customer> entities);

    /**
     * Apply mutable fields from an update request onto a managed entity.
     * Audit and identity fields are managed by JPA auditing / the service.
     */
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "status",       ignore = true)   // set by service from enum
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "createdDate",  ignore = true)
    @Mapping(target = "modifiedBy",   ignore = true)
    @Mapping(target = "modifiedDate", ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    @Mapping(target = "orgId",        ignore = true)
    @Mapping(target = "dcId",         ignore = true)
    @Mapping(target = "remarks",      ignore = true)
    void updateEntity(CustomerUpdateDto dto, @MappingTarget Customer entity);
}
