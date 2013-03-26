-- Add attributes "PrivilegeFilter" and "DisabledAttributes" in table "Grant", used to add privileges for row and columns

CREATE OR REPLACE FUNCTION patch_210_14() RETURNS VOID AS $$

BEGIN

	RAISE INFO 'Creating PrivilegeFilter attribute';
	PERFORM cm_create_class_attribute('Grant', 'PrivilegeFilter', 'text', null, false, false, 'MODE: read|DESCR: filter for row privileges|INDEX: 6|STATUS: active');
	
	RAISE INFO 'Creating DisabledAttributes attribute';
	PERFORM cm_create_class_attribute('Grant', 'DisabledAttributes', 'varchar[]', null, false, false, 'MODE: read|DESCR: disabled attributes for column privileges|INDEX: 7|STATUS: active');

END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_14();

DROP FUNCTION patch_210_14();