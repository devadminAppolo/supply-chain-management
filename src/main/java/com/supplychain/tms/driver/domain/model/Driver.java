package com.supplychain.tms.driver.domain.model;

import com.supplychain.common.domain.enums.AvailabilityStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "driver_number")
    private String driverNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "license_number")
    private String licenseNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "suffix")
    private String suffix;

    @Column(name = "alias")
    private String alias;

    @Column(name = "email")
    private String email;

    @Column(name = "home_phone")
    private String homePhone;

    @Column(name = "birth_date")
    private Date birthDate;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "address")
    private String address;

    @Column(name = "address_2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "dc_code")
    private String dcCode;

    @Column(name = "dc_id")
    private Long dcId;

    @Column(name = "dist_ctr")
    private String distCtr;

    @Column(name = "swat_dist_ctr")
    private String swatDistCtr;

    @Column(name = "carrier_code")
    private String carrierCode;

    @Column(name = "external_reference_id")
    private String externalReferenceId;

    @Column(name = "payroll_id")
    private String payrollId;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "department")
    private String department;

    @Column(name = "status")
    private String status;

    @Column(name = "hire_date")
    private Date hireDate;

    @Column(name = "join_date")
    private Date joinDate;

    @Column(name = "road_date")
    private LocalDate roadDate;

    @Column(name = "inactive_date")
    private LocalDate inactiveDate;

    @Column(name = "salary_type")
    private String salaryType;

    @Column(name = "full_part_time")
    private String fullPartTime;

    @Column(name = "wd_driver_id")
    private Long wdDriverId;

    @Column(name = "scheduled_days")
    private String scheduledDays;

    @Column(name = "start_time")
    private ZonedDateTime startTime;

    @Column(name = "shift_start_time")
    private LocalTime shiftStartTime;

    @Column(name = "shift_end_time")
    private LocalTime shiftEndTime;

    @Column(name = "time_zone")
    private String timeZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private AvailabilityStatus availabilityStatus;

    @Column(name = "valid_license")
    private boolean validLicense;

    @Column(name = "license_status")
    private String licenseStatus;

    @Column(name = "license_class")
    private String licenseClass;

    @Column(name = "dl_expiration_date")
    private Date dlExpirationDate;

    @Column(name = "dl_state")
    private String dlState;

    @Column(name = "license_review_date")
    private LocalDate licenseReviewDate;

    @Column(name = "license_review_user")
    private String licenseReviewUser;

    @Column(name = "license_days_til_expired")
    private Integer licenseDaysTilExpired;

    @Column(name = "med_exam_cert_date")
    private LocalDate medExamCertDate;

    @Column(name = "med_exam_expire_date")
    private LocalDate medExamExpireDate;

    @Column(name = "med_exam_review_date")
    private LocalDate medExamReviewDate;

    @Column(name = "med_exam_review_user")
    private String medExamReviewUser;

    @Column(name = "med_exam_days_til_expired")
    private Integer medExamDaysTilExpired;

    @Column(name = "record_violations_date")
    private LocalDate recordViolationsDate;

    @Column(name = "annual_review_date")
    private LocalDate annualReviewDate;

    @Column(name = "mvr_report_date")
    private LocalDate mvrReportDate;

    @Column(name = "random_required")
    private String randomRequired;

    @Column(name = "yrs_safe_driving")
    private Integer yrsSafeDriving;

    @Column(name = "safety_eligible")
    private String safetyEligible;

    @Column(name = "substance_record_date")
    private LocalDate substanceRecordDate;

    @Column(name = "substance_test_date")
    private LocalDate substanceTestDate;

    @Column(name = "substance_test_type")
    private String substanceTestType;

    @Column(name = "substance_test_reason")
    private String substanceTestReason;

    @Column(name = "substance_test_results")
    private String substanceTestResults;

    @Column(name = "substance_found")
    private String substanceFound;

    @Column(name = "prequal_status")
    private String prequalStatus;

    @Column(name = "training_status")
    private String trainingStatus;

    @Column(name = "assigned_tractor")
    private String assignedTractor;

    @Column(name = "assigned_tractor_2")
    private String assignedTractor2;

    @Column(name = "text_provider")
    private String textProvider;

    @Column(name = "email_auto_send")
    private String emailAutoSend;

    @Column(name = "text_auto_send")
    private String textAutoSend;

    @Column(name = "mail_box")
    private String mailBox;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "empty_miles_payment")
    private Double emptyMilesPayment;

    @Column(name = "loaded_miles_payment")
    private Double loadedMilesPayment;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "hours_of_service")
    private Double hoursOfService;

    @Column(name = "bonus_eligible")
    private String bonusEligible;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "driver_reports")
    private String driverReports;

    @Column(name = "fars_id")
    private Integer farsId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "changes")
    private String changes;

    @Column(name = "deleted_date")
    private ZonedDateTime deletedDate;
}
