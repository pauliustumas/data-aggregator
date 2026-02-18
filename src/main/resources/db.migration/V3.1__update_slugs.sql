ALTER TABLE aggregate.category_slugs
    ADD COLUMN IF NOT EXISTS id VARCHAR;

-- 2. Populate 'id' column with concatenated slug values
UPDATE aggregate.category_slugs
SET id = CONCAT(main_category_slug_en, '-', sub_category_slug_en, '-', sub_sub_category_slug_en)
WHERE id IS NULL;

-- 3. Ensure 'id' column has no NULLs
ALTER TABLE aggregate.category_slugs
    ALTER COLUMN id SET NOT NULL;

-- 4. Drop the existing composite primary key constraint
-- Replace 'category_slugs_pkey' with your actual constraint name if different
ALTER TABLE aggregate.category_slugs
DROP CONSTRAINT IF EXISTS category_slugs_pkey;

-- 5. Add a new primary key constraint on 'id'
ALTER TABLE aggregate.category_slugs
    ADD PRIMARY KEY (id);