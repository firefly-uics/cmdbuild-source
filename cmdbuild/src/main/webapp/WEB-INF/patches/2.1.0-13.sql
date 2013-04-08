-- Add attributes "Template" in table "_Filters", used to distinguish user filters from group filters

CREATE OR REPLACE FUNCTION patch_210_13() RETURNS VOID AS $$

BEGIN

	RAISE INFO 'Creating Template attribute';
	PERFORM cm_create_class_attribute('_Filters', 'Template', 'boolean', null, false, false, 'MODE: write|DESCR: User or group filter|INDEX: 6|STATUS: active');
	
	ALTER TABLE "_Filters" DISABLE TRIGGER USER;
	RAISE INFO 'Updating all values for Template attribute to false';
	UPDATE "_Filters" SET "Template" = false;
	ALTER TABLE "_Filters" ENABLE TRIGGER USER;
	PERFORM cm_modify_class_attribute('_Filters', 'Template', 'boolean', 'false', true, false, 'MODE: write|DESCR: User or group filter|INDEX: 6|STATUS: active');

END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_13();

DROP FUNCTION patch_210_13();