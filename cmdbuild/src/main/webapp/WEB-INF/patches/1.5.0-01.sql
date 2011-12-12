-- Report privileges saved by name and not by ID

CREATE OR REPLACE FUNCTION patch_150_01() RETURNS VOID AS $$
DECLARE
	ReportId int;
	ReportName text;
	GroupName varchar;
	AllGroups varchar[];
BEGIN
	RAISE INFO 'Creating new privileges column';
	ALTER TABLE "Report" ADD COLUMN "NewGroups" character varying[];

	RAISE INFO 'Copying privileges';
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

	RAISE INFO 'Dropping old privileges column';
	ALTER TABLE "Report" DROP COLUMN "Groups";
	ALTER TABLE "Report" RENAME "NewGroups"  TO "Groups";
	COMMENT ON COLUMN "Report"."Groups" IS 'MODE: reserved';
END
$$ LANGUAGE PLPGSQL;

SELECT patch_150_01();

DROP FUNCTION patch_150_01();
