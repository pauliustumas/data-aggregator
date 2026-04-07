-- V15: Remove EU additive entries that have no E-number assigned.
-- These are pending/under-evaluation substances scraped from the FIP portal
-- without a finalized E-code; they render poorly on /e-numbers/* pages.
-- Restriction and legislation rows are removed via ON DELETE CASCADE.

DELETE FROM aggregate.eu_additives WHERE e_number IS NULL OR e_number = '';
