-- Alter system tables for the new DAO: "User", "Role", "Grant" tables are now simple classes 

CREATE OR REPLACE FUNCTION patch_210_18() RETURNS VOID AS $$
BEGIN
	
	RAISE INFO 'creating backup schema';
	CREATE SCHEMA "backup_users_21";
	
	-- 'User' table
	ALTER TABLE "User" SET SCHEMA "backup_users_21";
	ALTER TABLE "User_history" SET SCHEMA "backup_users_21";

	RAISE INFO 'creating new table User';
	PERFORM cm_create_class('User', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Users|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('User', 'Code', 'character varying(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|BASEDSP: true|INDEX: 1');
	PERFORM cm_create_class_attribute('User', 'Description', 'character varying(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|BASEDSP: true|INDEX: 2');
	PERFORM cm_create_class_attribute('User', 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: read|INDEX: 3');
	PERFORM cm_create_class_attribute('User', 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Annotazioni|INDEX: 4');
	PERFORM cm_create_class_attribute('User', 'Username', 'varchar(40)', null, true, true, 'MODE: read|DESCR: Username|INDEX: 5|BASEDSP: true|STATUS: active');
	PERFORM cm_create_class_attribute('User', 'Password', 'varchar(40)', null, false, false, 'MODE: read|DESCR: Password|INDEX: 6|BASEDSP: false|STATUS: active');
	PERFORM cm_create_class_attribute('User', 'Email', 'varchar(320)', null, false, false, 'MODE: read|DESCR: Email|INDEX: 7');

	RAISE INFO 'copying data into table User';
	INSERT INTO "User"
		("Id", "IdClass", "User", "BeginDate", "Code", "Description", "Status", "Notes", "Username", "Password", "Email")
		SELECT "Id", '"User"'::regclass, "User", "BeginDate", "Code", "Description", "Status", "Notes", "Username", "Password", "Email"
			FROM  "backup_users_21"."User" WHERE "Status" = 'A' OR "Status" = 'N';
			
	
	-- 'Role' table
	ALTER TABLE "Role" SET SCHEMA "backup_users_21";
	ALTER TABLE "Role_history" SET SCHEMA "backup_users_21";

	RAISE INFO 'creating new table Role';
	PERFORM cm_create_class('Role', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Groups|SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('Role', 'Code', 'character varying(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|BASEDSP: true|INDEX: 1');
	PERFORM cm_create_class_attribute('Role', 'Description', 'character varying(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|BASEDSP: true|INDEX: 2');
	PERFORM cm_create_class_attribute('Role', 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: read|INDEX: 3');
	PERFORM cm_create_class_attribute('Role', 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Annotazioni|INDEX: 4');
	PERFORM cm_create_class_attribute('Role', 'Administrator', 'boolean', null, false, false, 'MODE: read|DESCR: Administrator|INDEX: 5|STATUS: active');
	PERFORM cm_create_class_attribute('Role', 'startingClass', 'regclass', null, false, false, 'MODE: read|DESCR: Starting Class|INDEX: 6|STATUS: active');
	PERFORM cm_create_class_attribute('Role', 'Email', 'varchar(320)', null, false, false, 'MODE: read|DESCR: Email|INDEX: 7');
	PERFORM cm_create_class_attribute('Role', 'DisabledModules', 'varchar[]', null, false, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'DisabledCardTabs', 'character varying[]', null, false, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'DisabledProcessTabs', 'character varying[]', null, false, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'HideSidePanel', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'FullScreenMode', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'SimpleHistoryModeForCard', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'SimpleHistoryModeForProcess', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'ProcessWidgetAlwaysEnabled', 'boolean', 'false', true, false, 'MODE: read');
	PERFORM cm_create_class_attribute('Role', 'CloudAdmin', 'boolean', 'false', true, false, 'MODE: read');

	RAISE INFO 'copying data into table Role';
	INSERT INTO "Role"
		("Id", "IdClass", "User", "BeginDate", "Code", "Description", "Status", "Notes", "Administrator", "startingClass", "Email", "DisabledModules", "DisabledCardTabs", "DisabledProcessTabs", "HideSidePanel", "FullScreenMode", "SimpleHistoryModeForCard", "SimpleHistoryModeForProcess", "ProcessWidgetAlwaysEnabled", "CloudAdmin")
		SELECT "Id", '"Role"'::regclass, "User", "BeginDate", "Code", "Description", "Status", "Notes", "Administrator", "startingClass", "Email", "DisabledModules", "DisabledCardTabs", "DisabledProcessTabs", "HideSidePanel", "FullScreenMode", "SimpleHistoryModeForCard", "SimpleHistoryModeForProcess", "ProcessWidgetAlwaysEnabled", "CloudAdmin"
			FROM  "backup_users_21"."Role" WHERE "Status" = 'A' OR "Status" = 'N';
			
			
			
	-- 'Grant' table
	ALTER TABLE "Grant" SET SCHEMA "backup_users_21";
	ALTER TABLE "Grant_history" SET SCHEMA "backup_users_21";

	RAISE INFO 'creating new table Grant';
	PERFORM cm_create_class('Grant', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Privileges |SUPERCLASS: false|STATUS: active');
	PERFORM cm_create_class_attribute('Grant', 'Code', 'character varying(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|BASEDSP: true|INDEX: 1');
	PERFORM cm_create_class_attribute('Grant', 'Description', 'character varying(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|BASEDSP: true|INDEX: 2');
	PERFORM cm_create_class_attribute('Grant', 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: read|INDEX: 3');
	PERFORM cm_create_class_attribute('Grant', 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Annotazioni|INDEX: 4');
	PERFORM cm_create_class_attribute('Grant', 'IdRole', 'integer', null, true, false, 'MODE: read|DESCR: RoleId|INDEX: 5|STATUS: active');
	PERFORM cm_create_class_attribute('Grant', 'IdGrantedClass', 'regclass', null, false, false, 'MODE: read|DESCR: granted class|INDEX: 6|STATUS: active');
	PERFORM cm_create_class_attribute('Grant', 'Mode', 'varchar(1)', null, true, false, 'MODE: read|DESCR: mode|INDEX: 7|STATUS: active');
	PERFORM cm_create_class_attribute('Grant', 'Type', 'varchar(70)', null, true, false, 'MODE: read|DESCR: type of grant|INDEX: 8|STATUS: active');
	PERFORM cm_create_class_attribute('Grant', 'IdPrivilegedObject', 'integer', null, false, false, 'MODE: read|DESCR: id of privileged object|INDEX: 9|STATUS: active');
	PERFORM cm_create_class_attribute('Grant', 'PrivilegeFilter', 'text', null, false, false, 'MODE: read|DESCR: filter for row privileges|INDEX: 10|STATUS: active');
	PERFORM cm_create_class_attribute('Grant', 'DisabledAttributes', 'varchar[]', null, false, false, 'MODE: read|DESCR: disabled attributes for column privileges|INDEX: 11|STATUS: active');

	RAISE INFO 'copying data into table Grant';
	INSERT INTO "Grant"
		("Id", "IdClass", "User", "BeginDate", "Code", "Description", "Status", "Notes", "IdRole", "IdGrantedClass", "Mode", "Type", "IdPrivilegedObject", "PrivilegeFilter", "DisabledAttributes")
		SELECT "Id", '"Grant"'::regclass, "User", "BeginDate", "Code", "Description", "Status", "Notes", "IdRole", "IdGrantedClass", "Mode", "Type", "IdPrivilegedObject", "PrivilegeFilter", "DisabledAttributes"
			FROM  "backup_users_21"."Grant" WHERE "Status" = 'A';
			
END
$$ LANGUAGE PLPGSQL;

SELECT patch_210_18();
DROP FUNCTION patch_210_18();