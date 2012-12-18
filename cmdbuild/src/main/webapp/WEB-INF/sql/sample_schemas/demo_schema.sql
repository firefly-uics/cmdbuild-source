
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;


CREATE FUNCTION _cm_add_class_cascade_delete_on_relations_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_class_history_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_class_sanity_check_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_domain_history_trigger(domainid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_domain_sanity_check_trigger(domainid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_fk_constraints(fksourceid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_fk_trigger(tableid oid, fksourceid oid, fkattribute text, fktargetid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_reference_handling(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
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
	RefSourceClassIdAttribute text := _cm_get_ref_source_class_domain_attribute(TableId, AttributeName);
	RefTargetIdAttribute text := _cm_get_ref_target_id_domain_attribute(TableId, AttributeName);

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
			SELECT '|| quote_ident(RefTargetIdAttribute) ||
			' FROM '|| ReferenceDomainId::regclass ||
			' WHERE '|| quote_ident(RefSourceClassIdAttribute) ||'='|| TableId ||
				' AND '|| quote_ident(RefSourceIdAttribute) ||'='|| objid ||
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
$$;



CREATE FUNCTION _cm_add_restrict_trigger(fktargetclassid oid, fkclassid oid, fkattribute text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_simpleclass_sanity_check_trigger(tableid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_spherical_mercator() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	FoundSrid integer;
BEGIN
	SELECT "srid" INTO FoundSrid FROM "spatial_ref_sys" WHERE "srid" = 900913 LIMIT 1;
	IF NOT FOUND THEN
		INSERT INTO "spatial_ref_sys" ("srid","auth_name","auth_srid","srtext","proj4text") VALUES (900913,'spatialreferencing.org',900913,'','+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +units=m +k=1.0 +nadgrids=@null +no_defs');
	END IF;
END;
$$;



CREATE FUNCTION _cm_add_update_reference_trigger(tableid oid, refattribute text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_add_update_relation_trigger(tableid oid, reftableid oid, refattribute text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_attribute_default_to_src(tableid oid, attributename text, newdefault text) RETURNS text
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_attribute_is_empty(tableid oid, attributename text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	Out boolean;
BEGIN
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||
		' WHERE '|| quote_ident(AttributeName) ||' IS NOT NULL' || 
	    ' AND '|| quote_ident(AttributeName) ||'::text <> '''' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$;



CREATE FUNCTION _cm_attribute_is_inherited(tableid oid, attributename text) RETURNS boolean
    LANGUAGE sql
    AS $_$
	SELECT pg_attribute.attinhcount <> 0
	FROM pg_attribute
	WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_attribute_is_local(tableid oid, attributename text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT (attinhcount = 0) FROM pg_attribute WHERE attrelid = $1 AND attname = $2 LIMIT 1;
$_$;



CREATE FUNCTION _cm_attribute_is_notnull(tableid oid, attributename text) RETURNS boolean
    LANGUAGE sql
    AS $_$
SELECT pg_attribute.attnotnull OR c.oid IS NOT NULL
FROM pg_attribute
LEFT JOIN pg_constraint AS c
	ON c.conrelid = pg_attribute.attrelid
	AND c.conname::text = _cm_notnull_constraint_name(pg_attribute.attname::text)
WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_attribute_is_unique(tableid oid, attributename text) RETURNS boolean
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
	IsUnique boolean;
BEGIN
	SELECT INTO IsUnique (count(*) > 0) FROM pg_class
		JOIN pg_index ON pg_class.oid = pg_index.indexrelid
		WHERE pg_index.indrelid = TableId AND relname = _cm_unique_index_name(TableId, AttributeName);
	RETURN IsUnique;
END;
$$;



CREATE FUNCTION _cm_attribute_list(tableid oid) RETURNS SETOF text
    LANGUAGE sql STABLE
    AS $_$
	SELECT attname::text FROM pg_attribute WHERE attrelid = $1 AND attnum > 0 AND atttypid > 0 ORDER BY attnum;
$_$;



CREATE FUNCTION _cm_attribute_list_cs(classid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT array_to_string(array(
		SELECT quote_ident(name) FROM _cm_attribute_list($1) AS name
	),',');
$_$;



CREATE FUNCTION _cm_attribute_notnull_is_check(tableid oid, attributename text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN NOT (
		_cm_is_simpleclass(TableId)
		OR _cm_is_system(TableId)
		OR _cm_check_comment(_cm_comment_for_attribute(TableId, AttributeName), 'MODE', 'reserved')
	);
END
$$;



CREATE FUNCTION _cm_attribute_root_table_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_attribute_set_notnull(tableid oid, attributename text, willbenotnull boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	IF WillBeNotNull = _cm_attribute_is_notnull(TableId, AttributeName) THEN
		RETURN;
	END IF;

    IF WillBeNotNull AND _cm_is_superclass(TableId) AND _cm_check_comment(AttributeComment, 'MODE', 'write')
    THEN
    	RAISE NOTICE 'Non-system superclass attributes cannot be not null';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
    END IF;

	PERFORM _cm_attribute_set_notnull_unsafe(TableId, AttributeName, WillBeNotNull);
END;
$$;



CREATE FUNCTION _cm_attribute_set_notnull_unsafe(tableid oid, attributename text, willbenotnull boolean) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_attribute_set_uniqueness(tableid oid, attributename text, attributeunique boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_attribute_is_unique(TableId, AttributeName) <> AttributeUnique THEN
		IF AttributeUnique AND (_cm_is_simpleclass(TableId) OR _cm_is_superclass(TableId)) AND NOT _cm_is_system(TableId) THEN
			RAISE NOTICE 'User defined superclass or simple class attributes cannot be unique';
			RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
		END IF;

		PERFORM _cm_attribute_set_uniqueness_unsafe(TableId, AttributeName, AttributeUnique);
	END IF;
END;
$$;



CREATE FUNCTION _cm_attribute_set_uniqueness_unsafe(tableid oid, attributename text, attributeunique boolean) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_is_simpleclass(TableId) THEN
		IF AttributeUnique THEN
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' ADD UNIQUE ('|| quote_ident(AttributeName) || ')';
		ELSE
			EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP UNIQUE ('|| quote_ident(AttributeName) || ')';
		END IF;
	ELSE
		IF AttributeUnique THEN
			EXECUTE 'CREATE UNIQUE INDEX '||
				quote_ident(_cm_unique_index_name(TableId, AttributeName)) ||
				' ON '|| TableId::regclass ||' USING btree (('||
				' CASE WHEN "Status"::text = ''N''::text THEN NULL'||
				' ELSE '|| quote_ident(AttributeName) || ' END))';
		ELSE
			EXECUTE 'DROP INDEX '|| _cm_unique_index_id(TableId, AttributeName)::regclass;
		END IF;
	END IF;
END
$$;



CREATE FUNCTION _cm_cascade(id integer, tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE 'DELETE FROM '|| TableId::regclass ||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$;



CREATE FUNCTION _cm_check_attribute_comment_and_type(attributecomment text, sqltype text) RETURNS void
    LANGUAGE plpgsql
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
		RAISE NOTICE 'Too many CMDBuild types specified';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	IF SpecialTypeCount = 1 AND SQLType NOT IN ('int4','integer') THEN
		RAISE NOTICE 'The SQL type does not match the CMDBuild type';
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;
END;
$$;



CREATE FUNCTION _cm_check_comment(classcomment text, key text, value text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT (_cm_read_comment($1, $2) ILIKE $3);
$_$;



CREATE FUNCTION _cm_check_id_exists(id integer, tableid oid, deletedalso boolean) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_value_exists($1, $2, 'Id', $3);
$_$;



CREATE FUNCTION _cm_check_value_exists(id integer, tableid oid, attributename text, deletedalso boolean) RETURNS boolean
    LANGUAGE plpgsql STABLE
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
$$;



CREATE FUNCTION _cm_class_has_children(tableid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT (COUNT(*) > 0) FROM pg_inherits WHERE inhparent = $1 AND _cm_is_cmobject(inhrelid) LIMIT 1;
$_$;



CREATE FUNCTION _cm_class_has_domains(tableid oid) RETURNS boolean
    LANGUAGE sql
    AS $_$
	SELECT (COUNT(*) > 0) FROM _cm_domain_list() AS d
	WHERE _cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS1')) = $1 OR
		_cm_table_id(_cm_read_comment(_cm_comment_for_cmobject(d), 'CLASS2')) = $1;
$_$;



CREATE FUNCTION _cm_class_list() RETURNS SETOF oid
    LANGUAGE sql STABLE
    AS $$
	SELECT oid FROM pg_class WHERE _cm_is_any_class_comment(_cm_comment_for_cmobject(oid));
$$;



CREATE FUNCTION _cm_classfk_name(cmclassname text, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$_$;



CREATE FUNCTION _cm_classfk_name(tableid oid, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_cmtable($1) || '_' || $2 || '_fkey';
$_$;



CREATE FUNCTION _cm_classidx_name(tableid oid, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT 'idx_' || REPLACE(_cm_cmtable_lc($1), '_', '') || '_' || lower($2);
$_$;



CREATE FUNCTION _cm_classpk_name(cmclassname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_cmtable($1) || '_pkey';
$_$;



CREATE FUNCTION _cm_cmschema(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT (_cm_split_cmname($1))[1];
$_$;



CREATE FUNCTION _cm_cmschema(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_namespace.nspname::text FROM pg_class
	JOIN pg_namespace ON pg_class.relnamespace = pg_namespace.oid
	WHERE pg_class.oid=$1
$_$;



CREATE FUNCTION _cm_cmtable(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT (_cm_split_cmname($1))[2];
$_$;



CREATE FUNCTION _cm_cmtable(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_class.relname::text FROM pg_class	WHERE pg_class.oid=$1
$_$;



CREATE FUNCTION _cm_cmtable_lc(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT lower(_cm_cmtable($1));
$_$;



CREATE FUNCTION _cm_cmtable_lc(tableid oid) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT lower(_cm_cmtable($1));
$_$;



CREATE FUNCTION _cm_comment_for_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
SELECT description
FROM pg_description
JOIN pg_attribute ON pg_description.objoid = pg_attribute.attrelid AND pg_description.objsubid = pg_attribute.attnum
WHERE attrelid = $1 and attname = $2 LIMIT 1;
$_$;



CREATE FUNCTION _cm_comment_for_class(cmclass text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_comment_for_table_id(_cm_table_id($1));
$_$;



CREATE FUNCTION _cm_comment_for_cmobject(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT description FROM pg_description
	WHERE objoid = $1 AND objsubid = 0 AND _cm_read_comment(description, 'TYPE') IS NOT NULL LIMIT 1;
$_$;



CREATE FUNCTION _cm_comment_for_domain(cmdomain text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_comment_for_table_id(_cm_domain_id($1));
$_$;



CREATE FUNCTION _cm_comment_for_table_id(tableid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT description FROM pg_description WHERE objoid = $1;
$_$;



CREATE FUNCTION _cm_copy_fk_trigger(fromid oid, toid oid) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '%_fkey');
$_$;



CREATE FUNCTION _cm_copy_restrict_trigger(fromid oid, toid oid) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '_Constr_%');
$_$;



CREATE FUNCTION _cm_copy_superclass_attribute_comments(tableid oid, parenttableid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_copy_trigger(fromid oid, toid oid, triggernamematcher text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_copy_update_relation_trigger(fromid oid, toid oid) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT _cm_copy_trigger($1, $2, '_UpdRel_%');
$_$;



CREATE FUNCTION _cm_create_class_history(cmclassname text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_create_class_indexes(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_create_index(TableId, 'Code');
	PERFORM _cm_create_index(TableId, 'Description');
	PERFORM _cm_create_index(TableId, 'IdClass');
END;
$$;



CREATE FUNCTION _cm_create_class_triggers(tableid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_create_domain_indexes(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    Cardinality text := _cm_domain_cardinality(DomainId);
BEGIN
	PERFORM _cm_create_index(DomainId, 'IdDomain');
	PERFORM _cm_create_index(DomainId, 'IdObj1');
	PERFORM _cm_create_index(DomainId, 'IdObj2');

	EXECUTE 'CREATE UNIQUE INDEX ' || quote_ident(_cm_domainidx_name(DomainId, 'ActiveRows')) ||
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
$$;



CREATE FUNCTION _cm_create_domain_triggers(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_add_domain_sanity_check_trigger(DomainId);
	PERFORM _cm_add_domain_history_trigger(DomainId);
END;
$$;



CREATE FUNCTION _cm_create_index(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_create_schema_if_needed(cmname text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_cmschema(CMName) IS NOT NULL THEN
		EXECUTE 'CREATE SCHEMA '||quote_ident(_cm_cmschema(CMName));
	END IF;
EXCEPTION
	WHEN duplicate_schema THEN
		RETURN;
END;
$$;



CREATE FUNCTION _cm_delete_local_attributes(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	AttributeName text;
BEGIN
	FOR AttributeName IN SELECT _cm_attribute_list(TableId) LOOP
		IF NOT _cm_attribute_is_inherited(TableId, AttributeName) THEN
			PERFORM cm_delete_attribute(TableId, AttributeName);
		END IF;
	END LOOP;
END
$$;



CREATE FUNCTION _cm_delete_relation(username text, domainid oid, cardidcolumn text, cardid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
BEGIN
	EXECUTE 'UPDATE ' || DomainId::regclass ||
		' SET "Status" = ''N'', "User" = ' || coalesce(quote_literal(UserName),'NULL') ||
		' WHERE "Status" = ''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId;
END;
$$;



CREATE FUNCTION _cm_dest_classid_for_domain_attribute(domainid oid, attributename text) RETURNS oid
    LANGUAGE sql STABLE STRICT
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
$_$;



CREATE FUNCTION _cm_dest_reference_classid(domainid oid, refidcolumn text, refid integer) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_subclassid(_cm_dest_classid_for_domain_attribute($1, $2), $3)
$_$;



CREATE FUNCTION _cm_disable_triggers_recursively(superclass regclass) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::regclass ||' DISABLE TRIGGER USER';
	END LOOP;
END;
$_$;



CREATE FUNCTION _cm_domain_cardinality(domainid oid) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_read_domain_cardinality(_cm_comment_for_table_id($1));
$_$;



CREATE FUNCTION _cm_domain_cmname(cmdomain text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT coalesce(_cm_cmschema($1)||'.','')||coalesce('Map_'||_cm_cmtable($1),'Map');
$_$;



CREATE FUNCTION _cm_domain_cmname_lc(cmdomainname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT lower(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_dbname(cmdomain text) RETURNS regclass
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_dbname_unsafe(cmdomain text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_direction(domainid oid) RETURNS boolean
    LANGUAGE plpgsql STABLE STRICT
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
$$;



CREATE FUNCTION _cm_domain_id(cmdomain text) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_table_id(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_domain_list() RETURNS SETOF oid
    LANGUAGE sql STABLE
    AS $$
	SELECT oid FROM pg_class WHERE _cm_is_domain_comment(_cm_comment_for_cmobject(oid));
$$;



CREATE FUNCTION _cm_domainidx_name(domainid oid, type text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT 'idx_' || _cm_cmtable_lc($1) || '_' || lower($2);
$_$;



CREATE FUNCTION _cm_domainpk_name(cmdomainname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_classpk_name(_cm_domain_cmname($1));
$_$;



CREATE FUNCTION _cm_drop_triggers_recursively(tableid oid, triggername text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'DROP TRIGGER IF EXISTS '|| quote_ident(TriggerName) ||' ON '|| SubClassId::regclass;
	END LOOP;
END;
$$;



CREATE FUNCTION _cm_enable_triggers_recursively(superclass regclass) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE
	CurrentClass regclass := $1;
BEGIN
	FOR CurrentClass IN SELECT _cm_subtables_and_itself(SuperClass) LOOP
		EXECUTE 'ALTER TABLE '|| CurrentClass::text ||' ENABLE TRIGGER USER';
	END LOOP;
END;
$_$;



CREATE FUNCTION _cm_function_list(OUT function_name text, OUT function_id oid, OUT arg_io character[], OUT arg_names text[], OUT arg_types text[], OUT returns_set boolean) RETURNS SETOF record
    LANGUAGE plpgsql STABLE
    AS $_$
DECLARE
	R record;
	i integer;
BEGIN
	FOR R IN
		SELECT *
		FROM pg_proc
		WHERE _cm_comment_for_cmobject(oid) IS NOT NULL
	LOOP
		function_name := R.proname::text;
		function_id := R.oid;
		returns_set := R.proretset;
		IF R.proargmodes IS NULL
		THEN
			arg_io := '{}'::char[];
			arg_types := '{}'::text[];
			arg_names := '{}'::text[];
			-- add input columns
			FOR i IN SELECT generate_series(1, array_upper(R.proargtypes,1)) LOOP
				arg_io := arg_io || 'i'::char;
				arg_types := arg_types || _cm_get_sqltype_string(R.proargtypes[i], NULL);
				arg_names := arg_names || COALESCE(R.proargnames[i], '$'||i);
			END LOOP;
			-- add single output column
			arg_io := arg_io || 'o'::char;
			arg_types := arg_types || _cm_get_sqltype_string(R.prorettype, NULL);
			arg_names := arg_names || function_name;
		ELSE
			-- just normalize existing columns
			arg_io := R.proargmodes;
			arg_types := '{}'::text[];
			arg_names := R.proargnames;
			FOR i IN SELECT generate_series(1, array_upper(arg_io,1)) LOOP
				-- normalize table output
				IF arg_io[i] = 't' THEN
					arg_io[i] := 'o';
				ELSIF arg_io[i] = 'b' THEN
					arg_io[i] := 'io';
				END IF;
				arg_types := arg_types || _cm_get_sqltype_string(R.proallargtypes[i], NULL);
				IF arg_names[i] = '' THEN
					IF arg_io[i] = 'i' THEN
						arg_names[i] = '$'||i;
					ELSE
						arg_names[i] = 'column'||i;
					END IF;
				END IF;
			END LOOP;
		END IF;
		RETURN NEXT;
	END LOOP;

	RETURN;
END
$_$;



CREATE FUNCTION _cm_get_attribute_default(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_attrdef.adsrc
		FROM pg_attribute JOIN pg_attrdef ON pg_attrdef.adrelid = pg_attribute.attrelid AND pg_attrdef.adnum = pg_attribute.attnum
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_get_attribute_sqltype(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_get_sqltype_string(pg_attribute.atttypid, pg_attribute.atttypmod)
		FROM pg_attribute
		WHERE pg_attribute.attrelid = $1 AND pg_attribute.attname = $2;
$_$;



CREATE FUNCTION _cm_get_domain_reference_target_comment(domaincomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT CASE _cm_read_domain_cardinality($1)
		WHEN '1:N' THEN _cm_read_comment($1, 'CLASS1')
		WHEN 'N:1' THEN _cm_read_comment($1, 'CLASS2')
		ELSE NULL
	END
$_$;



CREATE FUNCTION _cm_get_fk_target(tableid oid, attributename text) RETURNS text
    LANGUAGE plpgsql STABLE STRICT
    AS $$
DECLARE
	AttributeComment text := _cm_comment_for_attribute(TableId, AttributeName);
BEGIN
	RETURN COALESCE(
		_cm_get_fk_target_comment(AttributeComment),
		_cm_read_reference_target_comment(AttributeComment)
	);
END
$$;



CREATE FUNCTION _cm_get_fk_target_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_read_comment($1, 'FKTARGETCLASS');
$_$;



CREATE FUNCTION _cm_get_fk_target_table_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE plpgsql STABLE STRICT
    AS $_$ BEGIN
	RETURN _cm_table_id(_cm_get_fk_target($1, $2));
END $_$;



CREATE FUNCTION _cm_get_geometry_type(tableid oid, attribute text) RETURNS text
    LANGUAGE plpgsql STABLE
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
$_$;



CREATE FUNCTION _cm_get_lookup_type_comment(attributecomment text) RETURNS text
    LANGUAGE sql
    AS $_$
	SELECT _cm_read_comment($1, 'LOOKUP');
$_$;



CREATE FUNCTION _cm_get_ref_source_class_domain_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdClass1'
		WHEN FALSE THEN 'IdClass2'
		ELSE NULL
	END;
$_$;



CREATE FUNCTION _cm_get_ref_source_id_domain_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj1'
		WHEN FALSE THEN 'IdObj2'
		ELSE NULL
	END;
$_$;



CREATE FUNCTION _cm_get_ref_target_id_domain_attribute(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT CASE _cm_domain_direction(_cm_get_reference_domain_id($1, $2))
		WHEN TRUE THEN 'IdObj2'
		WHEN FALSE THEN 'IdObj1'
		ELSE NULL
	END;
$_$;



CREATE FUNCTION _cm_get_reference_domain_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_read_reference_domain_id_comment(_cm_comment_for_attribute($1, $2));
$_$;



CREATE FUNCTION _cm_get_sqltype_string(sqltypeid oid, typemod integer) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT pg_type.typname::text || COALESCE(
			CASE
				WHEN pg_type.typname IN ('varchar','bpchar') THEN '(' || $2 - 4 || ')'
				WHEN pg_type.typname = 'numeric' THEN '(' ||
					$2 / 65536 || ',' ||
					$2 - $2 / 65536 * 65536 - 4|| ')'
			END, '')
		FROM pg_type WHERE pg_type.oid = $1;
$_$;



CREATE FUNCTION _cm_get_type_comment(classcomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_read_comment($1, 'TYPE');
$_$;



CREATE FUNCTION _cm_history_cmname(cmclass text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT $1 || '_history';
$_$;



CREATE FUNCTION _cm_history_dbname(cmtable text) RETURNS regclass
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_history_dbname_unsafe(cmtable text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_history_id(cmtable text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_id(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_historyfk_name(cmclassname text, attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_classfk_name(_cm_history_cmname($1), $2);
$_$;



CREATE FUNCTION _cm_historypk_name(cmclassname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT _cm_classpk_name(_cm_history_cmname($1));
$_$;



CREATE FUNCTION _cm_insert_relation(username text, domainid oid, cardidcolumn text, cardid integer, refidcolumn text, refid integer, cardclassid oid) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_is_active_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'STATUS', 'active');
$_$;



CREATE FUNCTION _cm_is_any_class(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_any_class_comment(_cm_comment_for_table_id($1))
$_$;



CREATE FUNCTION _cm_is_any_class_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', '%class');
$_$;



CREATE FUNCTION _cm_is_cmobject(tableid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_comment_for_cmobject($1) IS NOT NULL;
$_$;



CREATE FUNCTION _cm_is_domain_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', 'domain');
$_$;



CREATE FUNCTION _cm_is_geometry_type(cmattributetype text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT $1 IN ('POINT','LINESTRING','POLYGON');
$_$;



CREATE FUNCTION _cm_is_process(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT $1 IN (SELECT _cm_subtables_and_itself(_cm_table_id('Activity')));
$_$;



CREATE FUNCTION _cm_is_process(cmclass text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_process(_cm_table_id($1));
$_$;



CREATE FUNCTION _cm_is_reference_comment(attributecomment text) RETURNS boolean
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT COALESCE(_cm_read_reference_domain_comment($1),'') != '';
$_$;



CREATE FUNCTION _cm_is_simpleclass(cmclass text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_class($1));
$_$;



CREATE FUNCTION _cm_is_simpleclass(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_simpleclass_comment(_cm_comment_for_table_id($1))
$_$;



CREATE FUNCTION _cm_is_simpleclass_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'TYPE', 'simpleclass');
$_$;



CREATE FUNCTION _cm_is_superclass(cmclass text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_superclass_comment(_cm_comment_for_class($1));
$_$;



CREATE FUNCTION _cm_is_superclass(classid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_is_superclass_comment(_cm_comment_for_table_id($1));
$_$;



CREATE FUNCTION _cm_is_superclass_comment(classcomment text) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment($1, 'SUPERCLASS', 'true');
$_$;



CREATE FUNCTION _cm_is_system(tableid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_check_comment(_cm_comment_for_table_id($1), 'MODE', 'reserved')
$_$;



CREATE FUNCTION _cm_join_cmname(cmschema name, cmtable name) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT $1 || '.' || $2;
$_$;



CREATE FUNCTION _cm_legacy_class_is_process(text) RETURNS boolean
    LANGUAGE sql
    AS $_$
	SELECT (_cm_legacy_read_comment($1, 'MANAGER') = 'activity');
$_$;



CREATE FUNCTION _cm_legacy_get_menu_code(boolean, boolean, boolean, boolean) RETURNS character varying
    LANGUAGE plpgsql
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
$_$;



CREATE FUNCTION _cm_legacy_get_menu_type(boolean, boolean, boolean, boolean) RETURNS character varying
    LANGUAGE plpgsql
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
$_$;



CREATE FUNCTION _cm_legacy_read_comment(text, text) RETURNS character varying
    LANGUAGE sql STABLE
    AS $_$
	SELECT COALESCE(_cm_read_comment($1, $2), '');
$_$;



CREATE FUNCTION _cm_new_card_id() RETURNS integer
    LANGUAGE sql
    AS $$
	SELECT nextval(('class_seq'::text)::regclass)::integer;
$$;



CREATE FUNCTION _cm_notnull_constraint_name(attributename text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT '_NotNull_'||$1;
$_$;



CREATE FUNCTION _cm_parent_id(tableid oid) RETURNS SETOF oid
    LANGUAGE sql
    AS $_$
	SELECT COALESCE((SELECT inhparent FROM pg_inherits WHERE inhrelid = $1 AND _cm_is_cmobject(inhparent) LIMIT 1), NULL);
$_$;



CREATE FUNCTION _cm_propagate_superclass_triggers(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	ParentId oid := _cm_parent_id(TableId);
BEGIN
	PERFORM _cm_copy_restrict_trigger(ParentId, TableId);
	PERFORM _cm_copy_update_relation_trigger(ParentId, TableId);
	PERFORM _cm_copy_fk_trigger(ParentId, TableId);
END
$$;



CREATE FUNCTION _cm_read_comment(comment text, key text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT TRIM(SUBSTRING($1 FROM E'(?:^|\\|)'||$2||E':[ ]*([^\\|]+)'));
$_$;



CREATE FUNCTION _cm_read_domain_cardinality(attributecomment text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_read_comment($1, 'CARDIN');
$_$;



CREATE FUNCTION _cm_read_reference_domain_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_read_comment($1, 'REFERENCEDOM');
$_$;



CREATE FUNCTION _cm_read_reference_domain_id_comment(attributecomment text) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_domain_id(_cm_read_reference_domain_comment($1));
$_$;



CREATE FUNCTION _cm_read_reference_target_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_get_domain_reference_target_comment(_cm_comment_for_domain(_cm_read_reference_domain_comment($1)));
$_$;



CREATE FUNCTION _cm_read_reference_target_id_comment(attributecomment text) RETURNS oid
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT _cm_table_id(_cm_read_reference_target_comment($1));
$_$;



CREATE FUNCTION _cm_read_reference_type_comment(attributecomment text) RETURNS text
    LANGUAGE sql STABLE STRICT
    AS $_$
	SELECT COALESCE(_cm_read_comment($1, 'REFERENCETYPE'),'restrict');
$_$;



CREATE FUNCTION _cm_remove_attribute_triggers(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM _cm_remove_fk_constraints(TableId, AttributeName);
	PERFORM _cm_remove_reference_handling(TableId, AttributeName);
END;
$$;



CREATE FUNCTION _cm_remove_constraint_trigger(fktargetclassid oid, fkclassid oid, fkattribute text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE '
		DROP TRIGGER ' || quote_ident('_Constr_'||_cm_cmtable(FKClassId)||'_'||FKAttribute) ||
			' ON ' || FKTargetClassId::regclass || ';
	';
END;
$$;



CREATE FUNCTION _cm_remove_fk_constraints(fksourceid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_remove_reference_handling(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_restrict(id integer, tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $_$
BEGIN
	IF _cm_check_value_exists($1, $2, $3, FALSE) THEN
		RAISE EXCEPTION 'CM_RESTRICT_VIOLATION';
	END IF;
END;
$_$;



CREATE FUNCTION _cm_set_attribute_comment(tableid oid, attributename text, comment text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	SubClassId oid;
BEGIN
	FOR SubClassId IN SELECT _cm_subtables_and_itself(TableId) LOOP
		EXECUTE 'COMMENT ON COLUMN '|| SubClassId::regclass ||'.'|| quote_ident(AttributeName) ||' IS '|| quote_literal(Comment);
	END LOOP;
END;
$$;



CREATE FUNCTION _cm_set_attribute_default(tableid oid, attributename text, newdefault text, updateexisting boolean) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_setnull(id integer, tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE 'UPDATE '|| TableId::regclass ||
		' SET '||quote_ident(AttributeName)||' = NULL'||
		' WHERE '||quote_ident(AttributeName)||' = '||Id::text;
END;
$$;



CREATE FUNCTION _cm_split_cmname(cmname text) RETURNS text[]
    LANGUAGE sql IMMUTABLE
    AS $_$
    SELECT regexp_matches($1,E'(?:([^\\.]+)\\.)?([^\\.]+)?');
$_$;



CREATE FUNCTION _cm_subclassid(superclassid oid, cardid integer) RETURNS oid
    LANGUAGE plpgsql STABLE STRICT
    AS $$
DECLARE
	Out integer;
BEGIN
	EXECUTE 'SELECT tableoid FROM '||SuperClassId::regclass||' WHERE "Id"='||CardId||' LIMIT 1' INTO Out;
	RETURN Out;
END;
$$;



CREATE FUNCTION _cm_subtables_and_itself(tableid oid) RETURNS SETOF oid
    LANGUAGE sql
    AS $_$
	SELECT $1 WHERE _cm_is_cmobject($1)
	UNION
	SELECT _cm_subtables_and_itself(inhrelid) FROM pg_inherits WHERE inhparent = $1
$_$;



CREATE FUNCTION _cm_table_dbname(cmname text) RETURNS regclass
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe($1)::regclass;
$_$;



CREATE FUNCTION _cm_table_dbname_unsafe(cmname text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT coalesce(quote_ident(_cm_cmschema($1))||'.','')||quote_ident(_cm_cmtable($1));
$_$;



CREATE FUNCTION _cm_table_id(cmname text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT _cm_table_dbname_unsafe($1)::regclass::oid;
$_$;



CREATE FUNCTION _cm_table_is_empty(tableid oid) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
	NotFound boolean;
BEGIN
	-- Note: FOUND variable is not set on EXECUTE, so we can't use it!
	EXECUTE 'SELECT (COUNT(*) = 0) FROM '|| TableId::regclass ||' LIMIT 1' INTO NotFound;
	RETURN NotFound;
END;
$$;



CREATE FUNCTION _cm_trigger_cascade_delete_on_relations() RETURNS trigger
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_trigger_create_card_history_row() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;



CREATE FUNCTION _cm_trigger_create_relation_history_row() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
	-- Does not create the row on logic deletion
	IF (TG_OP='UPDATE') THEN
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
$$;



CREATE FUNCTION _cm_trigger_fk() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;



CREATE FUNCTION _cm_trigger_restrict() RETURNS trigger
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION _cm_trigger_row_or_statement(tgtype smallint) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT CASE $1 & cast(1 as int2)
         WHEN 0 THEN 'STATEMENT'
         ELSE 'ROW'
       END;
$_$;



CREATE FUNCTION _cm_trigger_sanity_check() RETURNS trigger
    LANGUAGE plpgsql
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
		-- Class ID is needed because of the history tables
		BEGIN
			NEW."IdClass" = TG_RELID;
		EXCEPTION WHEN undefined_column THEN
			NEW."IdDomain" = TG_RELID;
		END;
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
$$;



CREATE FUNCTION _cm_trigger_sanity_check_simple() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
	END IF;
	RETURN NEW;
END;
$$;



CREATE FUNCTION _cm_trigger_update_reference() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;



CREATE FUNCTION _cm_trigger_update_relation() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;



CREATE FUNCTION _cm_trigger_when(tgtype smallint) RETURNS text
    LANGUAGE sql IMMUTABLE
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
$_$;



CREATE FUNCTION _cm_unique_index_id(tableid oid, attributename text) RETURNS oid
    LANGUAGE sql STABLE
    AS $_$
	SELECT (
		quote_ident(_cm_cmschema($1))
		||'.'||
		quote_ident(_cm_unique_index_name($1, $2))
	)::regclass::oid;
$_$;



CREATE FUNCTION _cm_unique_index_name(tableid oid, attributename text) RETURNS text
    LANGUAGE sql STABLE
    AS $_$
	SELECT '_Unique_'|| _cm_cmtable($1) ||'_'|| $2;
$_$;



CREATE FUNCTION _cm_update_reference(tableid oid, attributename text, cardid integer, referenceid integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	EXECUTE 'UPDATE ' || TableId::regclass ||
		' SET ' || quote_ident(AttributeName) || ' = ' || coalesce(ReferenceId::text, 'NULL') ||
		' WHERE "Status"=''A'' AND "Id" = ' || CardId::text ||
		' AND coalesce(' || quote_ident(AttributeName) || ', 0) <> ' || coalesce(ReferenceId, 0)::text;
END;
$$;



CREATE FUNCTION _cm_update_reference_trigger_name(reftableid oid, refattribute text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT '_UpdRef_'|| _cm_cmtable($1) ||'_'|| $2;
$_$;



CREATE FUNCTION _cm_update_relation(username text, domainid oid, cardidcolumn text, cardid integer, refidcolumn text, refid integer) RETURNS void
    LANGUAGE plpgsql
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
		' WHERE "Status"=''A'' AND ' || quote_ident(CardIdColumn) || ' = ' || CardId ||
			' AND ' || quote_ident(RefIdColumn) || ' <> ' || RefId;
END;
$$;



CREATE FUNCTION _cm_update_relation_trigger_name(reftableid oid, refattribute text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
	SELECT '_UpdRel_'|| _cm_cmtable($1) ||'_'|| $2;
$_$;



CREATE FUNCTION _cm_zero_rownum_sequence() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	temp BIGINT;
BEGIN
	SELECT INTO temp setval('rownum', 0, true);
EXCEPTION WHEN undefined_table THEN
	CREATE TEMPORARY SEQUENCE rownum MINVALUE 0 START 1;
END
$$;



CREATE FUNCTION _cmf_class_description(cid oid) RETURNS character varying
    LANGUAGE sql STABLE
    AS $_$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'DESCR');
$_$;



CREATE FUNCTION _cmf_is_displayable(cid oid) RETURNS boolean
    LANGUAGE sql STABLE
    AS $_$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'MODE') IN
('write','read','baseclass');
$_$;



CREATE FUNCTION cm_create_attribute(tableid oid, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION cm_create_class(cmclass text, parentid oid, classcomment text) RETURNS integer
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION cm_create_class(cmclass text, cmparentclass text, classcomment text) RETURNS integer
    LANGUAGE sql
    AS $_$
	SELECT cm_create_class($1, _cm_table_id($2), $3);
$_$;



CREATE FUNCTION cm_create_class_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_create_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cm_create_domain(cmdomain text, domaincomment text) RETURNS integer
    LANGUAGE plpgsql
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
	PERFORM _cm_copy_superclass_attribute_comments(DomainId, '"Map"'::regclass);

	EXECUTE 'CREATE TABLE '|| HistoryDBName ||
		' ( CONSTRAINT '|| quote_ident(_cm_historypk_name(_cm_domain_cmname(CMDomain))) ||
		' PRIMARY KEY ("IdDomain","IdClass1", "IdObj1", "IdClass2", "IdObj2","EndDate"))'||
		' INHERITS ('|| DomainId::regclass ||')';
	EXECUTE 'ALTER TABLE '|| HistoryDBName ||' ALTER COLUMN "EndDate" SET DEFAULT now()';

	PERFORM _cm_create_domain_indexes(DomainId);

	PERFORM _cm_create_domain_triggers(DomainId);

	RETURN DomainId;
END
$$;



CREATE FUNCTION cm_create_domain_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_create_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cm_delete_attribute(tableid oid, attributename text) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
	GeoType text := _cm_get_geometry_type(TableId, AttributeName);
BEGIN
	IF NOT _cm_attribute_is_local(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

    IF NOT _cm_attribute_is_empty(TableId, AttributeName) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_remove_attribute_triggers(TableId, AttributeName);

	IF GeoType IS NOT NULL THEN
		PERFORM DropGeometryColumn(_cm_cmschema(TableId), _cm_cmtable(TableId), AttributeName);
	ELSE
		EXECUTE 'ALTER TABLE '|| TableId::regclass ||' DROP COLUMN '|| quote_ident(AttributeName) ||' CASCADE';
	END IF;
END;
$$;



CREATE FUNCTION cm_delete_class(tableid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF _cm_class_has_domains(TableId) THEN
		RAISE EXCEPTION 'CM_HAS_DOMAINS';
	ELSEIF _cm_class_has_children(TableId) THEN
		RAISE EXCEPTION 'CM_HAS_CHILDREN';
	ELSEIF NOT _cm_table_is_empty(TableId) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_delete_local_attributes(TableId);

	-- Cascade for the history table
	EXECUTE 'DROP TABLE '|| TableId::regclass ||' CASCADE';
END;
$$;



CREATE FUNCTION cm_delete_class(cmclass text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_class(_cm_table_id($1));
$_$;



CREATE FUNCTION cm_delete_class_attribute(cmclass text, attributename text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_attribute(_cm_table_id($1), $2);
$_$;



CREATE FUNCTION cm_delete_domain(domainid oid) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
	IF NOT _cm_table_is_empty(DomainId) THEN
		RAISE EXCEPTION 'CM_CONTAINS_DATA';
	END IF;

	PERFORM _cm_delete_local_attributes(DomainId);

	EXECUTE 'DROP TABLE '|| DomainId::regclass ||' CASCADE';
END
$$;



CREATE FUNCTION cm_delete_domain(cmdomain text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_domain(_cm_domain_id($1));
$_$;



CREATE FUNCTION cm_delete_domain_attribute(cmclass text, attributename text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_attribute(_cm_domain_id($1), $2);
$_$;



CREATE FUNCTION cm_modify_attribute(tableid oid, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, newcomment text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION cm_modify_class(tableid oid, newcomment text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION cm_modify_class(cmclass text, newcomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_class(_cm_table_id($1), $2);
$_$;



CREATE FUNCTION cm_modify_class_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_attribute(_cm_table_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cm_modify_domain(domainid oid, newcomment text) RETURNS void
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION cm_modify_domain(cmdomain text, domaincomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_domain(_cm_domain_id($1), $2);
$_$;



CREATE FUNCTION cm_modify_domain_attribute(cmclass text, attributename text, sqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_modify_attribute(_cm_domain_id($1), $2, $3, $4, $5, $6, $7);
$_$;



CREATE FUNCTION cmf_active_asset_for_brand(OUT "Brand" character varying, OUT "Number" integer) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT coalesce("LookUp"."Description", ''N.D.'')::character varying AS "Brand", COUNT(*)::integer AS "CardCount"' ||
        '    FROM "Asset" ' ||
				'    LEFT OUTER JOIN "LookUp" on "LookUp"."Id" = "Asset"."Brand" and "LookUp"."Status" = ''A'' ' ||
        '    WHERE "Asset"."Status" = ''A'' ' ||
        '    GROUP BY "LookUp"."Description"' ||
        '    ORDER BY case when coalesce("LookUp"."Description", ''N.D.'') = ''N.D.'' then ''zz'' else "LookUp"."Description" end';
END
$$;



COMMENT ON FUNCTION cmf_active_asset_for_brand(OUT "Brand" character varying, OUT "Number" integer) IS 'TYPE: function';



CREATE FUNCTION cmf_active_cards_for_class("ClassName" character varying, OUT "Class" character varying, OUT "Number" integer) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $_$
BEGIN
RETURN QUERY EXECUTE
'SELECT _cmf_class_description("IdClass") AS "ClassDescription", COUNT(*)::integer
AS "CardCount"' ||
' FROM ' || quote_ident($1) ||
' WHERE "Status" = ' || quote_literal('A') ||
' AND _cmf_is_displayable("IdClass")' ||
' AND "IdClass" not IN (SELECT _cm_subtables_and_itself(_cm_table_id(' ||
quote_literal('Activity') || ')))'
' GROUP BY "IdClass"' ||
' ORDER BY "ClassDescription"';
END
$_$;



COMMENT ON FUNCTION cmf_active_cards_for_class("ClassName" character varying, OUT "Class" character varying, OUT "Number" integer) IS 'TYPE: function';



CREATE FUNCTION cmf_count_active_cards("ClassName" character varying, OUT "Count" integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
EXECUTE 'SELECT count(*) FROM ' || quote_ident("ClassName") || ' WHERE "Status" = ' ||
quote_literal('A') INTO "Count";
END
$$;



COMMENT ON FUNCTION cmf_count_active_cards("ClassName" character varying, OUT "Count" integer) IS 'TYPE: function';



CREATE FUNCTION cmf_open_rfc_for_status(OUT "Status" character varying, OUT "Number" integer) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT coalesce("LookUp"."Description", ''N.D.'')::character varying AS "Status", COUNT(*)::integer AS "CardCount"' ||
        '    FROM "RequestForChange" ' ||
				'    LEFT OUTER JOIN "LookUp" on "LookUp"."Id" = "RequestForChange"."RFCStatus" and "LookUp"."Status" = ''A'' ' ||
        '    WHERE "RequestForChange"."Status" = ''A'' ' ||
        '    GROUP BY "LookUp"."Description"' ||
        '    ORDER BY case when coalesce("LookUp"."Description", ''N.D.'') = ''N.D.'' then ''zz'' else "LookUp"."Description" end';
END
$$;



COMMENT ON FUNCTION cmf_open_rfc_for_status(OUT "Status" character varying, OUT "Number" integer) IS 'TYPE: function';



CREATE FUNCTION "cmwf_getRFCNumber"(OUT "RFCNumber" integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	select into "RFCNumber" coalesce(max("RequestNumber")+1,0) from "RequestForChange" where "Status"='A';
END
$$;



COMMENT ON FUNCTION "cmwf_getRFCNumber"(OUT "RFCNumber" integer) IS 'TYPE: function';



CREATE FUNCTION set_data_employee() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
  BEGIN
    NEW."Description" = coalesce(NEW."Surname", '') || ' ' || coalesce(NEW."Name", '');
    RETURN NEW;
  END;
$$;



CREATE FUNCTION set_data_suppliercontact() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
  BEGIN
    NEW."Description" = coalesce(NEW."Surname", '') || ' ' || coalesce(NEW."Name", '');
    RETURN NEW;
  END;
$$;



CREATE FUNCTION system_attribute_create(cmclass character varying, attributename character varying, denormalizedsqltype character varying, attributedefault character varying, attributenotnull boolean, attributeunique boolean, attributecomment character varying, attributereference character varying, attributereferencedomain character varying, attributereferencetype character varying, attributereferenceisdirect boolean) RETURNS integer
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION system_attribute_delete(cmclass character varying, attributename character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
BEGIN
	PERFORM cm_delete_class_attribute(CMClass, AttributeName);
	RETURN TRUE;
END;
$$;



CREATE FUNCTION system_attribute_modify(cmclass text, attributename text, attributenewname text, denormalizedsqltype text, attributedefault text, attributenotnull boolean, attributeunique boolean, attributecomment text) RETURNS boolean
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION system_class_create(classname character varying, parentclass character varying, issuperclass boolean, classcomment character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
	-- consistency checks for wrong signatures
	IF IsSuperClass <> _cm_is_superclass_comment(ClassComment) THEN
		RAISE EXCEPTION 'CM_FORBIDDEN_OPERATION';
	END IF;

	RETURN cm_create_class(ClassName, ParentClass, ClassComment);
END;
$$;



CREATE FUNCTION system_class_delete(cmclass character varying) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_class($1);
$_$;



CREATE FUNCTION system_class_modify(classid integer, newclassname character varying, newissuperclass boolean, newclasscomment character varying) RETURNS boolean
    LANGUAGE plpgsql
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
$$;



CREATE FUNCTION system_domain_create(cmdomain text, domainclass1 text, domainclass2 text, domaincomment text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
	TableName text := _cm_domain_cmname(CMDomain);
	HistoryTableName text := _cm_history_cmname(TableName);
    DomainId oid;
BEGIN
	-- TODO: Check DomainClass1 and DomainClass2

	RETURN cm_create_domain(CMDomain, DomainComment);
END
$$;



CREATE FUNCTION system_domain_delete(cmdomain text) RETURNS void
    LANGUAGE sql
    AS $_$
	SELECT cm_delete_domain($1);
$_$;



CREATE FUNCTION system_domain_modify(domainid oid, domainname text, domainclass1 text, domainclass2 text, newcomment text) RETURNS boolean
    LANGUAGE plpgsql
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
$$;


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



COMMENT ON COLUMN "Class"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Class"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Class"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Class"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



CREATE TABLE "Activity" (
    "FlowStatus" integer,
    "ActivityDefinitionId" character varying[],
    "ProcessCode" text,
    "NextExecutor" character varying[],
    "ActivityInstanceId" character varying[],
    "PrevExecutors" character varying[],
    "UniqueProcessDefinition" text
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



COMMENT ON COLUMN "Activity"."FlowStatus" IS 'MODE: read|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus';



COMMENT ON COLUMN "Activity"."ActivityDefinitionId" IS 'MODE: reserved|DESCR: Activity Definition Ids (for speed)';



COMMENT ON COLUMN "Activity"."ProcessCode" IS 'MODE: reserved|DESCR: Process Instance Id';



COMMENT ON COLUMN "Activity"."NextExecutor" IS 'MODE: reserved|DESCR: Activity Instance performers';



COMMENT ON COLUMN "Activity"."ActivityInstanceId" IS 'MODE: reserved|DESCR: Activity Instance Ids';



COMMENT ON COLUMN "Activity"."PrevExecutors" IS 'MODE: reserved|DESCR: Process Instance performers up to now';



COMMENT ON COLUMN "Activity"."UniqueProcessDefinition" IS 'MODE: reserved|DESCR: Unique Process Definition (for speed)';



CREATE TABLE "Asset" (
    "SerialNumber" character varying(40),
    "Supplier" integer,
    "PurchaseDate" date,
    "AcceptanceDate" date,
    "FinalCost" numeric(6,2),
    "Brand" integer,
    "Model" character varying(100),
    "Room" integer,
    "Assignee" integer,
    "TechnicalReference" integer,
    "Workplace" integer,
    "AcceptanceNotes" text
)
INHERITS ("Class");



COMMENT ON TABLE "Asset" IS 'DESCR: Asset|MODE: read|STATUS: active|SUPERCLASS: true|TYPE: class';



COMMENT ON COLUMN "Asset"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Asset"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Asset"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Asset"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Asset"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Asset"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Asset"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



CREATE TABLE "Building" (
    "Address" character varying(100),
    "ZIP" character varying(5),
    "City" character varying(50),
    "Country" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Building" IS 'DESCR: Building|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Building"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Name|FIELDMODE: write|GROUP: |INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Building"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Building"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Building"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Building"."Address" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Address|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Building"."ZIP" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: ZIP|FIELDMODE: write|GROUP: |INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "Building"."City" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: City|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Building"."Country" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Country|FIELDMODE: write|GROUP: |INDEX: 7|LOOKUP: Country|MODE: write|STATUS: active';



CREATE TABLE "Building_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Building");



CREATE TABLE "Computer" (
    "RAM" integer,
    "CPUNumber" integer,
    "CPUSpeed" numeric(5,3),
    "HDSize" integer,
    "IPAddress" inet
)
INHERITS ("Asset");



COMMENT ON TABLE "Computer" IS 'DESCR: Computer|MODE: read|STATUS: active|SUPERCLASS: true|TYPE: class';



COMMENT ON COLUMN "Computer"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Computer"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Computer"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Computer"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Computer"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Computer"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "Computer"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



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



COMMENT ON COLUMN "Email"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Email"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



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



CREATE TABLE "Employee" (
    "Surname" character varying(50),
    "Name" character varying(50),
    "Type" integer,
    "Qualification" integer,
    "Level" integer,
    "Email" character varying(50),
    "Office" integer,
    "Phone" character varying(20),
    "Mobile" character varying(20),
    "Fax" character varying(20),
    "State" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Employee" IS 'DESCR: Employee|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Employee"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."Code" IS 'BASEDSP: true|CLASSORDER: 1|DESCR: Number|FIELDMODE: write|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Employee"."Description" IS 'BASEDSP: true|CLASSORDER: -2|DESCR: Nominative|FIELDMODE: hidden|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Employee"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Employee"."Notes" IS 'BASEDSP: false|CLASSORDER: -3|DESCR: Notes|FIELDMODE: write|INDEX: 3|MODE: read|STATUS: active';



COMMENT ON COLUMN "Employee"."Surname" IS 'BASEDSP: true|CLASSORDER: -4|DESCR: Surname|FIELDMODE: write|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Name" IS 'BASEDSP: true|CLASSORDER: -5|DESCR: Name|FIELDMODE: write|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Type" IS 'BASEDSP: true|CLASSORDER: -6|DESCR: Type|FIELDMODE: write|INDEX: 6|LOOKUP: Employee type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Qualification" IS 'BASEDSP: true|CLASSORDER: -7|DESCR: Qualification|FIELDMODE: write|INDEX: 7|LOOKUP: Employee qualification|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Level" IS 'BASEDSP: false|CLASSORDER: -8|DESCR: Level|FIELDMODE: write|INDEX: 8|LOOKUP: Employee level|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Email" IS 'BASEDSP: true|CLASSORDER: -9|DESCR: Email|FIELDMODE: write|INDEX: 9|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Office" IS 'BASEDSP: true|CLASSORDER: -10|DESCR: Office|FIELDMODE: write|INDEX: 10|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: Members|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Employee"."Phone" IS 'BASEDSP: true|CLASSORDER: -11|DESCR: Phone|FIELDMODE: write|INDEX: 11|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Mobile" IS 'BASEDSP: false|CLASSORDER: -12|DESCR: Mobile|FIELDMODE: write|INDEX: 12|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."Fax" IS 'BASEDSP: false|CLASSORDER: -13|DESCR: Fax|FIELDMODE: write|INDEX: 13|MODE: write|STATUS: active';



COMMENT ON COLUMN "Employee"."State" IS 'BASEDSP: true|CLASSORDER: -14|DESCR: State|FIELDMODE: write|INDEX: 14|LOOKUP: Employee state|MODE: write|STATUS: active';



CREATE TABLE "Employee_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Employee");



CREATE TABLE "Floor" (
    "Building" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Floor" IS 'DESCR: Floor|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Floor"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Floor"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Floor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Floor"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Floor"."Building" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Building|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: BuildingFloor|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Floor_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Floor");



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



CREATE TABLE "Invoice" (
    "TotalAmount" numeric(6,2),
    "Type" integer,
    "Supplier" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Invoice" IS 'DESCR: Invoice|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Invoice"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Number|FIELDMODE: write|GROUP: |INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Invoice"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Invoice"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Invoice"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Invoice"."TotalAmount" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Total amount|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Invoice"."Type" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: |INDEX: 5|LOOKUP: Invoice type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Invoice"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierInvoice|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Invoice_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Invoice");



CREATE TABLE "License" (
    "Category" integer,
    "Version" character varying(20)
)
INHERITS ("Asset");



COMMENT ON TABLE "License" IS 'DESCR: License|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "License"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "License"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "License"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "License"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "License"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "License"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Category" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Category|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: License category|MODE: write|STATUS: active';



COMMENT ON COLUMN "License"."Version" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Version|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



CREATE TABLE "License_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("License");



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



COMMENT ON TABLE "Map" IS 'MODE: baseclass|TYPE: domain|DESCRDIR: è in relazione con|DESCRINV: è in relazione con|STATUS: active';



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



COMMENT ON COLUMN "Map_ActivityEmail"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_ActivityEmail"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_ActivityEmail_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_ActivityEmail");



CREATE TABLE "Map_AssetAssignee" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_AssetAssignee" IS 'CARDIN: 1:N|CLASS1: Employee|CLASS2: Asset|DESCRDIR: has in assignment|DESCRINV: assigned to|LABEL: Asset assignee|MASTERDETAIL: true|MDLABEL: Asset|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_AssetAssignee"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetAssignee"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_AssetAssignee_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_AssetAssignee");



CREATE TABLE "Map_AssetReference" (
    "Role" integer
)
INHERITS ("Map");



COMMENT ON TABLE "Map_AssetReference" IS 'CARDIN: 1:N|CLASS1: Employee|CLASS2: Asset|DESCRDIR: technical reference for assets|DESCRINV: has technical reference|LABEL: Asset reference|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_AssetReference"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_AssetReference"."Role" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Role|FIELDMODE: write|INDEX: 1|LOOKUP: Technical reference role|MODE: write|STATUS: active';



CREATE TABLE "Map_AssetReference_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_AssetReference");



CREATE TABLE "Map_BuildingFloor" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_BuildingFloor" IS 'CARDIN: 1:N|CLASS1: Building|CLASS2: Floor|DESCRDIR: includes floors|DESCRINV: belongs to building|LABEL: Building floor|MASTERDETAIL: true|MDLABEL: Floor|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_BuildingFloor"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_BuildingFloor"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_BuildingFloor_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_BuildingFloor");



CREATE TABLE "Map_FloorRoom" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_FloorRoom" IS 'CARDIN: 1:N|CLASS1: Floor|CLASS2: Room|DESCRDIR: includes rooms|DESCRINV: belongs to floor|LABEL: Floor room|MASTERDETAIL: true|MDLABEL: Room|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_FloorRoom"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_FloorRoom"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_FloorRoom_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_FloorRoom");



CREATE TABLE "Map_Members" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_Members" IS 'CARDIN: 1:N|CLASS1: Office|CLASS2: Employee|DESCRDIR: includes|DESCRINV: is member of|LABEL: Members|MASTERDETAIL: true|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_Members"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Members"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_Members_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_Members");



CREATE TABLE "Map_NetworkDeviceConnection" (
    "PortNumber" integer,
    "CableColor" integer
)
INHERITS ("Map");



COMMENT ON TABLE "Map_NetworkDeviceConnection" IS 'CARDIN: N:N|CLASS1: NetworkDevice|CLASS2: NetworkDevice|DESCRDIR: connected to|DESCRINV: connected to|LABEL: Network device connection|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."PortNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Port number|FIELDMODE: write|INDEX: 1|MODE: write|STATUS: active';



COMMENT ON COLUMN "Map_NetworkDeviceConnection"."CableColor" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Cable color|FIELDMODE: write|INDEX: 2|LOOKUP: Cable color|MODE: write|STATUS: active';



CREATE TABLE "Map_NetworkDeviceConnection_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_NetworkDeviceConnection");



CREATE TABLE "Map_OfficeRoom" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_OfficeRoom" IS 'CARDIN: 1:N|CLASS1: Office|CLASS2: Room|DESCRDIR: uses rooms|DESCRINV: used by office|LABEL: Office room|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_OfficeRoom"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_OfficeRoom"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_OfficeRoom_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_OfficeRoom");



CREATE TABLE "Map_RFCChangeManager" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RFCChangeManager" IS 'CARDIN: N:1|CLASS1: RequestForChange|CLASS2: Employee|DESCRDIR: has change manager|DESCRINV: change manager for|LABEL: RFCChangeManager|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCChangeManager"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RFCChangeManager_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RFCChangeManager");



CREATE TABLE "Map_RFCExecutor" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RFCExecutor" IS 'CARDIN: N:1|CLASS1: RequestForChange|CLASS2: Employee|DESCRDIR: Executed by|DESCRINV: Perform|LABEL: RFC Executor|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RFCExecutor"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCExecutor"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RFCExecutor_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RFCExecutor");



CREATE TABLE "Map_RFCRequester" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RFCRequester" IS 'CARDIN: N:1|CLASS1: RequestForChange|CLASS2: Employee|DESCRDIR: Requested by|DESCRINV: Requests|LABEL: RFC Requester|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RFCRequester"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RFCRequester"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RFCRequester_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RFCRequester");



CREATE TABLE "Map_RoomAsset" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RoomAsset" IS 'CARDIN: 1:N|CLASS1: Room|CLASS2: Asset|DESCRDIR: contains assets|DESCRINV: located in room|LABEL: Room asset|MASTERDETAIL: true|MDLABEL: Asset|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RoomAsset"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomAsset"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RoomAsset_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RoomAsset");



CREATE TABLE "Map_RoomNetworkPoint" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RoomNetworkPoint" IS 'CARDIN: 1:N|CLASS1: Room|CLASS2: NetworkPoint|DESCRDIR: contains network points|DESCRINV: located in room|LABEL: Room network point|MASTERDETAIL: true|MDLABEL: Network Points|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomNetworkPoint"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RoomNetworkPoint_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RoomNetworkPoint");



CREATE TABLE "Map_RoomWorkplace" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_RoomWorkplace" IS 'CARDIN: 1:N|CLASS1: Room|CLASS2: Workplace|DESCRDIR: contains workplaces|DESCRINV: located in room|LABEL: Room workplace|MASTERDETAIL: true|MDLABEL: Workplace|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_RoomWorkplace"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_RoomWorkplace_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_RoomWorkplace");



CREATE TABLE "Map_Supervisor" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_Supervisor" IS 'CARDIN: 1:N|CLASS1: Employee|CLASS2: Office|DESCRDIR: supervisor of|DESCRINV: has supervisor|LABEL: Supervisor|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_Supervisor"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_Supervisor"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_Supervisor_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_Supervisor");



CREATE TABLE "Map_SupplierAsset" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_SupplierAsset" IS 'CARDIN: 1:N|CLASS1: Supplier|CLASS2: Asset|DESCRDIR: provided assets|DESCRINV: provided by supplier|LABEL: Supplier asset|MASTERDETAIL: false|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_SupplierAsset"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierAsset"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_SupplierAsset_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_SupplierAsset");



CREATE TABLE "Map_SupplierContact" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_SupplierContact" IS 'CARDIN: 1:N|CLASS1: Supplier|CLASS2: SupplierContact|DESCRDIR: has contacts|DESCRINV: belongs to supplier|LABEL: Supplier contact|MASTERDETAIL: true|MDLABEL: SupplierContact|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_SupplierContact"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierContact"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_SupplierContact_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_SupplierContact");



CREATE TABLE "Map_SupplierInvoice" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_SupplierInvoice" IS 'CARDIN: 1:N|CLASS1: Supplier|CLASS2: Invoice|DESCRDIR: invoices delivered|DESCRINV: delivered by supplier|LABEL: Supplier invoice|MASTERDETAIL: true|MDLABEL: Invoice|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_SupplierInvoice"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_SupplierInvoice_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_SupplierInvoice");



CREATE TABLE "Map_UserRole" (
    "DefaultGroup" boolean
)
INHERITS ("Map");



COMMENT ON TABLE "Map_UserRole" IS 'MODE: reserved|TYPE: domain|CLASS1: User|CLASS2: Role|DESCRDIR: has role|DESCRINV: contains|CARDIN: N:N|STATUS: active';



COMMENT ON COLUMN "Map_UserRole"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_UserRole"."DefaultGroup" IS 'MODE: read|FIELDMODE: write|DESCR: Default Group|INDEX: 1|BASEDSP: true|STATUS: active';



CREATE TABLE "Map_UserRole_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_UserRole");



CREATE TABLE "Map_WorkplaceComposition" (
)
INHERITS ("Map");



COMMENT ON TABLE "Map_WorkplaceComposition" IS 'CARDIN: 1:N|CLASS1: Workplace|CLASS2: Asset|DESCRDIR: includes assets|DESCRINV: belongs to workplace|LABEL: Workplace composition|MASTERDETAIL: true|MDLABEL: Asset|MODE: write|OPENEDROWS: 0|STATUS: active|TYPE: domain';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdDomain" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdClass1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdObj1" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdClass2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."IdObj2" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."EndDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Map_WorkplaceComposition"."Id" IS 'MODE: reserved';



CREATE TABLE "Map_WorkplaceComposition_history" (
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Map_WorkplaceComposition");



CREATE TABLE "Menu" (
    "IdParent" integer DEFAULT 0,
    "IdElementClass" regclass,
    "IdElementObj" integer DEFAULT 0 NOT NULL,
    "Number" integer DEFAULT 0 NOT NULL,
    "IdGroup" integer DEFAULT 0 NOT NULL,
    "Type" character varying(70) NOT NULL
)
INHERITS ("Class");



COMMENT ON TABLE "Menu" IS 'MODE: reserved|TYPE: class|DESCR: Menu|SUPERCLASS: false|STATUS: active';



COMMENT ON COLUMN "Menu"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Menu"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Menu"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



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
    "Type" integer,
    "ScreenSize" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "Monitor" IS 'DESCR: Monitor|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Monitor"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Monitor"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Monitor"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Monitor"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Monitor"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Monitor"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."Type" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: Monitor type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Monitor"."ScreenSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: ScreenSize|FIELDMODE: write|GROUP: Technical data|INDEX: 16|LOOKUP: Screen size|MODE: write|STATUS: active';



CREATE TABLE "Monitor_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Monitor");



CREATE TABLE "NetworkDevice" (
    "Type" integer,
    "PortNumber" integer,
    "PortSpeed" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "NetworkDevice" IS 'DESCR: Network device|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "NetworkDevice"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkDevice"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "NetworkDevice"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."Type" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: Network device type|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."PortNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Port number|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "NetworkDevice"."PortSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Port speed (Mb)|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



CREATE TABLE "NetworkDevice_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("NetworkDevice");



CREATE TABLE "NetworkPoint" (
    "Room" integer
)
INHERITS ("Class");



COMMENT ON TABLE "NetworkPoint" IS 'DESCR: Network point|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "NetworkPoint"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "NetworkPoint"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "NetworkPoint"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "NetworkPoint"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "NetworkPoint"."Room" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomNetworkPoint|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "NetworkPoint_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("NetworkPoint");



CREATE TABLE "Notebook" (
    "ScreenSize" integer
)
INHERITS ("Computer");



COMMENT ON TABLE "Notebook" IS 'DESCR: Notebook|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Notebook"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Notebook"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Notebook"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Notebook"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Notebook"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Notebook"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "Notebook"."ScreenSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Screen size|FIELDMODE: write|GROUP: Technical data|INDEX: 19|LOOKUP: Screen size|MODE: write|STATUS: active';



CREATE TABLE "Notebook_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Notebook");



CREATE TABLE "Office" (
    "ShortDescription" character varying(100),
    "Supervisor" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Office" IS 'DESCR: Office|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Office"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Office"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Office"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Office"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Office"."ShortDescription" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Short description|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Office"."Supervisor" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supervisor|FIELDMODE: write|GROUP: |INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: Supervisor|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Office_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Office");



CREATE TABLE "PC" (
    "SoundCard" character varying(50),
    "VideoCard" character varying(50)
)
INHERITS ("Computer");



COMMENT ON TABLE "PC" IS 'DESCR: PC|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "PC"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "PC"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "PC"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "PC"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "PC"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 3|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 8|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 9|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 10|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "PC"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 14|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."SoundCard" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Sound card|FIELDMODE: write|GROUP: Technical data|INDEX: 20|MODE: write|STATUS: active';



COMMENT ON COLUMN "PC"."VideoCard" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Video card|FIELDMODE: write|GROUP: Technical data|INDEX: 21|MODE: write|STATUS: active';



CREATE TABLE "PC_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("PC");



CREATE TABLE "Patch" (
)
INHERITS ("Class");



COMMENT ON TABLE "Patch" IS 'DESCR: |MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Patch"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Patch"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Patch"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Patch"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



CREATE TABLE "Patch_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Patch");



CREATE TABLE "Printer" (
    "Type" integer,
    "PaperSize" integer,
    "Color" boolean,
    "Usage" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "Printer" IS 'DESCR: Printer|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Printer"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Printer"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Printer"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Printer"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Printer"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Printer"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Type" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|GROUP: Technical data|INDEX: 15|LOOKUP: Printer type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."PaperSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Paper size|FIELDMODE: write|GROUP: Technical data|INDEX: 16|LOOKUP: Paper size|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Color" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Color|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Printer"."Usage" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Usage|FIELDMODE: write|GROUP: Technical data|INDEX: 18|LOOKUP: Printer usage|MODE: write|STATUS: active';



CREATE TABLE "Printer_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Printer");



CREATE TABLE "Rack" (
    "UnitNumber" integer,
    "Depth" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "Rack" IS 'DESCR: Rack|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Rack"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Rack"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Rack"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Rack"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Rack"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Rack"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."UnitNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Unit number|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Rack"."Depth" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Depth (cm)|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



CREATE TABLE "Rack_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Rack");



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



CREATE TABLE "RequestForChange" (
    "Requester" integer,
    "RFCStartDate" timestamp without time zone,
    "RequestNumber" integer,
    "RFCStatus" integer,
    "RFCDescription" text,
    "Category" integer,
    "FormalEvaluation" integer,
    "RFCPriority" integer,
    "ImpactAnalysisRequest" boolean,
    "CostAnalysisRequest" boolean,
    "RiskAnalysisRequest" boolean,
    "ImpactAnalysisResult" text,
    "CostAnalysisResult" text,
    "RiskAnalysisResult" text,
    "Decision" integer,
    "PlannedActions" text,
    "ExecutionStartDate" timestamp without time zone,
    "ActionsPerformed" text,
    "ExecutionEndDate" timestamp without time zone,
    "FinalResult" integer,
    "RFCEndDate" timestamp without time zone
)
INHERITS ("Activity");



COMMENT ON TABLE "RequestForChange" IS 'DESCR: Request for change|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class|USERSTOPPABLE: false';



COMMENT ON COLUMN "RequestForChange"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."IdClass" IS 'MODE: reserved|DESCR: Classe';



COMMENT ON COLUMN "RequestForChange"."Code" IS 'MODE: read|DESCR: Nome Attività|INDEX: 0||LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: false|DATEEXPIRE: false|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Description" IS 'MODE: read|DESCR: Description|INDEX: 1|LOOKUP: |REFERENCEDOM: |REFERENCETYPE: |REFERENCEDIRECT: true|DATEEXPIRE: false|BASEDSP: false|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "RequestForChange"."Notes" IS 'MODE: read|DESCR: Annotazioni';



COMMENT ON COLUMN "RequestForChange"."FlowStatus" IS 'MODE: read|DESCR: Process Status|INDEX: 2|LOOKUP: FlowStatus';



COMMENT ON COLUMN "RequestForChange"."ActivityDefinitionId" IS 'MODE: reserved|DESCR: Activity Definition Ids (for speed)';



COMMENT ON COLUMN "RequestForChange"."ProcessCode" IS 'MODE: reserved|DESCR: Process Instance Id';



COMMENT ON COLUMN "RequestForChange"."NextExecutor" IS 'MODE: reserved|DESCR: Activity Instance performers';



COMMENT ON COLUMN "RequestForChange"."ActivityInstanceId" IS 'MODE: reserved|DESCR: Activity Instance Ids';



COMMENT ON COLUMN "RequestForChange"."PrevExecutors" IS 'MODE: reserved|DESCR: Process Instance performers up to now';



COMMENT ON COLUMN "RequestForChange"."UniqueProcessDefinition" IS 'MODE: reserved|DESCR: Unique Process Definition (for speed)';



COMMENT ON COLUMN "RequestForChange"."Requester" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Requester|FIELDMODE: write|INDEX: 24|MODE: write|REFERENCEDIRECT: true|REFERENCEDOM: RFCRequester|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCStartDate" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Start date|FIELDMODE: write|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RequestNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Request number|FIELDMODE: write|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCStatus" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Status|FIELDMODE: write|INDEX: 6|LOOKUP: RFC status|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCDescription" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Description|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Category" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Category|FIELDMODE: write|INDEX: 8|LOOKUP: RFC Category|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."FormalEvaluation" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Formal evaluation|FIELDMODE: write|INDEX: 9|LOOKUP: RFC formal evaluation|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCPriority" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Priority|FIELDMODE: write|INDEX: 25|LOOKUP: RFC priority|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ImpactAnalysisRequest" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Impact analysis request|FIELDMODE: write|INDEX: 11|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."CostAnalysisRequest" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Cost analysis request|FIELDMODE: write|INDEX: 12|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RiskAnalysisRequest" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Risk analysis request|FIELDMODE: write|INDEX: 13|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ImpactAnalysisResult" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Impact analysis result|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 14|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."CostAnalysisResult" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Cost analysis result|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RiskAnalysisResult" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Risk analysis result|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."Decision" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Decision|FIELDMODE: write|INDEX: 17|LOOKUP: RFC decision|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."PlannedActions" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Planned actions|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ExecutionStartDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Execution start date|FIELDMODE: write|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ActionsPerformed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Actions performed|EDITORTYPE: HTML|FIELDMODE: write|INDEX: 20|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."ExecutionEndDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Execution end date|FIELDMODE: write|INDEX: 21|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."FinalResult" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Final result|FIELDMODE: write|INDEX: 22|LOOKUP: RFC final result|MODE: write|STATUS: active';



COMMENT ON COLUMN "RequestForChange"."RFCEndDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: End date|FIELDMODE: write|INDEX: 23|MODE: write|STATUS: active';



CREATE TABLE "RequestForChange_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("RequestForChange");



CREATE TABLE "Role" (
    "Code" character varying(100) NOT NULL,
    "Administrator" boolean,
    "startingClass" regclass,
    "Email" character varying(320),
    "DisabledModules" character varying[],
    "DisabledCardTabs" character varying[],
    "DisabledProcessTabs" character varying[],
    "HideSidePanel" boolean DEFAULT false NOT NULL,
    "FullScreenMode" boolean DEFAULT false NOT NULL,
    "SimpleHistoryModeForCard" boolean DEFAULT false NOT NULL,
    "SimpleHistoryModeForProcess" boolean DEFAULT false NOT NULL,
    "ProcessWidgetAlwaysEnabled" boolean DEFAULT false NOT NULL
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



COMMENT ON COLUMN "Role"."DisabledCardTabs" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."DisabledProcessTabs" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."HideSidePanel" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."FullScreenMode" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."SimpleHistoryModeForCard" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."SimpleHistoryModeForProcess" IS 'MODE: reserved';



COMMENT ON COLUMN "Role"."ProcessWidgetAlwaysEnabled" IS 'MODE: reserved';



CREATE TABLE "Room" (
    "Floor" integer,
    "UsageType" integer,
    "Surface" numeric(6,2),
    "Office" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Room" IS 'DESCR: Room|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Room"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Room"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Room"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Room"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Room"."Floor" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Floor|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: FloorRoom|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Room"."UsageType" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Usage type|FIELDMODE: write|GROUP: |INDEX: 5|LOOKUP: Room usage type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Room"."Surface" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Surface|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Room"."Office" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Office|FIELDMODE: write|GROUP: |INDEX: 7|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: OfficeRoom|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Room_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Room");



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



COMMENT ON COLUMN "Scheduler"."CronExpression" IS 'MODE: read|DESCR: Cron Expression|STATUS: active';



COMMENT ON COLUMN "Scheduler"."Detail" IS 'MODE: read|DESCR: Job Detail|STATUS: active';



CREATE TABLE "Scheduler_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Scheduler");



CREATE TABLE "Server" (
    "RAID" integer,
    "RedundantPowerSupply" boolean
)
INHERITS ("Computer");



COMMENT ON TABLE "Server" IS 'DESCR: Server|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Server"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Server"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Server"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Server"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Server"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "Server"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."RAM" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAM|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."CPUNumber" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Number of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 16|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."CPUSpeed" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Speed of CPU|FIELDMODE: write|GROUP: Technical data|INDEX: 17|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."HDSize" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Harddisk total size (GB)|FIELDMODE: write|GROUP: Technical data|INDEX: 18|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."IPAddress" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: IPAddress|FIELDMODE: write|GROUP: Technical data|INDEX: 19|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."RAID" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: RAID|FIELDMODE: write|GROUP: Technical data|INDEX: 19|LOOKUP: RAID|MODE: write|STATUS: active';



COMMENT ON COLUMN "Server"."RedundantPowerSupply" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Redundant power supply|FIELDMODE: write|GROUP: Technical data|INDEX: 20|MODE: write|STATUS: active';



CREATE TABLE "Server_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Server");



CREATE TABLE "Supplier" (
    "Type" integer,
    "Address" character varying(50),
    "ZIP" character varying(5),
    "City" character varying(50),
    "Phone" character varying(20),
    "Email" character varying(50),
    "WebSite" character varying(50),
    "Country" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Supplier" IS 'DESCR: Supplier|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Supplier"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "Supplier"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "Supplier"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Supplier"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Supplier"."Type" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Type|FIELDMODE: write|INDEX: 3|LOOKUP: Supplier type|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Address" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Address|FIELDMODE: write|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."ZIP" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: ZIP|FIELDMODE: write|INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."City" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: City|FIELDMODE: write|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Phone" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Phone|FIELDMODE: write|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Email" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Email|FIELDMODE: write|INDEX: 9|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."WebSite" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: WebSite|FIELDMODE: write|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "Supplier"."Country" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Country|FIELDMODE: write|GROUP: |INDEX: 7|LOOKUP: Country|MODE: write|STATUS: active';



CREATE TABLE "SupplierContact" (
    "Surname" character varying(50),
    "Name" character varying(50),
    "Supplier" integer,
    "Phone" character varying(20),
    "Mobile" character varying(20),
    "Email" character varying(50)
)
INHERITS ("Class");



COMMENT ON TABLE "SupplierContact" IS 'DESCR: SupplierContact|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "SupplierContact"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "SupplierContact"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: hidden|GROUP: |INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "SupplierContact"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "SupplierContact"."Surname" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Surname|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Name" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Name|FIELDMODE: write|GROUP: |INDEX: 5|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: |INDEX: 6|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierContact|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Phone" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Phone|FIELDMODE: write|GROUP: |INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Mobile" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Mobile|FIELDMODE: write|GROUP: |INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "SupplierContact"."Email" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Email|FIELDMODE: write|GROUP: |INDEX: 9|MODE: write|STATUS: active';



CREATE TABLE "SupplierContact_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("SupplierContact");



CREATE TABLE "Supplier_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Supplier");



CREATE TABLE "UPS" (
    "Power" integer
)
INHERITS ("Asset");



COMMENT ON TABLE "UPS" IS 'DESCR: UPS|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "UPS"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."Code" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Code|FIELDMODE: write|GROUP: General data|INDEX: 1|MODE: read|STATUS: active';



COMMENT ON COLUMN "UPS"."Description" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Description|FIELDMODE: write|GROUP: General data|INDEX: 2|MODE: read|STATUS: active';



COMMENT ON COLUMN "UPS"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "UPS"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "UPS"."SerialNumber" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Serialnumber|FIELDMODE: write|GROUP: General data|INDEX: 4|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Supplier" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Supplier|FIELDMODE: write|GROUP: Administrative data|INDEX: 5|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: SupplierAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."PurchaseDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Purchase date|FIELDMODE: write|GROUP: Administrative data|INDEX: 6|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."AcceptanceDate" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance date|FIELDMODE: write|GROUP: Administrative data|INDEX: 7|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."FinalCost" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Final cost|FIELDMODE: write|GROUP: Administrative data|INDEX: 8|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Brand" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Brand|FIELDMODE: write|GROUP: Technical data|INDEX: 9|LOOKUP: Brand|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Model" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Model|FIELDMODE: write|GROUP: Technical data|INDEX: 10|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Room" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: General data|INDEX: 11|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomAsset|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."Assignee" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Assignee|FIELDMODE: write|GROUP: General data|INDEX: 12|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetAssignee|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."TechnicalReference" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Technical reference|FIELDMODE: write|GROUP: Technical data|INDEX: 13|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: AssetReference|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."Workplace" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Workplace|FIELDMODE: write|GROUP: General data|INDEX: 14|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: WorkplaceComposition|REFERENCETYPE: restrict|STATUS: active';



COMMENT ON COLUMN "UPS"."AcceptanceNotes" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Acceptance notes|EDITORTYPE: HTML|FIELDMODE: write|GROUP: Administrative data|INDEX: 15|MODE: write|STATUS: active';



COMMENT ON COLUMN "UPS"."Power" IS 'BASEDSP: false|CLASSORDER: 0|DESCR: Power (W)|FIELDMODE: write|GROUP: Technical data|INDEX: 15|MODE: write|STATUS: active';



CREATE TABLE "UPS_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("UPS");



CREATE TABLE "User" (
    "Username" character varying(40) NOT NULL,
    "Password" character varying(40),
    "Email" character varying(320)
)
INHERITS ("Class");



COMMENT ON TABLE "User" IS 'MODE: write|TYPE: class|DESCR: Utenti|SUPERCLASS: false|MANAGER: class|STATUS: active';



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



CREATE TABLE "Workplace" (
    "Room" integer
)
INHERITS ("Class");



COMMENT ON TABLE "Workplace" IS 'DESCR: Workplace|MODE: write|STATUS: active|SUPERCLASS: false|TYPE: class';



COMMENT ON COLUMN "Workplace"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."IdClass" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."Code" IS 'MODE: read|DESCR: Code|INDEX: 1|BASEDSP: true';



COMMENT ON COLUMN "Workplace"."Description" IS 'MODE: read|DESCR: Description|INDEX: 2|BASEDSP: true';



COMMENT ON COLUMN "Workplace"."Status" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."BeginDate" IS 'MODE: reserved';



COMMENT ON COLUMN "Workplace"."Notes" IS 'MODE: read|DESCR: Notes|INDEX: 3';



COMMENT ON COLUMN "Workplace"."Room" IS 'BASEDSP: true|CLASSORDER: 0|DESCR: Room|FIELDMODE: write|GROUP: |INDEX: 4|MODE: write|REFERENCEDIRECT: false|REFERENCEDOM: RoomWorkplace|REFERENCETYPE: restrict|STATUS: active';



CREATE TABLE "Workplace_history" (
    "CurrentId" integer NOT NULL,
    "EndDate" timestamp without time zone DEFAULT now() NOT NULL
)
INHERITS ("Workplace");



CREATE TABLE "_Dashboards" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Definition" text NOT NULL
);



COMMENT ON TABLE "_Dashboards" IS 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass';



COMMENT ON COLUMN "_Dashboards"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Dashboards"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Dashboards"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Dashboards"."Definition" IS 'MODE: write|STATUS: active';



CREATE TABLE "_Templates" (
    "Id" integer DEFAULT _cm_new_card_id() NOT NULL,
    "User" character varying(40),
    "BeginDate" timestamp without time zone DEFAULT now() NOT NULL,
    "Name" text NOT NULL,
    "Template" text NOT NULL
);



COMMENT ON TABLE "_Templates" IS 'MODE: reserved|STATUS: active|SUPERCLASS: false|TYPE: simpleclass';



COMMENT ON COLUMN "_Templates"."Id" IS 'MODE: reserved';



COMMENT ON COLUMN "_Templates"."User" IS 'MODE: reserved';



COMMENT ON COLUMN "_Templates"."BeginDate" IS 'MODE: write|FIELDMODE: read|BASEDSP: true';



COMMENT ON COLUMN "_Templates"."Name" IS 'MODE: write|STATUS: active';



COMMENT ON COLUMN "_Templates"."Template" IS 'MODE: write|STATUS: active';



CREATE SEQUENCE class_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;



COMMENT ON SEQUENCE class_seq IS 'Sequence for autoincrement class';



SELECT pg_catalog.setval('class_seq', 1264, true);



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
    (((SELECT DISTINCT (system_classcatalog.classid)::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Code", _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text) AS "Description", CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text =  ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <>  ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND ("Role"."Id" = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END AS "IdElementClass", 0 AS "IdElementObj", "Role"."Id" AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Type" FROM ((system_classcatalog JOIN "Role" ON (("Role"."Status" = 'A'::bpchar))) LEFT JOIN system_treecatalog ON ((system_treecatalog.childid = system_classcatalog.classid))) WHERE (((NOT (system_classcatalog.classid IN (SELECT ("Menu"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" WHERE (((("Menu"."Code")::text <>  ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND ("Menu"."Status" = 'A'::bpchar)) AND ("Role"."Id" = "Menu"."IdGroup"))))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('MODE'::character varying)::text))::text =  ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text]))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('STATUS'::character varying)::text))::text = 'active'::text)) ORDER BY (system_classcatalog.classid)::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false), _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text), CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text =  ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <>  ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND ("Role"."Id" = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END, 0::integer, "Role"."Id", _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false)) UNION SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", "AllReport"."RoleId" AS "IdGroup", "AllReport"."Type" FROM ((SELECT ((_cm_legacy_get_menu_type(false, false, true, false))::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i]) AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type", "Role"."Id" AS "RoleId" FROM generate_series(1, 6) i(i), ("Report" JOIN "Role" ON (("Role"."Status" = 'A'::bpchar))) WHERE ((("Report"."Status")::text = 'A'::text) AND (((i.i + 1) / 2) = CASE WHEN (("Report"."Type")::text = 'normal'::text) THEN 1 WHEN (("Report"."Type")::text = 'custom'::text) THEN 2 WHEN (("Report"."Type")::text = 'openoffice'::text) THEN 3 ELSE 0 END))) "AllReport" LEFT JOIN "Menu" ON ((((("AllReport"."IdElementObj" = "Menu"."IdElementObj") AND ("Menu"."Status" = 'A'::bpchar)) AND ("AllReport"."RoleId" = "Menu"."IdGroup")) AND ("AllReport"."Code" = ("Menu"."Code")::text)))) WHERE ("Menu"."Code" IS NULL)) UNION (SELECT DISTINCT (system_classcatalog.classid)::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Code", _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text) AS "Description", CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text =  ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <>  ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND (0 = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END AS "IdElementClass", 0 AS "IdElementObj", 0 AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false) AS "Type" FROM (system_classcatalog LEFT JOIN system_treecatalog ON ((system_treecatalog.childid = system_classcatalog.classid))) WHERE (((NOT (system_classcatalog.classid IN (SELECT ("Menu"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" WHERE (((("Menu"."Code")::text <>  ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND ("Menu"."Status" = 'A'::bpchar)) AND (0 = "Menu"."IdGroup"))))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('MODE'::character varying)::text))::text =  ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text]))) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('STATUS'::character varying)::text))::text = 'active'::text)) ORDER BY (system_classcatalog.classid)::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false), _cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('DESCR'::character varying)::text), CASE WHEN (((_cm_legacy_read_comment(((system_treecatalog.childcomment)::character varying)::text, ('MODE'::character varying)::text))::text =  ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text])) AND (NOT (((system_treecatalog.childid)::regclass)::oid IN (SELECT ("Menu1"."IdElementClass")::integer AS "IdElementClass" FROM "Menu" "Menu1" WHERE (((("Menu1"."Code")::text <>  ALL (ARRAY[('folder'::character varying)::text, ('report'::character varying)::text, ('view'::character varying)::text, ('Folder'::character varying)::text, ('Report'::character varying)::text, ('View'::character varying)::text])) AND ("Menu1"."Status" = 'A'::bpchar)) AND (0 = "Menu1"."IdGroup")))))) THEN (system_treecatalog.childid)::regclass ELSE NULL::regclass END, 0::integer, _cm_legacy_get_menu_code(_cm_is_superclass_comment(((system_treecatalog.childcomment)::character varying)::text), _cm_legacy_class_is_process(((system_classcatalog.classcomment)::character varying)::text), false, false))) UNION SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", 0 AS "IdGroup", "AllReport"."Type" FROM ((SELECT ((_cm_legacy_get_menu_type(false, false, true, false))::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i]) AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type" FROM generate_series(1, 6) i(i), "Report" WHERE ((("Report"."Status")::text = 'A'::text) AND (((i.i + 1) / 2) = CASE WHEN (("Report"."Type")::text = 'normal'::text) THEN 1 WHEN (("Report"."Type")::text = 'custom'::text) THEN 2 WHEN (("Report"."Type")::text = 'openoffice'::text) THEN 3 ELSE 0 END))) "AllReport" LEFT JOIN "Menu" ON ((((("AllReport"."IdElementObj" = "Menu"."IdElementObj") AND ("Menu"."Status" = 'A'::bpchar)) AND (0 = "Menu"."IdGroup")) AND ("AllReport"."Code" = ("Menu"."Code")::text)))) WHERE ("Menu"."Code" IS NULL);



CREATE VIEW system_privilegescatalog AS
    SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode" FROM ((SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode" FROM "Grant" UNION SELECT (-1), '"Grant"', '', '', 'A', 'admin', now() AS now, NULL::unknown AS unknown, "Role"."Id", (system_classcatalog.classid)::regclass AS classid, '-' FROM system_classcatalog, "Role" WHERE ((((system_classcatalog.classid)::regclass)::oid <> ('"Class"'::regclass)::oid) AND (NOT ((("Role"."Id")::text || ((system_classcatalog.classid)::integer)::text) IN (SELECT (("Grant"."IdRole")::text || ((("Grant"."IdGrantedClass")::oid)::integer)::text) FROM "Grant"))))) permission JOIN system_classcatalog ON ((((permission."IdGrantedClass")::oid = system_classcatalog.classid) AND ((_cm_legacy_read_comment(((system_classcatalog.classcomment)::character varying)::text, ('MODE'::character varying)::text))::text =  ANY (ARRAY[('write'::character varying)::text, ('read'::character varying)::text]))))) ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";



CREATE VIEW system_relationlist AS
    SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass1")::integer AS idclass1, "Map"."IdObj1" AS idobj1, ("Map"."IdClass2")::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, true AS direct, NULL::unknown AS version FROM ((((((("Map" JOIN "Class" ON ((((("Class"."IdClass")::oid = ("Map"."IdClass2")::oid) AND ("Class"."Id" = "Map"."IdObj2")) AND ("Class"."Status" = 'A'::bpchar)))) LEFT JOIN pg_class pg_class0 ON ((pg_class0.oid = ("Map"."IdDomain")::oid))) LEFT JOIN pg_description pg_description0 ON (((((pg_description0.objoid = pg_class0.oid) AND (pg_description0.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)))) LEFT JOIN pg_class pg_class1 ON ((pg_class1.oid = ("Map"."IdClass1")::oid))) LEFT JOIN pg_description pg_description1 ON (((pg_description1.objoid = pg_class1.oid) AND (pg_description1.objsubid = 0)))) LEFT JOIN pg_class pg_class2 ON ((pg_class2.oid = ("Map"."IdClass2")::oid))) LEFT JOIN pg_description pg_description2 ON (((pg_description2.objoid = pg_class2.oid) AND (pg_description2.objsubid = 0)))) UNION SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass2")::integer AS idclass1, "Map"."IdObj2" AS idobj1, ("Map"."IdClass1")::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."BeginDate" AS begindate, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRINV'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description1.description, 'DESCR'::text))::text AS classdescription, false AS direct, NULL::unknown AS version FROM ((((((("Map" JOIN "Class" ON ((((("Class"."IdClass")::oid = ("Map"."IdClass1")::oid) AND ("Class"."Id" = "Map"."IdObj1")) AND ("Class"."Status" = 'A'::bpchar)))) LEFT JOIN pg_class pg_class0 ON ((pg_class0.oid = ("Map"."IdDomain")::oid))) LEFT JOIN pg_description pg_description0 ON (((((pg_description0.objoid = pg_class0.oid) AND (pg_description0.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)))) LEFT JOIN pg_class pg_class1 ON ((pg_class1.oid = ("Map"."IdClass1")::oid))) LEFT JOIN pg_description pg_description1 ON (((pg_description1.objoid = pg_class1.oid) AND (pg_description1.objsubid = 0)))) LEFT JOIN pg_class pg_class2 ON ((pg_class2.oid = ("Map"."IdClass2")::oid))) LEFT JOIN pg_description pg_description2 ON (((pg_description2.objoid = pg_class2.oid) AND (pg_description2.objsubid = 0))));



CREATE VIEW system_relationlist_history AS
    SELECT "Map"."Id" AS id, pg_class1.relname AS class1, pg_class2.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass1")::integer AS idclass1, "Map"."IdObj1" AS idobj1, ("Map"."IdClass2")::integer AS idclass2, "Map"."IdObj2" AS idobj2, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRDIR'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, true AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::unknown AS version FROM ("Map" LEFT JOIN "Class" ON (((("Class"."IdClass")::oid = ("Map"."IdClass2")::oid) AND ("Class"."Id" = "Map"."IdObj2")))), pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2 WHERE (((((((((((("Map"."Status" = 'U'::bpchar) AND (pg_class1.oid = ("Map"."IdClass1")::oid)) AND (pg_class2.oid = ("Map"."IdClass2")::oid)) AND (pg_class0.oid = ("Map"."IdDomain")::oid)) AND (pg_description0.objoid = pg_class0.oid)) AND (pg_description0.objsubid = 0)) AND (pg_description1.objoid = pg_class1.oid)) AND (pg_description1.objsubid = 0)) AND (pg_description2.objoid = pg_class2.oid)) AND (pg_description2.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description)) UNION SELECT "Map"."Id" AS id, pg_class2.relname AS class1, pg_class1.relname AS class2, "Class"."Code" AS fieldcode, "Class"."Description" AS fielddescription, pg_class0.relname AS realname, ("Map"."IdDomain")::integer AS iddomain, ("Map"."IdClass2")::integer AS idclass1, "Map"."IdObj2" AS idobj1, ("Map"."IdClass1")::integer AS idclass2, "Map"."IdObj1" AS idobj2, "Map"."Status" AS status, (_cm_legacy_read_comment(pg_description0.description, 'DESCRINV'::text))::text AS domaindescription, (_cm_legacy_read_comment(pg_description0.description, 'MASTERDETAIL'::text))::text AS domainmasterdetail, (_cm_legacy_read_comment(pg_description0.description, 'CARDIN'::text))::text AS domaincardinality, (_cm_legacy_read_comment(pg_description2.description, 'DESCR'::text))::text AS classdescription, false AS direct, "Map"."User" AS username, "Map"."BeginDate" AS begindate, "Map"."EndDate" AS enddate, NULL::unknown AS version FROM ("Map" LEFT JOIN "Class" ON (((("Class"."IdClass")::oid = ("Map"."IdClass1")::oid) AND ("Class"."Id" = "Map"."IdObj1")))), pg_class pg_class0, pg_description pg_description0, pg_class pg_class1, pg_description pg_description1, pg_class pg_class2, pg_description pg_description2 WHERE (((((((((((("Map"."Status" = 'U'::bpchar) AND (pg_class1.oid = ("Map"."IdClass1")::oid)) AND (pg_class2.oid = ("Map"."IdClass2")::oid)) AND (pg_class0.oid = ("Map"."IdDomain")::oid)) AND (pg_description0.objoid = pg_class0.oid)) AND (pg_description0.objsubid = 0)) AND (pg_description1.objoid = pg_class1.oid)) AND (pg_description1.objsubid = 0)) AND (pg_description2.objoid = pg_class2.oid)) AND (pg_description2.objsubid = 0)) AND _cm_is_domain_comment(pg_description0.description)) AND _cm_is_active_comment(pg_description0.description));









INSERT INTO "Building" VALUES (64, '"Building"', 'DC', 'Data Center', 'A', 'admin', '2011-07-24 18:40:14.637', NULL, 'Main street 16', '58213', 'London', 25);
INSERT INTO "Building" VALUES (76, '"Building"', 'B2', 'Office Building B', 'A', 'admin', '2011-07-24 18:41:06.636', NULL, 'Liverpool Street 22', '12100', 'London', 25);
INSERT INTO "Building" VALUES (73, '"Building"', 'B1', 'Office Building A', 'A', 'admin', '2011-07-24 18:41:12.996', NULL, 'Liverpool Street 18', '12100', 'London', 25);



INSERT INTO "Building_history" VALUES (71, '"Building"', 'Data Center', 'Data Center', 'U', 'admin', '2011-07-24 18:28:28.63', NULL, 'Main street 16', '58213', 'London', NULL, 64, '2011-07-24 18:35:47.898');
INSERT INTO "Building_history" VALUES (74, '"Building"', 'Data Center', 'Data Center', 'U', 'admin', '2011-07-24 18:35:47.898', NULL, 'Main street 16', '58213', 'London', 25, 64, '2011-07-24 18:40:14.637');
INSERT INTO "Building_history" VALUES (77, '"Building"', 'B1', 'Office Building A', 'U', 'admin', '2011-07-24 18:40:06.618', NULL, 'Liverpool street 18', '12100', 'London', 25, 73, '2011-07-24 18:41:12.996');















INSERT INTO "Employee" VALUES (134, '"Employee"', '10', 'Taylor William', 'A', 'admin', '2011-07-24 23:35:18.412', NULL, 'Taylor', 'William', 21, 22, 146, 'william.taylor@gmail.com', 108, '23456', '763477', '', 24);
INSERT INTO "Employee" VALUES (118, '"Employee"', '02', 'Johnson Mary', 'A', 'admin', '2011-07-24 23:36:23.281', NULL, 'Johnson', 'Mary', 21, 147, 23, 'mary.johnson@gmail.com', 108, '76543', '9876554', '', 24);
INSERT INTO "Employee" VALUES (124, '"Employee"', '05', 'Brown Robert', 'A', 'admin', '2011-07-24 23:43:44.824', NULL, 'Brown', 'Robert', 149, 22, 146, 'robert.brown@gmail.com', 110, '65432', '24555556', '', 152);
INSERT INTO "Employee" VALUES (122, '"Employee"', '04', 'Jones Patricia', 'A', 'admin', '2011-07-24 23:45:11.466', NULL, 'Jones', 'Patricia', 21, 148, 145, 'patricia.jones@gmail.com', 112, '76543', '45678889', '', 24);
INSERT INTO "Employee" VALUES (132, '"Employee"', '09', 'Moore Elizabeth', 'A', 'admin', '2011-07-24 23:45:30.27', NULL, 'Moore', 'Elizabeth', 149, 22, 146, 'elizabeth.moore@gmail.com', 110, '76545', '2345666', '', 151);
INSERT INTO "Employee" VALUES (126, '"Employee"', '06', 'Davis Michael', 'A', 'admin', '2011-07-24 23:46:29.744', NULL, 'Davis', 'Michael', 21, 147, 23, 'michael.davis@gmail.com', 110, '45556', '3567789', '', 24);
INSERT INTO "Employee" VALUES (130, '"Employee"', '08', 'Wilson Barbara', 'A', 'admin', '2011-07-24 23:47:15.594', NULL, 'Wilson', 'Barbara', 21, 147, 146, 'barbara.wilson@gmail.com', 112, '644353', '7789999', '', 151);
INSERT INTO "Employee" VALUES (128, '"Employee"', '07', 'Miller Linda', 'A', 'admin', '2011-07-24 23:48:03.801', NULL, 'Miller', 'Linda', 21, 147, 23, 'linda.miller@gmail.com', 108, '5757578', '686868686', '', 24);
INSERT INTO "Employee" VALUES (120, '"Employee"', '03', 'Williams John', 'A', 'admin', '2011-07-24 23:48:45.557', NULL, 'Williams', 'John', 150, 22, 146, 'john.williams@gmail.com', 108, '64646', '56868768', '', 24);
INSERT INTO "Employee" VALUES (116, '"Employee"', '01', 'Smith James', 'A', 'admin', '2011-07-24 23:49:33.373', NULL, 'Smith', 'James', 149, 22, 146, 'james.smith@gmail.com', 112, '565675', '27575678', '', 24);



INSERT INTO "Employee_history" VALUES (164, '"Employee"', '10', 'Taylor William', 'U', 'admin', '2011-07-24 19:04:25.125', NULL, 'Taylor', 'William', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 134, '2011-07-24 23:35:18.412');
INSERT INTO "Employee_history" VALUES (167, '"Employee"', '02', 'Johnson Mary', 'U', 'admin', '2011-07-24 18:55:41.127', NULL, 'Johnson', 'Mary', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 118, '2011-07-24 23:36:23.281');
INSERT INTO "Employee_history" VALUES (171, '"Employee"', '09', 'Moore Elizabeth', 'U', 'admin', '2011-07-24 19:03:30.275', NULL, 'Moore', 'Elizabeth', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 132, '2011-07-24 23:40:39.563');
INSERT INTO "Employee_history" VALUES (174, '"Employee"', '05', 'Brown Robert', 'U', 'admin', '2011-07-24 18:56:57.522', NULL, 'Brown', 'Robert', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 124, '2011-07-24 23:43:44.824');
INSERT INTO "Employee_history" VALUES (177, '"Employee"', '04', 'Jones Patricia', 'U', 'admin', '2011-07-24 18:56:41.314', NULL, 'Jones', 'Patricia', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 122, '2011-07-24 23:45:11.466');
INSERT INTO "Employee_history" VALUES (180, '"Employee"', '09', 'Moore Elizabeth', 'U', 'admin', '2011-07-24 23:40:39.563', NULL, 'Moore', 'Elizabeth', 149, 22, 146, 'elizabeth.moore@gmail.com', 110, '76545', '2345666', '', NULL, 132, '2011-07-24 23:45:30.27');
INSERT INTO "Employee_history" VALUES (181, '"Employee"', '06', 'Davis Michael', 'U', 'admin', '2011-07-24 19:01:57.725', NULL, 'Davis', 'Michael', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 126, '2011-07-24 23:46:29.744');
INSERT INTO "Employee_history" VALUES (184, '"Employee"', '08', 'Wilson Barbara', 'U', 'admin', '2011-07-24 19:03:05.826', NULL, 'Wilson', 'Barbara', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 130, '2011-07-24 23:47:15.594');
INSERT INTO "Employee_history" VALUES (187, '"Employee"', '07', 'Miller Linda', 'U', 'admin', '2011-07-24 19:02:43.379', NULL, 'Miller', 'Linda', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 128, '2011-07-24 23:48:03.801');
INSERT INTO "Employee_history" VALUES (190, '"Employee"', '03', 'Williams John', 'U', 'admin', '2011-07-24 18:56:16.778', NULL, 'Williams', 'John', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 120, '2011-07-24 23:48:45.557');
INSERT INTO "Employee_history" VALUES (193, '"Employee"', '01', 'Smith James', 'U', 'admin', '2011-07-24 18:54:06.251', NULL, 'Smith', 'James', NULL, NULL, NULL, '', NULL, '', '', '', NULL, 116, '2011-07-24 23:49:33.373');



INSERT INTO "Floor" VALUES (79, '"Floor"', 'DC01', 'Data Center - Floor 1', 'A', 'admin', '2011-07-24 18:42:21.976', NULL, 64);
INSERT INTO "Floor" VALUES (87, '"Floor"', 'B102', 'Office Building A - Floor 2', 'A', 'admin', '2011-07-24 18:43:43.349', NULL, 73);
INSERT INTO "Floor" VALUES (83, '"Floor"', 'B101', 'Office Building A - Floor 1', 'A', 'admin', '2011-07-24 18:43:49.308', NULL, 73);
INSERT INTO "Floor" VALUES (92, '"Floor"', 'B103', 'Office Building A - Floor 3', 'A', 'admin', '2011-07-24 18:44:07.204', NULL, 73);
INSERT INTO "Floor" VALUES (96, '"Floor"', 'B201', 'Office Building B - Floor 1', 'A', 'admin', '2011-07-24 18:44:21.333', NULL, 76);
INSERT INTO "Floor" VALUES (100, '"Floor"', 'B202', 'Office Building B - Floor 2', 'A', 'admin', '2011-07-24 18:44:39.015', NULL, 76);



INSERT INTO "Floor_history" VALUES (90, '"Floor"', 'B101', 'Office Building - Floor 1', 'U', 'admin', '2011-07-24 18:43:05.005', NULL, 73, 83, '2011-07-24 18:43:49.308');



INSERT INTO "Grant" VALUES (684, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:39.184', NULL, 677, '"Asset"', 'w');
INSERT INTO "Grant" VALUES (685, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:40.023', NULL, 677, '"Building"', 'r');
INSERT INTO "Grant" VALUES (686, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:42.286', NULL, 677, '"Computer"', 'w');
INSERT INTO "Grant" VALUES (687, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:43.741', NULL, 677, '"Employee"', 'r');
INSERT INTO "Grant" VALUES (688, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:46.229', NULL, 677, '"Floor"', 'r');
INSERT INTO "Grant" VALUES (690, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:52.557', NULL, 677, '"License"', 'w');
INSERT INTO "Grant" VALUES (691, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:53.246', NULL, 677, '"Monitor"', 'w');
INSERT INTO "Grant" VALUES (692, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:54.607', NULL, 677, '"NetworkDevice"', 'w');
INSERT INTO "Grant" VALUES (693, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:55.341', NULL, 677, '"NetworkPoint"', 'w');
INSERT INTO "Grant" VALUES (694, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:56.333', NULL, 677, '"Notebook"', 'w');
INSERT INTO "Grant" VALUES (695, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:59.19', NULL, 677, '"Office"', 'r');
INSERT INTO "Grant" VALUES (689, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:45:50.334', NULL, 677, '"Invoice"', '-');
INSERT INTO "Grant" VALUES (696, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:46:08.549', NULL, 677, '"PC"', 'r');
INSERT INTO "Grant" VALUES (697, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:46:10.981', NULL, 677, '"Printer"', 'w');
INSERT INTO "Grant" VALUES (698, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:46:12.422', NULL, 677, '"Rack"', 'w');
INSERT INTO "Grant" VALUES (699, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:46:14.145', NULL, 677, '"Room"', 'r');
INSERT INTO "Grant" VALUES (700, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:46:15.638', NULL, 677, '"Server"', 'w');
INSERT INTO "Grant" VALUES (701, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:46:23.485', NULL, 677, '"UPS"', 'w');
INSERT INTO "Grant" VALUES (702, '"Grant"', NULL, NULL, 'A', 'system', '2011-08-23 22:46:24.318', NULL, 677, '"Workplace"', 'r');
INSERT INTO "Grant" VALUES (1136, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:01.026', NULL, 942, '"Asset"', 'w');
INSERT INTO "Grant" VALUES (1137, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:01.694', NULL, 942, '"Building"', 'w');
INSERT INTO "Grant" VALUES (1138, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:02.165', NULL, 942, '"Computer"', 'w');
INSERT INTO "Grant" VALUES (1139, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:02.597', NULL, 942, '"Employee"', 'w');
INSERT INTO "Grant" VALUES (1140, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:03.198', NULL, 942, '"Floor"', 'w');
INSERT INTO "Grant" VALUES (1141, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:03.653', NULL, 942, '"Invoice"', 'w');
INSERT INTO "Grant" VALUES (1142, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:04.068', NULL, 942, '"License"', 'w');
INSERT INTO "Grant" VALUES (1143, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:04.484', NULL, 942, '"Monitor"', 'w');
INSERT INTO "Grant" VALUES (1144, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:04.919', NULL, 942, '"NetworkDevice"', 'w');
INSERT INTO "Grant" VALUES (1145, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:05.292', NULL, 942, '"NetworkPoint"', 'w');
INSERT INTO "Grant" VALUES (1146, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:05.645', NULL, 942, '"Notebook"', 'w');
INSERT INTO "Grant" VALUES (1147, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:06.348', NULL, 942, '"Office"', 'w');
INSERT INTO "Grant" VALUES (1148, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:06.764', NULL, 942, '"PC"', 'w');
INSERT INTO "Grant" VALUES (1149, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:07.308', NULL, 942, '"Printer"', 'w');
INSERT INTO "Grant" VALUES (1150, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:07.733', NULL, 942, '"Rack"', 'w');
INSERT INTO "Grant" VALUES (1151, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:08.182', NULL, 942, '"RequestForChange"', 'w');
INSERT INTO "Grant" VALUES (1152, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:08.581', NULL, 942, '"Room"', 'w');
INSERT INTO "Grant" VALUES (1153, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:08.98', NULL, 942, '"Server"', 'w');
INSERT INTO "Grant" VALUES (1154, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:09.356', NULL, 942, '"Supplier"', 'w');
INSERT INTO "Grant" VALUES (1155, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:10.191', NULL, 942, '"SupplierContact"', 'w');
INSERT INTO "Grant" VALUES (1156, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:10.524', NULL, 942, '"UPS"', 'w');
INSERT INTO "Grant" VALUES (1157, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:11.228', NULL, 942, '"User"', 'w');
INSERT INTO "Grant" VALUES (1158, '"Grant"', NULL, NULL, 'A', 'system', '2012-08-24 11:02:11.525', NULL, 942, '"Workplace"', 'w');















INSERT INTO "LookUp" VALUES (1, '"LookUp"', NULL, 'New', 'A', NULL, '2011-03-16 11:15:25.057396', NULL, 'EmailStatus', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (2, '"LookUp"', NULL, 'Received', 'A', NULL, '2011-03-16 11:15:25.057396', NULL, 'EmailStatus', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (3, '"LookUp"', NULL, 'Draft', 'A', NULL, '2011-03-16 11:15:25.057396', NULL, 'EmailStatus', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (4, '"LookUp"', NULL, 'Outgoing', 'A', NULL, '2011-03-16 11:15:25.057396', NULL, 'EmailStatus', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (5, '"LookUp"', NULL, 'Sent', 'A', NULL, '2011-03-16 11:15:25.057396', NULL, 'EmailStatus', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (6, '"LookUp"', 'open.running', 'Avviato', 'A', NULL, '2011-03-16 11:15:27.503898', NULL, 'FlowStatus', NULL, NULL, 1, true);
INSERT INTO "LookUp" VALUES (7, '"LookUp"', 'open.not_running.suspended', 'Sospeso', 'A', NULL, '2011-03-16 11:15:27.503898', NULL, 'FlowStatus', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (8, '"LookUp"', 'closed.completed', 'Completato', 'A', NULL, '2011-03-16 11:15:27.503898', NULL, 'FlowStatus', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (9, '"LookUp"', 'closed.terminated', 'Terminato', 'A', NULL, '2011-03-16 11:15:27.503898', NULL, 'FlowStatus', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (10, '"LookUp"', 'closed.aborted', 'Interrotto', 'A', NULL, '2011-03-16 11:15:27.503898', NULL, 'FlowStatus', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (330, '"LookUp"', NULL, '15 inches', 'A', NULL, '2011-08-10 12:51:17.832', NULL, 'Screen size', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (25, '"LookUp"', 'EN', 'England', 'A', NULL, '2011-07-22 16:14:25.018', '', 'Country', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (65, '"LookUp"', 'IT', 'Italy', 'A', NULL, '2011-07-24 18:30:46.348', '', 'Country', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (66, '"LookUp"', 'DE', 'Germany', 'A', NULL, '2011-07-24 18:31:36.096', '', 'Country', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (67, '"LookUp"', 'FR', 'France', 'A', NULL, '2011-07-24 18:32:31.507', '', 'Country', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (68, '"LookUp"', 'ES', 'Spain', 'A', NULL, '2011-07-24 18:33:04.705', '', 'Country', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (69, '"LookUp"', 'US', 'United States', 'A', NULL, '2011-07-24 18:33:40.414', '', 'Country', NULL, NULL, 6, false);
INSERT INTO "LookUp" VALUES (70, '"LookUp"', 'AT', 'Austria', 'A', NULL, '2011-07-24 18:34:20.568', '', 'Country', NULL, NULL, 7, false);
INSERT INTO "LookUp" VALUES (31, '"LookUp"', '', 'IBM', 'A', NULL, '2011-07-22 17:18:15.301', '', 'Brand', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (135, '"LookUp"', '', 'HP', 'A', NULL, '2011-07-24 23:05:55.228', '', 'Brand', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (136, '"LookUp"', '', 'Sony', 'A', NULL, '2011-07-24 23:06:03.174', '', 'Brand', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (137, '"LookUp"', '', 'Cisco', 'A', NULL, '2011-07-24 23:06:08.352', '', 'Brand', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (138, '"LookUp"', '', 'Acer', 'A', NULL, '2011-07-24 23:07:09.511', '', 'Brand', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (139, '"LookUp"', '', 'Canon', 'A', NULL, '2011-07-24 23:07:16.833', '', 'Brand', NULL, NULL, 6, false);
INSERT INTO "LookUp" VALUES (140, '"LookUp"', '', 'Epson', 'A', NULL, '2011-07-24 23:07:26.466', '', 'Brand', NULL, NULL, 7, false);
INSERT INTO "LookUp" VALUES (141, '"LookUp"', '', 'Microsoft', 'A', NULL, '2011-07-24 23:08:28.5', '', 'Brand', NULL, NULL, 8, false);
INSERT INTO "LookUp" VALUES (30, '"LookUp"', '', 'In use', 'A', NULL, '2011-07-22 17:18:01.123', '', 'Asset state', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (142, '"LookUp"', '', 'To repair', 'A', NULL, '2011-07-24 23:11:03.069', '', 'Asset state', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (143, '"LookUp"', '', 'Scrapped', 'A', NULL, '2011-07-24 23:11:29.004', '', 'Asset state', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (144, '"LookUp"', '', 'Available', 'A', NULL, '2011-07-24 23:12:02.806', '', 'Asset state', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (23, '"LookUp"', '', 'Gold', 'A', NULL, '2011-07-22 13:26:03.44', '', 'Employee level', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (145, '"LookUp"', '', 'Platinum', 'A', NULL, '2011-07-24 23:14:09.353', '', 'Employee level', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (146, '"LookUp"', '', 'Silver', 'A', NULL, '2011-07-24 23:14:14.316', '', 'Employee level', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (22, '"LookUp"', '', 'Clerk', 'A', NULL, '2011-07-22 13:25:24.554', '', 'Employee qualification', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (147, '"LookUp"', '', 'Head office', 'A', NULL, '2011-07-24 23:16:13.323', '', 'Employee qualification', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (148, '"LookUp"', '', 'Manager', 'A', NULL, '2011-07-24 23:16:42.143', '', 'Employee qualification', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (21, '"LookUp"', '', 'Employee', 'A', NULL, '2011-07-22 13:24:18.278', '', 'Employee type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (149, '"LookUp"', '', 'External consultant', 'A', NULL, '2011-07-24 23:17:39.293', '', 'Employee type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (150, '"LookUp"', '', 'Stage', 'A', NULL, '2011-07-24 23:18:48.401', '', 'Employee type', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (24, '"LookUp"', '', 'Active', 'A', NULL, '2011-07-22 13:26:50.947', '', 'Employee state', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (151, '"LookUp"', '', 'Inactive', 'A', NULL, '2011-07-24 23:20:32.346', '', 'Employee state', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (152, '"LookUp"', '', 'Suspended', 'A', NULL, '2011-07-24 23:20:44.553', '', 'Employee state', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (331, '"LookUp"', NULL, '17 inches', 'A', NULL, '2011-08-10 12:51:49.421', NULL, 'Screen size', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (26, '"LookUp"', '', 'Sales', 'A', NULL, '2011-07-22 16:29:41.332', '', 'Invoice type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (153, '"LookUp"', '', 'Credit memo', 'A', NULL, '2011-07-24 23:21:34.255', '', 'Invoice type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (27, '"LookUp"', '', 'Office', 'A', NULL, '2011-07-22 16:53:21.211', '', 'Room usage type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (154, '"LookUp"', '', 'Warehouse', 'A', NULL, '2011-07-24 23:27:33.024', '', 'Room usage type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (155, '"LookUp"', '', 'Meeting room', 'A', NULL, '2011-07-24 23:27:56.837', '', 'Room usage type', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (156, '"LookUp"', '', 'Training room', 'A', NULL, '2011-07-24 23:28:51.596', '', 'Room usage type', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (157, '"LookUp"', '', 'Laboratory', 'A', NULL, '2011-07-24 23:29:09.958', '', 'Room usage type', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (28, '"LookUp"', '', 'Manufacturer', 'A', NULL, '2011-07-22 16:57:35.695', '', 'Supplier type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (158, '"LookUp"', '', 'Distributor', 'A', NULL, '2011-07-24 23:30:40.294', '', 'Supplier type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (32, '"LookUp"', '', 'Hardware', 'A', NULL, '2011-07-22 17:27:40.27', '', 'Technical reference role', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (159, '"LookUp"', '', 'Systemistic', 'A', NULL, '2011-07-24 23:31:31.02', '', 'Technical reference role', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (160, '"LookUp"', '', 'Applicative', 'A', NULL, '2011-07-24 23:31:52.712', '', 'Technical reference role', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (161, '"LookUp"', '', 'Security', 'A', NULL, '2011-07-24 23:32:14.583', '', 'Technical reference role', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (29, '"LookUp"', '', 'Single user', 'A', NULL, '2011-07-22 17:06:16.706', '', 'Workplace type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (162, '"LookUp"', '', 'Multiuser', 'A', NULL, '2011-07-24 23:32:53.643', '', 'Workplace type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (163, '"LookUp"', '', 'Public', 'A', NULL, '2011-07-24 23:33:31.912', '', 'Workplace type', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (279, '"LookUp"', 'CA', 'Canada', 'A', NULL, '2011-08-01 00:16:00.92', NULL, 'Country', NULL, NULL, 8, false);
INSERT INTO "LookUp" VALUES (327, '"LookUp"', NULL, 'RAID 1', 'A', NULL, '2011-08-10 12:47:58.52', NULL, 'RAID', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (328, '"LookUp"', NULL, 'RAID 2', 'A', NULL, '2011-08-10 12:48:15.145', NULL, 'RAID', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (329, '"LookUp"', NULL, 'RAID 5', 'A', NULL, '2011-08-10 12:48:22.138', NULL, 'RAID', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (332, '"LookUp"', NULL, '19 inches', 'A', NULL, '2011-08-10 12:51:54.889', NULL, 'Screen size', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (333, '"LookUp"', NULL, '16 inches', 'A', NULL, '2011-08-10 12:52:00.662', NULL, 'Screen size', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (334, '"LookUp"', NULL, '13 inches', 'A', NULL, '2011-08-10 12:52:13.899', NULL, 'Screen size', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (335, '"LookUp"', NULL, '21 inches', 'A', NULL, '2011-08-10 12:52:23.554', NULL, 'Screen size', NULL, NULL, 6, false);
INSERT INTO "LookUp" VALUES (393, '"LookUp"', '2', 'CRT', 'A', NULL, '2011-08-10 14:49:48.263', NULL, 'Monitor type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (395, '"LookUp"', NULL, 'A4', 'A', NULL, '2011-08-10 15:09:43.103', NULL, 'Paper size', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (396, '"LookUp"', NULL, 'A3', 'A', NULL, '2011-08-10 15:09:56.059', NULL, 'Paper size', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (397, '"LookUp"', NULL, 'A0', 'A', NULL, '2011-08-10 15:10:02.023', NULL, 'Paper size', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (398, '"LookUp"', NULL, 'Laser', 'A', NULL, '2011-08-10 15:10:16.39', NULL, 'Printer type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (399, '"LookUp"', NULL, 'Inkjet', 'A', NULL, '2011-08-10 15:10:32.346', NULL, 'Printer type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (400, '"LookUp"', NULL, 'Thermal', 'A', NULL, '2011-08-10 15:11:04.536', NULL, 'Printer type', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (401, '"LookUp"', NULL, 'Impact', 'A', NULL, '2011-08-10 15:12:02.035', NULL, 'Printer type', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (402, '"LookUp"', NULL, 'Plotter', 'A', NULL, '2011-08-10 15:12:07.47', NULL, 'Printer type', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (403, '"LookUp"', NULL, 'Local', 'A', NULL, '2011-08-10 15:14:24.402', NULL, 'Printer usage', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (404, '"LookUp"', NULL, 'Network', 'A', NULL, '2011-08-10 15:14:58.444', NULL, 'Printer usage', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (405, '"LookUp"', NULL, 'Personal productivity software', 'A', NULL, '2011-08-10 15:17:53.016', NULL, 'License category', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (406, '"LookUp"', NULL, 'Enterprise software', 'A', NULL, '2011-08-10 15:23:54.486', NULL, 'License category', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (407, '"LookUp"', NULL, 'Technical software', 'A', NULL, '2011-08-10 15:24:07.238', NULL, 'License category', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (408, '"LookUp"', NULL, 'Router', 'A', NULL, '2011-08-10 15:29:48.467', NULL, 'Network device type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (409, '"LookUp"', NULL, 'Switch', 'A', NULL, '2011-08-10 15:30:13.078', NULL, 'Network device type', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (410, '"LookUp"', NULL, 'Access point', 'A', NULL, '2011-08-10 15:30:25.474', NULL, 'Network device type', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (394, '"LookUp"', '3', 'Plasma', 'A', NULL, '2011-08-10 14:50:22.995', NULL, 'Monitor type', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (411, '"LookUp"', NULL, 'Patch panel', 'A', NULL, '2011-08-10 15:30:56.609', NULL, 'Network device type', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (482, '"LookUp"', NULL, 'Red', 'A', NULL, '2011-08-10 15:41:41.655', NULL, 'Cable color', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (483, '"LookUp"', NULL, 'Black', 'A', NULL, '2011-08-10 15:41:59.826', NULL, 'Cable color', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (484, '"LookUp"', NULL, 'White', 'A', NULL, '2011-08-10 15:42:04.579', NULL, 'Cable color', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (485, '"LookUp"', NULL, 'Yellow', 'A', NULL, '2011-08-10 15:42:10.843', NULL, 'Cable color', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (486, '"LookUp"', NULL, 'Blue', 'A', NULL, '2011-08-10 15:42:49.445', NULL, 'Cable color', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (487, '"LookUp"', NULL, 'Green', 'A', NULL, '2011-08-10 15:42:57.692', NULL, 'Cable color', NULL, NULL, 6, false);
INSERT INTO "LookUp" VALUES (488, '"LookUp"', NULL, 'Cyan', 'A', NULL, '2011-08-10 15:43:28.67', NULL, 'Cable color', NULL, NULL, 7, false);
INSERT INTO "LookUp" VALUES (489, '"LookUp"', NULL, 'Brown', 'A', NULL, '2011-08-10 15:43:39.476', NULL, 'Cable color', NULL, NULL, 8, false);
INSERT INTO "LookUp" VALUES (490, '"LookUp"', NULL, 'Gray', 'A', NULL, '2011-08-10 15:43:47.898', NULL, 'Cable color', NULL, NULL, 9, false);
INSERT INTO "LookUp" VALUES (491, '"LookUp"', NULL, 'Orange', 'A', NULL, '2011-08-10 15:44:02.059', NULL, 'Cable color', NULL, NULL, 10, false);
INSERT INTO "LookUp" VALUES (492, '"LookUp"', NULL, 'Pink', 'A', NULL, '2011-08-10 15:44:10.968', NULL, 'Cable color', NULL, NULL, 11, false);
INSERT INTO "LookUp" VALUES (493, '"LookUp"', NULL, 'Magenta', 'A', NULL, '2011-08-10 15:44:32.391', NULL, 'Cable color', NULL, NULL, 12, false);
INSERT INTO "LookUp" VALUES (392, '"LookUp"', '1', 'LCD', 'A', NULL, '2011-08-10 14:49:35.013', NULL, 'Monitor type', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (703, '"LookUp"', NULL, 'Document', 'A', NULL, '2011-08-23 23:01:52.029', NULL, 'AlfrescoCategory', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (704, '"LookUp"', NULL, 'Image', 'A', NULL, '2011-08-23 23:02:08.292', NULL, 'AlfrescoCategory', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (917, '"LookUp"', 'REC_RFC', 'Registered', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC status', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (920, '"LookUp"', 'REQ_EXE', 'Execution requested', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC status', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (921, '"LookUp"', 'IN_EXE', 'Implementation', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC status', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (922, '"LookUp"', 'OUT_EXE', 'Performed', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC status', NULL, NULL, 6, false);
INSERT INTO "LookUp" VALUES (923, '"LookUp"', 'CLOSED', 'Closed', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC status', NULL, NULL, 7, false);
INSERT INTO "LookUp" VALUES (924, '"LookUp"', 'FPC', 'Formatting PC', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC Category', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (925, '"LookUp"', 'ISE', 'External software installation', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC Category', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (926, '"LookUp"', 'ARI', 'Internet access', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC Category', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (927, '"LookUp"', 'MIR', 'Modify IP address', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC Category', NULL, NULL, 4, false);
INSERT INTO "LookUp" VALUES (928, '"LookUp"', 'NU_ERP', 'Create new ERP user', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC Category', NULL, NULL, 5, false);
INSERT INTO "LookUp" VALUES (929, '"LookUp"', 'NU_CRM', 'Create new CRM user', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC Category', NULL, NULL, 6, false);
INSERT INTO "LookUp" VALUES (930, '"LookUp"', 'NA', 'Not applicable', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC Category', NULL, NULL, 7, false);
INSERT INTO "LookUp" VALUES (931, '"LookUp"', 'HI', 'High', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC priority', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (932, '"LookUp"', 'MID', 'Medium', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC priority', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (933, '"LookUp"', 'LOW', 'Low', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC priority', NULL, NULL, 3, false);
INSERT INTO "LookUp" VALUES (934, '"LookUp"', 'ACCEPTED', 'Accepted', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC formal evaluation', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (935, '"LookUp"', 'REJECTED', 'Rejected', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC formal evaluation', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (936, '"LookUp"', 'APPROVED', 'Approved', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC decision', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (937, '"LookUp"', 'NOT_APPROVED', 'Not approved', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC decision', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (938, '"LookUp"', 'POSITIVE', 'Positive', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC final result', NULL, NULL, 1, false);
INSERT INTO "LookUp" VALUES (939, '"LookUp"', 'NEGATIVE', 'Negative', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC final result', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (918, '"LookUp"', 'REQ_DOC', 'Analysis requested', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC status', NULL, NULL, 2, false);
INSERT INTO "LookUp" VALUES (919, '"LookUp"', 'PRE_DOC', 'Analysis in progress', 'A', NULL, '2012-08-24 10:22:41.248', NULL, 'RFC status', NULL, NULL, 3, false);












INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 120, '"PC"', 518, 'A', 'admin', '2011-08-23 17:26:13.647', NULL, 520);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 126, '"PC"', 526, 'A', 'admin', '2011-08-23 17:28:42.292', NULL, 528);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 128, '"PC"', 534, 'A', 'admin', '2011-08-23 17:29:52.21', NULL, 536);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 130, '"PC"', 542, 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 544);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 134, '"Monitor"', 550, 'A', 'admin', '2011-08-23 17:34:12.416', NULL, 553);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 128, '"Monitor"', 555, 'A', 'admin', '2011-08-23 17:35:03.944', NULL, 557);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 130, '"Monitor"', 561, 'A', 'admin', '2011-08-23 17:36:00.497', NULL, 563);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 118, '"Monitor"', 567, 'A', 'admin', '2011-08-23 17:36:50.525', NULL, 569);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 132, '"Monitor"', 573, 'A', 'admin', '2011-08-23 17:37:57.173', NULL, 575);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 130, '"Printer"', 579, 'A', 'admin', '2011-08-23 17:38:55.033', NULL, 581);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 120, '"Printer"', 585, 'A', 'admin', '2011-08-23 17:39:42.706', NULL, 587);
INSERT INTO "Map_AssetAssignee" VALUES ('"Map_AssetAssignee"', '"Employee"', 122, '"Printer"', 591, 'A', 'admin', '2011-08-23 17:40:48.481', NULL, 593);






INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 526, 'A', 'admin', '2011-08-23 17:28:42.292', NULL, 532, NULL);
INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 134, '"PC"', 534, 'A', 'admin', '2011-08-23 17:29:52.21', NULL, 540, NULL);
INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 542, 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 548, NULL);
INSERT INTO "Map_AssetReference" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 518, 'A', 'admin', '2011-08-29 12:37:39.83', NULL, 524, 32);



INSERT INTO "Map_AssetReference_history" VALUES ('"Map_AssetReference"', '"Employee"', 116, '"PC"', 518, 'U', 'admin', '2011-08-23 17:26:13.647', '2011-08-29 12:37:39.83', 524, NULL);



INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 64, '"Floor"', 79, 'A', 'admin', '2011-07-24 18:42:21.976', NULL, 81);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 73, '"Floor"', 83, 'A', 'admin', '2011-07-24 18:43:05.005', NULL, 85);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 73, '"Floor"', 87, 'A', 'admin', '2011-07-24 18:43:43.349', NULL, 89);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 73, '"Floor"', 92, 'A', 'admin', '2011-07-24 18:44:07.204', NULL, 94);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 76, '"Floor"', 96, 'A', 'admin', '2011-07-24 18:44:21.333', NULL, 98);
INSERT INTO "Map_BuildingFloor" VALUES ('"Map_BuildingFloor"', '"Building"', 76, '"Floor"', 100, 'A', 'admin', '2011-07-24 18:44:39.015', NULL, 102);






INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 79, '"Room"', 104, 'A', 'admin', '2011-07-24 18:45:44.718', NULL, 106);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 79, '"Room"', 200, 'A', 'admin', '2011-07-24 23:51:13.304', NULL, 202);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 83, '"Room"', 206, 'A', 'admin', '2011-07-24 23:56:14.609', NULL, 208);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 83, '"Room"', 212, 'A', 'admin', '2011-07-24 23:56:56.466', NULL, 214);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 83, '"Room"', 218, 'A', 'admin', '2011-07-24 23:57:24.774', NULL, 220);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 87, '"Room"', 224, 'A', 'admin', '2011-07-24 23:57:56.042', NULL, 226);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 87, '"Room"', 230, 'A', 'admin', '2011-07-24 23:58:29.941', NULL, 232);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 92, '"Room"', 236, 'A', 'admin', '2011-07-24 23:59:12.074', NULL, 238);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 96, '"Room"', 242, 'A', 'admin', '2011-07-24 23:59:40.137', NULL, 244);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 96, '"Room"', 248, 'A', 'admin', '2011-07-25 00:00:13.196', NULL, 250);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 96, '"Room"', 254, 'A', 'admin', '2011-07-25 00:00:42.222', NULL, 256);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 100, '"Room"', 260, 'A', 'admin', '2011-07-25 00:01:29.684', NULL, 262);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 100, '"Room"', 266, 'A', 'admin', '2011-07-25 00:01:52.818', NULL, 268);
INSERT INTO "Map_FloorRoom" VALUES ('"Map_FloorRoom"', '"Floor"', 100, '"Room"', 272, 'A', 'admin', '2011-07-25 00:02:19.16', NULL, 274);






INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 134, 'A', 'admin', '2011-07-24 23:35:18.412', NULL, 166);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 118, 'A', 'admin', '2011-07-24 23:36:23.281', NULL, 169);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 110, '"Employee"', 132, 'A', 'admin', '2011-07-24 23:40:39.563', NULL, 173);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 110, '"Employee"', 124, 'A', 'admin', '2011-07-24 23:43:44.824', NULL, 176);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 112, '"Employee"', 122, 'A', 'admin', '2011-07-24 23:45:11.466', NULL, 179);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 110, '"Employee"', 126, 'A', 'admin', '2011-07-24 23:46:29.744', NULL, 183);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 112, '"Employee"', 130, 'A', 'admin', '2011-07-24 23:47:15.594', NULL, 186);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 128, 'A', 'admin', '2011-07-24 23:48:03.801', NULL, 189);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 108, '"Employee"', 120, 'A', 'admin', '2011-07-24 23:48:45.557', NULL, 192);
INSERT INTO "Map_Members" VALUES ('"Map_Members"', '"Office"', 112, '"Employee"', 116, 'A', 'admin', '2011-07-24 23:49:33.373', NULL, 195);






INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 12:18:14.794', NULL, 761, 7, 490);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 12:19:16.945', NULL, 765, 4, 492);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 15:15:53.993', NULL, 767, 5, 492);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'N', 'admin', '2011-09-02 15:17:32.047', NULL, 769, 3, 489);
INSERT INTO "Map_NetworkDeviceConnection" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'A', 'admin', '2011-09-02 15:17:43.319', NULL, 771, 5, 490);



INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 12:10:10.378', '2011-09-02 12:12:14.952', 761, 4, 487);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:12:14.952', '2011-09-02 12:17:42.029', 761, 5, 487);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:17:42.029', '2011-09-02 12:18:14.794', 761, 7, 490);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 12:18:31.48', '2011-09-02 12:18:39.058', 765, 3, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:18:39.058', '2011-09-02 12:19:16.945', 765, 4, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 12:20:29.104', '2011-09-02 12:20:40.731', 767, 4, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 12:20:40.731', '2011-09-02 15:15:53.993', 767, 5, 492);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 747, 'U', 'admin', '2011-09-02 15:16:37.924', '2011-09-02 15:16:57.895', 769, 4, 489);
INSERT INTO "Map_NetworkDeviceConnection_history" VALUES ('"Map_NetworkDeviceConnection"', '"NetworkDevice"', 755, '"NetworkDevice"', 755, 'U', 'admin', '2011-09-02 15:16:57.895', '2011-09-02 15:17:32.047', 769, 3, 489);



INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 104, 'A', 'admin', '2011-07-24 23:50:09.333', NULL, 198);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 200, 'A', 'admin', '2011-07-24 23:51:13.304', NULL, 204);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 206, 'A', 'admin', '2011-07-24 23:56:14.609', NULL, 210);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 212, 'A', 'admin', '2011-07-24 23:56:56.466', NULL, 216);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 218, 'A', 'admin', '2011-07-24 23:57:24.774', NULL, 222);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 224, 'A', 'admin', '2011-07-24 23:57:56.042', NULL, 228);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 230, 'A', 'admin', '2011-07-24 23:58:29.941', NULL, 234);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 236, 'A', 'admin', '2011-07-24 23:59:12.074', NULL, 240);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 242, 'A', 'admin', '2011-07-24 23:59:40.137', NULL, 246);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 248, 'A', 'admin', '2011-07-25 00:00:13.196', NULL, 252);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 266, 'A', 'admin', '2011-07-25 00:01:52.818', NULL, 270);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 260, 'A', 'admin', '2011-09-02 11:53:26.9', NULL, 264);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 272, 'A', 'admin', '2011-09-02 11:54:54.974', NULL, 276);
INSERT INTO "Map_OfficeRoom" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 254, 'A', 'admin', '2011-09-02 11:56:58.957', NULL, 258);



INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 260, 'U', 'admin', '2011-07-25 00:01:29.684', '2011-09-02 11:53:26.9', 264);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 260, 'U', 'admin', '2011-09-02 11:53:26.9', '2011-09-02 11:53:26.9', 264);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 112, '"Room"', 272, 'U', 'admin', '2011-07-25 00:02:19.16', '2011-09-02 11:54:54.974', 276);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 272, 'U', 'admin', '2011-09-02 11:54:54.974', '2011-09-02 11:54:54.974', 276);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 108, '"Room"', 254, 'U', 'admin', '2011-07-25 00:00:42.222', '2011-09-02 11:56:58.957', 258);
INSERT INTO "Map_OfficeRoom_history" VALUES ('"Map_OfficeRoom"', '"Office"', 110, '"Room"', 254, 'U', 'admin', '2011-09-02 11:56:58.957', '2011-09-02 11:56:58.957', 258);





















INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 236, '"PC"', 518, 'A', 'admin', '2011-08-23 17:26:13.647', NULL, 522);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 248, '"PC"', 526, 'A', 'admin', '2011-08-23 17:28:42.292', NULL, 530);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 104, '"PC"', 534, 'A', 'admin', '2011-08-23 17:29:52.21', NULL, 538);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 272, '"PC"', 542, 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 546);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 272, '"Monitor"', 555, 'A', 'admin', '2011-08-23 17:35:03.944', NULL, 559);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 242, '"Monitor"', 561, 'A', 'admin', '2011-08-23 17:36:00.497', NULL, 565);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 230, '"Monitor"', 567, 'A', 'admin', '2011-08-23 17:36:50.525', NULL, 571);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 272, '"Monitor"', 573, 'A', 'admin', '2011-08-23 17:37:57.173', NULL, 577);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 242, '"Printer"', 579, 'A', 'admin', '2011-08-23 17:38:55.033', NULL, 583);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 212, '"Printer"', 585, 'A', 'admin', '2011-08-23 17:39:42.706', NULL, 589);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 266, '"Printer"', 591, 'A', 'admin', '2011-08-23 17:40:48.481', NULL, 595);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 200, '"NetworkDevice"', 747, 'A', 'admin', '2011-09-02 12:06:33.699', NULL, 749);
INSERT INTO "Map_RoomAsset" VALUES ('"Map_RoomAsset"', '"Room"', 104, '"NetworkDevice"', 755, 'A', 'admin', '2011-09-02 12:08:39.585', NULL, 757);
























INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 714, '"PC"', 526, 'N', 'admin', '2011-08-29 13:07:08.776', NULL, 717);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"NetworkDevice"', 747, 'A', 'admin', '2011-09-02 12:06:33.699', NULL, 751);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"NetworkDevice"', 755, 'A', 'admin', '2011-09-02 12:08:39.585', NULL, 759);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"PC"', 526, 'N', 'admin', '2012-08-25 12:39:36.099', NULL, 725);
INSERT INTO "Map_SupplierAsset" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"PC"', 526, 'A', 'admin', '2012-08-25 12:41:15.881', NULL, 1254);



INSERT INTO "Map_SupplierAsset_history" VALUES ('"Map_SupplierAsset"', '"Supplier"', 714, '"PC"', 526, 'U', 'admin', '2011-08-29 13:03:27.919', '2011-08-29 13:07:08.776', 717);
INSERT INTO "Map_SupplierAsset_history" VALUES ('"Map_SupplierAsset"', '"Supplier"', 723, '"PC"', 526, 'U', 'admin', '2011-08-29 13:27:49.732', '2012-08-25 12:39:36.099', 725);















INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 13, '"Role"', 14, 'A', 'system', '2011-03-16 11:15:37.266624', NULL, 16, NULL);
INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 678, '"Role"', 677, 'A', 'admin', '2011-08-23 22:41:46.419', NULL, 681, NULL);
INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 679, '"Role"', 677, 'A', 'admin', '2011-08-23 22:41:46.632', NULL, 683, NULL);
INSERT INTO "Map_UserRole" VALUES ('"Map_UserRole"', '"User"', 943, '"Role"', 942, 'A', 'admin', '2012-08-24 10:22:41.248', NULL, 945, NULL);












INSERT INTO "Menu" VALUES (34, '"Menu"', 'folder', 'Basic archives', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (36, '"Menu"', 'class', 'Employee', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 34, '"Employee"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (38, '"Menu"', 'class', 'Office', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 34, '"Office"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (40, '"Menu"', 'class', 'Workplace', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 34, '"Workplace"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (42, '"Menu"', 'folder', 'Purchases', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (44, '"Menu"', 'class', 'Supplier', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 42, '"Supplier"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (46, '"Menu"', 'class', 'SupplierContact', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 42, '"SupplierContact"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (48, '"Menu"', 'class', 'Invoice', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 42, '"Invoice"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (50, '"Menu"', 'folder', 'Locations', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (52, '"Menu"', 'class', 'Building', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 50, '"Building"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (54, '"Menu"', 'class', 'Room', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 50, '"Room"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (56, '"Menu"', 'class', 'Floor', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 50, '"Floor"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (58, '"Menu"', 'folder', 'Assets', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (60, '"Menu"', 'class', 'Asset', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 58, '"Asset"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (62, '"Menu"', 'class', 'Rack', 'N', 'system', '2011-08-10 12:40:07.301', NULL, 58, '"Rack"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (296, '"Menu"', 'folder', 'Basic archives', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (298, '"Menu"', 'class', 'Employee', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 296, '"Employee"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (300, '"Menu"', 'class', 'Office', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 296, '"Office"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (302, '"Menu"', 'class', 'Workplace', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 296, '"Workplace"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (304, '"Menu"', 'folder', 'Purchases', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (306, '"Menu"', 'class', 'Supplier', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 304, '"Supplier"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (308, '"Menu"', 'class', 'SupplierContact', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 304, '"SupplierContact"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (310, '"Menu"', 'class', 'Invoice', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 304, '"Invoice"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (312, '"Menu"', 'folder', 'Locations', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (314, '"Menu"', 'class', 'Building', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 312, '"Building"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (316, '"Menu"', 'class', 'Room', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 312, '"Room"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (318, '"Menu"', 'class', 'Floor', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 312, '"Floor"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (320, '"Menu"', 'class', 'Network point', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 312, '"NetworkPoint"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (322, '"Menu"', 'folder', 'Assets', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (324, '"Menu"', 'class', 'Asset', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 322, '"Asset"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (326, '"Menu"', 'class', 'Rack', 'N', 'system', '2011-08-10 12:54:25.886', NULL, 322, '"Rack"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (353, '"Menu"', 'folder', 'Basic archives', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (355, '"Menu"', 'class', 'Employee', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 353, '"Employee"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (357, '"Menu"', 'class', 'Office', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 353, '"Office"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (359, '"Menu"', 'class', 'Workplace', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 353, '"Workplace"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (361, '"Menu"', 'folder', 'Purchases', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (363, '"Menu"', 'class', 'Supplier', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 361, '"Supplier"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (365, '"Menu"', 'class', 'SupplierContact', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 361, '"SupplierContact"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (367, '"Menu"', 'class', 'Invoice', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 361, '"Invoice"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (369, '"Menu"', 'folder', 'Locations', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (371, '"Menu"', 'class', 'Building', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 369, '"Building"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (373, '"Menu"', 'class', 'Room', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 369, '"Room"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (375, '"Menu"', 'class', 'Floor', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 369, '"Floor"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (377, '"Menu"', 'class', 'Network point', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 369, '"NetworkPoint"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (379, '"Menu"', 'folder', 'Assets', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (381, '"Menu"', 'class', 'Asset', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 379, '"Asset"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (383, '"Menu"', 'class', 'Computer', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 379, '"Computer"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (385, '"Menu"', 'class', 'PC', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 379, '"PC"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (387, '"Menu"', 'class', 'Notebook', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 379, '"Notebook"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (389, '"Menu"', 'class', 'Server', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 379, '"Server"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (391, '"Menu"', 'class', 'Rack', 'N', 'system', '2011-08-10 15:33:56.677', NULL, 379, '"Rack"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (433, '"Menu"', 'folder', 'Basic archives', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (435, '"Menu"', 'class', 'Employee', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 433, '"Employee"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (437, '"Menu"', 'class', 'Office', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 433, '"Office"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (439, '"Menu"', 'class', 'Workplace', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 433, '"Workplace"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (441, '"Menu"', 'folder', 'Purchases', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (443, '"Menu"', 'class', 'Supplier', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 441, '"Supplier"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (445, '"Menu"', 'class', 'SupplierContact', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 441, '"SupplierContact"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (447, '"Menu"', 'class', 'Invoice', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 441, '"Invoice"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (449, '"Menu"', 'folder', 'Locations', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (451, '"Menu"', 'class', 'Building', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 449, '"Building"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (453, '"Menu"', 'class', 'Room', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 449, '"Room"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (455, '"Menu"', 'class', 'Floor', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 449, '"Floor"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (457, '"Menu"', 'class', 'Network point', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 449, '"NetworkPoint"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (459, '"Menu"', 'folder', 'Assets', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (461, '"Menu"', 'class', 'Asset', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"Asset"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (463, '"Menu"', 'class', 'Computer', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"Computer"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (465, '"Menu"', 'class', 'PC', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"PC"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (467, '"Menu"', 'class', 'Notebook', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"Notebook"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (469, '"Menu"', 'class', 'Server', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"Server"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (471, '"Menu"', 'class', 'Monitor', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"Monitor"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (473, '"Menu"', 'class', 'Printer', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"Printer"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (475, '"Menu"', 'class', 'NetworkDevice', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"NetworkDevice"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (477, '"Menu"', 'class', 'Rack', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"Rack"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (479, '"Menu"', 'class', 'UPS', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"UPS"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (481, '"Menu"', 'class', 'License', 'N', 'system', '2011-08-23 18:30:52.77', NULL, 459, '"License"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (624, '"Menu"', 'folder', 'Basic archives', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (626, '"Menu"', 'class', 'Employee', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 624, '"Employee"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (628, '"Menu"', 'class', 'Office', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 624, '"Office"', 0, 1, 0, 'class');
INSERT INTO "Menu" VALUES (630, '"Menu"', 'class', 'Workplace', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 624, '"Workplace"', 0, 2, 0, 'class');
INSERT INTO "Menu" VALUES (632, '"Menu"', 'folder', 'Purchases', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 0, NULL, 0, 1, 0, 'folder');
INSERT INTO "Menu" VALUES (634, '"Menu"', 'class', 'Supplier', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 632, '"Supplier"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (636, '"Menu"', 'class', 'SupplierContact', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 632, '"SupplierContact"', 0, 1, 0, 'class');
INSERT INTO "Menu" VALUES (638, '"Menu"', 'class', 'Invoice', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 632, '"Invoice"', 0, 2, 0, 'class');
INSERT INTO "Menu" VALUES (640, '"Menu"', 'folder', 'Locations', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 0, NULL, 0, 2, 0, 'folder');
INSERT INTO "Menu" VALUES (642, '"Menu"', 'class', 'Building', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 640, '"Building"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (644, '"Menu"', 'class', 'Room', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 640, '"Room"', 0, 1, 0, 'class');
INSERT INTO "Menu" VALUES (646, '"Menu"', 'class', 'Floor', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 640, '"Floor"', 0, 2, 0, 'class');
INSERT INTO "Menu" VALUES (648, '"Menu"', 'class', 'Network point', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 640, '"NetworkPoint"', 0, 3, 0, 'class');
INSERT INTO "Menu" VALUES (650, '"Menu"', 'folder', 'Assets', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 0, NULL, 0, 3, 0, 'folder');
INSERT INTO "Menu" VALUES (652, '"Menu"', 'class', 'Asset', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"Asset"', 0, 0, 0, 'class');
INSERT INTO "Menu" VALUES (654, '"Menu"', 'class', 'Computer', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"Computer"', 0, 1, 0, 'class');
INSERT INTO "Menu" VALUES (656, '"Menu"', 'class', 'PC', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"PC"', 0, 2, 0, 'class');
INSERT INTO "Menu" VALUES (658, '"Menu"', 'class', 'Notebook', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"Notebook"', 0, 3, 0, 'class');
INSERT INTO "Menu" VALUES (660, '"Menu"', 'class', 'Server', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"Server"', 0, 4, 0, 'class');
INSERT INTO "Menu" VALUES (662, '"Menu"', 'class', 'Monitor', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"Monitor"', 0, 5, 0, 'class');
INSERT INTO "Menu" VALUES (664, '"Menu"', 'class', 'Printer', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"Printer"', 0, 6, 0, 'class');
INSERT INTO "Menu" VALUES (666, '"Menu"', 'class', 'NetworkDevice', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"NetworkDevice"', 0, 7, 0, 'class');
INSERT INTO "Menu" VALUES (668, '"Menu"', 'class', 'Rack', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"Rack"', 0, 8, 0, 'class');
INSERT INTO "Menu" VALUES (670, '"Menu"', 'class', 'UPS', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"UPS"', 0, 9, 0, 'class');
INSERT INTO "Menu" VALUES (672, '"Menu"', 'class', 'License', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 650, '"License"', 0, 10, 0, 'class');
INSERT INTO "Menu" VALUES (674, '"Menu"', 'folder', 'Report', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 0, NULL, 0, 4, 0, 'folder');
INSERT INTO "Menu" VALUES (676, '"Menu"', 'reportpdf', 'Location list with assets', 'N', 'system', '2012-08-23 22:20:01.394', NULL, 674, '"Report"', 597, 0, 0, 'custom');
INSERT INTO "Menu" VALUES (860, '"Menu"', 'folder', 'Dashboard', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (862, '"Menu"', 'dashboard', 'Item situation', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 860, '831', 0, 1, 0, 'dashboard');
INSERT INTO "Menu" VALUES (864, '"Menu"', 'folder', 'Basic archives', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 0, NULL, 0, 2, 0, 'folder');
INSERT INTO "Menu" VALUES (866, '"Menu"', 'class', 'Employee', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 864, '"Employee"', 0, 3, 0, 'class');
INSERT INTO "Menu" VALUES (868, '"Menu"', 'class', 'Office', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 864, '"Office"', 0, 4, 0, 'class');
INSERT INTO "Menu" VALUES (870, '"Menu"', 'class', 'Workplace', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 864, '"Workplace"', 0, 5, 0, 'class');
INSERT INTO "Menu" VALUES (872, '"Menu"', 'folder', 'Purchases', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 0, NULL, 0, 6, 0, 'folder');
INSERT INTO "Menu" VALUES (874, '"Menu"', 'class', 'Supplier', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 872, '"Supplier"', 0, 7, 0, 'class');
INSERT INTO "Menu" VALUES (876, '"Menu"', 'class', 'SupplierContact', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 872, '"SupplierContact"', 0, 8, 0, 'class');
INSERT INTO "Menu" VALUES (878, '"Menu"', 'class', 'Invoice', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 872, '"Invoice"', 0, 9, 0, 'class');
INSERT INTO "Menu" VALUES (880, '"Menu"', 'folder', 'Locations', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 0, NULL, 0, 10, 0, 'folder');
INSERT INTO "Menu" VALUES (882, '"Menu"', 'class', 'Building', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 880, '"Building"', 0, 11, 0, 'class');
INSERT INTO "Menu" VALUES (884, '"Menu"', 'class', 'Room', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 880, '"Room"', 0, 12, 0, 'class');
INSERT INTO "Menu" VALUES (886, '"Menu"', 'class', 'Floor', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 880, '"Floor"', 0, 13, 0, 'class');
INSERT INTO "Menu" VALUES (888, '"Menu"', 'class', 'Network point', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 880, '"NetworkPoint"', 0, 14, 0, 'class');
INSERT INTO "Menu" VALUES (890, '"Menu"', 'folder', 'Assets', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 0, NULL, 0, 15, 0, 'folder');
INSERT INTO "Menu" VALUES (892, '"Menu"', 'class', 'Asset', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"Asset"', 0, 16, 0, 'class');
INSERT INTO "Menu" VALUES (894, '"Menu"', 'class', 'Computer', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"Computer"', 0, 17, 0, 'class');
INSERT INTO "Menu" VALUES (896, '"Menu"', 'class', 'PC', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"PC"', 0, 18, 0, 'class');
INSERT INTO "Menu" VALUES (898, '"Menu"', 'class', 'Notebook', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"Notebook"', 0, 19, 0, 'class');
INSERT INTO "Menu" VALUES (900, '"Menu"', 'class', 'Server', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"Server"', 0, 20, 0, 'class');
INSERT INTO "Menu" VALUES (902, '"Menu"', 'class', 'Monitor', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"Monitor"', 0, 21, 0, 'class');
INSERT INTO "Menu" VALUES (904, '"Menu"', 'class', 'Printer', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"Printer"', 0, 22, 0, 'class');
INSERT INTO "Menu" VALUES (906, '"Menu"', 'class', 'NetworkDevice', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"NetworkDevice"', 0, 23, 0, 'class');
INSERT INTO "Menu" VALUES (908, '"Menu"', 'class', 'Rack', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"Rack"', 0, 24, 0, 'class');
INSERT INTO "Menu" VALUES (910, '"Menu"', 'class', 'UPS', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"UPS"', 0, 25, 0, 'class');
INSERT INTO "Menu" VALUES (912, '"Menu"', 'class', 'License', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 890, '"License"', 0, 26, 0, 'class');
INSERT INTO "Menu" VALUES (914, '"Menu"', 'folder', 'Report', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 0, NULL, 0, 27, 0, 'folder');
INSERT INTO "Menu" VALUES (916, '"Menu"', 'reportpdf', 'Location list with assets', 'N', 'system', '2012-08-24 10:28:10.449', NULL, 914, '"Report"', 597, 28, 0, 'custom');
INSERT INTO "Menu" VALUES (977, '"Menu"', 'folder', 'Dashboard', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 0, NULL, 0, 0, 0, 'folder');
INSERT INTO "Menu" VALUES (979, '"Menu"', 'dashboard', 'Item situation', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 977, '831', 0, 1, 0, 'dashboard');
INSERT INTO "Menu" VALUES (981, '"Menu"', 'dashboard', 'RfC situation', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 977, '946', 0, 2, 0, 'dashboard');
INSERT INTO "Menu" VALUES (983, '"Menu"', 'folder', 'Basic archives', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 0, NULL, 0, 3, 0, 'folder');
INSERT INTO "Menu" VALUES (985, '"Menu"', 'class', 'Employee', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 983, '"Employee"', 0, 4, 0, 'class');
INSERT INTO "Menu" VALUES (987, '"Menu"', 'class', 'Office', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 983, '"Office"', 0, 5, 0, 'class');
INSERT INTO "Menu" VALUES (989, '"Menu"', 'class', 'Workplace', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 983, '"Workplace"', 0, 6, 0, 'class');
INSERT INTO "Menu" VALUES (991, '"Menu"', 'folder', 'Purchases', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 0, NULL, 0, 7, 0, 'folder');
INSERT INTO "Menu" VALUES (993, '"Menu"', 'class', 'Supplier', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 991, '"Supplier"', 0, 8, 0, 'class');
INSERT INTO "Menu" VALUES (995, '"Menu"', 'class', 'SupplierContact', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 991, '"SupplierContact"', 0, 9, 0, 'class');
INSERT INTO "Menu" VALUES (997, '"Menu"', 'class', 'Invoice', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 991, '"Invoice"', 0, 10, 0, 'class');
INSERT INTO "Menu" VALUES (999, '"Menu"', 'folder', 'Locations', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 0, NULL, 0, 11, 0, 'folder');
INSERT INTO "Menu" VALUES (1001, '"Menu"', 'class', 'Building', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 999, '"Building"', 0, 12, 0, 'class');
INSERT INTO "Menu" VALUES (1003, '"Menu"', 'class', 'Room', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 999, '"Room"', 0, 13, 0, 'class');
INSERT INTO "Menu" VALUES (1005, '"Menu"', 'class', 'Floor', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 999, '"Floor"', 0, 14, 0, 'class');
INSERT INTO "Menu" VALUES (1007, '"Menu"', 'class', 'Network point', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 999, '"NetworkPoint"', 0, 15, 0, 'class');
INSERT INTO "Menu" VALUES (1009, '"Menu"', 'folder', 'Assets', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 0, NULL, 0, 16, 0, 'folder');
INSERT INTO "Menu" VALUES (1011, '"Menu"', 'class', 'Asset', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"Asset"', 0, 17, 0, 'class');
INSERT INTO "Menu" VALUES (1013, '"Menu"', 'class', 'Computer', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"Computer"', 0, 18, 0, 'class');
INSERT INTO "Menu" VALUES (1015, '"Menu"', 'class', 'PC', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"PC"', 0, 19, 0, 'class');
INSERT INTO "Menu" VALUES (1017, '"Menu"', 'class', 'Notebook', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"Notebook"', 0, 20, 0, 'class');
INSERT INTO "Menu" VALUES (1019, '"Menu"', 'class', 'Server', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"Server"', 0, 21, 0, 'class');
INSERT INTO "Menu" VALUES (1021, '"Menu"', 'class', 'Monitor', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"Monitor"', 0, 22, 0, 'class');
INSERT INTO "Menu" VALUES (1023, '"Menu"', 'class', 'Printer', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"Printer"', 0, 23, 0, 'class');
INSERT INTO "Menu" VALUES (1025, '"Menu"', 'class', 'NetworkDevice', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"NetworkDevice"', 0, 24, 0, 'class');
INSERT INTO "Menu" VALUES (1027, '"Menu"', 'class', 'Rack', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"Rack"', 0, 25, 0, 'class');
INSERT INTO "Menu" VALUES (1029, '"Menu"', 'class', 'UPS', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"UPS"', 0, 26, 0, 'class');
INSERT INTO "Menu" VALUES (1031, '"Menu"', 'class', 'License', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1009, '"License"', 0, 27, 0, 'class');
INSERT INTO "Menu" VALUES (1033, '"Menu"', 'folder', 'Report', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 0, NULL, 0, 28, 0, 'folder');
INSERT INTO "Menu" VALUES (1035, '"Menu"', 'reportpdf', 'Location list with assets', 'N', 'system', '2012-08-24 10:28:45.296', NULL, 1033, '"Report"', 597, 29, 0, 'custom');
INSERT INTO "Menu" VALUES (1067, '"Menu"', 'folder', 'Dashboard', 'A', 'system', '2012-08-24 10:28:45.462', NULL, 0, NULL, 0, 30, 0, 'folder');
INSERT INTO "Menu" VALUES (1069, '"Menu"', 'dashboard', 'Item situation', 'A', 'system', '2012-08-24 10:28:45.464', NULL, 1067, '831', 0, 31, 0, 'dashboard');
INSERT INTO "Menu" VALUES (1071, '"Menu"', 'dashboard', 'RfC situation', 'A', 'system', '2012-08-24 10:28:45.466', NULL, 1067, '946', 0, 32, 0, 'dashboard');
INSERT INTO "Menu" VALUES (1073, '"Menu"', 'folder', 'Basic archives', 'A', 'system', '2012-08-24 10:28:45.467', NULL, 0, NULL, 0, 33, 0, 'folder');
INSERT INTO "Menu" VALUES (1075, '"Menu"', 'class', 'Employee', 'A', 'system', '2012-08-24 10:28:45.469', NULL, 1073, '"Employee"', 0, 34, 0, 'class');
INSERT INTO "Menu" VALUES (1077, '"Menu"', 'class', 'Office', 'A', 'system', '2012-08-24 10:28:45.47', NULL, 1073, '"Office"', 0, 35, 0, 'class');
INSERT INTO "Menu" VALUES (1079, '"Menu"', 'class', 'Workplace', 'A', 'system', '2012-08-24 10:28:45.472', NULL, 1073, '"Workplace"', 0, 36, 0, 'class');
INSERT INTO "Menu" VALUES (1081, '"Menu"', 'folder', 'Purchases', 'A', 'system', '2012-08-24 10:28:45.473', NULL, 0, NULL, 0, 37, 0, 'folder');
INSERT INTO "Menu" VALUES (1083, '"Menu"', 'class', 'Supplier', 'A', 'system', '2012-08-24 10:28:45.475', NULL, 1081, '"Supplier"', 0, 38, 0, 'class');
INSERT INTO "Menu" VALUES (1085, '"Menu"', 'class', 'SupplierContact', 'A', 'system', '2012-08-24 10:28:45.476', NULL, 1081, '"SupplierContact"', 0, 39, 0, 'class');
INSERT INTO "Menu" VALUES (1087, '"Menu"', 'class', 'Invoice', 'A', 'system', '2012-08-24 10:28:45.479', NULL, 1081, '"Invoice"', 0, 40, 0, 'class');
INSERT INTO "Menu" VALUES (1089, '"Menu"', 'folder', 'Locations', 'A', 'system', '2012-08-24 10:28:45.481', NULL, 0, NULL, 0, 41, 0, 'folder');
INSERT INTO "Menu" VALUES (1091, '"Menu"', 'class', 'Building', 'A', 'system', '2012-08-24 10:28:45.483', NULL, 1089, '"Building"', 0, 42, 0, 'class');
INSERT INTO "Menu" VALUES (1093, '"Menu"', 'class', 'Room', 'A', 'system', '2012-08-24 10:28:45.485', NULL, 1089, '"Room"', 0, 43, 0, 'class');
INSERT INTO "Menu" VALUES (1095, '"Menu"', 'class', 'Floor', 'A', 'system', '2012-08-24 10:28:45.487', NULL, 1089, '"Floor"', 0, 44, 0, 'class');
INSERT INTO "Menu" VALUES (1097, '"Menu"', 'class', 'Network point', 'A', 'system', '2012-08-24 10:28:45.489', NULL, 1089, '"NetworkPoint"', 0, 45, 0, 'class');
INSERT INTO "Menu" VALUES (1099, '"Menu"', 'folder', 'Assets', 'A', 'system', '2012-08-24 10:28:45.492', NULL, 0, NULL, 0, 46, 0, 'folder');
INSERT INTO "Menu" VALUES (1101, '"Menu"', 'class', 'Asset', 'A', 'system', '2012-08-24 10:28:45.493', NULL, 1099, '"Asset"', 0, 47, 0, 'class');
INSERT INTO "Menu" VALUES (1103, '"Menu"', 'class', 'Computer', 'A', 'system', '2012-08-24 10:28:45.495', NULL, 1099, '"Computer"', 0, 48, 0, 'class');
INSERT INTO "Menu" VALUES (1105, '"Menu"', 'class', 'PC', 'A', 'system', '2012-08-24 10:28:45.499', NULL, 1099, '"PC"', 0, 49, 0, 'class');
INSERT INTO "Menu" VALUES (1107, '"Menu"', 'class', 'Notebook', 'A', 'system', '2012-08-24 10:28:45.501', NULL, 1099, '"Notebook"', 0, 50, 0, 'class');
INSERT INTO "Menu" VALUES (1109, '"Menu"', 'class', 'Server', 'A', 'system', '2012-08-24 10:28:45.503', NULL, 1099, '"Server"', 0, 51, 0, 'class');
INSERT INTO "Menu" VALUES (1111, '"Menu"', 'class', 'Monitor', 'A', 'system', '2012-08-24 10:28:45.504', NULL, 1099, '"Monitor"', 0, 52, 0, 'class');
INSERT INTO "Menu" VALUES (1113, '"Menu"', 'class', 'Printer', 'A', 'system', '2012-08-24 10:28:45.506', NULL, 1099, '"Printer"', 0, 53, 0, 'class');
INSERT INTO "Menu" VALUES (1115, '"Menu"', 'class', 'NetworkDevice', 'A', 'system', '2012-08-24 10:28:45.508', NULL, 1099, '"NetworkDevice"', 0, 54, 0, 'class');
INSERT INTO "Menu" VALUES (1117, '"Menu"', 'class', 'Rack', 'A', 'system', '2012-08-24 10:28:45.51', NULL, 1099, '"Rack"', 0, 55, 0, 'class');
INSERT INTO "Menu" VALUES (1119, '"Menu"', 'class', 'UPS', 'A', 'system', '2012-08-24 10:28:45.511', NULL, 1099, '"UPS"', 0, 56, 0, 'class');
INSERT INTO "Menu" VALUES (1121, '"Menu"', 'class', 'License', 'A', 'system', '2012-08-24 10:28:45.513', NULL, 1099, '"License"', 0, 57, 0, 'class');
INSERT INTO "Menu" VALUES (1123, '"Menu"', 'folder', 'Report', 'A', 'system', '2012-08-24 10:28:45.515', NULL, 0, NULL, 0, 58, 0, 'folder');
INSERT INTO "Menu" VALUES (1125, '"Menu"', 'reportpdf', 'Location list with assets', 'A', 'system', '2012-08-24 10:28:45.518', NULL, 1123, '"Report"', 597, 59, 0, 'custom');
INSERT INTO "Menu" VALUES (1127, '"Menu"', 'folder', 'Workflow', 'A', 'system', '2012-08-24 10:28:45.519', NULL, 0, NULL, 0, 60, 0, 'folder');
INSERT INTO "Menu" VALUES (1129, '"Menu"', 'processclass', 'Request for change', 'A', 'system', '2012-08-24 10:28:45.521', NULL, 1127, '"RequestForChange"', 0, 61, 0, 'processclass');



INSERT INTO "Menu_history" VALUES (280, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2011-07-22 17:59:02.57', NULL, 0, NULL, 0, 0, 0, 'folder', 34, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (281, '"Menu"', 'class', 'Employee', 'U', 'system', '2011-07-22 17:59:02.576', NULL, 34, '"Employee"', 0, 0, 0, 'class', 36, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (282, '"Menu"', 'class', 'Office', 'U', 'system', '2011-07-22 17:59:02.612', NULL, 34, '"Office"', 0, 0, 0, 'class', 38, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (283, '"Menu"', 'class', 'Workplace', 'U', 'system', '2011-07-22 17:59:02.615', NULL, 34, '"Workplace"', 0, 0, 0, 'class', 40, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (284, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2011-07-22 17:59:02.617', NULL, 0, NULL, 0, 0, 0, 'folder', 42, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (285, '"Menu"', 'class', 'Supplier', 'U', 'system', '2011-07-22 17:59:02.62', NULL, 42, '"Supplier"', 0, 0, 0, 'class', 44, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (286, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2011-07-22 17:59:02.623', NULL, 42, '"SupplierContact"', 0, 0, 0, 'class', 46, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (287, '"Menu"', 'class', 'Invoice', 'U', 'system', '2011-07-22 17:59:02.626', NULL, 42, '"Invoice"', 0, 0, 0, 'class', 48, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (288, '"Menu"', 'folder', 'Locations', 'U', 'system', '2011-07-22 17:59:02.629', NULL, 0, NULL, 0, 0, 0, 'folder', 50, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (289, '"Menu"', 'class', 'Building', 'U', 'system', '2011-07-22 17:59:02.632', NULL, 50, '"Building"', 0, 0, 0, 'class', 52, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (290, '"Menu"', 'class', 'Room', 'U', 'system', '2011-07-22 17:59:02.634', NULL, 50, '"Room"', 0, 0, 0, 'class', 54, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (291, '"Menu"', 'class', 'Floor', 'U', 'system', '2011-07-22 17:59:02.637', NULL, 50, '"Floor"', 0, 0, 0, 'class', 56, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (292, '"Menu"', 'folder', 'Assets', 'U', 'system', '2011-07-22 17:59:02.64', NULL, 0, NULL, 0, 0, 0, 'folder', 58, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (293, '"Menu"', 'class', 'Asset', 'U', 'system', '2011-07-22 17:59:02.643', NULL, 58, '"Asset"', 0, 0, 0, 'class', 60, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (294, '"Menu"', 'class', 'Rack', 'U', 'system', '2011-07-22 17:59:02.646', NULL, 58, '"Rack"', 0, 0, 0, 'class', 62, '2011-08-10 12:40:07.301');
INSERT INTO "Menu_history" VALUES (336, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2011-08-10 12:40:08.89', NULL, 0, NULL, 0, 0, 0, 'folder', 296, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (337, '"Menu"', 'class', 'Employee', 'U', 'system', '2011-08-10 12:40:08.893', NULL, 296, '"Employee"', 0, 0, 0, 'class', 298, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (338, '"Menu"', 'class', 'Office', 'U', 'system', '2011-08-10 12:40:08.895', NULL, 296, '"Office"', 0, 0, 0, 'class', 300, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (339, '"Menu"', 'class', 'Workplace', 'U', 'system', '2011-08-10 12:40:08.896', NULL, 296, '"Workplace"', 0, 0, 0, 'class', 302, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (340, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2011-08-10 12:40:08.899', NULL, 0, NULL, 0, 0, 0, 'folder', 304, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (341, '"Menu"', 'class', 'Supplier', 'U', 'system', '2011-08-10 12:40:08.9', NULL, 304, '"Supplier"', 0, 0, 0, 'class', 306, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (342, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2011-08-10 12:40:08.902', NULL, 304, '"SupplierContact"', 0, 0, 0, 'class', 308, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (343, '"Menu"', 'class', 'Invoice', 'U', 'system', '2011-08-10 12:40:08.903', NULL, 304, '"Invoice"', 0, 0, 0, 'class', 310, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (344, '"Menu"', 'folder', 'Locations', 'U', 'system', '2011-08-10 12:40:08.905', NULL, 0, NULL, 0, 0, 0, 'folder', 312, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (345, '"Menu"', 'class', 'Building', 'U', 'system', '2011-08-10 12:40:08.907', NULL, 312, '"Building"', 0, 0, 0, 'class', 314, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (346, '"Menu"', 'class', 'Room', 'U', 'system', '2011-08-10 12:40:08.908', NULL, 312, '"Room"', 0, 0, 0, 'class', 316, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (347, '"Menu"', 'class', 'Floor', 'U', 'system', '2011-08-10 12:40:08.91', NULL, 312, '"Floor"', 0, 0, 0, 'class', 318, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (348, '"Menu"', 'class', 'Network point', 'U', 'system', '2011-08-10 12:40:08.911', NULL, 312, '"NetworkPoint"', 0, 0, 0, 'class', 320, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (349, '"Menu"', 'folder', 'Assets', 'U', 'system', '2011-08-10 12:40:08.913', NULL, 0, NULL, 0, 0, 0, 'folder', 322, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (350, '"Menu"', 'class', 'Asset', 'U', 'system', '2011-08-10 12:40:08.915', NULL, 322, '"Asset"', 0, 0, 0, 'class', 324, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (351, '"Menu"', 'class', 'Rack', 'U', 'system', '2011-08-10 12:40:08.917', NULL, 322, '"Rack"', 0, 0, 0, 'class', 326, '2011-08-10 12:54:25.886');
INSERT INTO "Menu_history" VALUES (412, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2011-08-10 12:54:25.998', NULL, 0, NULL, 0, 0, 0, 'folder', 353, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (413, '"Menu"', 'class', 'Employee', 'U', 'system', '2011-08-10 12:54:26', NULL, 353, '"Employee"', 0, 0, 0, 'class', 355, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (414, '"Menu"', 'class', 'Office', 'U', 'system', '2011-08-10 12:54:26.001', NULL, 353, '"Office"', 0, 0, 0, 'class', 357, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (415, '"Menu"', 'class', 'Workplace', 'U', 'system', '2011-08-10 12:54:26.003', NULL, 353, '"Workplace"', 0, 0, 0, 'class', 359, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (416, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2011-08-10 12:54:26.004', NULL, 0, NULL, 0, 0, 0, 'folder', 361, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (417, '"Menu"', 'class', 'Supplier', 'U', 'system', '2011-08-10 12:54:26.006', NULL, 361, '"Supplier"', 0, 0, 0, 'class', 363, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (418, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2011-08-10 12:54:26.008', NULL, 361, '"SupplierContact"', 0, 0, 0, 'class', 365, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (419, '"Menu"', 'class', 'Invoice', 'U', 'system', '2011-08-10 12:54:26.009', NULL, 361, '"Invoice"', 0, 0, 0, 'class', 367, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (420, '"Menu"', 'folder', 'Locations', 'U', 'system', '2011-08-10 12:54:26.011', NULL, 0, NULL, 0, 0, 0, 'folder', 369, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (421, '"Menu"', 'class', 'Building', 'U', 'system', '2011-08-10 12:54:26.012', NULL, 369, '"Building"', 0, 0, 0, 'class', 371, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (422, '"Menu"', 'class', 'Room', 'U', 'system', '2011-08-10 12:54:26.014', NULL, 369, '"Room"', 0, 0, 0, 'class', 373, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (423, '"Menu"', 'class', 'Floor', 'U', 'system', '2011-08-10 12:54:26.015', NULL, 369, '"Floor"', 0, 0, 0, 'class', 375, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (424, '"Menu"', 'class', 'Network point', 'U', 'system', '2011-08-10 12:54:26.017', NULL, 369, '"NetworkPoint"', 0, 0, 0, 'class', 377, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (425, '"Menu"', 'folder', 'Assets', 'U', 'system', '2011-08-10 12:54:26.019', NULL, 0, NULL, 0, 0, 0, 'folder', 379, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (426, '"Menu"', 'class', 'Asset', 'U', 'system', '2011-08-10 12:54:26.176', NULL, 379, '"Asset"', 0, 0, 0, 'class', 381, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (427, '"Menu"', 'class', 'Computer', 'U', 'system', '2011-08-10 12:54:26.178', NULL, 379, '"Computer"', 0, 0, 0, 'class', 383, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (428, '"Menu"', 'class', 'PC', 'U', 'system', '2011-08-10 12:54:26.179', NULL, 379, '"PC"', 0, 0, 0, 'class', 385, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (429, '"Menu"', 'class', 'Notebook', 'U', 'system', '2011-08-10 12:54:26.181', NULL, 379, '"Notebook"', 0, 0, 0, 'class', 387, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (430, '"Menu"', 'class', 'Server', 'U', 'system', '2011-08-10 12:54:26.183', NULL, 379, '"Server"', 0, 0, 0, 'class', 389, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (431, '"Menu"', 'class', 'Rack', 'U', 'system', '2011-08-10 12:54:26.185', NULL, 379, '"Rack"', 0, 0, 0, 'class', 391, '2011-08-10 15:33:56.677');
INSERT INTO "Menu_history" VALUES (598, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2011-08-10 15:33:56.829', NULL, 0, NULL, 0, 0, 0, 'folder', 433, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (599, '"Menu"', 'class', 'Employee', 'U', 'system', '2011-08-10 15:33:56.834', NULL, 433, '"Employee"', 0, 0, 0, 'class', 435, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (600, '"Menu"', 'class', 'Office', 'U', 'system', '2011-08-10 15:33:56.836', NULL, 433, '"Office"', 0, 0, 0, 'class', 437, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (601, '"Menu"', 'class', 'Workplace', 'U', 'system', '2011-08-10 15:33:56.839', NULL, 433, '"Workplace"', 0, 0, 0, 'class', 439, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (602, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2011-08-10 15:33:56.841', NULL, 0, NULL, 0, 0, 0, 'folder', 441, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (603, '"Menu"', 'class', 'Supplier', 'U', 'system', '2011-08-10 15:33:56.844', NULL, 441, '"Supplier"', 0, 0, 0, 'class', 443, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (604, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2011-08-10 15:33:56.846', NULL, 441, '"SupplierContact"', 0, 0, 0, 'class', 445, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (605, '"Menu"', 'class', 'Invoice', 'U', 'system', '2011-08-10 15:33:56.849', NULL, 441, '"Invoice"', 0, 0, 0, 'class', 447, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (606, '"Menu"', 'folder', 'Locations', 'U', 'system', '2011-08-10 15:33:56.852', NULL, 0, NULL, 0, 0, 0, 'folder', 449, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (607, '"Menu"', 'class', 'Building', 'U', 'system', '2011-08-10 15:33:56.854', NULL, 449, '"Building"', 0, 0, 0, 'class', 451, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (608, '"Menu"', 'class', 'Room', 'U', 'system', '2011-08-10 15:33:56.857', NULL, 449, '"Room"', 0, 0, 0, 'class', 453, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (609, '"Menu"', 'class', 'Floor', 'U', 'system', '2011-08-10 15:33:56.86', NULL, 449, '"Floor"', 0, 0, 0, 'class', 455, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (610, '"Menu"', 'class', 'Network point', 'U', 'system', '2011-08-10 15:33:56.862', NULL, 449, '"NetworkPoint"', 0, 0, 0, 'class', 457, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (611, '"Menu"', 'folder', 'Assets', 'U', 'system', '2011-08-10 15:33:56.865', NULL, 0, NULL, 0, 0, 0, 'folder', 459, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (612, '"Menu"', 'class', 'Asset', 'U', 'system', '2011-08-10 15:33:56.867', NULL, 459, '"Asset"', 0, 0, 0, 'class', 461, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (613, '"Menu"', 'class', 'Computer', 'U', 'system', '2011-08-10 15:33:56.871', NULL, 459, '"Computer"', 0, 0, 0, 'class', 463, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (614, '"Menu"', 'class', 'PC', 'U', 'system', '2011-08-10 15:33:56.873', NULL, 459, '"PC"', 0, 0, 0, 'class', 465, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (615, '"Menu"', 'class', 'Notebook', 'U', 'system', '2011-08-10 15:33:56.875', NULL, 459, '"Notebook"', 0, 0, 0, 'class', 467, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (616, '"Menu"', 'class', 'Server', 'U', 'system', '2011-08-10 15:33:56.879', NULL, 459, '"Server"', 0, 0, 0, 'class', 469, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (617, '"Menu"', 'class', 'Monitor', 'U', 'system', '2011-08-10 15:33:56.881', NULL, 459, '"Monitor"', 0, 0, 0, 'class', 471, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (618, '"Menu"', 'class', 'Printer', 'U', 'system', '2011-08-10 15:33:56.883', NULL, 459, '"Printer"', 0, 0, 0, 'class', 473, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (619, '"Menu"', 'class', 'NetworkDevice', 'U', 'system', '2011-08-10 15:33:56.886', NULL, 459, '"NetworkDevice"', 0, 0, 0, 'class', 475, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (620, '"Menu"', 'class', 'Rack', 'U', 'system', '2011-08-10 15:33:56.889', NULL, 459, '"Rack"', 0, 0, 0, 'class', 477, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (621, '"Menu"', 'class', 'UPS', 'U', 'system', '2011-08-10 15:33:56.891', NULL, 459, '"UPS"', 0, 0, 0, 'class', 479, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (622, '"Menu"', 'class', 'License', 'U', 'system', '2011-08-10 15:33:56.893', NULL, 459, '"License"', 0, 0, 0, 'class', 481, '2011-08-23 18:30:52.77');
INSERT INTO "Menu_history" VALUES (790, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2011-08-23 18:30:53.073', NULL, 0, NULL, 0, 0, 0, 'folder', 624, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (791, '"Menu"', 'class', 'Employee', 'U', 'system', '2011-08-23 18:30:53.177', NULL, 624, '"Employee"', 0, 0, 0, 'class', 626, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (792, '"Menu"', 'class', 'Office', 'U', 'system', '2011-08-23 18:30:53.179', NULL, 624, '"Office"', 0, 0, 0, 'class', 628, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (793, '"Menu"', 'class', 'Workplace', 'U', 'system', '2011-08-23 18:30:53.181', NULL, 624, '"Workplace"', 0, 0, 0, 'class', 630, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (794, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2011-08-23 18:30:53.183', NULL, 0, NULL, 0, 0, 0, 'folder', 632, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (795, '"Menu"', 'class', 'Supplier', 'U', 'system', '2011-08-23 18:30:53.184', NULL, 632, '"Supplier"', 0, 0, 0, 'class', 634, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (796, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2011-08-23 18:30:53.186', NULL, 632, '"SupplierContact"', 0, 0, 0, 'class', 636, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (797, '"Menu"', 'class', 'Invoice', 'U', 'system', '2011-08-23 18:30:53.188', NULL, 632, '"Invoice"', 0, 0, 0, 'class', 638, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (798, '"Menu"', 'folder', 'Locations', 'U', 'system', '2011-08-23 18:30:53.189', NULL, 0, NULL, 0, 0, 0, 'folder', 640, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (799, '"Menu"', 'class', 'Building', 'U', 'system', '2011-08-23 18:30:53.191', NULL, 640, '"Building"', 0, 0, 0, 'class', 642, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (800, '"Menu"', 'class', 'Room', 'U', 'system', '2011-08-23 18:30:53.193', NULL, 640, '"Room"', 0, 0, 0, 'class', 644, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (801, '"Menu"', 'class', 'Floor', 'U', 'system', '2011-08-23 18:30:53.195', NULL, 640, '"Floor"', 0, 0, 0, 'class', 646, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (802, '"Menu"', 'class', 'Network point', 'U', 'system', '2011-08-23 18:30:53.197', NULL, 640, '"NetworkPoint"', 0, 0, 0, 'class', 648, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (803, '"Menu"', 'folder', 'Assets', 'U', 'system', '2011-08-23 18:30:53.199', NULL, 0, NULL, 0, 0, 0, 'folder', 650, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (804, '"Menu"', 'class', 'Asset', 'U', 'system', '2011-08-23 18:30:53.201', NULL, 650, '"Asset"', 0, 0, 0, 'class', 652, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (805, '"Menu"', 'class', 'Computer', 'U', 'system', '2011-08-23 18:30:53.203', NULL, 650, '"Computer"', 0, 0, 0, 'class', 654, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (806, '"Menu"', 'class', 'PC', 'U', 'system', '2011-08-23 18:30:53.204', NULL, 650, '"PC"', 0, 0, 0, 'class', 656, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (807, '"Menu"', 'class', 'Notebook', 'U', 'system', '2011-08-23 18:30:53.206', NULL, 650, '"Notebook"', 0, 0, 0, 'class', 658, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (808, '"Menu"', 'class', 'Server', 'U', 'system', '2011-08-23 18:30:53.208', NULL, 650, '"Server"', 0, 0, 0, 'class', 660, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (809, '"Menu"', 'class', 'Monitor', 'U', 'system', '2011-08-23 18:30:53.211', NULL, 650, '"Monitor"', 0, 0, 0, 'class', 662, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (810, '"Menu"', 'class', 'Printer', 'U', 'system', '2011-08-23 18:30:53.212', NULL, 650, '"Printer"', 0, 0, 0, 'class', 664, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (811, '"Menu"', 'class', 'NetworkDevice', 'U', 'system', '2011-08-23 18:30:53.214', NULL, 650, '"NetworkDevice"', 0, 0, 0, 'class', 666, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (812, '"Menu"', 'class', 'Rack', 'U', 'system', '2011-08-23 18:30:53.216', NULL, 650, '"Rack"', 0, 0, 0, 'class', 668, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (813, '"Menu"', 'class', 'UPS', 'U', 'system', '2011-08-23 18:30:53.218', NULL, 650, '"UPS"', 0, 0, 0, 'class', 670, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (814, '"Menu"', 'class', 'License', 'U', 'system', '2011-08-23 18:30:53.219', NULL, 650, '"License"', 0, 0, 0, 'class', 672, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (815, '"Menu"', 'folder', 'Report', 'U', 'system', '2011-08-23 18:30:53.221', NULL, 0, NULL, 0, 0, 0, 'folder', 674, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (816, '"Menu"', 'reportpdf', 'Location list with assets', 'U', 'system', '2011-08-23 18:30:53.223', NULL, 674, '"Report"', 597, 0, 0, 'custom', 676, '2012-01-31 11:29:35.93578');
INSERT INTO "Menu_history" VALUES (832, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 0, NULL, 0, 0, 0, 'folder', 624, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (833, '"Menu"', 'class', 'Employee', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 624, '"Employee"', 0, 0, 0, 'class', 626, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (834, '"Menu"', 'class', 'Office', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 624, '"Office"', 0, 1, 0, 'class', 628, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (835, '"Menu"', 'class', 'Workplace', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 624, '"Workplace"', 0, 2, 0, 'class', 630, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (836, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 0, NULL, 0, 1, 0, 'folder', 632, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (837, '"Menu"', 'class', 'Supplier', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 632, '"Supplier"', 0, 0, 0, 'class', 634, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (838, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 632, '"SupplierContact"', 0, 1, 0, 'class', 636, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (839, '"Menu"', 'class', 'Invoice', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 632, '"Invoice"', 0, 2, 0, 'class', 638, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (840, '"Menu"', 'folder', 'Locations', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 0, NULL, 0, 2, 0, 'folder', 640, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (841, '"Menu"', 'class', 'Building', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 640, '"Building"', 0, 0, 0, 'class', 642, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (842, '"Menu"', 'class', 'Room', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 640, '"Room"', 0, 1, 0, 'class', 644, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (843, '"Menu"', 'class', 'Floor', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 640, '"Floor"', 0, 2, 0, 'class', 646, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (844, '"Menu"', 'class', 'Network point', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 640, '"NetworkPoint"', 0, 3, 0, 'class', 648, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (845, '"Menu"', 'folder', 'Assets', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 0, NULL, 0, 3, 0, 'folder', 650, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (846, '"Menu"', 'class', 'Asset', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"Asset"', 0, 0, 0, 'class', 652, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (847, '"Menu"', 'class', 'Computer', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"Computer"', 0, 1, 0, 'class', 654, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (848, '"Menu"', 'class', 'PC', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"PC"', 0, 2, 0, 'class', 656, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (849, '"Menu"', 'class', 'Notebook', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"Notebook"', 0, 3, 0, 'class', 658, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (850, '"Menu"', 'class', 'Server', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"Server"', 0, 4, 0, 'class', 660, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (851, '"Menu"', 'class', 'Monitor', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"Monitor"', 0, 5, 0, 'class', 662, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (852, '"Menu"', 'class', 'Printer', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"Printer"', 0, 6, 0, 'class', 664, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (853, '"Menu"', 'class', 'NetworkDevice', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"NetworkDevice"', 0, 7, 0, 'class', 666, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (854, '"Menu"', 'class', 'Rack', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"Rack"', 0, 8, 0, 'class', 668, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (855, '"Menu"', 'class', 'UPS', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"UPS"', 0, 9, 0, 'class', 670, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (856, '"Menu"', 'class', 'License', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 650, '"License"', 0, 10, 0, 'class', 672, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (857, '"Menu"', 'folder', 'Report', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 0, NULL, 0, 4, 0, 'folder', 674, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (858, '"Menu"', 'reportpdf', 'Location list with assets', 'U', 'system', '2012-01-31 11:29:35.93578', NULL, 674, '"Report"', 597, 0, 0, 'custom', 676, '2012-08-23 22:20:01.394');
INSERT INTO "Menu_history" VALUES (947, '"Menu"', 'folder', 'Dashboard', 'U', 'system', '2012-08-23 22:20:01.732', NULL, 0, NULL, 0, 0, 0, 'folder', 860, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (948, '"Menu"', 'dashboard', 'Item situation', 'U', 'system', '2012-08-23 22:20:01.735', NULL, 860, '831', 0, 1, 0, 'dashboard', 862, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (949, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2012-08-23 22:20:01.736', NULL, 0, NULL, 0, 2, 0, 'folder', 864, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (950, '"Menu"', 'class', 'Employee', 'U', 'system', '2012-08-23 22:20:01.738', NULL, 864, '"Employee"', 0, 3, 0, 'class', 866, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (951, '"Menu"', 'class', 'Office', 'U', 'system', '2012-08-23 22:20:01.74', NULL, 864, '"Office"', 0, 4, 0, 'class', 868, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (952, '"Menu"', 'class', 'Workplace', 'U', 'system', '2012-08-23 22:20:01.744', NULL, 864, '"Workplace"', 0, 5, 0, 'class', 870, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (953, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2012-08-23 22:20:01.745', NULL, 0, NULL, 0, 6, 0, 'folder', 872, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (954, '"Menu"', 'class', 'Supplier', 'U', 'system', '2012-08-23 22:20:01.747', NULL, 872, '"Supplier"', 0, 7, 0, 'class', 874, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (955, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2012-08-23 22:20:01.748', NULL, 872, '"SupplierContact"', 0, 8, 0, 'class', 876, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (956, '"Menu"', 'class', 'Invoice', 'U', 'system', '2012-08-23 22:20:01.75', NULL, 872, '"Invoice"', 0, 9, 0, 'class', 878, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (957, '"Menu"', 'folder', 'Locations', 'U', 'system', '2012-08-23 22:20:01.751', NULL, 0, NULL, 0, 10, 0, 'folder', 880, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (958, '"Menu"', 'class', 'Building', 'U', 'system', '2012-08-23 22:20:01.754', NULL, 880, '"Building"', 0, 11, 0, 'class', 882, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (959, '"Menu"', 'class', 'Room', 'U', 'system', '2012-08-23 22:20:01.756', NULL, 880, '"Room"', 0, 12, 0, 'class', 884, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (960, '"Menu"', 'class', 'Floor', 'U', 'system', '2012-08-23 22:20:01.758', NULL, 880, '"Floor"', 0, 13, 0, 'class', 886, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (961, '"Menu"', 'class', 'Network point', 'U', 'system', '2012-08-23 22:20:01.76', NULL, 880, '"NetworkPoint"', 0, 14, 0, 'class', 888, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (962, '"Menu"', 'folder', 'Assets', 'U', 'system', '2012-08-23 22:20:01.763', NULL, 0, NULL, 0, 15, 0, 'folder', 890, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (963, '"Menu"', 'class', 'Asset', 'U', 'system', '2012-08-23 22:20:01.765', NULL, 890, '"Asset"', 0, 16, 0, 'class', 892, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (964, '"Menu"', 'class', 'Computer', 'U', 'system', '2012-08-23 22:20:01.767', NULL, 890, '"Computer"', 0, 17, 0, 'class', 894, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (965, '"Menu"', 'class', 'PC', 'U', 'system', '2012-08-23 22:20:01.768', NULL, 890, '"PC"', 0, 18, 0, 'class', 896, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (966, '"Menu"', 'class', 'Notebook', 'U', 'system', '2012-08-23 22:20:01.771', NULL, 890, '"Notebook"', 0, 19, 0, 'class', 898, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (967, '"Menu"', 'class', 'Server', 'U', 'system', '2012-08-23 22:20:01.773', NULL, 890, '"Server"', 0, 20, 0, 'class', 900, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (968, '"Menu"', 'class', 'Monitor', 'U', 'system', '2012-08-23 22:20:01.777', NULL, 890, '"Monitor"', 0, 21, 0, 'class', 902, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (969, '"Menu"', 'class', 'Printer', 'U', 'system', '2012-08-23 22:20:01.779', NULL, 890, '"Printer"', 0, 22, 0, 'class', 904, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (970, '"Menu"', 'class', 'NetworkDevice', 'U', 'system', '2012-08-23 22:20:01.781', NULL, 890, '"NetworkDevice"', 0, 23, 0, 'class', 906, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (971, '"Menu"', 'class', 'Rack', 'U', 'system', '2012-08-23 22:20:01.782', NULL, 890, '"Rack"', 0, 24, 0, 'class', 908, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (972, '"Menu"', 'class', 'UPS', 'U', 'system', '2012-08-23 22:20:01.784', NULL, 890, '"UPS"', 0, 25, 0, 'class', 910, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (973, '"Menu"', 'class', 'License', 'U', 'system', '2012-08-23 22:20:01.788', NULL, 890, '"License"', 0, 26, 0, 'class', 912, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (974, '"Menu"', 'folder', 'Report', 'U', 'system', '2012-08-23 22:20:01.789', NULL, 0, NULL, 0, 27, 0, 'folder', 914, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (975, '"Menu"', 'reportpdf', 'Location list with assets', 'U', 'system', '2012-08-23 22:20:01.791', NULL, 914, '"Report"', 597, 28, 0, 'custom', 916, '2012-08-24 10:28:10.449');
INSERT INTO "Menu_history" VALUES (1036, '"Menu"', 'folder', 'Dashboard', 'U', 'system', '2012-08-24 10:28:10.861', NULL, 0, NULL, 0, 0, 0, 'folder', 977, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1037, '"Menu"', 'dashboard', 'Item situation', 'U', 'system', '2012-08-24 10:28:10.863', NULL, 977, '831', 0, 1, 0, 'dashboard', 979, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1038, '"Menu"', 'dashboard', 'RfC situation', 'U', 'system', '2012-08-24 10:28:10.865', NULL, 977, '946', 0, 2, 0, 'dashboard', 981, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1039, '"Menu"', 'folder', 'Basic archives', 'U', 'system', '2012-08-24 10:28:10.866', NULL, 0, NULL, 0, 3, 0, 'folder', 983, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1040, '"Menu"', 'class', 'Employee', 'U', 'system', '2012-08-24 10:28:10.869', NULL, 983, '"Employee"', 0, 4, 0, 'class', 985, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1041, '"Menu"', 'class', 'Office', 'U', 'system', '2012-08-24 10:28:10.87', NULL, 983, '"Office"', 0, 5, 0, 'class', 987, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1042, '"Menu"', 'class', 'Workplace', 'U', 'system', '2012-08-24 10:28:10.872', NULL, 983, '"Workplace"', 0, 6, 0, 'class', 989, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1043, '"Menu"', 'folder', 'Purchases', 'U', 'system', '2012-08-24 10:28:10.876', NULL, 0, NULL, 0, 7, 0, 'folder', 991, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1044, '"Menu"', 'class', 'Supplier', 'U', 'system', '2012-08-24 10:28:10.878', NULL, 991, '"Supplier"', 0, 8, 0, 'class', 993, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1045, '"Menu"', 'class', 'SupplierContact', 'U', 'system', '2012-08-24 10:28:10.879', NULL, 991, '"SupplierContact"', 0, 9, 0, 'class', 995, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1046, '"Menu"', 'class', 'Invoice', 'U', 'system', '2012-08-24 10:28:10.881', NULL, 991, '"Invoice"', 0, 10, 0, 'class', 997, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1047, '"Menu"', 'folder', 'Locations', 'U', 'system', '2012-08-24 10:28:10.883', NULL, 0, NULL, 0, 11, 0, 'folder', 999, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1048, '"Menu"', 'class', 'Building', 'U', 'system', '2012-08-24 10:28:10.886', NULL, 999, '"Building"', 0, 12, 0, 'class', 1001, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1049, '"Menu"', 'class', 'Room', 'U', 'system', '2012-08-24 10:28:10.888', NULL, 999, '"Room"', 0, 13, 0, 'class', 1003, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1050, '"Menu"', 'class', 'Floor', 'U', 'system', '2012-08-24 10:28:10.89', NULL, 999, '"Floor"', 0, 14, 0, 'class', 1005, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1051, '"Menu"', 'class', 'Network point', 'U', 'system', '2012-08-24 10:28:10.892', NULL, 999, '"NetworkPoint"', 0, 15, 0, 'class', 1007, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1052, '"Menu"', 'folder', 'Assets', 'U', 'system', '2012-08-24 10:28:10.894', NULL, 0, NULL, 0, 16, 0, 'folder', 1009, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1053, '"Menu"', 'class', 'Asset', 'U', 'system', '2012-08-24 10:28:10.896', NULL, 1009, '"Asset"', 0, 17, 0, 'class', 1011, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1054, '"Menu"', 'class', 'Computer', 'U', 'system', '2012-08-24 10:28:10.898', NULL, 1009, '"Computer"', 0, 18, 0, 'class', 1013, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1055, '"Menu"', 'class', 'PC', 'U', 'system', '2012-08-24 10:28:10.9', NULL, 1009, '"PC"', 0, 19, 0, 'class', 1015, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1056, '"Menu"', 'class', 'Notebook', 'U', 'system', '2012-08-24 10:28:10.902', NULL, 1009, '"Notebook"', 0, 20, 0, 'class', 1017, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1057, '"Menu"', 'class', 'Server', 'U', 'system', '2012-08-24 10:28:10.903', NULL, 1009, '"Server"', 0, 21, 0, 'class', 1019, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1058, '"Menu"', 'class', 'Monitor', 'U', 'system', '2012-08-24 10:28:10.905', NULL, 1009, '"Monitor"', 0, 22, 0, 'class', 1021, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1059, '"Menu"', 'class', 'Printer', 'U', 'system', '2012-08-24 10:28:10.907', NULL, 1009, '"Printer"', 0, 23, 0, 'class', 1023, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1060, '"Menu"', 'class', 'NetworkDevice', 'U', 'system', '2012-08-24 10:28:10.909', NULL, 1009, '"NetworkDevice"', 0, 24, 0, 'class', 1025, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1061, '"Menu"', 'class', 'Rack', 'U', 'system', '2012-08-24 10:28:10.911', NULL, 1009, '"Rack"', 0, 25, 0, 'class', 1027, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1062, '"Menu"', 'class', 'UPS', 'U', 'system', '2012-08-24 10:28:10.913', NULL, 1009, '"UPS"', 0, 26, 0, 'class', 1029, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1063, '"Menu"', 'class', 'License', 'U', 'system', '2012-08-24 10:28:10.915', NULL, 1009, '"License"', 0, 27, 0, 'class', 1031, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1064, '"Menu"', 'folder', 'Report', 'U', 'system', '2012-08-24 10:28:10.918', NULL, 0, NULL, 0, 28, 0, 'folder', 1033, '2012-08-24 10:28:45.296');
INSERT INTO "Menu_history" VALUES (1065, '"Menu"', 'reportpdf', 'Location list with assets', 'U', 'system', '2012-08-24 10:28:10.919', NULL, 1033, '"Report"', 597, 29, 0, 'custom', 1035, '2012-08-24 10:28:45.296');



INSERT INTO "Metadata" VALUES (505, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.minzoom', 'N', 'system', '2011-09-19 16:59:23.120594', '0');
INSERT INTO "Metadata" VALUES (507, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.maxzoom', 'N', 'system', '2011-09-19 16:59:23.120594', '25');
INSERT INTO "Metadata" VALUES (509, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.style', 'N', 'system', '2011-09-19 16:59:23.120594', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Building.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}');
INSERT INTO "Metadata" VALUES (511, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.visibility', 'N', 'system', '2011-09-19 16:59:23.120594', 'Building');
INSERT INTO "Metadata" VALUES (513, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.index', 'N', 'system', '2011-09-19 16:59:23.120594', '1');
INSERT INTO "Metadata" VALUES (495, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.minzoom', 'N', 'system', '2011-09-19 16:59:28.659447', '0');
INSERT INTO "Metadata" VALUES (497, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.maxzoom', 'N', 'system', '2011-09-19 16:59:28.659447', '25');
INSERT INTO "Metadata" VALUES (499, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.style', 'N', 'system', '2011-09-19 16:59:28.659447', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Supplier.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}');
INSERT INTO "Metadata" VALUES (501, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.visibility', 'N', 'system', '2011-09-19 16:59:28.659447', 'Supplier');
INSERT INTO "Metadata" VALUES (503, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.index', 'N', 'system', '2011-09-19 16:59:28.659447', '0');
INSERT INTO "Metadata" VALUES (1249, '"Metadata"', 'PC', 'system.widgets', 'A', 'system', '2012-08-25 12:20:02.957', '[{"id":"4ea70051-9bab-436a-a5ef-5cb002a10912","label":"Ping","active":true,"alwaysenabled":true,"address":"{client:IPAddress}","count":3,"templates":{},"type":".Ping"},{"id":"06dc6599-2ad5-4d03-9262-d2dafd4277b6","label":"Warranty calendar","active":true,"alwaysenabled":true,"targetClass":"PC","startDate":"AcceptanceDate","endDate":null,"eventTitle":"SerialNumber","filter":"","defaultDate":null,"type":".Calendar"}]');



INSERT INTO "Metadata_history" VALUES (780, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.minzoom', 'U', 'system', '2011-08-23 15:41:08.854', '0', 505, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (781, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.maxzoom', 'U', 'system', '2011-08-23 15:41:08.854', '25', 507, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (782, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.style', 'U', 'system', '2011-08-23 15:41:08.854', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Building.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}', 509, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (783, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.visibility', 'U', 'system', '2011-08-23 15:41:08.854', 'Building', 511, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (784, '"Metadata"', 'gis.Detail_Building_Location', 'system.gis.index', 'U', 'system', '2011-08-23 15:41:08.854', '1', 513, '2011-09-19 16:59:23.120594');
INSERT INTO "Metadata_history" VALUES (785, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.minzoom', 'U', 'system', '2011-08-23 15:39:20.948', '0', 495, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (786, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.maxzoom', 'U', 'system', '2011-08-23 15:39:20.948', '25', 497, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (787, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.style', 'U', 'system', '2011-08-23 15:39:20.948', '{"strokeDashstyle":"solid","fillColor":"#CCFFFF","externalGraphic":"upload/images/gis/Supplier.jpg","pointRadius":10,"strokeColor":"#CCFFCC","strokeWidth":1}', 499, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (788, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.visibility', 'U', 'system', '2011-08-23 15:39:20.948', 'Supplier', 501, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (789, '"Metadata"', 'gis.Detail_Supplier_Location', 'system.gis.index', 'U', 'system', '2011-08-23 15:39:20.948', '0', 503, '2011-09-19 16:59:28.659447');
INSERT INTO "Metadata_history" VALUES (1251, '"Metadata"', 'PC', 'system.widgets', 'U', 'system', '2012-08-25 12:16:36.281', '[{"id":"4ea70051-9bab-436a-a5ef-5cb002a10912","label":"Ping","active":true,"alwaysenabled":true,"address":"{client:IPAddress}","count":3,"templates":{},"type":".Ping"}]', 1249, '2012-08-25 12:20:02.957');



INSERT INTO "Monitor" VALUES (550, '"Monitor"', 'MON0001', 'Acer - AL1716 ', 'A', 'admin', '2011-08-23 17:34:12.416', NULL, NULL, NULL, NULL, NULL, NULL, 138, 'AL1716 ', NULL, 134, NULL, NULL, NULL, 392, NULL);
INSERT INTO "Monitor" VALUES (555, '"Monitor"', 'MON0002', 'Acer - B243WCydr', 'A', 'admin', '2011-08-23 17:35:03.944', NULL, 'PRT576', NULL, NULL, NULL, NULL, 138, 'B243WCydr', 272, 128, NULL, NULL, NULL, 392, 330);
INSERT INTO "Monitor" VALUES (561, '"Monitor"', 'MON0003', 'Acer - V193HQb', 'A', 'admin', '2011-08-23 17:36:00.497', NULL, NULL, NULL, NULL, NULL, NULL, 138, 'V193HQb', 242, 130, NULL, NULL, NULL, 392, NULL);
INSERT INTO "Monitor" VALUES (573, '"Monitor"', 'MON0004', 'Epson - W1934S-BN', 'A', 'admin', '2011-08-23 17:37:57.173', NULL, 'KR57667', NULL, NULL, NULL, NULL, 140, 'W1934S-BN', 272, 132, NULL, NULL, NULL, 393, 330);
INSERT INTO "Monitor" VALUES (567, '"Monitor"', 'MON0007', 'Hp - V220', 'A', 'admin', '2011-09-07 11:59:52.223', NULL, 'SR6576', NULL, NULL, '2011-09-06', NULL, 135, 'V220', 230, 118, NULL, NULL, NULL, 392, 330);



INSERT INTO "Monitor_history" VALUES (551, '"Monitor"', 'MON0001', 'Acer - AL1716 ', 'U', 'admin', '2011-08-23 17:34:02.111', NULL, NULL, NULL, NULL, NULL, NULL, 138, 'AL1716 ', NULL, NULL, NULL, NULL, NULL, 392, NULL, 550, '2011-08-23 17:34:12.416');
INSERT INTO "Monitor_history" VALUES (774, '"Monitor"', 'MON0007', 'Hp - V220', 'U', 'admin', '2011-08-23 17:36:50.525', NULL, 'SR6576', NULL, NULL, NULL, NULL, 135, 'V220', 230, 118, NULL, NULL, NULL, 392, 330, 567, '2011-09-07 11:59:52.223');



INSERT INTO "NetworkDevice" VALUES (747, '"NetworkDevice"', 'ND0654', 'Switch Panel CISCO Catalyst 3750 S.N. YRTU87', 'A', 'admin', '2011-09-02 12:07:44.126', NULL, 'YRTU87', 723, '2011-05-08', '2011-06-06', NULL, 137, 'Catalyst 3750', 200, NULL, NULL, NULL, NULL, 409, 32, NULL);
INSERT INTO "NetworkDevice" VALUES (755, '"NetworkDevice"', 'ND0685', 'Switch Panel CISCO Catalyst 3750 S.N. YFGE87', 'A', 'admin', '2011-09-02 12:15:10.417', NULL, 'YFGE87', 723, '2011-07-04', '2011-09-13', NULL, 137, 'Catalyst 3750', 104, NULL, NULL, NULL, NULL, 409, 32, NULL);



INSERT INTO "NetworkDevice_history" VALUES (752, '"NetworkDevice"', 'ND0654', 'Switch Panel CISCO Catalyst 3750', 'U', 'admin', '2011-09-02 12:06:33.699', NULL, 'SNYRTU87', 723, '2011-05-08', '2011-06-14', NULL, 137, 'Catalyst 3750', 200, NULL, NULL, NULL, NULL, 409, 32, NULL, 747, '2011-09-02 12:07:04.477');
INSERT INTO "NetworkDevice_history" VALUES (753, '"NetworkDevice"', 'ND0654', 'Switch Panel CISCO Catalyst 3750', 'U', 'admin', '2011-09-02 12:07:04.477', NULL, 'SNYRTU87', 723, '2011-05-08', '2011-06-06', NULL, 137, 'Catalyst 3750', 200, NULL, NULL, NULL, NULL, 409, 32, NULL, 747, '2011-09-02 12:07:44.126');
INSERT INTO "NetworkDevice_history" VALUES (762, '"NetworkDevice"', 'ND0685', 'Switch Panel CISCO Catalyst 3750 S.N. YFGE87', 'U', 'admin', '2011-09-02 12:08:39.585', NULL, 'YFGE87', 723, NULL, NULL, NULL, 137, 'Catalyst 3750', 104, NULL, NULL, NULL, NULL, 409, 32, NULL, 755, '2011-09-02 12:14:44.964');
INSERT INTO "NetworkDevice_history" VALUES (763, '"NetworkDevice"', 'ND0685', 'Switch Panel CISCO Catalyst 3750 S.N. YFGE87', 'U', 'admin', '2011-09-02 12:14:44.964', NULL, 'YFGE87', 723, '2011-07-04', NULL, NULL, 137, 'Catalyst 3750', 104, NULL, NULL, NULL, NULL, 409, 32, NULL, 755, '2011-09-02 12:15:10.417');















INSERT INTO "Office" VALUES (110, '"Office"', 'OFF03', 'Office 03 - Legal Department', 'A', 'admin', '2011-07-24 18:49:18.638', NULL, 'Legal Department', NULL);
INSERT INTO "Office" VALUES (108, '"Office"', 'OFF02', 'Office 02 - Administration', 'A', 'admin', '2011-07-24 18:49:25.82', NULL, 'Administration', NULL);
INSERT INTO "Office" VALUES (112, '"Office"', 'OFF01', 'Office 01 - Headquarters', 'A', 'admin', '2011-07-24 23:38:05.699', NULL, 'Head Office', NULL);



INSERT INTO "Office_history" VALUES (113, '"Office"', 'OFF02', 'Office 02 - Legal Department', 'U', 'admin', '2011-07-24 18:48:13.386', NULL, 'Legal Department', NULL, 110, '2011-07-24 18:49:18.638');
INSERT INTO "Office_history" VALUES (114, '"Office"', 'OFF01', 'Office 01 - Administration', 'U', 'admin', '2011-07-24 18:47:26.769', NULL, 'Administration', NULL, 108, '2011-07-24 18:49:25.82');
INSERT INTO "Office_history" VALUES (170, '"Office"', 'OFF01', 'Office 01 - Head Office', 'U', 'admin', '2011-07-24 18:49:09.575', NULL, 'Head Office', NULL, 112, '2011-07-24 23:38:05.699');



INSERT INTO "PC" VALUES (534, '"PC"', 'PC0002', 'Intel Pentium P4', 'A', 'admin', '2011-08-23 17:29:52.21', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Pentium P4', 104, 128, 134, NULL, NULL, 1, 2, NULL, NULL, NULL, NULL, NULL);
INSERT INTO "PC" VALUES (542, '"PC"', 'PC0004', 'Sony Vajo F', 'A', 'admin', '2011-08-23 17:32:51.564', NULL, 'TY747687', NULL, NULL, NULL, NULL, 136, 'Vajo F', 272, 130, 116, NULL, NULL, 8, 4, NULL, 2, NULL, NULL, NULL);
INSERT INTO "PC" VALUES (518, '"PC"', 'PC0001', 'Acer - Netbook D250', 'A', 'admin', '2012-08-25 12:17:41.034', NULL, '43434', NULL, '2011-04-03', NULL, NULL, 138, 'D250', 236, 120, 116, NULL, NULL, 4, 2, NULL, 1, '127.0.0.1', NULL, NULL);
INSERT INTO "PC" VALUES (526, '"PC"', 'PC0003', 'Hp - A6316', 'A', 'admin', '2012-08-25 12:41:15.881', NULL, NULL, 723, NULL, '2011-09-06', NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);



INSERT INTO "PC_history" VALUES (710, '"PC"', 'PC0001', 'Acer - Netbook D250', 'U', 'admin', '2011-08-23 17:26:13.647', NULL, '43434', NULL, NULL, NULL, NULL, 138, 'D250', 236, 120, 116, NULL, NULL, 4, 2, NULL, 1, NULL, NULL, NULL, 518, '2011-08-23 23:46:34.587');
INSERT INTO "PC_history" VALUES (718, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-23 17:28:42.292', NULL, NULL, NULL, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-08-29 13:03:27.919');
INSERT INTO "PC_history" VALUES (719, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-29 13:03:27.919', NULL, NULL, 714, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-08-29 13:07:08.776');
INSERT INTO "PC_history" VALUES (726, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-29 13:07:08.776', NULL, NULL, NULL, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-08-29 13:27:49.732');
INSERT INTO "PC_history" VALUES (776, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-08-29 13:27:49.732', NULL, NULL, 723, NULL, NULL, NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2011-09-07 11:59:52.223');
INSERT INTO "PC_history" VALUES (1250, '"PC"', 'PC0001', 'Acer - Netbook D250', 'U', 'admin', '2011-08-23 23:46:34.587', NULL, '43434', NULL, '2011-04-03', NULL, NULL, 138, 'D250', 236, 120, 116, NULL, NULL, 4, 2, NULL, 1, NULL, NULL, NULL, 518, '2012-08-25 12:17:41.034');
INSERT INTO "PC_history" VALUES (1252, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2011-09-07 11:59:52.223', NULL, NULL, 723, NULL, '2011-09-06', NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2012-08-25 12:39:36.099');
INSERT INTO "PC_history" VALUES (1255, '"PC"', 'PC0003', 'Hp - A6316', 'U', 'admin', '2012-08-25 12:39:36.099', NULL, NULL, NULL, NULL, '2011-09-06', NULL, 135, 'A6316', 248, 126, 116, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 526, '2012-08-25 12:41:15.881');



INSERT INTO "Patch" VALUES (773, '"Patch"', '1.3.1-05', 'Create database', 'A', 'system', '2011-09-05 12:01:42.544', NULL);
INSERT INTO "Patch" VALUES (818, '"Patch"', '1.4.0-01', 'Reorders tree nodes that were not properly ordered when saving them', 'A', 'system', '2012-01-31 11:29:35.93578', NULL);
INSERT INTO "Patch" VALUES (820, '"Patch"', '1.4.0-02', 'Fixes reference values filling on attribute creation', 'A', 'system', '2012-01-31 11:29:36.004394', NULL);
INSERT INTO "Patch" VALUES (822, '"Patch"', '1.5.0-01', 'Creates DB templates table', 'A', 'system', '2012-08-23 21:55:23.55', NULL);
INSERT INTO "Patch" VALUES (824, '"Patch"', '2.0.0-01', 'Dashboard base functions', 'A', 'system', '2012-08-23 21:55:23.713', NULL);
INSERT INTO "Patch" VALUES (826, '"Patch"', '2.0.0-02', 'Alter workflow tables', 'A', 'system', '2012-08-23 21:55:23.773', NULL);
INSERT INTO "Patch" VALUES (828, '"Patch"', '2.0.0-03', 'Add UI profile attributes', 'A', 'system', '2012-08-23 21:55:23.92', NULL);
INSERT INTO "Patch" VALUES (830, '"Patch"', '2.0.0-04', 'A few Dashboard Functions', 'A', 'system', '2012-08-23 21:55:23.973', NULL);
INSERT INTO "Patch" VALUES (1264, '"Patch"', '2.0.0-05', 'Support for INOUT parameters in custom functions', 'A', 'system', '2012-08-30 16:14:58.493242', NULL);






INSERT INTO "Printer" VALUES (579, '"Printer"', 'PRT0001', 'Canon - IX5000', 'A', 'admin', '2011-08-23 17:38:55.033', NULL, 'YT687', NULL, NULL, NULL, NULL, 139, 'IX5000', 242, 130, NULL, NULL, NULL, 399, 395, true, NULL);
INSERT INTO "Printer" VALUES (585, '"Printer"', 'PRT0002', 'Epson - ELP 6200L', 'A', 'admin', '2011-08-23 17:39:42.706', NULL, 'RTD575', NULL, NULL, NULL, NULL, 140, 'ELP 6200L', 212, 120, NULL, NULL, NULL, 399, 395, false, NULL);
INSERT INTO "Printer" VALUES (591, '"Printer"', 'PRT0003', 'HP DesignJet Z2100', 'A', 'admin', '2011-09-07 11:59:52.223', NULL, 'YU6874', NULL, NULL, '2011-09-06', NULL, 135, 'DesignJet Z2100', 266, 122, NULL, NULL, NULL, 399, NULL, false, NULL);



INSERT INTO "Printer_history" VALUES (775, '"Printer"', 'PRT0003', 'HP DesignJet Z2100', 'U', 'admin', '2011-08-23 17:40:48.481', NULL, 'YU6874', NULL, NULL, NULL, NULL, 135, 'DesignJet Z2100', 266, 122, NULL, NULL, NULL, 399, NULL, false, NULL, 591, '2011-09-07 11:59:52.223');









INSERT INTO "Report" VALUES (597, 'Location list with assets', 'Location list with assets', 'A', NULL, '2011-08-23 18:16:36.567', 'custom', 'SELECT
"Asset"."Code" AS "AssetCode", max("Asset"."Description") AS "AssetDescription", max("LookUp1"."Description") AS "AssetBrand",
"Workplace"."Code" AS "WorkplaceCode", max("Workplace"."Description") AS "WorkplaceDescription", max("Employee"."Description") as "Assignee", max(lower("Employee"."Email")) as "Email",
coalesce("Room"."Code", ''Not defined'') AS "RoomCode",
max(coalesce("Room"."Description",''Not defined'')) AS "RoomDescription",
max(coalesce("Floor"."Description" ,''Not defined'')) AS "FloorDescription",
max(coalesce("Building"."Description",''Not defined'')) AS "BuildingDescription"
FROM "Asset"
LEFT OUTER JOIN "Workplace" ON "Workplace"."Id"="Asset"."Workplace" AND "Workplace"."Status"=''A''
LEFT OUTER JOIN "Employee" ON "Employee"."Id"="Asset"."Assignee" AND "Employee"."Status"=''A''
LEFT OUTER JOIN "Room" ON "Room"."Id"="Asset"."Room" AND "Room"."Status"=''A''
LEFT OUTER JOIN "Floor" ON "Floor"."Id"="Room"."Floor" AND "Floor"."Status"=''A''
LEFT OUTER JOIN "Building" ON "Building"."Id"="Floor"."Building" AND "Building"."Status"=''A''
LEFT OUTER JOIN "LookUp" AS "LookUp1" ON "LookUp1"."Id"="Asset"."Brand"
WHERE "Asset"."Status"=''A''
GROUP BY "Room"."Code", "Workplace"."Code", "Asset"."Code"
ORDER BY "Room"."Code"', '\\254\\355\\000\\005sr\\000(net.sf.jasperreports.engine.JasperReport\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\013compileDatat\\000\\026Ljava/io/Serializable;L\\000\\021compileNameSuffixt\\000\\022Ljava/lang/String;L\\000\\015compilerClassq\\000~\\000\\002xr\\000-net.sf.jasperreports.engine.base.JRBaseReport\\000\\000\\000\\000\\000\\000''\\330\\002\\000''I\\000\\014bottomMarginI\\000\\013columnCountI\\000\\015columnSpacingI\\000\\013columnWidthZ\\000\\020ignorePaginationZ\\000\\023isFloatColumnFooterZ\\000\\020isSummaryNewPageZ\\000 isSummaryWithPageHeaderAndFooterZ\\000\\016isTitleNewPageI\\000\\012leftMarginB\\000\\013orientationI\\000\\012pageHeightI\\000\\011pageWidthB\\000\\012printOrderI\\000\\013rightMarginI\\000\\011topMarginB\\000\\016whenNoDataTypeL\\000\\012backgroundt\\000$Lnet/sf/jasperreports/engine/JRBand;L\\000\\014columnFooterq\\000~\\000\\004L\\000\\014columnHeaderq\\000~\\000\\004[\\000\\010datasetst\\000([Lnet/sf/jasperreports/engine/JRDataset;L\\000\\013defaultFontt\\000*Lnet/sf/jasperreports/engine/JRReportFont;L\\000\\014defaultStylet\\000%Lnet/sf/jasperreports/engine/JRStyle;L\\000\\006detailq\\000~\\000\\004L\\000\\015detailSectiont\\000''Lnet/sf/jasperreports/engine/JRSection;[\\000\\005fontst\\000+[Lnet/sf/jasperreports/engine/JRReportFont;L\\000\\022formatFactoryClassq\\000~\\000\\002L\\000\\012importsSett\\000\\017Ljava/util/Set;L\\000\\010languageq\\000~\\000\\002L\\000\\016lastPageFooterq\\000~\\000\\004L\\000\\013mainDatasett\\000''Lnet/sf/jasperreports/engine/JRDataset;L\\000\\004nameq\\000~\\000\\002L\\000\\006noDataq\\000~\\000\\004L\\000\\012pageFooterq\\000~\\000\\004L\\000\\012pageHeaderq\\000~\\000\\004[\\000\\006stylest\\000&[Lnet/sf/jasperreports/engine/JRStyle;L\\000\\007summaryq\\000~\\000\\004[\\000\\011templatest\\000/[Lnet/sf/jasperreports/engine/JRReportTemplate;L\\000\\005titleq\\000~\\000\\004xp\\000\\000\\000\\024\\000\\000\\000\\001\\000\\000\\000\\000\\000\\000\\003\\016\\000\\000\\000\\000\\000\\000\\000\\000\\036\\002\\000\\000\\002S\\000\\000\\003J\\001\\000\\000\\000\\036\\000\\000\\000\\024\\001sr\\000+net.sf.jasperreports.engine.base.JRBaseBand\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\005I\\000\\031PSEUDO_SERIAL_VERSION_UIDI\\000\\006heightZ\\000\\016isSplitAllowedL\\000\\023printWhenExpressiont\\000*Lnet/sf/jasperreports/engine/JRExpression;L\\000\\011splitTypet\\000\\020Ljava/lang/Byte;xr\\0003net.sf.jasperreports.engine.base.JRBaseElementGroup\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\002L\\000\\010childrent\\000\\020Ljava/util/List;L\\000\\014elementGroupt\\000,Lnet/sf/jasperreports/engine/JRElementGroup;xpsr\\000\\023java.util.ArrayListx\\201\\322\\035\\231\\307a\\235\\003\\000\\001I\\000\\004sizexp\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001psr\\000\\016java.lang.Byte\\234N`\\204\\356P\\365\\034\\002\\000\\001B\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\005\\001pq\\000~\\000\\032sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\005w\\004\\000\\000\\000\\012sr\\0000net.sf.jasperreports.engine.base.JRBaseRectangle\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001L\\000\\006radiust\\000\\023Ljava/lang/Integer;xr\\0005net.sf.jasperreports.engine.base.JRBaseGraphicElement\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\004fillq\\000~\\000\\021L\\000\\007linePent\\000#Lnet/sf/jasperreports/engine/JRPen;L\\000\\003penq\\000~\\000\\021xr\\000.net.sf.jasperreports.engine.base.JRBaseElement\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\026I\\000\\006heightZ\\000\\027isPrintInFirstWholeBandZ\\000\\025isPrintRepeatedValuesZ\\000\\032isPrintWhenDetailOverflowsZ\\000\\025isRemoveLineWhenBlankB\\000\\014positionTypeB\\000\\013stretchTypeI\\000\\005widthI\\000\\001xI\\000\\001yL\\000\\011backcolort\\000\\020Ljava/awt/Color;L\\000\\024defaultStyleProvidert\\0004Lnet/sf/jasperreports/engine/JRDefaultStyleProvider;L\\000\\014elementGroupq\\000~\\000\\024L\\000\\011forecolorq\\000~\\000$L\\000\\003keyq\\000~\\000\\002L\\000\\004modeq\\000~\\000\\021L\\000\\013parentStyleq\\000~\\000\\007L\\000\\030parentStyleNameReferenceq\\000~\\000\\002L\\000\\023printWhenExpressionq\\000~\\000\\020L\\000\\025printWhenGroupChangest\\000%Lnet/sf/jasperreports/engine/JRGroup;L\\000\\015propertiesMapt\\000-Lnet/sf/jasperreports/engine/JRPropertiesMap;[\\000\\023propertyExpressionst\\0003[Lnet/sf/jasperreports/engine/JRPropertyExpression;xp\\000\\000\\000 \\000\\001\\000\\000\\002\\000\\000\\000\\003\\012\\000\\000\\000\\001\\000\\000\\000\\002sr\\000\\016java.awt.Color\\001\\245\\027\\203\\020\\2173u\\002\\000\\005F\\000\\006falphaI\\000\\005valueL\\000\\002cst\\000\\033Ljava/awt/color/ColorSpace;[\\000\\011frgbvaluet\\000\\002[F[\\000\\006fvalueq\\000~\\000,xp\\000\\000\\000\\000\\377\\360\\360\\360pppq\\000~\\000\\016q\\000~\\000\\035pt\\000\\013rectangle-1sq\\000~\\000\\030\\001pppppppsr\\000*net.sf.jasperreports.engine.base.JRBasePen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\004L\\000\\011lineColorq\\000~\\000$L\\000\\011lineStyleq\\000~\\000\\021L\\000\\011lineWidtht\\000\\021Ljava/lang/Float;L\\000\\014penContainert\\000,Lnet/sf/jasperreports/engine/JRPenContainer;xppsq\\000~\\000\\030\\000sr\\000\\017java.lang.Float\\332\\355\\311\\242\\333<\\360\\354\\002\\000\\001F\\000\\005valuexq\\000~\\000\\031\\000\\000\\000\\000q\\000~\\000)ppsr\\0001net.sf.jasperreports.engine.base.JRBaseStaticText\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001L\\000\\004textq\\000~\\000\\002xr\\0002net.sf.jasperreports.engine.base.JRBaseTextElement\\000\\000\\000\\000\\000\\000''\\330\\002\\000 L\\000\\006borderq\\000~\\000\\021L\\000\\013borderColorq\\000~\\000$L\\000\\014bottomBorderq\\000~\\000\\021L\\000\\021bottomBorderColorq\\000~\\000$L\\000\\015bottomPaddingq\\000~\\000 L\\000\\010fontNameq\\000~\\000\\002L\\000\\010fontSizeq\\000~\\000 L\\000\\023horizontalAlignmentq\\000~\\000\\021L\\000\\006isBoldt\\000\\023Ljava/lang/Boolean;L\\000\\010isItalicq\\000~\\0009L\\000\\015isPdfEmbeddedq\\000~\\0009L\\000\\017isStrikeThroughq\\000~\\0009L\\000\\014isStyledTextq\\000~\\0009L\\000\\013isUnderlineq\\000~\\0009L\\000\\012leftBorderq\\000~\\000\\021L\\000\\017leftBorderColorq\\000~\\000$L\\000\\013leftPaddingq\\000~\\000 L\\000\\007lineBoxt\\000''Lnet/sf/jasperreports/engine/JRLineBox;L\\000\\013lineSpacingq\\000~\\000\\021L\\000\\006markupq\\000~\\000\\002L\\000\\007paddingq\\000~\\000 L\\000\\013pdfEncodingq\\000~\\000\\002L\\000\\013pdfFontNameq\\000~\\000\\002L\\000\\012reportFontq\\000~\\000\\006L\\000\\013rightBorderq\\000~\\000\\021L\\000\\020rightBorderColorq\\000~\\000$L\\000\\014rightPaddingq\\000~\\000 L\\000\\010rotationq\\000~\\000\\021L\\000\\011topBorderq\\000~\\000\\021L\\000\\016topBorderColorq\\000~\\000$L\\000\\012topPaddingq\\000~\\000 L\\000\\021verticalAlignmentq\\000~\\000\\021xq\\000~\\000#\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000_\\000\\000\\000M\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\014staticText-3pppppppppppppsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexq\\000~\\000\\031\\000\\000\\000\\012psr\\000\\021java.lang.Boolean\\315 r\\200\\325\\234\\372\\356\\002\\000\\001Z\\000\\005valuexp\\001ppppppppsr\\000.net.sf.jasperreports.engine.base.JRBaseLineBox\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\013L\\000\\015bottomPaddingq\\000~\\000 L\\000\\011bottomPent\\000+Lnet/sf/jasperreports/engine/base/JRBoxPen;L\\000\\014boxContainert\\000,Lnet/sf/jasperreports/engine/JRBoxContainer;L\\000\\013leftPaddingq\\000~\\000 L\\000\\007leftPenq\\000~\\000BL\\000\\007paddingq\\000~\\000 L\\000\\003penq\\000~\\000BL\\000\\014rightPaddingq\\000~\\000 L\\000\\010rightPenq\\000~\\000BL\\000\\012topPaddingq\\000~\\000 L\\000\\006topPenq\\000~\\000Bxppsr\\0003net.sf.jasperreports.engine.base.JRBaseBoxBottomPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xr\\000-net.sf.jasperreports.engine.base.JRBaseBoxPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001L\\000\\007lineBoxq\\000~\\000:xq\\000~\\0000sq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dq\\000~\\000;psr\\0001net.sf.jasperreports.engine.base.JRBaseBoxLeftPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xq\\000~\\000Fsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dpsq\\000~\\000Fpppq\\000~\\000Dq\\000~\\000Dpsr\\0002net.sf.jasperreports.engine.base.JRBaseBoxRightPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xq\\000~\\000Fsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dpsr\\0000net.sf.jasperreports.engine.base.JRBaseBoxTopPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xq\\000~\\000Fsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dppppt\\000\\016Helvetica-Boldpppppppppt\\000\\013Asset Brandsq\\000~\\0007\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000_\\000\\000\\000M\\000\\000\\000\\022pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\015staticText-10pppppppppppppsq\\000~\\000=\\000\\000\\000\\012pq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\q\\000~\\000Ypsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\psq\\000~\\000Fpppq\\000~\\000\\\\q\\000~\\000\\\\psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\ppppt\\000\\016Helvetica-Boldpppppppppt\\000\\016Asset Assigneesq\\000~\\0007\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000\\213\\000\\000\\000\\264\\000\\000\\000\\022pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\015staticText-11pppppppppppppsq\\000~\\000=\\000\\000\\000\\012pq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000oq\\000~\\000lpsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000opsq\\000~\\000Fpppq\\000~\\000oq\\000~\\000opsq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000opsq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000oppppt\\000\\016Helvetica-Boldpppppppppt\\000\\016Assignee emailsq\\000~\\0007\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000\\212\\000\\000\\000\\264\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\015staticText-12pppppppppppppsq\\000~\\000=\\000\\000\\000\\012pq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202q\\000~\\000\\177psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202psq\\000~\\000Fpppq\\000~\\000\\202q\\000~\\000\\202psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202ppppt\\000\\016Helvetica-Boldpppppppppt\\000\\021Asset Descriptionxp\\000\\000w&\\000\\000\\000''\\001pq\\000~\\000\\032ppppsr\\000.net.sf.jasperreports.engine.base.JRBaseSection\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001[\\000\\005bandst\\000%[Lnet/sf/jasperreports/engine/JRBand;xpur\\000%[Lnet.sf.jasperreports.engine.JRBand;\\225\\335~\\354\\214\\312\\2055\\002\\000\\000xp\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\005w\\004\\000\\000\\000\\012sr\\0000net.sf.jasperreports.engine.base.JRBaseTextField\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\021I\\000\\015bookmarkLevelB\\000\\016evaluationTimeB\\000\\017hyperlinkTargetB\\000\\015hyperlinkTypeZ\\000\\025isStretchWithOverflowL\\000\\024anchorNameExpressionq\\000~\\000\\020L\\000\\017evaluationGroupq\\000~\\000&L\\000\\012expressionq\\000~\\000\\020L\\000\\031hyperlinkAnchorExpressionq\\000~\\000\\020L\\000\\027hyperlinkPageExpressionq\\000~\\000\\020[\\000\\023hyperlinkParameterst\\0003[Lnet/sf/jasperreports/engine/JRHyperlinkParameter;L\\000\\034hyperlinkReferenceExpressionq\\000~\\000\\020L\\000\\032hyperlinkTooltipExpressionq\\000~\\000\\020L\\000\\017isBlankWhenNullq\\000~\\0009L\\000\\012linkTargetq\\000~\\000\\002L\\000\\010linkTypeq\\000~\\000\\002L\\000\\007patternq\\000~\\000\\002xq\\000~\\0008\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\001\\000\\000\\000\\000\\263\\000\\000\\000\\017pq\\000~\\000\\016q\\000~\\000\\227pt\\000\\011textFieldpppppppppppppsq\\000~\\000=\\000\\000\\000\\011ppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236q\\000~\\000\\233psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236psq\\000~\\000Fpppq\\000~\\000\\236q\\000~\\000\\236psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\001ppsr\\0001net.sf.jasperreports.engine.base.JRBaseExpression\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\004I\\000\\002id[\\000\\006chunkst\\0000[Lnet/sf/jasperreports/engine/JRExpressionChunk;L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xp\\000\\000\\000\\030ur\\0000[Lnet.sf.jasperreports.engine.JRExpressionChunk;mY\\317\\336iK\\243U\\002\\000\\000xp\\000\\000\\000\\001sr\\0006net.sf.jasperreports.engine.base.JRBaseExpressionChunk\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\002B\\000\\004typeL\\000\\004textq\\000~\\000\\002xp\\003t\\000\\005Emailt\\000\\020java.lang.Stringppppppq\\000~\\000@pppsr\\000+net.sf.jasperreports.engine.base.JRBaseLine\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001B\\000\\011directionxq\\000~\\000!\\000\\000\\000\\001\\000\\001\\000\\000\\002\\000\\000\\000\\003\\015\\000\\000\\000\\001\\000\\000\\000\\037pq\\000~\\000\\016q\\000~\\000\\227sq\\000~\\000*\\000\\000\\000\\000\\377\\313\\307\\307pppt\\000\\006line-1ppppppppsq\\000~\\0000pppq\\000~\\000\\266p\\001sq\\000~\\000\\231\\000\\000\\000\\017\\000\\001\\000\\000\\002\\000\\000\\000\\000d\\000\\000\\000H\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\000\\227pppppppppppppppsq\\000~\\000=\\000\\000\\000\\012ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\000\\274q\\000~\\000\\274q\\000~\\000\\272psq\\000~\\000Jpppq\\000~\\000\\274q\\000~\\000\\274psq\\000~\\000Fpppq\\000~\\000\\274q\\000~\\000\\274psq\\000~\\000Opppq\\000~\\000\\274q\\000~\\000\\274psq\\000~\\000Spppq\\000~\\000\\274q\\000~\\000\\274pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\031uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\012AssetBrandt\\000\\020java.lang.Stringppppppq\\000~\\000@pppsq\\000~\\000\\231\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000d\\000\\000\\000H\\000\\000\\000\\017pq\\000~\\000\\016q\\000~\\000\\227pppppppppppppppsq\\000~\\000=\\000\\000\\000\\012ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\000\\311q\\000~\\000\\311q\\000~\\000\\307psq\\000~\\000Jpppq\\000~\\000\\311q\\000~\\000\\311psq\\000~\\000Fpppq\\000~\\000\\311q\\000~\\000\\311psq\\000~\\000Opppq\\000~\\000\\311q\\000~\\000\\311psq\\000~\\000Spppq\\000~\\000\\311q\\000~\\000\\311pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\032uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\010Assigneet\\000\\020java.lang.Stringppppppq\\000~\\000@pppsq\\000~\\000\\231\\000\\000\\000\\017\\000\\001\\000\\000\\002\\000\\000\\000\\001\\000\\000\\000\\000\\263\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\000\\227pppppppppppppppsq\\000~\\000=\\000\\000\\000\\012ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\000\\326q\\000~\\000\\326q\\000~\\000\\324psq\\000~\\000Jpppq\\000~\\000\\326q\\000~\\000\\326psq\\000~\\000Fpppq\\000~\\000\\326q\\000~\\000\\326psq\\000~\\000Opppq\\000~\\000\\326q\\000~\\000\\326psq\\000~\\000Spppq\\000~\\000\\326q\\000~\\000\\326pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\033uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\020AssetDescriptiont\\000\\020java.lang.Stringppppppq\\000~\\000@pppxp\\000\\000w&\\000\\000\\000!\\001pq\\000~\\000\\032ppsr\\000\\021java.util.HashSet\\272D\\205\\225\\226\\270\\2674\\003\\000\\000xpw\\014\\000\\000\\000\\004?@\\000\\000\\000\\000\\000\\003t\\000"net.sf.jasperreports.engine.data.*t\\000\\035net.sf.jasperreports.engine.*t\\000\\013java.util.*xt\\000\\004javasq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\004w\\004\\000\\000\\000\\012sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000M\\000\\000\\002\\254\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\013textField-1ppppppppppppppsq\\000~\\000\\030\\003pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354q\\000~\\000\\351psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354psq\\000~\\000Fpppq\\000~\\000\\354q\\000~\\000\\354psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\037uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\012"Page " + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\011 + " di "t\\000\\020java.lang.Stringppppppsq\\000~\\000?\\000pppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\024\\000\\000\\002\\371\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\013textField-2ppppppppppppppq\\000~\\000\\353pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006q\\000~\\001\\004psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006psq\\000~\\000Fpppq\\000~\\001\\006q\\000~\\001\\006psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000 uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\005"" + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\005 + ""t\\000\\020java.lang.Stringppppppq\\000~\\001\\003pppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000H\\000\\000\\000\\037\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\013textField-3ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037q\\000~\\001\\035psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037psq\\000~\\000Fpppq\\000~\\001\\037q\\000~\\001\\037psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000!uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\024new java.util.Date()t\\000\\016java.util.Dateppppppq\\000~\\001\\003ppt\\000\\012MM/dd/yyyysq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\034\\000\\000\\000\\001\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\015staticText-26ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015q\\000~\\0013psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015psq\\000~\\000Fpppq\\000~\\0015q\\000~\\0015psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015ppppppppppppppt\\000\\005Date:xp\\000\\000w&\\000\\000\\000\\032\\001pq\\000~\\000\\032sr\\000.net.sf.jasperreports.engine.base.JRBaseDataset\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\016Z\\000\\006isMainB\\000\\027whenResourceMissingType[\\000\\006fieldst\\000&[Lnet/sf/jasperreports/engine/JRField;L\\000\\020filterExpressionq\\000~\\000\\020[\\000\\006groupst\\000&[Lnet/sf/jasperreports/engine/JRGroup;L\\000\\004nameq\\000~\\000\\002[\\000\\012parameterst\\000*[Lnet/sf/jasperreports/engine/JRParameter;L\\000\\015propertiesMapq\\000~\\000''L\\000\\005queryt\\000%Lnet/sf/jasperreports/engine/JRQuery;L\\000\\016resourceBundleq\\000~\\000\\002L\\000\\016scriptletClassq\\000~\\000\\002[\\000\\012scriptletst\\000*[Lnet/sf/jasperreports/engine/JRScriptlet;[\\000\\012sortFieldst\\000*[Lnet/sf/jasperreports/engine/JRSortField;[\\000\\011variablest\\000)[Lnet/sf/jasperreports/engine/JRVariable;xp\\001\\001ur\\000&[Lnet.sf.jasperreports.engine.JRField;\\002<\\337\\307N*\\362p\\002\\000\\000xp\\000\\000\\000\\013sr\\000,net.sf.jasperreports.engine.base.JRBaseField\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\005L\\000\\013descriptionq\\000~\\000\\002L\\000\\004nameq\\000~\\000\\002L\\000\\015propertiesMapq\\000~\\000''L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xpt\\000\\000t\\000\\011AssetCodesr\\000+net.sf.jasperreports.engine.JRPropertiesMap\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\004baseq\\000~\\000''L\\000\\016propertiesListq\\000~\\000\\023L\\000\\015propertiesMapt\\000\\017Ljava/util/Map;xppppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\020AssetDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\012AssetBrandsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\015WorkplaceCodesq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\024WorkplaceDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\010Assigneesq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\005Emailsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\010RoomCodesq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\017RoomDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\020FloorDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\023BuildingDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringppur\\000&[Lnet.sf.jasperreports.engine.JRGroup;@\\243_zL\\375x\\352\\002\\000\\000xp\\000\\000\\000\\003sr\\000,net.sf.jasperreports.engine.base.JRBaseGroup\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\016B\\000\\016footerPositionZ\\000\\031isReprintHeaderOnEachPageZ\\000\\021isResetPageNumberZ\\000\\020isStartNewColumnZ\\000\\016isStartNewPageZ\\000\\014keepTogetherI\\000\\027minHeightToStartNewPageL\\000\\015countVariablet\\000(Lnet/sf/jasperreports/engine/JRVariable;L\\000\\012expressionq\\000~\\000\\020L\\000\\013groupFooterq\\000~\\000\\004L\\000\\022groupFooterSectionq\\000~\\000\\010L\\000\\013groupHeaderq\\000~\\000\\004L\\000\\022groupHeaderSectionq\\000~\\000\\010L\\000\\004nameq\\000~\\000\\002xp\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000sr\\000/net.sf.jasperreports.engine.base.JRBaseVariable\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\015B\\000\\013calculationB\\000\\015incrementTypeZ\\000\\017isSystemDefinedB\\000\\011resetTypeL\\000\\012expressionq\\000~\\000\\020L\\000\\016incrementGroupq\\000~\\000&L\\000\\033incrementerFactoryClassNameq\\000~\\000\\002L\\000\\037incrementerFactoryClassRealNameq\\000~\\000\\002L\\000\\026initialValueExpressionq\\000~\\000\\020L\\000\\004nameq\\000~\\000\\002L\\000\\012resetGroupq\\000~\\000&L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xp\\001\\005\\001\\004sq\\000~\\000\\254\\000\\000\\000\\010uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)t\\000\\021java.lang.Integerppppsq\\000~\\000\\254\\000\\000\\000\\011uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\015palazzo_COUNTq\\000~\\001\\215q\\000~\\001\\224psq\\000~\\000\\254\\000\\000\\000\\016uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\023BuildingDescriptiont\\000\\020java.lang.Objectppsq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001pq\\000~\\000\\032psq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\000\\037\\000\\000\\000\\021\\000\\001\\000\\000\\002\\000\\000\\000\\003\\012\\000\\000\\000\\001\\000\\000\\000\\006sq\\000~\\000*\\000\\000\\000\\000\\377\\340\\372\\351pppq\\000~\\000\\016q\\000~\\001\\245pt\\000\\013rectangle-2q\\000~\\000/pppppppsq\\000~\\0000pq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\247ppsq\\000~\\0007\\000\\000\\000\\026\\000\\001\\000\\000\\002\\000\\000\\000\\0008\\000\\000\\000\\004\\000\\000\\000\\004pq\\000~\\000\\016q\\000~\\001\\245sq\\000~\\000*\\000\\000\\000\\000\\377\\000ffpppt\\000\\015staticText-19pppppppppppppsq\\000~\\000=\\000\\000\\000\\016ppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260q\\000~\\001\\254psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260psq\\000~\\000Fpppq\\000~\\001\\260q\\000~\\001\\260psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260ppppppppppppppt\\000\\011Building:sq\\000~\\000\\231\\000\\000\\000\\024\\000\\001\\000\\000\\002\\000\\000\\000\\000\\304\\000\\000\\000?\\000\\000\\000\\004pq\\000~\\000\\016q\\000~\\001\\245pppppppppppppppsq\\000~\\000=\\000\\000\\000\\016ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\001\\301q\\000~\\001\\301q\\000~\\001\\277psq\\000~\\000Jpppq\\000~\\001\\301q\\000~\\001\\301psq\\000~\\000Fpppq\\000~\\001\\301q\\000~\\001\\301psq\\000~\\000Opppq\\000~\\001\\301q\\000~\\001\\301psq\\000~\\000Spppq\\000~\\001\\301q\\000~\\001\\301pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\017uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\023BuildingDescriptiont\\000\\020java.lang.Stringppppppppppxp\\000\\000w&\\000\\000\\000\\033\\001pq\\000~\\000\\032t\\000\\007palazzosq\\000~\\001\\213\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000sq\\000~\\001\\216\\001\\005\\001\\004sq\\000~\\000\\254\\000\\000\\000\\012uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\013uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014tavola_COUNTq\\000~\\001\\315q\\000~\\001\\224psq\\000~\\000\\254\\000\\000\\000\\020uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\020FloorDescriptionq\\000~\\001\\236ppsq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001pq\\000~\\000\\032psq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\000\\037\\000\\000\\000\\023\\000\\001\\000\\000\\002\\000\\000\\000\\002\\371\\000\\000\\000\\022\\000\\000\\000\\004sq\\000~\\000*\\000\\000\\000\\000\\377\\365\\354\\354pppq\\000~\\000\\016q\\000~\\001\\342pt\\000\\013rectangle-3q\\000~\\000/pppppppsq\\000~\\0000pq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\344ppsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000(\\000\\000\\000\\027\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\001\\342sq\\000~\\000*\\000\\000\\000\\000\\377f\\000\\000pppt\\000\\015staticText-20pppppppppppppsq\\000~\\000=\\000\\000\\000\\014ppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355q\\000~\\001\\351psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355psq\\000~\\000Fpppq\\000~\\001\\355q\\000~\\001\\355psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355ppppppppppppppt\\000\\006Floor:sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\314\\000\\000\\000H\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\001\\342ppppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\001\\375q\\000~\\001\\375q\\000~\\001\\374psq\\000~\\000Jpppq\\000~\\001\\375q\\000~\\001\\375psq\\000~\\000Fpppq\\000~\\001\\375q\\000~\\001\\375psq\\000~\\000Opppq\\000~\\001\\375q\\000~\\001\\375psq\\000~\\000Spppq\\000~\\001\\375q\\000~\\001\\375pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\021uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\020FloorDescriptiont\\000\\020java.lang.Stringppppppppppxp\\000\\000w&\\000\\000\\000\\033\\001pq\\000~\\000\\032t\\000\\006tavolasq\\000~\\001\\213\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000sq\\000~\\001\\216\\001\\005\\001\\004sq\\000~\\000\\254\\000\\000\\000\\014uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\015uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014stanza_COUNTq\\000~\\002\\011q\\000~\\001\\224psq\\000~\\000\\254\\000\\000\\000\\022uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\010RoomCodeq\\000~\\001\\236ppsq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001pq\\000~\\000\\032psq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\004w\\004\\000\\000\\000\\012sq\\000~\\000\\037\\000\\000\\000\\023\\000\\001\\000\\000\\002\\000\\000\\000\\002\\343\\000\\000\\000(\\000\\000\\000\\005sq\\000~\\000*\\000\\000\\000\\000\\377\\342\\372\\372pppq\\000~\\000\\016q\\000~\\002\\036pt\\000\\013rectangle-4q\\000~\\000/pppppppsq\\000~\\0000pq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002 ppsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000-\\000\\000\\000,\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\002\\036sq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\231pppt\\000\\015staticText-21ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(q\\000~\\002%psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(psq\\000~\\000Fpppq\\000~\\002(q\\000~\\002(psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(ppppppppppppppt\\000\\005Room:sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000d\\000\\000\\000\\\\\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\002\\036ppppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\0028q\\000~\\0028q\\000~\\0027psq\\000~\\000Jpppq\\000~\\0028q\\000~\\0028psq\\000~\\000Fpppq\\000~\\0028q\\000~\\0028psq\\000~\\000Opppq\\000~\\0028q\\000~\\0028psq\\000~\\000Spppq\\000~\\0028q\\000~\\0028pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\023uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\010RoomCodet\\000\\020java.lang.Stringppppppppppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\325\\000\\000\\000\\316\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\002\\036ppppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\002Dq\\000~\\002Dq\\000~\\002Cpsq\\000~\\000Jpppq\\000~\\002Dq\\000~\\002Dpsq\\000~\\000Fpppq\\000~\\002Dq\\000~\\002Dpsq\\000~\\000Opppq\\000~\\002Dq\\000~\\002Dpsq\\000~\\000Spppq\\000~\\002Dq\\000~\\002Dpppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\024uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\017RoomDescriptiont\\000\\020java.lang.Stringppppppppppxp\\000\\000w&\\000\\000\\000\\033\\001pq\\000~\\000\\032t\\000\\006stanzat\\000\\011AssetListur\\000*[Lnet.sf.jasperreports.engine.JRParameter;"\\000\\014\\215*\\303`!\\002\\000\\000xp\\000\\000\\000\\020sr\\0000net.sf.jasperreports.engine.base.JRBaseParameter\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\011Z\\000\\016isForPromptingZ\\000\\017isSystemDefinedL\\000\\026defaultValueExpressionq\\000~\\000\\020L\\000\\013descriptionq\\000~\\000\\002L\\000\\004nameq\\000~\\000\\002L\\000\\016nestedTypeNameq\\000~\\000\\002L\\000\\015propertiesMapq\\000~\\000''L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xp\\001\\001ppt\\000\\025REPORT_PARAMETERS_MAPpsq\\000~\\001Spppt\\000\\015java.util.Mappsq\\000~\\002S\\001\\001ppt\\000\\015JASPER_REPORTpsq\\000~\\001Spppt\\000(net.sf.jasperreports.engine.JasperReportpsq\\000~\\002S\\001\\001ppt\\000\\021REPORT_CONNECTIONpsq\\000~\\001Spppt\\000\\023java.sql.Connectionpsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_MAX_COUNTpsq\\000~\\001Spppq\\000~\\001\\224psq\\000~\\002S\\001\\001ppt\\000\\022REPORT_DATA_SOURCEpsq\\000~\\001Spppt\\000(net.sf.jasperreports.engine.JRDataSourcepsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_SCRIPTLETpsq\\000~\\001Spppt\\000/net.sf.jasperreports.engine.JRAbstractScriptletpsq\\000~\\002S\\001\\001ppt\\000\\015REPORT_LOCALEpsq\\000~\\001Spppt\\000\\020java.util.Localepsq\\000~\\002S\\001\\001ppt\\000\\026REPORT_RESOURCE_BUNDLEpsq\\000~\\001Spppt\\000\\030java.util.ResourceBundlepsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_TIME_ZONEpsq\\000~\\001Spppt\\000\\022java.util.TimeZonepsq\\000~\\002S\\001\\001ppt\\000\\025REPORT_FORMAT_FACTORYpsq\\000~\\001Spppt\\000.net.sf.jasperreports.engine.util.FormatFactorypsq\\000~\\002S\\001\\001ppt\\000\\023REPORT_CLASS_LOADERpsq\\000~\\001Spppt\\000\\025java.lang.ClassLoaderpsq\\000~\\002S\\001\\001ppt\\000\\032REPORT_URL_HANDLER_FACTORYpsq\\000~\\001Spppt\\000 java.net.URLStreamHandlerFactorypsq\\000~\\002S\\001\\001ppt\\000\\024REPORT_FILE_RESOLVERpsq\\000~\\001Spppt\\000-net.sf.jasperreports.engine.util.FileResolverpsq\\000~\\002S\\001\\001ppt\\000\\022REPORT_VIRTUALIZERpsq\\000~\\001Spppt\\000)net.sf.jasperreports.engine.JRVirtualizerpsq\\000~\\002S\\001\\001ppt\\000\\024IS_IGNORE_PAGINATIONpsq\\000~\\001Spppt\\000\\021java.lang.Booleanpsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_TEMPLATESpsq\\000~\\001Spppt\\000\\024java.util.Collectionpsq\\000~\\001Spsq\\000~\\000\\026\\000\\000\\000\\005w\\004\\000\\000\\000\\012t\\000\\031ireport.scriptlethandlingt\\000\\020ireport.encodingt\\000\\014ireport.zoomt\\000\\011ireport.xt\\000\\011ireport.yxsr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\005q\\000~\\002\\227t\\000\\0031.0q\\000~\\002\\226t\\000\\005UTF-8q\\000~\\002\\230t\\000\\0010q\\000~\\002\\231t\\000\\0010q\\000~\\002\\225t\\000\\0012xsr\\000,net.sf.jasperreports.engine.base.JRBaseQuery\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\002[\\000\\006chunkst\\000+[Lnet/sf/jasperreports/engine/JRQueryChunk;L\\000\\010languageq\\000~\\000\\002xpur\\000+[Lnet.sf.jasperreports.engine.JRQueryChunk;@\\237\\000\\241\\350\\2724\\244\\002\\000\\000xp\\000\\000\\000\\001sr\\0001net.sf.jasperreports.engine.base.JRBaseQueryChunk\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003B\\000\\004typeL\\000\\004textq\\000~\\000\\002[\\000\\006tokenst\\000\\023[Ljava/lang/String;xp\\001t\\004\\320SELECT\\012"Asset"."Code" AS "AssetCode", max("Asset"."Description") AS "AssetDescription", max("LookUp1"."Description") AS "AssetBrand",\\012"Workplace"."Code" AS "WorkplaceCode", max("Workplace"."Description") AS "WorkplaceDescription", max("Employee"."Description") as "Assignee", max(lower("Employee"."Email")) as "Email",\\012coalesce("Room"."Code", ''Not defined'') AS "RoomCode",\\012max(coalesce("Room"."Description",''Not defined'')) AS "RoomDescription",\\012max(coalesce("Floor"."Description" ,''Not defined'')) AS "FloorDescription",\\012max(coalesce("Building"."Description",''Not defined'')) AS "BuildingDescription"\\012FROM "Asset"\\012LEFT OUTER JOIN "Workplace" ON "Workplace"."Id"="Asset"."Workplace" AND "Workplace"."Status"=''A''\\012LEFT OUTER JOIN "Employee" ON "Employee"."Id"="Asset"."Assignee" AND "Employee"."Status"=''A''\\012LEFT OUTER JOIN "Room" ON "Room"."Id"="Asset"."Room" AND "Room"."Status"=''A''\\012LEFT OUTER JOIN "Floor" ON "Floor"."Id"="Room"."Floor" AND "Floor"."Status"=''A''\\012LEFT OUTER JOIN "Building" ON "Building"."Id"="Floor"."Building" AND "Building"."Status"=''A''\\012LEFT OUTER JOIN "LookUp" AS "LookUp1" ON "LookUp1"."Id"="Asset"."Brand"\\012WHERE "Asset"."Status"=''A''\\012GROUP BY "Room"."Code", "Workplace"."Code", "Asset"."Code"\\012ORDER BY "Room"."Code"pt\\000\\003sqlppppur\\000)[Lnet.sf.jasperreports.engine.JRVariable;b\\346\\203|\\230,\\267D\\002\\000\\000xp\\000\\000\\000\\010sq\\000~\\001\\216\\010\\005\\001\\001ppppsq\\000~\\000\\254\\000\\000\\000\\000uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224pt\\000\\013PAGE_NUMBERpq\\000~\\001\\224psq\\000~\\001\\216\\010\\005\\001\\002ppppsq\\000~\\000\\254\\000\\000\\000\\001uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224pt\\000\\015COLUMN_NUMBERpq\\000~\\001\\224psq\\000~\\001\\216\\001\\005\\001\\001sq\\000~\\000\\254\\000\\000\\000\\002uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\003uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014REPORT_COUNTpq\\000~\\001\\224psq\\000~\\001\\216\\001\\005\\001\\002sq\\000~\\000\\254\\000\\000\\000\\004uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\005uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\012PAGE_COUNTpq\\000~\\001\\224psq\\000~\\001\\216\\001\\005\\001\\003sq\\000~\\000\\254\\000\\000\\000\\006uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\007uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014COLUMN_COUNTpq\\000~\\001\\224pq\\000~\\001\\217q\\000~\\001\\316q\\000~\\002\\012q\\000~\\002Ppsq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\004w\\004\\000\\000\\000\\012sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000H\\000\\000\\000\\037\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\011textFieldppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333q\\000~\\002\\331psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333psq\\000~\\000Fpppq\\000~\\002\\333q\\000~\\002\\333psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\034uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\024new java.util.Date()t\\000\\016java.util.Dateppppppq\\000~\\001\\003ppt\\000\\012MM/dd/yyyysq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000M\\000\\000\\002\\254\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\011textFieldppppppppppppppq\\000~\\000\\353pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361q\\000~\\002\\357psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361psq\\000~\\000Fpppq\\000~\\002\\361q\\000~\\002\\361psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\035uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\012"Page " + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\011 + " di "t\\000\\020java.lang.Stringppppppq\\000~\\001\\003pppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\024\\000\\000\\002\\371\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\011textFieldppppppppppppppq\\000~\\000\\353pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012q\\000~\\003\\010psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012psq\\000~\\000Fpppq\\000~\\003\\012q\\000~\\003\\012psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\036uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\005"" + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\005 + ""t\\000\\020java.lang.Stringppppppq\\000~\\001\\003pppsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\034\\000\\000\\000\\001\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\015staticText-25ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#q\\000~\\003!psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#psq\\000~\\000Fpppq\\000~\\003#q\\000~\\003#psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#ppppppppppppppt\\000\\005Date:xp\\000\\000w&\\000\\000\\000\\031\\001pq\\000~\\000\\032sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\002w\\004\\000\\000\\000\\012sq\\000~\\0007\\000\\000\\000\\017\\000\\001\\000\\000\\002\\000\\000\\000\\000\\202\\000\\000\\002\\214\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\0032pt\\000\\015staticText-28ppppppppppppppq\\000~\\000\\353q\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036q\\000~\\0034psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036psq\\000~\\000Fpppq\\000~\\0036q\\000~\\0036psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036ppppt\\000\\016Helvetica-Boldpppppppppt\\000\\025Stampato con CMDBuildsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\001\\000\\002\\000\\000\\000\\001\\217\\000\\000\\000\\300\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\0032pt\\000\\015staticText-29pppppppppppppsq\\000~\\000=\\000\\000\\000\\014sq\\000~\\000\\030\\002q\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jq\\000~\\003Fpsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jpsq\\000~\\000Fpppq\\000~\\003Jq\\000~\\003Jpsq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jpsq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jppppt\\000\\016Helvetica-Boldpppppppppt\\000\\031Location list with assetsxp\\000\\000w&\\000\\000\\000$\\001sq\\000~\\000\\254\\000\\000\\000\\027uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\016new Boolean ( sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\021.intValue() > 1 )q\\000~\\002\\216pq\\000~\\000\\032psq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\005\\001pq\\000~\\000\\032psq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\0007\\000\\000\\000\\032\\000\\001\\000\\000\\002\\000\\000\\000\\001\\217\\000\\000\\000\\300\\000\\000\\000\\022pq\\000~\\000\\016q\\000~\\003dpt\\000\\014staticText-1pppppppppppppsq\\000~\\000=\\000\\000\\000\\020q\\000~\\003Iq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003iq\\000~\\003fpsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003ipsq\\000~\\000Fpppq\\000~\\003iq\\000~\\003ipsq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003ipsq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003ippppt\\000\\016Helvetica-Boldpppppppppt\\000\\031Location list with assetssr\\000,net.sf.jasperreports.engine.base.JRBaseImage\\000\\000\\000\\000\\000\\000''\\330\\002\\000$I\\000\\015bookmarkLevelB\\000\\016evaluationTimeB\\000\\017hyperlinkTargetB\\000\\015hyperlinkTypeZ\\000\\006isLazyB\\000\\013onErrorTypeL\\000\\024anchorNameExpressionq\\000~\\000\\020L\\000\\006borderq\\000~\\000\\021L\\000\\013borderColorq\\000~\\000$L\\000\\014bottomBorderq\\000~\\000\\021L\\000\\021bottomBorderColorq\\000~\\000$L\\000\\015bottomPaddingq\\000~\\000 L\\000\\017evaluationGroupq\\000~\\000&L\\000\\012expressionq\\000~\\000\\020L\\000\\023horizontalAlignmentq\\000~\\000\\021L\\000\\031hyperlinkAnchorExpressionq\\000~\\000\\020L\\000\\027hyperlinkPageExpressionq\\000~\\000\\020[\\000\\023hyperlinkParametersq\\000~\\000\\232L\\000\\034hyperlinkReferenceExpressionq\\000~\\000\\020L\\000\\032hyperlinkTooltipExpressionq\\000~\\000\\020L\\000\\014isUsingCacheq\\000~\\0009L\\000\\012leftBorderq\\000~\\000\\021L\\000\\017leftBorderColorq\\000~\\000$L\\000\\013leftPaddingq\\000~\\000 L\\000\\007lineBoxq\\000~\\000:L\\000\\012linkTargetq\\000~\\000\\002L\\000\\010linkTypeq\\000~\\000\\002L\\000\\007paddingq\\000~\\000 L\\000\\013rightBorderq\\000~\\000\\021L\\000\\020rightBorderColorq\\000~\\000$L\\000\\014rightPaddingq\\000~\\000 L\\000\\012scaleImageq\\000~\\000\\021L\\000\\011topBorderq\\000~\\000\\021L\\000\\016topBorderColorq\\000~\\000$L\\000\\012topPaddingq\\000~\\000 L\\000\\021verticalAlignmentq\\000~\\000\\021xq\\000~\\000!\\000\\000\\000%\\000\\001\\000\\000\\002\\000\\000\\000\\000q\\000\\000\\000\\001\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\003dppppppppppsq\\000~\\0000pppq\\000~\\003zp\\000\\000\\000\\000\\001\\001\\000\\000\\002pppppppsq\\000~\\000\\254\\000\\000\\000\\025uq\\000~\\000\\257\\000\\000\\000\\002sq\\000~\\000\\261\\002t\\000\\025REPORT_PARAMETERS_MAPsq\\000~\\000\\261\\001t\\000\\016.get("IMAGE0")t\\000\\023java.io.InputStreampppppppq\\000~\\000@pppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\003\\203q\\000~\\003\\203q\\000~\\003zpsq\\000~\\000Jpppq\\000~\\003\\203q\\000~\\003\\203psq\\000~\\000Fpppq\\000~\\003\\203q\\000~\\003\\203psq\\000~\\000Opppq\\000~\\003\\203q\\000~\\003\\203psq\\000~\\000Spppq\\000~\\003\\203q\\000~\\003\\203pppppppppppsq\\000~\\003y\\000\\000\\000%\\000\\001\\000\\000\\002\\000\\000\\000\\000q\\000\\000\\002\\235\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\003dppppppppppsq\\000~\\0000pppq\\000~\\003\\211p\\000\\000\\000\\000\\001\\001\\000\\000\\002pppppppsq\\000~\\000\\254\\000\\000\\000\\026uq\\000~\\000\\257\\000\\000\\000\\002sq\\000~\\000\\261\\002t\\000\\025REPORT_PARAMETERS_MAPsq\\000~\\000\\261\\001t\\000\\016.get("IMAGE1")q\\000~\\003\\202pppppppq\\000~\\000@pppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\003\\221q\\000~\\003\\221q\\000~\\003\\211psq\\000~\\000Jpppq\\000~\\003\\221q\\000~\\003\\221psq\\000~\\000Fpppq\\000~\\003\\221q\\000~\\003\\221psq\\000~\\000Opppq\\000~\\003\\221q\\000~\\003\\221psq\\000~\\000Spppq\\000~\\003\\221q\\000~\\003\\221pppppppppppxp\\000\\000w&\\000\\000\\000<\\001pq\\000~\\000\\032sr\\0006net.sf.jasperreports.engine.design.JRReportCompileData\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\023crosstabCompileDataq\\000~\\001TL\\000\\022datasetCompileDataq\\000~\\001TL\\000\\026mainDatasetCompileDataq\\000~\\000\\001xpsq\\000~\\002\\232?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\000xsq\\000~\\002\\232?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\000xur\\000\\002[B\\254\\363\\027\\370\\006\\010T\\340\\002\\000\\000xp\\000\\000\\037+\\312\\376\\272\\276\\000\\000\\000.\\001\\030\\001\\000\\036AssetList_1314116315778_112849\\007\\000\\001\\001\\000,net/sf/jasperreports/engine/fill/JREvaluator\\007\\000\\003\\001\\000\\027parameter_REPORT_LOCALE\\001\\0002Lnet/sf/jasperreports/engine/fill/JRFillParameter;\\001\\000\\027parameter_JASPER_REPORT\\001\\000\\034parameter_REPORT_VIRTUALIZER\\001\\000\\032parameter_REPORT_TIME_ZONE\\001\\000\\036parameter_REPORT_FILE_RESOLVER\\001\\000\\032parameter_REPORT_SCRIPTLET\\001\\000\\037parameter_REPORT_PARAMETERS_MAP\\001\\000\\033parameter_REPORT_CONNECTION\\001\\000\\035parameter_REPORT_CLASS_LOADER\\001\\000\\034parameter_REPORT_DATA_SOURCE\\001\\000$parameter_REPORT_URL_HANDLER_FACTORY\\001\\000\\036parameter_IS_IGNORE_PAGINATION\\001\\000\\037parameter_REPORT_FORMAT_FACTORY\\001\\000\\032parameter_REPORT_MAX_COUNT\\001\\000\\032parameter_REPORT_TEMPLATES\\001\\000 parameter_REPORT_RESOURCE_BUNDLE\\001\\000\\017field_AssetCode\\001\\000.Lnet/sf/jasperreports/engine/fill/JRFillField;\\001\\000\\025field_RoomDescription\\001\\000\\016field_RoomCode\\001\\000\\023field_WorkplaceCode\\001\\000\\013field_Email\\001\\000\\026field_AssetDescription\\001\\000\\020field_AssetBrand\\001\\000\\026field_FloorDescription\\001\\000\\032field_WorkplaceDescription\\001\\000\\031field_BuildingDescription\\001\\000\\016field_Assignee\\001\\000\\024variable_PAGE_NUMBER\\001\\0001Lnet/sf/jasperreports/engine/fill/JRFillVariable;\\001\\000\\026variable_COLUMN_NUMBER\\001\\000\\025variable_REPORT_COUNT\\001\\000\\023variable_PAGE_COUNT\\001\\000\\025variable_COLUMN_COUNT\\001\\000\\026variable_palazzo_COUNT\\001\\000\\025variable_tavola_COUNT\\001\\000\\025variable_stanza_COUNT\\001\\000\\006<init>\\001\\000\\003()V\\001\\000\\004Code\\014\\000+\\000,\\012\\000\\004\\000.\\014\\000\\005\\000\\006\\011\\000\\002\\0000\\014\\000\\007\\000\\006\\011\\000\\002\\0002\\014\\000\\010\\000\\006\\011\\000\\002\\0004\\014\\000\\011\\000\\006\\011\\000\\002\\0006\\014\\000\\012\\000\\006\\011\\000\\002\\0008\\014\\000\\013\\000\\006\\011\\000\\002\\000:\\014\\000\\014\\000\\006\\011\\000\\002\\000<\\014\\000\\015\\000\\006\\011\\000\\002\\000>\\014\\000\\016\\000\\006\\011\\000\\002\\000@\\014\\000\\017\\000\\006\\011\\000\\002\\000B\\014\\000\\020\\000\\006\\011\\000\\002\\000D\\014\\000\\021\\000\\006\\011\\000\\002\\000F\\014\\000\\022\\000\\006\\011\\000\\002\\000H\\014\\000\\023\\000\\006\\011\\000\\002\\000J\\014\\000\\024\\000\\006\\011\\000\\002\\000L\\014\\000\\025\\000\\006\\011\\000\\002\\000N\\014\\000\\026\\000\\027\\011\\000\\002\\000P\\014\\000\\030\\000\\027\\011\\000\\002\\000R\\014\\000\\031\\000\\027\\011\\000\\002\\000T\\014\\000\\032\\000\\027\\011\\000\\002\\000V\\014\\000\\033\\000\\027\\011\\000\\002\\000X\\014\\000\\034\\000\\027\\011\\000\\002\\000Z\\014\\000\\035\\000\\027\\011\\000\\002\\000\\\\\\014\\000\\036\\000\\027\\011\\000\\002\\000^\\014\\000\\037\\000\\027\\011\\000\\002\\000`\\014\\000 \\000\\027\\011\\000\\002\\000b\\014\\000!\\000\\027\\011\\000\\002\\000d\\014\\000"\\000#\\011\\000\\002\\000f\\014\\000$\\000#\\011\\000\\002\\000h\\014\\000%\\000#\\011\\000\\002\\000j\\014\\000&\\000#\\011\\000\\002\\000l\\014\\000''\\000#\\011\\000\\002\\000n\\014\\000(\\000#\\011\\000\\002\\000p\\014\\000)\\000#\\011\\000\\002\\000r\\014\\000*\\000#\\011\\000\\002\\000t\\001\\000\\017LineNumberTable\\001\\000\\016customizedInit\\001\\0000(Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;)V\\001\\000\\012initParams\\001\\000\\022(Ljava/util/Map;)V\\014\\000y\\000z\\012\\000\\002\\000{\\001\\000\\012initFields\\014\\000}\\000z\\012\\000\\002\\000~\\001\\000\\010initVars\\014\\000\\200\\000z\\012\\000\\002\\000\\201\\001\\000\\015REPORT_LOCALE\\010\\000\\203\\001\\000\\015java/util/Map\\007\\000\\205\\001\\000\\003get\\001\\000&(Ljava/lang/Object;)Ljava/lang/Object;\\014\\000\\207\\000\\210\\013\\000\\206\\000\\211\\001\\0000net/sf/jasperreports/engine/fill/JRFillParameter\\007\\000\\213\\001\\000\\015JASPER_REPORT\\010\\000\\215\\001\\000\\022REPORT_VIRTUALIZER\\010\\000\\217\\001\\000\\020REPORT_TIME_ZONE\\010\\000\\221\\001\\000\\024REPORT_FILE_RESOLVER\\010\\000\\223\\001\\000\\020REPORT_SCRIPTLET\\010\\000\\225\\001\\000\\025REPORT_PARAMETERS_MAP\\010\\000\\227\\001\\000\\021REPORT_CONNECTION\\010\\000\\231\\001\\000\\023REPORT_CLASS_LOADER\\010\\000\\233\\001\\000\\022REPORT_DATA_SOURCE\\010\\000\\235\\001\\000\\032REPORT_URL_HANDLER_FACTORY\\010\\000\\237\\001\\000\\024IS_IGNORE_PAGINATION\\010\\000\\241\\001\\000\\025REPORT_FORMAT_FACTORY\\010\\000\\243\\001\\000\\020REPORT_MAX_COUNT\\010\\000\\245\\001\\000\\020REPORT_TEMPLATES\\010\\000\\247\\001\\000\\026REPORT_RESOURCE_BUNDLE\\010\\000\\251\\001\\000\\011AssetCode\\010\\000\\253\\001\\000,net/sf/jasperreports/engine/fill/JRFillField\\007\\000\\255\\001\\000\\017RoomDescription\\010\\000\\257\\001\\000\\010RoomCode\\010\\000\\261\\001\\000\\015WorkplaceCode\\010\\000\\263\\001\\000\\005Email\\010\\000\\265\\001\\000\\020AssetDescription\\010\\000\\267\\001\\000\\012AssetBrand\\010\\000\\271\\001\\000\\020FloorDescription\\010\\000\\273\\001\\000\\024WorkplaceDescription\\010\\000\\275\\001\\000\\023BuildingDescription\\010\\000\\277\\001\\000\\010Assignee\\010\\000\\301\\001\\000\\013PAGE_NUMBER\\010\\000\\303\\001\\000/net/sf/jasperreports/engine/fill/JRFillVariable\\007\\000\\305\\001\\000\\015COLUMN_NUMBER\\010\\000\\307\\001\\000\\014REPORT_COUNT\\010\\000\\311\\001\\000\\012PAGE_COUNT\\010\\000\\313\\001\\000\\014COLUMN_COUNT\\010\\000\\315\\001\\000\\015palazzo_COUNT\\010\\000\\317\\001\\000\\014tavola_COUNT\\010\\000\\321\\001\\000\\014stanza_COUNT\\010\\000\\323\\001\\000\\010evaluate\\001\\000\\025(I)Ljava/lang/Object;\\001\\000\\012Exceptions\\001\\000\\023java/lang/Throwable\\007\\000\\330\\001\\000\\021java/lang/Integer\\007\\000\\332\\001\\000\\004(I)V\\014\\000+\\000\\334\\012\\000\\333\\000\\335\\001\\000\\010getValue\\001\\000\\024()Ljava/lang/Object;\\014\\000\\337\\000\\340\\012\\000\\256\\000\\341\\001\\000\\020java/lang/String\\007\\000\\343\\012\\000\\214\\000\\341\\001\\000\\006IMAGE0\\010\\000\\346\\001\\000\\023java/io/InputStream\\007\\000\\350\\001\\000\\006IMAGE1\\010\\000\\352\\001\\000\\021java/lang/Boolean\\007\\000\\354\\012\\000\\306\\000\\341\\001\\000\\010intValue\\001\\000\\003()I\\014\\000\\357\\000\\360\\012\\000\\333\\000\\361\\001\\000\\004(Z)V\\014\\000+\\000\\363\\012\\000\\355\\000\\364\\001\\000\\016java/util/Date\\007\\000\\366\\012\\000\\367\\000.\\001\\000\\026java/lang/StringBuffer\\007\\000\\371\\001\\000\\005Page \\010\\000\\373\\001\\000\\025(Ljava/lang/String;)V\\014\\000+\\000\\375\\012\\000\\372\\000\\376\\001\\000\\006append\\001\\000,(Ljava/lang/Object;)Ljava/lang/StringBuffer;\\014\\001\\000\\001\\001\\012\\000\\372\\001\\002\\001\\000\\004 di \\010\\001\\004\\001\\000,(Ljava/lang/String;)Ljava/lang/StringBuffer;\\014\\001\\000\\001\\006\\012\\000\\372\\001\\007\\001\\000\\010toString\\001\\000\\024()Ljava/lang/String;\\014\\001\\011\\001\\012\\012\\000\\372\\001\\013\\012\\000\\372\\000.\\001\\000\\013evaluateOld\\001\\000\\013getOldValue\\014\\001\\017\\000\\340\\012\\000\\256\\001\\020\\012\\000\\306\\001\\020\\001\\000\\021evaluateEstimated\\001\\000\\021getEstimatedValue\\014\\001\\024\\000\\340\\012\\000\\306\\001\\025\\001\\000\\012SourceFile\\000!\\000\\002\\000\\004\\000\\000\\000#\\000\\002\\000\\005\\000\\006\\000\\000\\000\\002\\000\\007\\000\\006\\000\\000\\000\\002\\000\\010\\000\\006\\000\\000\\000\\002\\000\\011\\000\\006\\000\\000\\000\\002\\000\\012\\000\\006\\000\\000\\000\\002\\000\\013\\000\\006\\000\\000\\000\\002\\000\\014\\000\\006\\000\\000\\000\\002\\000\\015\\000\\006\\000\\000\\000\\002\\000\\016\\000\\006\\000\\000\\000\\002\\000\\017\\000\\006\\000\\000\\000\\002\\000\\020\\000\\006\\000\\000\\000\\002\\000\\021\\000\\006\\000\\000\\000\\002\\000\\022\\000\\006\\000\\000\\000\\002\\000\\023\\000\\006\\000\\000\\000\\002\\000\\024\\000\\006\\000\\000\\000\\002\\000\\025\\000\\006\\000\\000\\000\\002\\000\\026\\000\\027\\000\\000\\000\\002\\000\\030\\000\\027\\000\\000\\000\\002\\000\\031\\000\\027\\000\\000\\000\\002\\000\\032\\000\\027\\000\\000\\000\\002\\000\\033\\000\\027\\000\\000\\000\\002\\000\\034\\000\\027\\000\\000\\000\\002\\000\\035\\000\\027\\000\\000\\000\\002\\000\\036\\000\\027\\000\\000\\000\\002\\000\\037\\000\\027\\000\\000\\000\\002\\000 \\000\\027\\000\\000\\000\\002\\000!\\000\\027\\000\\000\\000\\002\\000"\\000#\\000\\000\\000\\002\\000$\\000#\\000\\000\\000\\002\\000%\\000#\\000\\000\\000\\002\\000&\\000#\\000\\000\\000\\002\\000''\\000#\\000\\000\\000\\002\\000(\\000#\\000\\000\\000\\002\\000)\\000#\\000\\000\\000\\002\\000*\\000#\\000\\000\\000\\010\\000\\001\\000+\\000,\\000\\001\\000-\\000\\000\\001\\\\\\000\\002\\000\\001\\000\\000\\000\\264*\\267\\000/*\\001\\265\\0001*\\001\\265\\0003*\\001\\265\\0005*\\001\\265\\0007*\\001\\265\\0009*\\001\\265\\000;*\\001\\265\\000=*\\001\\265\\000?*\\001\\265\\000A*\\001\\265\\000C*\\001\\265\\000E*\\001\\265\\000G*\\001\\265\\000I*\\001\\265\\000K*\\001\\265\\000M*\\001\\265\\000O*\\001\\265\\000Q*\\001\\265\\000S*\\001\\265\\000U*\\001\\265\\000W*\\001\\265\\000Y*\\001\\265\\000[*\\001\\265\\000]*\\001\\265\\000_*\\001\\265\\000a*\\001\\265\\000c*\\001\\265\\000e*\\001\\265\\000g*\\001\\265\\000i*\\001\\265\\000k*\\001\\265\\000m*\\001\\265\\000o*\\001\\265\\000q*\\001\\265\\000s*\\001\\265\\000u\\261\\000\\000\\000\\001\\000v\\000\\000\\000\\226\\000%\\000\\000\\000\\025\\000\\004\\000\\034\\000\\011\\000\\035\\000\\016\\000\\036\\000\\023\\000\\037\\000\\030\\000 \\000\\035\\000!\\000"\\000"\\000''\\000#\\000,\\000$\\0001\\000%\\0006\\000&\\000;\\000''\\000@\\000(\\000E\\000)\\000J\\000*\\000O\\000+\\000T\\000,\\000Y\\000-\\000^\\000.\\000c\\000/\\000h\\0000\\000m\\0001\\000r\\0002\\000w\\0003\\000|\\0004\\000\\201\\0005\\000\\206\\0006\\000\\213\\0007\\000\\220\\0008\\000\\225\\0009\\000\\232\\000:\\000\\237\\000;\\000\\244\\000<\\000\\251\\000=\\000\\256\\000>\\000\\263\\000\\025\\000\\001\\000w\\000x\\000\\001\\000-\\000\\000\\0004\\000\\002\\000\\004\\000\\000\\000\\020*+\\267\\000|*,\\267\\000\\177*-\\267\\000\\202\\261\\000\\000\\000\\001\\000v\\000\\000\\000\\022\\000\\004\\000\\000\\000J\\000\\005\\000K\\000\\012\\000L\\000\\017\\000M\\000\\002\\000y\\000z\\000\\001\\000-\\000\\000\\001I\\000\\003\\000\\002\\000\\000\\000\\361*+\\022\\204\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0001*+\\022\\216\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0003*+\\022\\220\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0005*+\\022\\222\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0007*+\\022\\224\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0009*+\\022\\226\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000;*+\\022\\230\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000=*+\\022\\232\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000?*+\\022\\234\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000A*+\\022\\236\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000C*+\\022\\240\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000E*+\\022\\242\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000G*+\\022\\244\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000I*+\\022\\246\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000K*+\\022\\250\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000M*+\\022\\252\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000O\\261\\000\\000\\000\\001\\000v\\000\\000\\000F\\000\\021\\000\\000\\000U\\000\\017\\000V\\000\\036\\000W\\000-\\000X\\000<\\000Y\\000K\\000Z\\000Z\\000[\\000i\\000\\\\\\000x\\000]\\000\\207\\000^\\000\\226\\000_\\000\\245\\000`\\000\\264\\000a\\000\\303\\000b\\000\\322\\000c\\000\\341\\000d\\000\\360\\000e\\000\\002\\000}\\000z\\000\\001\\000-\\000\\000\\000\\352\\000\\003\\000\\002\\000\\000\\000\\246*+\\022\\254\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000Q*+\\022\\260\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000S*+\\022\\262\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000U*+\\022\\264\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000W*+\\022\\266\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000Y*+\\022\\270\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000[*+\\022\\272\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000]*+\\022\\274\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000_*+\\022\\276\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000a*+\\022\\300\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000c*+\\022\\302\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000e\\261\\000\\000\\000\\001\\000v\\000\\000\\0002\\000\\014\\000\\000\\000m\\000\\017\\000n\\000\\036\\000o\\000-\\000p\\000<\\000q\\000K\\000r\\000Z\\000s\\000i\\000t\\000x\\000u\\000\\207\\000v\\000\\226\\000w\\000\\245\\000x\\000\\002\\000\\200\\000z\\000\\001\\000-\\000\\000\\000\\261\\000\\003\\000\\002\\000\\000\\000y*+\\022\\304\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000g*+\\022\\310\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000i*+\\022\\312\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000k*+\\022\\314\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000m*+\\022\\316\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000o*+\\022\\320\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000q*+\\022\\322\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000s*+\\022\\324\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000u\\261\\000\\000\\000\\001\\000v\\000\\000\\000&\\000\\011\\000\\000\\000\\200\\000\\017\\000\\201\\000\\036\\000\\202\\000-\\000\\203\\000<\\000\\204\\000K\\000\\205\\000Z\\000\\206\\000i\\000\\207\\000x\\000\\210\\000\\001\\000\\325\\000\\326\\000\\002\\000\\327\\000\\000\\000\\004\\000\\001\\000\\331\\000-\\000\\000\\003\\350\\000\\004\\000\\003\\000\\000\\002\\274\\001M\\033\\252\\000\\000\\002\\267\\000\\000\\000\\000\\000\\000\\000!\\000\\000\\000\\225\\000\\000\\000\\241\\000\\000\\000\\255\\000\\000\\000\\271\\000\\000\\000\\305\\000\\000\\000\\321\\000\\000\\000\\335\\000\\000\\000\\351\\000\\000\\000\\365\\000\\000\\001\\001\\000\\000\\001\\015\\000\\000\\001\\031\\000\\000\\001%\\000\\000\\0011\\000\\000\\001=\\000\\000\\001K\\000\\000\\001Y\\000\\000\\001g\\000\\000\\001u\\000\\000\\001\\203\\000\\000\\001\\221\\000\\000\\001\\237\\000\\000\\001\\267\\000\\000\\001\\317\\000\\000\\001\\360\\000\\000\\001\\376\\000\\000\\002\\014\\000\\000\\002\\032\\000\\000\\002(\\000\\000\\0023\\000\\000\\002V\\000\\000\\002q\\000\\000\\002\\224\\000\\000\\002\\257\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\031\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\015\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\001\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\365\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\351\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\335\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\321\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\305\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\271\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\255\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\241\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\225\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\211\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001}*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001o*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001a*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001S*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001E*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\0017*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\001)*\\264\\000S\\266\\000\\342\\300\\000\\344M\\247\\001\\033*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\347\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\001\\003*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\353\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\000\\353\\273\\000\\355Y*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\000\\362\\004\\244\\000\\007\\004\\247\\000\\004\\003\\267\\000\\365M\\247\\000\\312*\\264\\000Y\\266\\000\\342\\300\\000\\344M\\247\\000\\274*\\264\\000]\\266\\000\\342\\300\\000\\344M\\247\\000\\256*\\264\\000e\\266\\000\\342\\300\\000\\344M\\247\\000\\240*\\264\\000[\\266\\000\\342\\300\\000\\344M\\247\\000\\222\\273\\000\\367Y\\267\\000\\370M\\247\\000\\207\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000d\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000I\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000&\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000\\013\\273\\000\\367Y\\267\\000\\370M,\\260\\000\\000\\000\\001\\000v\\000\\000\\001\\032\\000F\\000\\000\\000\\220\\000\\002\\000\\222\\000\\230\\000\\226\\000\\241\\000\\227\\000\\244\\000\\233\\000\\255\\000\\234\\000\\260\\000\\240\\000\\271\\000\\241\\000\\274\\000\\245\\000\\305\\000\\246\\000\\310\\000\\252\\000\\321\\000\\253\\000\\324\\000\\257\\000\\335\\000\\260\\000\\340\\000\\264\\000\\351\\000\\265\\000\\354\\000\\271\\000\\365\\000\\272\\000\\370\\000\\276\\001\\001\\000\\277\\001\\004\\000\\303\\001\\015\\000\\304\\001\\020\\000\\310\\001\\031\\000\\311\\001\\034\\000\\315\\001%\\000\\316\\001(\\000\\322\\0011\\000\\323\\0014\\000\\327\\001=\\000\\330\\001@\\000\\334\\001K\\000\\335\\001N\\000\\341\\001Y\\000\\342\\001\\\\\\000\\346\\001g\\000\\347\\001j\\000\\353\\001u\\000\\354\\001x\\000\\360\\001\\203\\000\\361\\001\\206\\000\\365\\001\\221\\000\\366\\001\\224\\000\\372\\001\\237\\000\\373\\001\\242\\000\\377\\001\\267\\001\\000\\001\\272\\001\\004\\001\\317\\001\\005\\001\\322\\001\\011\\001\\360\\001\\012\\001\\363\\001\\016\\001\\376\\001\\017\\002\\001\\001\\023\\002\\014\\001\\024\\002\\017\\001\\030\\002\\032\\001\\031\\002\\035\\001\\035\\002(\\001\\036\\002+\\001"\\0023\\001#\\0026\\001''\\002V\\001(\\002Y\\001,\\002q\\001-\\002t\\0011\\002\\224\\0012\\002\\227\\0016\\002\\257\\0017\\002\\262\\001;\\002\\272\\001C\\000\\001\\001\\016\\000\\326\\000\\002\\000\\327\\000\\000\\000\\004\\000\\001\\000\\331\\000-\\000\\000\\003\\350\\000\\004\\000\\003\\000\\000\\002\\274\\001M\\033\\252\\000\\000\\002\\267\\000\\000\\000\\000\\000\\000\\000!\\000\\000\\000\\225\\000\\000\\000\\241\\000\\000\\000\\255\\000\\000\\000\\271\\000\\000\\000\\305\\000\\000\\000\\321\\000\\000\\000\\335\\000\\000\\000\\351\\000\\000\\000\\365\\000\\000\\001\\001\\000\\000\\001\\015\\000\\000\\001\\031\\000\\000\\001%\\000\\000\\0011\\000\\000\\001=\\000\\000\\001K\\000\\000\\001Y\\000\\000\\001g\\000\\000\\001u\\000\\000\\001\\203\\000\\000\\001\\221\\000\\000\\001\\237\\000\\000\\001\\267\\000\\000\\001\\317\\000\\000\\001\\360\\000\\000\\001\\376\\000\\000\\002\\014\\000\\000\\002\\032\\000\\000\\002(\\000\\000\\0023\\000\\000\\002V\\000\\000\\002q\\000\\000\\002\\224\\000\\000\\002\\257\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\031\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\015\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\001\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\365\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\351\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\335\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\321\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\305\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\271\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\255\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\241\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\225\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\211\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001}*\\264\\000c\\266\\001\\021\\300\\000\\344M\\247\\001o*\\264\\000c\\266\\001\\021\\300\\000\\344M\\247\\001a*\\264\\000_\\266\\001\\021\\300\\000\\344M\\247\\001S*\\264\\000_\\266\\001\\021\\300\\000\\344M\\247\\001E*\\264\\000U\\266\\001\\021\\300\\000\\344M\\247\\0017*\\264\\000U\\266\\001\\021\\300\\000\\344M\\247\\001)*\\264\\000S\\266\\001\\021\\300\\000\\344M\\247\\001\\033*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\347\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\001\\003*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\353\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\000\\353\\273\\000\\355Y*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\000\\362\\004\\244\\000\\007\\004\\247\\000\\004\\003\\267\\000\\365M\\247\\000\\312*\\264\\000Y\\266\\001\\021\\300\\000\\344M\\247\\000\\274*\\264\\000]\\266\\001\\021\\300\\000\\344M\\247\\000\\256*\\264\\000e\\266\\001\\021\\300\\000\\344M\\247\\000\\240*\\264\\000[\\266\\001\\021\\300\\000\\344M\\247\\000\\222\\273\\000\\367Y\\267\\000\\370M\\247\\000\\207\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000d\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000I\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000&\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000\\013\\273\\000\\367Y\\267\\000\\370M,\\260\\000\\000\\000\\001\\000v\\000\\000\\001\\032\\000F\\000\\000\\001L\\000\\002\\001N\\000\\230\\001R\\000\\241\\001S\\000\\244\\001W\\000\\255\\001X\\000\\260\\001\\\\\\000\\271\\001]\\000\\274\\001a\\000\\305\\001b\\000\\310\\001f\\000\\321\\001g\\000\\324\\001k\\000\\335\\001l\\000\\340\\001p\\000\\351\\001q\\000\\354\\001u\\000\\365\\001v\\000\\370\\001z\\001\\001\\001{\\001\\004\\001\\177\\001\\015\\001\\200\\001\\020\\001\\204\\001\\031\\001\\205\\001\\034\\001\\211\\001%\\001\\212\\001(\\001\\216\\0011\\001\\217\\0014\\001\\223\\001=\\001\\224\\001@\\001\\230\\001K\\001\\231\\001N\\001\\235\\001Y\\001\\236\\001\\\\\\001\\242\\001g\\001\\243\\001j\\001\\247\\001u\\001\\250\\001x\\001\\254\\001\\203\\001\\255\\001\\206\\001\\261\\001\\221\\001\\262\\001\\224\\001\\266\\001\\237\\001\\267\\001\\242\\001\\273\\001\\267\\001\\274\\001\\272\\001\\300\\001\\317\\001\\301\\001\\322\\001\\305\\001\\360\\001\\306\\001\\363\\001\\312\\001\\376\\001\\313\\002\\001\\001\\317\\002\\014\\001\\320\\002\\017\\001\\324\\002\\032\\001\\325\\002\\035\\001\\331\\002(\\001\\332\\002+\\001\\336\\0023\\001\\337\\0026\\001\\343\\002V\\001\\344\\002Y\\001\\350\\002q\\001\\351\\002t\\001\\355\\002\\224\\001\\356\\002\\227\\001\\362\\002\\257\\001\\363\\002\\262\\001\\367\\002\\272\\001\\377\\000\\001\\001\\023\\000\\326\\000\\002\\000\\327\\000\\000\\000\\004\\000\\001\\000\\331\\000-\\000\\000\\003\\350\\000\\004\\000\\003\\000\\000\\002\\274\\001M\\033\\252\\000\\000\\002\\267\\000\\000\\000\\000\\000\\000\\000!\\000\\000\\000\\225\\000\\000\\000\\241\\000\\000\\000\\255\\000\\000\\000\\271\\000\\000\\000\\305\\000\\000\\000\\321\\000\\000\\000\\335\\000\\000\\000\\351\\000\\000\\000\\365\\000\\000\\001\\001\\000\\000\\001\\015\\000\\000\\001\\031\\000\\000\\001%\\000\\000\\0011\\000\\000\\001=\\000\\000\\001K\\000\\000\\001Y\\000\\000\\001g\\000\\000\\001u\\000\\000\\001\\203\\000\\000\\001\\221\\000\\000\\001\\237\\000\\000\\001\\267\\000\\000\\001\\317\\000\\000\\001\\360\\000\\000\\001\\376\\000\\000\\002\\014\\000\\000\\002\\032\\000\\000\\002(\\000\\000\\0023\\000\\000\\002V\\000\\000\\002q\\000\\000\\002\\224\\000\\000\\002\\257\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\031\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\015\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\001\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\365\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\351\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\335\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\321\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\305\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\271\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\255\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\241\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\225\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\211\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001}*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001o*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001a*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001S*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001E*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\0017*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\001)*\\264\\000S\\266\\000\\342\\300\\000\\344M\\247\\001\\033*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\347\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\001\\003*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\353\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\000\\353\\273\\000\\355Y*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\000\\362\\004\\244\\000\\007\\004\\247\\000\\004\\003\\267\\000\\365M\\247\\000\\312*\\264\\000Y\\266\\000\\342\\300\\000\\344M\\247\\000\\274*\\264\\000]\\266\\000\\342\\300\\000\\344M\\247\\000\\256*\\264\\000e\\266\\000\\342\\300\\000\\344M\\247\\000\\240*\\264\\000[\\266\\000\\342\\300\\000\\344M\\247\\000\\222\\273\\000\\367Y\\267\\000\\370M\\247\\000\\207\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000d\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000I\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000&\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000\\013\\273\\000\\367Y\\267\\000\\370M,\\260\\000\\000\\000\\001\\000v\\000\\000\\001\\032\\000F\\000\\000\\002\\010\\000\\002\\002\\012\\000\\230\\002\\016\\000\\241\\002\\017\\000\\244\\002\\023\\000\\255\\002\\024\\000\\260\\002\\030\\000\\271\\002\\031\\000\\274\\002\\035\\000\\305\\002\\036\\000\\310\\002"\\000\\321\\002#\\000\\324\\002''\\000\\335\\002(\\000\\340\\002,\\000\\351\\002-\\000\\354\\0021\\000\\365\\0022\\000\\370\\0026\\001\\001\\0027\\001\\004\\002;\\001\\015\\002<\\001\\020\\002@\\001\\031\\002A\\001\\034\\002E\\001%\\002F\\001(\\002J\\0011\\002K\\0014\\002O\\001=\\002P\\001@\\002T\\001K\\002U\\001N\\002Y\\001Y\\002Z\\001\\\\\\002^\\001g\\002_\\001j\\002c\\001u\\002d\\001x\\002h\\001\\203\\002i\\001\\206\\002m\\001\\221\\002n\\001\\224\\002r\\001\\237\\002s\\001\\242\\002w\\001\\267\\002x\\001\\272\\002|\\001\\317\\002}\\001\\322\\002\\201\\001\\360\\002\\202\\001\\363\\002\\206\\001\\376\\002\\207\\002\\001\\002\\213\\002\\014\\002\\214\\002\\017\\002\\220\\002\\032\\002\\221\\002\\035\\002\\225\\002(\\002\\226\\002+\\002\\232\\0023\\002\\233\\0026\\002\\237\\002V\\002\\240\\002Y\\002\\244\\002q\\002\\245\\002t\\002\\251\\002\\224\\002\\252\\002\\227\\002\\256\\002\\257\\002\\257\\002\\262\\002\\263\\002\\272\\002\\273\\000\\001\\001\\027\\000\\000\\000\\002\\000\\001t\\000\\025_1314116315778_112849t\\0002net.sf.jasperreports.engine.design.JRJavacCompiler', '\\254\\355\\000\\005sr\\000(net.sf.jasperreports.engine.JasperReport\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\013compileDatat\\000\\026Ljava/io/Serializable;L\\000\\021compileNameSuffixt\\000\\022Ljava/lang/String;L\\000\\015compilerClassq\\000~\\000\\002xr\\000-net.sf.jasperreports.engine.base.JRBaseReport\\000\\000\\000\\000\\000\\000''\\330\\002\\000''I\\000\\014bottomMarginI\\000\\013columnCountI\\000\\015columnSpacingI\\000\\013columnWidthZ\\000\\020ignorePaginationZ\\000\\023isFloatColumnFooterZ\\000\\020isSummaryNewPageZ\\000 isSummaryWithPageHeaderAndFooterZ\\000\\016isTitleNewPageI\\000\\012leftMarginB\\000\\013orientationI\\000\\012pageHeightI\\000\\011pageWidthB\\000\\012printOrderI\\000\\013rightMarginI\\000\\011topMarginB\\000\\016whenNoDataTypeL\\000\\012backgroundt\\000$Lnet/sf/jasperreports/engine/JRBand;L\\000\\014columnFooterq\\000~\\000\\004L\\000\\014columnHeaderq\\000~\\000\\004[\\000\\010datasetst\\000([Lnet/sf/jasperreports/engine/JRDataset;L\\000\\013defaultFontt\\000*Lnet/sf/jasperreports/engine/JRReportFont;L\\000\\014defaultStylet\\000%Lnet/sf/jasperreports/engine/JRStyle;L\\000\\006detailq\\000~\\000\\004L\\000\\015detailSectiont\\000''Lnet/sf/jasperreports/engine/JRSection;[\\000\\005fontst\\000+[Lnet/sf/jasperreports/engine/JRReportFont;L\\000\\022formatFactoryClassq\\000~\\000\\002L\\000\\012importsSett\\000\\017Ljava/util/Set;L\\000\\010languageq\\000~\\000\\002L\\000\\016lastPageFooterq\\000~\\000\\004L\\000\\013mainDatasett\\000''Lnet/sf/jasperreports/engine/JRDataset;L\\000\\004nameq\\000~\\000\\002L\\000\\006noDataq\\000~\\000\\004L\\000\\012pageFooterq\\000~\\000\\004L\\000\\012pageHeaderq\\000~\\000\\004[\\000\\006stylest\\000&[Lnet/sf/jasperreports/engine/JRStyle;L\\000\\007summaryq\\000~\\000\\004[\\000\\011templatest\\000/[Lnet/sf/jasperreports/engine/JRReportTemplate;L\\000\\005titleq\\000~\\000\\004xp\\000\\000\\000\\024\\000\\000\\000\\001\\000\\000\\000\\000\\000\\000\\003\\016\\000\\000\\000\\000\\000\\000\\000\\000\\036\\002\\000\\000\\002S\\000\\000\\003J\\001\\000\\000\\000\\036\\000\\000\\000\\024\\001sr\\000+net.sf.jasperreports.engine.base.JRBaseBand\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\005I\\000\\031PSEUDO_SERIAL_VERSION_UIDI\\000\\006heightZ\\000\\016isSplitAllowedL\\000\\023printWhenExpressiont\\000*Lnet/sf/jasperreports/engine/JRExpression;L\\000\\011splitTypet\\000\\020Ljava/lang/Byte;xr\\0003net.sf.jasperreports.engine.base.JRBaseElementGroup\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\002L\\000\\010childrent\\000\\020Ljava/util/List;L\\000\\014elementGroupt\\000,Lnet/sf/jasperreports/engine/JRElementGroup;xpsr\\000\\023java.util.ArrayListx\\201\\322\\035\\231\\307a\\235\\003\\000\\001I\\000\\004sizexp\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001psr\\000\\016java.lang.Byte\\234N`\\204\\356P\\365\\034\\002\\000\\001B\\000\\005valuexr\\000\\020java.lang.Number\\206\\254\\225\\035\\013\\224\\340\\213\\002\\000\\000xp\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\005\\001pq\\000~\\000\\032sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\005w\\004\\000\\000\\000\\012sr\\0000net.sf.jasperreports.engine.base.JRBaseRectangle\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001L\\000\\006radiust\\000\\023Ljava/lang/Integer;xr\\0005net.sf.jasperreports.engine.base.JRBaseGraphicElement\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\004fillq\\000~\\000\\021L\\000\\007linePent\\000#Lnet/sf/jasperreports/engine/JRPen;L\\000\\003penq\\000~\\000\\021xr\\000.net.sf.jasperreports.engine.base.JRBaseElement\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\026I\\000\\006heightZ\\000\\027isPrintInFirstWholeBandZ\\000\\025isPrintRepeatedValuesZ\\000\\032isPrintWhenDetailOverflowsZ\\000\\025isRemoveLineWhenBlankB\\000\\014positionTypeB\\000\\013stretchTypeI\\000\\005widthI\\000\\001xI\\000\\001yL\\000\\011backcolort\\000\\020Ljava/awt/Color;L\\000\\024defaultStyleProvidert\\0004Lnet/sf/jasperreports/engine/JRDefaultStyleProvider;L\\000\\014elementGroupq\\000~\\000\\024L\\000\\011forecolorq\\000~\\000$L\\000\\003keyq\\000~\\000\\002L\\000\\004modeq\\000~\\000\\021L\\000\\013parentStyleq\\000~\\000\\007L\\000\\030parentStyleNameReferenceq\\000~\\000\\002L\\000\\023printWhenExpressionq\\000~\\000\\020L\\000\\025printWhenGroupChangest\\000%Lnet/sf/jasperreports/engine/JRGroup;L\\000\\015propertiesMapt\\000-Lnet/sf/jasperreports/engine/JRPropertiesMap;[\\000\\023propertyExpressionst\\0003[Lnet/sf/jasperreports/engine/JRPropertyExpression;xp\\000\\000\\000 \\000\\001\\000\\000\\002\\000\\000\\000\\003\\012\\000\\000\\000\\001\\000\\000\\000\\002sr\\000\\016java.awt.Color\\001\\245\\027\\203\\020\\2173u\\002\\000\\005F\\000\\006falphaI\\000\\005valueL\\000\\002cst\\000\\033Ljava/awt/color/ColorSpace;[\\000\\011frgbvaluet\\000\\002[F[\\000\\006fvalueq\\000~\\000,xp\\000\\000\\000\\000\\377\\360\\360\\360pppq\\000~\\000\\016q\\000~\\000\\035pt\\000\\013rectangle-1sq\\000~\\000\\030\\001pppppppsr\\000*net.sf.jasperreports.engine.base.JRBasePen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\004L\\000\\011lineColorq\\000~\\000$L\\000\\011lineStyleq\\000~\\000\\021L\\000\\011lineWidtht\\000\\021Ljava/lang/Float;L\\000\\014penContainert\\000,Lnet/sf/jasperreports/engine/JRPenContainer;xppsq\\000~\\000\\030\\000sr\\000\\017java.lang.Float\\332\\355\\311\\242\\333<\\360\\354\\002\\000\\001F\\000\\005valuexq\\000~\\000\\031\\000\\000\\000\\000q\\000~\\000)ppsr\\0001net.sf.jasperreports.engine.base.JRBaseStaticText\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001L\\000\\004textq\\000~\\000\\002xr\\0002net.sf.jasperreports.engine.base.JRBaseTextElement\\000\\000\\000\\000\\000\\000''\\330\\002\\000 L\\000\\006borderq\\000~\\000\\021L\\000\\013borderColorq\\000~\\000$L\\000\\014bottomBorderq\\000~\\000\\021L\\000\\021bottomBorderColorq\\000~\\000$L\\000\\015bottomPaddingq\\000~\\000 L\\000\\010fontNameq\\000~\\000\\002L\\000\\010fontSizeq\\000~\\000 L\\000\\023horizontalAlignmentq\\000~\\000\\021L\\000\\006isBoldt\\000\\023Ljava/lang/Boolean;L\\000\\010isItalicq\\000~\\0009L\\000\\015isPdfEmbeddedq\\000~\\0009L\\000\\017isStrikeThroughq\\000~\\0009L\\000\\014isStyledTextq\\000~\\0009L\\000\\013isUnderlineq\\000~\\0009L\\000\\012leftBorderq\\000~\\000\\021L\\000\\017leftBorderColorq\\000~\\000$L\\000\\013leftPaddingq\\000~\\000 L\\000\\007lineBoxt\\000''Lnet/sf/jasperreports/engine/JRLineBox;L\\000\\013lineSpacingq\\000~\\000\\021L\\000\\006markupq\\000~\\000\\002L\\000\\007paddingq\\000~\\000 L\\000\\013pdfEncodingq\\000~\\000\\002L\\000\\013pdfFontNameq\\000~\\000\\002L\\000\\012reportFontq\\000~\\000\\006L\\000\\013rightBorderq\\000~\\000\\021L\\000\\020rightBorderColorq\\000~\\000$L\\000\\014rightPaddingq\\000~\\000 L\\000\\010rotationq\\000~\\000\\021L\\000\\011topBorderq\\000~\\000\\021L\\000\\016topBorderColorq\\000~\\000$L\\000\\012topPaddingq\\000~\\000 L\\000\\021verticalAlignmentq\\000~\\000\\021xq\\000~\\000#\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000_\\000\\000\\000M\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\014staticText-3pppppppppppppsr\\000\\021java.lang.Integer\\022\\342\\240\\244\\367\\201\\2078\\002\\000\\001I\\000\\005valuexq\\000~\\000\\031\\000\\000\\000\\012psr\\000\\021java.lang.Boolean\\315 r\\200\\325\\234\\372\\356\\002\\000\\001Z\\000\\005valuexp\\001ppppppppsr\\000.net.sf.jasperreports.engine.base.JRBaseLineBox\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\013L\\000\\015bottomPaddingq\\000~\\000 L\\000\\011bottomPent\\000+Lnet/sf/jasperreports/engine/base/JRBoxPen;L\\000\\014boxContainert\\000,Lnet/sf/jasperreports/engine/JRBoxContainer;L\\000\\013leftPaddingq\\000~\\000 L\\000\\007leftPenq\\000~\\000BL\\000\\007paddingq\\000~\\000 L\\000\\003penq\\000~\\000BL\\000\\014rightPaddingq\\000~\\000 L\\000\\010rightPenq\\000~\\000BL\\000\\012topPaddingq\\000~\\000 L\\000\\006topPenq\\000~\\000Bxppsr\\0003net.sf.jasperreports.engine.base.JRBaseBoxBottomPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xr\\000-net.sf.jasperreports.engine.base.JRBaseBoxPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001L\\000\\007lineBoxq\\000~\\000:xq\\000~\\0000sq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dq\\000~\\000;psr\\0001net.sf.jasperreports.engine.base.JRBaseBoxLeftPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xq\\000~\\000Fsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dpsq\\000~\\000Fpppq\\000~\\000Dq\\000~\\000Dpsr\\0002net.sf.jasperreports.engine.base.JRBaseBoxRightPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xq\\000~\\000Fsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dpsr\\0000net.sf.jasperreports.engine.base.JRBaseBoxTopPen\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\000xq\\000~\\000Fsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000Dq\\000~\\000Dppppt\\000\\016Helvetica-Boldpppppppppt\\000\\013Asset Brandsq\\000~\\0007\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000_\\000\\000\\000M\\000\\000\\000\\022pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\015staticText-10pppppppppppppsq\\000~\\000=\\000\\000\\000\\012pq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\q\\000~\\000Ypsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\psq\\000~\\000Fpppq\\000~\\000\\\\q\\000~\\000\\\\psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\\\q\\000~\\000\\\\ppppt\\000\\016Helvetica-Boldpppppppppt\\000\\016Asset Assigneesq\\000~\\0007\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000\\213\\000\\000\\000\\264\\000\\000\\000\\022pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\015staticText-11pppppppppppppsq\\000~\\000=\\000\\000\\000\\012pq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000oq\\000~\\000lpsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000opsq\\000~\\000Fpppq\\000~\\000oq\\000~\\000opsq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000opsq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000oq\\000~\\000oppppt\\000\\016Helvetica-Boldpppppppppt\\000\\016Assignee emailsq\\000~\\0007\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000\\212\\000\\000\\000\\264\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\000\\035pt\\000\\015staticText-12pppppppppppppsq\\000~\\000=\\000\\000\\000\\012pq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202q\\000~\\000\\177psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202psq\\000~\\000Fpppq\\000~\\000\\202q\\000~\\000\\202psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\202q\\000~\\000\\202ppppt\\000\\016Helvetica-Boldpppppppppt\\000\\021Asset Descriptionxp\\000\\000w&\\000\\000\\000''\\001pq\\000~\\000\\032ppppsr\\000.net.sf.jasperreports.engine.base.JRBaseSection\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001[\\000\\005bandst\\000%[Lnet/sf/jasperreports/engine/JRBand;xpur\\000%[Lnet.sf.jasperreports.engine.JRBand;\\225\\335~\\354\\214\\312\\2055\\002\\000\\000xp\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\005w\\004\\000\\000\\000\\012sr\\0000net.sf.jasperreports.engine.base.JRBaseTextField\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\021I\\000\\015bookmarkLevelB\\000\\016evaluationTimeB\\000\\017hyperlinkTargetB\\000\\015hyperlinkTypeZ\\000\\025isStretchWithOverflowL\\000\\024anchorNameExpressionq\\000~\\000\\020L\\000\\017evaluationGroupq\\000~\\000&L\\000\\012expressionq\\000~\\000\\020L\\000\\031hyperlinkAnchorExpressionq\\000~\\000\\020L\\000\\027hyperlinkPageExpressionq\\000~\\000\\020[\\000\\023hyperlinkParameterst\\0003[Lnet/sf/jasperreports/engine/JRHyperlinkParameter;L\\000\\034hyperlinkReferenceExpressionq\\000~\\000\\020L\\000\\032hyperlinkTooltipExpressionq\\000~\\000\\020L\\000\\017isBlankWhenNullq\\000~\\0009L\\000\\012linkTargetq\\000~\\000\\002L\\000\\010linkTypeq\\000~\\000\\002L\\000\\007patternq\\000~\\000\\002xq\\000~\\0008\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\001\\000\\000\\000\\000\\263\\000\\000\\000\\017pq\\000~\\000\\016q\\000~\\000\\227pt\\000\\011textFieldpppppppppppppsq\\000~\\000=\\000\\000\\000\\011ppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236q\\000~\\000\\233psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236psq\\000~\\000Fpppq\\000~\\000\\236q\\000~\\000\\236psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\236q\\000~\\000\\236pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\001ppsr\\0001net.sf.jasperreports.engine.base.JRBaseExpression\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\004I\\000\\002id[\\000\\006chunkst\\0000[Lnet/sf/jasperreports/engine/JRExpressionChunk;L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xp\\000\\000\\000\\030ur\\0000[Lnet.sf.jasperreports.engine.JRExpressionChunk;mY\\317\\336iK\\243U\\002\\000\\000xp\\000\\000\\000\\001sr\\0006net.sf.jasperreports.engine.base.JRBaseExpressionChunk\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\002B\\000\\004typeL\\000\\004textq\\000~\\000\\002xp\\003t\\000\\005Emailt\\000\\020java.lang.Stringppppppq\\000~\\000@pppsr\\000+net.sf.jasperreports.engine.base.JRBaseLine\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\001B\\000\\011directionxq\\000~\\000!\\000\\000\\000\\001\\000\\001\\000\\000\\002\\000\\000\\000\\003\\015\\000\\000\\000\\001\\000\\000\\000\\037pq\\000~\\000\\016q\\000~\\000\\227sq\\000~\\000*\\000\\000\\000\\000\\377\\313\\307\\307pppt\\000\\006line-1ppppppppsq\\000~\\0000pppq\\000~\\000\\266p\\001sq\\000~\\000\\231\\000\\000\\000\\017\\000\\001\\000\\000\\002\\000\\000\\000\\000d\\000\\000\\000H\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\000\\227pppppppppppppppsq\\000~\\000=\\000\\000\\000\\012ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\000\\274q\\000~\\000\\274q\\000~\\000\\272psq\\000~\\000Jpppq\\000~\\000\\274q\\000~\\000\\274psq\\000~\\000Fpppq\\000~\\000\\274q\\000~\\000\\274psq\\000~\\000Opppq\\000~\\000\\274q\\000~\\000\\274psq\\000~\\000Spppq\\000~\\000\\274q\\000~\\000\\274pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\031uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\012AssetBrandt\\000\\020java.lang.Stringppppppq\\000~\\000@pppsq\\000~\\000\\231\\000\\000\\000\\016\\000\\001\\000\\000\\002\\000\\000\\000\\000d\\000\\000\\000H\\000\\000\\000\\017pq\\000~\\000\\016q\\000~\\000\\227pppppppppppppppsq\\000~\\000=\\000\\000\\000\\012ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\000\\311q\\000~\\000\\311q\\000~\\000\\307psq\\000~\\000Jpppq\\000~\\000\\311q\\000~\\000\\311psq\\000~\\000Fpppq\\000~\\000\\311q\\000~\\000\\311psq\\000~\\000Opppq\\000~\\000\\311q\\000~\\000\\311psq\\000~\\000Spppq\\000~\\000\\311q\\000~\\000\\311pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\032uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\010Assigneet\\000\\020java.lang.Stringppppppq\\000~\\000@pppsq\\000~\\000\\231\\000\\000\\000\\017\\000\\001\\000\\000\\002\\000\\000\\000\\001\\000\\000\\000\\000\\263\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\000\\227pppppppppppppppsq\\000~\\000=\\000\\000\\000\\012ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\000\\326q\\000~\\000\\326q\\000~\\000\\324psq\\000~\\000Jpppq\\000~\\000\\326q\\000~\\000\\326psq\\000~\\000Fpppq\\000~\\000\\326q\\000~\\000\\326psq\\000~\\000Opppq\\000~\\000\\326q\\000~\\000\\326psq\\000~\\000Spppq\\000~\\000\\326q\\000~\\000\\326pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\033uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\020AssetDescriptiont\\000\\020java.lang.Stringppppppq\\000~\\000@pppxp\\000\\000w&\\000\\000\\000!\\001pq\\000~\\000\\032ppsr\\000\\021java.util.HashSet\\272D\\205\\225\\226\\270\\2674\\003\\000\\000xpw\\014\\000\\000\\000\\004?@\\000\\000\\000\\000\\000\\003t\\000"net.sf.jasperreports.engine.data.*t\\000\\035net.sf.jasperreports.engine.*t\\000\\013java.util.*xt\\000\\004javasq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\004w\\004\\000\\000\\000\\012sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000M\\000\\000\\002\\254\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\013textField-1ppppppppppppppsq\\000~\\000\\030\\003pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354q\\000~\\000\\351psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354psq\\000~\\000Fpppq\\000~\\000\\354q\\000~\\000\\354psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\000\\354q\\000~\\000\\354pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\037uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\012"Page " + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\011 + " di "t\\000\\020java.lang.Stringppppppsq\\000~\\000?\\000pppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\024\\000\\000\\002\\371\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\013textField-2ppppppppppppppq\\000~\\000\\353pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006q\\000~\\001\\004psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006psq\\000~\\000Fpppq\\000~\\001\\006q\\000~\\001\\006psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\006q\\000~\\001\\006pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000 uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\005"" + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\005 + ""t\\000\\020java.lang.Stringppppppq\\000~\\001\\003pppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000H\\000\\000\\000\\037\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\013textField-3ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037q\\000~\\001\\035psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037psq\\000~\\000Fpppq\\000~\\001\\037q\\000~\\001\\037psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\037q\\000~\\001\\037pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000!uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\024new java.util.Date()t\\000\\016java.util.Dateppppppq\\000~\\001\\003ppt\\000\\012MM/dd/yyyysq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\034\\000\\000\\000\\001\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\000\\347pt\\000\\015staticText-26ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015q\\000~\\0013psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015psq\\000~\\000Fpppq\\000~\\0015q\\000~\\0015psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0015q\\000~\\0015ppppppppppppppt\\000\\005Date:xp\\000\\000w&\\000\\000\\000\\032\\001pq\\000~\\000\\032sr\\000.net.sf.jasperreports.engine.base.JRBaseDataset\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\016Z\\000\\006isMainB\\000\\027whenResourceMissingType[\\000\\006fieldst\\000&[Lnet/sf/jasperreports/engine/JRField;L\\000\\020filterExpressionq\\000~\\000\\020[\\000\\006groupst\\000&[Lnet/sf/jasperreports/engine/JRGroup;L\\000\\004nameq\\000~\\000\\002[\\000\\012parameterst\\000*[Lnet/sf/jasperreports/engine/JRParameter;L\\000\\015propertiesMapq\\000~\\000''L\\000\\005queryt\\000%Lnet/sf/jasperreports/engine/JRQuery;L\\000\\016resourceBundleq\\000~\\000\\002L\\000\\016scriptletClassq\\000~\\000\\002[\\000\\012scriptletst\\000*[Lnet/sf/jasperreports/engine/JRScriptlet;[\\000\\012sortFieldst\\000*[Lnet/sf/jasperreports/engine/JRSortField;[\\000\\011variablest\\000)[Lnet/sf/jasperreports/engine/JRVariable;xp\\001\\001ur\\000&[Lnet.sf.jasperreports.engine.JRField;\\002<\\337\\307N*\\362p\\002\\000\\000xp\\000\\000\\000\\013sr\\000,net.sf.jasperreports.engine.base.JRBaseField\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\005L\\000\\013descriptionq\\000~\\000\\002L\\000\\004nameq\\000~\\000\\002L\\000\\015propertiesMapq\\000~\\000''L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xpt\\000\\000t\\000\\011AssetCodesr\\000+net.sf.jasperreports.engine.JRPropertiesMap\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\004baseq\\000~\\000''L\\000\\016propertiesListq\\000~\\000\\023L\\000\\015propertiesMapt\\000\\017Ljava/util/Map;xppppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\020AssetDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\012AssetBrandsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\015WorkplaceCodesq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\024WorkplaceDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\010Assigneesq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\005Emailsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\010RoomCodesq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\017RoomDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\020FloorDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringpsq\\000~\\001Ot\\000\\000t\\000\\023BuildingDescriptionsq\\000~\\001Spppt\\000\\020java.lang.Stringppur\\000&[Lnet.sf.jasperreports.engine.JRGroup;@\\243_zL\\375x\\352\\002\\000\\000xp\\000\\000\\000\\003sr\\000,net.sf.jasperreports.engine.base.JRBaseGroup\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\016B\\000\\016footerPositionZ\\000\\031isReprintHeaderOnEachPageZ\\000\\021isResetPageNumberZ\\000\\020isStartNewColumnZ\\000\\016isStartNewPageZ\\000\\014keepTogetherI\\000\\027minHeightToStartNewPageL\\000\\015countVariablet\\000(Lnet/sf/jasperreports/engine/JRVariable;L\\000\\012expressionq\\000~\\000\\020L\\000\\013groupFooterq\\000~\\000\\004L\\000\\022groupFooterSectionq\\000~\\000\\010L\\000\\013groupHeaderq\\000~\\000\\004L\\000\\022groupHeaderSectionq\\000~\\000\\010L\\000\\004nameq\\000~\\000\\002xp\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000sr\\000/net.sf.jasperreports.engine.base.JRBaseVariable\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\015B\\000\\013calculationB\\000\\015incrementTypeZ\\000\\017isSystemDefinedB\\000\\011resetTypeL\\000\\012expressionq\\000~\\000\\020L\\000\\016incrementGroupq\\000~\\000&L\\000\\033incrementerFactoryClassNameq\\000~\\000\\002L\\000\\037incrementerFactoryClassRealNameq\\000~\\000\\002L\\000\\026initialValueExpressionq\\000~\\000\\020L\\000\\004nameq\\000~\\000\\002L\\000\\012resetGroupq\\000~\\000&L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xp\\001\\005\\001\\004sq\\000~\\000\\254\\000\\000\\000\\010uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)t\\000\\021java.lang.Integerppppsq\\000~\\000\\254\\000\\000\\000\\011uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\015palazzo_COUNTq\\000~\\001\\215q\\000~\\001\\224psq\\000~\\000\\254\\000\\000\\000\\016uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\023BuildingDescriptiont\\000\\020java.lang.Objectppsq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001pq\\000~\\000\\032psq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\000\\037\\000\\000\\000\\021\\000\\001\\000\\000\\002\\000\\000\\000\\003\\012\\000\\000\\000\\001\\000\\000\\000\\006sq\\000~\\000*\\000\\000\\000\\000\\377\\340\\372\\351pppq\\000~\\000\\016q\\000~\\001\\245pt\\000\\013rectangle-2q\\000~\\000/pppppppsq\\000~\\0000pq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\247ppsq\\000~\\0007\\000\\000\\000\\026\\000\\001\\000\\000\\002\\000\\000\\000\\0008\\000\\000\\000\\004\\000\\000\\000\\004pq\\000~\\000\\016q\\000~\\001\\245sq\\000~\\000*\\000\\000\\000\\000\\377\\000ffpppt\\000\\015staticText-19pppppppppppppsq\\000~\\000=\\000\\000\\000\\016ppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260q\\000~\\001\\254psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260psq\\000~\\000Fpppq\\000~\\001\\260q\\000~\\001\\260psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\260q\\000~\\001\\260ppppppppppppppt\\000\\011Building:sq\\000~\\000\\231\\000\\000\\000\\024\\000\\001\\000\\000\\002\\000\\000\\000\\000\\304\\000\\000\\000?\\000\\000\\000\\004pq\\000~\\000\\016q\\000~\\001\\245pppppppppppppppsq\\000~\\000=\\000\\000\\000\\016ppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\001\\301q\\000~\\001\\301q\\000~\\001\\277psq\\000~\\000Jpppq\\000~\\001\\301q\\000~\\001\\301psq\\000~\\000Fpppq\\000~\\001\\301q\\000~\\001\\301psq\\000~\\000Opppq\\000~\\001\\301q\\000~\\001\\301psq\\000~\\000Spppq\\000~\\001\\301q\\000~\\001\\301pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\017uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\023BuildingDescriptiont\\000\\020java.lang.Stringppppppppppxp\\000\\000w&\\000\\000\\000\\033\\001pq\\000~\\000\\032t\\000\\007palazzosq\\000~\\001\\213\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000sq\\000~\\001\\216\\001\\005\\001\\004sq\\000~\\000\\254\\000\\000\\000\\012uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\013uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014tavola_COUNTq\\000~\\001\\315q\\000~\\001\\224psq\\000~\\000\\254\\000\\000\\000\\020uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\020FloorDescriptionq\\000~\\001\\236ppsq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001pq\\000~\\000\\032psq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\000\\037\\000\\000\\000\\023\\000\\001\\000\\000\\002\\000\\000\\000\\002\\371\\000\\000\\000\\022\\000\\000\\000\\004sq\\000~\\000*\\000\\000\\000\\000\\377\\365\\354\\354pppq\\000~\\000\\016q\\000~\\001\\342pt\\000\\013rectangle-3q\\000~\\000/pppppppsq\\000~\\0000pq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\344ppsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000(\\000\\000\\000\\027\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\001\\342sq\\000~\\000*\\000\\000\\000\\000\\377f\\000\\000pppt\\000\\015staticText-20pppppppppppppsq\\000~\\000=\\000\\000\\000\\014ppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355q\\000~\\001\\351psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355psq\\000~\\000Fpppq\\000~\\001\\355q\\000~\\001\\355psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\001\\355q\\000~\\001\\355ppppppppppppppt\\000\\006Floor:sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\314\\000\\000\\000H\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\001\\342ppppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\001\\375q\\000~\\001\\375q\\000~\\001\\374psq\\000~\\000Jpppq\\000~\\001\\375q\\000~\\001\\375psq\\000~\\000Fpppq\\000~\\001\\375q\\000~\\001\\375psq\\000~\\000Opppq\\000~\\001\\375q\\000~\\001\\375psq\\000~\\000Spppq\\000~\\001\\375q\\000~\\001\\375pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\021uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\020FloorDescriptiont\\000\\020java.lang.Stringppppppppppxp\\000\\000w&\\000\\000\\000\\033\\001pq\\000~\\000\\032t\\000\\006tavolasq\\000~\\001\\213\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000sq\\000~\\001\\216\\001\\005\\001\\004sq\\000~\\000\\254\\000\\000\\000\\014uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\015uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014stanza_COUNTq\\000~\\002\\011q\\000~\\001\\224psq\\000~\\000\\254\\000\\000\\000\\022uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\010RoomCodeq\\000~\\001\\236ppsq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\000\\001pq\\000~\\000\\032psq\\000~\\000\\222uq\\000~\\000\\225\\000\\000\\000\\001sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\004w\\004\\000\\000\\000\\012sq\\000~\\000\\037\\000\\000\\000\\023\\000\\001\\000\\000\\002\\000\\000\\000\\002\\343\\000\\000\\000(\\000\\000\\000\\005sq\\000~\\000*\\000\\000\\000\\000\\377\\342\\372\\372pppq\\000~\\000\\016q\\000~\\002\\036pt\\000\\013rectangle-4q\\000~\\000/pppppppsq\\000~\\0000pq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002 ppsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000-\\000\\000\\000,\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\002\\036sq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\231pppt\\000\\015staticText-21ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(q\\000~\\002%psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(psq\\000~\\000Fpppq\\000~\\002(q\\000~\\002(psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002(q\\000~\\002(ppppppppppppppt\\000\\005Room:sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000d\\000\\000\\000\\\\\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\002\\036ppppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\0028q\\000~\\0028q\\000~\\0027psq\\000~\\000Jpppq\\000~\\0028q\\000~\\0028psq\\000~\\000Fpppq\\000~\\0028q\\000~\\0028psq\\000~\\000Opppq\\000~\\0028q\\000~\\0028psq\\000~\\000Spppq\\000~\\0028q\\000~\\0028pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\023uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\010RoomCodet\\000\\020java.lang.Stringppppppppppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\325\\000\\000\\000\\316\\000\\000\\000\\005pq\\000~\\000\\016q\\000~\\002\\036ppppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\002Dq\\000~\\002Dq\\000~\\002Cpsq\\000~\\000Jpppq\\000~\\002Dq\\000~\\002Dpsq\\000~\\000Fpppq\\000~\\002Dq\\000~\\002Dpsq\\000~\\000Opppq\\000~\\002Dq\\000~\\002Dpsq\\000~\\000Spppq\\000~\\002Dq\\000~\\002Dpppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\024uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\003t\\000\\017RoomDescriptiont\\000\\020java.lang.Stringppppppppppxp\\000\\000w&\\000\\000\\000\\033\\001pq\\000~\\000\\032t\\000\\006stanzat\\000\\011AssetListur\\000*[Lnet.sf.jasperreports.engine.JRParameter;"\\000\\014\\215*\\303`!\\002\\000\\000xp\\000\\000\\000\\020sr\\0000net.sf.jasperreports.engine.base.JRBaseParameter\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\011Z\\000\\016isForPromptingZ\\000\\017isSystemDefinedL\\000\\026defaultValueExpressionq\\000~\\000\\020L\\000\\013descriptionq\\000~\\000\\002L\\000\\004nameq\\000~\\000\\002L\\000\\016nestedTypeNameq\\000~\\000\\002L\\000\\015propertiesMapq\\000~\\000''L\\000\\016valueClassNameq\\000~\\000\\002L\\000\\022valueClassRealNameq\\000~\\000\\002xp\\001\\001ppt\\000\\025REPORT_PARAMETERS_MAPpsq\\000~\\001Spppt\\000\\015java.util.Mappsq\\000~\\002S\\001\\001ppt\\000\\015JASPER_REPORTpsq\\000~\\001Spppt\\000(net.sf.jasperreports.engine.JasperReportpsq\\000~\\002S\\001\\001ppt\\000\\021REPORT_CONNECTIONpsq\\000~\\001Spppt\\000\\023java.sql.Connectionpsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_MAX_COUNTpsq\\000~\\001Spppq\\000~\\001\\224psq\\000~\\002S\\001\\001ppt\\000\\022REPORT_DATA_SOURCEpsq\\000~\\001Spppt\\000(net.sf.jasperreports.engine.JRDataSourcepsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_SCRIPTLETpsq\\000~\\001Spppt\\000/net.sf.jasperreports.engine.JRAbstractScriptletpsq\\000~\\002S\\001\\001ppt\\000\\015REPORT_LOCALEpsq\\000~\\001Spppt\\000\\020java.util.Localepsq\\000~\\002S\\001\\001ppt\\000\\026REPORT_RESOURCE_BUNDLEpsq\\000~\\001Spppt\\000\\030java.util.ResourceBundlepsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_TIME_ZONEpsq\\000~\\001Spppt\\000\\022java.util.TimeZonepsq\\000~\\002S\\001\\001ppt\\000\\025REPORT_FORMAT_FACTORYpsq\\000~\\001Spppt\\000.net.sf.jasperreports.engine.util.FormatFactorypsq\\000~\\002S\\001\\001ppt\\000\\023REPORT_CLASS_LOADERpsq\\000~\\001Spppt\\000\\025java.lang.ClassLoaderpsq\\000~\\002S\\001\\001ppt\\000\\032REPORT_URL_HANDLER_FACTORYpsq\\000~\\001Spppt\\000 java.net.URLStreamHandlerFactorypsq\\000~\\002S\\001\\001ppt\\000\\024REPORT_FILE_RESOLVERpsq\\000~\\001Spppt\\000-net.sf.jasperreports.engine.util.FileResolverpsq\\000~\\002S\\001\\001ppt\\000\\022REPORT_VIRTUALIZERpsq\\000~\\001Spppt\\000)net.sf.jasperreports.engine.JRVirtualizerpsq\\000~\\002S\\001\\001ppt\\000\\024IS_IGNORE_PAGINATIONpsq\\000~\\001Spppt\\000\\021java.lang.Booleanpsq\\000~\\002S\\001\\001ppt\\000\\020REPORT_TEMPLATESpsq\\000~\\001Spppt\\000\\024java.util.Collectionpsq\\000~\\001Spsq\\000~\\000\\026\\000\\000\\000\\005w\\004\\000\\000\\000\\012t\\000\\031ireport.scriptlethandlingt\\000\\020ireport.encodingt\\000\\014ireport.zoomt\\000\\011ireport.xt\\000\\011ireport.yxsr\\000\\021java.util.HashMap\\005\\007\\332\\301\\303\\026`\\321\\003\\000\\002F\\000\\012loadFactorI\\000\\011thresholdxp?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\005q\\000~\\002\\227t\\000\\0031.0q\\000~\\002\\226t\\000\\005UTF-8q\\000~\\002\\230t\\000\\0010q\\000~\\002\\231t\\000\\0010q\\000~\\002\\225t\\000\\0012xsr\\000,net.sf.jasperreports.engine.base.JRBaseQuery\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\002[\\000\\006chunkst\\000+[Lnet/sf/jasperreports/engine/JRQueryChunk;L\\000\\010languageq\\000~\\000\\002xpur\\000+[Lnet.sf.jasperreports.engine.JRQueryChunk;@\\237\\000\\241\\350\\2724\\244\\002\\000\\000xp\\000\\000\\000\\001sr\\0001net.sf.jasperreports.engine.base.JRBaseQueryChunk\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003B\\000\\004typeL\\000\\004textq\\000~\\000\\002[\\000\\006tokenst\\000\\023[Ljava/lang/String;xp\\001t\\004\\320SELECT\\012"Asset"."Code" AS "AssetCode", max("Asset"."Description") AS "AssetDescription", max("LookUp1"."Description") AS "AssetBrand",\\012"Workplace"."Code" AS "WorkplaceCode", max("Workplace"."Description") AS "WorkplaceDescription", max("Employee"."Description") as "Assignee", max(lower("Employee"."Email")) as "Email",\\012coalesce("Room"."Code", ''Not defined'') AS "RoomCode",\\012max(coalesce("Room"."Description",''Not defined'')) AS "RoomDescription",\\012max(coalesce("Floor"."Description" ,''Not defined'')) AS "FloorDescription",\\012max(coalesce("Building"."Description",''Not defined'')) AS "BuildingDescription"\\012FROM "Asset"\\012LEFT OUTER JOIN "Workplace" ON "Workplace"."Id"="Asset"."Workplace" AND "Workplace"."Status"=''A''\\012LEFT OUTER JOIN "Employee" ON "Employee"."Id"="Asset"."Assignee" AND "Employee"."Status"=''A''\\012LEFT OUTER JOIN "Room" ON "Room"."Id"="Asset"."Room" AND "Room"."Status"=''A''\\012LEFT OUTER JOIN "Floor" ON "Floor"."Id"="Room"."Floor" AND "Floor"."Status"=''A''\\012LEFT OUTER JOIN "Building" ON "Building"."Id"="Floor"."Building" AND "Building"."Status"=''A''\\012LEFT OUTER JOIN "LookUp" AS "LookUp1" ON "LookUp1"."Id"="Asset"."Brand"\\012WHERE "Asset"."Status"=''A''\\012GROUP BY "Room"."Code", "Workplace"."Code", "Asset"."Code"\\012ORDER BY "Room"."Code"pt\\000\\003sqlppppur\\000)[Lnet.sf.jasperreports.engine.JRVariable;b\\346\\203|\\230,\\267D\\002\\000\\000xp\\000\\000\\000\\010sq\\000~\\001\\216\\010\\005\\001\\001ppppsq\\000~\\000\\254\\000\\000\\000\\000uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224pt\\000\\013PAGE_NUMBERpq\\000~\\001\\224psq\\000~\\001\\216\\010\\005\\001\\002ppppsq\\000~\\000\\254\\000\\000\\000\\001uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224pt\\000\\015COLUMN_NUMBERpq\\000~\\001\\224psq\\000~\\001\\216\\001\\005\\001\\001sq\\000~\\000\\254\\000\\000\\000\\002uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\003uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014REPORT_COUNTpq\\000~\\001\\224psq\\000~\\001\\216\\001\\005\\001\\002sq\\000~\\000\\254\\000\\000\\000\\004uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\005uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\012PAGE_COUNTpq\\000~\\001\\224psq\\000~\\001\\216\\001\\005\\001\\003sq\\000~\\000\\254\\000\\000\\000\\006uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(1)q\\000~\\001\\224ppppsq\\000~\\000\\254\\000\\000\\000\\007uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\030new java.lang.Integer(0)q\\000~\\001\\224pt\\000\\014COLUMN_COUNTpq\\000~\\001\\224pq\\000~\\001\\217q\\000~\\001\\316q\\000~\\002\\012q\\000~\\002Ppsq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\004w\\004\\000\\000\\000\\012sq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000H\\000\\000\\000\\037\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\011textFieldppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333q\\000~\\002\\331psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333psq\\000~\\000Fpppq\\000~\\002\\333q\\000~\\002\\333psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\333q\\000~\\002\\333pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\034uq\\000~\\000\\257\\000\\000\\000\\001sq\\000~\\000\\261\\001t\\000\\024new java.util.Date()t\\000\\016java.util.Dateppppppq\\000~\\001\\003ppt\\000\\012MM/dd/yyyysq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000M\\000\\000\\002\\254\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\011textFieldppppppppppppppq\\000~\\000\\353pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361q\\000~\\002\\357psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361psq\\000~\\000Fpppq\\000~\\002\\361q\\000~\\002\\361psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\002\\361q\\000~\\002\\361pppppppppppppp\\000\\000\\000\\000\\001\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\035uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\012"Page " + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\011 + " di "t\\000\\020java.lang.Stringppppppq\\000~\\001\\003pppsq\\000~\\000\\231\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\024\\000\\000\\002\\371\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\011textFieldppppppppppppppq\\000~\\000\\353pppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012q\\000~\\003\\010psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012psq\\000~\\000Fpppq\\000~\\003\\012q\\000~\\003\\012psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003\\012q\\000~\\003\\012pppppppppppppp\\000\\000\\000\\000\\002\\001\\000\\000ppsq\\000~\\000\\254\\000\\000\\000\\036uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\005"" + sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\005 + ""t\\000\\020java.lang.Stringppppppq\\000~\\001\\003pppsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\000\\000\\002\\000\\000\\000\\000\\034\\000\\000\\000\\001\\000\\000\\000\\003pq\\000~\\000\\016q\\000~\\002\\327pt\\000\\015staticText-25ppppppppppppppppppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#q\\000~\\003!psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#psq\\000~\\000Fpppq\\000~\\003#q\\000~\\003#psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003#q\\000~\\003#ppppppppppppppt\\000\\005Date:xp\\000\\000w&\\000\\000\\000\\031\\001pq\\000~\\000\\032sq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\002w\\004\\000\\000\\000\\012sq\\000~\\0007\\000\\000\\000\\017\\000\\001\\000\\000\\002\\000\\000\\000\\000\\202\\000\\000\\002\\214\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\0032pt\\000\\015staticText-28ppppppppppppppq\\000~\\000\\353q\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036q\\000~\\0034psq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036psq\\000~\\000Fpppq\\000~\\0036q\\000~\\0036psq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036psq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\0036q\\000~\\0036ppppt\\000\\016Helvetica-Boldpppppppppt\\000\\025Stampato con CMDBuildsq\\000~\\0007\\000\\000\\000\\022\\000\\001\\001\\000\\002\\000\\000\\000\\001\\217\\000\\000\\000\\300\\000\\000\\000\\001pq\\000~\\000\\016q\\000~\\0032pt\\000\\015staticText-29pppppppppppppsq\\000~\\000=\\000\\000\\000\\014sq\\000~\\000\\030\\002q\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jq\\000~\\003Fpsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jpsq\\000~\\000Fpppq\\000~\\003Jq\\000~\\003Jpsq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jpsq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003Jq\\000~\\003Jppppt\\000\\016Helvetica-Boldpppppppppt\\000\\031Location list with assetsxp\\000\\000w&\\000\\000\\000$\\001sq\\000~\\000\\254\\000\\000\\000\\027uq\\000~\\000\\257\\000\\000\\000\\003sq\\000~\\000\\261\\001t\\000\\016new Boolean ( sq\\000~\\000\\261\\004t\\000\\013PAGE_NUMBERsq\\000~\\000\\261\\001t\\000\\021.intValue() > 1 )q\\000~\\002\\216pq\\000~\\000\\032psq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\000w\\004\\000\\000\\000\\012xp\\000\\000w&\\000\\000\\000\\005\\001pq\\000~\\000\\032psq\\000~\\000\\017sq\\000~\\000\\026\\000\\000\\000\\003w\\004\\000\\000\\000\\012sq\\000~\\0007\\000\\000\\000\\032\\000\\001\\000\\000\\002\\000\\000\\000\\001\\217\\000\\000\\000\\300\\000\\000\\000\\022pq\\000~\\000\\016q\\000~\\003dpt\\000\\014staticText-1pppppppppppppsq\\000~\\000=\\000\\000\\000\\020q\\000~\\003Iq\\000~\\000@ppppppppsq\\000~\\000Apsq\\000~\\000Esq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003iq\\000~\\003fpsq\\000~\\000Jsq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003ipsq\\000~\\000Fpppq\\000~\\003iq\\000~\\003ipsq\\000~\\000Osq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003ipsq\\000~\\000Ssq\\000~\\000*\\000\\000\\000\\000\\377\\000\\000\\000pppq\\000~\\0004sq\\000~\\0005\\000\\000\\000\\000q\\000~\\003iq\\000~\\003ippppt\\000\\016Helvetica-Boldpppppppppt\\000\\031Location list with assetssr\\000,net.sf.jasperreports.engine.base.JRBaseImage\\000\\000\\000\\000\\000\\000''\\330\\002\\000$I\\000\\015bookmarkLevelB\\000\\016evaluationTimeB\\000\\017hyperlinkTargetB\\000\\015hyperlinkTypeZ\\000\\006isLazyB\\000\\013onErrorTypeL\\000\\024anchorNameExpressionq\\000~\\000\\020L\\000\\006borderq\\000~\\000\\021L\\000\\013borderColorq\\000~\\000$L\\000\\014bottomBorderq\\000~\\000\\021L\\000\\021bottomBorderColorq\\000~\\000$L\\000\\015bottomPaddingq\\000~\\000 L\\000\\017evaluationGroupq\\000~\\000&L\\000\\012expressionq\\000~\\000\\020L\\000\\023horizontalAlignmentq\\000~\\000\\021L\\000\\031hyperlinkAnchorExpressionq\\000~\\000\\020L\\000\\027hyperlinkPageExpressionq\\000~\\000\\020[\\000\\023hyperlinkParametersq\\000~\\000\\232L\\000\\034hyperlinkReferenceExpressionq\\000~\\000\\020L\\000\\032hyperlinkTooltipExpressionq\\000~\\000\\020L\\000\\014isUsingCacheq\\000~\\0009L\\000\\012leftBorderq\\000~\\000\\021L\\000\\017leftBorderColorq\\000~\\000$L\\000\\013leftPaddingq\\000~\\000 L\\000\\007lineBoxq\\000~\\000:L\\000\\012linkTargetq\\000~\\000\\002L\\000\\010linkTypeq\\000~\\000\\002L\\000\\007paddingq\\000~\\000 L\\000\\013rightBorderq\\000~\\000\\021L\\000\\020rightBorderColorq\\000~\\000$L\\000\\014rightPaddingq\\000~\\000 L\\000\\012scaleImageq\\000~\\000\\021L\\000\\011topBorderq\\000~\\000\\021L\\000\\016topBorderColorq\\000~\\000$L\\000\\012topPaddingq\\000~\\000 L\\000\\021verticalAlignmentq\\000~\\000\\021xq\\000~\\000!\\000\\000\\000%\\000\\001\\000\\000\\002\\000\\000\\000\\000q\\000\\000\\000\\001\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\003dppppppppppsq\\000~\\0000pppq\\000~\\003zp\\000\\000\\000\\000\\001\\001\\000\\000\\002pppppppsq\\000~\\000\\254\\000\\000\\000\\025uq\\000~\\000\\257\\000\\000\\000\\002sq\\000~\\000\\261\\002t\\000\\025REPORT_PARAMETERS_MAPsq\\000~\\000\\261\\001t\\000\\016.get("IMAGE0")t\\000\\023java.io.InputStreampppppppq\\000~\\000@pppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\003\\203q\\000~\\003\\203q\\000~\\003zpsq\\000~\\000Jpppq\\000~\\003\\203q\\000~\\003\\203psq\\000~\\000Fpppq\\000~\\003\\203q\\000~\\003\\203psq\\000~\\000Opppq\\000~\\003\\203q\\000~\\003\\203psq\\000~\\000Spppq\\000~\\003\\203q\\000~\\003\\203pppppppppppsq\\000~\\003y\\000\\000\\000%\\000\\001\\000\\000\\002\\000\\000\\000\\000q\\000\\000\\002\\235\\000\\000\\000\\000pq\\000~\\000\\016q\\000~\\003dppppppppppsq\\000~\\0000pppq\\000~\\003\\211p\\000\\000\\000\\000\\001\\001\\000\\000\\002pppppppsq\\000~\\000\\254\\000\\000\\000\\026uq\\000~\\000\\257\\000\\000\\000\\002sq\\000~\\000\\261\\002t\\000\\025REPORT_PARAMETERS_MAPsq\\000~\\000\\261\\001t\\000\\016.get("IMAGE1")q\\000~\\003\\202pppppppq\\000~\\000@pppsq\\000~\\000Apsq\\000~\\000Epppq\\000~\\003\\221q\\000~\\003\\221q\\000~\\003\\211psq\\000~\\000Jpppq\\000~\\003\\221q\\000~\\003\\221psq\\000~\\000Fpppq\\000~\\003\\221q\\000~\\003\\221psq\\000~\\000Opppq\\000~\\003\\221q\\000~\\003\\221psq\\000~\\000Spppq\\000~\\003\\221q\\000~\\003\\221pppppppppppxp\\000\\000w&\\000\\000\\000<\\001pq\\000~\\000\\032sr\\0006net.sf.jasperreports.engine.design.JRReportCompileData\\000\\000\\000\\000\\000\\000''\\330\\002\\000\\003L\\000\\023crosstabCompileDataq\\000~\\001TL\\000\\022datasetCompileDataq\\000~\\001TL\\000\\026mainDatasetCompileDataq\\000~\\000\\001xpsq\\000~\\002\\232?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\000xsq\\000~\\002\\232?@\\000\\000\\000\\000\\000\\014w\\010\\000\\000\\000\\020\\000\\000\\000\\000xur\\000\\002[B\\254\\363\\027\\370\\006\\010T\\340\\002\\000\\000xp\\000\\000\\037+\\312\\376\\272\\276\\000\\000\\000.\\001\\030\\001\\000\\036AssetList_1314116315778_112849\\007\\000\\001\\001\\000,net/sf/jasperreports/engine/fill/JREvaluator\\007\\000\\003\\001\\000\\027parameter_REPORT_LOCALE\\001\\0002Lnet/sf/jasperreports/engine/fill/JRFillParameter;\\001\\000\\027parameter_JASPER_REPORT\\001\\000\\034parameter_REPORT_VIRTUALIZER\\001\\000\\032parameter_REPORT_TIME_ZONE\\001\\000\\036parameter_REPORT_FILE_RESOLVER\\001\\000\\032parameter_REPORT_SCRIPTLET\\001\\000\\037parameter_REPORT_PARAMETERS_MAP\\001\\000\\033parameter_REPORT_CONNECTION\\001\\000\\035parameter_REPORT_CLASS_LOADER\\001\\000\\034parameter_REPORT_DATA_SOURCE\\001\\000$parameter_REPORT_URL_HANDLER_FACTORY\\001\\000\\036parameter_IS_IGNORE_PAGINATION\\001\\000\\037parameter_REPORT_FORMAT_FACTORY\\001\\000\\032parameter_REPORT_MAX_COUNT\\001\\000\\032parameter_REPORT_TEMPLATES\\001\\000 parameter_REPORT_RESOURCE_BUNDLE\\001\\000\\017field_AssetCode\\001\\000.Lnet/sf/jasperreports/engine/fill/JRFillField;\\001\\000\\025field_RoomDescription\\001\\000\\016field_RoomCode\\001\\000\\023field_WorkplaceCode\\001\\000\\013field_Email\\001\\000\\026field_AssetDescription\\001\\000\\020field_AssetBrand\\001\\000\\026field_FloorDescription\\001\\000\\032field_WorkplaceDescription\\001\\000\\031field_BuildingDescription\\001\\000\\016field_Assignee\\001\\000\\024variable_PAGE_NUMBER\\001\\0001Lnet/sf/jasperreports/engine/fill/JRFillVariable;\\001\\000\\026variable_COLUMN_NUMBER\\001\\000\\025variable_REPORT_COUNT\\001\\000\\023variable_PAGE_COUNT\\001\\000\\025variable_COLUMN_COUNT\\001\\000\\026variable_palazzo_COUNT\\001\\000\\025variable_tavola_COUNT\\001\\000\\025variable_stanza_COUNT\\001\\000\\006<init>\\001\\000\\003()V\\001\\000\\004Code\\014\\000+\\000,\\012\\000\\004\\000.\\014\\000\\005\\000\\006\\011\\000\\002\\0000\\014\\000\\007\\000\\006\\011\\000\\002\\0002\\014\\000\\010\\000\\006\\011\\000\\002\\0004\\014\\000\\011\\000\\006\\011\\000\\002\\0006\\014\\000\\012\\000\\006\\011\\000\\002\\0008\\014\\000\\013\\000\\006\\011\\000\\002\\000:\\014\\000\\014\\000\\006\\011\\000\\002\\000<\\014\\000\\015\\000\\006\\011\\000\\002\\000>\\014\\000\\016\\000\\006\\011\\000\\002\\000@\\014\\000\\017\\000\\006\\011\\000\\002\\000B\\014\\000\\020\\000\\006\\011\\000\\002\\000D\\014\\000\\021\\000\\006\\011\\000\\002\\000F\\014\\000\\022\\000\\006\\011\\000\\002\\000H\\014\\000\\023\\000\\006\\011\\000\\002\\000J\\014\\000\\024\\000\\006\\011\\000\\002\\000L\\014\\000\\025\\000\\006\\011\\000\\002\\000N\\014\\000\\026\\000\\027\\011\\000\\002\\000P\\014\\000\\030\\000\\027\\011\\000\\002\\000R\\014\\000\\031\\000\\027\\011\\000\\002\\000T\\014\\000\\032\\000\\027\\011\\000\\002\\000V\\014\\000\\033\\000\\027\\011\\000\\002\\000X\\014\\000\\034\\000\\027\\011\\000\\002\\000Z\\014\\000\\035\\000\\027\\011\\000\\002\\000\\\\\\014\\000\\036\\000\\027\\011\\000\\002\\000^\\014\\000\\037\\000\\027\\011\\000\\002\\000`\\014\\000 \\000\\027\\011\\000\\002\\000b\\014\\000!\\000\\027\\011\\000\\002\\000d\\014\\000"\\000#\\011\\000\\002\\000f\\014\\000$\\000#\\011\\000\\002\\000h\\014\\000%\\000#\\011\\000\\002\\000j\\014\\000&\\000#\\011\\000\\002\\000l\\014\\000''\\000#\\011\\000\\002\\000n\\014\\000(\\000#\\011\\000\\002\\000p\\014\\000)\\000#\\011\\000\\002\\000r\\014\\000*\\000#\\011\\000\\002\\000t\\001\\000\\017LineNumberTable\\001\\000\\016customizedInit\\001\\0000(Ljava/util/Map;Ljava/util/Map;Ljava/util/Map;)V\\001\\000\\012initParams\\001\\000\\022(Ljava/util/Map;)V\\014\\000y\\000z\\012\\000\\002\\000{\\001\\000\\012initFields\\014\\000}\\000z\\012\\000\\002\\000~\\001\\000\\010initVars\\014\\000\\200\\000z\\012\\000\\002\\000\\201\\001\\000\\015REPORT_LOCALE\\010\\000\\203\\001\\000\\015java/util/Map\\007\\000\\205\\001\\000\\003get\\001\\000&(Ljava/lang/Object;)Ljava/lang/Object;\\014\\000\\207\\000\\210\\013\\000\\206\\000\\211\\001\\0000net/sf/jasperreports/engine/fill/JRFillParameter\\007\\000\\213\\001\\000\\015JASPER_REPORT\\010\\000\\215\\001\\000\\022REPORT_VIRTUALIZER\\010\\000\\217\\001\\000\\020REPORT_TIME_ZONE\\010\\000\\221\\001\\000\\024REPORT_FILE_RESOLVER\\010\\000\\223\\001\\000\\020REPORT_SCRIPTLET\\010\\000\\225\\001\\000\\025REPORT_PARAMETERS_MAP\\010\\000\\227\\001\\000\\021REPORT_CONNECTION\\010\\000\\231\\001\\000\\023REPORT_CLASS_LOADER\\010\\000\\233\\001\\000\\022REPORT_DATA_SOURCE\\010\\000\\235\\001\\000\\032REPORT_URL_HANDLER_FACTORY\\010\\000\\237\\001\\000\\024IS_IGNORE_PAGINATION\\010\\000\\241\\001\\000\\025REPORT_FORMAT_FACTORY\\010\\000\\243\\001\\000\\020REPORT_MAX_COUNT\\010\\000\\245\\001\\000\\020REPORT_TEMPLATES\\010\\000\\247\\001\\000\\026REPORT_RESOURCE_BUNDLE\\010\\000\\251\\001\\000\\011AssetCode\\010\\000\\253\\001\\000,net/sf/jasperreports/engine/fill/JRFillField\\007\\000\\255\\001\\000\\017RoomDescription\\010\\000\\257\\001\\000\\010RoomCode\\010\\000\\261\\001\\000\\015WorkplaceCode\\010\\000\\263\\001\\000\\005Email\\010\\000\\265\\001\\000\\020AssetDescription\\010\\000\\267\\001\\000\\012AssetBrand\\010\\000\\271\\001\\000\\020FloorDescription\\010\\000\\273\\001\\000\\024WorkplaceDescription\\010\\000\\275\\001\\000\\023BuildingDescription\\010\\000\\277\\001\\000\\010Assignee\\010\\000\\301\\001\\000\\013PAGE_NUMBER\\010\\000\\303\\001\\000/net/sf/jasperreports/engine/fill/JRFillVariable\\007\\000\\305\\001\\000\\015COLUMN_NUMBER\\010\\000\\307\\001\\000\\014REPORT_COUNT\\010\\000\\311\\001\\000\\012PAGE_COUNT\\010\\000\\313\\001\\000\\014COLUMN_COUNT\\010\\000\\315\\001\\000\\015palazzo_COUNT\\010\\000\\317\\001\\000\\014tavola_COUNT\\010\\000\\321\\001\\000\\014stanza_COUNT\\010\\000\\323\\001\\000\\010evaluate\\001\\000\\025(I)Ljava/lang/Object;\\001\\000\\012Exceptions\\001\\000\\023java/lang/Throwable\\007\\000\\330\\001\\000\\021java/lang/Integer\\007\\000\\332\\001\\000\\004(I)V\\014\\000+\\000\\334\\012\\000\\333\\000\\335\\001\\000\\010getValue\\001\\000\\024()Ljava/lang/Object;\\014\\000\\337\\000\\340\\012\\000\\256\\000\\341\\001\\000\\020java/lang/String\\007\\000\\343\\012\\000\\214\\000\\341\\001\\000\\006IMAGE0\\010\\000\\346\\001\\000\\023java/io/InputStream\\007\\000\\350\\001\\000\\006IMAGE1\\010\\000\\352\\001\\000\\021java/lang/Boolean\\007\\000\\354\\012\\000\\306\\000\\341\\001\\000\\010intValue\\001\\000\\003()I\\014\\000\\357\\000\\360\\012\\000\\333\\000\\361\\001\\000\\004(Z)V\\014\\000+\\000\\363\\012\\000\\355\\000\\364\\001\\000\\016java/util/Date\\007\\000\\366\\012\\000\\367\\000.\\001\\000\\026java/lang/StringBuffer\\007\\000\\371\\001\\000\\005Page \\010\\000\\373\\001\\000\\025(Ljava/lang/String;)V\\014\\000+\\000\\375\\012\\000\\372\\000\\376\\001\\000\\006append\\001\\000,(Ljava/lang/Object;)Ljava/lang/StringBuffer;\\014\\001\\000\\001\\001\\012\\000\\372\\001\\002\\001\\000\\004 di \\010\\001\\004\\001\\000,(Ljava/lang/String;)Ljava/lang/StringBuffer;\\014\\001\\000\\001\\006\\012\\000\\372\\001\\007\\001\\000\\010toString\\001\\000\\024()Ljava/lang/String;\\014\\001\\011\\001\\012\\012\\000\\372\\001\\013\\012\\000\\372\\000.\\001\\000\\013evaluateOld\\001\\000\\013getOldValue\\014\\001\\017\\000\\340\\012\\000\\256\\001\\020\\012\\000\\306\\001\\020\\001\\000\\021evaluateEstimated\\001\\000\\021getEstimatedValue\\014\\001\\024\\000\\340\\012\\000\\306\\001\\025\\001\\000\\012SourceFile\\000!\\000\\002\\000\\004\\000\\000\\000#\\000\\002\\000\\005\\000\\006\\000\\000\\000\\002\\000\\007\\000\\006\\000\\000\\000\\002\\000\\010\\000\\006\\000\\000\\000\\002\\000\\011\\000\\006\\000\\000\\000\\002\\000\\012\\000\\006\\000\\000\\000\\002\\000\\013\\000\\006\\000\\000\\000\\002\\000\\014\\000\\006\\000\\000\\000\\002\\000\\015\\000\\006\\000\\000\\000\\002\\000\\016\\000\\006\\000\\000\\000\\002\\000\\017\\000\\006\\000\\000\\000\\002\\000\\020\\000\\006\\000\\000\\000\\002\\000\\021\\000\\006\\000\\000\\000\\002\\000\\022\\000\\006\\000\\000\\000\\002\\000\\023\\000\\006\\000\\000\\000\\002\\000\\024\\000\\006\\000\\000\\000\\002\\000\\025\\000\\006\\000\\000\\000\\002\\000\\026\\000\\027\\000\\000\\000\\002\\000\\030\\000\\027\\000\\000\\000\\002\\000\\031\\000\\027\\000\\000\\000\\002\\000\\032\\000\\027\\000\\000\\000\\002\\000\\033\\000\\027\\000\\000\\000\\002\\000\\034\\000\\027\\000\\000\\000\\002\\000\\035\\000\\027\\000\\000\\000\\002\\000\\036\\000\\027\\000\\000\\000\\002\\000\\037\\000\\027\\000\\000\\000\\002\\000 \\000\\027\\000\\000\\000\\002\\000!\\000\\027\\000\\000\\000\\002\\000"\\000#\\000\\000\\000\\002\\000$\\000#\\000\\000\\000\\002\\000%\\000#\\000\\000\\000\\002\\000&\\000#\\000\\000\\000\\002\\000''\\000#\\000\\000\\000\\002\\000(\\000#\\000\\000\\000\\002\\000)\\000#\\000\\000\\000\\002\\000*\\000#\\000\\000\\000\\010\\000\\001\\000+\\000,\\000\\001\\000-\\000\\000\\001\\\\\\000\\002\\000\\001\\000\\000\\000\\264*\\267\\000/*\\001\\265\\0001*\\001\\265\\0003*\\001\\265\\0005*\\001\\265\\0007*\\001\\265\\0009*\\001\\265\\000;*\\001\\265\\000=*\\001\\265\\000?*\\001\\265\\000A*\\001\\265\\000C*\\001\\265\\000E*\\001\\265\\000G*\\001\\265\\000I*\\001\\265\\000K*\\001\\265\\000M*\\001\\265\\000O*\\001\\265\\000Q*\\001\\265\\000S*\\001\\265\\000U*\\001\\265\\000W*\\001\\265\\000Y*\\001\\265\\000[*\\001\\265\\000]*\\001\\265\\000_*\\001\\265\\000a*\\001\\265\\000c*\\001\\265\\000e*\\001\\265\\000g*\\001\\265\\000i*\\001\\265\\000k*\\001\\265\\000m*\\001\\265\\000o*\\001\\265\\000q*\\001\\265\\000s*\\001\\265\\000u\\261\\000\\000\\000\\001\\000v\\000\\000\\000\\226\\000%\\000\\000\\000\\025\\000\\004\\000\\034\\000\\011\\000\\035\\000\\016\\000\\036\\000\\023\\000\\037\\000\\030\\000 \\000\\035\\000!\\000"\\000"\\000''\\000#\\000,\\000$\\0001\\000%\\0006\\000&\\000;\\000''\\000@\\000(\\000E\\000)\\000J\\000*\\000O\\000+\\000T\\000,\\000Y\\000-\\000^\\000.\\000c\\000/\\000h\\0000\\000m\\0001\\000r\\0002\\000w\\0003\\000|\\0004\\000\\201\\0005\\000\\206\\0006\\000\\213\\0007\\000\\220\\0008\\000\\225\\0009\\000\\232\\000:\\000\\237\\000;\\000\\244\\000<\\000\\251\\000=\\000\\256\\000>\\000\\263\\000\\025\\000\\001\\000w\\000x\\000\\001\\000-\\000\\000\\0004\\000\\002\\000\\004\\000\\000\\000\\020*+\\267\\000|*,\\267\\000\\177*-\\267\\000\\202\\261\\000\\000\\000\\001\\000v\\000\\000\\000\\022\\000\\004\\000\\000\\000J\\000\\005\\000K\\000\\012\\000L\\000\\017\\000M\\000\\002\\000y\\000z\\000\\001\\000-\\000\\000\\001I\\000\\003\\000\\002\\000\\000\\000\\361*+\\022\\204\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0001*+\\022\\216\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0003*+\\022\\220\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0005*+\\022\\222\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0007*+\\022\\224\\271\\000\\212\\002\\000\\300\\000\\214\\265\\0009*+\\022\\226\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000;*+\\022\\230\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000=*+\\022\\232\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000?*+\\022\\234\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000A*+\\022\\236\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000C*+\\022\\240\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000E*+\\022\\242\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000G*+\\022\\244\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000I*+\\022\\246\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000K*+\\022\\250\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000M*+\\022\\252\\271\\000\\212\\002\\000\\300\\000\\214\\265\\000O\\261\\000\\000\\000\\001\\000v\\000\\000\\000F\\000\\021\\000\\000\\000U\\000\\017\\000V\\000\\036\\000W\\000-\\000X\\000<\\000Y\\000K\\000Z\\000Z\\000[\\000i\\000\\\\\\000x\\000]\\000\\207\\000^\\000\\226\\000_\\000\\245\\000`\\000\\264\\000a\\000\\303\\000b\\000\\322\\000c\\000\\341\\000d\\000\\360\\000e\\000\\002\\000}\\000z\\000\\001\\000-\\000\\000\\000\\352\\000\\003\\000\\002\\000\\000\\000\\246*+\\022\\254\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000Q*+\\022\\260\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000S*+\\022\\262\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000U*+\\022\\264\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000W*+\\022\\266\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000Y*+\\022\\270\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000[*+\\022\\272\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000]*+\\022\\274\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000_*+\\022\\276\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000a*+\\022\\300\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000c*+\\022\\302\\271\\000\\212\\002\\000\\300\\000\\256\\265\\000e\\261\\000\\000\\000\\001\\000v\\000\\000\\0002\\000\\014\\000\\000\\000m\\000\\017\\000n\\000\\036\\000o\\000-\\000p\\000<\\000q\\000K\\000r\\000Z\\000s\\000i\\000t\\000x\\000u\\000\\207\\000v\\000\\226\\000w\\000\\245\\000x\\000\\002\\000\\200\\000z\\000\\001\\000-\\000\\000\\000\\261\\000\\003\\000\\002\\000\\000\\000y*+\\022\\304\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000g*+\\022\\310\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000i*+\\022\\312\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000k*+\\022\\314\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000m*+\\022\\316\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000o*+\\022\\320\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000q*+\\022\\322\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000s*+\\022\\324\\271\\000\\212\\002\\000\\300\\000\\306\\265\\000u\\261\\000\\000\\000\\001\\000v\\000\\000\\000&\\000\\011\\000\\000\\000\\200\\000\\017\\000\\201\\000\\036\\000\\202\\000-\\000\\203\\000<\\000\\204\\000K\\000\\205\\000Z\\000\\206\\000i\\000\\207\\000x\\000\\210\\000\\001\\000\\325\\000\\326\\000\\002\\000\\327\\000\\000\\000\\004\\000\\001\\000\\331\\000-\\000\\000\\003\\350\\000\\004\\000\\003\\000\\000\\002\\274\\001M\\033\\252\\000\\000\\002\\267\\000\\000\\000\\000\\000\\000\\000!\\000\\000\\000\\225\\000\\000\\000\\241\\000\\000\\000\\255\\000\\000\\000\\271\\000\\000\\000\\305\\000\\000\\000\\321\\000\\000\\000\\335\\000\\000\\000\\351\\000\\000\\000\\365\\000\\000\\001\\001\\000\\000\\001\\015\\000\\000\\001\\031\\000\\000\\001%\\000\\000\\0011\\000\\000\\001=\\000\\000\\001K\\000\\000\\001Y\\000\\000\\001g\\000\\000\\001u\\000\\000\\001\\203\\000\\000\\001\\221\\000\\000\\001\\237\\000\\000\\001\\267\\000\\000\\001\\317\\000\\000\\001\\360\\000\\000\\001\\376\\000\\000\\002\\014\\000\\000\\002\\032\\000\\000\\002(\\000\\000\\0023\\000\\000\\002V\\000\\000\\002q\\000\\000\\002\\224\\000\\000\\002\\257\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\031\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\015\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\001\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\365\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\351\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\335\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\321\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\305\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\271\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\255\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\241\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\225\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\211\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001}*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001o*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001a*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001S*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001E*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\0017*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\001)*\\264\\000S\\266\\000\\342\\300\\000\\344M\\247\\001\\033*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\347\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\001\\003*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\353\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\000\\353\\273\\000\\355Y*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\000\\362\\004\\244\\000\\007\\004\\247\\000\\004\\003\\267\\000\\365M\\247\\000\\312*\\264\\000Y\\266\\000\\342\\300\\000\\344M\\247\\000\\274*\\264\\000]\\266\\000\\342\\300\\000\\344M\\247\\000\\256*\\264\\000e\\266\\000\\342\\300\\000\\344M\\247\\000\\240*\\264\\000[\\266\\000\\342\\300\\000\\344M\\247\\000\\222\\273\\000\\367Y\\267\\000\\370M\\247\\000\\207\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000d\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000I\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000&\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\000\\356\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000\\013\\273\\000\\367Y\\267\\000\\370M,\\260\\000\\000\\000\\001\\000v\\000\\000\\001\\032\\000F\\000\\000\\000\\220\\000\\002\\000\\222\\000\\230\\000\\226\\000\\241\\000\\227\\000\\244\\000\\233\\000\\255\\000\\234\\000\\260\\000\\240\\000\\271\\000\\241\\000\\274\\000\\245\\000\\305\\000\\246\\000\\310\\000\\252\\000\\321\\000\\253\\000\\324\\000\\257\\000\\335\\000\\260\\000\\340\\000\\264\\000\\351\\000\\265\\000\\354\\000\\271\\000\\365\\000\\272\\000\\370\\000\\276\\001\\001\\000\\277\\001\\004\\000\\303\\001\\015\\000\\304\\001\\020\\000\\310\\001\\031\\000\\311\\001\\034\\000\\315\\001%\\000\\316\\001(\\000\\322\\0011\\000\\323\\0014\\000\\327\\001=\\000\\330\\001@\\000\\334\\001K\\000\\335\\001N\\000\\341\\001Y\\000\\342\\001\\\\\\000\\346\\001g\\000\\347\\001j\\000\\353\\001u\\000\\354\\001x\\000\\360\\001\\203\\000\\361\\001\\206\\000\\365\\001\\221\\000\\366\\001\\224\\000\\372\\001\\237\\000\\373\\001\\242\\000\\377\\001\\267\\001\\000\\001\\272\\001\\004\\001\\317\\001\\005\\001\\322\\001\\011\\001\\360\\001\\012\\001\\363\\001\\016\\001\\376\\001\\017\\002\\001\\001\\023\\002\\014\\001\\024\\002\\017\\001\\030\\002\\032\\001\\031\\002\\035\\001\\035\\002(\\001\\036\\002+\\001"\\0023\\001#\\0026\\001''\\002V\\001(\\002Y\\001,\\002q\\001-\\002t\\0011\\002\\224\\0012\\002\\227\\0016\\002\\257\\0017\\002\\262\\001;\\002\\272\\001C\\000\\001\\001\\016\\000\\326\\000\\002\\000\\327\\000\\000\\000\\004\\000\\001\\000\\331\\000-\\000\\000\\003\\350\\000\\004\\000\\003\\000\\000\\002\\274\\001M\\033\\252\\000\\000\\002\\267\\000\\000\\000\\000\\000\\000\\000!\\000\\000\\000\\225\\000\\000\\000\\241\\000\\000\\000\\255\\000\\000\\000\\271\\000\\000\\000\\305\\000\\000\\000\\321\\000\\000\\000\\335\\000\\000\\000\\351\\000\\000\\000\\365\\000\\000\\001\\001\\000\\000\\001\\015\\000\\000\\001\\031\\000\\000\\001%\\000\\000\\0011\\000\\000\\001=\\000\\000\\001K\\000\\000\\001Y\\000\\000\\001g\\000\\000\\001u\\000\\000\\001\\203\\000\\000\\001\\221\\000\\000\\001\\237\\000\\000\\001\\267\\000\\000\\001\\317\\000\\000\\001\\360\\000\\000\\001\\376\\000\\000\\002\\014\\000\\000\\002\\032\\000\\000\\002(\\000\\000\\0023\\000\\000\\002V\\000\\000\\002q\\000\\000\\002\\224\\000\\000\\002\\257\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\031\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\015\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\001\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\365\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\351\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\335\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\321\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\305\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\271\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\255\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\241\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\225\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\211\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001}*\\264\\000c\\266\\001\\021\\300\\000\\344M\\247\\001o*\\264\\000c\\266\\001\\021\\300\\000\\344M\\247\\001a*\\264\\000_\\266\\001\\021\\300\\000\\344M\\247\\001S*\\264\\000_\\266\\001\\021\\300\\000\\344M\\247\\001E*\\264\\000U\\266\\001\\021\\300\\000\\344M\\247\\0017*\\264\\000U\\266\\001\\021\\300\\000\\344M\\247\\001)*\\264\\000S\\266\\001\\021\\300\\000\\344M\\247\\001\\033*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\347\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\001\\003*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\353\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\000\\353\\273\\000\\355Y*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\000\\362\\004\\244\\000\\007\\004\\247\\000\\004\\003\\267\\000\\365M\\247\\000\\312*\\264\\000Y\\266\\001\\021\\300\\000\\344M\\247\\000\\274*\\264\\000]\\266\\001\\021\\300\\000\\344M\\247\\000\\256*\\264\\000e\\266\\001\\021\\300\\000\\344M\\247\\000\\240*\\264\\000[\\266\\001\\021\\300\\000\\344M\\247\\000\\222\\273\\000\\367Y\\267\\000\\370M\\247\\000\\207\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000d\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000I\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000&\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\022\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000\\013\\273\\000\\367Y\\267\\000\\370M,\\260\\000\\000\\000\\001\\000v\\000\\000\\001\\032\\000F\\000\\000\\001L\\000\\002\\001N\\000\\230\\001R\\000\\241\\001S\\000\\244\\001W\\000\\255\\001X\\000\\260\\001\\\\\\000\\271\\001]\\000\\274\\001a\\000\\305\\001b\\000\\310\\001f\\000\\321\\001g\\000\\324\\001k\\000\\335\\001l\\000\\340\\001p\\000\\351\\001q\\000\\354\\001u\\000\\365\\001v\\000\\370\\001z\\001\\001\\001{\\001\\004\\001\\177\\001\\015\\001\\200\\001\\020\\001\\204\\001\\031\\001\\205\\001\\034\\001\\211\\001%\\001\\212\\001(\\001\\216\\0011\\001\\217\\0014\\001\\223\\001=\\001\\224\\001@\\001\\230\\001K\\001\\231\\001N\\001\\235\\001Y\\001\\236\\001\\\\\\001\\242\\001g\\001\\243\\001j\\001\\247\\001u\\001\\250\\001x\\001\\254\\001\\203\\001\\255\\001\\206\\001\\261\\001\\221\\001\\262\\001\\224\\001\\266\\001\\237\\001\\267\\001\\242\\001\\273\\001\\267\\001\\274\\001\\272\\001\\300\\001\\317\\001\\301\\001\\322\\001\\305\\001\\360\\001\\306\\001\\363\\001\\312\\001\\376\\001\\313\\002\\001\\001\\317\\002\\014\\001\\320\\002\\017\\001\\324\\002\\032\\001\\325\\002\\035\\001\\331\\002(\\001\\332\\002+\\001\\336\\0023\\001\\337\\0026\\001\\343\\002V\\001\\344\\002Y\\001\\350\\002q\\001\\351\\002t\\001\\355\\002\\224\\001\\356\\002\\227\\001\\362\\002\\257\\001\\363\\002\\262\\001\\367\\002\\272\\001\\377\\000\\001\\001\\023\\000\\326\\000\\002\\000\\327\\000\\000\\000\\004\\000\\001\\000\\331\\000-\\000\\000\\003\\350\\000\\004\\000\\003\\000\\000\\002\\274\\001M\\033\\252\\000\\000\\002\\267\\000\\000\\000\\000\\000\\000\\000!\\000\\000\\000\\225\\000\\000\\000\\241\\000\\000\\000\\255\\000\\000\\000\\271\\000\\000\\000\\305\\000\\000\\000\\321\\000\\000\\000\\335\\000\\000\\000\\351\\000\\000\\000\\365\\000\\000\\001\\001\\000\\000\\001\\015\\000\\000\\001\\031\\000\\000\\001%\\000\\000\\0011\\000\\000\\001=\\000\\000\\001K\\000\\000\\001Y\\000\\000\\001g\\000\\000\\001u\\000\\000\\001\\203\\000\\000\\001\\221\\000\\000\\001\\237\\000\\000\\001\\267\\000\\000\\001\\317\\000\\000\\001\\360\\000\\000\\001\\376\\000\\000\\002\\014\\000\\000\\002\\032\\000\\000\\002(\\000\\000\\0023\\000\\000\\002V\\000\\000\\002q\\000\\000\\002\\224\\000\\000\\002\\257\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\031\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\015\\273\\000\\333Y\\004\\267\\000\\336M\\247\\002\\001\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\365\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\351\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\335\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\321\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\305\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\271\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\255\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\241\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001\\225\\273\\000\\333Y\\004\\267\\000\\336M\\247\\001\\211\\273\\000\\333Y\\003\\267\\000\\336M\\247\\001}*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001o*\\264\\000c\\266\\000\\342\\300\\000\\344M\\247\\001a*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001S*\\264\\000_\\266\\000\\342\\300\\000\\344M\\247\\001E*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\0017*\\264\\000U\\266\\000\\342\\300\\000\\344M\\247\\001)*\\264\\000S\\266\\000\\342\\300\\000\\344M\\247\\001\\033*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\347\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\001\\003*\\264\\000=\\266\\000\\345\\300\\000\\206\\022\\353\\271\\000\\212\\002\\000\\300\\000\\351M\\247\\000\\353\\273\\000\\355Y*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\000\\362\\004\\244\\000\\007\\004\\247\\000\\004\\003\\267\\000\\365M\\247\\000\\312*\\264\\000Y\\266\\000\\342\\300\\000\\344M\\247\\000\\274*\\264\\000]\\266\\000\\342\\300\\000\\344M\\247\\000\\256*\\264\\000e\\266\\000\\342\\300\\000\\344M\\247\\000\\240*\\264\\000[\\266\\000\\342\\300\\000\\344M\\247\\000\\222\\273\\000\\367Y\\267\\000\\370M\\247\\000\\207\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000d\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000I\\273\\000\\372Y\\022\\374\\267\\000\\377*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\023\\001\\005\\266\\001\\010\\266\\001\\014M\\247\\000&\\273\\000\\372Y\\267\\001\\015*\\264\\000g\\266\\001\\026\\300\\000\\333\\266\\001\\003\\266\\001\\014M\\247\\000\\013\\273\\000\\367Y\\267\\000\\370M,\\260\\000\\000\\000\\001\\000v\\000\\000\\001\\032\\000F\\000\\000\\002\\010\\000\\002\\002\\012\\000\\230\\002\\016\\000\\241\\002\\017\\000\\244\\002\\023\\000\\255\\002\\024\\000\\260\\002\\030\\000\\271\\002\\031\\000\\274\\002\\035\\000\\305\\002\\036\\000\\310\\002"\\000\\321\\002#\\000\\324\\002''\\000\\335\\002(\\000\\340\\002,\\000\\351\\002-\\000\\354\\0021\\000\\365\\0022\\000\\370\\0026\\001\\001\\0027\\001\\004\\002;\\001\\015\\002<\\001\\020\\002@\\001\\031\\002A\\001\\034\\002E\\001%\\002F\\001(\\002J\\0011\\002K\\0014\\002O\\001=\\002P\\001@\\002T\\001K\\002U\\001N\\002Y\\001Y\\002Z\\001\\\\\\002^\\001g\\002_\\001j\\002c\\001u\\002d\\001x\\002h\\001\\203\\002i\\001\\206\\002m\\001\\221\\002n\\001\\224\\002r\\001\\237\\002s\\001\\242\\002w\\001\\267\\002x\\001\\272\\002|\\001\\317\\002}\\001\\322\\002\\201\\001\\360\\002\\202\\001\\363\\002\\206\\001\\376\\002\\207\\002\\001\\002\\213\\002\\014\\002\\214\\002\\017\\002\\220\\002\\032\\002\\221\\002\\035\\002\\225\\002(\\002\\226\\002+\\002\\232\\0023\\002\\233\\0026\\002\\237\\002V\\002\\240\\002Y\\002\\244\\002q\\002\\245\\002t\\002\\251\\002\\224\\002\\252\\002\\227\\002\\256\\002\\257\\002\\257\\002\\262\\002\\263\\002\\272\\002\\273\\000\\001\\001\\027\\000\\000\\000\\002\\000\\001t\\000\\025_1314116315778_112849t\\0002net.sf.jasperreports.engine.design.JRJavacCompiler', '\\254\\355\\000\\005p', '\\377\\330\\377\\340\\000\\020JFIF\\000\\001\\001\\001\\000`\\000`\\000\\000\\377\\333\\000C\\000\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\377\\333\\000C\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\377\\300\\000\\021\\010\\000%\\000q\\003\\001"\\000\\002\\021\\001\\003\\021\\001\\377\\304\\000\\034\\000\\000\\002\\002\\003\\001\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\010\\011\\012\\005\\006\\007\\001\\004\\377\\304\\000:\\020\\000\\001\\004\\002\\002\\001\\003\\001\\006\\003\\003\\015\\000\\000\\000\\000\\004\\002\\003\\005\\006\\001\\007\\000\\010\\022\\011\\021\\023\\024\\025\\026!1Q\\221\\031"#\\012AX\\0272RTa\\201\\222\\225\\226\\261\\321\\323\\325\\377\\304\\000\\033\\001\\000\\002\\003\\001\\001\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\005\\006\\007\\010\\004\\002\\001\\377\\304\\0009\\021\\000\\002\\003\\000\\001\\002\\004\\003\\003\\007\\015\\000\\000\\000\\000\\000\\002\\003\\001\\004\\005\\006\\021\\022\\000\\007\\023\\024\\0251A\\026!"\\010\\027$Sb\\222\\2412EFQRTacdq\\221\\321\\360\\377\\332\\000\\014\\003\\001\\000\\002\\021\\003\\021\\000?\\000\\277\\307\\022m\\361\\204\\201s\\260\\035{\\206\\331S12\\365\\332\\275\\177H\\225B*q\\230\\370\\255\\205$l\\234k\\300\\034\\364!\\301\\011\\003i\\227\\236>\\267\\210\\213\\005\\267(\\200t, &N\\035\\301O\\034\\236\\353\\277v\\322t\\206\\251\\264l\\254A=er\\277\\366C,\\303\\262VBC\\344\\315\\315G\\301\\212\\341\\247$c\\024\\014x\\317\\310\\266I\\244\\340W\\324\\201\\332s\\015\\264\\247\\024\\234s\\215\\327\\366S\\375\\240\\352\\335\\242\\336\\024\\012\\252\\366&\\303\\231~4$\\037\\231\\001\\203\\272\\320\\213f~\\275%\\031%\\221Cu\\341\\233\\236\\213\\213-\\265\\254V\\010\\031\\324:6r\\265\\263\\207\\334I<\\217\\0269\\030q)\\276\\270\\344G\\210\\316F\\031~\\233\\375R\\304U\\365f2\\374;\\322\\366\\276\\230\\337r\\353\\372R\\377\\000q$]\\360\\251\\\\\\021\\303(\\307\\322\\234r\\337\\212\\247\\360q\\323\\034r\\273\\336\\256\\310\\322:\\245tj\\372~\\247\\255''5@\\235\\337\\012\\225DGl\\262\\016`e\\246\\250\\263<=R\\264\\305\\245\\346\\310\\262\\263\\003\\022\\325\\201\\366\\262\\2256\\364\\313`\\260\\231''\\020\\244a(^\\026f\\036W\\233iJ\\027\\357\\344\\204\\3419\\3061\\261r?{E\\264\\247\\356\\3322\\252\\366\\215\\3311\\321WY\\351\\332)R\\220\\265{\\220Q\\227bb,\\242\\270#p\\321\\256\\204{\\022\\221\\245\\342\\301+\\006\\351\\216a\\320\\225\\364\\002\\032\\322\\337J\\026\\244/y\\263\\237\\266k=]\\002\\203-p1\\216\\313]\\251\\022t\\332l\\2442\\031\\232\\260&\\366tih\\002o\\011\\303\\203\\262KuQ\\324\\314\\245\\222}\\367G\\000V\\302|\\327\\212\\371]\\037\\017\\372F\\273]\\311-`\\0366\\275z\\325\\262\\250i\\217$r+\\375\\237\\262\\313\\266\\356Uf]k\\013\\262\\313\\314\\323\\242\\025\\006\\335\\324M\\001Z\\352\\333\\252`\\346\\033a~\\006\\347\\255x\\350\\325\\215\\034\\366\\271\\327\\355R\\234e\\265\\263\\254\\200\\255^\\263\\306\\363\\224i\\012\\303J\\321X*\\365\\231\\026\\210\\315\\365\\336$\\260\\020\\357\\227''\\207\\020\\037M\\355\\027\\332\\236\\275\\365\\347\\356/owZ\\267\\236\\320r\\343`\\231\\032\\312\\344\\254\\244\\363\\221U\\303\\2626#\\240\\22592\\3232\\022?\\013\\315\\024n2\\343IlL\\033\\201\\031\\312\\220\\3163\\235\\307e\\366?`\\253eK\\352]\\007\\256F\\330vj\\210c\\237w\\226\\223\\220\\372(85\\024\\322\\036\\026%\\013C\\203\\245\\331\\007\\233Zp\\254\\270cyK\\277 \\355\\014\\363\\214\\024\\261\\370\\374\\303\\346<g\\313e\\372\\373\\032N\\323M\\235\\210\\303\\304\\016=\\225\\257\\263\\241\\3114L,9(\\302\\306\\251D\\265\\356\\033*T\\265x\\207\\331.k\\323\\256\\3736}\\025(\\312:x\\237\\034\\331\\346\\015$\\346\\325USE\\011\\323\\322f\\255\\372\\031\\264\\262)\\211\\245Lv\\236\\215\\233!F\\260\\015\\213\\010\\25436\\012[a\\312J`\\330\\301\\031s<S\\237\\317\\030\\375\\261\\303\\333\\037\\246?lr>l\\335\\331\\222\\214\\321\\261\\373\\022>\\21437\\324l"\\265\\325\\206\\221,aJf.Z"<\\331\\031\\202\\031 d\\262K\\303 v\\006q\\214\\2518\\370\\362J\\231uN)\\234\\270\\254\\316\\346\\355\\374\\266\\267\\245iYh*\\274D\\345\\257jV!-f\\303\\224Q\\250\\012\\0262V:\\035\\314\\272\\225\\015\\237\\252\\312\\034\\226\\230h\\020\\324\\377\\000\\266\\035K/\\373\\347.7\\234b\\257\\263\\371H\\371EW?_Q\\234\\215\\345K\\023\\217q\\356M}\\241\\217\\255&\\031\\274\\242\\342\\363\\361\\324\\272\\363Ll3I\\266\\332\\265Y\\312\\205E\\3722]n!0''\\3336O\\223~`\\276\\336}\\021\\307P\\331\\323\\327\\327\\305\\250\\007\\243B\\004\\256a\\327+z\\014&\\305\\211PR\\004\\001\\032oI\\373[Q\\037\\243\\265\\275\\303\\325\\355\\366\\307\\351\\217\\333\\236a)\\307\\344\\224\\343\\337\\363\\366\\3061\\357\\357\\371\\377\\000w\\020\\311\\376\\313\\357R\\366\\026\\306\\250j\\375C^\\274\\001\\254\\031\\257\\375\\344%\\311\\242@=&J\\303&H\\220\\306io%\\267\\236h\\206O\\031\\246\\231C\\356\\247\\0147\\227Q\\225\\272\\224\\253\\023\\262\\373\\267)\\035\\2465\\016\\300\\325\\224\\200,7m\\263\\261\\030\\327aR,2$\\017\\210\\371\\226Z\\221nXg\\014\\017\\003\\251k\\024\\361\\300m\\222\\034K\\014\\250S\\333}\\346\\332\\312\\260\\224\\311\\370\\237\\233\\374#\\231\\362\\013\\274c\\026\\336\\244kS\\235\\337Htpvr\\351j\\207\\030\\330\\036?\\310[\\205\\247z\\2223\\266\\227\\215\\260k\\243\\2419\\326\\254\\025v\\265rc\\000]\\320\\223{\\313\\356M\\3072km\\351W\\2434,F_|\\323\\324\\316\\275f\\201mg|[ 5(\\325\\262\\313\\231\\247\\243\\237\\007j\\244\\\\B\\241\\313Y\\366\\317tDL\\205x\\247\\037\\222q\\217\\367c\\207\\212\\177L~\\330\\377\\000g\\3761\\373c\\210&\\242\\355\\226\\317''r\\021\\244\\273\\035\\252`\\365-\\210\\212\\031\\333\\026\\022n6\\322\\324\\214\\023\\265\\350\\262\\010\\036A\\351GH[\\214\\002\\313_D{\\237\\\\\\271\\024\\241\\254\\004\\362\\010\\035\\264-\\247\\326\\343ce\\353\\205GU\\2461\\260)9\\211\\274\\034\\304]*S\\026\\270,\\307\\\\$\\312R\\3204uX\\334\\037\\364\\326\\003\\210[n%\\201"],\\207\\224\\332\\322\\333j\\312U\\214Z@\\226\\262:\\255L8\\356\\355\\352\\000E\\035\\335\\244}\\275F''\\361v\\001\\037O\\237h\\221t\\351\\0231\\007\\223\\001\\236\\204B3\\323\\257I(\\211\\351\\326\\007\\257\\337?.\\342\\210\\353\\375s\\021\\363\\230\\361\\272\\370\\247\\375\\034~?\\237\\341\\217\\307\\373\\377\\000\\357\\370\\360\\366\\307\\351\\217\\333\\034\\204\\255\\351\\352Y\\330}>\\324\\223\\311\\323\\232\\216K\\027\\036\\374P\\272\\211\\241\\333\\032\\377\\000\\213\\023\\266\\252U\\214K\\031s\\373\\006\\330\\335VfEPS\\321(\\205\\015\\015\\326\\236\\304y\\002\\256M\\237\\264\\007N<s\\227\\023\\274}\\345\\241\\365?\\255\\275\\212\\333U\\253\\016\\272\\276l\\315\\023P\\213\\234''V.\\341\\032\\344\\303R\\226+$%R\\272=\\232\\026 \\347lQ\\000\\035/<\\023\\177+\\202\\016\\343\\251V\\022\\312\\374\\226\\234\\362N|+\\220\\015\\214j\\301UOn\\365\\240\\247\\233\\025\\255"\\3101\\346\\332\\025\\342\\032\\304\\231\\205p\\213:5\\352\\233\\034@\\003f\\034\\231(4\\262!P\\356gJ\\356\\264\\232k\\014\\365K\\254\\372\\252b\\210B\\001\\354\\352\\002b$\\311\\225WcDB$\\211}\\207\\021\\332c2\\370\\373c\\364\\307\\355\\216\\034\\257\\317\\361!\\365\\035\\377\\000\\011\\332c\\376{\\177\\377\\000\\335\\303\\215\\377\\0006|\\217\\365\\370\\037O\\351\\006W\\327\\247\\372\\217\\332\\217\\343\\343\\217\\355Fg\\3664>\\237\\315\\366\\376\\275?\\313\\375\\250\\376>$0j\\226\\320\\257\\331\\301\\226\\177R\\334\\255\\3621\\323\\327w\\266\\324\\303\\367:\\321\\225\\355\\253G\\237flx(\\212\\245vb\\314\\343\\007\\033\\026\\262\\353\\362,BMD\\324G\\211\\036\\022J0I\\022W \\323R\\031\\235{\\257\\351\\333v\\341y\\232\\205\\214\\261S\\264\\320eD\\300\\312k\\024\\214u>.\\347\\260\\241\\004Rg\\244l\\265\\2542\\031\\021\\341A\\012H\\025\\243aEX\\361\\326c\\343\\334.a\\203\\306\\216\\216p\\226B\\321\\253Qh\\230~c;\\003g\\300e\\366\\330o\\354\\312\\305\\271Q1\\015|\\015\\341\\277\\221\\220\\260\\013\\370m\\307}\\274\\337_\\311\\237\\221\\314\\345^\\330\\367\\366\\3452\\275P{\\271\\352\\033\\320N\\335\\354MC\\256wE\\236\\271\\251\\347M]\\367^;+\\011\\013*\\374\\2606e\\375|\\301/M\\035\\032\\267\\344\\316fa\\322Z9\\305\\253\\012B\\262\\337\\213M2\\246\\223\\205\\\\/\\206\\352s\\275\\250\\301\\307\\261\\234\\213\\347U\\366\\325\\032V[YO\\032\\322\\022\\325$\\225^\\311\\033\\341fN\\205\\372q\\022\\2458\\344\\243\\263\\244\\366ml\\325\\301\\245:\\027We\\225\\305\\253I\\315U\\003Mr\\336\\260&plT\\012\\344\\242\\003\\273\\273\\371d\\003\\323\\361u\\213\\234\\231\\245u\\011\\361I\\204''Y\\321\\263\\026\\332\\022\\206Ff\\261\\020/\\323\\370{x,W\\206\\021\\222\\005y\\031\\306\\024\\207\\307u\\267\\220\\274ais\\012\\3063\\305&\\332\\305\\203C\\333\\255qu\\260/\\033\\006\\327y\\247\\343:\\216\\326\\261\\211\\274Y(u\\210#c\\230\\271\\325\\234\\304\\251\\011\\025Q5\\346\\315\\032\\327\\022\\373\\231zV\\310Y-DJ\\242`\\250\\330\\274=F\\377\\000\\343\\203\\352k\\376$$\\277\\351\\232\\307\\377\\0003\\223o\\350\\235\\330^\\356z\\201\\356;\\305\\223\\260[\\202\\3531\\2535-T\\266"f\\240\\006\\213\\253\\026\\315\\332\\312\\340\\202\\375\\236$\\304trT\\362\\025\\020\\323\\357\\036"\\260\\352S\\361\\213\\234\\245\\013\\312W\\211\\317,\\3623\\230\\360\\314+\\234\\207j\\347\\035\\032\\024\\245\\002a[F\\323,\\265\\226^\\252\\352Uu39"\\326I\\266\\012G\\324\\036\\213\\003?\\220\\317\\204Y<\\357\\027j\\3723\\251''Fl?\\324\\221\\226\\326P\\250\\005K&\\0310\\306\\311\\310\\214\\010\\314wv\\317\\342\\221\\037\\231}\\326R\\320\\363\\223\\222\\247^EM\\216\\345s\\240G\\375\\330\\315F\\331}\\207LE\\210\\311\\243\\005\\224r\\345\\024\\217hJ\\353\\222\\021\\021+j\\011\\320\\315v)\\274\\262t\\224\\254Sd>\\314kxiy\\353%\\342\\233E\\234\\355Q\\227\\231\\370\\250;\\024~\\330\\261L\\317bP\\266\\0059\\310V\\024Z\\302u\\206\\236Z^-\\257,\\023\\206\\033\\035.-N:\\204\\245\\031S\\310\\363r*\\032\\335\\025\\031''dSy\\330\\326L\\2722\\306\\372\\033m\\241SQ\\315\\371\\255\\013\\371\\333\\027!\\017\\340J|<P\\357\\236}\\222\\265\\343\\307>^\\370\\321\\357\\235]\\321\\373*\\326\\213\\245\\272\\222!\\366\\014\\345\\214\\232S\\005\\030\\003r\\377\\000L\\226\\333c\\355\\201\\303}\\226d\\024\\206\\232m\\217\\221\\364\\345\\3250\\2042\\265\\251\\264!)\\307~gqNe\\253\\257\\301yg\\004\\3739g{\\205\\337\\3370\\314\\345v\\264(\\344\\332\\255\\310\\360\\254c6\\330\\333\\314\\245\\243eW\\263\\030i\\260\\225\\305^\\313uN\\365OqX\\334\\015\\213\\327\\204\\356\\361\\31249F\\017)\\370\\302r\\271%\\\\\\241+\\270H\\251j\\372\\035\\217\\252\\235\\020\\2575\\356\\331\\250\\223\\255t\\005\\252a\\372\\360Hx\\325\\177\\244\\341Y.R}\\321\\260\\241w\\373\\232x\\250\\010\\031X(2h\\373\\333a\\346:d0\\2049\\324@T\\345b\\302\\225[\\001\\022Sk\\024\\331F\\323\\364\\204:\\264<\\372\\025\\227\\262\\332S\\224)|\\026\\222\\223v\\213\\332^nY\\225\\266,\\325\\303Ni\\252\\270\\357g\\335h\\253jh!fn\\347\\266\\214\\345I\\300\\205L\\245\\247~D+\\3139\\034\\214-\\011\\3160\\234\\3141zw\\\\\\2330\\334\\353\\265\\261[9\\212A\\272\\344\\\\\\012\\343\\302\\006\\025>C\\313\\005\\304\\004\\000\\3566\\030\\215\\272\\225e\\0373\\014\\241\\364\\267\\204\\241.a)N1\\210\\210\\320Z\\246\\011\\2723QUv\\203o\\\\&\\301\\367A\\015\\232w\\264c\\226\\201\\334\\026h\\214\\371\\021\\237\\252(\\266\\035q\\030$\\257\\225\\346|\\363\\226V\\332\\275\\263\\314\\355\\310\\177&o0\\271G*o!\\336\\344\\274gC\\342\\027\\262\\355n\\212Wn\\202\\366''>\\207\\005\\343\\262sEY\\355MT\\006.\\1775\\322\\247\\236\\017j\\253\\354\\337\\307\\\\\\274\\301M\\266\\213{''\\316\\236#\\207\\204\\274\\214\\254]\\232\\236\\322\\265\\324e\\311\\225{g\\235\\026\\255r}\\210\\030\\262\\313ke\\206\\026\\235\\2765J\\305\\262X1\\271\\3254N\\026$\\305\\327j\\203\\327]\\231I\\251W;1\\270m\\266\\030\\210\\354\\331v\\265\\316Y\\241\\237<\\\\I\\232\\004_\\315\\210\\300\\201\\216S\\270,\\225\\272\\262\\362 -6\\332\\276E+\\011\\302\\274p\\245a\\004~\\231?p\\231\\352n\\267&\\362\\255G-j\\232\\334;\\331\\273K\\21601\\025\\006d\\262\\317\\330G3\\223\\012\\001\\226\\3374z\\313y\\003\\012%\\234\\345R\\010\\313\\177\\324VS\\311{\\217\\351\\227[\\343\\244\\231\\225N\\267\\000\\302Y#\\005a\\2719\\011i \\334\\177\\013\\371<\\310\\010\\303]\\024\\224\\371\\377\\0002\\232!\\247\\032_\\342\\225\\266\\244\\347)\\317\\337\\265\\272\\215\\240\\267]\\200\\013>\\306\\244"r^2\\024Z\\364{\\255\\313LF\\260$@o\\224@\\341\\260\\034i\\242\\210\\332\\020\\351\\217\\347\\311,\\341JNP\\214\\347(m\\030M\\241\\345\\037\\225<\\353\\216\\351q\\215\\036wc\\213\\2548G\\036\\345y\\030\\2258\\315\\335M#\\325\\326\\347;\\324\\367\\271''#\\330\\265\\243\\227\\2165\\214\\216\\202\\353Q\\315\\255^\\320\\204\\333\\275b\\305\\326\\034\\240F\\021\\346\\007;\\342\\372\\364\\366\\351\\361em\\221rml\\035\\0157\\355V\\245HhP\\342\\37162\\261\\261\\363\\353\\323\\275\\241\\353\\014\\015\\243u\\253\\216j&}\\275d\\246\\260\\017\\252E_\\276\\342[\\216\\353\\306\\271\\365\\002~[e\\017\\330\\211\\364t\\371\\350@7\\001E\\025!;T/aZa\\365\\320U\\024\\276\\324\\244\\214X\\255\\024}\\304r\\237@\\352\\313\\210\\371\\030V\\036mK,u\\353vTS\\342\\267''\\241\\347Ucd\\241M\\033\\250\\035k\\233\\355&\\334\\020\\002\\306$(Iz\\326\\222\\203\\221\\254J\\034\\261\\226\\342\\004#3\\361S\\3666\\260\\342P\\346C<R[VZ1\\016f\\301/\\364o\\253$\\352M\\213\\244\\037\\324u\\367\\265\\346\\327\\213DE\\3661\\345\\232\\371\\363\\341\\016C&\\200\\227\\346\\337%\\331vW\\025 0\\322q+\\030\\306~\\316\\223\\035\\231\\001p\\331H\\371s\\307\\265w\\2457Et\\326M\\177^iQ\\240\\244\\3455M\\273LLN"\\307f"vf\\211z\\021\\350\\3730rr\\217\\312\\254\\242dO\\216}Q\\315Lei\\221\\217\\217C!G>(\\3144\\332w_\\016\\345\\374\\177\\007\\217\\272\\225\\360\\325\\2351\\275\\310,\\326\\366U\\2532\\243>1\\306c\\217Ts\\334\\333\\250j\\231\\2306u\\\\\\000\\025\\333\\352\\035\\264\\230\\265^\\211\\0133^\\3266\\215\\375\\000}r\\251\\355f\\276z\\231\\353\\265\\240\\320\\366Z\\237\\021p\\000\\002\\030&\\026\\245U@\\210\\232\\035\\260\\223\\211\\003\\357\\031\\032\\263\\350S\\347\\014\\242zlL\\213\\030\\311\\326k^\\315\\357\\377\\000\\251D\\344I,)\\326\\3146\\210\\305\\240:2\\244\\031Fp\\343\\314\\255\\212\\022\\034\\360\\316s\\345\\211%\\374ng+\\316s\\205\\262u\\207\\2566\\336\\207\\372q\\335\\0048[gr;\\251\\3328\\027\\266\\205\\333\\357\\003\\3627{\\344u\\272\\354Fo\\220\\226\\306Rk\\231"\\032\\012N>\\250\\350\\341\\020#c\\2058\\234\\030\\204$\\322^uw\\004\\327]\\014\\352\\276\\252\\265j\\333\\225''W\\207\\0319\\246u,\\256\\220\\327.\\021+3&\\025\\177[\\316\\022\\361s0h\\214\\222<\\250\\363\\034\\225t\\222p|\\211\\243\\221 SD\\220\\313\\304\\255\\267\\234J\\270\\206\\254\\364\\200\\364\\371\\323\\033\\002\\267\\263\\365\\346\\204\\216\\203\\272S/\\310\\330\\364\\371_\\2746r\\323W\\237a\\012\\300c\\302\\206T\\263\\241\\211\\006\\003\\253\\311AB\\345\\227\\001d\\264\\264G\\306\\247\\030c-X\\277\\236|\\010\\264\\333U\\207\\221f\\222.?J\\250SE8\\370\\201\\262\\3670\\264\\234\\255\\023\\370\\210\\022s\\221;\\271\\006D\\270\\265\\353\\316q\\201V\\022UgDo\\354N\\207\\244\\012d\\347Y\\026%u\\234Nc\\377\\000G\\201F*Yn\\260\\373b\\203\\262\\310\\241r#\\272U!\\026 \\241\\223\\336\\300\\360\\350\\177\\221j\\217\\372\\243\\177\\360c\\207;\\007\\016g/\\177w\\373\\313\\277~\\177\\367\\322?\\343\\305\\225\\355\\321\\372\\240\\375\\330\\377\\000\\017\\372\\217\\007\\020\\336\\367\\372z\\365\\367\\277Z\\375\\212\\306\\342\\207$K\\005q\\242\\336\\246\\337\\340U\\201\\254\\365w\\336G\\233\\211\\035\\337$6p.8\\204\\270\\354q\\236c\\255x\\316q\\204\\341\\307\\260\\341\\303\\236h\\336\\273\\231n\\275\\374\\353V(\\336\\250\\321uku\\034\\304XCBz\\213\\024\\345\\020\\230\\024}\\361\\324J:\\304\\310\\317X\\231\\211\\372\\364&\\322Y^\\302\\226\\3644d\\032\\226\\200\\261l\\011\\371\\211\\201D\\211G\\373\\307\\317\\244\\307\\337\\036+iL\\376\\316\\236\\255\\224\\332\\371\\254\\314vR\\362]hy\\017\\027C\\022\\203\\012\\004\\221\\002\\241\\344aL}\\253\\367\\220\\2242\\265\\241YG\\316\\2002\\244\\377\\000\\235\\204{\\362\\326\\235_\\352\\306\\230\\351\\376\\251\\210\\323\\372B\\254\\305j\\255\\031\\234\\222[\\252Z\\312\\226\\235\\225u8\\3012\\363r/eD\\034s\\371\\306\\177\\231\\305\\345\\015#\\372m%8\\362\\312\\216\\034\\221r.y\\3149z\\253\\247\\222o\\336\\324EI\\202\\257]\\304\\265V\\006v@\\372\\323^\\262\\322\\226X\\355\\222\\037p\\3007\\300\\221\\0143\\241\\024J\\354\\334\\014|yc3s\\321U\\216\\352,`A\\033H:\\211vC\\032Fb\\276\\356\\205+\\022\\020\\231\\201\\231\\036\\243\\035\\030\\236\\0348r''\\341\\267\\203\\207\\016\\034<\\036\\016\\0348p\\360x8p\\341\\303\\301\\340\\341\\303\\207\\017\\007\\203\\207\\016\\034<\\036?\\377\\331\\377\\330\\377\\340\\000\\020JFIF\\000\\001\\001\\001\\000`\\000`\\000\\000\\377\\333\\000C\\000\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\377\\333\\000C\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\001\\377\\300\\000\\021\\010\\000%\\000q\\003\\001"\\000\\002\\021\\001\\003\\021\\001\\377\\304\\000\\034\\000\\000\\002\\002\\003\\001\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\010\\011\\012\\005\\006\\007\\001\\004\\377\\304\\000:\\020\\000\\001\\004\\002\\002\\001\\003\\001\\006\\003\\003\\015\\000\\000\\000\\000\\004\\002\\003\\005\\006\\001\\007\\000\\010\\022\\011\\021\\023\\024\\025\\026!1Q\\221\\031"#\\012AX\\0272RTa\\201\\222\\225\\226\\261\\321\\323\\325\\377\\304\\000\\033\\001\\000\\002\\003\\001\\001\\001\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\005\\006\\007\\010\\004\\002\\001\\377\\304\\0009\\021\\000\\002\\003\\000\\001\\002\\004\\003\\003\\007\\015\\000\\000\\000\\000\\000\\002\\003\\001\\004\\005\\006\\021\\022\\000\\007\\023\\024\\0251A\\026!"\\010\\027$Sb\\222\\2412EFQRTacdq\\221\\321\\360\\377\\332\\000\\014\\003\\001\\000\\002\\021\\003\\021\\000?\\000\\277\\307\\022m\\361\\204\\201s\\260\\035{\\206\\331S12\\365\\332\\275\\177H\\225B*q\\230\\370\\255\\205$l\\234k\\300\\034\\364!\\301\\011\\003i\\227\\236>\\267\\210\\213\\005\\267(\\200t, &N\\035\\301O\\034\\236\\353\\277v\\322t\\206\\251\\264l\\254A=er\\277\\366C,\\303\\262VBC\\344\\315\\315G\\301\\212\\341\\247$c\\024\\014x\\317\\310\\266I\\244\\340W\\324\\201\\332s\\015\\264\\247\\024\\234s\\215\\327\\366S\\375\\240\\352\\335\\242\\336\\024\\012\\252\\366&\\303\\231~4$\\037\\231\\001\\203\\272\\320\\213f~\\275%\\031%\\221Cu\\341\\233\\236\\213\\213-\\265\\254V\\010\\031\\324:6r\\265\\263\\207\\334I<\\217\\0269\\030q)\\276\\270\\344G\\210\\316F\\031~\\233\\375R\\304U\\365f2\\374;\\322\\366\\276\\230\\337r\\353\\372R\\377\\000q$]\\360\\251\\\\\\021\\303(\\307\\322\\234r\\337\\212\\247\\360q\\323\\034r\\273\\336\\256\\310\\322:\\245tj\\372~\\247\\255''5@\\235\\337\\012\\225DGl\\262\\016`e\\246\\250\\263<=R\\264\\305\\245\\346\\310\\262\\263\\003\\022\\325\\201\\366\\262\\2256\\364\\313`\\260\\231''\\020\\244a(^\\026f\\036W\\233iJ\\027\\357\\344\\204\\3419\\3061\\261r?{E\\264\\247\\356\\3322\\252\\366\\215\\3311\\321WY\\351\\332)R\\220\\265{\\220Q\\227bb,\\242\\270#p\\321\\256\\204{\\022\\221\\245\\342\\301+\\006\\351\\216a\\320\\225\\364\\002\\032\\322\\337J\\026\\244/y\\263\\237\\266k=]\\002\\203-p1\\216\\313]\\251\\022t\\332l\\2442\\031\\232\\260&\\366tih\\002o\\011\\303\\203\\262KuQ\\324\\314\\245\\222}\\367G\\000V\\302|\\327\\212\\371]\\037\\017\\372F\\273]\\311-`\\0366\\275z\\325\\262\\250i\\217$r+\\375\\237\\262\\313\\266\\356Uf]k\\013\\262\\313\\314\\323\\242\\025\\006\\335\\324M\\001Z\\352\\333\\252`\\346\\033a~\\006\\347\\255x\\350\\325\\215\\034\\366\\271\\327\\355R\\234e\\265\\263\\254\\200\\255^\\263\\306\\363\\224i\\012\\303J\\321X*\\365\\231\\026\\210\\315\\365\\336$\\260\\020\\357\\227''\\207\\020\\037M\\355\\027\\332\\236\\275\\365\\347\\356/owZ\\267\\236\\320r\\343`\\231\\032\\312\\344\\254\\244\\363\\221U\\303\\2626#\\240\\22592\\3232\\022?\\013\\315\\024n2\\343IlL\\033\\201\\031\\312\\220\\3163\\235\\307e\\366?`\\253eK\\352]\\007\\256F\\330vj\\210c\\237w\\226\\223\\220\\372(85\\024\\322\\036\\026%\\013C\\203\\245\\331\\007\\233Zp\\254\\270cyK\\277 \\355\\014\\363\\214\\024\\261\\370\\374\\303\\346<g\\313e\\372\\373\\032N\\323M\\235\\210\\303\\304\\016=\\225\\257\\263\\241\\3114L,9(\\302\\306\\251D\\265\\356\\033*T\\265x\\207\\331.k\\323\\256\\3736}\\025(\\312:x\\237\\034\\331\\346\\015$\\346\\325USE\\011\\323\\322f\\255\\372\\031\\264\\262)\\211\\245Lv\\236\\215\\233!F\\260\\015\\213\\010\\25436\\012[a\\312J`\\330\\301\\031s<S\\237\\317\\030\\375\\261\\303\\333\\037\\246?lr>l\\335\\331\\222\\214\\321\\261\\373\\022>\\21437\\324l"\\265\\325\\206\\221,aJf.Z"<\\331\\031\\202\\031 d\\262K\\303 v\\006q\\214\\2518\\370\\362J\\231uN)\\234\\270\\254\\316\\346\\355\\374\\266\\267\\245iYh*\\274D\\345\\257jV!-f\\303\\224Q\\250\\012\\0262V:\\035\\314\\272\\225\\015\\237\\252\\312\\034\\226\\230h\\020\\324\\377\\000\\266\\035K/\\373\\347.7\\234b\\257\\263\\371H\\371EW?_Q\\234\\215\\345K\\023\\217q\\356M}\\241\\217\\255&\\031\\274\\242\\342\\363\\361\\324\\272\\363Ll3I\\266\\332\\265Y\\312\\205E\\3722]n!0''\\3336O\\223~`\\276\\336}\\021\\307P\\331\\323\\327\\327\\305\\250\\007\\243B\\004\\256a\\327+z\\014&\\305\\211PR\\004\\001\\032oI\\373[Q\\037\\243\\265\\275\\303\\325\\355\\366\\307\\351\\217\\333\\236a)\\307\\344\\224\\343\\337\\363\\366\\3061\\357\\357\\371\\377\\000w\\020\\311\\376\\313\\357R\\366\\026\\306\\250j\\375C^\\274\\001\\254\\031\\257\\375\\344%\\311\\242@=&J\\303&H\\220\\306io%\\267\\236h\\206O\\031\\246\\231C\\356\\247\\0147\\227Q\\225\\272\\224\\253\\023\\262\\373\\267)\\035\\2465\\016\\300\\325\\224\\200,7m\\263\\261\\030\\327aR,2$\\017\\210\\371\\226Z\\221nXg\\014\\017\\003\\251k\\024\\361\\300m\\222\\034K\\014\\250S\\333}\\346\\332\\312\\260\\224\\311\\370\\237\\233\\374#\\231\\362\\013\\274c\\026\\336\\244kS\\235\\337Htpvr\\351j\\207\\030\\330\\036?\\310[\\205\\247z\\2223\\266\\227\\215\\260k\\243\\2419\\326\\254\\025v\\265rc\\000]\\320\\223{\\313\\356M\\3072km\\351W\\2434,F_|\\323\\324\\316\\275f\\201mg|[ 5(\\325\\262\\313\\231\\247\\243\\237\\007j\\244\\\\B\\241\\313Y\\366\\317tDL\\205x\\247\\037\\222q\\217\\367c\\207\\212\\177L~\\330\\377\\000g\\3761\\373c\\210&\\242\\355\\226\\317''r\\021\\244\\273\\035\\252`\\365-\\210\\212\\031\\333\\026\\022n6\\322\\324\\214\\023\\265\\350\\262\\010\\036A\\351GH[\\214\\002\\313_D{\\237\\\\\\271\\024\\241\\254\\004\\362\\010\\035\\264-\\247\\326\\343ce\\353\\205GU\\2461\\260)9\\211\\274\\034\\304]*S\\026\\270,\\307\\\\$\\312R\\3204uX\\334\\037\\364\\326\\003\\210[n%\\201"],\\207\\224\\332\\322\\333j\\312U\\214Z@\\226\\262:\\255L8\\356\\355\\352\\000E\\035\\335\\244}\\275F''\\361v\\001\\037O\\237h\\221t\\351\\0231\\007\\223\\001\\236\\204B3\\323\\257I(\\211\\351\\326\\007\\257\\337?.\\342\\210\\353\\375s\\021\\363\\230\\361\\272\\370\\247\\375\\034~?\\237\\341\\217\\307\\373\\377\\000\\357\\370\\360\\366\\307\\351\\217\\333\\034\\204\\255\\351\\352Y\\330}>\\324\\223\\311\\323\\232\\216K\\027\\036\\374P\\272\\211\\241\\333\\032\\377\\000\\213\\023\\266\\252U\\214K\\031s\\373\\006\\330\\335VfEPS\\321(\\205\\015\\015\\326\\236\\304y\\002\\256M\\237\\264\\007N<s\\227\\023\\274}\\345\\241\\365?\\255\\275\\212\\333U\\253\\016\\272\\276l\\315\\023P\\213\\234''V.\\341\\032\\344\\303R\\226+$%R\\272=\\232\\026 \\347lQ\\000\\035/<\\023\\177+\\202\\016\\343\\251V\\022\\312\\374\\226\\234\\362N|+\\220\\015\\214j\\301UOn\\365\\240\\247\\233\\025\\255"\\3101\\346\\332\\025\\342\\032\\304\\231\\205p\\213:5\\352\\233\\034@\\003f\\034\\231(4\\262!P\\356gJ\\356\\264\\232k\\014\\365K\\254\\372\\252b\\210B\\001\\354\\352\\002b$\\311\\225WcDB$\\211}\\207\\021\\332c2\\370\\373c\\364\\307\\355\\216\\034\\257\\317\\361!\\365\\035\\377\\000\\011\\332c\\376{\\177\\377\\000\\335\\303\\215\\377\\0006|\\217\\365\\370\\037O\\351\\006W\\327\\247\\372\\217\\332\\217\\343\\343\\217\\355Fg\\3664>\\237\\315\\366\\376\\275?\\313\\375\\250\\376>$0j\\226\\320\\257\\331\\301\\226\\177R\\334\\255\\3621\\323\\327w\\266\\324\\303\\367:\\321\\225\\355\\253G\\237flx(\\212\\245vb\\314\\343\\007\\033\\026\\262\\353\\362,BMD\\324G\\211\\036\\022J0I\\022W \\323R\\031\\235{\\257\\351\\333v\\341y\\232\\205\\214\\261S\\264\\320eD\\300\\312k\\024\\214u>.\\347\\260\\241\\004Rg\\244l\\265\\2542\\031\\021\\341A\\012H\\025\\243aEX\\361\\326c\\343\\334.a\\203\\306\\216\\216p\\226B\\321\\253Qh\\230~c;\\003g\\300e\\366\\330o\\354\\312\\305\\271Q1\\015|\\015\\341\\277\\221\\220\\260\\013\\370m\\307}\\274\\337_\\311\\237\\221\\314\\345^\\330\\367\\366\\3452\\275P{\\271\\352\\033\\320N\\335\\354MC\\256wE\\236\\271\\251\\347M]\\367^;+\\011\\013*\\374\\2606e\\375|\\301/M\\035\\032\\267\\344\\316fa\\322Z9\\305\\253\\012B\\262\\337\\213M2\\246\\223\\205\\\\/\\206\\352s\\275\\250\\301\\307\\261\\234\\213\\347U\\366\\325\\032V[YO\\032\\322\\022\\325$\\225^\\311\\033\\341fN\\205\\372q\\022\\2458\\344\\243\\263\\244\\366ml\\325\\301\\245:\\027We\\225\\305\\253I\\315U\\003Mr\\336\\260&plT\\012\\344\\242\\003\\273\\273\\371d\\003\\323\\361u\\213\\234\\231\\245u\\011\\361I\\204''Y\\321\\263\\026\\332\\022\\206Ff\\261\\020/\\323\\370{x,W\\206\\021\\222\\005y\\031\\306\\024\\207\\307u\\267\\220\\274ais\\012\\3063\\305&\\332\\305\\203C\\333\\255qu\\260/\\033\\006\\327y\\247\\343:\\216\\326\\261\\211\\274Y(u\\210#c\\230\\271\\325\\234\\304\\251\\011\\025Q5\\346\\315\\032\\327\\022\\373\\231zV\\310Y-DJ\\242`\\250\\330\\274=F\\377\\000\\343\\203\\352k\\376$$\\277\\351\\232\\307\\377\\0003\\223o\\350\\235\\330^\\356z\\201\\356;\\305\\223\\260[\\202\\3531\\2535-T\\266"f\\240\\006\\213\\253\\026\\315\\332\\312\\340\\202\\375\\236$\\304trT\\362\\025\\020\\323\\357\\036"\\260\\352S\\361\\213\\234\\245\\013\\312W\\211\\317,\\3623\\230\\360\\314+\\234\\207j\\347\\035\\032\\024\\245\\002a[F\\323,\\265\\226^\\252\\352Uu39"\\326I\\266\\012G\\324\\036\\213\\003?\\220\\317\\204Y<\\357\\027j\\3723\\251''Fl?\\324\\221\\226\\326P\\250\\005K&\\0310\\306\\311\\310\\214\\010\\314wv\\317\\342\\221\\037\\231}\\326R\\320\\363\\223\\222\\247^EM\\216\\345s\\240G\\375\\330\\315F\\331}\\207LE\\210\\311\\243\\005\\224r\\345\\024\\217hJ\\353\\222\\021\\021+j\\011\\320\\315v)\\274\\262t\\224\\254Sd>\\314kxiy\\353%\\342\\233E\\234\\355Q\\227\\231\\370\\250;\\024~\\330\\261L\\317bP\\266\\0059\\310V\\024Z\\302u\\206\\236Z^-\\257,\\023\\206\\033\\035.-N:\\204\\245\\031S\\310\\363r*\\032\\335\\025\\031''dSy\\330\\326L\\2722\\306\\372\\033m\\241SQ\\315\\371\\255\\013\\371\\333\\027!\\017\\340J|<P\\357\\236}\\222\\265\\343\\307>^\\370\\321\\357\\235]\\321\\373*\\326\\213\\245\\272\\222!\\366\\014\\345\\214\\232S\\005\\030\\003r\\377\\000L\\226\\333c\\355\\201\\303}\\226d\\024\\206\\232m\\217\\221\\364\\345\\3250\\2042\\265\\251\\264!)\\307~gqNe\\253\\257\\301yg\\004\\3739g{\\205\\337\\3370\\314\\345v\\264(\\344\\332\\255\\310\\360\\254c6\\330\\333\\314\\245\\243eW\\263\\030i\\260\\225\\305^\\313uN\\365OqX\\334\\015\\213\\327\\204\\356\\361\\31249F\\017)\\370\\302r\\271%\\\\\\241+\\270H\\251j\\372\\035\\217\\252\\235\\020\\2575\\356\\331\\250\\223\\255t\\005\\252a\\372\\360Hx\\325\\177\\244\\341Y.R}\\321\\260\\241w\\373\\232x\\250\\010\\031X(2h\\373\\333a\\346:d0\\2049\\324@T\\345b\\302\\225[\\001\\022Sk\\024\\331F\\323\\364\\204:\\264<\\372\\025\\227\\262\\332S\\224)|\\026\\222\\223v\\213\\332^nY\\225\\266,\\325\\303Ni\\252\\270\\357g\\335h\\253jh!fn\\347\\266\\214\\345I\\300\\205L\\245\\247~D+\\3139\\034\\214-\\011\\3160\\234\\3141zw\\\\\\2330\\334\\353\\265\\261[9\\212A\\272\\344\\\\\\012\\343\\302\\006\\025>C\\313\\005\\304\\004\\000\\3566\\030\\215\\272\\225e\\0373\\014\\241\\364\\267\\204\\241.a)N1\\210\\210\\320Z\\246\\011\\2723QUv\\203o\\\\&\\301\\367A\\015\\232w\\264c\\226\\201\\334\\026h\\214\\371\\021\\237\\252(\\266\\035q\\030$\\257\\225\\346|\\363\\226V\\332\\275\\263\\314\\355\\310\\177&o0\\271G*o!\\336\\344\\274gC\\342\\027\\262\\355n\\212Wn\\202\\366''>\\207\\005\\343\\262sEY\\355MT\\006.\\1775\\322\\247\\236\\017j\\253\\354\\337\\307\\\\\\274\\301M\\266\\213{''\\316\\236#\\207\\204\\274\\214\\254]\\232\\236\\322\\265\\324e\\311\\225{g\\235\\026\\255r}\\210\\030\\262\\313ke\\206\\026\\235\\2765J\\305\\262X1\\271\\3254N\\026$\\305\\327j\\203\\327]\\231I\\251W;1\\270m\\266\\030\\210\\354\\331v\\265\\316Y\\241\\237<\\\\I\\232\\004_\\315\\210\\300\\201\\216S\\270,\\225\\272\\262\\362 -6\\332\\276E+\\011\\302\\274p\\245a\\004~\\231?p\\231\\352n\\267&\\362\\255G-j\\232\\334;\\331\\273K\\21601\\025\\006d\\262\\317\\330G3\\223\\012\\001\\226\\3374z\\313y\\003\\012%\\234\\345R\\010\\313\\177\\324VS\\311{\\217\\351\\227[\\343\\244\\231\\225N\\267\\000\\302Y#\\005a\\2719\\011i \\334\\177\\013\\371<\\310\\010\\303]\\024\\224\\371\\377\\0002\\232!\\247\\032_\\342\\225\\266\\244\\347)\\317\\337\\265\\272\\215\\240\\267]\\200\\013>\\306\\244"r^2\\024Z\\364{\\255\\313LF\\260$@o\\224@\\341\\260\\034i\\242\\210\\332\\020\\351\\217\\347\\311,\\341JNP\\214\\347(m\\030M\\241\\345\\037\\225<\\353\\216\\351q\\215\\036wc\\213\\2548G\\036\\345y\\030\\2258\\315\\335M#\\325\\326\\347;\\324\\367\\271''#\\330\\265\\243\\227\\2165\\214\\216\\202\\353Q\\315\\255^\\320\\204\\333\\275b\\305\\326\\034\\240F\\021\\346\\007;\\342\\372\\364\\366\\351\\361em\\221rml\\035\\0157\\355V\\245HhP\\342\\37162\\261\\261\\363\\353\\323\\275\\241\\353\\014\\015\\243u\\253\\216j&}\\275d\\246\\260\\017\\252E_\\276\\342[\\216\\353\\306\\271\\365\\002~[e\\017\\330\\211\\364t\\371\\350@7\\001E\\025!;T/aZa\\365\\320U\\024\\276\\324\\244\\214X\\255\\024}\\304r\\237@\\352\\313\\210\\371\\030V\\036mK,u\\353vTS\\342\\267''\\241\\347Ucd\\241M\\033\\250\\035k\\233\\355&\\334\\020\\002\\306$(Iz\\326\\222\\203\\221\\254J\\034\\261\\226\\342\\004#3\\361S\\3666\\260\\342P\\346C<R[VZ1\\016f\\301/\\364o\\253$\\352M\\213\\244\\037\\324u\\367\\265\\346\\327\\213DE\\3661\\345\\232\\371\\363\\341\\016C&\\200\\227\\346\\337%\\331vW\\025 0\\322q+\\030\\306~\\316\\223\\035\\231\\001p\\331H\\371s\\307\\265w\\2457Et\\326M\\177^iQ\\240\\244\\3455M\\273LLN"\\307f"vf\\211z\\021\\350\\3730rr\\217\\312\\254\\242dO\\216}Q\\315Lei\\221\\217\\217C!G>(\\3144\\332w_\\016\\345\\374\\177\\007\\217\\272\\225\\360\\325\\2351\\275\\310,\\326\\366U\\2532\\243>1\\306c\\217Ts\\334\\333\\250j\\231\\2306u\\\\\\000\\025\\333\\352\\035\\264\\230\\265^\\211\\0133^\\3266\\215\\375\\000}r\\251\\355f\\276z\\231\\353\\265\\240\\320\\366Z\\237\\021p\\000\\002\\030&\\026\\245U@\\210\\232\\035\\260\\223\\211\\003\\357\\031\\032\\263\\350S\\347\\014\\242zlL\\213\\030\\311\\326k^\\315\\357\\377\\000\\251D\\344I,)\\326\\3146\\210\\305\\240:2\\244\\031Fp\\343\\314\\255\\212\\022\\034\\360\\316s\\345\\211%\\374ng+\\316s\\205\\262u\\207\\2566\\336\\207\\372q\\335\\0048[gr;\\251\\3328\\027\\266\\205\\333\\357\\003\\3627{\\344u\\272\\354Fo\\220\\226\\306Rk\\231"\\032\\012N>\\250\\350\\341\\020#c\\2058\\234\\030\\204$\\322^uw\\004\\327]\\014\\352\\276\\252\\265j\\333\\225''W\\207\\0319\\246u,\\256\\220\\327.\\021+3&\\025\\177[\\316\\022\\361s0h\\214\\222<\\250\\363\\034\\225t\\222p|\\211\\243\\221 SD\\220\\313\\304\\255\\267\\234J\\270\\206\\254\\364\\200\\364\\371\\323\\033\\002\\267\\263\\365\\346\\204\\216\\203\\272S/\\310\\330\\364\\371_\\2746r\\323W\\237a\\012\\300c\\302\\206T\\263\\241\\211\\006\\003\\253\\311AB\\345\\227\\001d\\264\\264G\\306\\247\\030c-X\\277\\236|\\010\\264\\333U\\207\\221f\\222.?J\\250SE8\\370\\201\\262\\3670\\264\\234\\255\\023\\370\\210\\022s\\221;\\271\\006D\\270\\265\\353\\316q\\201V\\022UgDo\\354N\\207\\244\\012d\\347Y\\026%u\\234Nc\\377\\000G\\201F*Yn\\260\\373b\\203\\262\\310\\241r#\\272U!\\026 \\241\\223\\336\\300\\360\\350\\177\\221j\\217\\372\\243\\177\\360c\\207;\\007\\016g/\\177w\\373\\313\\277~\\177\\367\\322?\\343\\305\\225\\355\\321\\372\\240\\375\\330\\377\\000\\017\\372\\217\\007\\020\\336\\367\\372z\\365\\367\\277Z\\375\\212\\306\\342\\207$K\\005q\\242\\336\\246\\337\\340U\\201\\254\\365w\\336G\\233\\211\\035\\337$6p.8\\204\\270\\354q\\236c\\255x\\316q\\204\\341\\307\\260\\341\\303\\236h\\336\\273\\231n\\275\\374\\353V(\\336\\250\\321uku\\034\\304XCBz\\213\\024\\345\\020\\230\\024}\\361\\324J:\\304\\310\\317X\\231\\211\\372\\364&\\322Y^\\302\\226\\3644d\\032\\226\\200\\261l\\011\\371\\211\\201D\\211G\\373\\307\\317\\244\\307\\337\\036+iL\\376\\316\\236\\255\\224\\332\\371\\254\\314vR\\362]hy\\017\\027C\\022\\203\\012\\004\\221\\002\\241\\344aL}\\253\\367\\220\\2242\\265\\241YG\\316\\2002\\244\\377\\000\\235\\204{\\362\\326\\235_\\352\\306\\230\\351\\376\\251\\210\\323\\372B\\254\\305j\\255\\031\\234\\222[\\252Z\\312\\226\\235\\225u8\\3012\\363r/eD\\034s\\371\\306\\177\\231\\305\\345\\015#\\372m%8\\362\\312\\216\\034\\221r.y\\3149z\\253\\247\\222o\\336\\324EI\\202\\257]\\304\\265V\\006v@\\372\\323^\\262\\322\\226X\\355\\222\\037p\\3007\\300\\221\\0143\\241\\024J\\354\\334\\014|yc3s\\321U\\216\\352,`A\\033H:\\211vC\\032Fb\\276\\356\\205+\\022\\020\\231\\201\\231\\036\\243\\035\\030\\236\\0348r''\\341\\267\\203\\207\\016\\034<\\036\\016\\0348p\\360x8p\\341\\303\\301\\340\\341\\303\\207\\017\\007\\203\\207\\016\\034<\\036?\\377\\331', '{4617,4617}', '{33072}', '"Report"', NULL, '{LogoCMDBuild1.jpg,LogoCMDBuild2.jpg}');









INSERT INTO "Role" VALUES (677, '"Role"', 'Helpdesk', 'Helpdesk', 'A', 'admin', '2011-08-23 22:31:24.685', NULL, false, '"Asset"', 'helpdesk@cmdbuild.org', '{bulkupdate,importcsv,exportcsv}', NULL, NULL, false, false, false, false, false);
INSERT INTO "Role" VALUES (940, '"Role"', 'ChangeManager', 'Change manager', 'A', 'system', '2012-08-24 10:22:41.248', NULL, false, '-', NULL, NULL, NULL, NULL, false, false, false, false, false);
INSERT INTO "Role" VALUES (941, '"Role"', 'Specialist', 'Specialist', 'A', 'system', '2012-08-24 10:22:41.248', NULL, false, '-', NULL, NULL, NULL, NULL, false, false, false, false, false);
INSERT INTO "Role" VALUES (942, '"Role"', 'Services', 'Services', 'A', 'system', '2012-08-24 10:22:41.248', NULL, true, '-', NULL, NULL, NULL, NULL, false, false, false, false, false);
INSERT INTO "Role" VALUES (14, '"Role"', 'SuperUser', 'SuperUser', 'A', 'system', '2011-03-16 11:15:37.240985', NULL, true, NULL, NULL, '{}', '{}', '{}', false, false, false, false, false);



INSERT INTO "Room" VALUES (104, '"Room"', 'DC01001', 'Data Center - Floor 1 - Room 001', 'A', 'admin', '2011-07-24 23:50:09.333', NULL, 79, 27, 28.00, 110);
INSERT INTO "Room" VALUES (200, '"Room"', 'DC01002', 'Data Center - Floor 1 - Room 002
', 'A', 'admin', '2011-07-24 23:51:13.304', NULL, 79, 157, 62.00, 108);
INSERT INTO "Room" VALUES (206, '"Room"', 'B101001', 'Office Building A - Floor 1 - Room 001', 'A', 'admin', '2011-07-24 23:56:14.609', NULL, 83, 27, 18.00, 110);
INSERT INTO "Room" VALUES (212, '"Room"', 'B101002', 'Office Building A - Floor 1 - Room 002', 'A', 'admin', '2011-07-24 23:56:56.466', NULL, 83, 27, 18.00, 110);
INSERT INTO "Room" VALUES (218, '"Room"', 'B101003', 'Office Building A - Floor 1 - Room 003', 'A', 'admin', '2011-07-24 23:57:24.774', NULL, 83, 27, 18.00, 110);
INSERT INTO "Room" VALUES (224, '"Room"', 'B102001', 'Office Building A - Floor 2 - Room 001', 'A', 'admin', '2011-07-24 23:57:56.042', NULL, 87, 155, 48.00, 110);
INSERT INTO "Room" VALUES (230, '"Room"', 'B102002', 'Office Building A - Floor 2 - Room 002', 'A', 'admin', '2011-07-24 23:58:29.941', NULL, 87, 156, 48.00, 110);
INSERT INTO "Room" VALUES (236, '"Room"', 'B103001', 'Office Building A - Floor 3 - Room 001', 'A', 'admin', '2011-07-24 23:59:12.074', NULL, 92, 154, 128.00, 112);
INSERT INTO "Room" VALUES (242, '"Room"', 'B201001', 'Office Building B - Floor 1 - Room 001', 'A', 'admin', '2011-07-24 23:59:40.137', NULL, 96, 27, 18.00, 108);
INSERT INTO "Room" VALUES (248, '"Room"', 'B201002', 'Office Building B - Floor 1 - Room 002', 'A', 'admin', '2011-07-25 00:00:13.196', NULL, 96, 27, 18.00, 108);
INSERT INTO "Room" VALUES (260, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'A', 'admin', '2011-09-02 11:53:26.9', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 26.00, 108);
INSERT INTO "Room" VALUES (254, '"Room"', 'B201003', 'Office Building B - Floor 1 - Room 003', 'A', 'admin', '2011-09-02 11:56:58.957', NULL, 96, 156, 18.00, 110);
INSERT INTO "Room" VALUES (266, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'A', 'admin', '2011-08-30 16:22:46.448', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the building C</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112);
INSERT INTO "Room" VALUES (272, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'A', 'admin', '2011-09-02 11:54:54.974', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4<span style="color: rgb(255, 0, 0);">gh ouregou</span>regireh goreh goreg oeufg orehg oureòg yu5y uy5 u 5yu 5yu yj yu5 5yu 5yu u5yu 5 u<br><ul><li>hore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh o</li><li>uregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg ir</li><li>egie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oure</li><li>òghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or</li></ul>4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 108);



INSERT INTO "Room_history" VALUES (196, '"Room"', 'DC01001', 'Data Center - Floor 1 - Room 001', 'U', 'admin', '2011-07-24 18:45:44.718', NULL, 79, NULL, 28.00, NULL, 104, '2011-07-24 23:50:09.333');
INSERT INTO "Room_history" VALUES (711, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-07-25 00:01:52.818', NULL, 100, 27, 24.00, 112, 266, '2011-08-29 12:20:03.608');
INSERT INTO "Room_history" VALUES (712, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-29 12:20:03.608', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of the work</span> <span class="hps">in the building</span> <span class="hps">C.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-29 12:20:42.359');
INSERT INTO "Room_history" VALUES (729, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-07-25 00:02:19.16', NULL, 100, 27, 24.00, 112, 272, '2011-08-30 16:20:50.591');
INSERT INTO "Room_history" VALUES (730, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-08-30 16:20:50.591', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 112, 272, '2011-08-30 16:21:11.789');
INSERT INTO "Room_history" VALUES (731, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-08-30 16:21:11.789', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4<span style="color: rgb(255, 0, 0);">gh ouregou</span>regireh goreh goreg oeufg orehg oureòg<br><ul><li>hore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh o</li><li>uregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg ir</li><li>egie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oure</li><li>òghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or</li></ul>4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 112, 272, '2011-08-30 16:21:22.461');
INSERT INTO "Room_history" VALUES (732, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-29 12:20:42.359', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the building</span> <span class="hps">C.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:21:38.937');
INSERT INTO "Room_history" VALUES (733, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:21:38.937', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the buildingb </span><span class="hps">C.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:21:48.929');
INSERT INTO "Room_history" VALUES (745, '"Room"', 'B201003', 'Office Building B - Floor 1 - Room 003', 'U', 'admin', '2011-08-30 16:36:35.379', NULL, 96, 156, 18.00, 108, 254, '2011-09-02 11:56:58.957');
INSERT INTO "Room_history" VALUES (734, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:21:48.929', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the buildingb </span><span class="hps">ruruhf3 ir3hfg 3ihf ir3hf i3h .<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:03.09');
INSERT INTO "Room_history" VALUES (735, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:22:03.09', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the building C</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:22.253');
INSERT INTO "Room_history" VALUES (736, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:22:22.253', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the buildingqC</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:36.904');
INSERT INTO "Room_history" VALUES (737, '"Room"', 'B202002', 'Office Building B - Floor 2 - Room 002', 'U', 'admin', '2011-08-30 16:22:36.904', '​<span id="result_box" class="short_text" lang="en"><span class="hps">The room is</span> <b><font color="#ff0000"><span class="hps"></span></font><font color="#ff0000"><span class="hps">temporary</span></font><font color="#ff0000"> </font></b><span class="hps">used by Administration, </span></span><span id="result_box" class="" lang="en"><span class="hps">pending the conclusion</span> <span class="hps">of works</span> <span class="hps">in the_building_C</span><span class="hps">.<br>
  <br>
Scheduled dates:<br>
</span></span>
<ul><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">start date: 15/10/2001</span></span></li><li><span id="result_box" class="short_text" lang="en"><span class="hps">temporary</span> <span class="hps">use end </span></span><span id="result_box" class="short_text" lang="en"><span class="hps">date: 15/05/2012</span></span></li></ul>
', 100, 27, 24.00, 112, 266, '2011-08-30 16:22:46.448');
INSERT INTO "Room_history" VALUES (738, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-07-25 00:01:29.684', NULL, 100, 27, 24.00, 112, 260, '2011-08-30 16:23:31.002');
INSERT INTO "Room_history" VALUES (739, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-08-30 16:23:31.002', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 24.00, 112, 260, '2011-08-30 16:23:44.308');
INSERT INTO "Room_history" VALUES (740, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-08-30 16:23:44.308', 'The room is <span style="color: rgb(255, 0, 0);">temporary </span>used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 24.00, 112, 260, '2011-08-30 16:24:10.851');
INSERT INTO "Room_history" VALUES (741, '"Room"', 'B201003', 'Office Building B - Floor 1 - Room 003', 'U', 'admin', '2011-07-25 00:00:42.222', NULL, 96, 27, 18.00, 108, 254, '2011-08-30 16:36:35.379');
INSERT INTO "Room_history" VALUES (742, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-08-30 16:24:10.851', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 24.00, 112, 260, '2011-09-02 11:53:03.347');
INSERT INTO "Room_history" VALUES (743, '"Room"', 'B202001', 'Office Building B - Floor 2 - Room 001', 'U', 'admin', '2011-09-02 11:53:03.347', 'The room is temporary used by Administration, pending the conclusion of works in the building C.<br><br>Scheduled dates:<br><br>&nbsp;&nbsp;&nbsp; * temporary use start date: 15/10/2001<br>&nbsp;&nbsp;&nbsp; * temporary use end date: 15/05/2012<br>', 100, 27, 26.00, 112, 260, '2011-09-02 11:53:26.9');
INSERT INTO "Room_history" VALUES (744, '"Room"', 'B202003', 'Office Building B - Floor 2 - Room 003', 'U', 'admin', '2011-08-30 16:21:22.461', '​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4<span style="color: rgb(255, 0, 0);">gh ouregou</span>regireh goreh goreg oeufg orehg oureòg yu5y uy5 u 5yu 5yu yj yu5 5yu 5yu u5yu 5 u<br><ul><li>hore goireò gierhg ierò girehg iregh iregh ireg iregie ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh o</li><li>uregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg ir</li><li>egie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oure</li><li>òghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or</li></ul>4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ​httrgh 4t h4tp h4otj t4ojh 4toh4rgh or4gh ouregouregireh goreh goreg 
oeufg orehg oureòghore goireò gierhg ierò girehg iregh iregh ireg iregie
 ', 100, 27, 24.00, 112, 272, '2011-09-02 11:54:54.974');



INSERT INTO "Scheduler" VALUES (515, '"Scheduler"', 'StartProcess', 'Test workflow', 'N', 'system', '2011-08-23 16:42:08.164', NULL, '0 0 0 * * ?', 'Test');



INSERT INTO "Scheduler_history" VALUES (516, '"Scheduler"', 'StartProcess', 'Test workflow', 'U', 'system', '2011-08-23 16:40:29.549', NULL, '0 0 0 * * ?', 'Test', 515, '2011-08-23 16:42:08.164');









INSERT INTO "Supplier" VALUES (706, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'A', 'admin', '2011-08-23 23:29:19.436', 'This supplier is very <font color="#ff0000">reliable</font>.<br><span id="result_box" class="short_text" lang="en"><span class="hps">Delivery dates</span> <span class="hps">are always</span> <span class="hps">fulfilled.<br></span></span>Rating:<br><ul><li>quality: good</li><li>prices: good</li></ul><span id="result_box" class="short_text" lang="en"><span class="hps"></span></span>', 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', 65);
INSERT INTO "Supplier" VALUES (714, '"Supplier"', 'SUP02', 'HP', 'A', 'admin', '2011-08-29 12:50:04.459', NULL, 28, NULL, NULL, NULL, NULL, 'info@hp.com', 'www.hp.com', 69);
INSERT INTO "Supplier" VALUES (721, '"Supplier"', 'SUP003', 'Dell', 'A', 'admin', '2011-08-29 13:21:30.725', NULL, 28, NULL, NULL, NULL, NULL, 'info@dell.com', 'www.dell.com', 69);
INSERT INTO "Supplier" VALUES (723, '"Supplier"', 'SUP004', 'Misco', 'A', 'admin', '2011-08-29 13:23:10.823', NULL, 158, NULL, NULL, NULL, NULL, NULL, NULL, 25);









INSERT INTO "Supplier_history" VALUES (707, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'U', 'admin', '2011-08-23 23:16:41.642', NULL, 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', NULL, 706, '2011-08-23 23:18:50.004');
INSERT INTO "Supplier_history" VALUES (708, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'U', 'admin', '2011-08-23 23:18:50.004', NULL, 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', 65, 706, '2011-08-23 23:23:33.472');
INSERT INTO "Supplier_history" VALUES (709, '"Supplier"', 'SUP001', 'Tecnoteca Srl', 'U', 'admin', '2011-08-23 23:23:33.472', '​This supplier is very reliable.<br><span id="result_box" class="short_text" lang="en"><span class="hps">Delivery dates</span> <span class="hps">are always</span> <span class="hps">fulfilled.</span></span><br>', 28, 'Via L''Aquila 1/B', '33010', 'Tavagnacco (UD)', '+39 0432 689094', 'tecnoteca@tecnoteca.com', 'http://www.tecnoteca.com', 65, 706, '2011-08-23 23:29:19.436');
INSERT INTO "Supplier_history" VALUES (715, '"Supplier"', 'SUP02', 'Dell ', 'U', 'admin', '2011-08-29 12:48:58.926', NULL, 28, NULL, NULL, NULL, NULL, 'info@dell.com', 'www.dell.com', 69, 714, '2011-08-29 12:50:04.459');









INSERT INTO "User" VALUES (13, '"User"', NULL, 'Administrator', 'A', 'system', '2011-03-16 11:15:37.221385', NULL, 'admin', 'DQdKW32Mlms=', NULL);
INSERT INTO "User" VALUES (943, '"User"', NULL, 'workflow', 'A', 'admin', '2012-08-24 10:22:41.248', NULL, 'workflow', 'sLPdlW/0y4msBompb4oRVw==', NULL);
INSERT INTO "User" VALUES (678, '"User"', NULL, 'Jones Patricia', 'A', 'admin', '2011-08-23 22:36:40.224', NULL, 'pjones', 'Tms67HRN+qusMUAsM6xIPA==', 'patricia.jones@gmail.com');
INSERT INTO "User" VALUES (679, '"User"', NULL, 'Davis Michael', 'A', 'admin', '2011-08-23 22:37:48.154', NULL, 'mdavis', 'Nlg70IVc7/U=', 'michael.davis@gmail.com');









INSERT INTO "_Dashboards" VALUES (831, 'system', '2012-08-23 22:04:26.088', '{"name":"Item situation","description":"Item situation","charts":{"6172e925-4aa7-4734-a112-2dd9e33863a9":{"name":"Total number of item","description":"Total number of item","dataSourceName":"cmf_count_active_cards","type":"gauge","singleSeriesField":"Count","fgcolor":"#99CC00","bgcolor":"#C0C0C0","active":true,"autoLoad":true,"legend":false,"height":0,"maximum":50,"minimum":0,"steps":5,"dataSourceParameters":[{"name":"ClassName","type":"STRING","fieldType":"classes","defaultValue":"Asset","required":false}]},"98b4927d-6a8e-49c5-8051-b64109aeee8b":{"name":"Number of items by item brand","description":"Number of items by item brand","dataSourceName":"cmf_active_asset_for_brand","type":"pie","singleSeriesField":"Number","labelField":"Brand","active":true,"autoLoad":true,"legend":true,"height":0,"maximum":0,"minimum":0,"steps":0},"3b6bb717-b9e4-402f-b188-1d8e81135adf":{"name":"Number of items by item type","description":"Number of items by item type","dataSourceName":"cmf_active_cards_for_class","type":"bar","categoryAxisField":"Class","categoryAxisLabel":"Asset type","valueAxisLabel":"Number","chartOrientation":"vertical","active":true,"autoLoad":true,"legend":true,"height":0,"maximum":0,"minimum":0,"steps":0,"dataSourceParameters":[{"name":"ClassName","type":"STRING","fieldType":"classes","defaultValue":"Asset","required":false}],"valueAxisFields":["Number"]}},"columns":[{"width":0.3,"charts":["6172e925-4aa7-4734-a112-2dd9e33863a9"]},{"width":0.36721992,"charts":["98b4927d-6a8e-49c5-8051-b64109aeee8b"]},{"width":0.3327801,"charts":["3b6bb717-b9e4-402f-b188-1d8e81135adf"]}],"groups":["SuperUser","Helpdesk"]}');
INSERT INTO "_Dashboards" VALUES (946, 'system', '2012-08-24 10:25:56.862', '{"name":"RfC situation","description":"RfC situation","charts":{"07706c7e-b4cc-4873-b112-9f2a6a2b0f2f":{"name":"Open RfC by status","description":"Open RfC by status","dataSourceName":"cmf_open_rfc_for_status","type":"pie","singleSeriesField":"Number","labelField":"Status","active":true,"autoLoad":true,"legend":false,"height":0,"maximum":0,"minimum":0,"steps":0}},"groups":["SuperUser","Helpdesk","ChangeManager","Specialist"]}');






ALTER TABLE ONLY "Activity"
    ADD CONSTRAINT "Activity_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Asset"
    ADD CONSTRAINT "Asset_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Building_history"
    ADD CONSTRAINT "Building_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Building"
    ADD CONSTRAINT "Building_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Class"
    ADD CONSTRAINT "Class_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Computer"
    ADD CONSTRAINT "Computer_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Email_history"
    ADD CONSTRAINT "Email_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Email"
    ADD CONSTRAINT "Email_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Employee_history"
    ADD CONSTRAINT "Employee_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Employee"
    ADD CONSTRAINT "Employee_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Floor_history"
    ADD CONSTRAINT "Floor_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Floor"
    ADD CONSTRAINT "Floor_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Grant"
    ADD CONSTRAINT "Grant_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Invoice_history"
    ADD CONSTRAINT "Invoice_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Invoice"
    ADD CONSTRAINT "Invoice_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "License_history"
    ADD CONSTRAINT "License_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "License"
    ADD CONSTRAINT "License_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "LookUp"
    ADD CONSTRAINT "LookUp_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Map_ActivityEmail_history"
    ADD CONSTRAINT "Map_ActivityEmail_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_ActivityEmail"
    ADD CONSTRAINT "Map_ActivityEmail_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_AssetAssignee_history"
    ADD CONSTRAINT "Map_AssetAssignee_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_AssetAssignee"
    ADD CONSTRAINT "Map_AssetAssignee_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_AssetReference_history"
    ADD CONSTRAINT "Map_AssetReference_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_AssetReference"
    ADD CONSTRAINT "Map_AssetReference_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_BuildingFloor_history"
    ADD CONSTRAINT "Map_BuildingFloor_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_BuildingFloor"
    ADD CONSTRAINT "Map_BuildingFloor_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_FloorRoom_history"
    ADD CONSTRAINT "Map_FloorRoom_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_FloorRoom"
    ADD CONSTRAINT "Map_FloorRoom_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_Members_history"
    ADD CONSTRAINT "Map_Members_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_Members"
    ADD CONSTRAINT "Map_Members_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_NetworkDeviceConnection_history"
    ADD CONSTRAINT "Map_NetworkDeviceConnection_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_NetworkDeviceConnection"
    ADD CONSTRAINT "Map_NetworkDeviceConnection_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_OfficeRoom_history"
    ADD CONSTRAINT "Map_OfficeRoom_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_OfficeRoom"
    ADD CONSTRAINT "Map_OfficeRoom_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RFCChangeManager_history"
    ADD CONSTRAINT "Map_RFCChangeManager_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RFCChangeManager"
    ADD CONSTRAINT "Map_RFCChangeManager_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RFCExecutor_history"
    ADD CONSTRAINT "Map_RFCExecutor_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RFCExecutor"
    ADD CONSTRAINT "Map_RFCExecutor_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RFCRequester_history"
    ADD CONSTRAINT "Map_RFCRequester_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RFCRequester"
    ADD CONSTRAINT "Map_RFCRequester_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RoomAsset_history"
    ADD CONSTRAINT "Map_RoomAsset_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RoomAsset"
    ADD CONSTRAINT "Map_RoomAsset_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RoomNetworkPoint_history"
    ADD CONSTRAINT "Map_RoomNetworkPoint_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RoomNetworkPoint"
    ADD CONSTRAINT "Map_RoomNetworkPoint_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_RoomWorkplace_history"
    ADD CONSTRAINT "Map_RoomWorkplace_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_RoomWorkplace"
    ADD CONSTRAINT "Map_RoomWorkplace_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_Supervisor_history"
    ADD CONSTRAINT "Map_Supervisor_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_Supervisor"
    ADD CONSTRAINT "Map_Supervisor_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_SupplierAsset_history"
    ADD CONSTRAINT "Map_SupplierAsset_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_SupplierAsset"
    ADD CONSTRAINT "Map_SupplierAsset_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_SupplierContact_history"
    ADD CONSTRAINT "Map_SupplierContact_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_SupplierContact"
    ADD CONSTRAINT "Map_SupplierContact_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_SupplierInvoice_history"
    ADD CONSTRAINT "Map_SupplierInvoice_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_SupplierInvoice"
    ADD CONSTRAINT "Map_SupplierInvoice_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_UserRole_history"
    ADD CONSTRAINT "Map_UserRole_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_UserRole"
    ADD CONSTRAINT "Map_UserRole_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



ALTER TABLE ONLY "Map_WorkplaceComposition_history"
    ADD CONSTRAINT "Map_WorkplaceComposition_history_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "EndDate");



ALTER TABLE ONLY "Map_WorkplaceComposition"
    ADD CONSTRAINT "Map_WorkplaceComposition_pkey" PRIMARY KEY ("IdDomain", "IdClass1", "IdObj1", "IdClass2", "IdObj2", "BeginDate");



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



ALTER TABLE ONLY "NetworkDevice_history"
    ADD CONSTRAINT "NetworkDevice_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "NetworkDevice"
    ADD CONSTRAINT "NetworkDevice_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "NetworkPoint_history"
    ADD CONSTRAINT "NetworkPoint_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "NetworkPoint"
    ADD CONSTRAINT "NetworkPoint_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Notebook_history"
    ADD CONSTRAINT "Notebook_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Notebook"
    ADD CONSTRAINT "Notebook_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Office_history"
    ADD CONSTRAINT "Office_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Office"
    ADD CONSTRAINT "Office_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "PC_history"
    ADD CONSTRAINT "PC_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "PC"
    ADD CONSTRAINT "PC_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Patch_history"
    ADD CONSTRAINT "Patch_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Patch"
    ADD CONSTRAINT "Patch_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Printer_history"
    ADD CONSTRAINT "Printer_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Printer"
    ADD CONSTRAINT "Printer_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Rack_history"
    ADD CONSTRAINT "Rack_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Rack"
    ADD CONSTRAINT "Rack_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Report"
    ADD CONSTRAINT "Report_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "RequestForChange_history"
    ADD CONSTRAINT "RequestForChange_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "RequestForChange"
    ADD CONSTRAINT "RequestForChange_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Role"
    ADD CONSTRAINT "Role_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Room_history"
    ADD CONSTRAINT "Room_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Room"
    ADD CONSTRAINT "Room_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Scheduler_history"
    ADD CONSTRAINT "Scheduler_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Scheduler"
    ADD CONSTRAINT "Scheduler_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Server_history"
    ADD CONSTRAINT "Server_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Server"
    ADD CONSTRAINT "Server_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "SupplierContact_history"
    ADD CONSTRAINT "SupplierContact_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "SupplierContact"
    ADD CONSTRAINT "SupplierContact_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Supplier_history"
    ADD CONSTRAINT "Supplier_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Supplier"
    ADD CONSTRAINT "Supplier_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "UPS_history"
    ADD CONSTRAINT "UPS_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "UPS"
    ADD CONSTRAINT "UPS_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Workplace_history"
    ADD CONSTRAINT "Workplace_history_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Workplace"
    ADD CONSTRAINT "Workplace_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Dashboards"
    ADD CONSTRAINT "_Dashboards_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "_Templates"
    ADD CONSTRAINT "_Templates_Name_key" UNIQUE ("Name");



ALTER TABLE ONLY "_Templates"
    ADD CONSTRAINT "_Templates_pkey" PRIMARY KEY ("Id");



ALTER TABLE ONLY "Role"
    ADD CONSTRAINT unique_role_code UNIQUE ("Code");



CREATE UNIQUE INDEX "Report_unique_code" ON "Report" USING btree ((CASE WHEN ((("Code")::text = ''::text) OR (("Status")::text <> 'A'::text)) THEN NULL::text ELSE ("Code")::text END));



CREATE INDEX idx_activity_code ON "Activity" USING btree ("Code");



CREATE INDEX idx_activity_description ON "Activity" USING btree ("Description");



CREATE INDEX idx_activity_idclass ON "Activity" USING btree ("IdClass");



CREATE INDEX idx_asset_code ON "Asset" USING btree ("Code");



CREATE INDEX idx_asset_description ON "Asset" USING btree ("Description");



CREATE INDEX idx_asset_idclass ON "Asset" USING btree ("IdClass");



CREATE INDEX idx_building_code ON "Building" USING btree ("Code");



CREATE INDEX idx_building_description ON "Building" USING btree ("Description");



CREATE INDEX idx_building_idclass ON "Building" USING btree ("IdClass");



CREATE INDEX idx_buildinghistory_currentid ON "Building_history" USING btree ("CurrentId");



CREATE INDEX idx_class_code ON "Class" USING btree ("Code");



CREATE INDEX idx_class_description ON "Class" USING btree ("Description");



CREATE INDEX idx_computer_code ON "Computer" USING btree ("Code");



CREATE INDEX idx_computer_description ON "Computer" USING btree ("Description");



CREATE INDEX idx_computer_idclass ON "Computer" USING btree ("IdClass");



CREATE INDEX idx_dashboards_begindate ON "_Dashboards" USING btree ("BeginDate");



CREATE INDEX idx_email_code ON "Email" USING btree ("Code");



CREATE INDEX idx_email_description ON "Email" USING btree ("Description");



CREATE INDEX idx_email_idclass ON "Email" USING btree ("IdClass");



CREATE INDEX idx_emailhistory_currentid ON "Email_history" USING btree ("CurrentId");



CREATE INDEX idx_employee_code ON "Employee" USING btree ("Code");



CREATE INDEX idx_employee_description ON "Employee" USING btree ("Description");



CREATE INDEX idx_employee_idclass ON "Employee" USING btree ("IdClass");



CREATE INDEX idx_employeehistory_currentid ON "Employee_history" USING btree ("CurrentId");



CREATE INDEX idx_floor_code ON "Floor" USING btree ("Code");



CREATE INDEX idx_floor_description ON "Floor" USING btree ("Description");



CREATE INDEX idx_floor_idclass ON "Floor" USING btree ("IdClass");



CREATE INDEX idx_floorhistory_currentid ON "Floor_history" USING btree ("CurrentId");



CREATE INDEX idx_idclass_id ON "Class" USING btree ("IdClass", "Id");



CREATE INDEX idx_invoice_code ON "Invoice" USING btree ("Code");



CREATE INDEX idx_invoice_description ON "Invoice" USING btree ("Description");



CREATE INDEX idx_invoice_idclass ON "Invoice" USING btree ("IdClass");



CREATE INDEX idx_invoicehistory_currentid ON "Invoice_history" USING btree ("CurrentId");



CREATE INDEX idx_license_code ON "License" USING btree ("Code");



CREATE INDEX idx_license_description ON "License" USING btree ("Description");



CREATE INDEX idx_license_idclass ON "License" USING btree ("IdClass");



CREATE INDEX idx_licensehistory_currentid ON "License_history" USING btree ("CurrentId");



CREATE UNIQUE INDEX idx_map_activityemail_activerows ON "Map_ActivityEmail" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_activityemail_uniqueright ON "Map_ActivityEmail" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_assetassignee_activerows ON "Map_AssetAssignee" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_assetassignee_uniqueright ON "Map_AssetAssignee" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_assetreference_activerows ON "Map_AssetReference" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_assetreference_uniqueright ON "Map_AssetReference" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_buildingfloor_activerows ON "Map_BuildingFloor" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_buildingfloor_uniqueright ON "Map_BuildingFloor" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_floorroom_activerows ON "Map_FloorRoom" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_floorroom_uniqueright ON "Map_FloorRoom" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE INDEX idx_map_iddomain ON "Map" USING btree ("IdDomain");



CREATE INDEX idx_map_idobj1 ON "Map" USING btree ("IdObj1");



CREATE INDEX idx_map_idobj2 ON "Map" USING btree ("IdObj2");



CREATE UNIQUE INDEX idx_map_members_activerows ON "Map_Members" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_members_uniqueright ON "Map_Members" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_networkdeviceconnection_activerows ON "Map_NetworkDeviceConnection" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_officeroom_activerows ON "Map_OfficeRoom" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_officeroom_uniqueright ON "Map_OfficeRoom" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_rfcchangemanager_activerows ON "Map_RFCChangeManager" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_rfcchangemanager_uniqueleft ON "Map_RFCChangeManager" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass1" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj1" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_rfcexecutor_activerows ON "Map_RFCExecutor" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_rfcexecutor_uniqueleft ON "Map_RFCExecutor" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass1" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj1" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_rfcrequester_activerows ON "Map_RFCRequester" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_rfcrequester_uniqueleft ON "Map_RFCRequester" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass1" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj1" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_roomasset_activerows ON "Map_RoomAsset" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_roomasset_uniqueright ON "Map_RoomAsset" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_roomnetworkpoint_activerows ON "Map_RoomNetworkPoint" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_roomnetworkpoint_uniqueright ON "Map_RoomNetworkPoint" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_roomworkplace_activerows ON "Map_RoomWorkplace" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_roomworkplace_uniqueright ON "Map_RoomWorkplace" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_supervisor_activerows ON "Map_Supervisor" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_supervisor_uniqueright ON "Map_Supervisor" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_supplierasset_activerows ON "Map_SupplierAsset" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_supplierasset_uniqueright ON "Map_SupplierAsset" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_suppliercontact_activerows ON "Map_SupplierContact" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_suppliercontact_uniqueright ON "Map_SupplierContact" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_supplierinvoice_activerows ON "Map_SupplierInvoice" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_supplierinvoice_uniqueright ON "Map_SupplierInvoice" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE UNIQUE INDEX idx_map_userrole_activerows ON "Map_UserRole" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_userrole_defaultgroup ON "Map_UserRole" USING btree ((CASE WHEN (("Status")::text = 'N'::text) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN (("Status")::text = 'N'::text) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN "DefaultGroup" THEN true ELSE NULL::boolean END));



CREATE UNIQUE INDEX idx_map_workplacecomposition_activerows ON "Map_WorkplaceComposition" USING btree ((CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdDomain" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj1" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::regclass ELSE "IdClass2" END), (CASE WHEN ("Status" = 'N'::bpchar) THEN NULL::integer ELSE "IdObj2" END));



CREATE UNIQUE INDEX idx_map_workplacecomposition_uniqueright ON "Map_WorkplaceComposition" USING btree ((CASE WHEN (("Status")::text = 'A'::text) THEN "IdClass2" ELSE NULL::regclass END), (CASE WHEN (("Status")::text = 'A'::text) THEN "IdObj2" ELSE NULL::integer END));



CREATE INDEX idx_mapactivityemail_iddomain ON "Map_ActivityEmail" USING btree ("IdDomain");



CREATE INDEX idx_mapactivityemail_idobj1 ON "Map_ActivityEmail" USING btree ("IdObj1");



CREATE INDEX idx_mapactivityemail_idobj2 ON "Map_ActivityEmail" USING btree ("IdObj2");



CREATE INDEX idx_mapassetassignee_iddomain ON "Map_AssetAssignee" USING btree ("IdDomain");



CREATE INDEX idx_mapassetassignee_idobj1 ON "Map_AssetAssignee" USING btree ("IdObj1");



CREATE INDEX idx_mapassetassignee_idobj2 ON "Map_AssetAssignee" USING btree ("IdObj2");



CREATE INDEX idx_mapassetreference_iddomain ON "Map_AssetReference" USING btree ("IdDomain");



CREATE INDEX idx_mapassetreference_idobj1 ON "Map_AssetReference" USING btree ("IdObj1");



CREATE INDEX idx_mapassetreference_idobj2 ON "Map_AssetReference" USING btree ("IdObj2");



CREATE INDEX idx_mapbuildingfloor_iddomain ON "Map_BuildingFloor" USING btree ("IdDomain");



CREATE INDEX idx_mapbuildingfloor_idobj1 ON "Map_BuildingFloor" USING btree ("IdObj1");



CREATE INDEX idx_mapbuildingfloor_idobj2 ON "Map_BuildingFloor" USING btree ("IdObj2");



CREATE INDEX idx_mapfloorroom_iddomain ON "Map_FloorRoom" USING btree ("IdDomain");



CREATE INDEX idx_mapfloorroom_idobj1 ON "Map_FloorRoom" USING btree ("IdObj1");



CREATE INDEX idx_mapfloorroom_idobj2 ON "Map_FloorRoom" USING btree ("IdObj2");



CREATE INDEX idx_mapmembers_iddomain ON "Map_Members" USING btree ("IdDomain");



CREATE INDEX idx_mapmembers_idobj1 ON "Map_Members" USING btree ("IdObj1");



CREATE INDEX idx_mapmembers_idobj2 ON "Map_Members" USING btree ("IdObj2");



CREATE INDEX idx_mapnetworkdeviceconnection_iddomain ON "Map_NetworkDeviceConnection" USING btree ("IdDomain");



CREATE INDEX idx_mapnetworkdeviceconnection_idobj1 ON "Map_NetworkDeviceConnection" USING btree ("IdObj1");



CREATE INDEX idx_mapnetworkdeviceconnection_idobj2 ON "Map_NetworkDeviceConnection" USING btree ("IdObj2");



CREATE INDEX idx_mapofficeroom_iddomain ON "Map_OfficeRoom" USING btree ("IdDomain");



CREATE INDEX idx_mapofficeroom_idobj1 ON "Map_OfficeRoom" USING btree ("IdObj1");



CREATE INDEX idx_mapofficeroom_idobj2 ON "Map_OfficeRoom" USING btree ("IdObj2");



CREATE INDEX idx_maprfcchangemanager_iddomain ON "Map_RFCChangeManager" USING btree ("IdDomain");



CREATE INDEX idx_maprfcchangemanager_idobj1 ON "Map_RFCChangeManager" USING btree ("IdObj1");



CREATE INDEX idx_maprfcchangemanager_idobj2 ON "Map_RFCChangeManager" USING btree ("IdObj2");



CREATE INDEX idx_maprfcexecutor_iddomain ON "Map_RFCExecutor" USING btree ("IdDomain");



CREATE INDEX idx_maprfcexecutor_idobj1 ON "Map_RFCExecutor" USING btree ("IdObj1");



CREATE INDEX idx_maprfcexecutor_idobj2 ON "Map_RFCExecutor" USING btree ("IdObj2");



CREATE INDEX idx_maprfcrequester_iddomain ON "Map_RFCRequester" USING btree ("IdDomain");



CREATE INDEX idx_maprfcrequester_idobj1 ON "Map_RFCRequester" USING btree ("IdObj1");



CREATE INDEX idx_maprfcrequester_idobj2 ON "Map_RFCRequester" USING btree ("IdObj2");



CREATE INDEX idx_maproomasset_iddomain ON "Map_RoomAsset" USING btree ("IdDomain");



CREATE INDEX idx_maproomasset_idobj1 ON "Map_RoomAsset" USING btree ("IdObj1");



CREATE INDEX idx_maproomasset_idobj2 ON "Map_RoomAsset" USING btree ("IdObj2");



CREATE INDEX idx_maproomnetworkpoint_iddomain ON "Map_RoomNetworkPoint" USING btree ("IdDomain");



CREATE INDEX idx_maproomnetworkpoint_idobj1 ON "Map_RoomNetworkPoint" USING btree ("IdObj1");



CREATE INDEX idx_maproomnetworkpoint_idobj2 ON "Map_RoomNetworkPoint" USING btree ("IdObj2");



CREATE INDEX idx_maproomworkplace_iddomain ON "Map_RoomWorkplace" USING btree ("IdDomain");



CREATE INDEX idx_maproomworkplace_idobj1 ON "Map_RoomWorkplace" USING btree ("IdObj1");



CREATE INDEX idx_maproomworkplace_idobj2 ON "Map_RoomWorkplace" USING btree ("IdObj2");



CREATE INDEX idx_mapsupervisor_iddomain ON "Map_Supervisor" USING btree ("IdDomain");



CREATE INDEX idx_mapsupervisor_idobj1 ON "Map_Supervisor" USING btree ("IdObj1");



CREATE INDEX idx_mapsupervisor_idobj2 ON "Map_Supervisor" USING btree ("IdObj2");



CREATE INDEX idx_mapsupplierasset_iddomain ON "Map_SupplierAsset" USING btree ("IdDomain");



CREATE INDEX idx_mapsupplierasset_idobj1 ON "Map_SupplierAsset" USING btree ("IdObj1");



CREATE INDEX idx_mapsupplierasset_idobj2 ON "Map_SupplierAsset" USING btree ("IdObj2");



CREATE INDEX idx_mapsuppliercontact_iddomain ON "Map_SupplierContact" USING btree ("IdDomain");



CREATE INDEX idx_mapsuppliercontact_idobj1 ON "Map_SupplierContact" USING btree ("IdObj1");



CREATE INDEX idx_mapsuppliercontact_idobj2 ON "Map_SupplierContact" USING btree ("IdObj2");



CREATE INDEX idx_mapsupplierinvoice_iddomain ON "Map_SupplierInvoice" USING btree ("IdDomain");



CREATE INDEX idx_mapsupplierinvoice_idobj1 ON "Map_SupplierInvoice" USING btree ("IdObj1");



CREATE INDEX idx_mapsupplierinvoice_idobj2 ON "Map_SupplierInvoice" USING btree ("IdObj2");



CREATE INDEX idx_mapuserrole_iddomain ON "Map_UserRole" USING btree ("IdDomain");



CREATE INDEX idx_mapuserrole_idobj1 ON "Map_UserRole" USING btree ("IdObj1");



CREATE INDEX idx_mapuserrole_idobj2 ON "Map_UserRole" USING btree ("IdObj2");



CREATE INDEX idx_mapworkplacecomposition_iddomain ON "Map_WorkplaceComposition" USING btree ("IdDomain");



CREATE INDEX idx_mapworkplacecomposition_idobj1 ON "Map_WorkplaceComposition" USING btree ("IdObj1");



CREATE INDEX idx_mapworkplacecomposition_idobj2 ON "Map_WorkplaceComposition" USING btree ("IdObj2");



CREATE INDEX idx_menu_code ON "Menu" USING btree ("Code");



CREATE INDEX idx_menu_description ON "Menu" USING btree ("Description");



CREATE INDEX idx_menu_idclass ON "Menu" USING btree ("IdClass");



CREATE INDEX idx_menuhistory_currentid ON "Menu_history" USING btree ("CurrentId");



CREATE INDEX idx_metadata_code ON "Metadata" USING btree ("Code");



CREATE INDEX idx_metadata_description ON "Metadata" USING btree ("Description");



CREATE INDEX idx_metadata_idclass ON "Metadata" USING btree ("IdClass");



CREATE INDEX idx_metadatahistory_currentid ON "Metadata_history" USING btree ("CurrentId");



CREATE INDEX idx_monitor_code ON "Monitor" USING btree ("Code");



CREATE INDEX idx_monitor_description ON "Monitor" USING btree ("Description");



CREATE INDEX idx_monitor_idclass ON "Monitor" USING btree ("IdClass");



CREATE INDEX idx_monitorhistory_currentid ON "Monitor_history" USING btree ("CurrentId");



CREATE INDEX idx_networkdevice_code ON "NetworkDevice" USING btree ("Code");



CREATE INDEX idx_networkdevice_description ON "NetworkDevice" USING btree ("Description");



CREATE INDEX idx_networkdevice_idclass ON "NetworkDevice" USING btree ("IdClass");



CREATE INDEX idx_networkdevicehistory_currentid ON "NetworkDevice_history" USING btree ("CurrentId");



CREATE INDEX idx_networkpoint_code ON "NetworkPoint" USING btree ("Code");



CREATE INDEX idx_networkpoint_description ON "NetworkPoint" USING btree ("Description");



CREATE INDEX idx_networkpoint_idclass ON "NetworkPoint" USING btree ("IdClass");



CREATE INDEX idx_networkpointhistory_currentid ON "NetworkPoint_history" USING btree ("CurrentId");



CREATE INDEX idx_notebook_code ON "Notebook" USING btree ("Code");



CREATE INDEX idx_notebook_description ON "Notebook" USING btree ("Description");



CREATE INDEX idx_notebook_idclass ON "Notebook" USING btree ("IdClass");



CREATE INDEX idx_notebookhistory_currentid ON "Notebook_history" USING btree ("CurrentId");



CREATE INDEX idx_office_code ON "Office" USING btree ("Code");



CREATE INDEX idx_office_description ON "Office" USING btree ("Description");



CREATE INDEX idx_office_idclass ON "Office" USING btree ("IdClass");



CREATE INDEX idx_officehistory_currentid ON "Office_history" USING btree ("CurrentId");



CREATE INDEX idx_patch_code ON "Patch" USING btree ("Code");



CREATE INDEX idx_patch_description ON "Patch" USING btree ("Description");



CREATE INDEX idx_patch_idclass ON "Patch" USING btree ("IdClass");



CREATE INDEX idx_patchhistory_currentid ON "Patch_history" USING btree ("CurrentId");



CREATE INDEX idx_pc_code ON "PC" USING btree ("Code");



CREATE INDEX idx_pc_description ON "PC" USING btree ("Description");



CREATE INDEX idx_pc_idclass ON "PC" USING btree ("IdClass");



CREATE INDEX idx_pchistory_currentid ON "PC_history" USING btree ("CurrentId");



CREATE INDEX idx_printer_code ON "Printer" USING btree ("Code");



CREATE INDEX idx_printer_description ON "Printer" USING btree ("Description");



CREATE INDEX idx_printer_idclass ON "Printer" USING btree ("IdClass");



CREATE INDEX idx_printerhistory_currentid ON "Printer_history" USING btree ("CurrentId");



CREATE INDEX idx_rack_code ON "Rack" USING btree ("Code");



CREATE INDEX idx_rack_description ON "Rack" USING btree ("Description");



CREATE INDEX idx_rack_idclass ON "Rack" USING btree ("IdClass");



CREATE INDEX idx_rackhistory_currentid ON "Rack_history" USING btree ("CurrentId");



CREATE INDEX idx_requestforchange_code ON "RequestForChange" USING btree ("Code");



CREATE INDEX idx_requestforchange_description ON "RequestForChange" USING btree ("Description");



CREATE INDEX idx_requestforchange_idclass ON "RequestForChange" USING btree ("IdClass");



CREATE INDEX idx_requestforchangehistory_currentid ON "RequestForChange_history" USING btree ("CurrentId");



CREATE INDEX idx_room_code ON "Room" USING btree ("Code");



CREATE INDEX idx_room_description ON "Room" USING btree ("Description");



CREATE INDEX idx_room_idclass ON "Room" USING btree ("IdClass");



CREATE INDEX idx_roomhistory_currentid ON "Room_history" USING btree ("CurrentId");



CREATE INDEX idx_scheduler_code ON "Scheduler" USING btree ("Code");



CREATE INDEX idx_scheduler_description ON "Scheduler" USING btree ("Description");



CREATE INDEX idx_scheduler_idclass ON "Scheduler" USING btree ("IdClass");



CREATE INDEX idx_schedulerhistory_currentid ON "Scheduler_history" USING btree ("CurrentId");



CREATE INDEX idx_server_code ON "Server" USING btree ("Code");



CREATE INDEX idx_server_description ON "Server" USING btree ("Description");



CREATE INDEX idx_server_idclass ON "Server" USING btree ("IdClass");



CREATE INDEX idx_serverhistory_currentid ON "Server_history" USING btree ("CurrentId");



CREATE INDEX idx_supplier_code ON "Supplier" USING btree ("Code");



CREATE INDEX idx_supplier_description ON "Supplier" USING btree ("Description");



CREATE INDEX idx_supplier_idclass ON "Supplier" USING btree ("IdClass");



CREATE INDEX idx_suppliercontact_code ON "SupplierContact" USING btree ("Code");



CREATE INDEX idx_suppliercontact_description ON "SupplierContact" USING btree ("Description");



CREATE INDEX idx_suppliercontact_idclass ON "SupplierContact" USING btree ("IdClass");



CREATE INDEX idx_suppliercontacthistory_currentid ON "SupplierContact_history" USING btree ("CurrentId");



CREATE INDEX idx_supplierhistory_currentid ON "Supplier_history" USING btree ("CurrentId");



CREATE INDEX idx_templates_begindate ON "_Templates" USING btree ("BeginDate");



CREATE INDEX idx_ups_code ON "UPS" USING btree ("Code");



CREATE INDEX idx_ups_description ON "UPS" USING btree ("Description");



CREATE INDEX idx_ups_idclass ON "UPS" USING btree ("IdClass");



CREATE INDEX idx_upshistory_currentid ON "UPS_history" USING btree ("CurrentId");



CREATE INDEX idx_workplace_code ON "Workplace" USING btree ("Code");



CREATE INDEX idx_workplace_description ON "Workplace" USING btree ("Description");



CREATE INDEX idx_workplace_idclass ON "Workplace" USING btree ("IdClass");



CREATE INDEX idx_workplacehistory_currentid ON "Workplace_history" USING btree ("CurrentId");



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"', '');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Assignee_fkey"
    BEFORE INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Assignee', '"Employee"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"', '');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Room_fkey"
    BEFORE INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"', '');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"', '');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_TechnicalReference_fkey"
    BEFORE INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('TechnicalReference', '"Employee"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"', '');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Asset_Workplace_fkey"
    BEFORE INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Workplace', '"Workplace"');



CREATE TRIGGER "Email_Activity_fkey"
    BEFORE INSERT OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Activity', '"Activity"', '');



CREATE TRIGGER "Employee_Office_fkey"
    BEFORE INSERT OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Office', '"Office"', '');



CREATE TRIGGER "Floor_Building_fkey"
    BEFORE INSERT OR UPDATE ON "Floor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Building', '"Building"', '');



CREATE TRIGGER "Invoice_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "Invoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"', '');



CREATE TRIGGER "NetworkPoint_Room_fkey"
    BEFORE INSERT OR UPDATE ON "NetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"', '');



CREATE TRIGGER "Office_Supervisor_fkey"
    BEFORE INSERT OR UPDATE ON "Office"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supervisor', '"Employee"', '');



CREATE TRIGGER "RequestForChange_Requester_fkey"
    BEFORE INSERT OR UPDATE ON "RequestForChange"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Requester', '"Employee"', '');



CREATE TRIGGER "Room_Floor_fkey"
    BEFORE INSERT OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Floor', '"Floor"', '');



CREATE TRIGGER "Room_Office_fkey"
    BEFORE INSERT OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Office', '"Office"', '');



CREATE TRIGGER "SupplierContact_Supplier_fkey"
    BEFORE INSERT OR UPDATE ON "SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Supplier', '"Supplier"', '');



CREATE TRIGGER "Workplace_Room_fkey"
    BEFORE INSERT OR UPDATE ON "Workplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_fk('Room', '"Room"', '');



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Menu"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Metadata"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Scheduler"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Patch"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Office"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Building"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Floor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Invoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Supplier"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Workplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "NetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_CascadeDeleteOnRelations"
    AFTER UPDATE ON "RequestForChange"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_cascade_delete_on_relations();



CREATE TRIGGER "_Constr_Asset_Assignee"
    BEFORE DELETE OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Assignee');



CREATE TRIGGER "_Constr_Asset_Room"
    BEFORE DELETE OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Room');



CREATE TRIGGER "_Constr_Asset_Supplier"
    BEFORE DELETE OR UPDATE ON "Supplier"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Supplier');



CREATE TRIGGER "_Constr_Asset_TechnicalReference"
    BEFORE DELETE OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'TechnicalReference');



CREATE TRIGGER "_Constr_Asset_Workplace"
    BEFORE DELETE OR UPDATE ON "Workplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Asset"', 'Workplace');



CREATE TRIGGER "_Constr_Email_Activity"
    BEFORE DELETE OR UPDATE ON "Activity"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Activity');



CREATE TRIGGER "_Constr_Email_Activity"
    BEFORE DELETE OR UPDATE ON "RequestForChange"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Email"', 'Activity');



CREATE TRIGGER "_Constr_Employee_Office"
    BEFORE DELETE OR UPDATE ON "Office"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Employee"', 'Office');



CREATE TRIGGER "_Constr_Floor_Building"
    BEFORE DELETE OR UPDATE ON "Building"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Floor"', 'Building');



CREATE TRIGGER "_Constr_Invoice_Supplier"
    BEFORE DELETE OR UPDATE ON "Supplier"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Invoice"', 'Supplier');



CREATE TRIGGER "_Constr_NetworkPoint_Room"
    BEFORE DELETE OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"NetworkPoint"', 'Room');



CREATE TRIGGER "_Constr_Office_Supervisor"
    BEFORE DELETE OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Office"', 'Supervisor');



CREATE TRIGGER "_Constr_RequestForChange_Requester"
    BEFORE DELETE OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"RequestForChange"', 'Requester');



CREATE TRIGGER "_Constr_Room_Floor"
    BEFORE DELETE OR UPDATE ON "Floor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Room"', 'Floor');



CREATE TRIGGER "_Constr_Room_Office"
    BEFORE DELETE OR UPDATE ON "Office"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Room"', 'Office');



CREATE TRIGGER "_Constr_SupplierContact_Supplier"
    BEFORE DELETE OR UPDATE ON "Supplier"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"SupplierContact"', 'Supplier');



CREATE TRIGGER "_Constr_Workplace_Room"
    BEFORE DELETE OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_restrict('"Workplace"', 'Room');



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Menu"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_ActivityEmail"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Metadata"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Scheduler"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_UserRole"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Patch"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Office"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_Members"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Building"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Floor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Invoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Supplier"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Workplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_BuildingFloor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_SupplierInvoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_FloorRoom"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_OfficeRoom"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_RoomWorkplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_WorkplaceComposition"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_SupplierAsset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_AssetAssignee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_AssetReference"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_RoomAsset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "NetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_RoomNetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_NetworkDeviceConnection"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "RequestForChange"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_card_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_RFCChangeManager"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_RFCExecutor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_RFCRequester"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_CreateHistoryRow"
    AFTER DELETE OR UPDATE ON "Map_Supervisor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_create_relation_history_row();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Menu"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_ActivityEmail"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Metadata"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Scheduler"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_UserRole"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Patch"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Office"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_Members"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Building"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Floor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Invoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Supplier"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Workplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_BuildingFloor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_SupplierInvoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_FloorRoom"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_OfficeRoom"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_RoomWorkplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_WorkplaceComposition"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_SupplierAsset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_AssetAssignee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_AssetReference"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_RoomAsset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "NetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_RoomNetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_NetworkDeviceConnection"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "_Templates"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "_Dashboards"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check_simple();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "RequestForChange"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_RFCChangeManager"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_RFCExecutor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_RFCRequester"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_SanityCheck"
    BEFORE INSERT OR DELETE OR UPDATE ON "Map_Supervisor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_sanity_check();



CREATE TRIGGER "_UpdRef_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Map_AssetAssignee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Assignee', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_Room"
    AFTER INSERT OR UPDATE ON "Map_RoomAsset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Room', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Map_SupplierAsset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Supplier', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Map_AssetReference"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('TechnicalReference', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Map_WorkplaceComposition"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Workplace', '"Asset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Email_Activity"
    AFTER INSERT OR UPDATE ON "Map_ActivityEmail"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Activity', '"Email"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Employee_Office"
    AFTER INSERT OR UPDATE ON "Map_Members"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Office', '"Employee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Floor_Building"
    AFTER INSERT OR UPDATE ON "Map_BuildingFloor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Building', '"Floor"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Invoice_Supplier"
    AFTER INSERT OR UPDATE ON "Map_SupplierInvoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Supplier', '"Invoice"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_NetworkPoint_Room"
    AFTER INSERT OR UPDATE ON "Map_RoomNetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Room', '"NetworkPoint"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Office_Supervisor"
    AFTER INSERT OR UPDATE ON "Map_Supervisor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Supervisor', '"Office"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_RequestForChange_Requester"
    AFTER INSERT OR UPDATE ON "Map_RFCRequester"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Requester', '"RequestForChange"', 'IdObj1', 'IdObj2');



CREATE TRIGGER "_UpdRef_Room_Floor"
    AFTER INSERT OR UPDATE ON "Map_FloorRoom"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Floor', '"Room"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Room_Office"
    AFTER INSERT OR UPDATE ON "Map_OfficeRoom"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Office', '"Room"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_SupplierContact_Supplier"
    AFTER INSERT OR UPDATE ON "Map_SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Supplier', '"SupplierContact"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRef_Workplace_Room"
    AFTER INSERT OR UPDATE ON "Map_RoomWorkplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_reference('Room', '"Workplace"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Assignee"
    AFTER INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Assignee', '"Map_AssetAssignee"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Room"
    AFTER INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Supplier"
    AFTER INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierAsset"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_TechnicalReference"
    AFTER INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('TechnicalReference', '"Map_AssetReference"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Asset"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Rack"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Computer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "PC"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Server"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Notebook"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Monitor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "Printer"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "UPS"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "License"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Asset_Workplace"
    AFTER INSERT OR UPDATE ON "NetworkDevice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Workplace', '"Map_WorkplaceComposition"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Email_Activity"
    AFTER INSERT OR UPDATE ON "Email"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Activity', '"Map_ActivityEmail"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Employee_Office"
    AFTER INSERT OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Office', '"Map_Members"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Floor_Building"
    AFTER INSERT OR UPDATE ON "Floor"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Building', '"Map_BuildingFloor"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Invoice_Supplier"
    AFTER INSERT OR UPDATE ON "Invoice"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierInvoice"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_NetworkPoint_Room"
    AFTER INSERT OR UPDATE ON "NetworkPoint"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomNetworkPoint"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Office_Supervisor"
    AFTER INSERT OR UPDATE ON "Office"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supervisor', '"Map_Supervisor"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_RequestForChange_Requester"
    AFTER INSERT OR UPDATE ON "RequestForChange"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Requester', '"Map_RFCRequester"', 'IdObj1', 'IdObj2');



CREATE TRIGGER "_UpdRel_Room_Floor"
    AFTER INSERT OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Floor', '"Map_FloorRoom"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Room_Office"
    AFTER INSERT OR UPDATE ON "Room"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Office', '"Map_OfficeRoom"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_SupplierContact_Supplier"
    AFTER INSERT OR UPDATE ON "SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Supplier', '"Map_SupplierContact"', 'IdObj2', 'IdObj1');



CREATE TRIGGER "_UpdRel_Workplace_Room"
    AFTER INSERT OR UPDATE ON "Workplace"
    FOR EACH ROW
    EXECUTE PROCEDURE _cm_trigger_update_relation('Room', '"Map_RoomWorkplace"', 'IdObj2', 'IdObj1');



CREATE TRIGGER set_data_employee
    BEFORE INSERT OR UPDATE ON "Employee"
    FOR EACH ROW
    EXECUTE PROCEDURE set_data_employee();



CREATE TRIGGER set_data_suppliercontact
    BEFORE INSERT OR UPDATE ON "SupplierContact"
    FOR EACH ROW
    EXECUTE PROCEDURE set_data_suppliercontact();



ALTER TABLE ONLY "Building_history"
    ADD CONSTRAINT "Building_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Building"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Email_history"
    ADD CONSTRAINT "Email_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Email"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Employee_history"
    ADD CONSTRAINT "Employee_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Employee"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Floor_history"
    ADD CONSTRAINT "Floor_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Floor"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Invoice_history"
    ADD CONSTRAINT "Invoice_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Invoice"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "License_history"
    ADD CONSTRAINT "License_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "License"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Menu_history"
    ADD CONSTRAINT "Menu_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Menu"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Metadata_history"
    ADD CONSTRAINT "Metadata_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Metadata"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Monitor_history"
    ADD CONSTRAINT "Monitor_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Monitor"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "NetworkDevice_history"
    ADD CONSTRAINT "NetworkDevice_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "NetworkDevice"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "NetworkPoint_history"
    ADD CONSTRAINT "NetworkPoint_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "NetworkPoint"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Notebook_history"
    ADD CONSTRAINT "Notebook_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Notebook"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Office_history"
    ADD CONSTRAINT "Office_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Office"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "PC_history"
    ADD CONSTRAINT "PC_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "PC"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Patch_history"
    ADD CONSTRAINT "Patch_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Patch"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Printer_history"
    ADD CONSTRAINT "Printer_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Printer"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Rack_history"
    ADD CONSTRAINT "Rack_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Rack"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "RequestForChange_history"
    ADD CONSTRAINT "RequestForChange_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "RequestForChange"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Room_history"
    ADD CONSTRAINT "Room_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Room"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Scheduler_history"
    ADD CONSTRAINT "Scheduler_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Scheduler"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Server_history"
    ADD CONSTRAINT "Server_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Server"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "SupplierContact_history"
    ADD CONSTRAINT "SupplierContact_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "SupplierContact"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Supplier_history"
    ADD CONSTRAINT "Supplier_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Supplier"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "UPS_history"
    ADD CONSTRAINT "UPS_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "UPS"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;



ALTER TABLE ONLY "Workplace_history"
    ADD CONSTRAINT "Workplace_history_CurrentId_fkey" FOREIGN KEY ("CurrentId") REFERENCES "Workplace"("Id") ON UPDATE RESTRICT ON DELETE SET NULL;




