-- Packing Task Schema
-- Packing tasks are created automatically by the backend after all required
-- picking for an outbound order is completed. Users cannot create packing tasks manually.

DROP TABLE IF EXISTS packing_tasks CASCADE;

CREATE TABLE packing_tasks
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

    -- Business keys
    packing_number          VARCHAR(100) NOT NULL,
    package_id              VARCHAR(100),          -- generated at packing completion

    -- Parent reference
    outbound_order_id       BIGINT NOT NULL REFERENCES outbound_orders (id),

    -- Package details (recorded at completion)
    package_type            VARCHAR(50),           -- BOX, CARTON, PALLET, CRATE, etc.

    -- Quantity tracking
    quantity_to_pack        NUMERIC(19, 4) NOT NULL,
    quantity_packed         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    quantity_uom            VARCHAR(50),

    -- Physical properties (recorded at completion)
    weight                  NUMERIC(19, 4),
    weight_uom              VARCHAR(50),
    volume                  NUMERIC(19, 4),
    volume_uom              VARCHAR(50),

    -- Assignment
    assigned_staff_id       VARCHAR(100),
    assigned_staff_name     VARCHAR(255),

    -- Lifecycle
    packing_status          VARCHAR(50) NOT NULL,
    started_at              TIMESTAMP,
    completed_at            TIMESTAMP,
    notes                   TEXT
);

CREATE UNIQUE INDEX uidx_packing_tasks_number
    ON packing_tasks (packing_number) WHERE deleted = FALSE;

CREATE INDEX idx_packing_tasks_order_id
    ON packing_tasks (outbound_order_id);

CREATE INDEX idx_packing_tasks_status
    ON packing_tasks (packing_status);

CREATE INDEX idx_packing_tasks_staff_id
    ON packing_tasks (assigned_staff_id);

CREATE INDEX idx_packing_tasks_package_id
    ON packing_tasks (package_id);

CREATE INDEX idx_packing_tasks_deleted
    ON packing_tasks (deleted);
