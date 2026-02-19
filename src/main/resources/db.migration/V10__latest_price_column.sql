ALTER TABLE aggregate.products ADD COLUMN latest_price NUMERIC;

UPDATE aggregate.products
SET latest_price = price[array_length(price, 1)]
WHERE price IS NOT NULL AND array_length(price, 1) > 0;

CREATE INDEX idx_products_latest_price_btree ON aggregate.products (latest_price);

CREATE OR REPLACE FUNCTION aggregate.sync_latest_price()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.price IS NOT NULL AND array_length(NEW.price, 1) > 0 THEN
        NEW.latest_price := NEW.price[array_length(NEW.price, 1)];
    ELSE
        NEW.latest_price := NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_latest_price
    BEFORE INSERT OR UPDATE OF price ON aggregate.products
    FOR EACH ROW
    EXECUTE FUNCTION aggregate.sync_latest_price();

DROP INDEX IF EXISTS aggregate.idx_products_latest_price;
