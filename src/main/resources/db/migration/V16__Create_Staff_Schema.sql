-- ============================================================
-- WMS Staff Master Schema and Initial Seed Data
-- ============================================================

CREATE TABLE IF NOT EXISTS staff
(
    id            BIGSERIAL PRIMARY KEY,
    org_id        BIGINT,
    dc_id         BIGINT,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100),
    user_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    phone_number  VARCHAR(50),
    role          VARCHAR(100),
    department    VARCHAR(100),
    status        VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    remarks       VARCHAR(500),
    created_by    VARCHAR(100),
    created_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by   VARCHAR(100),
    modified_date TIMESTAMP,
    deleted       BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_staff_user_name ON staff (user_name) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_staff_status           ON staff (status);
CREATE INDEX IF NOT EXISTS idx_staff_org_dc           ON staff (org_id, dc_id);
CREATE INDEX IF NOT EXISTS idx_staff_deleted          ON staff (deleted);

-- ------------------------------------------------------------
-- Seed Initial Staff Records (Reference Staff Master)
-- ------------------------------------------------------------
INSERT INTO staff (id, first_name, last_name, user_name, email, status, role, department, created_by, created_date, deleted)
VALUES
    (342, 'Jonas', 'K', 'jonas.k@cscs.io', 'jonas.k@cscs.io', 'ACTIVE', 'Warehouse Staff', 'Warehouse Operations', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (400, 'Randy', 'Orton', 'automationscript2@cscs.io', 'automationscript2@cscs.io', 'ACTIVE', 'Forklift Operator', 'Inbound & Putaway', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (402, 'Solvi', 'L', 'solvi@gmail.com', 'solvi@gmail.com', 'ACTIVE', 'Inventory Specialist', 'Inventory Control', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (422, 'priya', 'N', 'dskoperator@cscs.io', 'dskoperator@cscs.io', 'ACTIVE', 'Receiving Operator', 'Receiving', 'SYSTEM', CURRENT_TIMESTAMP, FALSE),
    (427, 'TEST', 'USER', 'testuser@gmail.com', 'testuser@gmail.com', 'INACTIVE', 'Quality Inspector', 'Quality Assurance', 'SYSTEM', CURRENT_TIMESTAMP, FALSE)
ON CONFLICT (id) DO NOTHING;

-- Advance sequence if needed
SELECT setval('staff_id_seq', (SELECT COALESCE(MAX(id), 1) FROM staff));
