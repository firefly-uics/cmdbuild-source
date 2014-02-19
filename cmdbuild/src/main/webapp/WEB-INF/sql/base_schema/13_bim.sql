--
-- Stored-procedure called by the BIM features.
--
CREATE OR REPLACE FUNCTION _bim_carddata_from_globalid(IN globalid varchar, OUT "Id" integer, OUT "IdClass" integer, OUT "Description" varchar, OUT "ClassName" varchar)
  RETURNS record AS
$BODY$
DECLARE
	query varchar;
	table_name varchar;
	tables CURSOR FOR SELECT tablename FROM pg_tables WHERE schemaname = 'bim' ORDER BY tablename;
	
BEGIN
	query='';
	FOR table_record IN tables LOOP
		query= query || '
		SELECT	b."Master" as "Id" , 
			p."Description" AS "Description", 
			p."IdClass"::integer as "IdClass" ,
			p."IdClass" as "ClassName"
		FROM bim."' || table_record.tablename || '" AS b 
			JOIN public."' ||  table_record.tablename || '" AS p 
			ON b."Master"=p."Id" 
		WHERE p."Status"=''A'' AND b."GlobalId" = ''' || globalid || ''' UNION ALL';
	END LOOP;

	SELECT substring(query from 0 for LENGTH(query)-9) INTO query;
	RAISE NOTICE 'execute query : %', query;
	EXECUTE(query) INTO "Id","Description","IdClass","ClassName";
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _bim_carddata_from_globalid(character varying) IS 'TYPE: function';



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


CREATE OR REPLACE FUNCTION _bim_data_for_export(IN id integer, IN classname varchar, OUT code varchar, OUT description varchar, OUT globalid varchar, OUT x varchar, OUT y varchar, OUT z varchar)
  RETURNS record AS
$BODY$
DECLARE
	query varchar;
	myrecord record;
BEGIN	
	query = '
	SELECT master."Code", master."Description", bimclass."GlobalId", st_x(bimclass."Position"),st_y(bimclass."Position"),st_z(bimclass."Position")
	FROM "' || classname || '" AS master JOIN bim."' || classname || '" AS bimclass ON ' || ' bimclass."Master"=master."Id" WHERE master."Id" = ' || id || ' AND master."Status"=''A''';

	RAISE NOTICE '%',query;

	EXECUTE(query) INTO code, description, globalid, x, y, z;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
COMMENT ON FUNCTION _bim_data_for_export(integer, varchar) IS 'TYPE: function';(integer, varchar) IS 'TYPE: function';


CREATE OR REPLACE FUNCTION _bim_set_coordinates(IN globalid varchar, IN classname character varying, IN x character varying, IN y character varying, IN z character varying)
  RETURNS void AS
$BODY$
DECLARE
	query varchar;
	myrecord record;
BEGIN	

	query = 
	' UPDATE bim.' || classname || --
	' SET "Position"= ST_GeomFromText(''POINT(' || x || ' ' || y || ' ' || z || ')'')' || --
	' WHERE "GlobalId"= ''' || globalid || '''';
			
	RAISE NOTICE '%',query;

	--EXECUTE(query);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION _bim_set_coordinates(varchar, varchar, varchar, varchar, varchar)
  OWNER TO postgres;
COMMENT ON FUNCTION _bim_set_coordinates(varchar, varchar, varchar, varchar, varchar) IS 'TYPE: function';



