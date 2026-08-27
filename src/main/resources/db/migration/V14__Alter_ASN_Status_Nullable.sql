-- Make status column nullable in advance_shipping_notices (inherited from BaseEntity)
ALTER TABLE advance_shipping_notices ALTER COLUMN status DROP NOT NULL;
