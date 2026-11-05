-- The list position of an order item was being written over product_id.
-- Give it its own column so product_id can hold the real product.
ALTER TABLE order_items ADD COLUMN item_index int NOT NULL DEFAULT 0;

-- In rows saved before this migration, product_id holds the list position.
-- Move it across to keep item order. The original product IDs cannot be recovered.
UPDATE order_items SET item_index = product_id::int WHERE product_id ~ '^[0-9]{1,9}$';
