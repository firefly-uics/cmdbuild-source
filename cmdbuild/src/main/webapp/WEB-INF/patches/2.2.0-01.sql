-- Create table to manage BIM Projects

CREATE OR REPLACE FUNCTION patch_220_01() RETURNS VOID AS $$

BEGIN
	RAISE INFO 'create table _BIMProject';

	PERFORM cm_create_class('_BIMProject', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('_BIMProject', 'Name', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active');
	PERFORM cm_create_class_attribute('_BIMProject', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active');
	PERFORM cm_create_class_attribute('_BIMProject', 'ProjectId', 'varchar', null, true, true, 'MODE: write|DESCR: Project ID|INDEX: 3|STATUS: active');
	PERFORM cm_create_class_attribute('_BIMProject', 'Active', 'boolean', 'TRUE', true, false, 'MODE: write|DESCR: Active|INDEX: 4|STATUS: active');
	PERFORM cm_create_class_attribute('_BIMProject', 'LastCheckin', 'timestamp', null, false, false, 'MODE: write|DESCR: Last Checkin|INDEX: 5|STATUS: active');
END

$$ LANGUAGE PLPGSQL;

SELECT patch_220_01();

DROP FUNCTION patch_220_01();
