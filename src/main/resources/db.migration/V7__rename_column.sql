ALTER TABLE aggregate.products
    RENAME COLUMN manufacturer_description TO manufacturer_description_lt;

ALTER TABLE aggregate.products
    ADD COLUMN manufacturer_description_en TEXT;
