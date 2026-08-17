-- Inventory Management Schema
-- Tracks bin-level stock for each SKU within a warehouse
-- References: warehouses, bins, products, skus, uoms

CREATE TABLE IF NOT EXISTS inventory (
    id                 BIGSERIAL PRIMARY KEY,
    org_id             BIGINT,
    dc_id              BIGINT,
    warehouse_id       BIGINT         NOT NULL REFERENCES warehouses (id),
    bin_id             BIGINT         NOT NULL REFERENCES bins (id),
    product_id         BIGINT         NOT NULL REFERENCES products (id),
    sku_id             BIGINT         NOT NULL REFERENCES skus (id),
    uom_id             BIGINT         NOT NULL REFERENCES uoms (id),
    lot_number         VARCHAR(100),
    serial_number      VARCHAR(100),
    manufacture_date   DATE,
    expiry_date        DATE,
    inventory_status   VARCHAR(50)    NOT NULL,
    on_hand_quantity   NUMERIC(19, 4) NOT NULL DEFAULT 0,
    reserved_quantity  NUMERIC(19, 4) NOT NULL DEFAULT 0,
    allocated_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0,
    available_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0,
    status             VARCHAR(50),
    remarks            VARCHAR(500),
    created_by         VARCHAR(100),
    created_date       TIMESTAMP,
    modified_by        VARCHAR(100),
    modified_date      TIMESTAMP,
    deleted            BOOLEAN        NOT NULL DEFAULT FALSE
);

-- Unique constraint: no two active records for the same logical stock position.
-- COALESCE converts NULL lot/serial to '' so that NULL = NULL is enforced as a duplicate.
CREATE UNIQUE INDEX IF NOT EXISTS uidx_inventory_unique
    ON inventory (warehouse_id, bin_id, sku_id,
                  COALESCE(lot_number, ''), COALESCE(serial_number, ''),
                  inventory_status)
    WHERE deleted = FALSE;