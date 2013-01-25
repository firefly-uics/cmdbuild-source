---------------------------------------------
-- User
---------------------------------------------

CREATE TABLE "User"
(
  "Username" varchar(40) NOT NULL,
  "Password" varchar(40),
  "Email" varchar(320),
  CONSTRAINT "User_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");
COMMENT ON TABLE "User" IS 'MODE: reserved|TYPE: class|DESCR: Users|SUPERCLASS: false|MANAGER: class|STATUS: active';
COMMENT ON COLUMN "User"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "User"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "User"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "User"."Notes" IS 'MODE: read|DESCR: Notes';
COMMENT ON COLUMN "User"."Username" IS 'MODE: read|DESCR: Username|INDEX: 1|BASEDSP: true|STATUS: active';
COMMENT ON COLUMN "User"."Password" IS 'MODE: read|DESCR: Password|INDEX: 2|BASEDSP: false|STATUS: active';
COMMENT ON COLUMN "User"."Email" IS 'MODE: read|DESCR: Email|INDEX: 5';

---------------------------------------------
-- Role
---------------------------------------------

CREATE TABLE "Role"
(
  "Administrator" boolean,
  "startingClass" regclass,
  "Email" varchar(320),
  "DisabledModules" varchar[],
  "DisabledCardTabs" character varying[],
  "DisabledProcessTabs" character varying[],
  "HideSidePanel" boolean DEFAULT false NOT NULL,
  "FullScreenMode" boolean DEFAULT false NOT NULL,
  "SimpleHistoryModeForCard" boolean DEFAULT false NOT NULL,
  "SimpleHistoryModeForProcess" boolean DEFAULT false NOT NULL,
  "ProcessWidgetAlwaysEnabled" boolean DEFAULT false NOT NULL,
  "CloudAdmin" boolean DEFAULT FALSE NOT NULL,
  CONSTRAINT "Role_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");
COMMENT ON TABLE "Role" IS 'MODE: reserved|TYPE: class|DESCR: Groups|SUPERCLASS: false|MANAGER: class|STATUS: active';
COMMENT ON COLUMN "Role"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Role"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Role"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."Notes" IS 'MODE: read|DESCR: Notes';
COMMENT ON COLUMN "Role"."Administrator" IS 'MODE: read|DESCR: Administrator|INDEX: 1|STATUS: active';
COMMENT ON COLUMN "Role"."startingClass" IS 'MODE: read|DESCR: Starting Class|INDEX: 2|STATUS: active';
COMMENT ON COLUMN "Role"."Email" IS 'MODE: read|DESCR: Email|INDEX: 5';
COMMENT ON COLUMN "Role"."DisabledModules" IS 'MODE: read';
COMMENT ON COLUMN "Role"."DisabledCardTabs" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."DisabledProcessTabs" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."HideSidePanel" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."FullScreenMode"IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."SimpleHistoryModeForCard" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."SimpleHistoryModeForProcess" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."ProcessWidgetAlwaysEnabled" IS 'MODE: reserved';
COMMENT ON COLUMN "Role"."CloudAdmin" IS 'MODE: reserved';

ALTER TABLE "Role" ALTER COLUMN "Code" SET NOT NULL;

ALTER TABLE ONLY "Role" ADD CONSTRAINT unique_role_code UNIQUE ("Code");

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

CREATE TABLE "Grant"
(
  "IdRole" integer NOT NULL,
  "IdGrantedClass" regclass,
  "Mode" varchar(1) NOT NULL,
  CONSTRAINT "Grant_pkey" PRIMARY KEY ("Id")
)
INHERITS ("Class");
COMMENT ON TABLE "Grant" IS 'MODE: reserved|TYPE: class|DESCR: Privileges|STATUS: active';
COMMENT ON COLUMN "Grant"."Id" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."IdClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Grant"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';
COMMENT ON COLUMN "Grant"."Status" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."User" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."BeginDate" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';
COMMENT ON COLUMN "Grant"."IdRole" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."IdGrantedClass" IS 'MODE: reserved';
COMMENT ON COLUMN "Grant"."Mode" IS 'MODE: reserved';
