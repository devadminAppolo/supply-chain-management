package com.supplychain.wms.putaway.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompletePutawayRequestDto {

    @NotNull(message = "Destination putaway location (bin) ID is required")
    private Long putawayLocationId;

    private String notes;
}
