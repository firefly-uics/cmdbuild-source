-- Change process system attributes MODE

CREATE OR REPLACE FUNCTION patch_210_15() RETURNS void AS $$
DECLARE
	query text;
	currentClass regclass;
BEGIN
FOR currentClass IN SELECT _cm_subtables_and_itself('"Activity"'::regclass) LOOP
	-- disable trigger
	query = 'alter table ' || currentClass::regclass || ' disable trigger user;';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."FlowStatus" IS ''MODE: system|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."ActivityDefinitionId" IS ''MODE: system|DESCR: Activity Definition Ids (for speed)''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."ProcessCode" IS ''MODE: system|DESCR: Process Instance Id''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."NextExecutor" IS ''MODE: system|DESCR: Activity Instance performers''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."ActivityInstanceId" IS ''MODE: system|DESCR: Activity Instance Ids''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."PrevExecutors" IS ''MODE: system|DESCR: Process Instance performers up to now''';
	raise notice '%', query;
	execute query;

	query = 'COMMENT ON COLUMN ' || currentClass::regclass || '."UniqueProcessDefinition" IS ''MODE: system|DESCR: Unique Process Definition (for speed)''';
	raise notice '%', query;
	execute query;

	-- enable trigger
	query = 'alter table ' || currentClass::regclass || ' enable trigger user;';
	raise notice '%', query;
	execute query;
END LOOP;
END;
$$ LANGUAGE PLPGSQL;

select patch_210_15();

DROP FUNCTION patch_210_15();
