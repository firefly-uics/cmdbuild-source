-- Create table to manage Bim Mapper Configuration

CREATE OR REPLACE FUNCTION patch_220_02() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'create table _BimMapperInfo';
	
	PERFORM cm_create_class('_BimMapperInfo', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('_BimMapperInfo', 'ClassName', 'varchar', null, true, true, 'MODE: write|DESCR: ClassName|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_BimMapperInfo', 'Active', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: Active|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_BimMapperInfo', 'BimRoot', 'boolean', 'FALSE', true, false, 'MODE: write|DESCR: BimRoot|INDEX: 3|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_220_02();

DROP FUNCTION patch_220_02();
