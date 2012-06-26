--
-- Report
--

SELECT cm_create_class('Menu', 'Class', 'MODE: reserved|TYPE: class|DESCR: Menu|SUPERCLASS: false|STATUS: active');
SELECT cm_create_class_attribute('Menu', 'IdParent', 'integer', '0', false, false, 'MODE: reserved|DESCR: Parent Item, 0 means no parent');
SELECT cm_create_class_attribute('Menu', 'IdElementClass', 'regclass', null, false, false, 'MODE: reserved|DESCR: Class connect to this item');
SELECT cm_create_class_attribute('Menu', 'IdElementObj', 'integer', '0', true, false, 'MODE: reserved|DESCR: Object connected to this item, 0 means no object');
SELECT cm_create_class_attribute('Menu', 'Number', 'integer', '0', true, false, 'MODE: reserved|DESCR: Ordering');
SELECT cm_create_class_attribute('Menu', 'IdGroup', 'integer', '0', true, false, 'MODE: reserved|DESCR: Group owner of this item, 0 means default group');
SELECT cm_create_class_attribute('Menu', 'Type', 'varchar (70)', '', true, false, 'MODE: reserved|DESCR: Group owner of this item, 0 means default group');

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
  "Groups" integer[],
  "ImagesName" varchar[],
  CONSTRAINT "Report_pkey" PRIMARY KEY ("Id")
);
COMMENT ON TABLE "Report" IS 'MODE: reserved|TYPE: class|DESCR: Report|SUPERCLASS: false|STATUS: active';
COMMENT ON COLUMN "Report"."Id" IS 'MODE: reserved';
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
COMMENT ON COLUMN "Report"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."Groups" IS 'MODE: reserved';
COMMENT ON COLUMN "Report"."ImagesName" IS 'MODE: reserved';

CREATE UNIQUE INDEX "Report_unique_code"
  ON "Report"
  USING btree
  ((
CASE
    WHEN "Code"::text = ''::text OR "Status"::text <> 'A'::text THEN NULL::text
    ELSE "Code"::text
END));

-- Create Metadata class

SELECT cm_create_class('Metadata', 'Class', 'MODE: reserved|TYPE: class|DESCR: Metadata|SUPERCLASS: false|STATUS: active');

COMMENT ON COLUMN "Metadata"."Code" IS 'MODE: read|DESCR: Schema|INDEX: 1';
COMMENT ON COLUMN "Metadata"."Description" IS 'MODE: read|DESCR: Key|INDEX: 2';
COMMENT ON COLUMN "Metadata"."Notes" IS 'MODE: read|DESCR: Value|INDEX: 3';

-- Create Scheduler class
SELECT cm_create_class('Scheduler', 'Class', 'MODE: reserved|TYPE: class|DESCR: Scheduler|SUPERCLASS: false|STATUS: active');

COMMENT ON COLUMN "Scheduler"."Code" IS 'MODE: read|DESCR: Job Type|INDEX: 1';
COMMENT ON COLUMN "Scheduler"."Description" IS 'MODE: read|DESCR: Job Description|INDEX: 2';
COMMENT ON COLUMN "Scheduler"."Notes" IS 'MODE: read|DESCR: Job Parameters|INDEX: 3';

SELECT cm_create_class_attribute('Scheduler', 'CronExpression', 'text', '', true, false, 'MODE: read|DESCR: Cron Expression|STATUS: active');
SELECT cm_create_class_attribute('Scheduler', 'Detail', 'text', '', true, false, 'MODE: read|DESCR: Job Detail|STATUS: active');
