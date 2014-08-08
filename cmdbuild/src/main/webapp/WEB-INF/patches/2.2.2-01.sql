-- Creates AccountTemplate domain and Account attribute for _EmailTemplate class

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
BEGIN
	PERFORM cm_create_domain('AccountTemplate', 'MODE: reserved|TYPE: domain|CLASS1: _EmailAccount|CLASS2: _EmailTemplate|DESCRDIR: is default|DESCRINV: has default|CARDIN: 1:N|STATUS: active');
	PERFORM cm_create_class_attribute('_EmailTemplate', 'Account', 'integer', null, false, false, 'MODE: user|FIELDMODE: write|DESCR: Account|INDEX: 8|REFERENCEDOM: AccountTemplate|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active');
END;
$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION IF EXISTS apply_patch();