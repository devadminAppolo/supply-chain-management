-- Advance Shipping Notice (ASN) and ASN Line Item Schema
-- AdvanceShippingNotice is the aggregate root owning AdvanceShippingNoticeLine records

-- ------------------------------------------------------------
-- Advance Shipping Notices Table (Header)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS advance_shipping_notices
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
    asn_number                 VARCHAR(50) NOT NULL,
    asn_status                 VARCHAR(50) NOT NULL,
    purchase_order_id          BIGINT NOT NULL REFERENCES purchase_orders (id),
    supplier_id                VARCHAR(100),
    supplier_name              VARCHAR(255),
    warehouse_id               VARCHAR(100),
    warehouse_name             VARCHAR(255),
    shipment_date              DATE NOT NULL,
    expected_arrival_date      DATE,
    carrier_id                 VARCHAR(100),
    carrier_name               VARCHAR(255),
    tracking_number            VARCHAR(100),
    vehicle_number             VARCHAR(100),
    shipping_method            VARCHAR(50),
    priority                   VARCHAR(50),
    packing_slip_number        VARCHAR(100),
    bill_of_lading_number      VARCHAR(100),
    container_number           VARCHAR(100),
    total_packages             INTEGER,
    total_weight               NUMERIC(19, 4),
    weight_uom                 VARCHAR(50),
    total_volume               NUMERIC(19, 4),
    volume_uom                 VARCHAR(50),
    special_instructions       TEXT,
    notes                      TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_asn_number           ON advance_shipping_notices (asn_number) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_asn_purchase_order_id        ON advance_shipping_notices (purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_asn_supplier_id              ON advance_shipping_notices (supplier_id);
CREATE INDEX IF NOT EXISTS idx_asn_warehouse_id             ON advance_shipping_notices (warehouse_id);
CREATE INDEX IF NOT EXISTS idx_asn_status                   ON advance_shipping_notices (asn_status);
CREATE INDEX IF NOT EXISTS idx_asn_shipment_date            ON advance_shipping_notices (shipment_date);
CREATE INDEX IF NOT EXISTS idx_asn_org_dc                   ON advance_shipping_notices (org_id, dc_id);
CREATE INDEX IF NOT EXISTS idx_asn_deleted                  ON advance_shipping_notices (deleted);

-- ------------------------------------------------------------
-- Advance Shipping Notice Line Items Table (Child)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS advance_shipping_notice_lines
(
    id                         BIGSERIAL PRIMARY KEY,
    asn_id                     BIGINT NOT NULL REFERENCES advance_shipping_notices (id) ON DELETE CASCADE,
    po_line_id                 BIGINT NOT NULL REFERENCES purchase_order_line_items (id),
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
    line_number                INTEGER NOT NULL,
    item_code                  VARCHAR(100) NOT NULL,
    item_name                  VARCHAR(255) NOT NULL,
    supplier_item_code         VARCHAR(100),
    item_gtin                  VARCHAR(100),
    quantity_ordered           NUMERIC(19, 4) NOT NULL,
    previously_shipped_quantity NUMERIC(19, 4) NOT NULL DEFAULT 0,
    shipment_quantity          NUMERIC(19, 4) NOT NULL,
    remaining_quantity         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    quantity_uom               VARCHAR(50) NOT NULL,
    unit_price                 NUMERIC(19, 4) NOT NULL,
    currency_code              VARCHAR(10) NOT NULL,
    total_monetary_value       NUMERIC(19, 4),
    batch_number               VARCHAR(100),
    serial_number              VARCHAR(100),
    manufacturing_date         DATE,
    expiry_date                DATE,
    country_of_origin          VARCHAR(100),
    weight                     NUMERIC(19, 4),
    weight_uom                 VARCHAR(50),
    volume                     NUMERIC(19, 4),
    volume_uom                 VARCHAR(50),
    line_status                VARCHAR(50) NOT NULL,
    notes                      TEXT
);

CREATE INDEX IF NOT EXISTS idx_asn_lines_asn_id             ON advance_shipping_notice_lines (asn_id);
CREATE INDEX IF NOT EXISTS idx_asn_lines_po_line_id         ON advance_shipping_notice_lines (po_line_id);
CREATE INDEX IF NOT EXISTS idx_asn_lines_item_code          ON advance_shipping_notice_lines (item_code);
CREATE INDEX IF NOT EXISTS idx_asn_lines_line_status        ON advance_shipping_notice_lines (line_status);
CREATE INDEX IF NOT EXISTS idx_asn_lines_deleted            ON advance_shipping_notice_lines (deleted);
