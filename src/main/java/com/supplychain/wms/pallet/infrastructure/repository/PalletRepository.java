package com.supplychain.wms.pallet.infrastructure.repository;

import com.supplychain.wms.pallet.domain.enums.PalletStatus;
import com.supplychain.wms.pallet.domain.model.Pallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PalletRepository extends JpaRepository<Pallet, Long>, JpaSpecificationExecutor<Pallet> {

    Optional<Pallet> findByIdAndDeletedFalse(Long id);

    Optional<Pallet> findByPalletNumberAndDeletedFalse(String palletNumber);

    Optional<Pallet> findByLpnNumberAndDeletedFalse(String lpnNumber);

    Optional<Pallet> findByBarcodeAndDeletedFalse(String barcode);

    boolean existsByPalletNumberAndDeletedFalse(String palletNumber);

    boolean existsByLpnNumberAndDeletedFalse(String lpnNumber);

    boolean existsByBarcodeAndDeletedFalse(String barcode);

    boolean existsByBarcodeAndIdNotAndDeletedFalse(String barcode, Long id);

    List<Pallet> findAllByPalletStatusAndActiveTrueAndDeletedFalse(PalletStatus palletStatus);

    List<Pallet> findAllByActiveTrueAndDeletedFalseOrderByPalletNumberAsc();

    List<Pallet> findAllByDeletedFalseOrderByPalletNumberAsc();
}
