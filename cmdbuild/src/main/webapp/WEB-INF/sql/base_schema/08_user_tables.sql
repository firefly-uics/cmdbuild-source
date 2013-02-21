---------------------------------------------
-- User
---------------------------------------------

SELECT cm_create_class('User', 'Class', 'MODE: reserved|TYPE: class|DESCR: Users|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('User', 'Username', 'varchar(40)', null, true, true, 'MODE: read|DESCR: Username|INDEX: 1|BASEDSP: true|STATUS: active');
SELECT cm_create_class_attribute('User', 'Password', 'varchar(40)', null, false, false, 'MODE: read|DESCR: Password|INDEX: 2|BASEDSP: false|STATUS: active');
SELECT cm_create_class_attribute('User', 'Email', 'varchar(320)', null, false, false, 'MODE: read|DESCR: Email|INDEX: 5');
SELECT cm_create_class_attribute('User', 'Active', 'boolean', 'true', true, false, 'MODE: read');

---------------------------------------------
-- Role
---------------------------------------------

SELECT cm_create_class('Role', 'Class', 'MODE: reserved|TYPE: class|DESCR: Groups|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('Role', 'Administrator', 'boolean', null, false, false, 'MODE: read|DESCR: Administrator|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('Role', 'startingClass', 'regclass', null, false, false, 'MODE: read|DESCR: Starting Class|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('Role', 'Email', 'varchar(320)', null, false, false, 'MODE: read|DESCR: Email|INDEX: 5');
SELECT cm_create_class_attribute('Role', 'DisabledModules', 'varchar[]', null, false, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'DisabledCardTabs', 'character varying[]', null, false, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'DisabledProcessTabs', 'character varying[]', null, false, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'HideSidePanel', 'boolean', 'false', true, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'FullScreenMode', 'boolean', 'false', true, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'SimpleHistoryModeForCard', 'boolean', 'false', true, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'SimpleHistoryModeForProcess', 'boolean', 'false', true, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'ProcessWidgetAlwaysEnabled', 'boolean', 'false', true, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'Active', 'boolean', 'true', true, false, 'MODE: read');
SELECT cm_create_class_attribute('Role', 'CloudAdmin', 'boolean', 'false', true, false, 'MODE: reserved');

---------------------------------------------
-- Map_UserRole
---------------------------------------------

SELECT cm_create_domain('UserRole', 'MODE: reserved|TYPE: domain|CLASS1: User|CLASS2: Role|DESCRDIR: has role|DESCRINV: contains|CARDIN: N:N|STATUS: active');

SELECT cm_create_domain_attribute('UserRole', 'DefaultGroup', 'boolean', '', false, false, 'MODE: read|FIELDMODE: write|DESCR: Default Group|INDEX: 1|BASEDSP: true|STATUS: active');

CREATE UNIQUE INDEX idx_map_userrole_defaultgroup
  ON "Map_UserRole"
  USING btree
  ((
CASE
    WHEN "Status"::text = 'N'::text THEN NULL::regclass
    ELSE "IdClass1"
END), (
CASE
    WHEN "Status"::text = 'N'::text THEN NULL::integer
    ELSE "IdObj1"
END), (
CASE
    WHEN "DefaultGroup" THEN TRUE
    ELSE NULL::boolean
END));

---------------------------------------------
-- Grant
---------------------------------------------

SELECT cm_create_class('Grant', 'Class', 'MODE: reserved|TYPE: class|DESCR: Privileges |SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('Grant', 'IdRole', 'integer', null, true, false, 'MODE: read');
SELECT cm_create_class_attribute('Grant', 'IdGrantedClass', 'regclass', null, false, false, 'MODE: read');
SELECT cm_create_class_attribute('Grant', 'Mode', 'varchar(1)', null, true, false, 'MODE: read');

---------------------------------------------
-- Filters
---------------------------------------------

SELECT cm_create_class('_Filters', 'Class', 'MODE: reserved|TYPE: class|DESCR: Filters|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Filters', 'Master', 'int', null, false, false, 'MODE: write|DESCR: Master|INDEX: 1|STATUS: active');
SELECT cm_modify_class_attribute('_Filters', 'Code', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 2|STATUS: active');
SELECT cm_modify_class_attribute('_Filters', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_Filters', 'Filter', 'varchar', null, false, false, 'MODE: write|DESCR: Filter|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_Filters', 'TableId', 'regclass', null, true, false, 'MODE: write|DESCR: Class Reference|INDEX: 5|STATUS: active');

ALTER TABLE "_Filters" ADD CONSTRAINT filter_name_table_unique UNIQUE ("Code", "Master", "TableId");
