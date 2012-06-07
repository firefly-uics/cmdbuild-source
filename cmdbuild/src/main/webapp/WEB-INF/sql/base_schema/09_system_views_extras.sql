
CREATE OR REPLACE FUNCTION _cm_legacy_get_menu_type(boolean, boolean, boolean, boolean)
  RETURNS varchar AS
$BODY$
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
$BODY$
  LANGUAGE PLPGSQL VOLATILE;

CREATE OR REPLACE FUNCTION _cm_legacy_get_menu_code(boolean, boolean, boolean, boolean)
  RETURNS varchar AS
$BODY$
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
$BODY$
  LANGUAGE PLPGSQL VOLATILE;

/******************************
 * NO! NO! NO! IT CAN'T WORK! *
 * There's no MANAGER anymore *
 ******************************/

CREATE OR REPLACE FUNCTION _cm_legacy_class_is_process(text) RETURNS boolean AS $$
	SELECT (_cm_legacy_read_comment($1, 'MANAGER') = 'activity');
$$ LANGUAGE SQL;


CREATE OR REPLACE VIEW system_availablemenuitems AS 
        (        (        ( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Code", _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar) AS "Description", 
                                CASE
                                    WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                                       FROM "Menu" "Menu1"
                                      WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                                    ELSE NULL::regclass
                                END AS "IdElementClass", 0 AS "IdElementObj", "Role"."Id" AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Type"
                           FROM system_classcatalog
                      JOIN "Role" ON "Role"."Status" = 'A'::bpchar
                 LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
                WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
                         FROM "Menu"
                        WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu"."IdGroup")) AND (_cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'STATUS'::varchar)::text = 'active'::text
                ORDER BY system_classcatalog.classid::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false), _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar), 
                      CASE
                          WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                             FROM "Menu" "Menu1"
                            WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND "Role"."Id" = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                          ELSE NULL::regclass
                      END, 0::integer, "Role"."Id", _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false))
                UNION 
                         SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", "AllReport"."RoleId" AS "IdGroup", "AllReport"."Type"
                           FROM ( SELECT _cm_legacy_get_menu_type(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type", "Role"."Id" AS "RoleId"
                                   FROM generate_series(1, 6) i(i), "Report"
                              JOIN "Role" ON "Role"."Status" = 'A'::bpchar
                             WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
                                   CASE
                                       WHEN "Report"."Type"::text = 'normal'::text THEN 1
                                       WHEN "Report"."Type"::text = 'custom'::text THEN 2
                                       WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
                                       ELSE 0
                                   END) "AllReport"
                      LEFT JOIN "Menu" ON "AllReport"."IdElementObj" = "Menu"."IdElementObj" AND "Menu"."Status" = 'A'::bpchar AND "AllReport"."RoleId" = "Menu"."IdGroup" AND "AllReport"."Code" = "Menu"."Code"::text
                     WHERE "Menu"."Code" IS NULL)
        UNION 
                ( SELECT DISTINCT system_classcatalog.classid::regclass AS "IdClass", _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Code", _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar) AS "Description", 
                        CASE
                            WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                               FROM "Menu" "Menu1"
                              WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                            ELSE NULL::regclass
                        END AS "IdElementClass", 0 AS "IdElementObj", 0 AS "IdGroup", _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false) AS "Type"
                   FROM system_classcatalog
              LEFT JOIN system_treecatalog ON system_treecatalog.childid = system_classcatalog.classid
             WHERE NOT (system_classcatalog.classid IN ( SELECT "Menu"."IdElementClass"::integer AS "IdElementClass"
                      FROM "Menu"
                     WHERE ("Menu"."Code"::text <> ALL (ARRAY['folder'::text, 'report'::text, 'view'::text, 'Folder'::text, 'Report'::text, 'View'::text])) AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup")) AND (_cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'STATUS'::varchar)::text = 'active'::text
             ORDER BY system_classcatalog.classid::regclass, _cm_legacy_get_menu_type(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false), _cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'DESCR'::varchar), 
                   CASE
                       WHEN (_cm_legacy_read_comment(system_treecatalog.childcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text])) AND NOT (system_treecatalog.childid::regclass::oid IN ( SELECT "Menu1"."IdElementClass"::integer AS "IdElementClass"
                          FROM "Menu" "Menu1"
                         WHERE ("Menu1"."Code"::text <> ALL (ARRAY['folder'::varchar::text, 'report'::varchar::text, 'view'::varchar::text, 'Folder'::varchar::text, 'Report'::varchar::text, 'View'::varchar::text])) AND "Menu1"."Status" = 'A'::bpchar AND 0 = "Menu1"."IdGroup")) THEN system_treecatalog.childid::regclass
                       ELSE NULL::regclass
                   END, 0::integer, _cm_legacy_get_menu_code(_cm_is_superclass_comment(system_treecatalog.childcomment::varchar), _cm_legacy_class_is_process(system_classcatalog.classcomment::varchar), false, false)))
UNION 
         SELECT "AllReport"."IdClass", "AllReport"."Code", "AllReport"."Description", "AllReport"."IdClass" AS "IdElementClass", "AllReport"."IdElementObj", 0 AS "IdGroup", "AllReport"."Type"
           FROM ( SELECT _cm_legacy_get_menu_type(false, false, true, false)::text || (ARRAY['pdf'::text, 'csv'::text, 'pdf'::text, 'csv'::text, 'xml'::text, 'odt'::text])[i.i] AS "Code", "Report"."Id" AS "IdElementObj", "Report"."IdClass", "Report"."Code" AS "Description", "Report"."Type"
                   FROM generate_series(1, 6) i(i), "Report"
                  WHERE "Report"."Status"::text = 'A'::text AND ((i.i + 1) / 2) = 
                        CASE
                            WHEN "Report"."Type"::text = 'normal'::text THEN 1
                            WHEN "Report"."Type"::text = 'custom'::text THEN 2
                            WHEN "Report"."Type"::text = 'openoffice'::text THEN 3
                            ELSE 0
                        END) "AllReport"
      LEFT JOIN "Menu" ON "AllReport"."IdElementObj" = "Menu"."IdElementObj" AND "Menu"."Status" = 'A'::bpchar AND 0 = "Menu"."IdGroup" AND "AllReport"."Code" = "Menu"."Code"::text
     WHERE "Menu"."Code" IS NULL;


CREATE OR REPLACE VIEW system_privilegescatalog AS 
 SELECT DISTINCT ON (permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass") permission."Id", permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."BeginDate", permission."Notes", permission."IdRole", permission."IdGrantedClass", permission."Mode"
   FROM (         SELECT "Grant"."Id", "Grant"."IdClass", "Grant"."Code", "Grant"."Description", "Grant"."Status", "Grant"."User", "Grant"."BeginDate", "Grant"."Notes", "Grant"."IdRole", "Grant"."IdGrantedClass", "Grant"."Mode"
                   FROM "Grant"
        UNION 
                 SELECT (-1), '"Grant"', '', '', 'A', 'admin', now() AS now, NULL::unknown AS unknown, "Role"."Id", system_classcatalog.classid::regclass AS classid, '-'
                   FROM system_classcatalog, "Role"
                  WHERE system_classcatalog.classid::regclass::oid <> '"Class"'::regclass::oid AND NOT ("Role"."Id"::text || system_classcatalog.classid::integer::text IN ( SELECT "Grant"."IdRole"::text || "Grant"."IdGrantedClass"::oid::integer::text
                           FROM "Grant"))) permission
   JOIN system_classcatalog ON permission."IdGrantedClass"::oid = system_classcatalog.classid AND (_cm_legacy_read_comment(system_classcatalog.classcomment::varchar, 'MODE'::varchar)::text = ANY (ARRAY['write'::varchar::text, 'read'::varchar::text]))
  ORDER BY permission."IdClass", permission."Code", permission."Description", permission."Status", permission."User", permission."Notes", permission."IdRole", permission."IdGrantedClass";
