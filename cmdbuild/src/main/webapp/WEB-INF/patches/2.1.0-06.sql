-- Create _Widget table and import widgets from the Metadata table

DROP TABLE IF EXISTS "_Widget" CASCADE;

SELECT cm_create_class('_Widget', 'Class', 'MODE: reserved|TYPE: class|DESCR: Widget|SUPERCLASS: false|STATUS: active');

CREATE OR REPLACE FUNCTION widget_table_creation_and_import_from_metadata() RETURNS VOID AS $$
DECLARE
	targetClass varchar;
	widgets text;
	metadataToRemoveId integer;
	singleWidget text;
	
BEGIN
	FOR metadataToRemoveId, targetClass, widgets IN
			SELECT "Id" as id, "Code" as code, "Notes" as notes
				FROM "Metadata"
				WHERE "Metadata"."Status" = 'A' AND "Description" = 'system.widgets'
	LOOP

	RAISE INFO 'widgets: %, targetClass: %', widgets, targetClass; 

		FOR singleWidget IN 	 
			SELECT * FROM regexp_split_to_table(
			       regexp_replace(
				       regexp_replace(widgets, E'(^\\[|\\]$)', '', 'g'),
				       E'},{',
				       '}|{',
				       'g'),
			       E'\\|') AS singleWidget
		LOOP

		RAISE INFO 'single widget to insert: %', singleWidget;	

		INSERT INTO "_Widget" ("Code", "Description") VALUES(targetClass, singleWidget);

		END LOOP;

		RAISE INFO 'deleting widgets from Metadata table...';

		PERFORM cm_delete_card(metadataToRemoveId, _cm_table_id('Metadata'));

	END LOOP;


END
$$ LANGUAGE PLPGSQL;

SELECT widget_table_creation_and_import_from_metadata();

DROP FUNCTION widget_table_creation_and_import_from_metadata();