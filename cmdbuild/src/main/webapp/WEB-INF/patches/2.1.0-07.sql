-- Alter system tables for the new DAO: update attributes MODE in "Role" table 

CREATE OR REPLACE FUNCTION patch_210_07() RETURNS VOID AS $$
BEGIN
	COMMENT ON COLUMN "Role"."DisabledCardTabs" IS 'MODE: read';
	COMMENT ON COLUMN "Role"."DisabledProcessTabs" IS 'MODE: read';
	COMMENT ON COLUMN "Role"."HideSidePanel" IS 'MODE: read';
	COMMENT ON COLUMN "Role"."FullScreenMode" IS 'MODE: read';
	COMMENT ON COLUMN "Role"."SimpleHistoryModeForCard" IS 'MODE: read';
	COMMENT ON COLUMN "Role"."SimpleHistoryModeForProcess" IS 'MODE: read';
	COMMENT ON COLUMN "Role"."ProcessWidgetAlwaysEnabled" IS 'MODE: read';
END
$$ LANGUAGE PLPGSQL;


SELECT patch_210_07();

DROP FUNCTION patch_210_07();