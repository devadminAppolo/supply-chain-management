CREATE TABLE IF NOT EXISTS scm.shipments
(
    id BIGSERIAL PRIMARY KEY,

    shipment_number VARCHAR(50) NOT NULL,

    customer_name VARCHAR(150) NOT NULL,

    origin VARCHAR(100),

    destination VARCHAR(100)
);