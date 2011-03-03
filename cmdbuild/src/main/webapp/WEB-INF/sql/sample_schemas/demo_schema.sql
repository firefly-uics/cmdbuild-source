SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

CREATE FUNCTION _cm_add_class_cascade_delete_on_relations_trigger(tableid oid) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CascadeDeleteOnRelations"
			AFTER UPDATE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_class_history_trigger(tableid oid) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CreateHistoryRow"
			AFTER DELETE OR UPDATE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_create_card_history_row()
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_class_sanity_check_trigger(tableid oid) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check();
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_domain_history_trigger(domainid oid) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_CreateHistoryRow"
			AFTER DELETE OR UPDATE
			ON '|| DomainId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_create_relation_history_row()
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_domain_sanity_check_trigger(domainid oid) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| DomainId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check();
	';
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_fk_constraints(fksourceid oid, attributename text) RETURNS void
    AS $$
DECLARE
	FKTargetId oid := _cm_get_fk_target_table_id(FKSourceId, AttributeName);
	SubTableId oid;
BEGIN
	IF FKTargetId IS NULL THEN
		RETURN;
	END IF;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKSourceId) LOOP
		PERFORM _cm_add_fk_trigger(SubTableId, FKSourceId, AttributeName, FKTargetId);
	END LOOP;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKTargetId) LOOP
		PERFORM _cm_add_restrict_trigger(SubTableId, FKSourceId, AttributeName);
	END LOOP;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_fk_trigger(tableid oid, fksourceid oid, fkattribute text, fktargetid oid) RETURNS void
    AS $$
DECLARE
	TriggerVariant text;
BEGIN
	IF _cm_is_simpleclass(FKSourceId) THEN
		TriggerVariant := 'simple';
	ELSE
		TriggerVariant := '';
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_classfk_name(FKSourceId, FKAttribute)) || '
			BEFORE INSERT OR UPDATE
			ON ' || TableId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_fk('||
				quote_literal(FKAttribute) || ',' ||
				quote_literal(FKTargetId::regclass) || ',' ||
				quote_literal(TriggerVariant) ||
			');
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_reference_handling(tableid oid, attributename text) RETURNS void
    AS $$
DECLARE
	objid integer;
	referencedid integer;
	ctrlint integer;

	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
	ReferenceTargetId oid := _cm_read_reference_target_id_comment(AttributeComment);
	AttributeReferenceType text := _cm_read_reference_type_comment(AttributeComment);
	ReferenceDomainId oid := _cm_read_reference_domain_id_comment(AttributeComment);

	RefSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(TableId, AttributeName);
	RefTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, AttributeName);
	RefTargetClassIdAttribute text := _cm_get_ref_target_class_domain_attribute(TableId, AttributeName);

	ChildId oid;
BEGIN
	IF ReferenceTargetId IS NULL OR AttributeReferenceType IS NULL OR ReferenceDomainId IS NULL THEN
		RETURN;
	END IF;

	-- Updates the reference for every relation
	-- TODO: UNDERSTAND WHAT IT DOES AND MAKE IT READABLE!
	FOR objid IN EXECUTE 'SELECT "Id" from '||TableId::regclass||' WHERE "Status"=''A'''
	LOOP
		FOR referencedid IN EXECUTE '
			SELECT '|| quote_ident(RefSourceIdAttribute) ||
			' FROM '|| ReferenceDomainId::regclass ||
			' WHERE '|| quote_ident(RefTargetClassIdAttribute) ||'='|| TableId ||
				' AND '|| quote_ident(RefTargetIdAttribute) ||'='|| objid ||
				' AND "Status"=''A'''
		LOOP
			EXECUTE 'SELECT count(*) FROM '||ReferenceTargetId::regclass||' where "Id"='||referencedid INTO ctrlint;
			IF(ctrlint<>0) THEN
				EXECUTE 'UPDATE '|| TableId::regclass ||
					' SET '|| quote_ident(AttributeName) ||'='|| referencedid ||
					' WHERE "Id"='|| objid;
			END IF;
		END LOOP;
	END LOOP;

	-- Trigger on reference class (reference -> relation)
	FOR ChildId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		PERFORM _cm_add_update_relation_trigger(ChildId, TableId, AttributeName);
	END LOOP;

	-- Trigger on domain (relation -> reference)
	PERFORM _cm_add_update_reference_trigger(TableId, AttributeName);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_restrict_trigger(fktargetclassid oid, fkclassid oid, fkattribute text) RETURNS void
    AS $$
BEGIN
	IF FKClassId IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident('_Constr_'||_cm_cmtable(FKClassId)||'_'||FKAttribute) || '
			BEFORE UPDATE OR DELETE
			ON ' || FKTargetClassId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_restrict(' ||
					quote_literal(FKClassId::regclass) || ',' ||
					quote_literal(FKAttribute) ||
				');
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_simpleclass_sanity_check_trigger(tableid oid) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		CREATE TRIGGER "_SanityCheck"
			BEFORE INSERT OR UPDATE OR DELETE
			ON '|| TableId::regclass ||'
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_spherical_mercator() RETURNS void
    AS $$
DECLARE
	FoundSrid integer;
BEGIN
	SELECT "srid" INTO FoundSrid FROM "spatial_ref_sys" WHERE "srid" = 900913 LIMIT 1;
	IF NOT FOUND THEN
		INSERT INTO "spatial_ref_sys" ("srid","auth_name","auth_srid","srtext","proj4text") VALUES (900913,'spatialreferencing.org',900913,'','+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +units=m +k=1.0 +nadgrids=@null +no_defs');
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_update_reference_trigger(tableid oid, refattribute text) RETURNS void
    AS $$
DECLARE
	DomainId oid := _cm_get_reference_domain_id(TableId, RefAttribute);
	DomainSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(TableId, RefAttribute);
	DomainTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, RefAttribute);
BEGIN
	IF DomainId IS NULL OR DomainSourceIdAttribute IS NULL OR DomainTargetIdAttribute IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_update_reference_trigger_name(TableId, RefAttribute)) || '
			AFTER INSERT OR UPDATE
			ON ' || DomainId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_update_reference(' ||
					quote_literal(RefAttribute) || ',' ||
					quote_literal(TableId::regclass) || ',' ||
					quote_literal(DomainSourceIdAttribute) || ',' ||
					quote_literal(DomainTargetIdAttribute) ||
				');
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_add_update_relation_trigger(tableid oid, reftableid oid, refattribute text) RETURNS void
    AS $$
DECLARE
	DomainId oid := _cm_get_reference_domain_id(RefTableId, RefAttribute);
	DomainSourceIdAttribute text := _cm_get_ref_source_id_domain_attribute(RefTableId, RefAttribute);
	DomainTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(RefTableId, RefAttribute);
BEGIN
	IF DomainId IS NULL OR DomainSourceIdAttribute IS NULL OR DomainTargetIdAttribute IS NULL THEN
		RETURN;
	END IF;

	EXECUTE '
		CREATE TRIGGER ' || quote_ident(_cm_update_relation_trigger_name(RefTableId, RefAttribute)) || '
			AFTER INSERT OR UPDATE
			ON ' || TableId::regclass || '
			FOR EACH ROW
			EXECUTE PROCEDURE _cm_trigger_update_relation(' ||
				quote_literal(RefAttribute) || ',' ||
				quote_literal(DomainId::regclass) || ',' ||
				quote_literal(DomainSourceIdAttribute) || ',' ||
				quote_literal(DomainTargetIdAttribute) ||
			');
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_default_to_src(tableid oid, attributename text, newdefault text) RETURNS text
    AS $$
DECLARE
	SQLType text := _cm_get_attribute_sqltype(TableId, AttributeName);
BEGIN
	IF (NewDefault IS NULL OR TRIM(NewDefault) = '') THEN
		RETURN NULL;
	END IF;

    IF SQLType ILIKE 'varchar%' OR SQLType = 'text' OR
    	((SQLType = 'date' OR SQLType = 'timestamp') AND TRIM(NewDefault) <> 'now()')
    THEN
		RETURN quote_literal(NewDefault);
	ELSE
		RETURN NewDefault;
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_is_empty(tableid oid, attributename text) RETURNS boolean
    AS $$
DECLARE
	Out boolean;
BEGIN
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||
		' WHERE '|| quote_ident(AttributeName) ||' IS NOT NULL' || 
	    ' AND '|| quote_ident(AttributeName) ||'::text <> '''' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_is_inherited(tableid oid, attributename text) RETURNS boolean
    AS $_$
	SELECT pg_attribute.attinhcount <> 0
	FROM pg_attribute
	WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_attribute_is_local(tableid oid, attributename text) RETURNS boolean
    AS $_$
	SELECT (attinhcount = 0) FROM pg_attribute WHERE attrelid = $1 AND attname = $2 LIMIT 1;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_attribute_is_notnull(tableid oid, attributename text) RETURNS boolean
    AS $_$
SELECT pg_attribute.attnotnull OR c.oid IS NOT NULL
FROM pg_attribute
LEFT JOIN pg_constraint AS c
	ON c.conrelid = pg_attribute.attrelid
	AND c.conname::text = _cm_notnull_constraint_name(pg_attribute.attname::text)
WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_attribute_is_unique(tableid oid, attributename text) RETURNS boolean
    AS $$
DECLARE
	IsUnique boolean;
BEGIN
	SELECT INTO IsUnique (count(*) > 0) FROM pg_class
		JOIN pg_index ON pg_class.oid = pg_index.indexrelid
		WHERE pg_index.indrelid = TableId AND relname = _cm_unique_index_name(TableId, AttributeName);
	RETURN IsUnique;
END;
$$
    LANGUAGE plpgsql STABLE;



CREATE FUNCTION _cm_attribute_list(tableid oid) RETURNS SETOF text
    AS $_$
	SELECT attname::text FROM pg_attribute WHERE attrelid = $1 AND attnum > 0 AND atttypid > 0 ORDER BY attnum;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_attribute_list_cs(classid oid) RETURNS text
    AS $_$
	SELECT array_to_string(array(
		SELECT quote_ident(name) FROM _cm_attribute_list($1) AS name
	),',');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_attribute_notnull_is_check(tableid oid, attributename text) RETURNS boolean
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN NOT (
		_cm_is_simpleclass(TableId)
		OR _cm_check_comment(_cm_comment_for_table_id(TableId), 'MODE', 'reserved')
		OR _cm_check_comment(_cm_comment_for_attribute(TableId, AttributeName), 'MODE', 'reserved')
	);
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_root_table_id(tableid oid, attributename text) RETURNS oid
    AS $$
DECLARE
	CurrentTableId oid := TableId;
BEGIN
	LOOP
	    EXIT WHEN CurrentTableId IS NULL OR _cm_attribute_is_local(CurrentTableId, AttributeName);
		CurrentTableId := _cm_parent_id(CurrentTableId);
	END LOOP;
	RETURN CurrentTableId;
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_set_notnull(tableid oid, attributename text, willbenotnull boolean) RETURNS void
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	IF WillBeNotNull = _cm_attribute_is_notnull(TableId, AttributeName) THEN
		RETURN;
	END IF;

    IF WillBeNotNull AND _cm_is_superclass(TableId) AND _cm_check_comment(AttributeComment, 'MODE', 'write')
    THEN
    	RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: %', 'Non-system superclass attributes cannot be not null';
    END IF;

	PERFORM _cm_attribute_set_notnull_unsafe(TableId, AttributeName, WillBeNotNull);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_set_notnull_unsafe(tableid oid, attributename text, willbenotnull boolean) RETURNS void
    AS $$
DECLARE
    IsCheck boolean := _cm_attribute_notnull_is_check(TableId, AttributeName);
BEGIN
	IF (WillBeNotNull) THEN
		IF (IsCheck) THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||
				' ADD CONSTRAINT ' || quote_ident(_cm_notnull_constraint_name(AttributeName)) ||
				' CHECK ("Status"<>''A'' OR ' || quote_ident(AttributeName) || ' IS NOT NULL)';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' SET NOT NULL';
		END IF;
	ELSE
		IF (IsCheck) THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP CONSTRAINT '||
				quote_ident(_cm_notnull_constraint_name(AttributeName));
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' DROP NOT NULL';
		END IF;
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_set_uniqueness(tableid oid, attributename text, attributeunique boolean) RETURNS void
    AS $$
BEGIN
	IF _cm_attribute_is_unique(TableId, AttributeName) <> AttributeUnique THEN
		IF AttributeUnique AND (_cm_is_simpleclass(TableId) OR _cm_is_superclass(TableId)) THEN
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: %', 'Superclass or simple class attributes cannot be unique';
		END IF;

		PERFORM _cm_attribute_set_uniqueness_unsafe(TableId, AttributeName, AttributeUnique);
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_attribute_set_uniqueness_unsafe(tableid oid, attributename text, attributeunique boolean) RETURNS void
    AS $$
BEGIN
	IF AttributeUnique THEN
		EXECUTE 'CREATE UNIQUE INDEX '||
			quote_ident(_cm_unique_index_name(TableId, AttributeName)) ||
			' ON '|| TableId::regclass ||' USING btree (('||
			' CASE WHEN "Status"::text = ''N''::text THEN NULL'||
			' ELSE '|| quote_ident(AttributeName) || ' END))';
	ELSE
		EXECUTE 'DROP INDEX '|| _cm_unique_index_id(TableId, AttributeName)::regclass;
	END IF;
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_cascade(id integer, tableid oid, attributename text) RETURNS void
    AS $$
BEGIN
	EXECUTE 'DELETE FROM '|| TableId::regclass ||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_check_attribute_comment_and_type(attributecomment text, sqltype text) RETURNS void
    AS $$
DECLARE
	SpecialTypeCount integer := 0; 
BEGIN
	IF _cm_read_reference_domain_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF _cm_get_fk_target_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF _cm_get_lookup_type_comment(AttributeComment) IS NOT NULL THEN
		SpecialTypeCount := SpecialTypeCount +1;
	END IF;

	IF (SpecialTypeCount > 1) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: Too many CMDBuild types specified';
	END IF;

	IF SpecialTypeCount = 1 AND SQLType NOT IN ('int4','integer') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: The SQL type does not match the CMDBuild type';
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_check_comment(classcomment text, key text, value text) RETURNS boolean
    AS $_$
	SELECT (_cm_read_comment($1, $2) ILIKE $3);
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_check_id_exists(id integer, tableid oid, deletedalso boolean) RETURNS boolean
    AS $_$
	SELECT _cm_check_value_exists($1, $2, 'Id', $3);
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_check_value_exists(id integer, tableid oid, attributename text, deletedalso boolean) RETURNS boolean
    AS $$
DECLARE
	Out BOOLEAN := TRUE;
	StatusPart TEXT;
BEGIN
	IF _cm_is_simpleclass(TableId) OR DeletedAlso THEN
		StatusPart := '';
	ELSE
		StatusPart := ' AND "Status"=''A''';
	END IF;
	IF Id IS NOT NULL THEN
		EXECUTE 'SELECT (COUNT(*) > 0) FROM '|| TableId::regclass ||' WHERE '||
		quote_ident(AttributeName)||'='||Id||StatusPart||' LIMIT 1' INTO Out;
	END IF;
	RETURN Out;
END
$$
    LANGUAGE plpgsql STABLE;



CREATE FUNCTION _cm_class_has_children(tableid oid) RETURNS boolean
    AS $_$
	SELECT (COUNT(*) > 0) FROM pg_inherits WHERE inhparent = $1 AND _cm_is_cmobject(inhrelid) LIMIT 1;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_class_has_domains(tableid oid) RETURNS boolean
    AS $_$
	SELECT (COUNT(*) > 0) FROM _cm_domain_list() AS d
	WHERE _cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS1')) = $1 OR
		_cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS2')) = $1;
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_class_list() RETURNS SETOF oid
    AS $$
	SELECT oid FROM pg_class WHERE _cm_is_any_class_comment(_cm_comment_for_cmobject(oid));
$$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_classfk_name(cmclassname text, attributename text) RETURNS text
    AS $_$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_classfk_name(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_classidx_name(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT 'idx_' || REPLACE(_cm_cmtable_lc($1), '_', '') || '_' || lower($2);
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_classpk_name(cmclassname text) RETURNS text
    AS $_$
	SELECT _cm_cmtable($1) || '_pkey';
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_cmschema(cmname text) RETURNS text
    AS $_$
	SELECT (_cm_split_cmname($1))[1];
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_cmschema(tableid oid) RETURNS text
    AS $_$
	SELECT pg_namespace.nspname::text FROM pg_class
	JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid
	WHERE pg_class.oid=$1
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_cmtable(cmname text) RETURNS text
    AS $_$
	SELECT (_cm_split_cmname($1))[2];
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_cmtable(tableid oid) RETURNS text
    AS $_$
	SELECT pg_class.relname::text FROM pg_class	WHERE pg_class.oid=$1
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_cmtable_lc(cmname text) RETURNS text
    AS $_$
	SELECT lower(_cm_cmtable($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_cmtable_lc(tableid oid) RETURNS text
    AS $_$
	SELECT lower(_cm_cmtable($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_comment_for_attribute(tableid oid, attributename text) RETURNS text
    AS $_$
SELECT description
FROM pg_description
JOIN pg_attribute ON pg_description.objoid = pg_attribute.attrelid AND pg_description.objsubid = pg_attribute.attnum
WHERE attrelid = $1 and attname = $2 LIMIT 1;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_comment_for_class(cmclass text) RETURNS text
    AS $_$
	SELECT _cm_comment_for_table_id(_cm_table_id($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_comment_for_cmobject(tableid oid) RETURNS text
    AS $_$
	SELECT description FROM pg_description
	WHERE objoid = $1 AND objsubid = 0 AND _cm_read_comment(description, 'TYPE') IS NOT NULL LIMIT 1;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_comment_for_domain(cmdomain text) RETURNS text
    AS $_$
	SELECT _cm_comment_for_table_id(_cm_domain_id($1));
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_comment_for_table_id(tableid oid) RETURNS text
    AS $_$
	SELECT description FROM pg_description WHERE objoid = $1;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_copy_fk_trigger(fromid oid, toid oid) RETURNS void
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '%_fkey');
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_copy_restrict_trigger(fromid oid, toid oid) RETURNS void
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '_Constr_%');
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_copy_superclass_attribute_comments(tableid oid, parenttableid oid) RETURNS void
    AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT * FROM _cm_attribute_list(ParentTableId)
	LOOP
		EXECUTE 'COMMENT ON COLUMN '|| TableId::regclass || '.' || quote_ident(AttributeName) ||
			' IS '|| quote_literal(_cm_comment_for_attribute(ParentTableId, AttributeName));
	END LOOP;
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_copy_trigger(fromid oid, toid oid, triggernamematcher text) RETURNS void
    AS $$
DECLARE
	TriggerData record;
BEGIN
	FOR TriggerData IN
		SELECT
			t.tgname AS TriggerName,
			t.tgtype AS TriggerType,
			p.proname AS TriggerFunction,
			array_to_string(array(
				SELECT quote_literal(q.param)
					FROM (SELECT regexp_split_to_table(encode(tgargs, 'escape'), E'\\\\000') AS param) AS q
					WHERE q.param <> ''
			),',') AS TriggerParams
		FROM pg_trigger t, pg_proc p
		WHERE tgrelid = FromId AND tgname LIKE TriggerNameMatcher AND t.tgfoid = p.oid
	LOOP
		EXECUTE '
			CREATE TRIGGER '|| quote_ident(TriggerData.TriggerName) ||'
				'|| _cm_trigger_when(TriggerData.TriggerType) ||'
				ON '|| ToId::regclass ||'
				FOR EACH '|| _cm_trigger_row_or_statement(TriggerData.TriggerType) ||'
				EXECUTE PROCEDURE '|| quote_ident(TriggerData.TriggerFunction) ||'('|| TriggerData.TriggerParams ||')
		';
	END LOOP;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_copy_update_relation_trigger(fromid oid, toid oid) RETURNS void
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '_UpdRel_%');
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_create_class_history(cmclassname text) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		CREATE TABLE '|| _cm_history_dbname_unsafe(CMClassName) ||'
		(
			"CurrentId" int4 NOT NULL,
			"EndDate" timestamp NOT NULL DEFAULT now(),
			CONSTRAINT ' || quote_ident(_cm_historypk_name(CMClassName)) ||' PRIMARY KEY ("Id"),
			CONSTRAINT '|| quote_ident(_cm_historyfk_name(CMClassName, 'CurrentId')) ||' FOREIGN KEY ("CurrentId")
				REFERENCES '||_cm_table_dbname(CMClassName)||' ("Id") ON UPDATE RESTRICT ON DELETE SET NULL
		) INHERITS ('||_cm_table_dbname(CMClassName)||');
	';
	PERFORM _cm_create_index(_cm_history_id(CMClassName), 'CurrentId');
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_create_class_indexes(tableid oid) RETURNS void
    AS $$
BEGIN
	PERFORM _cm_create_index(TableId, 'Code');
	PERFORM _cm_create_index(TableId, 'Description');
	PERFORM _cm_create_index(TableId, 'IdClass');
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_create_class_triggers(tableid oid) RETURNS void
    AS $$
BEGIN
	IF _cm_is_superclass(TableId) THEN
		RAISE DEBUG 'Not creating triggers for class %', TableId::regclass;
	ELSIF _cm_is_simpleclass(TableId) THEN
		PERFORM _cm_add_simpleclass_sanity_check_trigger(TableId);
	ELSE
		PERFORM _cm_add_class_sanity_check_trigger(TableId);
		PERFORM _cm_add_class_history_trigger(TableId);
		PERFORM _cm_add_class_cascade_delete_on_relations_trigger(TableId);
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_create_domain_indexes(domainid oid) RETURNS void
    AS $$
DECLARE
    Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	PERFORM _cm_create_index(DomainId, 'IdDomain');
	PERFORM _cm_create_index(DomainId, 'IdObj1');
	PERFORM _cm_create_index(DomainId, 'IdObj2');

	EXECUTE 'CREATE INDEX ' || quote_ident(_cm_domainidx_name(DomainId, 'ActiveRows')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ('||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdDomain" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj1" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdClass2" END),'||
			'(CASE WHEN "Status" = ''N'' THEN NULL ELSE "IdObj2" END)'||
		')';

	IF substring(Cardinality, 3, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueLeft')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass1" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj1" ELSE NULL END)'||
		' )';
	END IF;

	IF substring(Cardinality, 1, 1) = '1' THEN
		EXECUTE
		'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId,'UniqueRight')) ||
		' ON ' || DomainId::regclass ||
		' USING btree ( '||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdClass2" ELSE NULL END),'||
			'(CASE WHEN "Status"::text = ''A'' THEN "IdObj2" ELSE NULL END)'||
		' )';
	END IF;
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_create_domain_triggers(domainid oid) RETURNS void
    AS $$
BEGIN
	PERFORM _cm_add_domain_sanity_check_trigger(DomainId);
	PERFORM _cm_add_domain_history_trigger(DomainId);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_create_index(tableid oid, attributename text) RETURNS void
    AS $$
BEGIN
	EXECUTE 'CREATE INDEX ' || quote_ident(_cm_classidx_name(TableId, AttributeName)) ||
		' ON ' || TableId::regclass ||
		' USING btree (' || quote_ident(AttributeName) || ')';
EXCEPTION
	WHEN undefined_column THEN
		RAISE LOG 'Index for attribute %.% not created because the attribute does not exist',
			TableId::regclass, quote_ident(AttributeName);
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_create_schema_if_needed(cmname text) RETURNS void
    AS $$
BEGIN
	IF _cm_cmschema(CMName) IS NOT NULL THEN
		EXECUTE 'CREATE SCHEMA '||quote_ident(_cm_cmschema(CMName));
	END IF;
EXCEPTION
	WHEN duplicate_schema THEN
		RETURN;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_delete_local_attributes_or_triggers(tableid oid) RETURNS void
    AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT _cm_attribute_list(TableId) LOOP
		IF _cm_attribute_is_inherited(TableId, AttributeName) THEN
			PERFORM _cm_remove_attribute_triggers(TableId, AttributeName);
		ELSE
			PERFORM cm_delete_attribute(TableId, AttributeName);
		END IF;
	END LOOP;
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_delete_relation(username text, domainid oid, cardidcolumn text, cardid integer) RETURNS void
    AS $$
DECLARE
BEGIN
	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET "Status" = ''N'', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
		' WHERE "Status" = ''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_dest_classid_for_domain_attribute(domainid oid, attributename text) RETURNS oid
    AS $_$
	SELECT _cm_table_id(
		_cm_read_comment(
			_cm_comment_for_table_id($1),
			CASE $2
			WHEN 'IdObj1' THEN
				'CLASS1'
			WHEN 'IdObj2' THEN
				'CLASS2'
			ELSE
				NULL
			END
		)
	);
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_dest_reference_classid(domainid oid, refidcolumn text, refid integer) RETURNS oid
    AS $_$
	SELECT _cm_subclassid(_cm_dest_classid_for_domain_attribute($1, $2), $3)
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_domain_cardinality(domainid oid) RETURNS text
    AS $_$
	SELECT _cm_read_domain_cardinality(_cm_comment_for_table_id($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_domain_cmname(cmdomain text) RETURNS text
    AS $_$
	SELECT coalesce(_cm_cmschema($1)||'.','')||coalesce('Map_'||_cm_cmtable($1),'Map');
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_domain_cmname_lc(cmdomainname text) RETURNS text
    AS $_$
	SELECT lower(_cm_domain_cmname($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_domain_dbname(cmdomain text) RETURNS regclass
    AS $_$
	SELECT _cm_table_dbname(_cm_domain_cmname($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_domain_dbname_unsafe(cmdomain text) RETURNS text
    AS $_$
	SELECT _cm_table_dbname_unsafe(_cm_domain_cmname($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_domain_direction(domainid oid) RETURNS boolean
    AS $$
DECLARE
	Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	IF Cardinality = 'N:1' THEN
		RETURN TRUE;
	ELSIF Cardinality = '1:N' THEN
		RETURN FALSE;
	ELSE
		RETURN NULL;
	END IF;
END
$$
    LANGUAGE plpgsql STABLE STRICT;



CREATE FUNCTION _cm_domain_id(cmdomain text) RETURNS oid
    AS $_$
	SELECT _cm_table_id(_cm_domain_cmname($1));
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_domain_list() RETURNS SETOF oid
    AS $$
	SELECT oid FROM pg_class WHERE _cm_is_domain_comment(_cm_comment_for_cmobject(oid));
$$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_domainidx_name(domainid oid, type text) RETURNS text
    AS $_$
	SELECT 'idx_' || _cm_cmtable_lc($1) || '_' || lower($2);
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_domainpk_name(cmdomainname text) RETURNS text
    AS $_$
	SELECT _cm_classpk_name(_cm_domain_cmname($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_drop_triggers_recursively(tableid oid, triggername text) RETURNS void
    AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'DROP TRIGGER IF EXISTS '|| quote_ident(TriggerName) ||' ON '|| SubClassId::regclass;
	END LOOP;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_get_attribute_default(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT pg_attrdef.adsrc
		FROM pg_attribute JOIN pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_get_attribute_sqltype(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT pg_type.typname::text || CASE
				WHEN pg_type.typname IN ('varchar','bpchar') THEN '(' || pg_attribute.atttypmod - 4 || ')'
				WHEN pg_type.typname = 'numeric' THEN '(' ||
					pg_attribute.atttypmod / 65536 || ',' ||
					pg_attribute.atttypmod - pg_attribute.atttypmod / 65536 * 65536 - 4|| ')'
				ELSE ''
			END
		FROM pg_attribute JOIN pg_type ON pg_type.oid = pg_attribute.atttypid
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_get_domain_reference_target_comment(domaincomment text) RETURNS text
    AS $_$
	SELECT CASE _cm_read_domain_cardinality($1)
		WHEN '1:N' THEN _cm_read_comment($1, 'CLASS1')
		WHEN 'N:1' THEN _cm_read_comment($1, 'CLASS2')
		ELSE NULL
	END
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_get_fk_target(tableid oid, attributename text) RETURNS text
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN COALESCE(
		_cm_get_fk_target_comment(AttributeComment),
		_cm_read_reference_target_comment(AttributeComment)
	);
END
$$
    LANGUAGE plpgsql STABLE STRICT;



CREATE FUNCTION _cm_get_fk_target_comment(attributecomment text) RETURNS text
    AS $_$
	SELECT _cm_read_comment($1, 'FKTARGETCLASS');
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_get_fk_target_table_id(tableid oid, attributename text) RETURNS oid
    AS $_$ BEGIN
	RETURN _cm_table_id(_cm_get_fk_target($1, $2));
END $_$
    LANGUAGE plpgsql STABLE STRICT;



CREATE FUNCTION _cm_get_geometry_type(tableid oid, attribute text) RETURNS text
    AS $_$
DECLARE
	GeoType text;
BEGIN
	SELECT geometry_columns.type INTO GeoType
	FROM pg_attribute
	LEFT JOIN geometry_columns
		ON f_table_schema = _cm_cmschema($1)
		AND f_table_name = _cm_cmtable($1)
		AND f_geometry_column = $2
	WHERE attrelid = $1 AND attname = $2 AND attnum > 0 AND atttypid > 0;
	RETURN GeoType;
EXCEPTION WHEN undefined_table THEN
	RETURN NULL;
END
$_$
    LANGUAGE plpgsql STABLE;



CREATE FUNCTION _cm_get_lookup_type_comment(attributecomment text) RETURNS text
    AS $_$
	SELECT _cm_read_comment($1, 'LOOKUP');
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_get_ref_source_id_domain_attribute(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj1'
		WHEN FALSE THEN 'IdObj2'
		ELSE NULL
	END;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_get_ref_target_class_domain_attribute(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdClass2'
		WHEN FALSE THEN 'IdClass1'
		ELSE NULL
	END;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_get_ref_target_id_domain_attribute(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj2'
		WHEN FALSE THEN 'IdObj1'
		ELSE NULL
	END;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_get_reference_domain_id(tableid oid, attributename text) RETURNS oid
    AS $_$
	SELECT _cm_read_reference_domain_id_comment(_cm_comment_for_attribute($1, $2));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_get_type_comment(classcomment text) RETURNS text
    AS $_$
	SELECT _cm_read_comment($1, 'TYPE');
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_history_cmname(cmclass text) RETURNS text
    AS $_$
	SELECT $1 || '_history';
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_history_dbname(cmtable text) RETURNS regclass
    AS $_$
	SELECT _cm_table_dbname(_cm_history_cmname($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_history_dbname_unsafe(cmtable text) RETURNS text
    AS $_$
	SELECT _cm_table_dbname_unsafe(_cm_history_cmname($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_history_id(cmtable text) RETURNS oid
    AS $_$
	SELECT _cm_table_id(_cm_history_cmname($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_historyfk_name(cmclassname text, attributename text) RETURNS text
    AS $_$
	SELECT _cm_classfk_name(_cm_history_cmname($1), $2);
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_historypk_name(cmclassname text) RETURNS text
    AS $_$
	SELECT _cm_classpk_name(_cm_history_cmname($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_insert_relation(username text, domainid oid, cardidcolumn text, cardid integer, refidcolumn text, refid integer, cardclassid oid) RETURNS void
    AS $$
DECLARE
	CardClassIdColumnPart text;
	RefClassIdColumnPart text;
	CardClassIdValuePart text;
	RefClassIdValuePart text;
	StopRecursion boolean;
BEGIN
	IF (CardId IS NULL OR RefId IS NULL) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- Needed for backward compatibility
	CardClassIdColumnPart := coalesce(quote_ident('IdClass'||substring(CardIdColumn from '^IdObj(.)+')) || ', ', '');
	RefClassIdColumnPart := coalesce(quote_ident('IdClass'||substring(RefIdColumn from '^IdObj(.)+')) || ', ', '');
	CardClassIdValuePart := CASE WHEN CardClassIdColumnPart IS NOT NULL THEN (coalesce(CardClassId::text, 'NULL') || ', ') ELSE '' END;
	RefClassIdValuePart := coalesce(_cm_dest_reference_classid(DomainId, RefIdColumn, RefId)::text, 'NULL') || ', ';

	-- Stop trigger recursion
	EXECUTE 'SELECT (COUNT(*) > 0) FROM ' || DomainId::regclass ||
		' WHERE' ||
			' "IdDomain" = ' || DomainId::text || -- NOTE: why is this check done?
			' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId::text ||
			' AND ' || quote_ident(RefIdColumn) || ' = ' || RefId::text ||
			' AND "Status" = ''A''' INTO StopRecursion;
	IF NOT StopRecursion THEN
		EXECUTE 'INSERT INTO ' || DomainId::regclass ||
			' (' ||
				'"IdDomain", ' ||
				quote_ident(CardIdColumn) || ', ' ||
				quote_ident(RefIdColumn) || ', ' ||
				CardClassIdColumnPart ||
				RefClassIdColumnPart ||
				'"Status", ' ||
				'"User"' ||
			') VALUES (' ||
				DomainId::text || ', ' ||
				CardId::text || ', ' ||
				RefId::text || ', ' ||
				CardClassIdValuePart ||
				RefClassIdValuePart ||
				'''A'', ' ||
				coalesce(quote_literal(UserName), 'NULL') ||
			')';
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_is_active_comment(classcomment text) RETURNS boolean
    AS $_$
	SELECT _cm_check_comment($1, 'STATUS', 'active');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_any_class(classid oid) RETURNS boolean
    AS $_$
	SELECT _cm_is_any_class_comment(_cm_comment_for_table_id($1))
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_any_class_comment(classcomment text) RETURNS boolean
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', '%class');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_cmobject(tableid oid) RETURNS boolean
    AS $_$
	SELECT _cm_comment_for_cmobject($1) IS NOT NULL;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_domain_comment(classcomment text) RETURNS boolean
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', 'domain');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_geometry_type(cmattributetype text) RETURNS boolean
    AS $_$
	SELECT $1 IN ('POINT','LINESTRING','POLYGON');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_reference_comment(attributecomment text) RETURNS boolean
    AS $_$
	SELECT COALESCE(_cm_read_reference_domain_comment($1),'') != '';
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_is_simpleclass(cmclass text) RETURNS boolean
    AS $_$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_class($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_simpleclass(classid oid) RETURNS boolean
    AS $_$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_table_id($1))
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_simpleclass_comment(classcomment text) RETURNS boolean
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', 'simpleclass');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_superclass(cmclass text) RETURNS boolean
    AS $_$
	SELECT _cm_is_superclass_comment(_cm_comment_for_class($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_superclass(classid oid) RETURNS boolean
    AS $_$
	SELECT _cm_is_superclass_comment(_cm_comment_for_table_id($1));
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_is_superclass_comment(classcomment text) RETURNS boolean
    AS $_$
	SELECT _cm_check_comment($1, 'SUPERCLASS', 'true');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_join_cmname(cmschema name, cmtable name) RETURNS text
    AS $_$
	SELECT $1 || '.' || $2;
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_legacy_class_is_process(text) RETURNS boolean
    AS $_$
	SELECT (_cm_legacy_read_comment($1, 'MANAGER') = 'activity');
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_legacy_get_menu_code(boolean, boolean, boolean, boolean) RETURNS character varying
    AS $_$
    DECLARE 
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menucode varchar;
    BEGIN
	IF (issuperclass) THEN IF (isprocess) THEN menucode='superclassprocess'; ELSE menucode='superclass'; END IF;
	ELSIF(isview) THEN menucode='view';
	ELSIF(isreport) THEN menucode='report';
	ELSIF (isprocess) THEN menucode='processclass'; ELSE menucode='class';
	END IF;

	RETURN menucode;
    END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_legacy_get_menu_type(boolean, boolean, boolean, boolean) RETURNS character varying
    AS $_$
    DECLARE 
        issuperclass ALIAS FOR $1;
        isprocess ALIAS FOR $2;
        isreport ALIAS FOR $3;
        isview ALIAS FOR $4;
	menutype varchar;
    BEGIN
	IF (isprocess) THEN menutype='processclass';
	ELSIF(isview) THEN menutype='view';
	ELSIF(isreport) THEN menutype='report';
	ELSE menutype='class';
	END IF;

	RETURN menutype;
    END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_legacy_read_comment(text, text) RETURNS character varying
    AS $_$
	SELECT COALESCE(_cm_read_comment($1, $2), '');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_new_card_id() RETURNS integer
    AS $$
	SELECT nextval(('class_seq'::text)::regclass)::integer;
$$
    LANGUAGE sql;



CREATE FUNCTION _cm_notnull_constraint_name(attributename text) RETURNS text
    AS $_$
	SELECT '_NotNull_'||$1;
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_parent_id(tableid oid) RETURNS SETOF oid
    AS $_$
	SELECT inhparent FROM pg_inherits WHERE inhrelid = $1 AND _cm_is_cmobject(inhparent);
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_propagate_superclass_triggers(tableid oid) RETURNS void
    AS $$
DECLARE
	ParentId oid := _cm_parent_id(TableId);
BEGIN
	PERFORM _cm_copy_restrict_trigger(ParentId, TableId);
	PERFORM _cm_copy_update_relation_trigger(ParentId, TableId);
	PERFORM _cm_copy_fk_trigger(ParentId, TableId);
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_read_comment(comment text, key text) RETURNS text
    AS $_$
	SELECT SUBSTRING($1 FROM E'(?:^|\\|)'||$2||E':[ ]*([^ \\|]+)');
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_read_domain_cardinality(attributecomment text) RETURNS text
    AS $_$
	SELECT _cm_read_comment($1, 'CARDIN');
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_read_reference_domain_comment(attributecomment text) RETURNS text
    AS $_$
	SELECT _cm_read_comment($1, 'REFERENCEDOM');
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_read_reference_domain_id_comment(attributecomment text) RETURNS oid
    AS $_$
	SELECT _cm_domain_id(_cm_read_reference_domain_comment($1));
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_read_reference_target_comment(attributecomment text) RETURNS text
    AS $_$
	SELECT _cm_get_domain_reference_target_comment(_cm_comment_for_domain(_cm_read_reference_domain_comment($1)));
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_read_reference_target_id_comment(attributecomment text) RETURNS oid
    AS $_$
	SELECT _cm_table_id(_cm_read_reference_target_comment($1));
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_read_reference_type_comment(attributecomment text) RETURNS text
    AS $_$
	SELECT COALESCE(_cm_read_comment($1, 'REFERENCETYPE'),'restrict');
$_$
    LANGUAGE sql STABLE STRICT;



CREATE FUNCTION _cm_remove_attribute_triggers(tableid oid, attributename text) RETURNS void
    AS $$
BEGIN
	PERFORM _cm_remove_fk_constraints(TableId, AttributeName);
	PERFORM _cm_remove_reference_handling(TableId, AttributeName);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_remove_constraint_trigger(fktargetclassid oid, fkclassid oid, fkattribute text) RETURNS void
    AS $$
BEGIN
	EXECUTE '
		DROP TRIGGER ' || quote_ident('_Constr_'||_cm_cmtable(FKClassId)||'_'||FKAttribute) ||
			' ON ' || FKTargetClassId::regclass || ';
	';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_remove_fk_constraints(fksourceid oid, attributename text) RETURNS void
    AS $$
DECLARE
	TargetId oid := _cm_get_fk_target_table_id(FKSourceId, AttributeName);
	SubTableId oid;
BEGIN
	IF TargetId IS NULL THEN
		RETURN;
	END IF;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(FKSourceId) LOOP
		EXECUTE 'DROP TRIGGER '|| quote_ident(_cm_classfk_name(FKSourceId, AttributeName)) ||
			' ON '|| SubTableId::regclass;
	END LOOP;

	FOR SubTableId IN SELECT _cm_subtables_and_itself(TargetId) LOOP
		PERFORM _cm_remove_constraint_trigger(SubTableId, FKSourceId, AttributeName);
	END LOOP;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_remove_reference_handling(tableid oid, attributename text) RETURNS void
    AS $$
BEGIN
	-- remove UpdRel and UpdRef triggers
	PERFORM _cm_drop_triggers_recursively(
		TableId,
		_cm_update_relation_trigger_name(TableId, AttributeName)
	);
	PERFORM _cm_drop_triggers_recursively(
		_cm_get_reference_domain_id(TableId, AttributeName),
		_cm_update_reference_trigger_name(TableId, AttributeName)
	);
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_restrict(id integer, tableid oid, attributename text) RETURNS void
    AS $_$
BEGIN
	IF _cm_check_value_exists($1, $2, $3, FALSE) THEN
		RAISE EXCEPTION 'CM_RESTRICT_VIOLATION';
	END IF;
END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_set_attribute_comment(tableid oid, attributename text, comment text) RETURNS void
    AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'COMMENT ON COLUMN '|| SubClassId::regclass ||'.'|| quote_ident(AttributeName) ||' IS '|| quote_literal(Comment);
	END LOOP;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_set_attribute_default(tableid oid, attributename text, newdefault text, updateexisting boolean) RETURNS void
    AS $$
DECLARE
	CurrentDefaultSrc text := _cm_get_attribute_default(TableId, AttributeName);
	NewDefaultSrc text := _cm_attribute_default_to_src(TableId, AttributeName, NewDefault);
BEGIN
    IF (NewDefaultSrc IS DISTINCT FROM CurrentDefaultSrc) THEN
    	IF (CurrentDefaultSrc IS NULL) THEN
	        EXECUTE 'ALTER TABLE ' || TableId::regclass ||
					' ALTER COLUMN ' || quote_ident(AttributeName) ||
					' SET DEFAULT ' || NewDefaultSrc;
			IF UpdateExisting THEN
	        	EXECUTE 'UPDATE '|| TableId::regclass ||' SET '|| quote_ident(AttributeName) ||' = '|| NewDefaultSrc;
	        END IF;
	    ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN	'|| quote_ident(AttributeName) ||' DROP DEFAULT';
		END IF;
    END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_setnull(id integer, tableid oid, attributename text) RETURNS void
    AS $$
BEGIN
	EXECUTE 'UPDATE '|| TableId::regclass ||
		' SET '||quote_ident(AttributeName)||' = NULL'||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_split_cmname(cmname text) RETURNS text[]
    AS $_$
    SELECT regexp_matches($1,E'(?:([^\\.]+)\\.)?([^\\.]+)?');
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_subclassid(superclassid oid, cardid integer) RETURNS oid
    AS $$
DECLARE
	Out integer;
BEGIN
	EXECUTE 'SELECT tableoid FROM '||SuperClassId::regclass||' WHERE "Id"='||CardId||' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$
    LANGUAGE plpgsql STABLE STRICT;



CREATE FUNCTION _cm_subtables_and_itself(tableid oid) RETURNS SETOF oid
    AS $_$
	SELECT $1 WHERE _cm_is_cmobject($1)
	UNION
	SELECT _cm_subtables_and_itself(inhrelid) FROM pg_inherits WHERE inhparent = $1
$_$
    LANGUAGE sql;



CREATE FUNCTION _cm_table_dbname(cmname text) RETURNS regclass
    AS $_$
	SELECT _cm_table_dbname_unsafe($1)::regclass;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_table_dbname_unsafe(cmname text) RETURNS text
    AS $_$
	SELECT coalesce(quote_ident(_cm_cmschema($1))||'.','')||quote_ident(_cm_cmtable($1));
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_table_id(cmname text) RETURNS oid
    AS $_$
	SELECT _cm_table_dbname_unsafe($1)::regclass::oid;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_table_is_empty(tableid oid) RETURNS boolean
    AS $$
DECLARE
	NotFound boolean;
BEGIN
	-- Note: FOUND variable is not set on EXECUTE, so we can't use it!
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||' LIMIT 1' INTO NotFound;
	RETURN NotFound;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_cascade_delete_on_relations() RETURNS trigger
    AS $$
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (NEW."Status"='N') THEN
		UPDATE "Map" SET "Status"='N'
			WHERE "Status"='A' AND (
				("IdObj1" = OLD."Id" AND "IdClass1" = TG_RELID)
				OR ("IdObj2" = OLD."Id" AND "IdClass2" = TG_RELID)
			);
	END IF;
	RETURN NEW;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_create_card_history_row() RETURNS trigger
    AS $_$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
		OLD."Id" = _cm_new_card_id();
		OLD."Status" = 'U';
		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
			' ('||_cm_attribute_list_cs(TG_RELID)||',"CurrentId","EndDate")' ||
			' VALUES (' ||
			' (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').*, ' ||
			' (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ')."Id", now())';
	ELSIF (TG_OP='DELETE') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	RETURN NEW;
END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_create_relation_history_row() RETURNS trigger
    AS $_$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
		OLD."Id" = _cm_new_card_id();
		OLD."Status" = 'U';
		OLD."EndDate" = now();
		EXECUTE 'INSERT INTO '||_cm_history_dbname(_cm_join_cmname(TG_TABLE_SCHEMA, TG_TABLE_NAME)) ||
			' ('||_cm_attribute_list_cs(TG_RELID)||')' ||
			' VALUES (' ||
			' (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').*)';
	ELSIF (TG_OP='DELETE') THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	RETURN NEW;
END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_fk() RETURNS trigger
    AS $_$
DECLARE
	SourceAttribute text := TG_ARGV[0];
	TargetClassId oid := TG_ARGV[1]::regclass::oid;
	TriggerVariant text := TG_ARGV[2];
	RefValue integer;
	ActiveCardsOnly boolean;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(SourceAttribute) INTO RefValue;

	IF (TriggerVariant = 'simple') THEN
		ActiveCardsOnly := FALSE;
	ELSE
		ActiveCardsOnly := NEW."Status" <> 'A';
	END IF;

	IF NOT _cm_check_id_exists(RefValue, TargetClassId, ActiveCardsOnly) THEN
		RETURN NULL;
	END IF;

	RETURN NEW;
END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_restrict() RETURNS trigger
    AS $$
DECLARE
	TableId oid := TG_ARGV[0]::regclass::oid;
	AttributeName text := TG_ARGV[1];
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (TG_OP='UPDATE' AND NEW."Status"='N') THEN
		PERFORM _cm_restrict(OLD."Id", TableId, AttributeName);
	END IF;
	RETURN NEW;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_row_or_statement(tgtype smallint) RETURNS text
    AS $_$
	SELECT CASE $1 & cast(1 as int2)
         WHEN 0 THEN 'STATEMENT'
         ELSE 'ROW'
       END;
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_trigger_sanity_check() RETURNS trigger
    AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		IF (NEW."Status"='N' AND OLD."Status"='N') THEN -- Deletion of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='INSERT') THEN
		IF (NEW."Status" IS NULL) THEN
			NEW."Status"='A';
		ELSIF (NEW."Status"='N') THEN -- Creation of a deleted card
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
		NEW."Id" = _cm_new_card_id();
	ELSE -- TG_OP='DELETE'
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- 'U' is reserved for history tables only
	IF (position(NEW."Status" IN 'AND') = 0) THEN -- Invalid status
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
	NEW."BeginDate" = now();
	RETURN NEW;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_sanity_check_simple() RETURNS trigger
    AS $$
BEGIN
	IF (TG_OP='UPDATE') THEN
		IF (NEW."Id" <> OLD."Id") THEN -- Id change
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;
	ELSIF (TG_OP='DELETE') THEN
		-- RETURN NEW would return NULL forbidding the operation
		RETURN OLD;
	END IF;
	RETURN NEW;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_update_reference() RETURNS trigger
    AS $_$
DECLARE
	AttributeName text := TG_ARGV[0];
	TableId oid := TG_ARGV[1]::regclass::oid;
	CardColumn text := TG_ARGV[2]; -- Domain column name for the card id
	RefColumn text := TG_ARGV[3];  -- Domain column name for the reference id

	OldCardId integer;
	NewCardId integer;
	OldRefValue integer;
	NewRefValue integer;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (NEW."Status"='A') THEN
		EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(RefColumn) INTO NewRefValue;
	ELSIF (NEW."Status"<>'N') THEN
		-- Ignore history rows
		RETURN NEW;
	END IF;

	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(CardColumn) INTO NewCardId;

	IF (TG_OP='UPDATE') THEN
		EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(CardColumn) INTO OldCardId;
		IF (OldCardId <> NewCardId) THEN -- If the non-reference side changes...
			PERFORM _cm_update_reference(TableId, AttributeName, OldCardId, NULL);
			-- OldRefValue is kept null because it is like a new relation
		ELSE
			EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(RefColumn) INTO OldRefValue;
		END IF;
	END IF;

	IF ((NewRefValue IS NULL) OR (OldRefValue IS NULL) OR (OldRefValue <> NewRefValue)) THEN
		PERFORM _cm_update_reference(TableId, AttributeName, NewCardId, NewRefValue);
	END IF;

	RETURN NEW;
END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_update_relation() RETURNS trigger
    AS $_$
DECLARE
	AttributeName text := TG_ARGV[0];
	DomainId oid := TG_ARGV[1]::regclass::oid;
	CardColumn text := TG_ARGV[2]; -- Domain column name for the card id
	RefColumn text := TG_ARGV[3];  -- Domain column name for the reference id

	OldRefValue integer;
	NewRefValue integer;
BEGIN
	RAISE DEBUG 'Trigger % on %', TG_NAME, TG_TABLE_NAME;
	IF (TG_OP = 'UPDATE') THEN
		EXECUTE 'SELECT (' || quote_literal(OLD) || '::' || TG_RELID::regclass || ').' || quote_ident(AttributeName) INTO OldRefValue;
	END IF;
	EXECUTE 'SELECT (' || quote_literal(NEW) || '::' || TG_RELID::regclass || ').' || quote_ident(AttributeName) INTO NewRefValue;

	IF (NewRefValue IS NOT NULL) THEN
		IF (OldRefValue IS NOT NULL) THEN
			IF (OldRefValue <> NewRefValue) THEN
				PERFORM _cm_update_relation(NEW."User", DomainId, CardColumn, NEW."Id", RefColumn, NewRefValue);
			END IF;
		ELSE
			PERFORM _cm_insert_relation(NEW."User", DomainId, CardColumn, NEW."Id", RefColumn, NewRefValue, TG_RELID);
		END IF;
	ELSE
		IF (OldRefValue IS NOT NULL) THEN
			PERFORM _cm_delete_relation(NEW."User", DomainId, CardColumn, NEW."Id");
		END IF;
	END IF;
	RETURN NEW;
END;
$_$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_trigger_when(tgtype smallint) RETURNS text
    AS $_$
	SELECT CASE $1 & cast(2 as int2)
         WHEN 0 THEN 'AFTER'
         ELSE 'BEFORE'
       END || ' ' ||
       CASE $1 & cast(28 as int2)
         WHEN 16 THEN 'UPDATE'
         WHEN  8 THEN 'DELETE'
         WHEN  4 THEN 'INSERT'
         WHEN 20 THEN 'INSERT OR UPDATE'
         WHEN 28 THEN 'INSERT OR UPDATE OR DELETE'
         WHEN 24 THEN 'UPDATE OR DELETE'
         WHEN 12 THEN 'INSERT OR DELETE'
       END;
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_unique_index_id(tableid oid, attributename text) RETURNS oid
    AS $_$
	SELECT (
		quote_ident(_cm_cmschema($1))
		||'.'||
		quote_ident(_cm_unique_index_name($1, $2))
	)::regclass::oid;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_unique_index_name(tableid oid, attributename text) RETURNS text
    AS $_$
	SELECT '_Unique_'|| _cm_cmtable($1) ||'_'|| $2;
$_$
    LANGUAGE sql STABLE;



CREATE FUNCTION _cm_update_reference(tableid oid, attributename text, cardid integer, referenceid integer) RETURNS void
    AS $$
BEGIN
	EXECUTE 'UPDATE ' || TableId::regclass ||
		' SET ' || quote_ident(AttributeName) || ' = ' || coalesce(ReferenceId::text, 'NULL') ||
		' WHERE "Status"=''A'' AND "Id" = ' || CardId::text ||
		' AND coalesce(' || quote_ident(AttributeName) || ', 0) <> ' || coalesce(ReferenceId, 0)::text;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_update_reference_trigger_name(reftableid oid, refattribute text) RETURNS text
    AS $_$
	SELECT '_UpdRef_'|| _cm_cmtable($1) ||'_'|| $2;
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_update_relation(username text, domainid oid, cardidcolumn text, cardid integer, refidcolumn text, refid integer) RETURNS void
    AS $$
DECLARE
	RefClassUpdatePart text;
BEGIN
	-- Needed to update IdClassX (if the domain attributres are IdClass1/2)
	RefClassUpdatePart := coalesce(
		', ' || quote_ident('IdClass'||substring(RefIdColumn from E'^IdObj(\\d)+')) || 
			'=' || _cm_dest_reference_classid(DomainId, RefIdColumn, RefId),
		''
	);

	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET ' || quote_ident(RefIdColumn) || ' = ' || RefId ||
			', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
			RefClassUpdatePart ||
		' WHERE "Status"=''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION _cm_update_relation_trigger_name(reftableid oid, refattribute text) RETURNS text
    AS $_$
	SELECT '_UpdRel_'|| _cm_cmtable($1) ||'_'|| $2;
$_$
    LANGUAGE sql IMMUTABLE;



CREATE FUNCTION _cm_zero_rownum_sequence() RETURNS void
    AS $$
DECLARE
	temp BIGINT;
BEGIN
	SELECT INTO temp setval('rownum', 0, true);
EXCEPTION WHEN undefined_table THEN
	CREATE TEMPORARY SEQUENCE rownum MINVALUE 0 START 1;
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_create_attribute(tableid oid, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    AS $$
BEGIN
	PERFORM _cm_check_attribute_comment_and_type(AttributeComment, SQLType);

	IF _cm_is_geometry_type(SQLType) THEN
		PERFORM _cm_add_spherical_mercator();
		PERFORM AddGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName, 900913, SQLType, 2);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ADD COLUMN '|| quote_ident(AttributeName) ||' '|| SQLType;
	END IF;

    PERFORM _cm_set_attribute_default(TableId, AttributeName, AttributeDefault, TRUE);

	-- set the comment recursively (needs to be performed before unique and notnull, because they depend on the comment)
    PERFORM _cm_set_attribute_comment(TableId, AttributeName, AttributeComment);

	PERFORM _cm_attribute_set_notnull(TableId, AttributeName, AttributeNotNull);
	PERFORM _cm_attribute_set_uniqueness(TableId, AttributeName, AttributeUnique);

    PERFORM _cm_add_fk_constraints(TableId, AttributeName);
	PERFORM _cm_add_reference_handling(TableId, AttributeName);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_create_class(cmclass text, parentid oid, classcomment text) RETURNS integer
    AS $$
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
		IF NOT IsSimpleClass THEN
			PERFORM cm_create_attribute(TableId, 'IdClass', 'regclass', NULL, TRUE, FALSE, 'MODE: reserved');
			PERFORM cm_create_attribute(TableId, 'Code', 'varchar(100)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true');
			PERFORM cm_create_attribute(TableId, 'Description', 'varchar(250)', NULL, FALSE, FALSE, 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true');
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
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_create_class(cmclass text, cmparentclass text, classcomment text) RETURNS integer
    AS $_$
	SELECT cm_create_class($1, _cm_table_id($2), $3);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_create_class_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    AS $_$
	SELECT cm_create_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_create_domain(cmdomain text, domaincomment text) RETURNS integer
    AS $$
DECLARE
	DomainId oid;
	HistoryDBName text := _cm_history_dbname_unsafe(_cm_domain_cmname(CMDomain));
BEGIN
	-- TODO: Add Creation of Map (from its name)
	EXECUTE 'CREATE TABLE '|| _cm_domain_dbname_unsafe(CMDomain) ||
		' (CONSTRAINT '|| quote_ident(_cm_domainpk_name(CMDomain)) ||
		' PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate"))'||
		' INHERITS ("Map")';

	DomainId := _cm_domain_id(CMDomain);

	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass ||' IS '|| quote_literal(DomainComment);
	EXECUTE 'CREATE TABLE '|| HistoryDBName ||
		' ( CONSTRAINT '|| quote_ident(_cm_historypk_name(_cm_domain_cmname(CMDomain))) ||
		' PRIMARY KEY ("IdDomain","IdClass1", "IdObj1", "IdClass2", "IdObj2","EndDate"))'||
		' INHERITS ('|| DomainId::regclass ||')';
	EXECUTE 'ALTER TABLE '|| HistoryDBName ||' ALTER COLUMN "EndDate" SET DEFAULT now()';

	PERFORM _cm_create_domain_indexes(DomainId);

	PERFORM _cm_create_domain_triggers(DomainId);

	RETURN DomainId;
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_create_domain_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    AS $_$
	SELECT cm_create_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_delete_attribute(tableid oid, attributename text) RETURNS void
    AS $$
DECLARE
	GeoType text := _cm_get_geometry_type(TableId, AttributeName);
BEGIN
	IF NOT _cm_attribute_is_local(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

    IF NOT _cm_attribute_is_empty(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION: %', 'Contains data';
	END IF;

	PERFORM _cm_remove_attribute_triggers(TableId, AttributeName);

	IF GeoType IS NOT NULL THEN
		PERFORM DropGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP COLUMN '|| quote_ident(AttributeName) ||' CASCADE';
	END IF;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_delete_class(tableid oid) RETURNS void
    AS $$
BEGIN
	IF _cm_class_has_domains(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: has domains', TableId::regclass;
	ELSEIF _cm_class_has_children(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: has childs', TableId::regclass;
	ELSEIF NOT _cm_table_is_empty(TableId) THEN
		RAISE EXCEPTION 'Cannot delete class %: contains data', TableId::regclass;
	END IF;

	PERFORM _cm_delete_local_attributes_or_triggers(TableId);

	-- Cascade for the history table
	EXECUTE 'DROP TABLE '|| TableId::regclass ||' CASCADE';
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_delete_class(cmclass text) RETURNS void
    AS $_$
	SELECT cm_delete_class(_cm_table_id($1));
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_delete_class_attribute(cmclass text, attributename text) RETURNS void
    AS $_$
	SELECT cm_delete_attribute(_cm_table_id($1), $2);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_delete_domain(domainid oid) RETURNS void
    AS $$
BEGIN
	IF NOT _cm_table_is_empty(DomainId) THEN
		RAISE EXCEPTION 'Cannot delete domain %, contains data', DomainId::regclass;
	END IF;

	PERFORM _cm_delete_local_attributes_or_triggers(DomainId);

	EXECUTE 'DROP TABLE '|| DomainId::regclass ||' CASCADE';
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_delete_domain(cmdomain text) RETURNS void
    AS $_$
	SELECT cm_delete_domain(_cm_domain_id($1));
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_delete_domain_attribute(cmclass text, attributename text) RETURNS void
    AS $_$
	SELECT cm_delete_attribute(_cm_domain_id($1), $2);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_modify_attribute(tableid oid, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, newcomment text) RETURNS void
    AS $$
DECLARE
	OldComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	IF _cm_read_reference_domain_comment(OldComment) IS DISTINCT FROM _cm_read_reference_domain_comment(NewComment)
		OR  _cm_read_reference_type_comment(OldComment) IS DISTINCT FROM _cm_read_reference_type_comment(NewComment)
		OR  _cm_get_fk_target_comment(OldComment) IS DISTINCT FROM _cm_get_fk_target_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM _cm_check_attribute_comment_and_type(NewComment, SQLType);

	IF _cm_get_attribute_sqltype(TableId, AttributeName) <> trim(SQLType) THEN
		IF _cm_attribute_is_inherited(TableId, AttributeName) THEN
			RAISE NOTICE 'Not altering column type'; -- Fail silently
			--RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ALTER COLUMN '|| quote_ident(AttributeName) ||' TYPE '|| SQLType;
		END IF;
	END IF;

	PERFORM _cm_attribute_set_uniqueness(TableId, AttributeName, AttributeUnique);
	PERFORM _cm_attribute_set_notnull(TableId, AttributeName, AttributeNotNull);
	PERFORM _cm_set_attribute_default(TableId, AttributeName, AttributeDefault, FALSE);
	PERFORM _cm_set_attribute_comment(TableId, AttributeName, NewComment);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_modify_class(tableid oid, newcomment text) RETURNS void
    AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(TableId);
BEGIN
	IF _cm_is_superclass_comment(OldComment) <> _cm_is_superclass_comment(NewComment)
		OR _cm_get_type_comment(OldComment) <> _cm_get_type_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	EXECUTE 'COMMENT ON TABLE ' || TableId::regclass || ' IS ' || quote_literal(NewComment);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_modify_class(cmclass text, newcomment text) RETURNS void
    AS $_$
	SELECT cm_modify_class(_cm_table_id($1), $2);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_modify_class_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    AS $_$
	SELECT cm_modify_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_modify_domain(domainid oid, newcomment text) RETURNS void
    AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	IF _cm_read_domain_cardinality(OldComment) <> _cm_read_domain_cardinality(NewComment)
		OR _cm_read_comment(OldComment, 'CLASS1') <> _cm_read_comment(NewComment, 'CLASS1')
		OR _cm_read_comment(OldComment, 'CLASS2') <> _cm_read_comment(NewComment, 'CLASS2')
		OR _cm_get_type_comment(OldComment) <> _cm_get_type_comment(NewComment)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	-- Check that the cardinality does not change
	EXECUTE 'COMMENT ON TABLE '|| DomainId::regclass || ' IS '|| quote_literal(NewComment);
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION cm_modify_domain(cmdomain text, domaincomment text) RETURNS void
    AS $_$
	SELECT cm_modify_domain(_cm_domain_id($1), $2);
$_$
    LANGUAGE sql;



CREATE FUNCTION cm_modify_domain_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    AS $_$
	SELECT cm_modify_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$_$
    LANGUAGE sql;



CREATE FUNCTION reference_dispositividirete_pdl() RETURNS trigger
    AS $$
			DECLARE
				ReferenceDomain varchar(40);
				direct varchar(10);
				directb bool;
				domcard varchar(10);
				Reference varchar(40);
				ReferenceValue integer;
				OldReferenceValue integer;
				DomainId integer;
				field_list refcursor;
				thisid integer; 
				otherid integer; 
				refdom varchar(50);
				rowinmap integer;

			BEGIN

				OPEN field_list FOR EXECUTE '
					SELECT "classid",case when(system_read_comment("attributecomment", ''REFERENCEDIRECT'' ) ILIKE ''true'') then domainclass2 else domainclass1 end,system_read_comment("attributecomment", 
				  ''REFERENCEDOM'' ),system_read_comment("attributecomment", ''REFERENCEDIRECT'' )  FROM "system_attributecatalog" JOIN system_domaincatalog ON 
			  system_domaincatalog."domainname"=system_read_comment("attributecomment", ''REFERENCEDOM'') WHERE "classname"='''||TG_RELNAME||''' AND "attributename"=''PDL'';';
					FETCH field_list INTO thisid,Reference,ReferenceDomain,direct;
				CLOSE field_list;
				refdom = ''||ReferenceDomain||'';
			
				OPEN field_list FOR EXECUTE '
					SELECT "domainid",system_read_comment("domaincomment", ''CARDIN''::varchar) FROM "system_domaincatalog" WHERE "domainname"='''||refdom||''';';
					FETCH field_list INTO DomainId,domcard;
				CLOSE field_list;


					IF (NEW."PDL" is null) 
					THEN otherid=null;
					ELSE
						OPEN field_list FOR EXECUTE '
			  				SELECT "IdClass"::oid FROM "'||Reference||'" WHERE "Id"='||NEW."PDL"||';';
			  				FETCH field_list INTO otherid;
			  			CLOSE field_list;
					END IF;

					IF(domcard='1:N')
					THEN directb=true;
					ELSE directb=false; END IF;

					IF(TG_OP='INSERT') 
					THEN
						IF(directb) THEN
							PERFORM system_reference_inserted(otherid,NEW."PDL",thisid,NEW."Id",ReferenceDomain,DomainId,directb);
						ELSE
							PERFORM system_reference_inserted(thisid,NEW."Id",otherid,NEW."PDL",ReferenceDomain,DomainId,directb);
						END IF;
					ELSIF(TG_OP='DELETE') 
					THEN
						IF(directb) THEN
							PERFORM system_reference_deleted(otherid,OLD."PDL",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
						ELSE
							PERFORM system_reference_deleted(thisid,OLD."Id",otherid,OLD."PDL",ReferenceDomain,DomainId,directb);
						END IF;
					ELSIF(TG_OP='UPDATE') 
					THEN
						IF(directb) THEN
							IF(NEW."Status"='A' AND ( (NEW."PDL" IS NULL AND OLD."PDL" IS NOT NULL) OR (NEW."PDL" IS NOT NULL AND OLD."PDL" IS NULL) OR (NEW."PDL"<>OLD."PDL") ) ) THEN
								PERFORM system_reference_updated(otherid,NEW."PDL",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
							ELSIF(NEW."Status"='N') THEN
								PERFORM system_reference_logicdelete(otherid,NEW."PDL",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
							END IF;
						ELSE
							IF(NEW."Status"='A' AND ( (NEW."PDL" IS NULL AND OLD."PDL" IS NOT NULL) OR (NEW."PDL" IS NOT NULL AND OLD."PDL" IS NULL) OR (NEW."PDL"<>OLD."PDL") ) ) THEN
								PERFORM system_reference_updated(thisid,OLD."Id",otherid,NEW."PDL",ReferenceDomain,DomainId,directb);
							ELSIF(NEW."Status"='N') THEN
								PERFORM system_reference_logicdelete(thisid,OLD."Id",otherid,NEW."PDL",ReferenceDomain,DomainId,directb);
							END IF;
						END IF;
					END IF;
		  RETURN NEW;
		  END;
		  $$
    LANGUAGE plpgsql;



CREATE FUNCTION reference_dispositividirete_stanza() RETURNS trigger
    AS $$
			DECLARE
				ReferenceDomain varchar(40);
				direct varchar(10);
				directb bool;
				domcard varchar(10);
				Reference varchar(40);
				ReferenceValue integer;
				OldReferenceValue integer;
				DomainId integer;
				field_list refcursor;
				thisid integer; 
				otherid integer; 
				refdom varchar(50);
				rowinmap integer;

			BEGIN

				OPEN field_list FOR EXECUTE '
					SELECT "classid",case when(system_read_comment("attributecomment", ''REFERENCEDIRECT'' ) ILIKE ''true'') then domainclass2 else domainclass1 end,system_read_comment("attributecomment", 
				  ''REFERENCEDOM'' ),system_read_comment("attributecomment", ''REFERENCEDIRECT'' )  FROM "system_attributecatalog" JOIN system_domaincatalog ON 
			  system_domaincatalog."domainname"=system_read_comment("attributecomment", ''REFERENCEDOM'') WHERE "classname"='''||TG_RELNAME||''' AND "attributename"=''Stanza'';';
					FETCH field_list INTO thisid,Reference,ReferenceDomain,direct;
				CLOSE field_list;
				refdom = ''||ReferenceDomain||'';
			
				OPEN field_list FOR EXECUTE '
					SELECT "domainid",system_read_comment("domaincomment", ''CARDIN''::varchar) FROM "system_domaincatalog" WHERE "domainname"='''||refdom||''';';
					FETCH field_list INTO DomainId,domcard;
				CLOSE field_list;


					IF (NEW."Stanza" is null) 
					THEN otherid=null;
					ELSE
						OPEN field_list FOR EXECUTE '
			  				SELECT "IdClass"::oid FROM "'||Reference||'" WHERE "Id"='||NEW."Stanza"||';';
			  				FETCH field_list INTO otherid;
			  			CLOSE field_list;
					END IF;

					IF(domcard='1:N')
					THEN directb=true;
					ELSE directb=false; END IF;

					IF(TG_OP='INSERT') 
					THEN
						IF(directb) THEN
							PERFORM system_reference_inserted(otherid,NEW."Stanza",thisid,NEW."Id",ReferenceDomain,DomainId,directb);
						ELSE
							PERFORM system_reference_inserted(thisid,NEW."Id",otherid,NEW."Stanza",ReferenceDomain,DomainId,directb);
						END IF;
					ELSIF(TG_OP='DELETE') 
					THEN
						IF(directb) THEN
							PERFORM system_reference_deleted(otherid,OLD."Stanza",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
						ELSE
							PERFORM system_reference_deleted(thisid,OLD."Id",otherid,OLD."Stanza",ReferenceDomain,DomainId,directb);
						END IF;
					ELSIF(TG_OP='UPDATE') 
					THEN
						IF(directb) THEN
							IF(NEW."Status"='A' AND ( (NEW."Stanza" IS NULL AND OLD."Stanza" IS NOT NULL) OR (NEW."Stanza" IS NOT NULL AND OLD."Stanza" IS NULL) OR (NEW."Stanza"<>OLD."Stanza") ) ) THEN
								PERFORM system_reference_updated(otherid,NEW."Stanza",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
							ELSIF(NEW."Status"='N') THEN
								PERFORM system_reference_logicdelete(otherid,NEW."Stanza",thisid,OLD."Id",ReferenceDomain,DomainId,directb);
							END IF;
						ELSE
							IF(NEW."Status"='A' AND ( (NEW."Stanza" IS NULL AND OLD."Stanza" IS NOT NULL) OR (NEW."Stanza" IS NOT NULL AND OLD."Stanza" IS NULL) OR (NEW."Stanza"<>OLD."Stanza") ) ) THEN
								PERFORM system_reference_updated(thisid,OLD."Id",otherid,NEW."Stanza",ReferenceDomain,DomainId,directb);
							ELSIF(NEW."Status"='N') THEN
								PERFORM system_reference_logicdelete(thisid,OLD."Id",otherid,NEW."Stanza",ReferenceDomain,DomainId,directb);
							END IF;
						END IF;
					END IF;
		  RETURN NEW;
		  END;
		  $$
    LANGUAGE plpgsql;



CREATE FUNCTION restrict_composizionepdl_dispositividirete() RETURNS trigger
    AS $$
	DECLARE
		ctrl int4;
	BEGIN
	IF( TG_OP='UPDATE' AND NEW."Status"='N' AND OLD."Status"='A') THEN
		SELECT INTO ctrl COUNT(*) FROM "DispositiviDiRete" WHERE "PDL"=NEW."Id" AND "Status"='A';
		IF(ctrl <> 0) THEN
			RAISE EXCEPTION 'PDL instance has relations on domain ComposizionePDL and is restricted';
		END IF;
	END IF;
	RETURN NEW;
	END;
	$$
    LANGUAGE plpgsql;



CREATE FUNCTION restrict_stanzaitem_dispositividirete() RETURNS trigger
    AS $$
	DECLARE
		ctrl int4;
	BEGIN
	IF( TG_OP='UPDATE' AND NEW."Status"='N' AND OLD."Status"='A') THEN
		SELECT INTO ctrl COUNT(*) FROM "DispositiviDiRete" WHERE "Stanza"=NEW."Id" AND "Status"='A';
		IF(ctrl <> 0) THEN
			RAISE EXCEPTION 'Stanza instance has relations on domain StanzaItem and is restricted';
		END IF;
	END IF;
	RETURN NEW;
	END;
	$$
    LANGUAGE plpgsql;



CREATE FUNCTION system_attribute_create(cmclass character varying, attributename character varying, denormalizedsqltype character varying, attributedefault character varying, attributenotnull boolean, attributeunique boolean, attributecomment character varying, attributereference character varying, attributereferencedomain character varying, attributereferencetype character varying, attributereferenceisdirect boolean) RETURNS integer
    AS $$
DECLARE
    AttributeIndex integer;
    SQLType varchar;
BEGIN
	-- redundant parameters sanity check
	IF COALESCE(AttributeReferenceDomain,'') <> COALESCE(_cm_read_reference_domain_comment(AttributeComment),'')
		OR (COALESCE(_cm_read_reference_domain_comment(AttributeComment),'') <> '' AND
			(
			COALESCE(AttributeReferenceIsDirect,FALSE) <> COALESCE(_cm_read_comment(AttributeComment, 'REFERENCEDIRECT')::boolean,FALSE)
			OR COALESCE(AttributeReference,'') <> COALESCE(_cm_read_reference_target_comment(AttributeComment),'')
			OR COALESCE(AttributeReferenceType,'') <> COALESCE(_cm_read_comment(AttributeComment, 'REFERENCETYPE'),'')
			)
		)
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	IF DenormalizedSQLType ILIKE 'bpchar%' THEN
		SQLType := 'bpchar(1)';
	ELSE
		SQLType := DenormalizedSQLType;
	END IF;

	PERFORM cm_create_class_attribute(CMClass, AttributeName, SQLType, AttributeDefault, AttributeNotNull, AttributeUnique, AttributeComment);

    SELECT CASE
	    	WHEN _cm_check_comment(AttributeComment,'MODE','reserved') THEN -1
			ELSE COALESCE(_cm_read_comment(AttributeComment, 'INDEX'),'0')::integer
		END INTO AttributeIndex;
    RETURN AttributeIndex;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION system_attribute_delete(cmclass character varying, attributename character varying) RETURNS boolean
    AS $$
BEGIN
	PERFORM cm_delete_class_attribute(CMClass, AttributeName);
	RETURN TRUE;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION system_attribute_modify(cmclass text, attributename text, attributenewname text, denormalizedsqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS boolean
    AS $$
DECLARE
    SQLType varchar;
BEGIN
	IF (AttributeName <> AttributeNewName) THEN 
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
    END IF;

   	IF DenormalizedSQLType ILIKE 'bpchar%' THEN
		SQLType := 'bpchar(1)';
	ELSE
		SQLType := DenormalizedSQLType;
	END IF;

	PERFORM cm_modify_class_attribute(CMClass, AttributeName, SQLType,
		AttributeDefault, AttributeNotNull, AttributeUnique, AttributeComment);
	RETURN TRUE;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION system_class_create(classname character varying, parentclass character varying, issuperclass boolean, classcomment character varying) RETURNS integer
    AS $$
BEGIN
	-- consistency checks for wrong signatures
	IF IsSuperClass <> _cm_is_superclass_comment(ClassComment) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	RETURN cm_create_class(ClassName, ParentClass, ClassComment);
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION system_class_delete(cmclass character varying) RETURNS void
    AS $_$
	SELECT cm_delete_class($1);
$_$
    LANGUAGE sql;



CREATE FUNCTION system_class_modify(classid integer, newclassname character varying, newissuperclass boolean, newclasscomment character varying) RETURNS boolean
    AS $$
BEGIN
	IF _cm_cmtable(ClassId) <> NewClassName
		OR _cm_is_superclass_comment(NewClassComment) <> NewIsSuperClass
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM cm_modify_class(ClassId::oid, NewClassComment);
	RETURN TRUE;
END;
$$
    LANGUAGE plpgsql;



CREATE FUNCTION system_domain_create(cmdomain text, domainclass1 text, domainclass2 text, domaincomment text) RETURNS integer
    AS $$
DECLARE
	TableName text := _cm_domain_cmname(CMDomain);
	HistoryTableName text := _cm_history_cmname(TableName);
    DomainId oid;
BEGIN
	-- TODO: Check DomainClass1 and DomainClass2

	RETURN cm_create_domain(CMDomain, DomainComment);
END
$$
    LANGUAGE plpgsql;



CREATE FUNCTION system_domain_delete(cmdomain text) RETURNS void
    AS $_$
	SELECT cm_delete_domain($1);
$_$
    LANGUAGE sql;



CREATE FUNCTION system_domain_modify(domainid oid, domainname text, domainclass1 text, domainclass2 text, newcomment text) RETURNS boolean
    AS $$
DECLARE
	OldComment text := _cm_comment_for_table_id(DomainId);
BEGIN
	-- TODO: Check DomainName, DomainClass1 and DomainClass2
	IF _cm_domain_id(DomainName) <> DomainId
		OR _cm_read_comment(NewComment, 'CLASS1') <> DomainClass1
		OR _cm_read_comment(NewComment, 'CLASS2') <> DomainClass2
	THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	PERFORM cm_modify_domain(DomainId, NewComment);

	RETURN TRUE;
END;
$$
    LANGUAGE plpgsql;


SET default_tablespace = '';

SET default_with_oids = false;


CREATE TABLE "Class" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "IdClass" regclass NOT NULL,
    "Code" character varying(100),
    "Description" character varying(250),
    "Status" character(1),
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Notes" text
);



COMMENT ON TABLE "Class" IS 'MODE: baseclass|TYPE: class|DESCR: Class|SUPERCLASS: true|STATUS: active';



COMMENT ON COLUMN "Class"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Class"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Class"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



CREATE TABLE "Activity" (
    "FlowStatus" integer,
    "Priority" integer,
    "ActivityDefinitionId" character varying(200),
    "ProcessCode" character varying(200),
    "IsQuickAccept" boolean DEFAULT false NOT NULL,
    "ActivityDescription" text,
    "NextExecutor" character varying(200)
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



CREATE TABLE "Item" (
    "DataAcquisto" date,
    "DataCollaudo" date,
    "NumeroDiSerie" character varying(50),
    "CostoFinale" numeric(10,2),
    "Stato" integer,
    "Stanza" integer,
    "PDL" integer,
    "Marca" integer,
    "Modello" character varying(50)
)
INHERITS ("Class");



COMMENT ON TABLE "Item" IS 'MODE: read|TYPE: class|DESCR: Item|SUPERCLASS: true|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Item"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Item"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Item"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Item"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Item"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Item"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."DataAcquisto" IS 'MODE: write|FIELDMODE: write|DESCR: Data Acquisto|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."DataCollaudo" IS 'MODE: write|FIELDMODE: write|DESCR: Data Collaudo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."NumeroDiSerie" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di serie|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."CostoFinale" IS 'MODE: write|FIELDMODE: write|DESCR: Costo finale|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."Stato" IS 'MODE: write|FIELDMODE: write|DESCR: Stato|INDEX: 8|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Stato Item|STATUS: active';



COMMENT ON COLUMN "Item"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaItem|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."PDL" IS 'MODE: write|FIELDMODE: write|DESCR: Posto di lavoro|INDEX: 10|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ComposizionePDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Item"."Marca" IS 'MODE: write|FIELDMODE: write|DESCR: Marca|INDEX: 11|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Marca Item|STATUS: active';



COMMENT ON COLUMN "Item"."Modello" IS 'MODE: write|FIELDMODE: write|DESCR: Modello|INDEX: 12|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Computer" (
    "Tipo" integer,
    "Processore" character varying(50),
    "NumeroDiProcessori" integer,
    "VelocitaProcessore" integer,
    "RAM" integer,
    "HardDisk" integer
)
INHERITS ("Item");



COMMENT ON TABLE "Computer" IS 'MODE: write|TYPE: class|DESCR: Computer|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Computer"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."DataAcquisto" IS 'MODE: write|FIELDMODE: write|DESCR: Data Acquisto|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."DataCollaudo" IS 'MODE: write|FIELDMODE: write|DESCR: Data Collaudo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."NumeroDiSerie" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di serie|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."CostoFinale" IS 'MODE: write|FIELDMODE: write|DESCR: Costo finale|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."Stato" IS 'MODE: write|FIELDMODE: write|DESCR: Stato|INDEX: 8|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Stato Item|STATUS: active';



COMMENT ON COLUMN "Computer"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaItem|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."PDL" IS 'MODE: write|FIELDMODE: write|DESCR: Posto di lavoro|INDEX: 10|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ComposizionePDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."Marca" IS 'MODE: write|FIELDMODE: write|DESCR: Marca|INDEX: 11|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Marca Item|STATUS: active';



COMMENT ON COLUMN "Computer"."Modello" IS 'MODE: write|FIELDMODE: write|DESCR: Modello|INDEX: 12|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."Tipo" IS 'MODE: write|FIELDMODE: write|DESCR: Tipo|INDEX: 13|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Tipo Computer|STATUS: active';



COMMENT ON COLUMN "Computer"."Processore" IS 'MODE: write|FIELDMODE: write|DESCR: Processore|INDEX: 14|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."NumeroDiProcessori" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di processori|INDEX: 15|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."VelocitaProcessore" IS 'MODE: write|FIELDMODE: write|DESCR: Velocità processore|INDEX: 16|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."RAM" IS 'MODE: write|FIELDMODE: write|DESCR: RAM|INDEX: 17|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Computer"."HardDisk" IS 'MODE: write|FIELDMODE: write|DESCR: Hard Disk|INDEX: 18|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Computer_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Computer");



CREATE TABLE "Dipendente" (
    "Tipo" integer,
    "Qualifica" integer,
    "Livello" integer,
    "Email" character varying(100),
    "Ufficio" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Dipendente" IS 'MODE: write|TYPE: class|DESCR: Dipendente|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Dipendente"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Dipendente"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Dipendente"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Matricola|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Dipendente"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Nominativo|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Dipendente"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Dipendente"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Dipendente"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Dipendente"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Dipendente"."Tipo" IS 'MODE: write|FIELDMODE: write|DESCR: Tipo|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Tipo Dipendente|STATUS: active';



COMMENT ON COLUMN "Dipendente"."Qualifica" IS 'MODE: write|FIELDMODE: write|DESCR: Qualifica|INDEX: 5|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Qualifica Dipendente|STATUS: active';



COMMENT ON COLUMN "Dipendente"."Livello" IS 'MODE: write|FIELDMODE: write|DESCR: Livello|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Livello Dipendenti|STATUS: active';



COMMENT ON COLUMN "Dipendente"."Email" IS 'MODE: write|FIELDMODE: write|DESCR: Email|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Dipendente"."Ufficio" IS 'MODE: write|FIELDMODE: write|DESCR: Ufficio|INDEX: 8|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: Appartenenti|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Dipendente_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Dipendente");



CREATE TABLE "Email" (
    "Activity" integer,
    "EmailStatus" integer NOT NULL,
    "FromAddress" text,
    "ToAddresses" text,
    "CcAddresses" text,
    "Subject" text,
    "Content" text
)
INHERITS ("Class");



COMMENT ON TABLE "Email" IS 'MODE: reserved|TYPE: class|DESCR: Email|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Email"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Email"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Email"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Email"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Email"."Activity" IS 'MODE: read|FIELDMODE: write|DESCR: Activity|INDEX: 4|REFERENCEDOM: ActivityEmail|REFERENCEDIRECT: false|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Email"."EmailStatus" IS 'MODE: read|FIELDMODE: write|DESCR: EmailStatus|INDEX: 5|BASEDSP: true|LOOKUP: EmailStatus|STATUS: active';



COMMENT ON COLUMN "Email"."FromAddress" IS 'MODE: read|FIELDMODE: write|DESCR: From|INDEX: 6|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Email"."ToAddresses" IS 'MODE: read|FIELDMODE: write|DESCR: TO|INDEX: 7|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Email"."CcAddresses" IS 'MODE: read|FIELDMODE: write|DESCR: CC|INDEX: 8|CLASSORDER: 0|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "Email"."Subject" IS 'MODE: read|FIELDMODE: write|DESCR: Subject|INDEX: 9|BASEDSP: true|STATUS: active';



COMMENT ON COLUMN "Email"."Content" IS 'MODE: read|FIELDMODE: write|DESCR: Body|INDEX: 10|BASEDSP: false|STATUS: active';



CREATE TABLE "Email_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Email");



CREATE TABLE "Grant" (
    "IdRole" integer NOT NULL,
    "IdGrantedClass" regclass,
    "Mode" character varying(1) NOT NULL
)
INHERITS ("Class");



COMMENT ON TABLE "Grant" IS 'MODE: reserved|TYPE: class|DESCR: Grants|STATUS: active';



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



CREATE TABLE "LookUp" (
    "Type" character varying(64),
    "ParentType" character varying(64),
    "ParentId" integer,
    "Number" integer NOT NULL,
    "IsDefault" boolean NOT NULL
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



CREATE TABLE "Map" (
    "IdDomain" regclass NOT NULL,
    "IdClass1" regclass NOT NULL,
    "IdObj1" integer NOT NULL,
    "IdClass2" regclass NOT NULL,
    "IdObj2" integer NOT NULL,
    "Status" character(1),
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "EndDate" timestamp without time zone,
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL
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



CREATE TABLE "Map_ActivityEmail" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_ActivityEmail" IS 'MODE: reserved|TYPE: domain|CLASS1: Activity|CLASS2: Email|DESCRDIR: |DESCRINV: |CARDIN: 1:N|STATUS: active';



CREATE TABLE "Map_ActivityEmail_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_ActivityEmail");



CREATE TABLE "Map_Appartenenti" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_Appartenenti" IS 'MODE: reserved|TYPE: domain|DESCRDIR: comprende|DESCRINV: fa parte di|CARDIN: 1:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Ufficio|CLASS2: Dipendente|LABEL: Appartenenti';



CREATE TABLE "Map_Appartenenti_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_Appartenenti");



CREATE TABLE "Map_Assegnazione" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_Assegnazione" IS 'MODE: reserved|TYPE: domain|DESCRDIR: utilizza|DESCRINV: utilizzato da|CARDIN: N:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Dipendente|CLASS2: PDL|LABEL: Assegnazione';



CREATE TABLE "Map_Assegnazione_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_Assegnazione");



CREATE TABLE "Map_ComposizionePDL" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_ComposizionePDL" IS 'MODE: reserved|TYPE: domain|DESCRDIR: comprende|DESCRINV: fa parte di|CARDIN: 1:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: PDL|CLASS2: Item|LABEL: ComposizionePDL';



CREATE TABLE "Map_ComposizionePDL_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_ComposizionePDL");



CREATE TABLE "Map_ComputerMonitor" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_ComputerMonitor" IS 'MODE: reserved|TYPE: domain|DESCRDIR: possiede Monitor|DESCRINV: collegato a Computer|CARDIN: 1:N|MASTERDETAIL: true|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Computer|CLASS2: Monitor|LABEL: ComputerMonitor';



CREATE TABLE "Map_ComputerMonitor_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_ComputerMonitor");



CREATE TABLE "Map_ItemSoftware" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_ItemSoftware" IS 'MODE: reserved|TYPE: domain|DESCRDIR: ha installato|DESCRINV: installato in|CARDIN: N:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Item|CLASS2: Software|LABEL: SoftwareInstallato';



CREATE TABLE "Map_ItemSoftware_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_ItemSoftware");



CREATE TABLE "Map_PianoPalazzo" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_PianoPalazzo" IS 'MODE: reserved|TYPE: domain|DESCRDIR: si trova in|DESCRINV: contiene|CARDIN: N:1|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Piano|CLASS2: Palazzo|LABEL: Piano Palazzo';



CREATE TABLE "Map_PianoPalazzo_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_PianoPalazzo");



CREATE TABLE "Map_PianoStanza" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_PianoStanza" IS 'MODE: reserved|TYPE: domain|DESCRDIR: contiene|DESCRINV: si trova in|CARDIN: 1:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Piano|CLASS2: Stanza|LABEL: Piano Stanza';



CREATE TABLE "Map_PianoStanza_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_PianoStanza");



CREATE TABLE "Map_Responsabile" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_Responsabile" IS 'MODE: reserved|TYPE: domain|DESCRDIR: responsabile di|DESCRINV: diretto da|CARDIN: 1:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Dipendente|CLASS2: Ufficio|LABEL: Responsabile';



CREATE TABLE "Map_Responsabile_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_Responsabile");



CREATE TABLE "Map_StanzaItem" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_StanzaItem" IS 'MODE: reserved|TYPE: domain|DESCRDIR: contiene|DESCRINV: si trova in|CARDIN: 1:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Stanza|CLASS2: Item|LABEL: Stanza Item';



CREATE TABLE "Map_StanzaItem_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_StanzaItem");



CREATE TABLE "Map_StanzaPDL" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_StanzaPDL" IS 'MODE: reserved|TYPE: domain|DESCRDIR: contiene|DESCRINV: si trova in|CARDIN: 1:N|MASTERDETAIL: false|OPENEDROWS: 0|STATUS: ACTIVE|CLASS1: Stanza|CLASS2: PDL|LABEL: Stanza PDL';



CREATE TABLE "Map_StanzaPDL_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_StanzaPDL");



CREATE TABLE "Map_UserRole" (
    "DefaultGroup" boolean
)
INHERITS ("Map");



COMMENT ON TABLE "Map_UserRole" IS 'MODE: reserved|TYPE: domain|CLASS1: User|CLASS2: Role|DESCRDIR: has role|DESCRINV: contains|CARDIN: N:N|STATUS: active';



COMMENT ON COLUMN "Map_UserRole"."DefaultGroup" IS 'MODE: read|FIELDMODE: write|DESCR: Default Group|INDEX: 1|BASEDSP: true|STATUS: active';



CREATE TABLE "Map_UserRole_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_UserRole");



CREATE TABLE "Menu" (
    "IdParent" integer DEFAULT 0,
    "IdElementClass" regclass,
    "IdElementObj" integer DEFAULT 0 NOT NULL,
    "Number" integer DEFAULT 0 NOT NULL,
    "IdGroup" integer DEFAULT 0 NOT NULL,
    "Type" character varying(70) NOT NULL
)
INHERITS ("Class");



COMMENT ON TABLE "Menu" IS 'MODE: reserved|TYPE: class|DESCR: Menu|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Menu"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Menu"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Menu"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Menu"."IdParent" IS 'MODE: reserved|DESCR: Parent Item, 0 means no parent';



COMMENT ON COLUMN "Menu"."IdElementClass" IS 'MODE: reserved|DESCR: Class connect to this item';



COMMENT ON COLUMN "Menu"."IdElementObj" IS 'MODE: reserved|DESCR: Object connected to this item, 0 means no object';



COMMENT ON COLUMN "Menu"."Number" IS 'MODE: reserved|DESCR: Ordering';



COMMENT ON COLUMN "Menu"."IdGroup" IS 'MODE: reserved|DESCR: Group owner of this item, 0 means default group';



COMMENT ON COLUMN "Menu"."Type" IS 'MODE: reserved|DESCR: Group owner of this item, 0 means default group';



CREATE TABLE "Menu_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Menu");



CREATE TABLE "Metadata" (
)
INHERITS ("Class");



COMMENT ON TABLE "Metadata" IS 'MODE: reserved|TYPE: class|DESCR: Metadata|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Metadata"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."Code" IS 'MODE: read|DESCR: Schema|INDEX: 1';



COMMENT ON COLUMN "Metadata"."Description" IS 'MODE: read|DESCR: Key|INDEX: 2';



COMMENT ON COLUMN "Metadata"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Metadata"."Notes" IS 'MODE: read|DESCR: Value|INDEX: 3';



CREATE TABLE "Metadata_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Metadata");



CREATE TABLE "Monitor" (
    "Tipo" integer,
    "Dimensione" numeric(10,2)
)
INHERITS ("Item");



COMMENT ON TABLE "Monitor" IS 'MODE: write|TYPE: class|DESCR: Monitor|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Monitor"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."DataAcquisto" IS 'MODE: write|FIELDMODE: write|DESCR: Data Acquisto|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."DataCollaudo" IS 'MODE: write|FIELDMODE: write|DESCR: Data Collaudo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."NumeroDiSerie" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di serie|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."CostoFinale" IS 'MODE: write|FIELDMODE: write|DESCR: Costo finale|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."Stato" IS 'MODE: write|FIELDMODE: write|DESCR: Stato|INDEX: 8|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Stato Item|STATUS: active';



COMMENT ON COLUMN "Monitor"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaItem|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."PDL" IS 'MODE: write|FIELDMODE: write|DESCR: Posto di lavoro|INDEX: 10|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ComposizionePDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."Marca" IS 'MODE: write|FIELDMODE: write|DESCR: Marca|INDEX: 11|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Marca Item|STATUS: active';



COMMENT ON COLUMN "Monitor"."Modello" IS 'MODE: write|FIELDMODE: write|DESCR: Modello|INDEX: 12|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Monitor"."Tipo" IS 'MODE: write|FIELDMODE: write|DESCR: Tipo|INDEX: 13|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Tipo Monitor|STATUS: active';



COMMENT ON COLUMN "Monitor"."Dimensione" IS 'MODE: write|FIELDMODE: write|DESCR: Dimensione|INDEX: 14|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Monitor_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Monitor");



CREATE TABLE "PDL" (
    "Stanza" integer,
    "Tipo" integer
)
INHERITS ("Class");



COMMENT ON TABLE "PDL" IS 'MODE: write|TYPE: class|DESCR: Posto di lavoro|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "PDL"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "PDL"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "PDL"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "PDL"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "PDL"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "PDL"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "PDL"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "PDL"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Notes|INDEX: 3|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "PDL"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaPDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "PDL"."Tipo" IS 'MODE: write|FIELDMODE: write|DESCR: Tipo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Tipo PdL|STATUS: active';



CREATE TABLE "PDL_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("PDL");



CREATE TABLE "Palazzo" (
    "Indirizzo" character varying(100),
    "CAP" integer,
    "Comune" character varying(50),
    "Provincia" character varying(50)
)
INHERITS ("Class");



COMMENT ON TABLE "Palazzo" IS 'MODE: write|TYPE: class|DESCR: Palazzo|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Palazzo"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Palazzo"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Palazzo"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Nome|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Palazzo"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Palazzo"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Palazzo"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Palazzo"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Palazzo"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Palazzo"."Indirizzo" IS 'MODE: write|FIELDMODE: write|DESCR: Indirizzo|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Palazzo"."CAP" IS 'MODE: write|FIELDMODE: write|DESCR: CAP|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Palazzo"."Comune" IS 'MODE: write|FIELDMODE: write|DESCR: Comune|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Palazzo"."Provincia" IS 'MODE: write|FIELDMODE: write|DESCR: Provincia|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Palazzo_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Palazzo");



CREATE TABLE "Patch" (
)
INHERITS ("Class");



COMMENT ON TABLE "Patch" IS 'MODE: reserved|TYPE: class|DESCR: |SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Patch"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Patch"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Patch"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



CREATE TABLE "Patch_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Patch");



CREATE TABLE "Periferica" (
    "Tipo" integer
)
INHERITS ("Item");



COMMENT ON TABLE "Periferica" IS 'MODE: write|TYPE: class|DESCR: Periferica|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Periferica"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Periferica"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Periferica"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Periferica"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Periferica"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Periferica"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."DataAcquisto" IS 'MODE: write|FIELDMODE: write|DESCR: Data Acquisto|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."DataCollaudo" IS 'MODE: write|FIELDMODE: write|DESCR: Data Collaudo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."NumeroDiSerie" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di serie|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."CostoFinale" IS 'MODE: write|FIELDMODE: write|DESCR: Costo finale|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."Stato" IS 'MODE: write|FIELDMODE: write|DESCR: Stato|INDEX: 8|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Stato Item|STATUS: active';



COMMENT ON COLUMN "Periferica"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaItem|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."PDL" IS 'MODE: write|FIELDMODE: write|DESCR: Posto di lavoro|INDEX: 10|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ComposizionePDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."Marca" IS 'MODE: write|FIELDMODE: write|DESCR: Marca|INDEX: 11|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Marca Item|STATUS: active';



COMMENT ON COLUMN "Periferica"."Modello" IS 'MODE: write|FIELDMODE: write|DESCR: Modello|INDEX: 12|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Periferica"."Tipo" IS 'MODE: write|FIELDMODE: write|DESCR: Tipo|INDEX: 13|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: TIpo Periferica|STATUS: active';



CREATE TABLE "Periferica_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Periferica");



CREATE TABLE "Piano" (
    "Nome" character varying(50),
    "Palazzo" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Piano" IS 'MODE: write|TYPE: class|DESCR: Piano|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Piano"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Piano"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Piano"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Piano"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Piano"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Piano"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Piano"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Piano"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Piano"."Nome" IS 'MODE: write|FIELDMODE: write|DESCR: Nome|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Piano"."Palazzo" IS 'MODE: write|FIELDMODE: write|DESCR: Palazzo|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: PianoPalazzo|REFERENCEDIRECT: true|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Piano_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Piano");



CREATE TABLE "Report" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "Code" character varying(40),
    "Description" character varying(100),
    "Status" character varying(1),
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Type" character varying(20),
    "Query" text,
    "SimpleReport" bytea,
    "RichReport" bytea,
    "Wizard" bytea,
    "Images" bytea,
    "ImagesLength" integer[],
    "ReportLength" integer[],
    "IdClass" regclass,
    "Groups" integer[],
    "ImagesName" character varying[]
);



COMMENT ON TABLE "Report" IS 'MODE: reserved|TYPE: class|DESCR: Report|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Report"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."Code" IS 'MODE: read|DESCR: Codice';



COMMENT ON COLUMN "Report"."Description" IS 'MODE: read|DESCR: Descrizione';



COMMENT ON COLUMN "Report"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Report"."Type" IS 'MODE: read|DESCR: Tipo';



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



CREATE TABLE "Role" (
    "Code" character varying(100) NOT NULL,
    "Administrator" boolean,
    "startingClass" regclass,
    "Email" character varying(320),
    "DisabledModules" character varying[]
)
INHERITS ("Class");



COMMENT ON TABLE "Role" IS 'MODE: reserved|TYPE: class|DESCR: Roles|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Role"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Role"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC';



COMMENT ON COLUMN "Role"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."Notes" IS 'MODE: read|DESCR: Notes';



COMMENT ON COLUMN "Role"."Administrator" IS 'MODE: read|DESCR: Administrator|INDEX: 1|STATUS: active';



COMMENT ON COLUMN "Role"."startingClass" IS 'MODE: read|DESCR: Administrator|INDEX: 2|STATUS: active';



COMMENT ON COLUMN "Role"."Email" IS 'MODE: read|DESCR: Email|INDEX: 5';



COMMENT ON COLUMN "Role"."DisabledModules" IS 'MODE: read';



CREATE TABLE "Scheduler" (
    "CronExpression" text NOT NULL,
    "Detail" text NOT NULL
)
INHERITS ("Class");



COMMENT ON TABLE "Scheduler" IS 'MODE: reserved|TYPE: class|DESCR: Scheduler|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Scheduler"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Scheduler"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Scheduler"."Code" IS 'MODE: read|DESCR: Job Type|INDEX: 1';



COMMENT ON COLUMN "Scheduler"."Description" IS 'MODE: read|DESCR: Job Description|INDEX: 2';



COMMENT ON COLUMN "Scheduler"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Scheduler"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Scheduler"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Scheduler"."Notes" IS 'MODE: read|DESCR: Job Parameters|INDEX: 3';



COMMENT ON COLUMN "Scheduler"."CronExpression" IS 'MODE: read|DESCR: Cron Expression|INDEX: 8|STATUS: active';



COMMENT ON COLUMN "Scheduler"."Detail" IS 'MODE: read|DESCR: Job Detail|INDEX: 8|STATUS: active';



CREATE TABLE "Scheduler_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Scheduler");



CREATE TABLE "Software" (
    "Tipo" integer,
    "Versione" numeric(10,4)
)
INHERITS ("Item");



COMMENT ON TABLE "Software" IS 'MODE: write|TYPE: class|DESCR: Software|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Software"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Software"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Software"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Software"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Software"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Software"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."DataAcquisto" IS 'MODE: write|FIELDMODE: write|DESCR: Data Acquisto|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."DataCollaudo" IS 'MODE: write|FIELDMODE: write|DESCR: Data Collaudo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."NumeroDiSerie" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di serie|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."CostoFinale" IS 'MODE: write|FIELDMODE: write|DESCR: Costo finale|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."Stato" IS 'MODE: write|FIELDMODE: write|DESCR: Stato|INDEX: 8|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Stato Item|STATUS: active';



COMMENT ON COLUMN "Software"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaItem|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."PDL" IS 'MODE: write|FIELDMODE: write|DESCR: Posto di lavoro|INDEX: 10|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ComposizionePDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."Marca" IS 'MODE: write|FIELDMODE: write|DESCR: Marca|INDEX: 11|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Marca Item|STATUS: active';



COMMENT ON COLUMN "Software"."Modello" IS 'MODE: write|FIELDMODE: write|DESCR: Modello|INDEX: 12|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Software"."Tipo" IS 'MODE: write|FIELDMODE: write|DESCR: Categoria|INDEX: 13|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Tipo Software|STATUS: active';



COMMENT ON COLUMN "Software"."Versione" IS 'MODE: write|FIELDMODE: write|DESCR: Versione|INDEX: 14|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Software_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Software");



CREATE TABLE "Stampante" (
    "Tipo" integer,
    "Colore" boolean,
    "Fax" boolean,
    "Copiatrice" boolean,
    "Scanner" boolean,
    "FormatoCarta" integer
)
INHERITS ("Item");



COMMENT ON TABLE "Stampante" IS 'MODE: write|TYPE: class|DESCR: Stampante|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Stampante"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Stampante"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Stampante"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Stampante"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Stampante"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Stampante"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."DataAcquisto" IS 'MODE: write|FIELDMODE: write|DESCR: Data Acquisto|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."DataCollaudo" IS 'MODE: write|FIELDMODE: write|DESCR: Data Collaudo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."NumeroDiSerie" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di serie|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."CostoFinale" IS 'MODE: write|FIELDMODE: write|DESCR: Costo finale|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Stato" IS 'MODE: write|FIELDMODE: write|DESCR: Stato|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Stato Item|STATUS: active';



COMMENT ON COLUMN "Stampante"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 10|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaItem|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."PDL" IS 'MODE: write|FIELDMODE: write|DESCR: Posto di lavoro|INDEX: 11|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ComposizionePDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Marca" IS 'MODE: write|FIELDMODE: write|DESCR: Marca|INDEX: 12|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Marca Item|STATUS: active';



COMMENT ON COLUMN "Stampante"."Modello" IS 'MODE: write|FIELDMODE: write|DESCR: Modello|INDEX: 13|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Tipo" IS 'MODE: write|FIELDMODE: write|DESCR: Tipo|INDEX: 14|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Tipo Stampante|STATUS: active';



COMMENT ON COLUMN "Stampante"."Colore" IS 'MODE: write|FIELDMODE: write|DESCR: Colore|INDEX: 15|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Fax" IS 'MODE: write|FIELDMODE: write|DESCR: Fax|INDEX: 16|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Copiatrice" IS 'MODE: write|FIELDMODE: write|DESCR: Copiatrice|INDEX: 17|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."Scanner" IS 'MODE: write|FIELDMODE: write|DESCR: Scanner|INDEX: 18|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stampante"."FormatoCarta" IS 'MODE: write|FIELDMODE: write|DESCR: Formato carta|INDEX: 8|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Formato Carta|STATUS: active';



CREATE TABLE "Stampante_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Stampante");



CREATE TABLE "Stanza" (
    "Piano" integer,
    "Nome" character varying(50)
)
INHERITS ("Class");



COMMENT ON TABLE "Stanza" IS 'MODE: write|TYPE: class|DESCR: Stanza|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Stanza"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Stanza"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Stanza"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stanza"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stanza"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Stanza"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Stanza"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Stanza"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stanza"."Piano" IS 'MODE: write|FIELDMODE: write|DESCR: Piano|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: PianoStanza|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Stanza"."Nome" IS 'MODE: write|FIELDMODE: write|DESCR: Nome|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Stanza_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Stanza");



CREATE TABLE "Storage" (
    "Dimensione" integer
)
INHERITS ("Item");



COMMENT ON TABLE "Storage" IS 'MODE: write|TYPE: class|DESCR: Storage|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Storage"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Storage"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Storage"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Storage"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Storage"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Storage"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."DataAcquisto" IS 'MODE: write|FIELDMODE: write|DESCR: Data Acquisto|INDEX: 4|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."DataCollaudo" IS 'MODE: write|FIELDMODE: write|DESCR: Data Collaudo|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."NumeroDiSerie" IS 'MODE: write|FIELDMODE: write|DESCR: Numero di serie|INDEX: 6|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."CostoFinale" IS 'MODE: write|FIELDMODE: write|DESCR: Costo finale|INDEX: 7|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."Stato" IS 'MODE: write|FIELDMODE: write|DESCR: Stato|INDEX: 8|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Stato Item|STATUS: active';



COMMENT ON COLUMN "Storage"."Stanza" IS 'MODE: write|FIELDMODE: write|DESCR: Stanza|INDEX: 9|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: StanzaItem|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."PDL" IS 'MODE: write|FIELDMODE: write|DESCR: Posto di lavoro|INDEX: 10|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: ComposizionePDL|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."Marca" IS 'MODE: write|FIELDMODE: write|DESCR: Marca|INDEX: 11|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: Marca Item|STATUS: active';



COMMENT ON COLUMN "Storage"."Modello" IS 'MODE: write|FIELDMODE: write|DESCR: Modello|INDEX: 12|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Storage"."Dimensione" IS 'MODE: write|FIELDMODE: write|DESCR: Dimensione|INDEX: 13|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Storage_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Storage");



CREATE TABLE "Ufficio" (
    "DescrizioneBreve" character varying(100),
    "Respnsabile" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Ufficio" IS 'MODE: write|TYPE: class|DESCR: Ufficio|SUPERCLASS: false|MANAGER: class|STATUS: active';



COMMENT ON COLUMN "Ufficio"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Ufficio"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Ufficio"."Code" IS 'MODE: read|FIELDMODE: write|DESCR: Codice|INDEX: 1|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Ufficio"."Description" IS 'MODE: read|FIELDMODE: write|DESCR: Descrizione|INDEX: 2|BASEDSP: true|COLOR: #FFFFCC|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Ufficio"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Ufficio"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Ufficio"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Ufficio"."Notes" IS 'MODE: read|FIELDMODE: write|DESCR: Note|INDEX: 3|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Ufficio"."DescrizioneBreve" IS 'MODE: write|FIELDMODE: write|DESCR: Descrizione Breve|INDEX: 4|BASEDSP: true|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: |REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



COMMENT ON COLUMN "Ufficio"."Respnsabile" IS 'MODE: write|FIELDMODE: write|DESCR: Responsabile|INDEX: 5|BASEDSP: false|COLOR: #77FFFF|FONTCOLOR: #770000|LINEAFTER: false|REFERENCEDOM: Responsabile|REFERENCEDIRECT: false|REFERENCETYPE: restrict|LOOKUP: |STATUS: active';



CREATE TABLE "Ufficio_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Ufficio");



CREATE TABLE "User" (
    "Username" character varying(40) NOT NULL,
    "Password" character varying(40),
    "Email" character varying(320)
)
INHERITS ("Class");



COMMENT ON TABLE "User" IS 'MODE: reserved|TYPE: class|DESCR: Utenti|SUPERCLASS: false|MANAGER: class|STATUS: active';



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



CREATE SEQUENCE class_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;



COMMENT ON SEQUENCE class_seq IS 'Sequence for autoincrement class';



SELECT pg_catalog.setval('class_seq', 407, true);



CREATE VIEW system_classcatalog AS
    SELECT pg_class.oid AS classid, (CASE WHEN (pg_namespace.nspname = 'public'::name) THEN ''::text ELSE ((pg_namespace.nspname)::text || '.'::text) END || (pg_class.relname)::text) AS classname, pg_description.description AS classcomment, (pg_class.relkind = 'v'::"char") AS isview FROM ((pg_class JOIN pg_description ON ((((pg_description.objoid = pg_class.oid) AND (pg_description.objsubid = 0)) AND _cm_is_any_class_comment(pg_description.description)))) JOIN pg_namespace ON ((pg_namespace.oid = pg_class.relnamespace))) WHERE (pg_class.reltype > (0)::oid);



CREATE VIEW system_domaincatalog AS
    SELECT pg_class.oid AS domainid, "substring"((pg_class.relname)::text, 5) AS domainname, "substring"(pg_description.description, 'CLASS1: ([^|]*)'::text) AS domainclass1, "substring"(pg_description.description, 'CLASS2: ([^|]*)'::text) AS domainclass2, "substring"(pg_description.description, 'CARDIN: ([^|]*)'::text) AS domaincardinality, pg_description.description AS domaincomment, (pg_class.relkind = 'v'::"char") AS isview FROM (pg_class LEFT JOIN pg_description pg_description ON (((pg_description.objoid = pg_class.oid) AND (pg_description.objsubid = 0)))) WHERE (strpos(pg_description.description, 'TYPE: domain'::text) > 0);



CREATE VIEW system_attributecatalog AS
    SELECT cmtable.classid, cmtable.classname, pg_attribute.attname AS attributename, pg_attribute.attnum AS dbindex, CASE WHEN (strpos(attribute_description.description, 'MODE: reserved'::text) > 0) THEN (-1) WHEN (strpos(attribute_description.description, 'INDEX: '::text) > 0) THEN ("substring"(attribute_description.description, 'INDEX: ([^|]*)'::text))::integer ELSE 0 END AS attributeindex, (pg_attribute.attinhcount = 0) AS attributeislocal, CASE pg_type.typname WHEN 'geometry'::name THEN (_cm_get_geometry_type(cmtable.classid, (pg_attribute.attname)::text))::name ELSE pg_type.typname END AS attributetype, CASE WHEN (pg_type.typname = 'varchar'::name) THEN (pg_attribute.atttypmod - 4) ELSE NULL::integer END AS attributelength, CASE WHEN (pg_type.typname = 'numeric'::name) THEN (pg_attribute.atttypmod / 65536) ELSE NULL::integer END AS attributeprecision, CASE WHEN (pg_type.typname = 'numeric'::name) THEN ((pg_attribute.atttypmod - ((pg_attribute.atttypmod / 65536) * 65536)) - 4) ELSE NULL::integer END AS attributescale, ((notnulljoin.oid IS NOT NULL) OR pg_attribute.attnotnull) AS attributenotnull, pg_attrdef.adsrc AS attributedefault, attribute_description.description AS attributecomment, _cm_attribute_is_unique(cmtable.classid, (pg_attribute.attname)::text) AS isunique, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('LOOKUP'::character varying)::text) AS attributelookup, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCEDOM'::character varying)::text) AS attributereferencedomain, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCETYPE'::character varying)::text) AS attributereferencetype, _cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCEDIRECT'::character varying)::text) AS attributereferencedirect, CASE WHEN (system_domaincatalog.domaincardinality = '1:N'::text) THEN system_domaincatalog.domainclass1 ELSE system_domaincatalog.domainclass2 END AS attributereference FROM ((((((pg_attribute JOIN (SELECT system_classcatalog.classid, system_classcatalog.classname FROM system_classcatalog UNION SELECT system_domaincatalog.domainid AS classid, system_domaincatalog.domainname AS classname FROM system_domaincatalog) cmtable ON ((pg_attribute.attrelid = cmtable.classid))) LEFT JOIN pg_type ON ((pg_type.oid = pg_attribute.atttypid))) LEFT JOIN pg_description attribute_description ON (((attribute_description.objoid = cmtable.classid) AND (attribute_description.objsubid = pg_attribute.attnum)))) LEFT JOIN pg_attrdef pg_attrdef ON (((pg_attrdef.adrelid = pg_attribute.attrelid) AND (pg_attrdef.adnum = pg_attribute.attnum)))) LEFT JOIN system_domaincatalog ON (((_cm_legacy_read_comment(((attribute_description.description)::character varying)::text, ('REFERENCEDOM'::character varying)::text))::text = system_domaincatalog.domainname))) LEFT JOIN pg_constraint notnulljoin ON (((notnulljoin.conrelid = pg_attribute.attrelid) AND ((notnulljoin.conname)::text = _cm_notnull_constraint_name((pg_attribute.attname)::text))))) WHERE (((pg_attribute.atttypid > (0)::oid) AND (pg_attribute.attnum > 0)) AND (attribute_description.description IS NOT NULL));



CREATE VIEW system_inheritcatalog AS
    SELECT pg_inherits.inhparent AS parentid, pg_inherits.inhrelid AS childid FROM pg_inherits UNION SELECT ('"Class"'::regclass)::oid AS parentid, pg_class.oid AS childid FROM ((pg_class JOIN pg_description ON (((pg_description.objoid = pg_class.oid) AND (pg_description.objsubid = 0)))) LEFT JOIN pg_inherits ON ((pg_inherits.inhrelid = pg_class.oid))) WHERE ((pg_class.relkind = 'v'::"char") AND (strpos(pg_description.description, 'TYPE: class'::text) > 0));



CREATE VIEW system_treecatalog AS
    SELECT parent_class.classid AS parentid, parent_class.classname AS parent, parent_class.classcomment AS parentcomment, child_class.classid AS childid, child_class.classname AS child, child_class.classcomment AS childcomment FROM ((system_inheritcatalog JOIN system_classcatalog parent_class ON ((system_inheritcatalog.parentid = parent_class.classid))) JOIN system_classcatalog child_class ON ((system_inheritcatalog.childid = child_class.classid)));



CREATE VIEW system_availablemenuitems AS
    (((SELECT DISTINCT (system_classcatalog.classid)::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Code", _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text) AS "Description", CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <> ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND ("Role"."Id" = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END AS "IdElementClass", 0 AS "IdElementObj", "Role"."Id" AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Type" FROM ((system_classcatalog JOIN "Role" ON (("Role"."Status" = 'A'::bpchar))) LEFT JOIN system_treecatalog ON ((system_treecatalog.childid = system_classcatalog.classid))) WHERE (((NOT (system_classcatalog.classid IN (SELECT ("Menu"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" WHERE (((("Menu"."Code")::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND ("Menu"."Status" = 'A'::bpchar)) AND ("Role"."Id" = "Menu"."IdGroup"))))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text]))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('STATUS'::character varying)::text))::text = 'active'::text)) ORDER BY (system_classcatalog.classid)::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false), _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text), CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <> ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND ("Role"."Id" = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END, 0::integer, "Role"."Id", _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false)) UNION SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", "AllReport"."RoleId" AS "IdGroup", "AllReport"."Type" FROM ((SELECT ((_cm_legacy_get_menu_type(false, false, true, false))::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i]) AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type", "Role"."Id" AS "RoleId" FROM generate_series(1, 6) i(i), ("Report" JOIN "Role" ON (("Role"."Status" = 'A'::bpchar))) WHERE ((("Report"."Status")::text = 'A'::text) AND (((i.i + 1) / 2) = CASE WHEN (("Report"."Type")::text = 'normal'::text) THEN 1 WHEN (("Report"."Type")::text = 'custom'::text) THEN 2 WHEN (("Report"."Type")::text = 'openoffice'::text) THEN 3 ELSE 0 END))) "AllReport" LEFT JOIN "Menu" ON ((((("AllReport"."IdElementObj" = "Menu"."IdElementObj") AND ("Menu"."Status" = 'A'::bpchar)) AND ("AllReport"."RoleId" = "Menu"."IdGroup")) AND ("AllReport"."Code" = ("Menu"."Code")::text)))) WHERE ("Menu"."Code" IS NULL)) UNION (SELECT DISTINCT (system_classcatalog.classid)::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Code", _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text) AS "Description", CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <> ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND (0 = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END AS "IdElementClass", 0 AS "IdElementObj", 0 AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Type" FROM (system_classcatalog LEFT JOIN system_treecatalog ON ((system_treecatalog.childid = system_classcatalog.classid))) WHERE (((NOT (system_classcatalog.classid IN (SELECT ("Menu"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" WHERE (((("Menu"."Code")::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND ("Menu"."Status" = 'A'::bpchar)) AND (0 = "Menu"."IdGroup"))))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text]))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('STATUS'::character varying)::text))::text = 'active'::text)) ORDER BY (system_classcatalog.classid)::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false), _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text), CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <> ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND (0 = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END, 0::integer, _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false), 0::integer)) UNION SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", 0 AS "IdGroup", "AllReport"."Type" FROM ((SELECT ((_cm_legacy_get_menu_type(false, false, true, false))::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i]) AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type" FROM generate_series(1, 6) i(i), "Report" WHERE ((("Report"."Status")::text = 'A'::text) AND (((i.i + 1) / 2) = CASE WHEN (("Report"."Type")::text = 'normal'::text) THEN 1 WHEN (("Report"."Type")::text = 'custom'::text) THEN 2 WHEN (("Report"."Type")::text = 'openoffice'::text) THEN 3 ELSE 0 END))) "AllReport" LEFT JOIN "Menu" ON ((((("AllReport"."IdElementObj" = "Menu"."IdElementObj") AND ("Menu"."Status" = 'A'::bpchar)) AND (0 = "Menu"."IdGroup")) AND ("AllReport"."Code" = ("Menu"."Code")::text)))) WHERE ("Menu"."Code" IS NULL);



CREATE VIEW system_privilegescatalog AS
    SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode" FROM ((SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode" FROM "Grant" UNION SELECT (-1), '"Grant"', '', '', 'A', 'admin', now() AS now, NULL::unknown AS unknown, "Role"."Id", (system_classcatalog.classid)::regclass AS classid, '-' FROM system_classcatalog, "Role" WHERE ((((system_classcatalog.classid)::regclass)::oid <> ('"Class"'::regclass)::oid) AND (NOT ((("Role"."Id")::text || ((system_classcatalog.classid)::integer)::text) IN (SELECT (("Grant"."IdRole")::text || ((("Grant"."IdGrantedClass")::oid)::integer)::text) FROM "Grant"))))) permission JOIN system_classcatalog ON ((((permission."IdGrantedClass")::oid = system_classcatalog.classid) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('MODE'::character varying)::text))::text = ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text]))))) ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";



CREATE VIEW system_relationlist AS
    SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass1")::integer AS idclass1, "Map"."IdObj1" AS idobj1, ("Map"."IdClass2")::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, true AS direct, NULL::unknown AS version FROM ((((((("Map" JOIN "Class" ON ((((("Class"."IdClass")::oid = ("Map"."IdClass2")::oid) AND ("Class"."Id" = "Map"."IdObj2")) AND ("Class"."Status" = 'A'::bpchar)))) LEFT JOIN pg_class pg_class0 ON ((pg_class0.oid = ("Map"."IdDomain")::oid))) LEFT JOIN pg_description pg_description0 ON (((((pg_description0.objoid = pg_class0.oid) AND (pg_description0.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)))) LEFT JOIN pg_class pg_class1 ON ((pg_class1.oid = ("Map"."IdClass1")::oid))) LEFT JOIN pg_description pg_description1 ON (((pg_description1.objoid = pg_class1.oid) AND (pg_description1.objsubid = 0)))) LEFT JOIN pg_class pg_class2 ON ((pg_class2.oid = ("Map"."IdClass2")::oid))) LEFT JOIN pg_description pg_description2 ON (((pg_description2.objoid = pg_class2.oid) AND (pg_description2.objsubid = 0)))) UNION SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass2")::integer AS idclass1, "Map"."IdObj2" AS idobj1, ("Map"."IdClass1")::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRINV'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description1.description, 'DESCR'::text))::text AS classdescription, false AS direct, NULL::unknown AS version FROM ((((((("Map" JOIN "Class" ON ((((("Class"."IdClass")::oid = ("Map"."IdClass1")::oid) AND ("Class"."Id" = "Map"."IdObj1")) AND ("Class"."Status" = 'A'::bpchar)))) LEFT JOIN pg_class pg_class0 ON ((pg_class0.oid = ("Map"."IdDomain")::oid))) LEFT JOIN pg_description pg_description0 ON (((((pg_description0.objoid = pg_class0.oid) AND (pg_description0.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)))) LEFT JOIN pg_class pg_class1 ON ((pg_class1.oid = ("Map"."IdClass1")::oid))) LEFT JOIN pg_description pg_description1 ON (((pg_description1.objoid = pg_class1.oid) AND (pg_description1.objsubid = 0)))) LEFT JOIN pg_class pg_class2 ON ((pg_class2.oid = ("Map"."IdClass2")::oid))) LEFT JOIN pg_description pg_description2 ON (((pg_description2.objoid = pg_class2.oid) AND (pg_description2.objsubid = 0))));



CREATE VIEW system_relationlist_history AS
    SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass1")::integer AS idclass1, "Map"."IdObj1" AS idobj1, ("Map"."IdClass2")::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, true AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::unknown AS version FROM ("Map" LEFT JOIN "Class" ON (((("Class"."IdClass")::oid = ("Map"."IdClass2")::oid) AND ("Class"."Id" = "Map"."IdObj2")))), pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2 WHERE (((((((((((("Map"."Status" = 'U'::bpchar) AND (pg_class1.oid = ("Map"."IdClass1")::oid)) AND (pg_class2.oid = ("Map"."IdClass2")::oid)) AND (pg_class0.oid = ("Map"."IdDomain")::oid)) AND (pg_description0.objoid = pg_class0.oid)) AND (pg_description0.objsubid = 0)) AND (pg_description1.objoid = pg_class1.oid)) AND (pg_description1.objsubid = 0)) AND (pg_description2.objoid = pg_class2.oid)) AND (pg_description2.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)) UNION SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass2")::integer AS idclass1, "Map"."IdObj2" AS idobj1, ("Map"."IdClass1")::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRINV'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, false AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::unknown AS version FROM ("Map" LEFT JOIN "Class" ON (((("Class"."IdClass")::oid = ("Map"."IdClass1")::oid) AND ("Class"."Id" = "Map"."IdObj1")))), pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2 WHERE (((((((((((("Map"."Status" = 'U'::bpchar) AND (pg_class1.oid = ("Map"."IdClass1")::oid)) AND (pg_class2.oid = ("Map"."IdClass2")::oid)) AND (pg_class0.oid = ("Map"."IdDomain")::oid)) AND (pg_description0.objoid = pg_class0.oid)) AND (pg_description0.objsubid = 0)) AND (pg_description1.objoid = pg_class1.oid)) AND (pg_description1.objsubid = 0)) AND (pg_description2.objoid = pg_class2.oid)) AND (pg_description2.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description));









INSERT INTO "Computer" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk") VALUES (217, '"Computer"', 'COM0002', 'Intel  Pentium P4', 'A', 'admin', '2009-06-30 17:53:21.155239', NULL, NULL, NULL, '', NULL, 24, 76, 114, 216, 'Intel Pentium P4', 57, 'Intel Pentium P4', 1, 3, 1024, 100);
INSERT INTO "Computer" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk") VALUES (215, '"Computer"', 'COM0001', 'Pentium P4', 'A', 'admin', '2009-06-30 18:09:11.776697', NULL, '2007-06-02', '2007-06-03', '', NULL, 24, 76, 115, 50, 'HP1118', 57, 'Pentium P4 - 2.6GHz', 1, 3, 1024, 80);
INSERT INTO "Computer" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk") VALUES (221, '"Computer"', 'COM0003', 'Hp - A6316', 'A', 'admin', '2009-06-30 18:20:37.840517', NULL, NULL, NULL, '', NULL, 24, 75, 116, 50, 'A6316', 57, 'AMD Athlon 64 X2 Dual Core 5000+ 2.6Hz', 2, 3, 2048, 250);
INSERT INTO "Computer" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk") VALUES (225, '"Computer"', 'COM0004', 'Acer - Netbook D250', 'A', 'admin', '2009-06-30 18:29:11.78126', NULL, NULL, NULL, '', NULL, 24, 85, 113, 45, 'Netbook D250', 58, 'Intel Atom N280', 1, 2, 1024, 160);



INSERT INTO "Computer_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk", "CurrentId", "EndDate") VALUES (220, '"Computer"', 'COM0001', '', 'U', 'admin', '2009-06-30 17:46:24.514761', NULL, '2007-06-02', '2007-06-03', '', NULL, 24, 76, 115, 50, 'HP1118', 57, 'Pentium P4 - 2.6GHz', 1, 3, 1024, 80, 215, '2009-06-30 18:09:11.776697');
INSERT INTO "Computer_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk", "CurrentId", "EndDate") VALUES (224, '"Computer"', 'COM003', 'Hp - A6316', 'U', 'admin', '2009-06-30 18:16:13.411187', NULL, NULL, NULL, '', NULL, 24, 75, 116, 50, 'A6316', 57, 'AMD Athlon 64 X2 Dual Core 5000+ 2.6Hz', 2, 3, 2048, 250, 221, '2009-06-30 18:20:37.840517');
INSERT INTO "Computer_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk", "CurrentId", "EndDate") VALUES (227, '"Computer"', 'COM004', 'Acer - Netbook D250', 'U', 'admin', '2009-06-30 18:28:30.007478', NULL, NULL, NULL, '', NULL, NULL, NULL, NULL, 45, 'Netbook D250', 58, 'Intel Atom N280', 1, 2, 1024, 160, 225, '2009-06-30 18:29:02.457246');
INSERT INTO "Computer_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Processore", "NumeroDiProcessori", "VelocitaProcessore", "RAM", "HardDisk", "CurrentId", "EndDate") VALUES (229, '"Computer"', 'COM004', 'Acer - Netbook D250', 'U', 'admin', '2009-06-30 18:29:02.457246', NULL, NULL, NULL, '', NULL, 24, 85, 113, 45, 'Netbook D250', 58, 'Intel Atom N280', 1, 2, 1024, 160, 225, '2009-06-30 18:29:11.78126');



INSERT INTO "Dipendente" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio") VALUES (100, '"Dipendente"', '000004', 'Martino Gialli', 'A', 'admin', '2009-06-30 16:49:44.851829', NULL, 14, 11, 6, 'm.gialli@consulente.it', NULL);
INSERT INTO "Dipendente" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio") VALUES (101, '"Dipendente"', '000005', 'Fabio Ciani', 'A', 'admin', '2009-06-30 16:52:55.495003', NULL, 15, 10, 8, 'f.ciani@azienda.it', 102);
INSERT INTO "Dipendente" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio") VALUES (98, '"Dipendente"', '000002', 'Lisa Bianchi', 'A', 'admin', '2009-06-30 16:53:11.068833', NULL, 15, 10, 6, 'l.bianchi@azienda.it', 103);
INSERT INTO "Dipendente" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio") VALUES (99, '"Dipendente"', '000003', 'Giuseppe Verdi', 'A', 'admin', '2009-06-30 16:53:21.376255', NULL, 15, 9, 5, 'g.verdi@azienda.it', 103);
INSERT INTO "Dipendente" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio") VALUES (61, '"Dipendente"', '000001', 'Paolo Rossi', 'A', 'admin', '2009-06-30 16:54:12.346912', NULL, 16, 9, 5, 'p.rossi@azienda.it', 103);
INSERT INTO "Dipendente" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio") VALUES (124, '"Dipendente"', '000007', 'Alex Arancio', 'A', 'admin', '2009-06-30 17:02:39.753117', NULL, 17, 9, 6, 'a.arancio@azienda.it', 119);
INSERT INTO "Dipendente" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio") VALUES (117, '"Dipendente"', '000006', 'Enrico Bruni', 'A', 'admin', '2009-06-30 17:03:05.39877', NULL, 17, 10, 7, 'e.bruni@azienda.it', 119);



INSERT INTO "Dipendente_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio", "CurrentId", "EndDate") VALUES (105, '"Dipendente"', '000005', 'Fabio Ciani', 'U', 'admin', '2009-06-30 16:50:58.127269', NULL, 15, 10, 8, 'f.ciani@azienda.it', NULL, 101, '2009-06-30 16:52:55.495003');
INSERT INTO "Dipendente_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio", "CurrentId", "EndDate") VALUES (107, '"Dipendente"', '000002', 'Lisa Bianchi', 'U', 'admin', '2009-06-30 16:47:25.61384', NULL, 15, 10, 6, 'l.bianchi@azienda.it', NULL, 98, '2009-06-30 16:53:11.068833');
INSERT INTO "Dipendente_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio", "CurrentId", "EndDate") VALUES (109, '"Dipendente"', '000003', 'Giuseppe Verdi', 'U', 'admin', '2009-06-30 16:48:11.266026', NULL, 15, 9, 5, 'g.verdi@azienda.it', NULL, 99, '2009-06-30 16:53:21.376255');
INSERT INTO "Dipendente_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio", "CurrentId", "EndDate") VALUES (112, '"Dipendente"', '000001', 'Paolo Rossi', 'U', 'admin', '2009-06-30 16:12:26.281936', NULL, NULL, 9, 5, 'p.rossi@azienda.it', NULL, 61, '2009-06-30 16:54:12.346912');
INSERT INTO "Dipendente_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Tipo", "Qualifica", "Livello", "Email", "Ufficio", "CurrentId", "EndDate") VALUES (126, '"Dipendente"', '000006', 'Enrico Bruni', 'U', 'admin', '2009-06-30 16:59:34.804545', NULL, 17, 10, 7, 'e.bruni@azienda.it', NULL, 117, '2009-06-30 17:03:05.39877');









INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (129, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:05:44.156605', NULL, 128, '"PDL"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (130, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:05:48.330896', NULL, 128, '"Ufficio"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (131, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:05:57.957447', NULL, 128, '"Dipendente"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (132, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:04.812634', NULL, 128, '"Stanza"', 'r');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (133, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:11.20351', NULL, 128, '"Piano"', 'r');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (134, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:18.172848', NULL, 128, '"Palazzo"', 'r');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (135, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:34.649606', NULL, 127, '"Item"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (136, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:36.092633', NULL, 127, '"Monitor"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (137, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:38.853432', NULL, 127, '"Periferica"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (138, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:43.69403', NULL, 127, '"Software"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (139, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:47.259344', NULL, 127, '"Stampante"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (140, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:06:52.573407', NULL, 127, '"Storage"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (141, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:07:00.99092', NULL, 127, '1140387', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (142, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:07:03.701194', NULL, 127, '"Computer"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (143, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:07:34.478136', NULL, 127, '"Stanza"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (144, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:07:37.598049', NULL, 127, '"PDL"', 'w');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (146, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:08:08.212371', NULL, 127, '"Piano"', 'r');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (147, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:08:12.153837', NULL, 127, '"Palazzo"', 'r');
INSERT INTO "Grant" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdRole", "IdGrantedClass", "Mode") VALUES (148, '"Grant"', '', '', 'A', 'admin', '2009-06-30 17:08:22.144464', NULL, 127, '"Dipendente"', '-');






INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (4, '"LookUp"', NULL, 'Immagini', 'A', NULL, '2009-06-30 10:15:31.740966', '', 'Alfresco Category', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (6, '"LookUp"', NULL, 'Silver', 'A', NULL, '2009-06-30 10:16:08.335873', '', 'Livello Dipendenti', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (7, '"LookUp"', NULL, 'Gold', 'A', NULL, '2009-06-30 10:16:23.179319', '', 'Livello Dipendenti', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (8, '"LookUp"', NULL, 'Platinum', 'A', NULL, '2009-06-30 10:16:30.313859', '', 'Livello Dipendenti', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (10, '"LookUp"', NULL, 'Responsabile', 'A', NULL, '2009-06-30 10:22:39.147032', '', 'Qualifica Dipendente', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (11, '"LookUp"', NULL, 'Consulente', 'A', NULL, '2009-06-30 10:22:50.837796', '', 'Qualifica Dipendente', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (13, '"LookUp"', NULL, 'Passivo', 'A', NULL, '2009-06-30 10:23:20.053589', '', 'Stato Dipendente', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (15, '"LookUp"', NULL, 'Amministrativo', 'A', NULL, '2009-06-30 10:23:45.123152', '', 'Tipo Dipendente', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (16, '"LookUp"', NULL, 'Legale', 'A', NULL, '2009-06-30 10:24:09.030103', '', 'Tipo Dipendente', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (17, '"LookUp"', NULL, 'Servizio', 'A', NULL, '2009-06-30 10:24:18.381479', '', 'Tipo Dipendente', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (18, '"LookUp"', NULL, 'Altro', 'A', NULL, '2009-06-30 10:24:29.137284', '', 'Tipo Dipendente', NULL, NULL, 5, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (20, '"LookUp"', NULL, 'Dismesso', 'A', NULL, '2009-06-30 11:00:35.04218', '', 'Stato Item', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (21, '"LookUp"', NULL, 'Esterno', 'A', NULL, '2009-06-30 11:00:48.822242', '', 'Stato Item', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (22, '"LookUp"', NULL, 'Manutenzione', 'A', NULL, '2009-06-30 11:01:01.231146', '', 'Stato Item', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (23, '"LookUp"', NULL, 'Installato', 'A', NULL, '2009-06-30 11:01:12.549688', '', 'Stato Item', NULL, NULL, 5, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (24, '"LookUp"', NULL, 'Operativo', 'A', NULL, '2009-06-30 11:01:23.87764', '', 'Stato Item', NULL, NULL, 6, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (25, '"LookUp"', NULL, 'Ordinato', 'A', NULL, '2009-06-30 11:01:34.013947', '', 'Stato Item', NULL, NULL, 7, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (27, '"LookUp"', NULL, 'Software applicativo', 'A', NULL, '2009-06-30 12:40:27.423618', '', 'Tipo Software', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (28, '"LookUp"', NULL, 'Software di ambiente', 'A', NULL, '2009-06-30 12:40:40.613844', '', 'Tipo Software', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (29, '"LookUp"', NULL, 'Software di utilità', 'A', NULL, '2009-06-30 12:40:58.791797', '', 'Tipo Software', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (31, '"LookUp"', NULL, 'Router', 'A', NULL, '2009-06-30 12:41:55.28508', '', 'Dispositivo di rete', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (32, '"LookUp"', NULL, 'Switch', 'A', NULL, '2009-06-30 12:42:02.416857', '', 'Dispositivo di rete', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (34, '"LookUp"', NULL, 'Inkjet', 'A', NULL, '2009-06-30 12:42:36.916259', '', 'Tipo Stampante', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (35, '"LookUp"', NULL, 'Laser', 'A', NULL, '2009-06-30 12:42:53.110516', '', 'Tipo Stampante', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (36, '"LookUp"', NULL, 'Termica', 'A', NULL, '2009-06-30 12:43:04.96145', '', 'Tipo Stampante', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (38, '"LookUp"', NULL, 'LCD', 'A', NULL, '2009-06-30 12:48:48.734753', '', 'Tipo Monitor', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (39, '"LookUp"', NULL, 'Plasma', 'A', NULL, '2009-06-30 12:49:11.452245', '', 'Tipo Monitor', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (41, '"LookUp"', NULL, 'Microfono', 'A', NULL, '2009-06-30 12:49:52.894411', '', 'TIpo Periferica', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (42, '"LookUp"', NULL, 'Videocamera', 'A', NULL, '2009-06-30 12:50:10.882538', '', 'TIpo Periferica', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (43, '"LookUp"', NULL, 'Scanner', 'A', NULL, '2009-06-30 12:50:25.78501', '', 'TIpo Periferica', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (44, '"LookUp"', NULL, 'Modem', 'A', NULL, '2009-06-30 12:51:16.519468', '', 'TIpo Periferica', NULL, NULL, 5, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (46, '"LookUp"', NULL, 'Nokia', 'A', NULL, '2009-06-30 12:52:11.696909', '', 'Marca Item', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (47, '"LookUp"', NULL, 'Samsung', 'A', NULL, '2009-06-30 12:52:18.648021', '', 'Marca Item', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (48, '"LookUp"', NULL, 'Lg', 'A', NULL, '2009-06-30 12:52:25.429756', '', 'Marca Item', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (54, '"LookUp"', NULL, 'A4', 'A', NULL, '2009-06-30 13:10:01.910413', '', 'Formato Carta', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (55, '"LookUp"', NULL, 'Etichette', 'A', NULL, '2009-06-30 13:10:09.105645', '', 'Formato Carta', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (57, '"LookUp"', NULL, 'Desktop', 'A', NULL, '2009-06-30 13:18:44.593361', '', 'Tipo Computer', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (58, '"LookUp"', NULL, 'Notebook', 'A', NULL, '2009-06-30 13:19:29.056117', '', 'Tipo Computer', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (59, '"LookUp"', NULL, 'Palmare', 'A', NULL, '2009-06-30 13:19:36.539722', '', 'Tipo Computer', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (60, '"LookUp"', NULL, 'Server', 'A', NULL, '2009-06-30 13:19:43.503911', '', 'Tipo Computer', NULL, NULL, 5, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (91, '"LookUp"', NULL, 'Multiutente', 'A', NULL, '2009-06-30 16:40:27.78775', '', 'Tipo PdL', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (93, '"LookUp"', NULL, 'Pubblico', 'A', NULL, '2009-06-30 16:40:33.815313', '', 'Tipo PdL', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (208, '"LookUp"', NULL, 'Sun', 'A', NULL, '2009-06-30 17:32:19.338933', '', 'Marca Item', NULL, NULL, 13, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (110, '"LookUp"', NULL, 'Microsoft', 'A', NULL, '2009-06-30 16:53:47.246647', '', 'Marca Item', NULL, NULL, 10, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (50, '"LookUp"', NULL, 'Hp', 'A', NULL, '2009-06-30 12:52:36.54214', '', 'Marca Item', NULL, NULL, 6, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (49, '"LookUp"', NULL, 'Brother', 'A', NULL, '2009-06-30 12:52:31.252638', '', 'Marca Item', NULL, NULL, 5, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (216, '"LookUp"', NULL, 'Intel', 'A', NULL, '2009-06-30 17:48:55.708835', '', 'Marca Item', NULL, NULL, 7, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (187, '"LookUp"', NULL, 'Canon', 'A', NULL, '2009-06-30 17:17:18.745601', '', 'Marca Item', NULL, NULL, 11, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (181, '"LookUp"', NULL, 'Epson', 'A', NULL, '2009-06-30 17:14:53.593535', '', 'Marca Item', NULL, NULL, 12, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (52, '"LookUp"', NULL, 'Packard Bell', 'A', NULL, '2009-06-30 12:52:53.85109', '', 'Marca Item', NULL, NULL, 8, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (51, '"LookUp"', NULL, 'Sony', 'A', NULL, '2009-06-30 12:52:43.207411', '', 'Marca Item', NULL, NULL, 9, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (302, '"LookUp"', 'open.running', 'Avviato', 'A', NULL, '2009-07-07 09:59:23.315756', NULL, 'FlowStatus', NULL, NULL, 1, true);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (303, '"LookUp"', 'open.not_running.suspended', 'Sospeso', 'A', NULL, '2009-07-07 09:59:23.315756', NULL, 'FlowStatus', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (304, '"LookUp"', 'closed.completed', 'Completato', 'A', NULL, '2009-07-07 09:59:23.315756', NULL, 'FlowStatus', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (305, '"LookUp"', 'closed.terminated', 'Terminato', 'A', NULL, '2009-07-07 09:59:23.315756', NULL, 'FlowStatus', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (306, '"LookUp"', 'closed.aborted', 'Interrotto', 'A', NULL, '2009-07-07 09:59:23.315756', NULL, 'FlowStatus', NULL, NULL, 5, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (3, '"LookUp"', NULL, 'Documenti', 'A', NULL, '2009-06-30 10:15:04.110393', '', 'Alfresco Category', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (5, '"LookUp"', NULL, 'Normal', 'A', NULL, '2009-06-30 10:15:44.081418', '', 'Livello Dipendenti', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (9, '"LookUp"', NULL, 'Impiegato', 'A', NULL, '2009-06-30 10:16:45.594614', '', 'Qualifica Dipendente', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (12, '"LookUp"', NULL, 'Attivo', 'A', NULL, '2009-06-30 10:23:02.781694', '', 'Stato Dipendente', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (14, '"LookUp"', NULL, 'Esterno', 'A', NULL, '2009-06-30 10:23:34.373344', '', 'Tipo Dipendente', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (19, '"LookUp"', NULL, 'In Magazzino', 'A', NULL, '2009-06-30 10:59:52.158011', '', 'Stato Item', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (26, '"LookUp"', NULL, 'Sistema operativo', 'A', NULL, '2009-06-30 12:39:57.347908', '', 'Tipo Software', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (30, '"LookUp"', NULL, 'Hub', 'A', NULL, '2009-06-30 12:41:27.828658', '', 'Dispositivo di rete', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (33, '"LookUp"', NULL, 'Impatto', 'A', NULL, '2009-06-30 12:42:16.034283', '', 'Tipo Stampante', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (37, '"LookUp"', NULL, 'RCT', 'A', NULL, '2009-06-30 12:48:29.261919', '', 'Tipo Monitor', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (40, '"LookUp"', NULL, 'Lettore SmartCard', 'A', NULL, '2009-06-30 12:49:28.131765', '', 'TIpo Periferica', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (45, '"LookUp"', NULL, 'Acer', 'A', NULL, '2009-06-30 12:51:53.753454', '', 'Marca Item', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (53, '"LookUp"', NULL, 'A3', 'A', NULL, '2009-06-30 13:09:50.448017', '', 'Formato Carta', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (56, '"LookUp"', NULL, 'Desktop non vedenti', 'A', NULL, '2009-06-30 13:18:14.820366', '', 'Tipo Computer', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (90, '"LookUp"', NULL, 'Monoutente', 'A', NULL, '2009-06-30 16:39:58.463912', '', 'Tipo PdL', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (308, '"LookUp"', NULL, 'New', 'A', NULL, '2009-11-27 15:40:14.778169', NULL, 'EmailStatus', NULL, NULL, 1, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (309, '"LookUp"', NULL, 'Received', 'A', NULL, '2009-11-27 15:40:14.778169', NULL, 'EmailStatus', NULL, NULL, 2, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (310, '"LookUp"', NULL, 'Draft', 'A', NULL, '2009-11-27 15:40:14.778169', NULL, 'EmailStatus', NULL, NULL, 3, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (311, '"LookUp"', NULL, 'Outgoing', 'A', NULL, '2009-11-27 15:40:14.778169', NULL, 'EmailStatus', NULL, NULL, 4, false);
INSERT INTO "LookUp" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Type", "ParentType", "ParentId", "Number", "IsDefault") VALUES (312, '"LookUp"', NULL, 'Sent', 'A', NULL, '2009-11-27 15:40:14.778169', NULL, 'EmailStatus', NULL, NULL, 5, false);












INSERT INTO "Map_Appartenenti" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Appartenenti"', '"Ufficio"', 102, '"Dipendente"', 101, 'A', NULL, '2009-06-30 16:52:55.495003', NULL, 346);
INSERT INTO "Map_Appartenenti" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Appartenenti"', '"Ufficio"', 103, '"Dipendente"', 98, 'A', NULL, '2009-06-30 16:53:11.068833', NULL, 347);
INSERT INTO "Map_Appartenenti" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Appartenenti"', '"Ufficio"', 103, '"Dipendente"', 99, 'A', NULL, '2009-06-30 16:53:21.376255', NULL, 348);
INSERT INTO "Map_Appartenenti" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Appartenenti"', '"Ufficio"', 103, '"Dipendente"', 61, 'A', NULL, '2009-06-30 16:54:12.346912', NULL, 349);
INSERT INTO "Map_Appartenenti" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Appartenenti"', '"Ufficio"', 119, '"Dipendente"', 124, 'A', NULL, '2009-06-30 17:02:39.753117', NULL, 350);
INSERT INTO "Map_Appartenenti" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Appartenenti"', '"Ufficio"', 119, '"Dipendente"', 117, 'A', 'admin', '2009-06-30 17:03:05.39877', NULL, 351);






INSERT INTO "Map_Assegnazione" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Assegnazione"', '"Dipendente"', 98, '"PDL"', 114, 'A', 'admin', '2009-06-30 16:55:46.534664', NULL, 352);
INSERT INTO "Map_Assegnazione" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Assegnazione"', '"Dipendente"', 99, '"PDL"', 115, 'A', 'admin', '2009-06-30 16:56:43.074088', NULL, 353);
INSERT INTO "Map_Assegnazione" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Assegnazione"', '"Dipendente"', 61, '"PDL"', 115, 'A', 'admin', '2009-06-30 16:56:43.127179', NULL, 354);
INSERT INTO "Map_Assegnazione" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Assegnazione"', '"Dipendente"', 101, '"PDL"', 116, 'A', 'admin', '2009-06-30 16:57:26.751916', NULL, 355);
INSERT INTO "Map_Assegnazione" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Assegnazione"', '"Dipendente"', 117, '"PDL"', 123, 'A', 'admin', '2009-06-30 17:01:46.816602', NULL, 356);






INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 114, '"Stampante"', 204, 'A', NULL, '2009-06-30 17:28:42.583816', NULL, 357);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 115, '"Stampante"', 200, 'A', NULL, '2009-06-30 17:29:13.81156', NULL, 358);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 123, '"Storage"', 207, 'A', NULL, '2009-06-30 17:31:57.362872', NULL, 359);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 123, '"Storage"', 211, 'A', NULL, '2009-06-30 17:33:48.431835', NULL, 360);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 115, '"Computer"', 215, 'A', NULL, '2009-06-30 17:46:24.514761', NULL, 361);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 114, '"Computer"', 217, 'A', NULL, '2009-06-30 17:53:21.155239', NULL, 362);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 116, '"Monitor"', 218, 'A', NULL, '2009-06-30 18:08:39.357642', NULL, 363);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 116, '"Computer"', 221, 'A', NULL, '2009-06-30 18:16:13.411187', NULL, 364);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 116, '"Monitor"', 222, 'A', NULL, '2009-06-30 18:19:27.794392', NULL, 365);
INSERT INTO "Map_ComposizionePDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComposizionePDL"', '"PDL"', 113, '"Computer"', 225, 'A', NULL, '2009-06-30 18:29:02.457246', NULL, 366);






INSERT INTO "Map_ComputerMonitor" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComputerMonitor"', '"Computer"', 215, '"Monitor"', 218, 'A', 'admin', '2009-06-30 18:08:40.544971', NULL, 367);
INSERT INTO "Map_ComputerMonitor" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ComputerMonitor"', '"Computer"', 221, '"Monitor"', 222, 'A', 'admin', '2009-06-30 18:19:28.023613', NULL, 368);






INSERT INTO "Map_ItemSoftware" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ItemSoftware"', '"Computer"', 217, '"Software"', 118, 'A', 'admin', '2009-06-30 18:09:43.841107', NULL, 369);
INSERT INTO "Map_ItemSoftware" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_ItemSoftware"', '"Computer"', 221, '"Software"', 122, 'A', 'admin', '2009-06-30 18:16:31.196085', NULL, 370);






INSERT INTO "Map_PianoPalazzo" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoPalazzo"', '"Piano"', 68, '"Palazzo"', 62, 'A', NULL, '2009-06-30 16:21:21.358659', NULL, 371);
INSERT INTO "Map_PianoPalazzo" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoPalazzo"', '"Piano"', 73, '"Palazzo"', 62, 'A', NULL, '2009-06-30 16:22:24.223117', NULL, 372);
INSERT INTO "Map_PianoPalazzo" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoPalazzo"', '"Piano"', 74, '"Palazzo"', 65, 'A', NULL, '2009-06-30 16:23:04.278072', NULL, 373);






INSERT INTO "Map_PianoStanza" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoStanza"', '"Piano"', 68, '"Stanza"', 75, 'A', NULL, '2009-06-30 16:26:57.34133', NULL, 374);
INSERT INTO "Map_PianoStanza" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoStanza"', '"Piano"', 68, '"Stanza"', 76, 'A', NULL, '2009-06-30 16:27:25.339432', NULL, 375);
INSERT INTO "Map_PianoStanza" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoStanza"', '"Piano"', 73, '"Stanza"', 81, 'A', NULL, '2009-06-30 16:30:29.24521', NULL, 376);
INSERT INTO "Map_PianoStanza" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoStanza"', '"Piano"', 73, '"Stanza"', 82, 'A', NULL, '2009-06-30 16:31:47.935447', NULL, 377);
INSERT INTO "Map_PianoStanza" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoStanza"', '"Piano"', 74, '"Stanza"', 84, 'A', NULL, '2009-06-30 16:32:48.764241', NULL, 378);
INSERT INTO "Map_PianoStanza" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoStanza"', '"Piano"', 74, '"Stanza"', 85, 'A', NULL, '2009-06-30 16:33:06.611229', NULL, 379);
INSERT INTO "Map_PianoStanza" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_PianoStanza"', '"Piano"', 74, '"Stanza"', 86, 'A', NULL, '2009-06-30 16:33:41.210728', NULL, 380);






INSERT INTO "Map_Responsabile" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Responsabile"', '"Dipendente"', 101, '"Ufficio"', 102, 'A', NULL, '2009-06-30 16:52:04.296633', NULL, 381);
INSERT INTO "Map_Responsabile" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Responsabile"', '"Dipendente"', 98, '"Ufficio"', 103, 'A', NULL, '2009-06-30 16:52:27.937903', NULL, 382);
INSERT INTO "Map_Responsabile" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_Responsabile"', '"Dipendente"', 117, '"Ufficio"', 119, 'A', NULL, '2009-06-30 17:00:11.143587', NULL, 383);






INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 76, '"Monitor"', 89, 'A', NULL, '2009-06-30 16:38:58.762406', NULL, 384);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 75, '"Monitor"', 92, 'A', NULL, '2009-06-30 16:40:29.249118', NULL, 385);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 76, '"Stampante"', 204, 'A', NULL, '2009-06-30 17:28:42.583816', NULL, 386);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 76, '"Stampante"', 200, 'A', NULL, '2009-06-30 17:29:13.81156', NULL, 387);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 84, '"Storage"', 207, 'A', NULL, '2009-06-30 17:31:57.362872', NULL, 388);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 84, '"Storage"', 211, 'A', NULL, '2009-06-30 17:33:48.431835', NULL, 389);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 76, '"Computer"', 215, 'A', NULL, '2009-06-30 17:46:24.514761', NULL, 390);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 76, '"Computer"', 217, 'A', NULL, '2009-06-30 17:53:21.155239', NULL, 391);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 75, '"Monitor"', 218, 'A', NULL, '2009-06-30 18:08:39.357642', NULL, 392);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 75, '"Computer"', 221, 'A', NULL, '2009-06-30 18:16:13.411187', NULL, 393);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 75, '"Monitor"', 222, 'A', NULL, '2009-06-30 18:19:27.794392', NULL, 394);
INSERT INTO "Map_StanzaItem" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaItem"', '"Stanza"', 85, '"Computer"', 225, 'A', NULL, '2009-06-30 18:29:02.457246', NULL, 395);






INSERT INTO "Map_StanzaPDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaPDL"', '"Stanza"', 82, '"PDL"', 113, 'A', NULL, '2009-06-30 16:55:03.03035', NULL, 396);
INSERT INTO "Map_StanzaPDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaPDL"', '"Stanza"', 76, '"PDL"', 114, 'A', NULL, '2009-06-30 16:55:24.066833', NULL, 397);
INSERT INTO "Map_StanzaPDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaPDL"', '"Stanza"', 76, '"PDL"', 115, 'A', NULL, '2009-06-30 16:56:04.967188', NULL, 398);
INSERT INTO "Map_StanzaPDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaPDL"', '"Stanza"', 75, '"PDL"', 116, 'A', NULL, '2009-06-30 16:57:11.987021', NULL, 399);
INSERT INTO "Map_StanzaPDL" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "Id") VALUES ('"Map_StanzaPDL"', '"Stanza"', 85, '"PDL"', 123, 'A', NULL, '2009-06-30 17:01:25.526583', NULL, 400);






INSERT INTO "Map_UserRole" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "DefaultGroup", "Id") VALUES ('"Map_UserRole"', '"User"', 1, '"Role"', 2, 'A', NULL, '2009-06-30 09:30:48.193496', NULL, NULL, 401);
INSERT INTO "Map_UserRole" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "DefaultGroup", "Id") VALUES ('"Map_UserRole"', '"User"', 1, '"Role"', 127, 'A', 'admin', '2009-06-30 17:05:17.003169', NULL, NULL, 402);
INSERT INTO "Map_UserRole" ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "Status", "User", "BeginDate", "EndDate", "DefaultGroup", "Id") VALUES ('"Map_UserRole"', '"User"', 1, '"Role"', 128, 'A', 'admin', '2009-06-30 17:05:22.483479', NULL, NULL, 403);






INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (157, '"Menu"', 'class', 'Item', 'A', NULL, '2009-06-30 17:14:27.112914', NULL, 0, '"Item"', 0, 0, 127, 'superclass');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (158, '"Menu"', 'class', 'Computer', 'A', NULL, '2009-06-30 17:14:27.112914', NULL, 0, '"Computer"', 0, 1, 127, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (160, '"Menu"', 'class', 'Monitor', 'A', NULL, '2009-06-30 17:14:27.112914', NULL, 0, '"Monitor"', 0, 3, 127, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (161, '"Menu"', 'class', 'Periferica', 'A', NULL, '2009-06-30 17:14:27.112914', NULL, 0, '"Periferica"', 0, 4, 127, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (162, '"Menu"', 'class', 'Software', 'A', NULL, '2009-06-30 17:14:27.112914', NULL, 0, '"Software"', 0, 5, 127, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (163, '"Menu"', 'class', 'Stampante', 'A', NULL, '2009-06-30 17:14:27.112914', NULL, 0, '"Stampante"', 0, 6, 127, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (164, '"Menu"', 'class', 'Storage', 'A', NULL, '2009-06-30 17:14:27.112914', NULL, 0, '"Storage"', 0, 7, 127, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (149, '"Menu"', 'class', 'Item', 'N', NULL, '2009-06-30 17:14:37.913963', NULL, 0, '"Item"', 0, 0, 128, 'superclass');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (150, '"Menu"', 'class', 'Computer', 'N', NULL, '2009-06-30 17:14:37.913963', NULL, 0, '"Computer"', 0, 1, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (152, '"Menu"', 'class', 'Monitor', 'N', NULL, '2009-06-30 17:14:37.913963', NULL, 0, '"Monitor"', 0, 3, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (153, '"Menu"', 'class', 'Periferica', 'N', NULL, '2009-06-30 17:14:37.913963', NULL, 0, '"Periferica"', 0, 4, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (154, '"Menu"', 'class', 'Software', 'N', NULL, '2009-06-30 17:14:37.913963', NULL, 0, '"Software"', 0, 5, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (155, '"Menu"', 'class', 'Stampante', 'N', NULL, '2009-06-30 17:14:37.913963', NULL, 0, '"Stampante"', 0, 6, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (156, '"Menu"', 'class', 'Storage', 'N', NULL, '2009-06-30 17:14:37.913963', NULL, 0, '"Storage"', 0, 7, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (182, '"Menu"', 'class', 'Palazzo', 'N', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Palazzo"', 0, 0, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (183, '"Menu"', 'class', 'Piano', 'N', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Piano"', 0, 1, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (184, '"Menu"', 'class', 'Periferica', 'N', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Periferica"', 0, 2, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (185, '"Menu"', 'class', 'Posto Di Lavoro', 'N', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"PDL"', 0, 3, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (196, '"Menu"', 'class', 'Dipendente', 'A', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Dipendente"', 0, 0, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (197, '"Menu"', 'class', 'Palazzo', 'A', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Palazzo"', 0, 1, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (198, '"Menu"', 'class', 'Piano', 'A', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Piano"', 0, 2, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (199, '"Menu"', 'class', 'Periferica', 'A', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Periferica"', 0, 3, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (201, '"Menu"', 'class', 'Posto Di Lavoro', 'A', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"PDL"', 0, 4, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (202, '"Menu"', 'class', 'Stanza', 'A', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Stanza"', 0, 5, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (203, '"Menu"', 'class', 'Ufficio', 'A', NULL, '2009-06-30 17:19:00.486528', NULL, 0, '"Ufficio"', 0, 6, 128, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (238, '"Menu"', 'folder', 'Anagrafiche', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (239, '"Menu"', 'class', 'Dipendente', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 238, '"Dipendente"', 0, 0, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (240, '"Menu"', 'class', 'Ufficio', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 238, '"Ufficio"', 0, 1, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (241, '"Menu"', 'folder', 'Ubicazioni', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 0, NULL, 0, 1, 0, 'folder');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (242, '"Menu"', 'class', 'Palazzo', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 241, '"Palazzo"', 0, 0, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (243, '"Menu"', 'class', 'Piano', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 241, '"Piano"', 0, 1, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (244, '"Menu"', 'class', 'Stanza', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 241, '"Stanza"', 0, 2, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (245, '"Menu"', 'folder', 'Dotazioni', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 0, NULL, 0, 2, 0, 'folder');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (246, '"Menu"', 'class', 'Item', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"Item"', 0, 0, 0, 'superclass');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (247, '"Menu"', 'class', 'Computer', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"Computer"', 0, 1, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (248, '"Menu"', 'class', 'Monitor', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"Monitor"', 0, 2, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (249, '"Menu"', 'class', 'Stampante', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"Stampante"', 0, 3, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (250, '"Menu"', 'class', 'Storage', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"Storage"', 0, 4, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (251, '"Menu"', 'class', 'Periferica', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"Periferica"', 0, 5, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (252, '"Menu"', 'class', 'Software', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"Software"', 0, 6, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (253, '"Menu"', 'class', 'Posto Di Lavoro', 'N', NULL, '2009-06-30 18:41:37.156704', NULL, 245, '"PDL"', 0, 7, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (286, '"Menu"', 'folder', 'Anagrafiche', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (287, '"Menu"', 'class', 'Dipendente', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 286, '"Dipendente"', 0, 0, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (288, '"Menu"', 'class', 'Ufficio', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 286, '"Ufficio"', 0, 1, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (289, '"Menu"', 'folder', 'Ubicazioni', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 0, NULL, 0, 1, 0, 'folder');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (290, '"Menu"', 'class', 'Palazzo', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 289, '"Palazzo"', 0, 0, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (291, '"Menu"', 'class', 'Piano', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 289, '"Piano"', 0, 1, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (292, '"Menu"', 'class', 'Stanza', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 289, '"Stanza"', 0, 2, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (293, '"Menu"', 'folder', 'Dotazioni', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 0, NULL, 0, 2, 0, 'folder');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (294, '"Menu"', 'class', 'Item', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"Item"', 0, 0, 0, 'superclass');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (295, '"Menu"', 'class', 'Computer', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"Computer"', 0, 1, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (296, '"Menu"', 'class', 'Monitor', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"Monitor"', 0, 2, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (297, '"Menu"', 'class', 'Stampante', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"Stampante"', 0, 3, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (298, '"Menu"', 'class', 'Storage', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"Storage"', 0, 4, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (299, '"Menu"', 'class', 'Periferica', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"Periferica"', 0, 5, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (300, '"Menu"', 'class', 'Software', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"Software"', 0, 6, 0, 'class');
INSERT INTO "Menu" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type") VALUES (301, '"Menu"', 'class', 'Posto di lavoro', 'A', NULL, '2009-06-30 18:41:37.156704', NULL, 293, '"PDL"', 0, 7, 0, 'class');



INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (166, '"Menu"', 'class', 'Item', 'U', NULL, '2009-06-30 17:11:14.20773', NULL, 0, '"Item"', 0, 0, 128, 'superclass', 149, '2009-06-30 17:14:37.913963');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (168, '"Menu"', 'class', 'Computer', 'U', NULL, '2009-06-30 17:11:14.20773', NULL, 0, '"Computer"', 0, 1, 128, 'class', 150, '2009-06-30 17:14:37.913963');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (172, '"Menu"', 'class', 'Monitor', 'U', NULL, '2009-06-30 17:11:14.20773', NULL, 0, '"Monitor"', 0, 3, 128, 'class', 152, '2009-06-30 17:14:37.913963');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (174, '"Menu"', 'class', 'Periferica', 'U', NULL, '2009-06-30 17:11:14.20773', NULL, 0, '"Periferica"', 0, 4, 128, 'class', 153, '2009-06-30 17:14:37.913963');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (176, '"Menu"', 'class', 'Software', 'U', NULL, '2009-06-30 17:11:14.20773', NULL, 0, '"Software"', 0, 5, 128, 'class', 154, '2009-06-30 17:14:37.913963');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (178, '"Menu"', 'class', 'Stampante', 'U', NULL, '2009-06-30 17:11:14.20773', NULL, 0, '"Stampante"', 0, 6, 128, 'class', 155, '2009-06-30 17:14:37.913963');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (180, '"Menu"', 'class', 'Storage', 'U', NULL, '2009-06-30 17:11:14.20773', NULL, 0, '"Storage"', 0, 7, 128, 'class', 156, '2009-06-30 17:14:37.913963');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (189, '"Menu"', 'class', 'Palazzo', 'U', NULL, '2009-06-30 17:15:08.717075', NULL, 0, '"Palazzo"', 0, 0, 128, 'class', 182, '2009-06-30 17:19:00.486528');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (191, '"Menu"', 'class', 'Piano', 'U', NULL, '2009-06-30 17:15:08.717075', NULL, 0, '"Piano"', 0, 1, 128, 'class', 183, '2009-06-30 17:19:00.486528');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (193, '"Menu"', 'class', 'Periferica', 'U', NULL, '2009-06-30 17:15:08.717075', NULL, 0, '"Periferica"', 0, 2, 128, 'class', 184, '2009-06-30 17:19:00.486528');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (195, '"Menu"', 'class', 'Posto Di Lavoro', 'U', NULL, '2009-06-30 17:15:08.717075', NULL, 0, '"PDL"', 0, 3, 128, 'class', 185, '2009-06-30 17:19:00.486528');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (255, '"Menu"', 'folder', 'Anagrafiche', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 0, '"Class"', 0, 0, 0, 'folder', 238, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (257, '"Menu"', 'class', 'Dipendente', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 238, '"Dipendente"', 0, 0, 0, 'class', 239, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (259, '"Menu"', 'class', 'Ufficio', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 238, '"Ufficio"', 0, 1, 0, 'class', 240, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (261, '"Menu"', 'folder', 'Ubicazioni', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 0, '"Class"', 0, 1, 0, 'folder', 241, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (263, '"Menu"', 'class', 'Palazzo', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 241, '"Palazzo"', 0, 0, 0, 'class', 242, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (265, '"Menu"', 'class', 'Piano', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 241, '"Piano"', 0, 1, 0, 'class', 243, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (267, '"Menu"', 'class', 'Stanza', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 241, '"Stanza"', 0, 2, 0, 'class', 244, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (269, '"Menu"', 'folder', 'Dotazioni', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 0, '"Class"', 0, 2, 0, 'folder', 245, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (271, '"Menu"', 'class', 'Item', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"Item"', 0, 0, 0, 'superclass', 246, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (273, '"Menu"', 'class', 'Computer', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"Computer"', 0, 1, 0, 'class', 247, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (275, '"Menu"', 'class', 'Monitor', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"Monitor"', 0, 2, 0, 'class', 248, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (277, '"Menu"', 'class', 'Stampante', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"Stampante"', 0, 3, 0, 'class', 249, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (279, '"Menu"', 'class', 'Storage', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"Storage"', 0, 4, 0, 'class', 250, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (281, '"Menu"', 'class', 'Periferica', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"Periferica"', 0, 5, 0, 'class', 251, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (283, '"Menu"', 'class', 'Software', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"Software"', 0, 6, 0, 'class', 252, '2009-06-30 18:41:37.156704');
INSERT INTO "Menu_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "IdParent", "IdElementClass", "IdElementObj", "Number", "IdGroup", "Type", "CurrentId", "EndDate") VALUES (285, '"Menu"', 'class', 'Posto Di Lavoro', 'U', NULL, '2009-06-30 18:37:28.960545', NULL, 245, '"PDL"', 0, 7, 0, 'class', 253, '2009-06-30 18:41:37.156704');









INSERT INTO "Monitor" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione") VALUES (83, '"Monitor"', 'MON0001', 'Acer - AL1716 ', 'A', 'admin', '2009-06-30 16:32:15.896384', NULL, NULL, NULL, '', NULL, 20, NULL, NULL, 45, 'AL1716', 38, 17.00);
INSERT INTO "Monitor" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione") VALUES (87, '"Monitor"', 'MON0002', 'Acer - V193HQb', 'A', 'admin', '2009-06-30 16:34:19.598583', NULL, NULL, NULL, '', NULL, 25, NULL, NULL, 45, 'V193HQb', 38, 18.50);
INSERT INTO "Monitor" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione") VALUES (88, '"Monitor"', 'MON0003', 'Acer - B243WCydr', 'A', 'admin', '2009-06-30 16:36:30.861802', NULL, NULL, NULL, '', NULL, 25, NULL, NULL, 45, 'B243WCydr', 38, 24.00);
INSERT INTO "Monitor" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione") VALUES (89, '"Monitor"', 'MON0004', 'Lg - W1934S-BN', 'A', 'admin', '2009-06-30 16:38:58.762406', NULL, NULL, NULL, '', NULL, NULL, 76, NULL, 48, 'W1934S-BN', 38, 19.00);
INSERT INTO "Monitor" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione") VALUES (92, '"Monitor"', 'MON0005', 'Samsung - Syncmaster 2333', 'A', 'admin', '2009-06-30 16:40:52.65423', NULL, NULL, NULL, '', NULL, 24, 75, NULL, 47, 'Syncmaster 2333', 38, 23.00);
INSERT INTO "Monitor" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione") VALUES (218, '"Monitor"', 'MON0006', 'Packard Bell - Maestro190W', 'A', 'admin', '2009-06-30 18:08:39.357642', NULL, '2008-05-05', '2008-05-05', '', NULL, 24, 75, 116, 52, '', 38, 19.00);
INSERT INTO "Monitor" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione") VALUES (222, '"Monitor"', 'MON0007', 'Hp - v220', 'A', 'admin', '2009-06-30 18:19:27.794392', NULL, NULL, NULL, '', NULL, 24, 75, 116, 50, 'HP v220', 38, 22.00);



INSERT INTO "Monitor_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione", "CurrentId", "EndDate") VALUES (95, '"Monitor"', 'MON005', 'Samsung - Syncmaster 2333', 'U', 'admin', '2009-06-30 16:40:29.249118', NULL, NULL, NULL, '', NULL, NULL, 75, NULL, 47, 'Syncmaster 2333', 38, 23.00, 92, '2009-06-30 16:40:43.31306');
INSERT INTO "Monitor_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Dimensione", "CurrentId", "EndDate") VALUES (97, '"Monitor"', 'MON005', 'Samsung - Syncmaster 2333', 'U', 'admin', '2009-06-30 16:40:43.31306', NULL, NULL, NULL, '', NULL, 24, 75, NULL, 47, 'Syncmaster 2333', 38, 23.00, 92, '2009-06-30 16:40:52.65423');



INSERT INTO "PDL" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Stanza", "Tipo") VALUES (113, '"PDL"', 'Magazzino', 'Magazzino', 'A', 'admin', '2009-06-30 16:55:03.03035', NULL, 82, 93);
INSERT INTO "PDL" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Stanza", "Tipo") VALUES (114, '"PDL"', 'Responsabile Segreteria', 'Responsabile Segreteria', 'A', 'admin', '2009-06-30 16:55:24.066833', NULL, 76, 90);
INSERT INTO "PDL" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Stanza", "Tipo") VALUES (115, '"PDL"', 'Segreteria', 'Segreteria', 'A', 'admin', '2009-06-30 16:56:04.967188', NULL, 76, 91);
INSERT INTO "PDL" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Stanza", "Tipo") VALUES (116, '"PDL"', 'Direttore generale', 'Direttore generale', 'A', 'admin', '2009-06-30 16:57:11.987021', NULL, 75, 90);
INSERT INTO "PDL" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Stanza", "Tipo") VALUES (123, '"PDL"', 'Sala Controllo', 'Sala Controllo Server', 'A', 'admin', '2009-06-30 17:01:25.526583', NULL, 85, 91);






INSERT INTO "Palazzo" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Indirizzo", "CAP", "Comune", "Provincia") VALUES (62, '"Palazzo"', 'Direzione Generale', 'Direzione Generale (Palazzo Vecchio)', 'A', 'admin', '2009-06-30 16:21:40.793902', NULL, 'via Vecchia 15', 33100, 'Udine', 'UD');
INSERT INTO "Palazzo" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Indirizzo", "CAP", "Comune", "Provincia") VALUES (65, '"Palazzo"', 'Data Center', 'Data Center', 'A', 'admin', '2009-06-30 16:21:55.367919', NULL, 'via Nuova 16', 33010, 'Tavagnacco', 'UD');



INSERT INTO "Palazzo_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Indirizzo", "CAP", "Comune", "Provincia", "CurrentId", "EndDate") VALUES (64, '"Palazzo"', 'Direzione Generale', 'Palazzo Vecchio', 'U', 'admin', '2009-06-30 16:13:44.595689', NULL, NULL, 33100, 'Udine', 'UD', 62, '2009-06-30 16:16:50.117652');
INSERT INTO "Palazzo_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Indirizzo", "CAP", "Comune", "Provincia", "CurrentId", "EndDate") VALUES (67, '"Palazzo"', 'Direzione Generale', 'Palazzo Vecchio', 'U', 'admin', '2009-06-30 16:16:50.117652', NULL, 'via Vecchia 15', 33100, 'Udine', 'UD', 62, '2009-06-30 16:18:35.271966');
INSERT INTO "Palazzo_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Indirizzo", "CAP", "Comune", "Provincia", "CurrentId", "EndDate") VALUES (70, '"Palazzo"', 'Direzione Generale', 'Direzione Generale - Palazzo Vecchio', 'U', 'admin', '2009-06-30 16:18:35.271966', NULL, 'via Vecchia 15', 33100, 'Udine', 'UD', 62, '2009-06-30 16:21:40.793902');
INSERT INTO "Palazzo_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Indirizzo", "CAP", "Comune", "Provincia", "CurrentId", "EndDate") VALUES (72, '"Palazzo"', 'Data center', 'Data center', 'U', 'admin', '2009-06-30 16:18:19.080027', NULL, 'via Nuova 16', 33010, 'Tavagnacco', 'UD', 65, '2009-06-30 16:21:55.367919');



INSERT INTO "Patch" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes") VALUES (407, '"Patch"', '1.3.0-01', 'Demo 1.3.0', 'A', 'system', '2011-01-13 15:51:08.761272', NULL);












INSERT INTO "Piano" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Nome", "Palazzo") VALUES (68, '"Piano"', 'DGP1', 'Direzione Generale  - Primo Piano', 'A', 'admin', '2009-06-30 16:21:21.358659', NULL, 'Primo Piano', 62);
INSERT INTO "Piano" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Nome", "Palazzo") VALUES (73, '"Piano"', 'DGP0', 'Direzione Generale - Piano Terra', 'A', 'admin', '2009-06-30 16:22:24.223117', NULL, 'Piano Terra', 62);
INSERT INTO "Piano" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Nome", "Palazzo") VALUES (74, '"Piano"', 'DCPU', 'Data Center - Piano Unico', 'A', 'admin', '2009-06-30 16:23:04.278072', NULL, 'Piano Unico', 65);









INSERT INTO "Role" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Administrator", "startingClass", "Email", "DisabledModules") VALUES (2, '"Role"', 'SuperUser', 'SuperUser', 'A', NULL, '2009-06-30 09:30:48.180508', NULL, true, NULL, NULL, NULL);
INSERT INTO "Role" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Administrator", "startingClass", "Email", "DisabledModules") VALUES (128, '"Role"', 'GestioneAmministrativa', 'GestioneAmministrativa', 'A', 'admin', '2009-06-30 17:04:45.933248', NULL, false, '"PDL"', NULL, NULL);
INSERT INTO "Role" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Administrator", "startingClass", "Email", "DisabledModules") VALUES (127, '"Role"', 'GestioneTecnica', 'GestioneTecnica', 'A', 'admin', '2009-06-30 17:04:21.99404', NULL, false, '"Item"', NULL, NULL);









INSERT INTO "Software" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Versione") VALUES (118, '"Software"', 'SOF0001', 'Windows XP Professional', 'A', 'admin', '2009-06-30 16:59:48.98507', NULL, '2007-03-02', '2007-03-10', '', NULL, 23, NULL, NULL, 110, 'XP Professional - SP2', 26, NULL);
INSERT INTO "Software" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Versione") VALUES (122, '"Software"', 'SOF0002', 'Windows Vista Home Edition', 'A', 'admin', '2009-06-30 17:01:13.43409', NULL, '2008-09-30', '2008-10-11', '', NULL, 23, NULL, NULL, 110, 'Vista Home Edition', 26, NULL);
INSERT INTO "Software" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Versione") VALUES (145, '"Software"', 'SOF0003', 'Windows 2003 Server Edition', 'A', 'admin', '2009-06-30 18:30:18.363745', NULL, '2005-06-14', '2005-06-16', '', NULL, 23, NULL, NULL, 110, 'Windows 2003 Server', 26, NULL);



INSERT INTO "Software_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Versione", "CurrentId", "EndDate") VALUES (231, '"Software"', 'SOF003', 'Windows 2003 Server Edition', 'U', 'admin', '2009-06-30 17:07:48.166416', NULL, '2005-06-14', '2005-06-16', '', NULL, 23, NULL, NULL, 110, 'Windows 2003 Server', 26, NULL, 145, '2009-06-30 18:30:18.363745');



INSERT INTO "Stampante" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Colore", "Fax", "Copiatrice", "Scanner", "FormatoCarta") VALUES (186, '"Stampante"', 'STA0001', 'Epson - ELP 6200L', 'A', 'admin', '2009-06-30 17:16:13.140558', NULL, NULL, NULL, '', NULL, 24, NULL, NULL, 181, 'ELP 6200L', 35, false, false, false, false, 54);
INSERT INTO "Stampante" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Colore", "Fax", "Copiatrice", "Scanner", "FormatoCarta") VALUES (204, '"Stampante"', 'STA0003', 'HP DesignJet Z2100', 'A', 'admin', '2009-06-30 17:28:42.583816', NULL, '2008-12-09', '2008-12-20', '', NULL, 22, 76, 114, 50, 'DesignJet Z2100', 34, true, false, false, false, 54);
INSERT INTO "Stampante" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Colore", "Fax", "Copiatrice", "Scanner", "FormatoCarta") VALUES (200, '"Stampante"', 'STA0002', 'Canon - IX5000', 'A', 'admin', '2009-06-30 17:29:13.81156', NULL, '2009-06-03', '2009-06-10', '', NULL, 25, 76, 115, 187, 'IX5000', 34, true, true, true, true, 53);



INSERT INTO "Stampante_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Tipo", "Colore", "Fax", "Copiatrice", "Scanner", "FormatoCarta", "CurrentId", "EndDate") VALUES (206, '"Stampante"', 'STA0002', 'Canon - IX5000', 'U', 'admin', '2009-06-30 17:19:00.512258', NULL, '2009-06-03', '2009-06-10', '', NULL, 25, NULL, NULL, 187, 'IX5000', 34, true, true, true, true, 53, 200, '2009-06-30 17:29:13.81156');



INSERT INTO "Stanza" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome") VALUES (75, '"Stanza"', 'DG01', 'Direzione Generale - Primo Piano - Ufficio del direttore', 'A', 'admin', '2009-06-30 16:29:25.486964', NULL, 68, 'Ufficio del direttore');
INSERT INTO "Stanza" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome") VALUES (76, '"Stanza"', 'DG02', 'Direzione Generale - Primo Piano - Segreteria', 'A', 'admin', '2009-06-30 16:29:30.973332', NULL, 68, 'Segreteria');
INSERT INTO "Stanza" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome") VALUES (81, '"Stanza"', 'DG03', 'Direzione Generale - Piano Terra - Atrio', 'A', 'admin', '2009-06-30 16:30:29.24521', NULL, 73, 'Atrio');
INSERT INTO "Stanza" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome") VALUES (82, '"Stanza"', 'DG04', 'Direzione Generale - Piano Terra - Magazzino', 'A', 'admin', '2009-06-30 16:31:47.935447', NULL, 73, 'Magazzino');
INSERT INTO "Stanza" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome") VALUES (84, '"Stanza"', 'DC01', 'Data Center - Piano Unico - Sala server', 'A', 'admin', '2009-06-30 16:32:48.764241', NULL, 74, 'Sala server');
INSERT INTO "Stanza" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome") VALUES (85, '"Stanza"', 'DC02', 'Data Center - Piano Unico - Sala controllo', 'A', 'admin', '2009-06-30 16:33:06.611229', NULL, 74, 'Sala controllo');
INSERT INTO "Stanza" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome") VALUES (86, '"Stanza"', 'DC03', 'Data Center - Piano Unico - Sala generatori', 'A', 'admin', '2009-06-30 16:33:41.210728', NULL, 74, 'Sala generatori');



INSERT INTO "Stanza_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome", "CurrentId", "EndDate") VALUES (78, '"Stanza"', 'SN01', 'Direzione Generale - Primo Piano - Ufficio del direttore', 'U', 'admin', '2009-06-30 16:26:57.34133', NULL, 68, 'Ufficio del direttore', 75, '2009-06-30 16:29:25.486964');
INSERT INTO "Stanza_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Piano", "Nome", "CurrentId", "EndDate") VALUES (80, '"Stanza"', 'SN02', 'Direzione Generale - Primo Piano - Segreteria', 'U', 'admin', '2009-06-30 16:27:25.339432', NULL, 68, 'Segreteria', 76, '2009-06-30 16:29:30.973332');



INSERT INTO "Storage" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Dimensione") VALUES (211, '"Storage"', 'STO002', 'Sun - StorageTek 2500', 'A', 'admin', '2009-06-30 17:33:48.431835', NULL, '2008-06-15', '2008-06-20', '', NULL, 24, 84, 123, 208, 'StorageTek 2500', NULL);
INSERT INTO "Storage" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Dimensione") VALUES (207, '"Storage"', 'STO0001', 'Sun - StorageTek 2500', 'A', 'admin', '2009-06-30 17:34:20.111038', NULL, '2008-06-10', '2008-06-14', '', NULL, 24, 84, 123, 208, 'StorageTek 2500', NULL);
INSERT INTO "Storage" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Dimensione") VALUES (214, '"Storage"', 'STO0003', 'HP StorageWorks RDX Removable Disk Backup System', 'A', 'admin', '2009-06-30 17:37:15.538867', NULL, '2007-06-03', '2007-06-05', '', NULL, 24, NULL, NULL, 50, 'HP StorageWorks RDX ', NULL);



INSERT INTO "Storage_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Dimensione", "CurrentId", "EndDate") VALUES (210, '"Storage"', 'STO0001', 'Sun - StorageTek 2500', 'U', 'admin', '2009-06-30 17:31:57.362872', NULL, NULL, NULL, '', NULL, NULL, 84, 123, NULL, 'StorageTek 2500', NULL, 207, '2009-06-30 17:32:37.31368');
INSERT INTO "Storage_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DataAcquisto", "DataCollaudo", "NumeroDiSerie", "CostoFinale", "Stato", "Stanza", "PDL", "Marca", "Modello", "Dimensione", "CurrentId", "EndDate") VALUES (213, '"Storage"', 'STO0001', 'Sun - StorageTek 2500', 'U', 'admin', '2009-06-30 17:32:37.31368', NULL, NULL, NULL, '', NULL, NULL, 84, 123, 208, 'StorageTek 2500', NULL, 207, '2009-06-30 17:34:20.111038');



INSERT INTO "Ufficio" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DescrizioneBreve", "Respnsabile") VALUES (119, '"Ufficio"', 'UFF01', 'Sala Controllo Server', 'A', 'admin', '2009-06-30 18:33:02.312907', NULL, 'Sala Controllo', 117);
INSERT INTO "Ufficio" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DescrizioneBreve", "Respnsabile") VALUES (103, '"Ufficio"', 'UFF02', 'Segreteria', 'A', 'admin', '2009-06-30 18:33:12.364093', NULL, 'Segreteria', 98);
INSERT INTO "Ufficio" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DescrizioneBreve", "Respnsabile") VALUES (102, '"Ufficio"', 'UFF03', 'Ufficio del direttore', 'A', 'admin', '2009-06-30 18:33:20.730084', NULL, 'Ufficio del direttore', 101);



INSERT INTO "Ufficio_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DescrizioneBreve", "Respnsabile", "CurrentId", "EndDate") VALUES (121, '"Ufficio"', '', 'Data Center - Sala Controllo', 'U', 'admin', '2009-06-30 17:00:11.143587', NULL, 'Sala Controllo', 117, 119, '2009-06-30 17:00:49.901623');
INSERT INTO "Ufficio_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DescrizioneBreve", "Respnsabile", "CurrentId", "EndDate") VALUES (233, '"Ufficio"', '', 'Sala Controllo Server', 'U', 'admin', '2009-06-30 17:00:49.901623', NULL, 'Sala Controllo', 117, 119, '2009-06-30 18:33:02.312907');
INSERT INTO "Ufficio_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DescrizioneBreve", "Respnsabile", "CurrentId", "EndDate") VALUES (235, '"Ufficio"', '', 'Segreteria', 'U', 'admin', '2009-06-30 16:52:27.937903', NULL, 'Segreteria', 98, 103, '2009-06-30 18:33:12.364093');
INSERT INTO "Ufficio_history" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "DescrizioneBreve", "Respnsabile", "CurrentId", "EndDate") VALUES (237, '"Ufficio"', '', 'Ufficio del direttore', 'U', 'admin', '2009-06-30 16:52:04.296633', NULL, 'Ufficio del direttore', 101, 102, '2009-06-30 18:33:20.730084');



INSERT INTO "User" ("Id", "IdClass", "Code", "Description", "Status", "User", "BeginDate", "Notes", "Username", "Password", "Email") VALUES (1, '"User"', NULL, 'Administrator', 'A', NULL, '2009-06-30 09:30:48.085066', NULL, 'admin', 'DQdKW32Mlms=', NULL);



ALTER TABLE ONLY "Activity"
    ADD CONSTRAINT "Activity_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Class"
    ADD CONSTRAINT "Class_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Computer_history"
    ADD CONSTRAINT "Computer_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Computer"
    ADD CONSTRAINT "Computer_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Dipendente_history"
    ADD CONSTRAINT "Dipendente_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Dipendente"
    ADD CONSTRAINT "Dipendente_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Email_history"
    ADD CONSTRAINT "Email_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Email"
    ADD CONSTRAINT "Email_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Grant"
    ADD CONSTRAINT "Grant_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Item"
    ADD CONSTRAINT "Item_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "LookUp"
    ADD CONSTRAINT "LookUp_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Map_ActivityEmail_history"
    ADD CONSTRAINT "Map_ActivityEmail_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_ActivityEmail"
    ADD CONSTRAINT "Map_ActivityEmail_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_Appartenenti_history"
    ADD CONSTRAINT "Map_Appartenenti_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_Appartenenti"
    ADD CONSTRAINT "Map_Appartenenti_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_Assegnazione_history"
    ADD CONSTRAINT "Map_Assegnazione_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_Assegnazione"
    ADD CONSTRAINT "Map_Assegnazione_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_ComposizionePDL_history"
    ADD CONSTRAINT "Map_ComposizionePDL_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_ComposizionePDL"
    ADD CONSTRAINT "Map_ComposizionePDL_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_ComputerMonitor_history"
    ADD CONSTRAINT "Map_ComputerMonitor_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_ComputerMonitor"
    ADD CONSTRAINT "Map_ComputerMonitor_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_ItemSoftware_history"
    ADD CONSTRAINT "Map_ItemSoftware_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_ItemSoftware"
    ADD CONSTRAINT "Map_ItemSoftware_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_PianoPalazzo_history"
    ADD CONSTRAINT "Map_PianoPalazzo_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_PianoPalazzo"
    ADD CONSTRAINT "Map_PianoPalazzo_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_PianoStanza_history"
    ADD CONSTRAINT "Map_PianoStanza_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_PianoStanza"
    ADD CONSTRAINT "Map_PianoStanza_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_Responsabile_history"
    ADD CONSTRAINT "Map_Responsabile_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_Responsabile"
    ADD CONSTRAINT "Map_Responsabile_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_StanzaItem_history"
    ADD CONSTRAINT "Map_StanzaItem_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_StanzaItem"
    ADD CONSTRAINT "Map_StanzaItem_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_StanzaPDL_history"
    ADD CONSTRAINT "Map_StanzaPDL_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_StanzaPDL"
    ADD CONSTRAINT "Map_StanzaPDL_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_UserRole_history"
    ADD CONSTRAINT "Map_UserRole_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_UserRole"
    ADD CONSTRAINT "Map_UserRole_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map"
    ADD CONSTRAINT "Map_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2");



ALTER TABLE ONLY "Menu_history"
    ADD CONSTRAINT "Menu_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Menu"
    ADD CONSTRAINT "Menu_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Metadata_history"
    ADD CONSTRAINT "Metadata_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Metadata"
    ADD CONSTRAINT "Metadata_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Monitor_history"
    ADD CONSTRAINT "Monitor_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Monitor"
    ADD CONSTRAINT "Monitor_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "PDL_history"
    ADD CONSTRAINT "PDL_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "PDL"
    ADD CONSTRAINT "PDL_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Palazzo_history"
    ADD CONSTRAINT "Palazzo_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Palazzo"
    ADD CONSTRAINT "Palazzo_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Patch_history"
    ADD CONSTRAINT "Patch_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Patch"
    ADD CONSTRAINT "Patch_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Periferica_history"
    ADD CONSTRAINT "Periferica_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Periferica"
    ADD CONSTRAINT "Periferica_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Piano_history"
    ADD CONSTRAINT "Piano_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Piano"
    ADD CONSTRAINT "Piano_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Report"
    ADD CONSTRAINT "Report_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Role"
    ADD CONSTRAINT "Role_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Scheduler_history"
    ADD CONSTRAINT "Scheduler_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Scheduler"
    ADD CONSTRAINT "Scheduler_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Software_history"
    ADD CONSTRAINT "Software_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Software"
    ADD CONSTRAINT "Software_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Stampante_history"
    ADD CONSTRAINT "Stampante_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Stampante"
    ADD CONSTRAINT "Stampante_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Stanza_history"
    ADD CONSTRAINT "Stanza_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Stanza"
    ADD CONSTRAINT "Stanza_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Storage_history"
    ADD CONSTRAINT "Storage_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Storage"
    ADD CONSTRAINT "Storage_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Ufficio_history"
    ADD CONSTRAINT "Ufficio_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Ufficio"
    ADD CONSTRAINT "Ufficio_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Role"
    ADD CONSTRAINT unique_role_code UNIQUE ("Code");



CREATE UNIQUE INDEX "Report_unique_code" ON "Report" USING btree ((CASE WHEN ((("Code")::text = ''::text) OR (("Status")::text <> 'A'::text)) THEN NULL::text ELSE ("Code")::text END));



CREATE INDEX idx_activity_code ON "Activity" USING btree ("Code");



CREATE INDEX idx_activity_description ON "Activity" USING btree ("Description");



CREATE INDEX idx_activity_idclass ON "Activity" USING btree ("IdClass");



CREATE INDEX idx_class_code ON "Class" USING btree ("Code");



CREATE INDEX idx_class_description ON "Class" USING btree ("Description");



CREATE INDEX idx_computer_code ON "Computer" USING btree ("Code");



CREATE INDEX idx_computer_description ON "Computer" USING btree ("Description");



CREATE INDEX idx_computer_idclass ON "Computer" USING btree ("IdClass");



CREATE INDEX idx_computerhistory_currentid ON "Computer_history" USING btree ("CurrentId");



CREATE INDEX idx_dipendente_code ON "Dipendente" USING btree ("Code");



CREATE INDEX idx_dipendente_description ON "Dipendente" USING btree ("Description");



CREATE INDEX idx_dipendente_idclass ON "Dipendente" USING btree ("IdClass");



CREATE INDEX idx_dipendentehistory_currentid ON "Dipendente_history" USING btree ("CurrentId");



CREATE INDEX idx_email_code ON "Email" USING btree ("Code");



CREATE INDEX idx_email_description ON "Email" USING btree ("Description");



CREATE INDEX idx_email_idclass ON "Email" USING btree ("IdClass");



CREATE INDEX idx_emailhistory_currentid ON "Email_history" USING btree ("CurrentId");



CREATE INDEX idx_idclass_id ON "Class" USING btree ("IdClass", "Id");



CREATE INDEX idx_item_code ON "Item" USING btree ("Code");



CREATE INDEX idx_item_description ON "Item" USING btree ("Description");



CREATE INDEX idx_item_idclass ON "Item" USING btree ("IdClass");



CREATE UNIQUE INDEX idx_map_activityemail_activerows ON "Map_ActivityEmail" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_activityemail_iddomain ON "Map_ActivityEmail" USING btree ("IdDomain");



CREATE INDEX idx_map_activityemail_idobj1 ON "Map_ActivityEmail" USING btree ("IdObj1");



CREATE INDEX idx_map_activityemail_idobj2 ON "Map_ActivityEmail" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_activityemail_uniqueright ON "Map_ActivityEmail" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_appartenenti_activerows ON "Map_Appartenenti" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_appartenenti_iddomain ON "Map_Appartenenti" USING btree ("IdDomain");



CREATE INDEX idx_map_appartenenti_idobj1 ON "Map_Appartenenti" USING btree ("IdObj1");



CREATE INDEX idx_map_appartenenti_idobj2 ON "Map_Appartenenti" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_appartenenti_uniqueright ON "Map_Appartenenti" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_assegnazione_activerows ON "Map_Assegnazione" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_assegnazione_iddomain ON "Map_Assegnazione" USING btree ("IdDomain");



CREATE INDEX idx_map_assegnazione_idobj1 ON "Map_Assegnazione" USING btree ("IdObj1");



CREATE INDEX idx_map_assegnazione_idobj2 ON "Map_Assegnazione" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_composizionepdl_activerows ON "Map_ComposizionePDL" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_composizionepdl_iddomain ON "Map_ComposizionePDL" USING btree ("IdDomain");



CREATE INDEX idx_map_composizionepdl_idobj1 ON "Map_ComposizionePDL" USING btree ("IdObj1");



CREATE INDEX idx_map_composizionepdl_idobj2 ON "Map_ComposizionePDL" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_composizionepdl_uniqueright ON "Map_ComposizionePDL" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_computermonitor_activerows ON "Map_ComputerMonitor" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_computermonitor_iddomain ON "Map_ComputerMonitor" USING btree ("IdDomain");



CREATE INDEX idx_map_computermonitor_idobj1 ON "Map_ComputerMonitor" USING btree ("IdObj1");



CREATE INDEX idx_map_computermonitor_idobj2 ON "Map_ComputerMonitor" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_computermonitor_uniqueright ON "Map_ComputerMonitor" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE INDEX idx_map_iddomain ON "Map" USING btree ("IdDomain");



CREATE INDEX idx_map_idobj1 ON "Map" USING btree ("IdObj1");



CREATE INDEX idx_map_idobj2 ON "Map" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_itemsoftware_activerows ON "Map_ItemSoftware" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_itemsoftware_iddomain ON "Map_ItemSoftware" USING btree ("IdDomain");



CREATE INDEX idx_map_itemsoftware_idobj1 ON "Map_ItemSoftware" USING btree ("IdObj1");



CREATE INDEX idx_map_itemsoftware_idobj2 ON "Map_ItemSoftware" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_pianopalazzo_activerows ON "Map_PianoPalazzo" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_pianopalazzo_iddomain ON "Map_PianoPalazzo" USING btree ("IdDomain");



CREATE INDEX idx_map_pianopalazzo_idobj1 ON "Map_PianoPalazzo" USING btree ("IdObj1");



CREATE INDEX idx_map_pianopalazzo_idobj2 ON "Map_PianoPalazzo" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_pianopalazzo_uniqueleft ON "Map_PianoPalazzo" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass1" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj1" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_pianostanza_activerows ON "Map_PianoStanza" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_pianostanza_iddomain ON "Map_PianoStanza" USING btree ("IdDomain");



CREATE INDEX idx_map_pianostanza_idobj1 ON "Map_PianoStanza" USING btree ("IdObj1");



CREATE INDEX idx_map_pianostanza_idobj2 ON "Map_PianoStanza" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_pianostanza_uniqueright ON "Map_PianoStanza" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_responsabile_activerows ON "Map_Responsabile" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_responsabile_iddomain ON "Map_Responsabile" USING btree ("IdDomain");



CREATE INDEX idx_map_responsabile_idobj1 ON "Map_Responsabile" USING btree ("IdObj1");



CREATE INDEX idx_map_responsabile_idobj2 ON "Map_Responsabile" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_responsabile_uniqueright ON "Map_Responsabile" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_stanzaitem_activerows ON "Map_StanzaItem" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_stanzaitem_iddomain ON "Map_StanzaItem" USING btree ("IdDomain");



CREATE INDEX idx_map_stanzaitem_idobj1 ON "Map_StanzaItem" USING btree ("IdObj1");



CREATE INDEX idx_map_stanzaitem_idobj2 ON "Map_StanzaItem" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_stanzaitem_uniqueright ON "Map_StanzaItem" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_stanzapdl_activerows ON "Map_StanzaPDL" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE INDEX idx_map_stanzapdl_iddomain ON "Map_StanzaPDL" USING btree ("IdDomain");



CREATE INDEX idx_map_stanzapdl_idobj1 ON "Map_StanzaPDL" USING btree ("IdObj1");



CREATE INDEX idx_map_stanzapdl_idobj2 ON "Map_StanzaPDL" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_stanzapdl_uniqueright ON "Map_StanzaPDL" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_userrole_activerows ON "Map_UserRole" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj2" END), (CASE WHEN (("Status")::text = 'N'::text) THEN (NULL::text)::bpchar ELSE "Status" END));



CREATE UNIQUE INDEX idx_map_userrole_defaultgroup ON "Map_UserRole" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN "DefaultGroup" THEN true ELSE NULL::boolean END));



CREATE INDEX idx_map_userrole_iddomain ON "Map_UserRole" USING btree ("IdDomain");



CREATE INDEX idx_map_userrole_idobj1 ON "Map_UserRole" USING btree ("IdObj1");



CREATE INDEX idx_map_userrole_idobj2 ON "Map_UserRole" USING btree ("IdObj2");



CREATE INDEX idx_menu_code ON "Menu" USING btree ("Code");



CREATE INDEX idx_menu_description ON "Menu" USING btree ("Description");



CREATE INDEX idx_menu_idclass ON "Menu" USING btree ("IdClass");



CREATE INDEX idx_metadata_code ON "Metadata" USING btree ("Code");



CREATE INDEX idx_metadata_description ON "Metadata" USING btree ("Description");



CREATE INDEX idx_metadata_idclass ON "Metadata" USING btree ("IdClass");



CREATE INDEX idx_metadatahistory_currentid ON "Metadata_history" USING btree ("CurrentId");



CREATE INDEX idx_monitor_code ON "Monitor" USING btree ("Code");



CREATE INDEX idx_monitor_description ON "Monitor" USING btree ("Description");



CREATE INDEX idx_monitor_idclass ON "Monitor" USING btree ("IdClass");



CREATE INDEX idx_monitorhistory_currentid ON "Monitor_history" USING btree ("CurrentId");



CREATE INDEX idx_palazzo_code ON "Palazzo" USING btree ("Code");



CREATE INDEX idx_palazzo_description ON "Palazzo" USING btree ("Description");



CREATE INDEX idx_palazzo_idclass ON "Palazzo" USING btree ("IdClass");



CREATE INDEX idx_palazzohistory_currentid ON "Palazzo_history" USING btree ("CurrentId");



CREATE INDEX idx_patch_code ON "Patch" USING btree ("Code");



CREATE INDEX idx_patch_description ON "Patch" USING btree ("Description");



CREATE INDEX idx_patch_idclass ON "Patch" USING btree ("IdClass");



CREATE INDEX idx_patchhistory_currentid ON "Patch_history" USING btree ("CurrentId");



CREATE INDEX idx_pdl_code ON "PDL" USING btree ("Code");



CREATE INDEX idx_pdl_description ON "PDL" USING btree ("Description");



CREATE INDEX idx_pdl_idclass ON "PDL" USING btree ("IdClass");



CREATE INDEX idx_pdlhistory_currentid ON "PDL_history" USING btree ("CurrentId");



CREATE INDEX idx_periferica_code ON "Periferica" USING btree ("Code");



CREATE INDEX idx_periferica_description ON "Periferica" USING btree ("Description");



CREATE INDEX idx_periferica_idclass ON "Periferica" USING btree ("IdClass");



CREATE INDEX idx_perifericahistory_currentid ON "Periferica_history" USING btree ("CurrentId");



CREATE INDEX idx_piano_code ON "Piano" USING btree ("Code");



CREATE INDEX idx_piano_description ON "Piano" USING btree ("Description");



CREATE INDEX idx_piano_idclass ON "Piano" USING btree ("IdClass");



CREATE INDEX idx_pianohistory_currentid ON "Piano_history" USING btree ("CurrentId");



CREATE INDEX idx_scheduler_code ON "Scheduler" USING btree ("Code");



CREATE INDEX idx_scheduler_description ON "Scheduler" USING btree ("Description");



CREATE INDEX idx_scheduler_idclass ON "Scheduler" USING btree ("IdClass");



CREATE INDEX idx_schedulerhistory_currentid ON "Scheduler_history" USING btree ("CurrentId");



CREATE INDEX idx_software_code ON "Software" USING btree ("Code");



CREATE INDEX idx_software_description ON "Software" USING btree ("Description");



CREATE INDEX idx_software_idclass ON "Software" USING btree ("IdClass");



CREATE INDEX idx_softwarehistory_currentid ON "Software_history" USING btree ("CurrentId");



CREATE INDEX idx_stampante_code ON "Stampante" USING btree ("Code");



CREATE INDEX idx_stampante_description ON "Stampante" USING btree ("Description");



CREATE INDEX idx_stampante_idclass ON "Stampante" USING btree ("IdClass");



CREATE INDEX idx_stampantehistory_currentid ON "Stampante_history" USING btree ("CurrentId");



CREATE INDEX idx_stanza_code ON "Stanza" USING btree ("Code");



CREATE INDEX idx_stanza_description ON "Stanza" USING btree ("Description");



CREATE INDEX idx_stanza_idclass ON "Stanza" USING btree ("IdClass");



CREATE INDEX idx_stanzahistory_currentid ON "Stanza_history" USING btree ("CurrentId");



CREATE INDEX idx_storage_code ON "Storage" USING btree ("Code");



CREATE INDEX idx_storage_description ON "Storage" USING btree ("Description");



CREATE INDEX idx_storage_idclass ON "Storage" USING btree ("IdClass");



CREATE INDEX idx_storagehistory_currentid ON "Storage_history" USING btree ("CurrentId");



CREATE INDEX idx_ufficio_code ON "Ufficio" USING btree ("Code");



CREATE INDEX idx_ufficio_description ON "Ufficio" USING btree ("Description");



CREATE INDEX idx_ufficio_idclass ON "Ufficio" USING btree ("IdClass");



CREATE INDEX idx_ufficiohistory_currentid ON "Ufficio_history" USING btree ("CurrentId");



CREATE TRIGGER "Dipendente_Ufficio_fkey"
    BEFORE INSERT OR UPDATE ON "Dipendente"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Ufficio', '"Ufficio"', '');



CREATE TRIGGER "Email_Activity_fkey"
    BEFORE INSERT OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Activity', '"Activity"', '');



CREATE TRIGGER "Item_PDL_fkey"
    BEFORE INSERT OR UPDATE ON "Item"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('PDL', '"PDL"', '');



CREATE TRIGGER "Item_PDL_fkey"
    BEFORE INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('PDL', '"PDL"', '');



CREATE TRIGGER "Item_PDL_fkey"
    BEFORE INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('PDL', '"PDL"', '');



CREATE TRIGGER "Item_PDL_fkey"
    BEFORE INSERT OR UPDATE ON "Periferica"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('PDL', '"PDL"', '');



CREATE TRIGGER "Item_PDL_fkey"
    BEFORE INSERT OR UPDATE ON "Software"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('PDL', '"PDL"', '');



CREATE TRIGGER "Item_PDL_fkey"
    BEFORE INSERT OR UPDATE ON "Stampante"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('PDL', '"PDL"', '');



CREATE TRIGGER "Item_PDL_fkey"
    BEFORE INSERT OR UPDATE ON "Storage"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('PDL', '"PDL"', '');



CREATE TRIGGER "Item_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "Item"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "Item_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "Item_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "Item_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "Periferica"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "Item_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "Software"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "Item_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "Stampante"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "Item_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "Storage"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "PDL_Stanza_fkey"
    BEFORE INSERT OR UPDATE ON "PDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Stanza', '"Stanza"', '');



CREATE TRIGGER "Piano_Palazzo_fkey"
    BEFORE INSERT OR UPDATE ON "Piano"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Palazzo', '"Palazzo"', '');



CREATE TRIGGER "Stanza_Piano_fkey"
    BEFORE INSERT OR UPDATE ON "Stanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Piano', '"Piano"', '');



CREATE TRIGGER "Ufficio_Respnsabile_fkey"
    BEFORE INSERT OR UPDATE ON "Ufficio"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Respnsabile', '"Dipendente"', '');



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Menu"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Metadata"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Patch"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Scheduler"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Periferica"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Palazzo"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Software"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Stampante"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Piano"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Stanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "PDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Storage"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Dipendente"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Ufficio"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_Constr_Dipendente_Ufficio"
    BEFORE DELETE OR UPDATE ON "Ufficio"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Dipendente"', 'Ufficio');



CREATE TRIGGER "_Constr_Email_Activity"
    BEFORE DELETE OR UPDATE ON "Activity"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Activity');



CREATE TRIGGER "_Constr_Item_PDL"
    BEFORE DELETE OR UPDATE ON "PDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Item"', 'PDL');



CREATE TRIGGER "_Constr_Item_Stanza"
    BEFORE DELETE OR UPDATE ON "Stanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Item"', 'Stanza');



CREATE TRIGGER "_Constr_PDL_Stanza"
    BEFORE DELETE OR UPDATE ON "Stanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"PDL"', 'Stanza');



CREATE TRIGGER "_Constr_Piano_Palazzo"
    BEFORE DELETE OR UPDATE ON "Palazzo"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Piano"', 'Palazzo');



CREATE TRIGGER "_Constr_Stanza_Piano"
    BEFORE DELETE OR UPDATE ON "Piano"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Stanza"', 'Piano');



CREATE TRIGGER "_Constr_Ufficio_Respnsabile"
    BEFORE DELETE OR UPDATE ON "Dipendente"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Ufficio"', 'Respnsabile');



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Menu"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Metadata"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Patch"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Scheduler"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Periferica"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Palazzo"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Software"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Stampante"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Piano"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Stanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "PDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Storage"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Dipendente"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Ufficio"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_ActivityEmail"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_Appartenenti"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_Assegnazione"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_ComposizionePDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_ComputerMonitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_ItemSoftware"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_PianoPalazzo"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_PianoStanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_Responsabile"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_StanzaItem"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_StanzaPDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_UserRole"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Menu"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Metadata"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Patch"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Scheduler"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Periferica"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Palazzo"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Software"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Stampante"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Piano"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Stanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "PDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Storage"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Dipendente"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Ufficio"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_ActivityEmail"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_Appartenenti"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_Assegnazione"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_ComposizionePDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_ComputerMonitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_ItemSoftware"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_PianoPalazzo"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_PianoStanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_Responsabile"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_StanzaItem"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_StanzaPDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_UserRole"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_UpdRef_Dipendente_Ufficio"
    AFTER INSERT OR UPDATE ON "Map_Appartenenti"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Ufficio', '"Dipendente"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Email_Activity"
    AFTER INSERT OR UPDATE ON "Map_ActivityEmail"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Activity', '"Email"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Item_PDL"
    AFTER INSERT OR UPDATE ON "Map_ComposizionePDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('PDL', '"Item"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Map_StanzaItem"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Stanza', '"Item"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_PDL_Stanza"
    AFTER INSERT OR UPDATE ON "Map_StanzaPDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Stanza', '"PDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Piano_Palazzo"
    AFTER INSERT OR UPDATE ON "Map_PianoPalazzo"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Palazzo', '"Piano"', 'IdObj1', 'IdObj2');



CREATE TRIGGER "_UpdRef_Stanza_Piano"
    AFTER INSERT OR UPDATE ON "Map_PianoStanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Piano', '"Stanza"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Ufficio_Respnsabile"
    AFTER INSERT OR UPDATE ON "Map_Responsabile"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Respnsabile', '"Ufficio"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Dipendente_Ufficio"
    AFTER INSERT OR UPDATE ON "Dipendente"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Ufficio', '"Map_Appartenenti"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Email_Activity"
    AFTER INSERT OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Activity', '"Map_ActivityEmail"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_PDL"
    AFTER INSERT OR UPDATE ON "Item"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('PDL', '"Map_ComposizionePDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_PDL"
    AFTER INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('PDL', '"Map_ComposizionePDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_PDL"
    AFTER INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('PDL', '"Map_ComposizionePDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_PDL"
    AFTER INSERT OR UPDATE ON "Periferica"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('PDL', '"Map_ComposizionePDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_PDL"
    AFTER INSERT OR UPDATE ON "Software"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('PDL', '"Map_ComposizionePDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_PDL"
    AFTER INSERT OR UPDATE ON "Stampante"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('PDL', '"Map_ComposizionePDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_PDL"
    AFTER INSERT OR UPDATE ON "Storage"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('PDL', '"Map_ComposizionePDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Item"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaItem"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaItem"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaItem"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Periferica"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaItem"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Software"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaItem"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Stampante"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaItem"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Item_Stanza"
    AFTER INSERT OR UPDATE ON "Storage"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaItem"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_PDL_Stanza"
    AFTER INSERT OR UPDATE ON "PDL"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Stanza', '"Map_StanzaPDL"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Piano_Palazzo"
    AFTER INSERT OR UPDATE ON "Piano"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Palazzo', '"Map_PianoPalazzo"', 'IdObj1', 'IdObj2');



CREATE TRIGGER "_UpdRel_Stanza_Piano"
    AFTER INSERT OR UPDATE ON "Stanza"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Piano', '"Map_PianoStanza"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Ufficio_Respnsabile"
    AFTER INSERT OR UPDATE ON "Ufficio"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Respnsabile', '"Map_Responsabile"', 'IdObj2', 'IdObj1');



ALTER TABLE ONLY "Computer_history"
    ADD CONSTRAINT "Computer_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Computer"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Dipendente_history"
    ADD CONSTRAINT "Dipendente_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Dipendente"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Email_history"
    ADD CONSTRAINT "Email_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Email"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Menu_history"
    ADD CONSTRAINT "Menu_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Menu"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Metadata_history"
    ADD CONSTRAINT "Metadata_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Metadata"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Monitor_history"
    ADD CONSTRAINT "Monitor_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Monitor"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "PDL_history"
    ADD CONSTRAINT "PDL_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "PDL"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Palazzo_history"
    ADD CONSTRAINT "Palazzo_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Palazzo"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Patch_history"
    ADD CONSTRAINT "Patch_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Patch"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Periferica_history"
    ADD CONSTRAINT "Periferica_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Periferica"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Piano_history"
    ADD CONSTRAINT "Piano_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Piano"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Scheduler_history"
    ADD CONSTRAINT "Scheduler_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Scheduler"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Software_history"
    ADD CONSTRAINT "Software_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Software"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Stampante_history"
    ADD CONSTRAINT "Stampante_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Stampante"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Stanza_history"
    ADD CONSTRAINT "Stanza_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Stanza"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Storage_history"
    ADD CONSTRAINT "Storage_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Storage"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Ufficio_history"
    ADD CONSTRAINT "Ufficio_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Ufficio"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;

