-- Add attributes "Type" and "IdPrivilegedObject" to Grant table

CREATE OR REPLACE FUNCTION patch_210_12() RETURNS VOID AS $$

BEGIN

	RAISE INFO 'Creating Type attribute';
	PERFORM cm_create_class_attribute('Grant', 'Type', 'varchar(70)', null, false, false, 'MODE: read');
	
	RAISE INFO 'Creating IdPrivilegedObject attribute';
	PERFORM cm_create_class_attribute('Grant', 'IdPrivilegedObject', 'integer', null, false, false, 'MODE: read');

	ALTER TABLE "Grant" DISABLE TRIGGER USER;
	RAISE INFO 'Updating all values for Type attribute to Class';
	UPDATE "Grant" SET "Type" = 'Class';
	ALTER TABLE "Grant" ENABLE TRIGGER USER;
	PERFORM cm_modify_class_attribute('Grant', 'Type', 'varchar(70)', null, true, false, 'MODE: read');

END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_12();

DROP FUNCTION patch_210_12();