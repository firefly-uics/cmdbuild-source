-- Creates the Filter table

SELECT cm_create_class('_Filter', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Filter|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Code', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'IdOwner', 'int', null, false, false, 'MODE: write|DESCR: IdOwner|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Filter', 'text', null, false, false, 'MODE: write|DESCR: Filter|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'IdSourceClass', 'regclass', null, true, false, 'MODE: write|DESCR: Class Reference|INDEX: 5|STATUS: active');

ALTER TABLE "_Filter" ADD CONSTRAINT filter_name_table_unique UNIQUE ("Code", "IdOwner", "IdSourceClass");



