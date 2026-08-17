-- Enhance the existing locations table.
-- Preserves all existing columns: id, location_code, location_name, city, state.
-- Adds BaseEntity audit fields, full address/geographic/contact/operational fields,
-- a LocationType classification column, and appropriate indexes.

-- ── BaseEntity audit fields ───────────────────────────────────────────────────
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS org_id        BIGINT,
    ADD COLUMN IF NOT EXISTS dc_id         BIGINT,
    ADD COLUMN IF NOT EXISTS status        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS remarks       VARCHAR(500),
    ADD COLUMN IF NOT EXISTS created_by    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS created_date  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS modified_by   VARCHAR(100),
    ADD COLUMN IF NOT EXISTS modified_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted       BOOLEAN NOT NULL DEFAULT FALSE;

-- ── Location classification ───────────────────────────────────────────────────
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS location_type     VARCHAR(50),
    ADD COLUMN IF NOT EXISTS location_sub_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS description       VARCHAR(500);

-- ── Extended address fields ───────────────────────────────────────────────────
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS address_line1 VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address_line2 VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address_line3 VARCHAR(255),
    ADD COLUMN IF NOT EXISTS state_code    VARCHAR(10),
    ADD COLUMN IF NOT EXISTS country       VARCHAR(100),
    ADD COLUMN IF NOT EXISTS country_code  VARCHAR(10),
    ADD COLUMN IF NOT EXISTS postal_code   VARCHAR(20),
    ADD COLUMN IF NOT EXISTS county        VARCHAR(100);

-- ── Geographic information ────────────────────────────────────────────────────
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS latitude      NUMERIC(11, 7),
    ADD COLUMN IF NOT EXISTS longitude     NUMERIC(11, 7),
    ADD COLUMN IF NOT EXISTS timezone      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS timezone_code VARCHAR(50);

-- ── Contact information ───────────────────────────────────────────────────────
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS contact_name    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS contact_phone   VARCHAR(50),
    ADD COLUMN IF NOT EXISTS alternate_phone VARCHAR(50),
    ADD COLUMN IF NOT EXISTS contact_email   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fax_number      VARCHAR(50);

-- ── Operational information ───────────────────────────────────────────────────
ALTER TABLE locations
    ADD COLUMN IF NOT EXISTS operating_hours VARCHAR(255),
    ADD COLUMN IF NOT EXISTS notes           TEXT,
    ADD COLUMN IF NOT EXISTS active          BOOLEAN NOT NULL DEFAULT TRUE;

-- ── Indexes ───────────────────────────────────────────────────────────────────
-- Unique location code among non-deleted records
CREATE UNIQUE INDEX IF NOT EXISTS uidx_locations_code
    ON locations (location_code)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_locations_type     ON locations (location_type);
CREATE INDEX IF NOT EXISTS idx_locations_status   ON locations (status);
CREATE INDEX IF NOT EXISTS idx_locations_org_dc   ON locations (org_id, dc_id);
CREATE INDEX IF NOT EXISTS idx_locations_city     ON locations (city);
CREATE INDEX IF NOT EXISTS idx_locations_country  ON locations (country_code);
CREATE INDEX IF NOT EXISTS idx_locations_active   ON locations (active);
CREATE INDEX IF NOT EXISTS idx_locations_deleted  ON locations (deleted);
