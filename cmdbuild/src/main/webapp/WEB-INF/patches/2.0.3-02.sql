-- Add table to store the GIS layers configuration

CREATE OR REPLACE FUNCTION patch_203_02() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class('_Layer', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
	PERFORM cm_create_class_attribute('_Layer', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'FullName', 'character varying', NULL, FALSE, TRUE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Index', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'MinimumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'MaximumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'MapStyle', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Name', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'GeoServerName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Type', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'Visibility', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
	PERFORM cm_create_class_attribute('_Layer', 'CardsBinding', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_203_02();

DROP FUNCTION patch_203_02();