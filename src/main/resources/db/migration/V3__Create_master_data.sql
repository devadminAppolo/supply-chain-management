CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    location_code VARCHAR(50),
    location_name VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS drivers (
    id BIGSERIAL PRIMARY KEY,
    driver_number VARCHAR(50),
    driver_name VARCHAR(255),
    license_number VARCHAR(100),
    phone_number VARCHAR(20)
);