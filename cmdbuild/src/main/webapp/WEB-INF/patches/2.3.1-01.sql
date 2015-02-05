-- Creates BccAddresses attribute for Email class

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
BEGIN
	PERFORM cm_create_class_attribute('Email', 'BccAddresses', 'text', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: BCC|INDEX: 14|BASEDSP: false|STATUS: active');
END;
$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION IF EXISTS apply_patch();