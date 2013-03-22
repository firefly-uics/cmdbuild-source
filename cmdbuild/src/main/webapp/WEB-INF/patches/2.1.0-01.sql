-- Alter system tables for the new DAO: update attributes MODE in "Grant" table 

CREATE OR REPLACE FUNCTION patch_210_01() RETURNS VOID AS $$
BEGIN
	COMMENT ON COLUMN "Grant"."IdRole" IS 'MODE: read';
	COMMENT ON COLUMN "Grant"."IdGrantedClass" IS 'MODE: read';
	COMMENT ON COLUMN "Grant"."Mode" IS 'MODE: read';
END
$$ LANGUAGE PLPGSQL;


SELECT patch_210_01();


DROP FUNCTION patch_210_01();

