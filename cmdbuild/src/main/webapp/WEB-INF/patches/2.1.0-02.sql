-- Alter system tables for the new DAO: create SanityCheck triggers for "User", "Role" and "Grant" tables

CREATE OR REPLACE FUNCTION patch_210_02() RETURNS VOID AS $$
BEGIN
	CREATE TRIGGER "_SanityCheck"
		BEFORE INSERT OR UPDATE OR DELETE
		ON "User"
		FOR EACH ROW
		EXECUTE PROCEDURE _cm_trigger_sanity_check();

	CREATE TRIGGER "_SanityCheck"
		BEFORE INSERT OR UPDATE OR DELETE
		ON "Role"
		FOR EACH ROW
		EXECUTE PROCEDURE _cm_trigger_sanity_check();

	CREATE TRIGGER "_SanityCheck"
		BEFORE INSERT OR UPDATE OR DELETE
		ON "Grant"
		FOR EACH ROW
		EXECUTE PROCEDURE _cm_trigger_sanity_check();
		
	ALTER TABLE "User" 
		ADD CONSTRAINT username_unique UNIQUE ("Username");
		
	ALTER TABLE "Role" ADD COLUMN "Active" boolean;
	ALTER TABLE "Role" ALTER COLUMN "Active" SET NOT NULL;
	ALTER TABLE "Role" ALTER COLUMN "Active" SET DEFAULT true;
	COMMENT ON COLUMN "Role"."Active" IS 'MODE: read';
	
	ALTER TABLE "User" ADD COLUMN "Active" boolean;
	ALTER TABLE "User" ALTER COLUMN "Active" SET NOT NULL;
	ALTER TABLE "User" ALTER COLUMN "Active" SET DEFAULT true;
	COMMENT ON COLUMN "User"."Active" IS 'MODE: read';
	
END
$$ LANGUAGE PLPGSQL;


SELECT patch_210_02();


DROP FUNCTION patch_210_02();

