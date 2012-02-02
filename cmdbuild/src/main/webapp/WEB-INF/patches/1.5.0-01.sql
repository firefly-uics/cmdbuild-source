-- Alter system tables for the new DAO

CREATE OR REPLACE FUNCTION _patch_update_report_privileges_column() RETURNS VOID AS $$
DECLARE
	ReportId int;
	ReportName text;
	GroupName varchar;
	AllGroups varchar[];
BEGIN
	RAISE INFO 'Creating new report privileges column';
	ALTER TABLE "Report" ADD COLUMN "NewGroups" character varying[];
	COMMENT ON COLUMN "Report"."NewGroups" IS 'MODE: read';

	RAISE INFO 'Copying report privileges';
	FOR ReportId, ReportName IN SELECT "Id", "Code" FROM "Report" LOOP
		RAISE INFO '... %', ReportName;
		AllGroups := NULL;
		FOR GroupName IN SELECT "Role"."Code" FROM "Role"
				JOIN "Report" ON "Report"."Id" = ReportId AND "Role"."Id" = ANY ("Report"."Groups")
		LOOP
			AllGroups := AllGroups || GroupName;
		END LOOP;
		RAISE INFO '... -> %', AllGroups;
		UPDATE "Report" SET "NewGroups" = AllGroups WHERE "Id" = ReportId;
	END LOOP;

	RAISE INFO 'Dropping old report privileges column';
	ALTER TABLE "Report" DROP COLUMN "Groups";
	ALTER TABLE "Report" RENAME "NewGroups"  TO "Groups";
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _patch_update_menu_group_column() RETURNS VOID AS $$
BEGIN
	RAISE INFO 'Creating new menu group reference column';
	ALTER TABLE "Menu" ADD COLUMN "GroupName" text;
	COMMENT ON COLUMN "Menu"."GroupName" IS 'MODE: read';

	RAISE INFO 'Copying menu group references';
	UPDATE "Menu" SET "GroupName" = (SELECT "Code" FROM "Role" WHERE "Id"="IdGroup") WHERE "Status"='A';

	RAISE INFO 'Dropping old menu group reference column';
	ALTER TABLE "Menu" DROP COLUMN "IdGroup";

	RAISE INFO 'Menu table cleanup';
	ALTER TABLE "Menu" DISABLE TRIGGER USER;
	DELETE FROM "Menu" WHERE "Status"<>'A';
	ALTER TABLE "Menu" ENABLE TRIGGER USER;
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION patch_150_01() RETURNS VOID AS $$
BEGIN
	PERFORM _patch_update_report_privileges_column();
	DROP VIEW system_availablemenuitems;
	PERFORM _patch_update_menu_group_column();

	COMMENT ON COLUMN "Grant"."IdRole" IS 'MODE: read';
	COMMENT ON COLUMN "Grant"."IdGrantedClass" IS 'MODE: read';
	COMMENT ON COLUMN "Grant"."Mode" IS 'MODE: read';

	COMMENT ON COLUMN "Menu"."IdParent" IS 'MODE: read|DESCR: Parent Item, 0 means no parent';
	COMMENT ON COLUMN "Menu"."IdElementClass" IS 'MODE: read|DESCR: Class connect to this item';
	COMMENT ON COLUMN "Menu"."IdElementObj" IS 'MODE: read|DESCR: Object connected to this item, 0 means no object';
	--COMMENT ON COLUMN "Menu"."GroupName" IS 'MODE: read';
	COMMENT ON COLUMN "Menu"."Number" IS 'MODE: read|DESCR: Ordering';
	COMMENT ON COLUMN "Menu"."Type" IS 'MODE: read';

	COMMENT ON COLUMN "Report"."Type" IS 'MODE: read|DESCR: Tipo';
	COMMENT ON COLUMN "Report"."Query" IS 'MODE: read|DESCR: Query';
	COMMENT ON COLUMN "Report"."SimpleReport" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."RichReport" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."Wizard" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."Images" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."ImagesLength" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."ReportLength" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."IdClass" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."Groups" IS 'MODE: read';
	COMMENT ON COLUMN "Report"."ImagesName" IS 'MODE: read';
END
$$ LANGUAGE PLPGSQL;


SELECT patch_150_01();


DROP FUNCTION patch_150_01();
