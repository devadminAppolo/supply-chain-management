-- Inventory Adjustment Schema
-- Records user-initiated inventory adjustment requests (INCREASE / DECREASE)
-- References: warehouses, skus, bins, uoms
-- Future flow: InventoryAdjustment → InventoryTransaction → Inventory

CREATE TABLE IF NOT EXISTS inventory_adjustments (
    id               BIGSERIAL PRIMARY KEY,
    org_id           BIGINT,
    dc_id            BIGINT,
    warehouse_id     BIGINT         NOT NULL REFERENCES warehouses (id),
    sku_id           BIGINT         NOT NULL REFERENCES skus (id),
    bin_id           BIGINT         NOT NULL REFERENCES bins (id),
    uom_id           BIGINT         NOT NULL REFERENCES uoms (id),
    adjustment_type  VARCHAR(50)    NOT NULL,
    quantity         NUMERIC(19, 4) NOT NULL,
    reason_code      VARCHAR(100),
    reference_number VARCHAR(100),
    adjustment_date  TIMESTAMP      NOT NULL,
    status           VARCHAR(50),
    remarks          VARCHAR(500),
    created_by       VARCHAR(100),
    created_date     TIMESTAMP,
    modified_by      VARCHAR(100),
    modified_date    TIMESTAMP,
    deleted          BOOLEAN        NOT NULL DEFAULT FALSE
);
