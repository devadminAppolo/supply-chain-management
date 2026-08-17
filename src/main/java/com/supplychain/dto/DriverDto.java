package com.supplychain.dto;

import com.supplychain.enums.AvailabilityStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Getter
@Setter
public class DriverDto {

    // -------------------------------------------------------------------------
    // Existing fields — unchanged
    // -------------------------------------------------------------------------

    private Long id;
    private String driverNumber;
    private String driverName;
    private String licenseNumber;
    private String phoneNumber;

    // -------------------------------------------------------------------------
    // Personal / identity
    // -------------------------------------------------------------------------

    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String alias;
    private String email;
    private String homePhone;
    private Date birthDate;
    private String zipCode;
    private String address;
    private String address2;
    private String city;
    private String state;
    private String country;

    // -------------------------------------------------------------------------
    // Employment / codes
    // -------------------------------------------------------------------------

    private String employeeCode;
    private String carrierCode;
    private String payrollId;
    private String jobTitle;
    private String department;
    private String status;
    private Date hireDate;
    private Date joinDate;
    private LocalDate roadDate;
    private LocalDate inactiveDate;
    private String salaryType;
    private String fullPartTime;

    // -------------------------------------------------------------------------
    // Schedule / availability
    // -------------------------------------------------------------------------

    private String scheduledDays;
    private ZonedDateTime startTime;
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private String timeZone;
    private AvailabilityStatus availabilityStatus;

    // -------------------------------------------------------------------------
    // License / compliance
    // -------------------------------------------------------------------------

    private Boolean validLicense;
    private String licenseStatus;
    private String licenseClass;
    private Date dlExpirationDate;
    private String dlState;
    private LocalDate licenseReviewDate;
    private String licenseReviewUser;
    private Integer licenseDaysTilExpired;
    private LocalDate medExamCertDate;
    private LocalDate medExamExpireDate;
    private LocalDate medExamReviewDate;
    private String medExamReviewUser;
    private Integer medExamDaysTilExpired;
    private LocalDate recordViolationsDate;
    private LocalDate annualReviewDate;
    private Integer yrsSafeDriving;
    private String safetyEligible;

    // -------------------------------------------------------------------------
    // Payment
    // -------------------------------------------------------------------------

    private String paymentType;
    private Double emptyMilesPayment;
    private Double loadedMilesPayment;
    private Double amount;
    private Double percentage;
    private Double hoursOfService;
    private String bonusEligible;

    private ZonedDateTime deletedDate;
}
