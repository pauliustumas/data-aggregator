-- ====================================================
-- Script: initialize_category_slugs.sql
-- Description: Creates and initializes the 'category_slugs' table with both slug and non-slug category names within the 'aggregate' schema
-- Author: [Your Name]
-- Date: [Current Date]
-- ====================================================

-- 0. Ensure the 'aggregate' schema exists
CREATE SCHEMA IF NOT EXISTS aggregate;

-- 1. Drop the table if it already exists to ensure a fresh creation
DROP TABLE IF EXISTS aggregate.category_slugs;

-- 2. Create the new table with both slug and non-slug columns
CREATE TABLE aggregate.category_slugs (
    -- Slug Columns (English)
                                          main_category_slug_en VARCHAR(255) NOT NULL,
                                          sub_category_slug_en VARCHAR(255) NOT NULL,
                                          sub_sub_category_slug_en VARCHAR(255) NOT NULL,

    -- Slug Columns (Lithuanian)
                                          main_category_slug_lt VARCHAR(255),
                                          sub_category_slug_lt VARCHAR(255),
                                          sub_sub_category_slug_lt VARCHAR(255),

    -- Original Non-Slug Columns (English)
                                          main_category_en VARCHAR(255) NOT NULL,
                                          sub_category_en VARCHAR(255) NOT NULL,
                                          sub_sub_category_en VARCHAR(255) NOT NULL,

    -- Original Non-Slug Columns (Lithuanian)
                                          main_category_lt VARCHAR(255),
                                          sub_category_lt VARCHAR(255),
                                          sub_sub_category_lt VARCHAR(255),

    -- 3. Define Primary Key
                                          PRIMARY KEY (main_category_slug_en, sub_category_slug_en, sub_sub_category_slug_en)
);

-- 4. Insert distinct records into the new table
INSERT INTO aggregate.category_slugs (
    main_category_slug_en,
    sub_category_slug_en,
    sub_sub_category_slug_en,
    main_category_slug_lt,
    sub_category_slug_lt,
    sub_sub_category_slug_lt,
    main_category_en,
    sub_category_en,
    sub_sub_category_en,
    main_category_lt,
    sub_category_lt,
    sub_sub_category_lt
)
SELECT DISTINCT
    main_category_en AS main_category_slug_en,
    sub_category_en AS sub_category_slug_en,
    sub_sub_category_en AS sub_sub_category_slug_en,
    main_category_lt AS main_category_slug_lt,
    sub_category_lt AS sub_category_slug_lt,
    sub_sub_category_lt AS sub_sub_category_slug_lt,
    main_category_en,
    sub_category_en,
    sub_sub_category_en,
    main_category_lt,
    sub_category_lt,
    sub_sub_category_lt
FROM aggregate.products
WHERE main_category_en IS NOT NULL
  AND sub_category_en IS NOT NULL
  AND sub_sub_category_en IS NOT NULL
ORDER BY main_category_en ASC, sub_category_en ASC, sub_sub_category_en ASC;

-- 5. Create Indexes to Optimize Query Performance

-- Index on English Main Category Slug
CREATE INDEX idx_category_slugs_main_en ON aggregate.category_slugs (main_category_slug_en);

-- Index on English Sub Category Slug
CREATE INDEX idx_category_slugs_sub_en ON aggregate.category_slugs (sub_category_slug_en);

-- Index on Lithuanian Main Category Slug
CREATE INDEX idx_category_slugs_main_lt ON aggregate.category_slugs (main_category_slug_lt);

-- Index on Lithuanian Sub Category Slug
CREATE INDEX idx_category_slugs_sub_lt ON aggregate.category_slugs (sub_category_slug_lt);

-- 6. Add Comments to Columns for Better Documentation

COMMENT ON COLUMN aggregate.category_slugs.main_category_slug_en IS 'English main category slug';
COMMENT ON COLUMN aggregate.category_slugs.sub_category_slug_en IS 'English sub category slug';
COMMENT ON COLUMN aggregate.category_slugs.sub_sub_category_slug_en IS 'English sub-sub category slug';
COMMENT ON COLUMN aggregate.category_slugs.main_category_slug_lt IS 'Lithuanian main category slug';
COMMENT ON COLUMN aggregate.category_slugs.sub_category_slug_lt IS 'Lithuanian sub category slug';
COMMENT ON COLUMN aggregate.category_slugs.sub_sub_category_slug_lt IS 'Lithuanian sub-sub category slug';
COMMENT ON COLUMN aggregate.category_slugs.main_category_en IS 'Original English main category name';
COMMENT ON COLUMN aggregate.category_slugs.sub_category_en IS 'Original English sub category name';
COMMENT ON COLUMN aggregate.category_slugs.sub_sub_category_en IS 'Original English sub-sub category name';
COMMENT ON COLUMN aggregate.category_slugs.main_category_lt IS 'Original Lithuanian main category name';
COMMENT ON COLUMN aggregate.category_slugs.sub_category_lt IS 'Original Lithuanian sub category name';
COMMENT ON COLUMN aggregate.category_slugs.sub_sub_category_lt IS 'Original Lithuanian sub-sub category name';

-- ====================================================
-- End of Script
-- ====================================================
