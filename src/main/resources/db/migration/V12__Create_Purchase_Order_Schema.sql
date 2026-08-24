-- Purchase Order and Purchase Order Line Item Schema
-- PurchaseOrder is the aggregate root owning PurchaseOrderLineItem records

-- ------------------------------------------------------------
-- Purchase Orders Table (Header)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_orders
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

    -- 40 UI / Business Fields
    po_number                  VARCHAR(50) NOT NULL,
    po_type                    VARCHAR(50) NOT NULL,
    po_status                  VARCHAR(50) NOT NULL,
    po_date                    DATE NOT NULL,
    supplier                   VARCHAR(255) NOT NULL,
    supplier_reference_number  VARCHAR(100),
    supplier_quotation_number  VARCHAR(100),
    company                    VARCHAR(255),
    purchasing_organization    VARCHAR(100),
    purchasing_group           VARCHAR(100),
    distribution_center        VARCHAR(100),
    warehouse                  VARCHAR(255),
    requested_delivery_date    DATE,
    promised_delivery_date     DATE,
    latest_delivery_date       DATE,
    priority                   VARCHAR(50) NOT NULL,
    currency_code              VARCHAR(10) NOT NULL,
    exchange_rate              NUMERIC(15, 6),
    exchange_rate_fixed        BOOLEAN DEFAULT FALSE,
    payment_terms              VARCHAR(100),
    net_payment_days           INTEGER,
    cash_discount1_days        INTEGER,
    cash_discount1_percent     NUMERIC(7, 4),
    cash_discount2_days        INTEGER,
    cash_discount2_percent     NUMERIC(7, 4),
    incoterms                  VARCHAR(50),
    incoterms_version          VARCHAR(50),
    incoterms_location         VARCHAR(255),
    shipping_method            VARCHAR(50),
    freight_terms              VARCHAR(50),
    carrier                    VARCHAR(255),
    appointment_required       BOOLEAN DEFAULT FALSE,
    tax_code                   VARCHAR(50),
    tax_country                VARCHAR(100),
    subtotal_amount            NUMERIC(19, 4),
    discount_amount            NUMERIC(19, 4),
    freight_amount             NUMERIC(19, 4),
    tax_amount                 NUMERIC(19, 4),
    total_amount               NUMERIC(19, 4),
    notes                      TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_purchase_orders_number ON purchase_orders (po_number) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_purchase_orders_status         ON purchase_orders (po_status);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_date           ON purchase_orders (po_date);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_supplier       ON purchase_orders (supplier);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_org_dc         ON purchase_orders (org_id, dc_id);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_deleted        ON purchase_orders (deleted);

-- ------------------------------------------------------------
-- Purchase Order Line Items Table (Child)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchase_order_line_items
(
    id                         BIGSERIAL PRIMARY KEY,
    purchase_order_id          BIGINT NOT NULL REFERENCES purchase_orders (id) ON DELETE CASCADE,
    org_id                     BIGINT,
    dc_id                      BIGINT,
    status                     VARCHAR(50),
    remarks                    VARCHAR(500),
    created_by                 VARCHAR(100),
    created_date               TIMESTAMP,
    modified_by                VARCHAR(100),
    modified_date              TIMESTAMP,
    deleted                    BOOLEAN NOT NULL DEFAULT FALSE,

    -- 25 UI / Business Fields
    line_number                INTEGER NOT NULL,
    line_type                  VARCHAR(50) NOT NULL,
    item_code                  VARCHAR(100) NOT NULL,
    item_name                  VARCHAR(255) NOT NULL,
    product_class              VARCHAR(50) NOT NULL,
    supplier_item_code         VARCHAR(100),
    item_gtin                  VARCHAR(100),
    quantity_ordered           NUMERIC(19, 4) NOT NULL,
    quantity_received          NUMERIC(19, 4) DEFAULT 0,
    shipped_quantity           NUMERIC(19, 4) DEFAULT 0,
    original_order_quantity    NUMERIC(19, 4) NOT NULL,
    quantity_uom               VARCHAR(50) NOT NULL,
    unit_price                 NUMERIC(19, 4) NOT NULL,
    currency_code              VARCHAR(10) NOT NULL,
    total_monetary_value       NUMERIC(19, 4),
    total_weight               NUMERIC(19, 4),
    weight_uom                 VARCHAR(50),
    total_volume               NUMERIC(19, 4),
    volume_uom                 VARCHAR(50),
    protection_level_code      VARCHAR(50),
    planned_size1              VARCHAR(50),
    planned_size2              VARCHAR(50),
    planned_weight             NUMERIC(19, 4),
    planned_volume             NUMERIC(19, 4),
    line_status                VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_po_lines_po_line_num ON purchase_order_line_items (purchase_order_id, line_number) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_po_lines_po_id               ON purchase_order_line_items (purchase_order_id);
CREATE INDEX IF NOT EXISTS idx_po_lines_item_code           ON purchase_order_line_items (item_code);
CREATE INDEX IF NOT EXISTS idx_po_lines_line_status         ON purchase_order_line_items (line_status);
CREATE INDEX IF NOT EXISTS idx_po_lines_deleted             ON purchase_order_line_items (deleted);
