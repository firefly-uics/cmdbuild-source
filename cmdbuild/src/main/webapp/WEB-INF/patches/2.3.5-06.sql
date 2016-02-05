-- Updates the table "_TaskParameter" changing the key of a parameter

DROP FUNCTION IF EXISTS apply_patch();
CREATE OR REPLACE FUNCTION apply_patch() RETURNS void AS $$
BEGIN
	UPDATE "_TaskParameter"
		SET "Key" = regexp_replace("Key", 'filter.(\w+).regex', 'filter.regex.\1', 'g')
		WHERE "Key" like 'filter.%.regex' AND "Id" IN (
			SELECT  p."Id"
				FROM "_TaskParameter" AS p
				JOIN "_Task" AS t ON t."Id" = p."Owner" and t."Type" = 'emailService'
				where p."Status" = 'A'
			);
END;
$$ LANGUAGE PLPGSQL;

SELECT apply_patch();

DROP FUNCTION apply_patch();