--
-- Put here what the PM decides that should be added to the empty database
--


-- 
-- Predefined DMS categories
-- 

INSERT INTO "LookUp" ("IdClass","Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'AlfrescoCategory', 1, 'Document', true, 'A');
INSERT INTO "LookUp" ("IdClass","Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'AlfrescoCategory', 2, 'Image', false, 'A');


-- 
-- Custom Functions
-- 

CREATE OR REPLACE FUNCTION _cmf_class_description(cid oid) RETURNS character varying AS $$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'DESCR');
$$ LANGUAGE sql STABLE;


CREATE OR REPLACE FUNCTION _cmf_is_displayable(cid oid) RETURNS boolean AS $$
    SELECT _cm_read_comment(_cm_comment_for_table_id($1), 'MODE') IN
('write','read','baseclass');
$$ LANGUAGE sql STABLE;


CREATE OR REPLACE FUNCTION cmf_active_cards_for_class(IN "ClassName" character varying, OUT "Class" character varying, OUT "Number" integer)
  RETURNS SETOF record AS $$
BEGIN
    RETURN QUERY EXECUTE
        'SELECT _cmf_class_description("IdClass") AS "ClassDescription", COUNT(*)::integer AS "CardCount"' ||
        '    FROM ' || quote_ident($1) ||
        '    WHERE' ||
        '        "Status" = ' || quote_literal('A') ||
        '        AND _cmf_is_displayable("IdClass")' ||
        '        AND "IdClass" not IN (SELECT _cm_subtables_and_itself(_cm_table_id(' || quote_literal('Activity') || ')))'
        '    GROUP BY "IdClass"' ||
        '    ORDER BY "ClassDescription"';
END
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION cmf_active_cards_for_class(character varying) IS 'TYPE: function';


CREATE OR REPLACE FUNCTION cmf_count_active_cards(IN "ClassName" character varying, OUT "Count" integer)
  RETURNS integer AS $$
BEGIN
    EXECUTE 'SELECT count(*) FROM '|| quote_ident("ClassName") ||' WHERE "Status" like ''A''' INTO "Count";
END
$$ LANGUAGE plpgsql;
COMMENT ON FUNCTION cmf_count_active_cards(character varying) IS 'TYPE: function';


-- TO MOVE SOMEWHERE ELSE !!!!!!

CREATE OR REPLACE FUNCTION _cm_get_id_from_globalid(IN globalid character varying, OUT id integer, OUT classid integer)
  RETURNS record AS
$BODY$
DECLARE
	query varchar;
	table_name varchar;
	tables CURSOR FOR SELECT tablename FROM pg_tables WHERE schemaname = 'bim' ORDER BY tablename;
	
BEGIN
	query='';
	FOR table_record IN tables LOOP
		query= query || ' SELECT "Master","IdClass"::varchar FROM bim."' || table_record.tablename || '" WHERE "GlobalId" = ''' || globalid || ''' UNION ALL';
	END LOOP;

	SELECT substring(query from 0 for LENGTH(query)-9) INTO query;
	RAISE NOTICE '%', query;
	EXECUTE(query) INTO id,table_name;
	RAISE NOTICE '% %',id,table_name;

	-- get the regclass of the corresponding class in 'public' schema
	SELECT substring(table_name from 5) INTO table_name;
	query = 'SELECT ''public.' || table_name::text || '''::regclass::oid'; 
	RAISE NOTICE '%', query;
	EXECUTE(query) INTO classid;

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _cm_get_id_from_globalid(IN character varying, OUT integer, OUT integer) IS 'TYPE: function';

