package com.supplychain.wms.staff.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {

    private Long id;

    private Long orgId;

    private Long dcId;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String displayName;

    @NotBlank(message = "User name is required")
    private String userName;

    private String email;

    private String phoneNumber;

    private String role;

    private String department;

    private String status;

    private String remarks;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;
}
