package com.supplychain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverDto {

    private Long id;
    private String driverNumber;
    private String driverName;
    private String licenseNumber;
    private String phoneNumber;
}
