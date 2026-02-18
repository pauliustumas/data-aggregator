-- Step 1: Create the 'aggregate' schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS aggregate;

-- Step 2: Create the 'products' table with all required fields
CREATE TABLE aggregate.products (
                                  internal_product_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Internal unique identifier
                                  barcode TEXT UNIQUE NOT NULL,                                    -- Unique barcode identifier
                                  name_en TEXT NOT NULL,                                           -- Product name in EN
                                  name_lt TEXT NOT NULL,                                           -- Product name in LT
                                  image_url TEXT,                                                  -- Image URL
                                  price NUMERIC[],                                                 -- Product price history
                                  original_price NUMERIC[],                                        -- Original price history before discount
                                  raw_description TEXT,                                            -- Raw product description
                                  description_en TEXT,                                             -- Product description in English
                                  description_lt TEXT,                                             -- Product description in Lithuanian
                                  country_of_origin_lt TEXT,                                       -- Country of origin in Lithuanian
                                  country_of_origin_en TEXT,                                       -- Country of origin in English
                                  net_weight_g INTEGER,                                            -- Net weight in grams
                                  ingredients_lt TEXT,                                           -- Array of ingredients in Lithuanian
                                  ingredients_en TEXT,                                           -- Array of ingredients in English
                                  additives_lt JSONB,                                              -- JSONB for additives in Lithuanian
                                  additives_en JSONB,                                              -- JSONB for additives in English
                                  nutrition_per_100g JSONB,                                        -- JSONB for nutrition details per 100g
                                  storage_conditions JSONB,                                        -- JSONB for storage conditions
                                  packaging_lt TEXT,                                               -- Packaging description in Lithuanian
                                  packaging_en TEXT,                                               -- Packaging description in English
                                  brand TEXT,                                                      -- Brand name
                                  ai_opinion_lt TEXT,                                              -- AI opinion in Lithuanian
                                  ai_opinion_en TEXT,                                              -- AI opinion in English
                                  ai_datasource TEXT,                                             -- AI data source
                                  main_category_en TEXT,                                           -- Main category in English
                                  sub_category_en TEXT,                                            -- Sub-category in English
                                  sub_sub_category_en TEXT,                                        -- Sub-sub-category in English
                                  main_category_lt TEXT,                                           -- Main category in Lithuanian
                                  sub_category_lt TEXT,                                            -- Sub-category in Lithuanian
                                  sub_sub_category_lt TEXT,                                        -- Sub-sub-category in Lithuanian
                                  original_url_en TEXT,                                           -- Original URL in English
                                  original_url_lt TEXT,                                           -- Original URL in Lithuanian
                                  allergens_en TEXT[],                                            -- Allergens in English
                                  allergens_lt TEXT[],                                            -- Allergens in Lithuanian
                                  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),   -- Timestamp when the record was created
                                  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()    -- Timestamp when the record was last updated
);

-- Step 3: Create Indexes for JSONB, Category Fields, and Barcode (Already Unique)
CREATE INDEX idx_products_additives_lt ON aggregate.products USING gin(additives_lt);
CREATE INDEX idx_products_additives_en ON aggregate.products USING gin(additives_en);
CREATE INDEX idx_products_nutrition_per_100g ON aggregate.products USING gin(nutrition_per_100g);
CREATE INDEX idx_products_storage_conditions ON aggregate.products USING gin(storage_conditions);

CREATE INDEX idx_products_main_category_en ON aggregate.products(main_category_en);
CREATE INDEX idx_products_sub_category_en ON aggregate.products(sub_category_en);
CREATE INDEX idx_products_sub_sub_category_en ON aggregate.products(sub_sub_category_en);
CREATE INDEX idx_products_main_category_lt ON aggregate.products(main_category_lt);
CREATE INDEX idx_products_sub_category_lt ON aggregate.products(sub_category_lt);
CREATE INDEX idx_products_sub_sub_category_lt ON aggregate.products(sub_sub_category_lt);

-- Step 4: Create a Trigger to Automatically Update 'updated_at' on Record Modification
-- Ensure the pgcrypto extension is enabled for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create the function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

-- Create the trigger
CREATE TRIGGER trigger_update_updated_at
    BEFORE UPDATE ON aggregate.products
    FOR EACH ROW
    EXECUTE PROCEDURE update_updated_at_column();
