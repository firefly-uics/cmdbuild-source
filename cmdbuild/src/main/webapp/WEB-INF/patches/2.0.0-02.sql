-- Alter workflow tables

CREATE OR REPLACE FUNCTION _cm_disable_triggers_recursively(SuperClass regclass) RETURNS VOID AS $$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::regclass ||' DISABLE TRIGGER USER';
	END LOOP;
END;
$$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION _cm_enable_triggers_recursively(SuperClass regclass) RETURNS VOID AS $$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::text ||' ENABLE TRIGGER USER';
	END LOOP;
END;
$$
LANGUAGE PLPGSQL;


CREATE OR REPLACE FUNCTION _patch_prevexecutors(Class regclass, Id integer) RETURNS varchar[] AS $$
DECLARE
	ClassName text := _cm_cmtable(Class);
	ClassHistory regclass := _cm_history_dbname(ClassName);
	RetVal varchar[];
BEGIN
	EXECUTE 'SELECT ARRAY(SELECT DISTINCT Q."NextExecutor" FROM (
				SELECT "NextExecutor" FROM '|| Class ||' WHERE "Id"=$1
				UNION
				SELECT "NextExecutor" FROM '|| ClassHistory ||' WHERE "CurrentId"=$1
			) AS Q WHERE Q."NextExecutor" IS NOT NULL)' INTO RetVal USING Id;
	RETURN RetVal;
END;
$$
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION patch_200_02() RETURNS VOID AS $$
DECLARE
	Activity regclass := '"Activity"'::regclass;
BEGIN
	RAISE INFO 'Altering Activity table';

	PERFORM _cm_disable_triggers_recursively(Activity);

	RAISE INFO '... basic changes';

	ALTER TABLE "Activity" DROP COLUMN "Priority";
	ALTER TABLE "Activity" DROP COLUMN "IsQuickAccept";
	ALTER TABLE "Activity" DROP COLUMN "ActivityDescription";

	ALTER TABLE "Activity" ALTER COLUMN "ProcessCode" TYPE text;

	ALTER TABLE "Activity" RENAME COLUMN "ActivityDefinitionId" TO "ActivityInstanceId";
	ALTER TABLE "Activity" ALTER COLUMN "ActivityInstanceId" TYPE varchar[] USING ARRAY[]::varchar[];

	RAISE INFO '... ActivityDefinitionName';

	ALTER TABLE "Activity" ADD COLUMN "ActivityDefinitionName" varchar[];
	UPDATE "Activity" SET "ActivityDefinitionName" = CASE
			WHEN "Code" IS NULL THEN ARRAY[]::varchar[]
			ELSE ARRAY["Code"::varchar]
		END;

	UPDATE "Activity" SET "Code" = NULL;

	RAISE INFO '... PrevExecutors';

	ALTER TABLE "Activity" ADD COLUMN "PrevExecutors" varchar[];
	UPDATE "Activity" SET "PrevExecutors" = CASE
			WHEN "Status" = 'A' THEN _patch_prevexecutors("Activity"."IdClass", "Activity"."Id")
			ELSE ARRAY[]::varchar[]
		END;

	RAISE INFO '... NextExecutor';

	ALTER TABLE "Activity" ALTER COLUMN "NextExecutor" TYPE varchar[] USING CASE
			WHEN "NextExecutor" IS NULL THEN ARRAY[]::varchar[]
			ELSE ARRAY["NextExecutor"::varchar]
		END;

	PERFORM _cm_enable_triggers_recursively(Activity);

	PERFORM _cm_set_attribute_comment(Activity, 'FlowStatus', 'MODE: read|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus|STATUS: active');
	PERFORM _cm_set_attribute_comment(Activity, 'ProcessCode', 'MODE: reserved|DESCR: Process Instance Id');
	PERFORM _cm_set_attribute_comment(Activity, 'ActivityInstanceId', 'MODE: reserved|DESCR: Activity Instance Ids');
	PERFORM _cm_set_attribute_comment(Activity, 'ActivityDefinitionName', 'MODE: reserved|DESCR: Activity Definition name (for the grid)|INDEX: 4|STATUS: active');
	PERFORM _cm_set_attribute_comment(Activity, 'NextExecutor', 'MODE: reserved|DESCR: Activity Instance performers');
	PERFORM _cm_set_attribute_comment(Activity, 'PrevExecutors', 'MODE: reserved|DESCR: Process Instance performers up to now');

END
$$ LANGUAGE PLPGSQL;

SELECT patch_200_02();

DROP FUNCTION _patch_prevexecutors(Class regclass, Id integer);
DROP FUNCTION patch_200_02();
