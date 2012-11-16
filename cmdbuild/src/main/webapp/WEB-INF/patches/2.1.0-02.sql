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
END
$$ LANGUAGE PLPGSQL;


SELECT patch_210_02();


DROP FUNCTION patch_210_02();

