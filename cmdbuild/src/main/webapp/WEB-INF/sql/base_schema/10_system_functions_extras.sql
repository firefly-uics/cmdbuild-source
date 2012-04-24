--
-- Function objects
--

CREATE OR REPLACE FUNCTION _cm_function_list(
		OUT function_name text,
		OUT arg_io char[],
		OUT arg_names text[],
		OUT arg_types text[],
		OUT returns_set boolean
	) RETURNS SETOF record AS $$
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
		returns_set := R.proretset;
		IF R.proargmodes IS NULL
		THEN
			-- create arrays from other columns
			arg_io := ARRAY['o'::char];
			arg_types := ARRAY[_cm_get_sqltype_string(R.prorettype, NULL)];
			arg_names := ARRAY[''::text];
			FOR i IN SELECT generate_series(1, array_upper(R.proargtypes,1)) LOOP
				arg_io := 'i'::char || arg_io;
				arg_types := _cm_get_sqltype_string(R.proargtypes[i], NULL) || arg_types;
				arg_names := COALESCE(R.proargnames[i], ''::text) || arg_names;
			END LOOP;
		ELSE
			-- just normalize existing columns
			arg_io := R.proargmodes;
			arg_types := '{}'::text[];
			arg_names := R.proargnames;
			FOR i IN SELECT generate_series(1, array_upper(arg_io,1)) LOOP
				-- normalize table output
				IF arg_io[i] = 't' THEN
					arg_io[i] := 'o';
				END IF;
				arg_types := _cm_get_sqltype_string(R.proallargtypes[i], NULL) || arg_types;
			END LOOP;
		END IF;
		RETURN NEXT;
	END LOOP;

	RETURN;
END
$$ LANGUAGE PLPGSQL STABLE;
