-- Create IdClass column in tables representing simple classes

CREATE OR REPLACE FUNCTION add_id_class_attribute_to_simple_classes() RETURNS VOID AS $$
DECLARE
	id regclass;
BEGIN
	FOR id IN
		SELECT table_id as id
			FROM _cm_class_list() AS table_id
			WHERE _cm_check_comment(_cm_comment_for_table_id(table_id), 'TYPE', 'simpleclass')
	LOOP
		RAISE INFO 'creating IdClass attribute for class %', id;
		PERFORM cm_create_attribute(id, 'IdClass', 'regclass', NULL, FALSE, FALSE, 'MODE: reserved');

		RAISE INFO 'setting IdClass attribute value for class %', id;
		EXECUTE 'UPDATE ' || id::regclass || ' SET "IdClass" = ' || id::oid;

		PERFORM _cm_attribute_set_notnull(id, 'IdClass', TRUE);
	END LOOP;
END
$$ LANGUAGE PLPGSQL;

SELECT add_id_class_attribute_to_simple_classes();

DROP FUNCTION add_id_class_attribute_to_simple_classes();

CREATE OR REPLACE FUNCTION _cm_trigger_sanity_check_simple() RETURNS trigger AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='DELETE') THEN
		-- RETURN NEW would return NULL forbidding the operation
		RETURN OLD;
	ELSE
		NEW."BeginDate" = now();
		NEW."IdClass" = TG_RELID;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION cm_create_class(
		CMClass text,
		ParentId oid,
		ClassComment text)
	RETURNS integer AS $$
DECLARE
	IsSimpleClass boolean := _cm_is_simpleclass_comment(ClassComment);
	TableId oid;
BEGIN
	IF (IsSimpleClass AND ParentId IS NOT NULL) OR (NOT _cm_is_any_class_comment(ClassComment))
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	-- TODO: Check if the superclass is a superclass

	PERFORM _cm_create_schema_if_needed(CMClass);

	DECLARE
		DBClassName text := _cm_table_dbname_unsafe(CMClass);
		InheritancePart text;
		AttributesPart text;
	BEGIN
		IF ParentId IS NULL THEN
			AttributesPart := '
				"Id" integer NOT NULL DEFAULT _cm_new_card_id(),
			';
			InheritancePart := '';
		ELSE
			AttributesPart := '';
			InheritancePart := ' INHERITS ('|| ParentId::regclass ||')';
		END IF;
		EXECUTE 'CREATE TABLE '|| DBClassName ||
			'('|| AttributesPart ||
				' CONSTRAINT '|| quote_ident(_cm_classpk_name(CMClass)) ||' PRIMARY KEY ("Id")'||
			')' || InheritancePart;
		EXECUTE 'COMMENT ON TABLE '|| DBClassName ||' IS '|| quote_literal(ClassComment);
		EXECUTE 'COMMENT ON COLUMN '|| DBClassName ||'."Id" IS '|| quote_literal('MODE: reserved');
		TableId := _cm_table_id(CMClass);
	END;

	PERFORM _cm_copy_superclass_attribute_comments(TableId, ParentId);

	PERFORM _cm_create_class_triggers(TableId);

	IF ParentId IS NULL THEN
		PERFORM cm_create_attribute(TableId, 'IdClass', 'regclass', NULL, TRUE, FALSE, 'MODE: reserved');
		IF NOT IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'Code', 'varchar(100)', NULL, FALSE, FALSE, 'MODE: write|DESCR: Code|INDEX: 1|BASEDSP: true');
			PERFORM cm_create_attribute(TableId, 'Description', 'varchar(250)', NULL, FALSE, FALSE, 'MODE: write|DESCR: Description|INDEX: 2|BASEDSP: true');
			-- Status is the only attribute needed
			PERFORM cm_create_attribute(TableId, 'Status', 'character(1)', NULL, FALSE, FALSE, 'MODE: reserved');
		END IF;
		PERFORM cm_create_attribute(TableId, 'User', 'varchar(40)', NULL, FALSE, FALSE, 'MODE: reserved');
		IF IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'BeginDate', 'timestamp', 'now()', TRUE, FALSE, 'MODE: write|FIELDMODE: read|BASEDSP: true');
		ELSE
			PERFORM cm_create_attribute(TableId, 'BeginDate', 'timestamp', 'now()', TRUE, FALSE, 'MODE: reserved');
			PERFORM cm_create_attribute(TableId, 'Notes', 'text', NULL, FALSE, FALSE, 'MODE: read|DESCR: Notes|INDEX: 3');
		END IF;
	ELSE
	    PERFORM _cm_propagate_superclass_triggers(TableId);
	END IF;

	IF IsSimpleClass THEN
		PERFORM _cm_create_index(TableId, 'BeginDate');
	ELSE
		PERFORM _cm_create_class_indexes(TableId);
		IF NOT _cm_is_superclass_comment(ClassComment) THEN
			PERFORM _cm_create_class_history(CMClass);
		END IF;
	END IF;

	RETURN TableId::integer;
END;
$$ LANGUAGE PLPGSQL;

