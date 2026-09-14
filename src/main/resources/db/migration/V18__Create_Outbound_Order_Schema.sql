-- Outbound Order and Outbound Order Line Schema
-- OutboundOrder is the aggregate root owning OutboundOrderLine records
-- References: locations (origin/destination), skus (line items)
-- Phase 1: Creation and basic management only. No inventory impact.

-- Drop and recreate to ensure clean schema (development idempotency)
DROP TABLE IF EXISTS outbound_order_lines CASCADE;
DROP TABLE IF EXISTS outbound_orders CASCADE;

-- ------------------------------------------------------------
-- Outbound Orders Table (Header)
-- ------------------------------------------------------------
CREATE TABLE outbound_orders
(
    id                              BIGSERIAL PRIMARY KEY,
    org_id                          BIGINT,
    dc_id                           BIGINT,
    status                          VARCHAR(50),
    remarks                         VARCHAR(500),
    created_by                      VARCHAR(100),
    created_date                    TIMESTAMP,
    modified_by                     VARCHAR(100),
    modified_date                   TIMESTAMP,
    deleted                         BOOLEAN NOT NULL DEFAULT FALSE,

    -- Business / UI Fields
    order_number                    VARCHAR(50)  NOT NULL,
    order_date                      DATE         NOT NULL,
    order_type                      VARCHAR(50)  NOT NULL,
    order_status                    VARCHAR(50)  NOT NULL,

    -- Customer (denormalized – no separate customer master)
    customer_code                   VARCHAR(100) NOT NULL,
    customer_name                   VARCHAR(255) NOT NULL,

    -- Origin Location (FK to locations)
    origin_location_id              BIGINT       NOT NULL REFERENCES locations (id),
    origin_location_code            VARCHAR(100),
    origin_location_name            VARCHAR(255),

    -- Destination Location (FK to locations)
    destination_location_id         BIGINT       NOT NULL REFERENCES locations (id),
    destination_location_code       VARCHAR(100),
    destination_location_name       VARCHAR(255),

    -- Pick Time Window
    pick_time_window_start          TIMESTAMP,
    pick_time_window_end            TIMESTAMP,

    -- Delivery Time Window
    delivery_time_window_start      TIMESTAMP,
    delivery_time_window_end        TIMESTAMP,

    -- Financials
    total_amount                    NUMERIC(19, 4),
    currency_code                   VARCHAR(10),

    -- Instructions / Notes
    instructions                    TEXT,
    notes                           TEXT
);

CREATE UNIQUE INDEX uidx_outbound_orders_number
    ON outbound_orders (order_number) WHERE deleted = FALSE;

CREATE INDEX idx_outbound_orders_status
    ON outbound_orders (order_status);

CREATE INDEX idx_outbound_orders_order_date
    ON outbound_orders (order_date);

CREATE INDEX idx_outbound_orders_customer
    ON outbound_orders (customer_code);

CREATE INDEX idx_outbound_orders_origin
    ON outbound_orders (origin_location_id);

CREATE INDEX idx_outbound_orders_destination
    ON outbound_orders (destination_location_id);

CREATE INDEX idx_outbound_orders_org_dc
    ON outbound_orders (org_id, dc_id);

CREATE INDEX idx_outbound_orders_deleted
    ON outbound_orders (deleted);

-- ------------------------------------------------------------
-- Outbound Order Lines Table (Child)
-- ------------------------------------------------------------
CREATE TABLE outbound_order_lines
(
    id                      BIGSERIAL PRIMARY KEY,
    outbound_order_id       BIGINT  NOT NULL REFERENCES outbound_orders (id) ON DELETE CASCADE,
    org_id                  BIGINT,
    dc_id                   BIGINT,
    status                  VARCHAR(50),
    remarks                 VARCHAR(500),
    created_by              VARCHAR(100),
    created_date            TIMESTAMP,
    modified_by             VARCHAR(100),
    modified_date           TIMESTAMP,
    deleted                 BOOLEAN NOT NULL DEFAULT FALSE,

    -- Line Identity
    line_number             INTEGER NOT NULL,

    -- Item Reference (FK to skus + snapshot)
    sku_id                  BIGINT  NOT NULL REFERENCES skus (id),
    sku_code                VARCHAR(100),
    sku_name                VARCHAR(255),

    -- Quantity
    quantity_ordered        NUMERIC(19, 4) NOT NULL,
    quantity_uom            VARCHAR(50),

    -- Pricing (backend-calculated)
    unit_price              NUMERIC(19, 4),
    total_price             NUMERIC(19, 4),
    currency_code           VARCHAR(10),

    -- Weight (backend-calculated from SKU master weight × qty)
    total_weight            NUMERIC(19, 4),
    weight_uom              VARCHAR(50),

    -- Volume (backend-calculated from Product master volume × qty)
    total_volume            NUMERIC(19, 4),
    volume_uom              VARCHAR(50)
);

CREATE UNIQUE INDEX uidx_outbound_order_lines_num
    ON outbound_order_lines (outbound_order_id, line_number) WHERE deleted = FALSE;

CREATE INDEX idx_outbound_order_lines_order_id
    ON outbound_order_lines (outbound_order_id);

CREATE INDEX idx_outbound_order_lines_sku_id
    ON outbound_order_lines (sku_id);

CREATE INDEX idx_outbound_order_lines_deleted
    ON outbound_order_lines (deleted);
