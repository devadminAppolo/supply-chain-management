package com.supplychain.wms.pallet.application.dto;

import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PalletStatusUpdateRequestDto {

    @NotNull(message = "Pallet status is required")
    private PalletStatus status;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
