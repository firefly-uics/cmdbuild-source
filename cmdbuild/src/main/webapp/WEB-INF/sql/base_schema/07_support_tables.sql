---------------------------------------------
-- Menu
---------------------------------------------

SELECT cm_create_class('Menu', 'Class', 'MODE: reserved|TYPE: class|DESCR: Menu|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('Menu', 'IdParent', 'integer', '0', false, false, 'MODE: read|DESCR: Parent Item, 0 means no parent');
SELECT cm_create_class_attribute('Menu', 'IdElementClass', 'regclass', null, false, false, 'MODE: read|DESCR: Class connect to this item');
SELECT cm_create_class_attribute('Menu', 'IdElementObj', 'integer', '0', true, false, 'MODE: read|DESCR: Object connected to this item, 0 means no object');
SELECT cm_create_class_attribute('Menu', 'Number', 'integer', '0', true, false, 'MODE: read|DESCR: Ordering');
SELECT cm_create_class_attribute('Menu', 'GroupName', 'text', null, true, false, 'MODE: read');
SELECT cm_create_class_attribute('Menu', 'Type', 'varchar (70)', '', true, false, 'MODE: read');

---------------------------------------------
-- Report
---------------------------------------------

CREATE TABLE "Report"
(
  "Id" integer NOT NULL DEFAULT _cm_new_card_id(),
  "Code" varchar(40),
  "Description" varchar(100),
  "Status" varchar(1),
  "User" varchar(40),
  "BeginDate" timestamp without time zone NOT NULL DEFAULT now(),
  "Type" varchar(20),
  "Query" text,
  "SimpleReport" bytea,
  "RichReport" bytea,
  "Wizard" bytea,
  "Images" bytea,
  "ImagesLength" integer[],
  "ReportLength" integer[],
  "IdClass" regclass,
  "Groups" varchar[],
  "ImagesName" varchar[],
  CONSTRAINT "Report_pkey" PRIMARY KEY ("Id")
);
COMMENT ON TABLE "Report" IS 'MODE: reserved|TYPE: class|DESCR: Report|SUPERCLASS: false|STATUS: active';
COMMENT ON COLUMN "Report"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Code" IS 'MODE: read|DESCR: Code';
COMMENT ON COLUMN "Report"."Description" IS 'MODE: read|DESCR: Description';
COMMENT ON COLUMN "Report"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Type" IS 'MODE: read|DESCR: Type';
COMMENT ON COLUMN "Report"."Query" IS 'MODE: read|DESCR: Query';
COMMENT ON COLUMN "Report"."SimpleReport" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."RichReport" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Wizard" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Images" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."ImagesLength" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."ReportLength" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Groups" IS 'MODE: read';
COMMENT ON COLUMN "Report"."ImagesName" IS 'MODE: read';

CREATE UNIQUE INDEX "Report_unique_code"
  ON "Report"
  USING btree
  ((
CASE
    WHEN "Code"::text = ''::text OR "Status"::text <> 'A'::text THEN NULL::text
    ELSE "Code"::text
END));

---------------------------------------------
-- Create Metadata class
---------------------------------------------

SELECT cm_create_class('Metadata', 'Class', 'MODE: reserved|TYPE: class|DESCR: Metadata|SUPERCLASS: false|STATUS: active');

COMMENT ON COLUMN "Metadata"."Code" IS 'MODE: read|DESCR: Schema|INDEX: 1';
COMMENT ON COLUMN "Metadata"."Description" IS 'MODE: read|DESCR: Key|INDEX: 2';
COMMENT ON COLUMN "Metadata"."Notes" IS 'MODE: read|DESCR: Value|INDEX: 3';

---------------------------------------------
-- Create Scheduler class
---------------------------------------------

SELECT cm_create_class('Scheduler', 'Class', 'MODE: reserved|TYPE: class|DESCR: Scheduler|SUPERCLASS: false|STATUS: active');

COMMENT ON COLUMN "Scheduler"."Code" IS 'MODE: read|DESCR: Job Type|INDEX: 1';
COMMENT ON COLUMN "Scheduler"."Description" IS 'MODE: read|DESCR: Job Description|INDEX: 2';
COMMENT ON COLUMN "Scheduler"."Notes" IS 'MODE: read|DESCR: Job Parameters|INDEX: 3';

SELECT cm_create_class_attribute('Scheduler', 'CronExpression', 'text', '', true, false, 'MODE: read|DESCR: Cron Expression|STATUS: active');
SELECT cm_create_class_attribute('Scheduler', 'Detail', 'text', '', true, false, 'MODE: read|DESCR: Job Detail|STATUS: active');

---------------------------------------------
-- Create Data Store Templates class
---------------------------------------------

SELECT cm_create_class('_Templates', NULL, 'DESCR: Templates|MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_Templates', 'Name', 'text', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Templates', 'Template', 'text', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create Dashboard class
---------------------------------------------

SELECT cm_create_class('_Dashboards', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_Dashboards', 'Definition', 'text', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create DomainTreeNavigation class
---------------------------------------------

SELECT cm_create_class('_DomainTreeNavigation', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'IdParent', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'IdGroup', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'Type', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'DomainName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'Direct', 'boolean', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'BaseNode', 'boolean', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'TargetClassName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_DomainTreeNavigation', 'TargetClassDescription', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create Layer class
---------------------------------------------

SELECT cm_create_class('_Layer', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_Layer', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'FullName', 'character varying', NULL, FALSE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Index', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'MinimumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'MaximumZoom', 'integer', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'MapStyle', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Name', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'GeoServerName', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Type', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'Visibility', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_Layer', 'CardsBinding', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create Widget class
---------------------------------------------

SELECT cm_create_class('_Widget', 'Class', 'MODE: reserved|TYPE: class|DESCR: Widget|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Widget', 'Definition', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Create Views class
---------------------------------------------

SELECT cm_create_class('_View', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_View', 'Name', 'character varying', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'Description', 'character varying', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'Filter', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'IdSourceClass', 'regclass', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'SourceFunction', 'text', NULL, FALSE, FALSE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_View', 'Type', 'character varying', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- Filter
---------------------------------------------

SELECT cm_create_class('_Filter', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: Filter|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Code', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'IdOwner', 'int', null, false, false, 'MODE: write|DESCR: IdOwner|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Filter', 'text', null, false, false, 'MODE: write|DESCR: Filter|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'IdSourceClass', 'regclass', null, true, false, 'MODE: write|DESCR: Class Reference|INDEX: 5|STATUS: active');
SELECT cm_create_class_attribute('_Filter', 'Template', 'boolean', 'false', true, false, 'MODE: write|DESCR: User or group filter|INDEX: 6|STATUS: active');

ALTER TABLE "_Filter" ADD CONSTRAINT filter_name_table_unique UNIQUE ("Code", "IdOwner", "IdSourceClass");

---------------------------------------------
-- MdrScopedId
---------------------------------------------
SELECT cm_create_class('_MdrScopedId', NULL, 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass');
SELECT cm_create_class_attribute('_MdrScopedId', 'MdrScopedId', 'text', NULL, TRUE, TRUE, 'MODE: write|STATUS: active');
SELECT cm_create_class_attribute('_MdrScopedId', 'IdItem', 'int4', NULL, TRUE, FALSE, 'MODE: write|STATUS: active');

---------------------------------------------
-- BIM
---------------------------------------------

SELECT cm_create_class('_BIMProject', NULL, 'MODE: reserved|TYPE: simpleclass|DESCR: BIM Project|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('_BIMProject', 'Name', 'varchar', null, true, false, 'MODE: write|DESCR: Name|INDEX: 1|STATUS: active');
SELECT cm_create_class_attribute('_BIMProject', 'Description', 'varchar', null, false, false, 'MODE: write|DESCR: Description|INDEX: 2|STATUS: active');
SELECT cm_create_class_attribute('_BIMProject', 'ProjectId', 'varchar', null, true, true, 'MODE: write|DESCR: Project ID|INDEX: 3|STATUS: active');
SELECT cm_create_class_attribute('_BIMProject', 'Active', 'boolean', 'TRUE', true, false, 'MODE: write|DESCR: Active|INDEX: 4|STATUS: active');
SELECT cm_create_class_attribute('_BIMProject', 'LastCheckin', 'timestamp', null, false, false, 'MODE: write|DESCR: Last Checkin|INDEX: 5|STATUS: active');
