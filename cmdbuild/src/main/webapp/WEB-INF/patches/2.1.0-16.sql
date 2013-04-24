-- Enable Restricted Administrator

CREATE OR REPLACE FUNCTION patch_210_16() RETURNS VOID AS $$

BEGIN
	COMMENT ON COLUMN "Role"."CloudAdmin" IS 'MODE: read';
END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_16();

DROP FUNCTION patch_210_16();