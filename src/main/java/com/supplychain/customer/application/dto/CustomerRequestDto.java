package com.supplychain.customer.application.dto;

import com.supplychain.customer.domain.enums.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for creating a new Customer.
 */
@Getter
@Setter
public class CustomerRequestDto {

    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Status is required")
    private CustomerStatus status;

    private String contactName;

    @Email(message = "Invalid email format")
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
