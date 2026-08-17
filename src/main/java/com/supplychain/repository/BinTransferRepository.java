package com.supplychain.repository;

import com.supplychain.entity.BinTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BinTransferRepository
        extends JpaRepository<BinTransfer, Long>, JpaSpecificationExecutor<BinTransfer> {
}
