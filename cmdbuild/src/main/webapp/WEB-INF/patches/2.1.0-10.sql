-- Update the Report table management

CREATE OR REPLACE FUNCTION patch_210_10() RETURNS VOID AS $$
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

SELECT patch_210_10();

DROP FUNCTION patch_210_10();