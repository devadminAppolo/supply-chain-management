CREATE TABLE IF NOT EXISTS customers
(
    id            BIGSERIAL PRIMARY KEY,
    org_id        BIGINT,
    dc_id         BIGINT,
    status        VARCHAR(50)  NOT NULL,
    remarks       VARCHAR(500),
    created_by    VARCHAR(100),
    created_date  TIMESTAMP,
    modified_by   VARCHAR(100),
    modified_date TIMESTAMP,
    deleted       BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Business keys
    customer_code VARCHAR(100) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,

    -- Contact
    contact_name  VARCHAR(255),
    email         VARCHAR(255),
    phone_number  VARCHAR(50),

    -- Address fields – kept separate per requirement
    address       VARCHAR(500),
    address1      VARCHAR(500),
    address2      VARCHAR(500),
    city          VARCHAR(100),
    state         VARCHAR(100),
    country       VARCHAR(100),
    postal_code   VARCHAR(20)
);

ALTER TABLE outbound_orders
    ADD COLUMN IF NOT EXISTS customer_id BIGINT REFERENCES customers (id);
