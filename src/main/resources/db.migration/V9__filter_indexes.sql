-- GIN indexes on allergens arrays for overlap (&&) operator
CREATE INDEX IF NOT EXISTS idx_products_allergens_en ON aggregate.products USING gin(allergens_en);
CREATE INDEX IF NOT EXISTS idx_products_allergens_lt ON aggregate.products USING gin(allergens_lt);

-- Functional B-tree index on latest price for filtering and sorting
CREATE INDEX IF NOT EXISTS idx_products_latest_price ON aggregate.products ((price[array_length(price, 1)]));
