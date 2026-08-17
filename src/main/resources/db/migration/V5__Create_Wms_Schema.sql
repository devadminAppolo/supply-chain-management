-- WMS Master Data Schema
-- Warehouses → Zones → Aisles → Racks → Bins
-- Categories, UOMs, Products, SKUs

-- ------------------------------------------------------------
-- Warehouses
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS warehouses
(
    id             BIGSERIAL PRIMARY KEY,
    org_id         BIGINT,
    dc_id          BIGINT,
    warehouse_code VARCHAR(50)  NOT NULL,
    warehouse_name VARCHAR(255) NOT NULL,
    warehouse_type VARCHAR(100),
    address1       VARCHAR(255),
    address2       VARCHAR(255),
    city           VARCHAR(100),
    state          VARCHAR(100),
    country        VARCHAR(100),
    postal_code    VARCHAR(20),
    timezone       VARCHAR(100),
    contact_name   VARCHAR(255),
    contact_number VARCHAR(50),
    email          VARCHAR(255),
    status         VARCHAR(50),
    remarks        VARCHAR(500),
    created_by     VARCHAR(100),
    created_date   TIMESTAMP,
    modified_by    VARCHAR(100),
    modified_date  TIMESTAMP,
    deleted        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_warehouses_code ON warehouses (warehouse_code) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_warehouses_status       ON warehouses (status);
CREATE INDEX IF NOT EXISTS idx_warehouses_org_dc       ON warehouses (org_id, dc_id);

-- ------------------------------------------------------------
-- Zones
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS zones
(
    id               BIGSERIAL PRIMARY KEY,
    org_id           BIGINT,
    dc_id            BIGINT,
    warehouse_id     BIGINT       NOT NULL REFERENCES warehouses (id),
    zone_code        VARCHAR(50)  NOT NULL,
    zone_name        VARCHAR(255) NOT NULL,
    zone_type        VARCHAR(50),
    temperature_type VARCHAR(50),
    sequence         INTEGER,
    status           VARCHAR(50),
    remarks          VARCHAR(500),
    created_by       VARCHAR(100),
    created_date     TIMESTAMP,
    modified_by      VARCHAR(100),
    modified_date    TIMESTAMP,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_zones_code_warehouse ON zones (zone_code, warehouse_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_zones_warehouse              ON zones (warehouse_id);
CREATE INDEX IF NOT EXISTS idx_zones_status                ON zones (status);

-- ------------------------------------------------------------
-- Aisles
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS aisles
(
    id            BIGSERIAL PRIMARY KEY,
    org_id        BIGINT,
    dc_id         BIGINT,
    warehouse_id  BIGINT       NOT NULL REFERENCES warehouses (id),
    zone_id       BIGINT       NOT NULL REFERENCES zones (id),
    aisle_code    VARCHAR(50)  NOT NULL,
    aisle_name    VARCHAR(255) NOT NULL,
    sequence      INTEGER,
    length        DOUBLE PRECISION,
    width         DOUBLE PRECISION,
    status        VARCHAR(50),
    remarks       VARCHAR(500),
    created_by    VARCHAR(100),
    created_date  TIMESTAMP,
    modified_by   VARCHAR(100),
    modified_date TIMESTAMP,
    deleted       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_aisles_code_zone ON aisles (aisle_code, zone_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_aisles_warehouse          ON aisles (warehouse_id);
CREATE INDEX IF NOT EXISTS idx_aisles_zone              ON aisles (zone_id);
CREATE INDEX IF NOT EXISTS idx_aisles_status            ON aisles (status);

-- ------------------------------------------------------------
-- Racks
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS racks
(
    id            BIGSERIAL PRIMARY KEY,
    org_id        BIGINT,
    dc_id         BIGINT,
    warehouse_id  BIGINT       NOT NULL REFERENCES warehouses (id),
    zone_id       BIGINT       NOT NULL REFERENCES zones (id),
    aisle_id      BIGINT       NOT NULL REFERENCES aisles (id),
    rack_code     VARCHAR(50)  NOT NULL,
    rack_name     VARCHAR(255) NOT NULL,
    rack_type     VARCHAR(50),
    levels        INTEGER,
    max_weight    DOUBLE PRECISION,
    status        VARCHAR(50),
    remarks       VARCHAR(500),
    created_by    VARCHAR(100),
    created_date  TIMESTAMP,
    modified_by   VARCHAR(100),
    modified_date TIMESTAMP,
    deleted       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_racks_code_aisle ON racks (rack_code, aisle_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_racks_warehouse           ON racks (warehouse_id);
CREATE INDEX IF NOT EXISTS idx_racks_zone               ON racks (zone_id);
CREATE INDEX IF NOT EXISTS idx_racks_aisle              ON racks (aisle_id);
CREATE INDEX IF NOT EXISTS idx_racks_status             ON racks (status);

-- ------------------------------------------------------------
-- Bins
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bins
(
    id              BIGSERIAL PRIMARY KEY,
    org_id          BIGINT,
    dc_id           BIGINT,
    warehouse_id    BIGINT      NOT NULL REFERENCES warehouses (id),
    zone_id         BIGINT      NOT NULL REFERENCES zones (id),
    aisle_id        BIGINT      NOT NULL REFERENCES aisles (id),
    rack_id         BIGINT      NOT NULL REFERENCES racks (id),
    bin_code        VARCHAR(50) NOT NULL,
    bin_type        VARCHAR(50),
    capacity        DOUBLE PRECISION,
    capacity_uom    VARCHAR(50),
    max_weight      DOUBLE PRECISION,
    occupied        BOOLEAN NOT NULL DEFAULT FALSE,
    allow_mixed_sku BOOLEAN NOT NULL DEFAULT FALSE,
    allow_mixed_lot BOOLEAN NOT NULL DEFAULT FALSE,
    barcode         VARCHAR(100),
    status          VARCHAR(50),
    remarks         VARCHAR(500),
    created_by      VARCHAR(100),
    created_date    TIMESTAMP,
    modified_by     VARCHAR(100),
    modified_date   TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_bins_code_rack ON bins (bin_code, rack_id) WHERE deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uidx_bins_barcode   ON bins (barcode) WHERE deleted = FALSE AND barcode IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_bins_warehouse          ON bins (warehouse_id);
CREATE INDEX IF NOT EXISTS idx_bins_zone              ON bins (zone_id);
CREATE INDEX IF NOT EXISTS idx_bins_aisle             ON bins (aisle_id);
CREATE INDEX IF NOT EXISTS idx_bins_rack              ON bins (rack_id);
CREATE INDEX IF NOT EXISTS idx_bins_status            ON bins (status);

-- ------------------------------------------------------------
-- Categories
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories
(
    id                 BIGSERIAL PRIMARY KEY,
    org_id             BIGINT,
    dc_id              BIGINT,
    category_code      VARCHAR(50)  NOT NULL,
    category_name      VARCHAR(255) NOT NULL,
    parent_category_id BIGINT REFERENCES categories (id),
    description        TEXT,
    display_sequence   INTEGER,
    status             VARCHAR(50),
    remarks            VARCHAR(500),
    created_by         VARCHAR(100),
    created_date       TIMESTAMP,
    modified_by        VARCHAR(100),
    modified_date      TIMESTAMP,
    deleted            BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_categories_code ON categories (category_code) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_categories_parent        ON categories (parent_category_id);
CREATE INDEX IF NOT EXISTS idx_categories_status        ON categories (status);

-- ------------------------------------------------------------
-- Unit of Measures
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS uoms
(
    id                BIGSERIAL PRIMARY KEY,
    org_id            BIGINT,
    dc_id             BIGINT,
    uom_code          VARCHAR(50)  NOT NULL,
    uom_name          VARCHAR(255) NOT NULL,
    symbol            VARCHAR(20),
    uom_type          VARCHAR(50),
    base_uom          BOOLEAN NOT NULL DEFAULT FALSE,
    conversion_factor DOUBLE PRECISION,
    status            VARCHAR(50),
    remarks           VARCHAR(500),
    created_by        VARCHAR(100),
    created_date      TIMESTAMP,
    modified_by       VARCHAR(100),
    modified_date     TIMESTAMP,
    deleted           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_uoms_code ON uoms (uom_code) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_uoms_type          ON uoms (uom_type);
CREATE INDEX IF NOT EXISTS idx_uoms_status        ON uoms (status);

-- ------------------------------------------------------------
-- Products
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products
(
    id                BIGSERIAL PRIMARY KEY,
    org_id            BIGINT,
    dc_id             BIGINT,
    product_code      VARCHAR(100) NOT NULL,
    product_name      VARCHAR(255) NOT NULL,
    short_description VARCHAR(500),
    long_description  TEXT,
    category_id       BIGINT REFERENCES categories (id),
    uom_id            BIGINT REFERENCES uoms (id),
    barcode           VARCHAR(100),
    manufacturer      VARCHAR(255),
    brand             VARCHAR(255),
    country_of_origin VARCHAR(100),
    weight            DOUBLE PRECISION,
    length            DOUBLE PRECISION,
    width             DOUBLE PRECISION,
    height            DOUBLE PRECISION,
    volume            DOUBLE PRECISION,
    shelf_life_days   INTEGER,
    expiry_required   BOOLEAN NOT NULL DEFAULT FALSE,
    serial_tracking   BOOLEAN NOT NULL DEFAULT FALSE,
    lot_tracking      BOOLEAN NOT NULL DEFAULT FALSE,
    hazardous         BOOLEAN NOT NULL DEFAULT FALSE,
    temperature_type  VARCHAR(50),
    image_url         VARCHAR(500),
    status            VARCHAR(50),
    remarks           VARCHAR(500),
    created_by        VARCHAR(100),
    created_date      TIMESTAMP,
    modified_by       VARCHAR(100),
    modified_date     TIMESTAMP,
    deleted           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_products_code    ON products (product_code) WHERE deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uidx_products_barcode ON products (barcode) WHERE deleted = FALSE AND barcode IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_products_category        ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_uom             ON products (uom_id);
CREATE INDEX IF NOT EXISTS idx_products_status          ON products (status);

-- ------------------------------------------------------------
-- SKUs
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skus
(
    id            BIGSERIAL PRIMARY KEY,
    org_id        BIGINT,
    dc_id         BIGINT,
    product_id    BIGINT       NOT NULL REFERENCES products (id),
    sku_code      VARCHAR(100) NOT NULL,
    upc           VARCHAR(100),
    ean           VARCHAR(100),
    pack_type     VARCHAR(50),
    color         VARCHAR(100),
    size          VARCHAR(100),
    weight        DOUBLE PRECISION,
    status        VARCHAR(50),
    remarks       VARCHAR(500),
    created_by    VARCHAR(100),
    created_date  TIMESTAMP,
    modified_by   VARCHAR(100),
    modified_date TIMESTAMP,
    deleted       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_skus_code_product ON skus (sku_code, product_id) WHERE deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uidx_skus_upc          ON skus (upc) WHERE deleted = FALSE AND upc IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uidx_skus_ean          ON skus (ean) WHERE deleted = FALSE AND ean IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_skus_product              ON skus (product_id);
CREATE INDEX IF NOT EXISTS idx_skus_status              ON skus (status);
