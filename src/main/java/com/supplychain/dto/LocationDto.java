package com.supplychain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationDto {

    private String id;
    private String locationCode;
    private String locationName;
    private String city;
    private String state;
}
