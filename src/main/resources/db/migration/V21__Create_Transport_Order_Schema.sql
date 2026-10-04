-- Transport Order Schema
-- Transport Orders are created from one or more READY_TO_SHIP Outbound Orders.
-- References: locations (origin/destination), outbound_orders, outbound_order_lines, skus

-- ------------------------------------------------------------
-- Transport Orders Table (Header)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transport_orders
(
    id                          BIGSERIAL PRIMARY KEY,
    org_id                      BIGINT,
    dc_id                       BIGINT,
    status                      VARCHAR(50),
    remarks                     VARCHAR(500),
    created_by                  VARCHAR(100),
    created_date                TIMESTAMP,
    modified_by                 VARCHAR(100),
    modified_date               TIMESTAMP,
    deleted                     BOOLEAN NOT NULL DEFAULT FALSE,

    -- Business key – backend-generated, never client-supplied
    to_number                   VARCHAR(50)  NOT NULL,

    -- Status
    to_status                   VARCHAR(50)  NOT NULL,

    -- Transportation details
    mode                        VARCHAR(100) NOT NULL,
    service_level_name          VARCHAR(255) NOT NULL,
    notes                       TEXT,

    -- Origin Location (FK + snapshot)
    origin_location_id          BIGINT       NOT NULL REFERENCES locations (id),
    origin_location_code        VARCHAR(100),
    origin_location_name        VARCHAR(255),

    -- Destination Location (FK + snapshot)
    destination_location_id     BIGINT       NOT NULL REFERENCES locations (id),
    destination_location_code   VARCHAR(100),
    destination_location_name   VARCHAR(255),

    -- Pickup Window
    pick_up_from                TIMESTAMP    NOT NULL,
    pick_up_to                  TIMESTAMP    NOT NULL,

    -- Delivery Window
    delivery_from               TIMESTAMP    NOT NULL,
    delivery_to                 TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_transport_orders_number
    ON transport_orders (to_number) WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_transport_orders_status
    ON transport_orders (to_status);

CREATE INDEX IF NOT EXISTS idx_transport_orders_origin
    ON transport_orders (origin_location_id);

CREATE INDEX IF NOT EXISTS idx_transport_orders_destination
    ON transport_orders (destination_location_id);

CREATE INDEX IF NOT EXISTS idx_transport_orders_pick_up_from
    ON transport_orders (pick_up_from);

CREATE INDEX IF NOT EXISTS idx_transport_orders_org_dc
    ON transport_orders (org_id, dc_id);

CREATE INDEX IF NOT EXISTS idx_transport_orders_deleted
    ON transport_orders (deleted);

-- ------------------------------------------------------------
-- Transport Order Line Items Table (Child)
-- Derived from Outbound Order Lines at TO creation time.
-- Retains full traceability back to source OO and OO Line.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transport_order_line_items
(
    id                      BIGSERIAL PRIMARY KEY,
    transport_order_id      BIGINT  NOT NULL REFERENCES transport_orders (id) ON DELETE CASCADE,
    org_id                  BIGINT,
    dc_id                   BIGINT,
    status                  VARCHAR(50),
    remarks                 VARCHAR(500),
    created_by              VARCHAR(100),
    created_date            TIMESTAMP,
    modified_by             VARCHAR(100),
    modified_date           TIMESTAMP,
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    -- Traceability back to source
    outbound_order_id       BIGINT  NOT NULL REFERENCES outbound_orders (id),
    outbound_order_line_id  BIGINT           REFERENCES outbound_order_lines (id),

    -- Line identity (sequential within TO)
    line_number             INTEGER NOT NULL,

    -- SKU Reference (FK + snapshot at creation time)
    sku_id                  BIGINT           REFERENCES skus (id),
    sku_code                VARCHAR(100),
    sku_name                VARCHAR(255),

    -- Quantity
    quantity_ordered        NUMERIC(19, 4) NOT NULL,
    quantity_uom            VARCHAR(50),

    -- Weight (carried from OO Line)
    total_weight            NUMERIC(19, 4),
    weight_uom              VARCHAR(50),

    -- Volume (carried from OO Line)
    total_volume            NUMERIC(19, 4),
    volume_uom              VARCHAR(50)
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_to_line_items_num
    ON transport_order_line_items (transport_order_id, line_number) WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_to_line_items_transport_order_id
    ON transport_order_line_items (transport_order_id);

CREATE INDEX IF NOT EXISTS idx_to_line_items_outbound_order_id
    ON transport_order_line_items (outbound_order_id);

CREATE INDEX IF NOT EXISTS idx_to_line_items_sku_id
    ON transport_order_line_items (sku_id);

CREATE INDEX IF NOT EXISTS idx_to_line_items_deleted
    ON transport_order_line_items (deleted);

-- ------------------------------------------------------------
-- Transport Order – Outbound Order Association Table
-- One TO can contain multiple OOs.
-- One OO can only be in one active (non-cancelled) TO at a time.
-- The unique partial index prevents duplicate active associations.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transport_order_outbound_orders
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

    transport_order_id      BIGINT  NOT NULL REFERENCES transport_orders (id),
    outbound_order_id       BIGINT  NOT NULL REFERENCES outbound_orders (id)
);

-- Prevent the same OO from being associated with the same TO more than once
CREATE UNIQUE INDEX IF NOT EXISTS uidx_to_outbound_order_pair
    ON transport_order_outbound_orders (transport_order_id, outbound_order_id) WHERE deleted = FALSE;

-- Fast lookup: find which TO a given OO belongs to
CREATE INDEX IF NOT EXISTS idx_to_outbound_order_assoc_oo
    ON transport_order_outbound_orders (outbound_order_id);

-- Fast lookup: find all OOs for a given TO
CREATE INDEX IF NOT EXISTS idx_to_outbound_order_assoc_to
    ON transport_order_outbound_orders (transport_order_id);

CREATE INDEX IF NOT EXISTS idx_to_outbound_order_assoc_deleted
    ON transport_order_outbound_orders (deleted);


ALTER TABLE transport_orders
    ADD COLUMN po_number  VARCHAR(100),
    ADD COLUMN asn_number VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_transport_orders_po_number
    ON transport_orders (po_number)
    WHERE po_number IS NOT NULL AND deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_transport_orders_asn_number
    ON transport_orders (asn_number)
    WHERE asn_number IS NOT NULL AND deleted = FALSE;

ALTER TABLE transport_order_line_items
    ALTER COLUMN outbound_order_id DROP NOT NULL;
