-- Alter system tables for the new DAO: update attributes MODE in "Report" table 

CREATE OR REPLACE FUNCTION patch_210_19() RETURNS VOID AS $$
BEGIN
	COMMENT ON COLUMN "Report"."SimpleReport" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."RichReport" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."Wizard" IS 'MODE: reserved';
	COMMENT ON COLUMN "Report"."Images" IS 'MODE: reserved';
END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_19();

DROP FUNCTION patch_210_19();