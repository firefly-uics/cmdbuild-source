-- Create table to manage Bim Layers Configuration

CREATE OR REPLACE FUNCTION patch_220_02() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'create table _BimLayer';
	PERFORM cm_create_class('_BimLayer', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'ClassName', 'varchar', null, true, true, 'MODE: write|DESCR: ClassName|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Root', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Root|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Active', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Active|INDEX: 3|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Export', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Export|INDEX: 4|STATUS: active');
	PERFORM cm_create_class_attribute('_BimLayer', 'Container', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Container|INDEX: 5|STATUS: active');

END

$$ LANGUAGE PLPGSQL;

SELECT patch_220_02();

DROP FUNCTION patch_220_02();
