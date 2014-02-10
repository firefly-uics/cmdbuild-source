--
-- Stored-procedure called by the BIM features.
--

CREATE OR REPLACE FUNCTION _cm_all_globalid_map(OUT globalid character varying, OUT id integer, OUT card_description character varying, OUT idclass integer)
  RETURNS SETOF record AS
$BODY$
DECLARE
	query varchar;
	table_name varchar;
	tables CURSOR FOR SELECT tablename FROM pg_tables WHERE schemaname = 'bim' ORDER BY tablename;
	
BEGIN
	query='';
	FOR table_record IN tables LOOP
		query= query || ' SELECT b."GlobalId" AS gloabalid, b."Master" as id , p."Description" AS card_description, p."IdClass"::integer as idclass FROM bim."' || table_record.tablename || '" AS b JOIN public."' ||  table_record.tablename || '" AS p ON b."Master"=p."Id" WHERE p."Status"=''A'' UNION ALL';
	END LOOP;

	SELECT substring(query from 0 for LENGTH(query)-9) INTO query;
	RAISE NOTICE 'execute query : %', query;
	RETURN QUERY EXECUTE(query);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _cm_all_globalid_map() IS 'TYPE: function';


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
	RAISE NOTICE 'execute query : %', query;
	EXECUTE(query) INTO id,table_name;
	RAISE NOTICE 'id = % table_name = %',id,table_name;

	-- get the classid of the corresponding class in 'public' schema
	IF(table_name is not null) THEN
		SELECT substring(table_name from 5) INTO table_name;
		query = 'SELECT ''public.' || table_name::text || '''::regclass::oid'; 
		RAISE NOTICE 'execute %', query;
		EXECUTE(query) INTO classid;
	END IF;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _cm_get_id_from_globalid(character varying) IS 'TYPE: function';


CREATE OR REPLACE FUNCTION _opm_find_the_building(IN cardid integer, OUT buildingid integer)
  RETURNS integer AS
$BODY$
BEGIN
	RAISE NOTICE 'Function _opm_find_the_building with cardid % ', cardid;

	SELECT "IsInBuilding" INTO buildingid
	FROM "InventoryItem"
	WHERE "Id"=cardid;

	IF(buildingid IS NULL) THEN
		SELECT "Building" INTO buildingid
		FROM "Room"
		WHERE "Id"=cardid;
	END IF;

	IF(buildingid IS NULL) THEN
		SELECT "Building" INTO buildingid
		FROM "Floor"
		WHERE "Id"=cardid;
	END IF;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _opm_find_the_building(integer) IS 'TYPE: function';


CREATE OR REPLACE FUNCTION cm_attribute_exists(IN schemaname text, IN tablename text, IN attributename text, OUT attribute_exists boolean)
  RETURNS boolean AS
$BODY$
DECLARE
	attribute_name varchar;
BEGIN
	SELECT attname into attribute_name
	FROM pg_attribute 
	WHERE 	attrelid = (SELECT oid FROM pg_class WHERE relname = tablename AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname=schemaname)) AND
		attname = attributename;

	IF(attribute_name is not null) THEN
		attribute_exists = true;
	ELSE
		attribute_exists = false;
	END IF;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION cm_attribute_exists(text, text, text) IS 'TYPE: function';


