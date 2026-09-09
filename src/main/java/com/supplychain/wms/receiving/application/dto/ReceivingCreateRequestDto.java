package com.supplychain.wms.receiving.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReceivingCreateRequestDto {

    private String receivingNumber;

    @NotNull(message = "ASN ID is required")
    private Long asnId;

    private LocalDateTime arrivalTime;

    private LocalDateTime receivingDate;

    private String notes;

    @NotEmpty(message = "Receiving must contain at least one pallet")
    @Valid
    private List<ReceivingPalletRequestDto> pallets = new ArrayList<>();
}
