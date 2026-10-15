package com.supplychain.customer.domain.model;

import com.supplychain.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Customer Master entity.
 * <p>
 * The lifecycle status (ACTIVE / INACTIVE) is stored in the inherited {@code status}
 * column from {@link BaseEntity}.  A partial unique index on {@code customer_code}
 * (WHERE deleted = FALSE) is enforced at the database level.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends BaseEntity {

    @Column(name = "customer_code", nullable = false)
    private String customerCode;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    // Contact
    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Address fields – kept separate per requirement
    @Column(name = "address")
    private String address;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "postal_code")
    private String postalCode;
}
