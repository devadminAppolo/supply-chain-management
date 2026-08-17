-- Adds new columns to the drivers table.
-- Existing columns and data are preserved.
-- All columns are nullable to avoid breaking existing rows.

ALTER TABLE drivers
    -- Personal / identity
    ADD COLUMN IF NOT EXISTS first_name               VARCHAR(255),
    ADD COLUMN IF NOT EXISTS middle_name              VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_name                VARCHAR(255),
    ADD COLUMN IF NOT EXISTS suffix                   VARCHAR(20),
    ADD COLUMN IF NOT EXISTS alias                    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS email                    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS home_phone               VARCHAR(20),
    ADD COLUMN IF NOT EXISTS birth_date               TIMESTAMP,
    ADD COLUMN IF NOT EXISTS zip_code                 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS address                  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address_2                VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city                     VARCHAR(100),
    ADD COLUMN IF NOT EXISTS state                    VARCHAR(100),
    ADD COLUMN IF NOT EXISTS country                  VARCHAR(100),

    -- Employment / codes
    ADD COLUMN IF NOT EXISTS employee_code            VARCHAR(100),
    ADD COLUMN IF NOT EXISTS dc_code                  VARCHAR(100),
    ADD COLUMN IF NOT EXISTS dc_id                    BIGINT,
    ADD COLUMN IF NOT EXISTS dist_ctr                 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS swat_dist_ctr            VARCHAR(100),
    ADD COLUMN IF NOT EXISTS carrier_code             VARCHAR(100),
    ADD COLUMN IF NOT EXISTS external_reference_id    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payroll_id               VARCHAR(100),
    ADD COLUMN IF NOT EXISTS job_title                VARCHAR(255),
    ADD COLUMN IF NOT EXISTS department               VARCHAR(255),
    ADD COLUMN IF NOT EXISTS status                   VARCHAR(100),
    ADD COLUMN IF NOT EXISTS hire_date                TIMESTAMP,
    ADD COLUMN IF NOT EXISTS join_date                TIMESTAMP,
    ADD COLUMN IF NOT EXISTS road_date                DATE,
    ADD COLUMN IF NOT EXISTS inactive_date            DATE,
    ADD COLUMN IF NOT EXISTS salary_type              VARCHAR(100),
    ADD COLUMN IF NOT EXISTS full_part_time           VARCHAR(10),
    ADD COLUMN IF NOT EXISTS wd_driver_id             BIGINT,

    -- Schedule / availability
    ADD COLUMN IF NOT EXISTS scheduled_days           VARCHAR(255),
    ADD COLUMN IF NOT EXISTS start_time               TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS shift_start_time         TIME,
    ADD COLUMN IF NOT EXISTS shift_end_time           TIME,
    ADD COLUMN IF NOT EXISTS time_zone                VARCHAR(100),
    ADD COLUMN IF NOT EXISTS availability_status      VARCHAR(50),

    -- License / compliance
    ADD COLUMN IF NOT EXISTS valid_license            BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS license_status           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS license_class            VARCHAR(100),
    ADD COLUMN IF NOT EXISTS dl_expiration_date       TIMESTAMP,
    ADD COLUMN IF NOT EXISTS dl_state                 VARCHAR(100),
    ADD COLUMN IF NOT EXISTS license_review_date      DATE,
    ADD COLUMN IF NOT EXISTS license_review_user      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS license_days_til_expired INTEGER,
    ADD COLUMN IF NOT EXISTS med_exam_cert_date       DATE,
    ADD COLUMN IF NOT EXISTS med_exam_expire_date     DATE,
    ADD COLUMN IF NOT EXISTS med_exam_review_date     DATE,
    ADD COLUMN IF NOT EXISTS med_exam_review_user     VARCHAR(255),
    ADD COLUMN IF NOT EXISTS med_exam_days_til_expired INTEGER,
    ADD COLUMN IF NOT EXISTS record_violations_date   DATE,
    ADD COLUMN IF NOT EXISTS annual_review_date       DATE,
    ADD COLUMN IF NOT EXISTS mvr_report_date          DATE,
    ADD COLUMN IF NOT EXISTS random_required          VARCHAR(10),
    ADD COLUMN IF NOT EXISTS yrs_safe_driving         INTEGER,
    ADD COLUMN IF NOT EXISTS safety_eligible          VARCHAR(10),

    -- Substance testing
    ADD COLUMN IF NOT EXISTS substance_record_date    DATE,
    ADD COLUMN IF NOT EXISTS substance_test_date      DATE,
    ADD COLUMN IF NOT EXISTS substance_test_type      VARCHAR(100),
    ADD COLUMN IF NOT EXISTS substance_test_reason    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS substance_test_results   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS substance_found          VARCHAR(255),

    -- Training / pre-qualification
    ADD COLUMN IF NOT EXISTS prequal_status           VARCHAR(100),
    ADD COLUMN IF NOT EXISTS training_status          VARCHAR(100),

    -- Equipment
    ADD COLUMN IF NOT EXISTS assigned_tractor         VARCHAR(100),
    ADD COLUMN IF NOT EXISTS assigned_tractor_2       VARCHAR(100),

    -- Notifications / messaging
    ADD COLUMN IF NOT EXISTS text_provider            VARCHAR(100),
    ADD COLUMN IF NOT EXISTS email_auto_send          VARCHAR(10),
    ADD COLUMN IF NOT EXISTS text_auto_send           VARCHAR(10),
    ADD COLUMN IF NOT EXISTS mail_box                 VARCHAR(100),

    -- Payment
    ADD COLUMN IF NOT EXISTS payment_type             VARCHAR(100),
    ADD COLUMN IF NOT EXISTS empty_miles_payment      DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS loaded_miles_payment     DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS amount                   DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS percentage               DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS hours_of_service         DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS bonus_eligible           VARCHAR(10),

    -- Documents
    ADD COLUMN IF NOT EXISTS document_url             VARCHAR(500),
    ADD COLUMN IF NOT EXISTS document_name            VARCHAR(255),

    -- Reporting / misc
    ADD COLUMN IF NOT EXISTS driver_reports           VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fars_id                  INTEGER,
    ADD COLUMN IF NOT EXISTS notes                    TEXT,
    ADD COLUMN IF NOT EXISTS changes                  TEXT,

    -- Soft delete / audit
    ADD COLUMN IF NOT EXISTS deleted_date             TIMESTAMPTZ;
