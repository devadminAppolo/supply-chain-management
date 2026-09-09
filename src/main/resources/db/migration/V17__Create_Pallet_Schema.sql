-- ============================================================
-- WMS Pallet Master Schema and Linkage
-- ============================================================

CREATE TABLE IF NOT EXISTS pallets
(
    id                         BIGSERIAL PRIMARY KEY,
    org_id                     BIGINT,
    dc_id                      BIGINT,
    status                     VARCHAR(50),
    remarks                    VARCHAR(500),
    created_by                 VARCHAR(100),
    created_date               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by                VARCHAR(100),
    modified_date              TIMESTAMP,
    deleted                    BOOLEAN NOT NULL DEFAULT FALSE,
    version                    BIGINT NOT NULL DEFAULT 0,

    -- Pallet Master Identification
    pallet_number              VARCHAR(50) NOT NULL,
    lpn_number                 VARCHAR(50) NOT NULL,
    pallet_type                VARCHAR(50) NOT NULL,
    pallet_material            VARCHAR(50) NOT NULL,
    pallet_status              VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',

    -- Physical Properties & Capacities
    max_weight                 NUMERIC(19, 4),
    weight_uom                 VARCHAR(50),
    length                     NUMERIC(19, 4),
    width                      NUMERIC(19, 4),
    height                     NUMERIC(19, 4),
    dimension_uom              VARCHAR(50),
    tare_weight                NUMERIC(19, 4),
    tare_weight_uom            VARCHAR(50),

    -- Identifiers & Additional Details
    barcode                    VARCHAR(100),
    description                VARCHAR(500),
    notes                      TEXT,
    active                     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_pallets_pallet_number ON pallets (pallet_number) WHERE deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uidx_pallets_lpn_number    ON pallets (lpn_number) WHERE deleted = FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uidx_pallets_barcode       ON pallets (barcode) WHERE deleted = FALSE AND barcode IS NOT NULL AND barcode <> '';
CREATE INDEX IF NOT EXISTS idx_pallets_status                ON pallets (pallet_status);
CREATE INDEX IF NOT EXISTS idx_pallets_type                  ON pallets (pallet_type);
CREATE INDEX IF NOT EXISTS idx_pallets_material              ON pallets (pallet_material);
CREATE INDEX IF NOT EXISTS idx_pallets_active                ON pallets (active);
CREATE INDEX IF NOT EXISTS idx_pallets_org_dc                ON pallets (org_id, dc_id);
CREATE INDEX IF NOT EXISTS idx_pallets_deleted               ON pallets (deleted);

-- Linkage column in receiving_pallets to standalone Pallet master record
ALTER TABLE receiving_pallets ADD COLUMN IF NOT EXISTS pallet_master_id BIGINT REFERENCES pallets (id);
CREATE INDEX IF NOT EXISTS idx_rec_pallets_master_id ON receiving_pallets (pallet_master_id);

-- Seed initial standard pallet master units
INSERT INTO pallets (id, pallet_number, lpn_number, pallet_type, pallet_material, pallet_status, max_weight, weight_uom, length, width, height, dimension_uom, tare_weight, tare_weight_uom, barcode, description, active, status, created_by, created_date, deleted)
VALUES
    (1, 'PAL-20260905-000001', 'LPN-20260905-000001', 'STANDARD', 'WOOD', 'AVAILABLE', 1500.00, 'KG', 120.00, 100.00, 15.00, 'CM', 25.00, 'KG', 'BAR-PLT-000001', 'Standard GMA 48x40 Heavy Duty Wood Pallet', TRUE, 'AVAILABLE', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (2, 'PAL-20260905-000002', 'LPN-20260905-000002', 'EURO', 'WOOD', 'AVAILABLE', 1200.00, 'KG', 120.00, 80.00, 14.40, 'CM', 20.00, 'KG', 'BAR-PLT-000002', 'Standard EUR-EPAL 1200x800 Exchange Pallet', TRUE, 'AVAILABLE', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (3, 'PAL-20260905-000003', 'LPN-20260905-000003', 'PLASTIC', 'PLASTIC', 'AVAILABLE', 1000.00, 'KG', 120.00, 100.00, 15.00, 'CM', 18.00, 'KG', 'BAR-PLT-000003', 'Hygienic Cleanroom Heavy Duty Plastic Pallet', TRUE, 'AVAILABLE', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (4, 'PAL-20260905-000004', 'LPN-20260905-000004', 'CHEP', 'WOOD', 'AVAILABLE', 1500.00, 'KG', 120.00, 100.00, 16.00, 'CM', 28.00, 'KG', 'BAR-PLT-000004', 'Blue CHEP Pooled Standard Pallet', TRUE, 'AVAILABLE', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (5, 'PAL-20260905-000005', 'LPN-20260905-000005', 'METAL', 'METAL', 'AVAILABLE', 2500.00, 'KG', 120.00, 100.00, 15.00, 'CM', 40.00, 'KG', 'BAR-PLT-000005', 'Heavy Duty Steel High-Load Pallet', TRUE, 'AVAILABLE', 'SYSTEM', CURRENT_TIMESTAMP, FALSE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('pallets_id_seq', (SELECT COALESCE(MAX(id), 1) FROM pallets));
