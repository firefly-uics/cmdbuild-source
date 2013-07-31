-- Update Email table to handle notify templates

CREATE OR REPLACE FUNCTION patch_212_06() RETURNS VOID AS $$

BEGIN
	PERFORM cm_create_class_attribute('Email', 'NotifyWith', 'text', null, false, false, 'MODE: write|DESCR: NotifyWith|INDEX: 10|BASEDSP: false|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_212_06();

DROP FUNCTION patch_212_06();