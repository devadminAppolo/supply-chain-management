-- Inventory Transaction Schema
-- Records every inventory movement event (receipts, transfers, adjustments, etc.)
-- References: warehouses, inventory, products, skus, uoms, bins

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id               BIGSERIAL PRIMARY KEY,
    org_id           BIGINT,
    dc_id            BIGINT,
    warehouse_id     BIGINT         NOT NULL REFERENCES warehouses (id),
    inventory_id     BIGINT         NOT NULL REFERENCES inventory (id),
    product_id       BIGINT         NOT NULL REFERENCES products (id),
    sku_id           BIGINT         NOT NULL REFERENCES skus (id),
    uom_id           BIGINT         NOT NULL REFERENCES uoms (id),
    from_bin_id      BIGINT                  REFERENCES bins (id),
    to_bin_id        BIGINT                  REFERENCES bins (id),
    transaction_type VARCHAR(50)    NOT NULL,
    quantity         NUMERIC(19, 4) NOT NULL,
    lot_number       VARCHAR(100),
    serial_number    VARCHAR(100),
    reference_type   VARCHAR(100),
    reference_id     VARCHAR(100),
    reason_code      VARCHAR(100),
    transaction_date TIMESTAMP      NOT NULL,
    status           VARCHAR(50),
    remarks          VARCHAR(500),
    created_by       VARCHAR(100),
    created_date     TIMESTAMP,
    modified_by      VARCHAR(100),
    modified_date    TIMESTAMP,
    deleted          BOOLEAN        NOT NULL DEFAULT FALSE
);
