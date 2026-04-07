-- V18: Drop empty restriction and legislation rows that came from scrape stubs.
-- These cause "Conditions of Use" sections to render with no content.

DELETE FROM aggregate.eu_additive_restrictions
WHERE food_category_id IS NULL
  AND food_category_number IS NULL
  AND food_category_name_en IS NULL
  AND food_category_name_lt IS NULL
  AND restriction_type IS NULL
  AND restriction_value IS NULL
  AND restriction_comment_en IS NULL
  AND note_text_en IS NULL;

DELETE FROM aggregate.eu_additive_legislations
WHERE title_en IS NULL
  AND title_lt IS NULL
  AND text IS NULL
  AND eurlex_link IS NULL;
