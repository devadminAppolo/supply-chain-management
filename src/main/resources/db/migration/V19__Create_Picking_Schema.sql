-- Picking Task Schema
-- Picking tasks are created automatically by the backend when an outbound order
-- is confirmed and inventory is allocated. Users cannot create picking tasks manually.

DROP TABLE IF EXISTS picking_tasks CASCADE;

CREATE TABLE picking_tasks
(
    id                      BIGSERIAL PRIMARY KEY,
    org_id                  BIGINT,
    dc_id                   BIGINT,
    status                  VARCHAR(50),
    remarks                 VARCHAR(500),
    created_by              VARCHAR(100),
    created_date            TIMESTAMP,
    modified_by             VARCHAR(100),
    modified_date           TIMESTAMP,
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    -- Business key
    pick_number             VARCHAR(100) NOT NULL,

    -- Parent references
    outbound_order_id       BIGINT NOT NULL REFERENCES outbound_orders (id),
    outbound_order_line_id  BIGINT NOT NULL REFERENCES outbound_order_lines (id),

    -- Item snapshot (from SKU master at allocation time)
    sku_id                  BIGINT NOT NULL REFERENCES skus (id),
    sku_code                VARCHAR(100),
    sku_name                VARCHAR(255),

    -- Source inventory record (with pessimistic-lock key for updates)
    inventory_id            BIGINT NOT NULL REFERENCES inventory (id),
    warehouse_id            BIGINT,
    source_bin_id           BIGINT,
    source_bin_code         VARCHAR(100),

    -- Quantity tracking
    quantity_to_pick        NUMERIC(19, 4) NOT NULL,
    quantity_picked         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    quantity_uom            VARCHAR(50),

    -- Assignment
    assigned_staff_id       VARCHAR(100),
    assigned_staff_name     VARCHAR(255),

    -- Lifecycle
    picking_status          VARCHAR(50)  NOT NULL,
    started_at              TIMESTAMP,
    completed_at            TIMESTAMP,
    notes                   TEXT
);

CREATE UNIQUE INDEX uidx_picking_tasks_number
    ON picking_tasks (pick_number) WHERE deleted = FALSE;

CREATE INDEX idx_picking_tasks_order_id
    ON picking_tasks (outbound_order_id);

CREATE INDEX idx_picking_tasks_order_line_id
    ON picking_tasks (outbound_order_line_id);

CREATE INDEX idx_picking_tasks_status
    ON picking_tasks (picking_status);

CREATE INDEX idx_picking_tasks_staff_id
    ON picking_tasks (assigned_staff_id);

CREATE INDEX idx_picking_tasks_inventory_id
    ON picking_tasks (inventory_id);

CREATE INDEX idx_picking_tasks_deleted
    ON picking_tasks (deleted);
