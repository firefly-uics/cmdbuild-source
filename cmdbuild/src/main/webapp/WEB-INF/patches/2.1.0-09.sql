-- Update the Menu table management

CREATE OR REPLACE FUNCTION patch_210_09() RETURNS VOID AS $$
BEGIN
	RAISE INFO 'Creating new menu group reference column';
	ALTER TABLE "Menu" ADD COLUMN "GroupName" text;
	COMMENT ON COLUMN "Menu"."GroupName" IS 'MODE: read';

	RAISE INFO 'Copying menu group references';
	UPDATE "Menu" SET "GroupName" = (SELECT "Code" FROM "Role" WHERE "Id"="IdGroup") WHERE "Status"='A';

	RAISE INFO 'Dropping old menu group reference column';
	ALTER TABLE "Menu" DROP COLUMN "IdGroup" CASCADE;

	RAISE INFO 'Menu table cleanup';
	ALTER TABLE "Menu" DISABLE TRIGGER USER;
	DELETE FROM "Menu" WHERE "Status"<>'A';
	ALTER TABLE "Menu" ENABLE TRIGGER USER;

	RAISE INFO 'Set to read the base column';
	COMMENT ON COLUMN "Menu"."IdParent" IS 'MODE: read|DESCR: Parent Item, 0 means no parent';
	COMMENT ON COLUMN "Menu"."IdElementClass" IS 'MODE: read|DESCR: Class connect to this item';
	COMMENT ON COLUMN "Menu"."IdElementObj" IS 'MODE: read|DESCR: Object connected to this item, 0 means no object';
	COMMENT ON COLUMN "Menu"."Number" IS 'MODE: read|DESCR: Ordering';
	COMMENT ON COLUMN "Menu"."Type" IS 'MODE: read|DESCR: Group owner of this item, 0 means default group';
	COMMENT ON COLUMN "Menu"."GroupName" IS 'MODE: read';

END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_09();

DROP FUNCTION patch_210_09();