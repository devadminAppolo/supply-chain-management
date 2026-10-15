package com.supplychain.customer.infrastructure.repository;

import com.supplychain.customer.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByIdAndDeletedFalse(Long id);

    boolean existsByCustomerCodeAndDeletedFalse(String customerCode);

    boolean existsByCustomerCodeAndIdNotAndDeletedFalse(String customerCode, Long id);

    List<Customer> findAllByDeletedFalseOrderByCustomerNameAsc();

    List<Customer> findAllByStatusAndDeletedFalse(String status);
}
