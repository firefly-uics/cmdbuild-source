-- Alter system tables for the new DAO: "LookUp" table is now a simple class 

CREATE OR REPLACE FUNCTION patch_210_17() RETURNS VOID AS $$
BEGIN
	RAISE INFO 'creating backup schema';
	CREATE SCHEMA "backup_lookup_21";
	ALTER TABLE "LookUp" SET SCHEMA "backup_lookup_21";

	RAISE INFO 'creating new table';
	PERFORM cm_create_class('LookUp', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Lookup list|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('LookUp', 'Code', 'character varying(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|BASEDSP: true');
	PERFORM cm_create_class_attribute('LookUp', 'Description', 'character varying(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|BASEDSP: true');
	PERFORM cm_create_class_attribute('LookUp', 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Annotazioni');
	PERFORM cm_create_class_attribute('LookUp', 'Type', 'character varying(64)', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'ParentType', 'character varying(64)', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'ParentId', 'integer', NULL, FALSE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'Number', 'integer', NULL, TRUE, FALSE, 'MODE: read');
	PERFORM cm_create_class_attribute('LookUp', 'IsDefault', 'boolean', NULL, FALSE, FALSE, 'MODE: read');

	RAISE INFO 'copying data';
	INSERT INTO "LookUp"
		("Id", "IdClass", "Code", "Description", "ParentType", "ParentId", "Type", "IsDefault", "Number", "Notes", "Status")
		SELECT "Id", '"LookUp"'::regclass, "Code", "Description", "ParentType", "ParentId", "Type", "IsDefault", "Number", "Notes", "Status"
			FROM "backup_lookup_21"."LookUp";
END
$$ LANGUAGE PLPGSQL;


SELECT patch_210_17();

DROP FUNCTION patch_210_17();