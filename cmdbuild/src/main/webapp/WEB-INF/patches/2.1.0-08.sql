-- Alter system tables for the new DAO: update attributes MODE in "LookUp" table 

CREATE OR REPLACE FUNCTION patch_210_08() RETURNS VOID AS $$
BEGIN
	COMMENT ON COLUMN "LookUp"."Type" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."ParentType" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."ParentId" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."Number" IS 'MODE: read';
	COMMENT ON COLUMN "LookUp"."IsDefault" IS 'MODE: read';
	
	CREATE TRIGGER "_SanityCheck"
		BEFORE INSERT OR UPDATE OR DELETE
		ON "LookUp"
		FOR EACH ROW
		EXECUTE PROCEDURE _cm_trigger_sanity_check();
		END
$$ LANGUAGE PLPGSQL;


SELECT patch_210_08();

DROP FUNCTION patch_210_08();