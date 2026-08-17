-- Add quantity_before and quantity_after to inventory_transactions.
-- These columns record the on-hand inventory level immediately before and after
-- each movement, making every row a self-contained audit entry.
-- Column order follows the existing table definition in V7.

ALTER TABLE inventory_transactions
    ADD COLUMN IF NOT EXISTS quantity_before NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS quantity_after  NUMERIC(19, 4);

CREATE INDEX IF NOT EXISTS idx_inv_txn_reference ON inventory_transactions (reference_type, reference_id)
    WHERE reference_id IS NOT NULL;
