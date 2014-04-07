-- Add column to _DomainTreeNavigation table for storing filters

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
BEGIN
	RAISE INFO 'add column to _DomainTreeNavigation table';
	PERFORM cm_create_class_attribute('_DomainTreeNavigation', 'TargetFilter', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
END
$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION IF EXISTS apply_patch();
