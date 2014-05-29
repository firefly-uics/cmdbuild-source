-- Generate UUIDs for Menu elements

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
DECLARE
	menuItem record;
BEGIN
	RAISE INFO 'check consistency between code and type';
	FOR menuItem IN (
		SELECT "Id","Code","Type"
		FROM "Menu"
		WHERE "Status"='A' AND "Code" <> "Type"
		
	) LOOP
		RAISE EXCEPTION 'Inconsistency between Code ''%'' and Type ''%'' for row with Id ''%''',menuItem."Code",menuItem."Type",menuItem."Id";
	END LOOP;
	
	RAISE INFO 'clean Code column';
	RAISE INFO 'disable triggers';
	ALTER TABLE "Menu" DISABLE TRIGGER ALL;
	UPDATE "Menu" 
	SET "Code" = null;
	RAISE INFO 'enable triggers';
	ALTER TABLE "Menu" ENABLE TRIGGER ALL;

	RAISE INFO 'verify that all the values for the BeginDate are distinct';

	FOR menuItem IN (SELECT COUNT(*) AS count,"BeginDate" AS beginDate
				FROM "Menu"
				WHERE "Status"='A' 
				GROUP BY "BeginDate"
	)LOOP
		IF menuItem.count <> 1 THEN
			RAISE EXCEPTION 'More than one row with BeginDate ''%''',menuItem.beginDate;
		END IF;
	END LOOP;
		
	RAISE INFO 'set the uuids for active rows';
		
	UPDATE "Menu"
	SET "Code"= uuid_in(md5("BeginDate"::text)::cstring)
	WHERE "Status"='A';
END;
$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION IF EXISTS apply_patch();