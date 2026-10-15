package com.supplychain.customer.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

/**
 * Response DTO for Customer Master records.
 * Extends {@link BaseDto} which exposes id, audit fields, and the generic status column.
 */
@Getter
@Setter
public class CustomerResponseDto extends BaseDto {

    private String customerCode;
    private String customerName;

    private String contactName;
    private String email;
    private String phoneNumber;

    private String address;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
