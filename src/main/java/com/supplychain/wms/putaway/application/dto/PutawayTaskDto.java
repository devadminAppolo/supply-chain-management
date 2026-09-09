package com.supplychain.wms.putaway.application.dto;

import com.supplychain.wms.putaway.domain.enums.PutawayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayTaskDto {
    private Long id;
    private String taskNumber;
    private Long receivingId;
    private String receivingNumber;
    private Long palletId;
    private String palletNumber;
    private String lpnNumber;
    private Long asnId;
    private String asnNumber;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long productId;
    private Long skuId;
    private Long uomId;
    private String itemCode;
    private String itemName;
    private BigDecimal quantity;
    private Long stagingLocationId;
    private String stagingBinCode;
    private Long putawayLocationId;
    private String putawayBinCode;
    private String assignedStaffId;
    private String assignedStaffName;
    private PutawayStatus putawayStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
