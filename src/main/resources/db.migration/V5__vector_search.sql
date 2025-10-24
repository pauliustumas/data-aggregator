CREATE OR REPLACE FUNCTION public.immutable_unaccent(text)
RETURNS text
LANGUAGE SQL
IMMUTABLE
STRICT
AS $$
    SELECT unaccent($1);
$$;

-- Create a functional index on the Lithuanian name column
CREATE INDEX IF NOT EXISTS idx_products_name_lt_tsv
    ON aggregate.products (
    to_tsvector('simple', immutable_unaccent(LOWER(name_lt)))
    );

-- Create a functional index on the English name column
CREATE INDEX IF NOT EXISTS idx_products_name_en_tsv
    ON aggregate.products (
    to_tsvector('simple', immutable_unaccent(LOWER(name_en)))
    );