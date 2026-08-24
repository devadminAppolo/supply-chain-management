package com.supplychain.wms.bintransfer.infrastructure.repository;

import com.supplychain.wms.bintransfer.domain.model.BinTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BinTransferRepository
        extends JpaRepository<BinTransfer, Long>, JpaSpecificationExecutor<BinTransfer> {
}
