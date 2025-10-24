CREATE TABLE aggregate.product_fetch_counts
(
    internal_product_id UUID PRIMARY KEY,
    fetch_count         BIGINT NOT NULL DEFAULT 0,
    last_fetched_at     TIMESTAMP WITH TIME ZONE
);
