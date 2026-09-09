-- ============================================================
-- WMS Receiving and Putaway Schema
-- ============================================================

-- ------------------------------------------------------------
-- 1. Receivings Table (Header)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS receivings
(
    id                         BIGSERIAL PRIMARY KEY,
    org_id                     BIGINT,
    dc_id                      BIGINT,
    status                     VARCHAR(50),
    remarks                    VARCHAR(500),
    created_by                 VARCHAR(100),
    created_date               TIMESTAMP,
    modified_by                VARCHAR(100),
    modified_date              TIMESTAMP,
    deleted                    BOOLEAN NOT NULL DEFAULT FALSE,

    -- Business Fields
    receiving_number           VARCHAR(50) NOT NULL,
    asn_id                     BIGINT NOT NULL REFERENCES advance_shipping_notices (id),
    asn_number                 VARCHAR(50),
    purchase_order_id          BIGINT NOT NULL REFERENCES purchase_orders (id),
    purchase_order_number      VARCHAR(50),
    supplier_id                VARCHAR(100),
    supplier_name              VARCHAR(255),
    warehouse_id               BIGINT REFERENCES warehouses (id),
    warehouse_code             VARCHAR(50),
    warehouse_name             VARCHAR(255),
    arrival_time               TIMESTAMP,
    receiving_date             TIMESTAMP NOT NULL,
    receiving_status           VARCHAR(50) NOT NULL,
    notes                      TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_receiving_number      ON receivings (receiving_number) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_receiving_asn_id              ON receivings (asn_id);
CREATE INDEX IF NOT EXISTS idx_receiving_po_id               ON receivings (purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_receiving_warehouse_id        ON receivings (warehouse_id);
CREATE INDEX IF NOT EXISTS idx_receiving_status              ON receivings (receiving_status);
CREATE INDEX IF NOT EXISTS idx_receiving_org_dc              ON receivings (org_id, dc_id);
CREATE INDEX IF NOT EXISTS idx_receiving_deleted             ON receivings (deleted);

-- ------------------------------------------------------------
-- 2. Receiving Pallets Table (Handling Units / LPNs)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS receiving_pallets
(
    id                         BIGSERIAL PRIMARY KEY,
    org_id                     BIGINT,
    dc_id                      BIGINT,
    status                     VARCHAR(50),
    remarks                    VARCHAR(500),
    created_by                 VARCHAR(100),
    created_date               TIMESTAMP,
    modified_by                VARCHAR(100),
    modified_date              TIMESTAMP,
    deleted                    BOOLEAN NOT NULL DEFAULT FALSE,

    -- Parent & Traceability References
    receiving_id               BIGINT NOT NULL REFERENCES receivings (id) ON DELETE CASCADE,
    asn_id                     BIGINT NOT NULL REFERENCES advance_shipping_notices (id),
    asn_line_id                BIGINT NOT NULL REFERENCES advance_shipping_notice_lines (id),
    purchase_order_id          BIGINT NOT NULL REFERENCES purchase_orders (id),
    po_line_id                 BIGINT REFERENCES purchase_order_line_items (id),

    -- Pallet Identification
    pallet_number              VARCHAR(50) NOT NULL,
    lpn_number                 VARCHAR(50),

    -- Item & Stock Details
    product_id                 BIGINT REFERENCES products (id),
    sku_id                     BIGINT NOT NULL REFERENCES skus (id),
    uom_id                     BIGINT REFERENCES uoms (id),
    item_code                  VARCHAR(100) NOT NULL,
    item_name                  VARCHAR(255),
    quantity                   NUMERIC(19, 4) NOT NULL,
    condition                  VARCHAR(50) DEFAULT 'GOOD',

    -- Storage Coordinates (Initially STAGING, updated on Putaway)
    staging_bin_id             BIGINT NOT NULL REFERENCES bins (id),
    current_bin_id             BIGINT REFERENCES bins (id),

    -- Lot & Serial Traceability
    batch_number               VARCHAR(100),
    serial_number              VARCHAR(100),
    manufacturing_date         DATE,
    expiry_date                DATE,
    received_at                TIMESTAMP,
    pallet_status              VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_rec_pallets_receiving_id      ON receiving_pallets (receiving_id);
CREATE INDEX IF NOT EXISTS idx_rec_pallets_asn_line_id       ON receiving_pallets (asn_line_id);
CREATE INDEX IF NOT EXISTS idx_rec_pallets_sku_id            ON receiving_pallets (sku_id);
CREATE INDEX IF NOT EXISTS idx_rec_pallets_staging_bin_id    ON receiving_pallets (staging_bin_id);
CREATE INDEX IF NOT EXISTS idx_rec_pallets_current_bin_id    ON receiving_pallets (current_bin_id);
CREATE INDEX IF NOT EXISTS idx_rec_pallets_deleted           ON receiving_pallets (deleted);

-- ------------------------------------------------------------
-- 3. Putaway Tasks Table
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS putaway_tasks
(
    id                         BIGSERIAL PRIMARY KEY,
    org_id                     BIGINT,
    dc_id                      BIGINT,
    status                     VARCHAR(50),
    remarks                    VARCHAR(500),
    created_by                 VARCHAR(100),
    created_date               TIMESTAMP,
    modified_by                VARCHAR(100),
    modified_date              TIMESTAMP,
    deleted                    BOOLEAN NOT NULL DEFAULT FALSE,

    -- Business Identification
    task_number                VARCHAR(50) NOT NULL,
    receiving_id               BIGINT NOT NULL REFERENCES receivings (id),
    pallet_id                  BIGINT NOT NULL REFERENCES receiving_pallets (id),
    asn_id                     BIGINT NOT NULL REFERENCES advance_shipping_notices (id),
    purchase_order_id          BIGINT NOT NULL REFERENCES purchase_orders (id),
    warehouse_id               BIGINT REFERENCES warehouses (id),

    -- Item & Movement Quantities
    product_id                 BIGINT REFERENCES products (id),
    sku_id                     BIGINT NOT NULL REFERENCES skus (id),
    uom_id                     BIGINT REFERENCES uoms (id),
    item_code                  VARCHAR(100),
    item_name                  VARCHAR(255),
    quantity                   NUMERIC(19, 4) NOT NULL,

    -- Storage Coordinates (From Staging -> Final Storage)
    staging_bin_id             BIGINT NOT NULL REFERENCES bins (id),
    putaway_bin_id             BIGINT REFERENCES bins (id),

    -- Task Execution & Staff Assignment
    assigned_staff_id          VARCHAR(100),
    assigned_staff_name        VARCHAR(255),
    putaway_status             VARCHAR(50) NOT NULL,
    started_at                 TIMESTAMP,
    completed_at               TIMESTAMP,
    notes                      TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_putaway_task_number   ON putaway_tasks (task_number) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_putaway_tasks_receiving_id    ON putaway_tasks (receiving_id);
CREATE INDEX IF NOT EXISTS idx_putaway_tasks_pallet_id       ON putaway_tasks (pallet_id);
CREATE INDEX IF NOT EXISTS idx_putaway_tasks_staging_bin     ON putaway_tasks (staging_bin_id);
CREATE INDEX IF NOT EXISTS idx_putaway_tasks_putaway_bin     ON putaway_tasks (putaway_bin_id);
CREATE INDEX IF NOT EXISTS idx_putaway_tasks_status          ON putaway_tasks (putaway_status);
CREATE INDEX IF NOT EXISTS idx_putaway_tasks_deleted         ON putaway_tasks (deleted);
