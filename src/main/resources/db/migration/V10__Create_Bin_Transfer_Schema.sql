-- Bin Transfer Schema
-- Records inventory movements between bins within the same warehouse.
-- Designed for future extension to cross-warehouse transfers by keeping
-- separate source/destination warehouse foreign keys.
-- References: warehouses, bins, skus, uoms

CREATE TABLE IF NOT EXISTS bin_transfers (
    id                       BIGSERIAL PRIMARY KEY,
    org_id                   BIGINT,
    dc_id                    BIGINT,
    source_warehouse_id      BIGINT         NOT NULL REFERENCES warehouses (id),
    source_bin_id            BIGINT         NOT NULL REFERENCES bins (id),
    destination_warehouse_id BIGINT         NOT NULL REFERENCES warehouses (id),
    destination_bin_id       BIGINT         NOT NULL REFERENCES bins (id),
    sku_id                   BIGINT         NOT NULL REFERENCES skus (id),
    uom_id                   BIGINT         NOT NULL REFERENCES uoms (id),
    quantity                 NUMERIC(19, 4) NOT NULL,
    reason_code              VARCHAR(100),
    transfer_date            TIMESTAMP      NOT NULL,
    status                   VARCHAR(50),
    remarks                  VARCHAR(500),
    created_by               VARCHAR(100),
    created_date             TIMESTAMP,
    modified_by              VARCHAR(100),
    modified_date            TIMESTAMP,
    deleted                  BOOLEAN        NOT NULL DEFAULT FALSE
);

-- Lookup indexes
CREATE INDEX IF NOT EXISTS idx_bin_txfr_src_wh   ON bin_transfers (source_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_src_bin  ON bin_transfers (source_bin_id);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_dst_wh   ON bin_transfers (destination_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_dst_bin  ON bin_transfers (destination_bin_id);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_sku      ON bin_transfers (sku_id);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_uom      ON bin_transfers (uom_id);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_date     ON bin_transfers (transfer_date);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_status   ON bin_transfers (status);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_org_dc   ON bin_transfers (org_id, dc_id);
CREATE INDEX IF NOT EXISTS idx_bin_txfr_deleted  ON bin_transfers (deleted);
