package com.supplychain.tms.location.application.dto;

import com.supplychain.common.application.dto.BaseDto;
import com.supplychain.common.domain.enums.LocationType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LocationDto extends BaseDto {

    @NotBlank(message = "Location code is required")
    private String locationCode;

    @NotBlank(message = "Location name is required")
    private String locationName;

    @NotNull(message = "Location type is required")
    private LocationType locationType;

    private String locationSubType;

    private String description;

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String state;
    private String stateCode;
    private String country;
    private String countryCode;
    private String postalCode;
    private String county;

    @DecimalMin(value = "-90.0",  message = "Latitude must be >= -90")
    @DecimalMax(value =  "90.0",  message = "Latitude must be <= 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value =  "180.0", message = "Longitude must be <= 180")
    private BigDecimal longitude;

    private String timezone;
    private String timezoneCode;

    private String contactName;
    private String contactPhone;
    private String alternatePhone;
    private String contactEmail;
    private String faxNumber;

    private String operatingHours;
    private String notes;
    private boolean active;
}
