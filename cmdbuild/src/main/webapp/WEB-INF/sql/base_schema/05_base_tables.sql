CREATE SEQUENCE class_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
COMMENT ON SEQUENCE class_seq IS 'Sequence for autoincrement class';

---------------------------------------------
-- Class
---------------------------------------------

SELECT cm_create_class('Class', NULL, 'MODE: baseclass|TYPE: class|DESCR: Class|SUPERCLASS: true|STATUS: active');

CREATE INDEX idx_idclass_id
  ON "Class"
  USING btree
  ("IdClass", "Id");

DROP INDEX idx_class_idclass;

---------------------------------------------
-- Map
---------------------------------------------

CREATE TABLE "Map"
(
  "IdDomain" regclass NOT NULL,
  "IdClass1" regclass NOT NULL,
  "IdObj1" integer NOT NULL,
  "IdClass2" regclass NOT NULL,
  "IdObj2" integer NOT NULL,
  "Status" character(1),
  "User" varchar(40),
  "BeginDate" timestamp without time zone NOT NULL DEFAULT now(),
  "EndDate" timestamp without time zone,
  "Id" integer NOT NULL DEFAULT _cm_new_card_id(),
  CONSTRAINT "Map_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2")
);

COMMENT ON TABLE "Map" IS 'MODE: reserved|TYPE: domain|DESCRDIR: è in relazione con|DESCRINV: è in relazione con|STATUS: active';
COMMENT ON COLUMN "Map"."IdDomain" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdClass1" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdObj1" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdClass2" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."IdObj2" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."EndDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Map"."Id" IS 'MODE: reserved';

CREATE INDEX idx_map_iddomain
  ON "Map"
  USING btree
  ("IdDomain");

CREATE INDEX idx_map_idobj1
  ON "Map"
  USING btree
  ("IdObj1");

CREATE INDEX idx_map_idobj2
  ON "Map"
  USING btree
  ("IdObj2");

---------------------------------------------
-- Lookup
---------------------------------------------

CREATE TABLE "LookUp"
(
  "Type" varchar(64),
  "ParentType" varchar(64),
  "ParentId" integer,
  "Number" integer NOT NULL,
  "IsDefault" boolean NOT NULL,
  CONSTRAINT "LookUp_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");

COMMENT ON TABLE "LookUp" IS 'MODE: reserved|TYPE: class|DESCR: Lookup list|SUPERCLASS: false|STATUS: active';
COMMENT ON COLUMN "LookUp"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Code" IS 'MODE: read|DESCR: Code|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "LookUp"."Description" IS 'MODE: read|DESCR: Description|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "LookUp"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Notes" IS 'MODE: read|DESCR: Annotazioni';
COMMENT ON COLUMN "LookUp"."Type" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."ParentType" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."ParentId" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."Number" IS 'MODE: reserved';
COMMENT ON COLUMN "LookUp"."IsDefault" IS 'MODE: reserved';

CREATE TABLE "Activity"
(
  "FlowStatus" integer, -- MODE: read|DESCR: Stato attività|INDEX: 2|LOOKUP: FlowStatus|REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: false|STATUS: active
  "Priority" integer, -- MODE: reserved|INDEX: -1
  "ActivityDefinitionId" varchar(200), -- MODE: reserved
  "ProcessCode" varchar(200), -- MODE: reserved
  "IsQuickAccept" boolean NOT NULL DEFAULT false, -- MODE: reserved
  "ActivityDescription" text, -- MODE: write|DESCR: Descrizione Attività|INDEX: 4|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: |STATUS: active
  "NextExecutor" varchar(200), -- MODE: reserved
  CONSTRAINT "Activity_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");
COMMENT ON TABLE "Activity" IS 'MODE: baseclass|TYPE: class|DESCR: Attività|SUPERCLASS: true|MANAGER: activity|STATUS: active';
COMMENT ON COLUMN "Activity"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."IdClass" IS 'MODE: reserved|DESCR: Classe';
COMMENT ON COLUMN "Activity"."Code" IS 'MODE: read|DESCR: Nome Attività|INDEX: 0||LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Description" IS 'MODE: read|DESCR: Descrizione|INDEX: 1|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: true|DATEEXPIRE: false|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "Activity"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."Notes" IS 'MODE: read|DESCR: Annotazioni';
COMMENT ON COLUMN "Activity"."FlowStatus" IS 'MODE: read|DESCR: Stato attività|INDEX: 2|LOOKUP: FlowStatus|REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: false|STATUS: active';
COMMENT ON COLUMN "Activity"."Priority" IS 'MODE: reserved|INDEX: -1';
COMMENT ON COLUMN "Activity"."ActivityDefinitionId" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."ProcessCode" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."IsQuickAccept" IS 'MODE: reserved';
COMMENT ON COLUMN "Activity"."ActivityDescription" IS 'MODE: write|DESCR: Descrizione Attività|INDEX: 4|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: true|COLOR: #FFFFFF|FONTCOLOR: #000000|LINEAFTER: false|CLASSORDER: |STATUS: active';
COMMENT ON COLUMN "Activity"."NextExecutor" IS 'MODE: reserved';


CREATE INDEX idx_activity_code
  ON "Activity"
  USING btree
  ("Code");

CREATE INDEX idx_activity_description
  ON "Activity"
  USING btree
  ("Description");

CREATE INDEX idx_activity_idclass
  ON "Activity"
  USING btree
  ("IdClass");


