-- Create a table to manage the views

CREATE OR REPLACE FUNCTION patch_210_11() RETURNS VOID AS $$

BEGIN

	RAISE INFO 'Creating _View table';
	PERFORM cm_create_class('_View', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
	PERFORM cm_create_class_attribute('_View', 'Name', 'character varying', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'Filter', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'SourceClass', 'regclass', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'SourceFunction', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_View', 'Type', 'character varying', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_11();

DROP FUNCTION patch_210_11();