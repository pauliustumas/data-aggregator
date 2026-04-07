-- V16: Keep only entries whose e_number matches a real E-code pattern
-- (E followed by digits, optionally followed by a single letter, e.g. E150a).
-- This drops scraped junk like '.....', 'GroupI', 'xxx', '1210', '960b', etc.
-- Cascading FKs remove related restriction/legislation rows.

DELETE FROM aggregate.eu_additives
WHERE e_number !~* '^E\d{3,4}[a-z]?$';
