-- Creates the Filters table

SELECT cm_create_class('_Filters', 'Class', 'MODE: reserved|TYPE: class|DESCR: Filters|SUPERCLASS: false|MANAGER: class|STATUS: active');
SELECT cm_create_class_attribute('_Filters', 'Master', 'int', null, false, false, 'MODE: write|DESCR: Master|INDEX: 1|STATUS: active');
SELECT cm_modify_class_attribute('_Filters', 'Code', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 2|STATUS: active');
SELECT cm_modify_class_attribute('_Filters', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_Filters', 'Filter', 'varchar', null, false, false, 'MODE: write|DESCR: Filter|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_Filters', 'TableId', 'regclass', null, true, false, 'MODE: write|DESCR: Class Reference|INDEX: 5|STATUS: active');

ALTER TABLE "_Filters" ADD CONSTRAINT filter_name_table_unique UNIQUE ("Code", "Master", "TableId");



