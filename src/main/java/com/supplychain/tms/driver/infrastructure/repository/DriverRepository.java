package com.supplychain.tms.driver.infrastructure.repository;

import com.supplychain.tms.driver.domain.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findAllByDeletedDateIsNullOrderByDriverNameAsc();
}
