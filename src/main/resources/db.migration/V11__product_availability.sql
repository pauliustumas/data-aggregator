CREATE TABLE aggregate.product_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    internal_product_id UUID NOT NULL REFERENCES aggregate.products(internal_product_id) ON DELETE CASCADE,
    source TEXT NOT NULL,
    url TEXT,
    price NUMERIC,
    original_price NUMERIC,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_product_availability_product_id ON aggregate.product_availability(internal_product_id);
CREATE INDEX idx_product_availability_source ON aggregate.product_availability(source);

CREATE TRIGGER update_product_availability_updated_at
    BEFORE UPDATE ON aggregate.product_availability
    FOR EACH ROW
    EXECUTE FUNCTION aggregate.update_updated_at_column();
