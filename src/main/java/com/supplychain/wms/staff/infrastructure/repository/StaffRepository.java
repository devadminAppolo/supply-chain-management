package com.supplychain.wms.staff.infrastructure.repository;

import com.supplychain.wms.staff.domain.model.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long>, JpaSpecificationExecutor<Staff> {

    Optional<Staff> findByIdAndDeletedFalse(Long id);

    Optional<Staff> findByUserNameAndDeletedFalse(String userName);

    boolean existsByUserNameAndDeletedFalse(String userName);

    boolean existsByUserNameAndIdNotAndDeletedFalse(String userName, Long id);

    List<Staff> findAllByDeletedFalseAndStatusIgnoreCaseOrderByFirstNameAsc(String status);

    List<Staff> findAllByDeletedFalseOrderByFirstNameAsc();

    Page<Staff> findAllByDeletedFalse(Pageable pageable);
}
