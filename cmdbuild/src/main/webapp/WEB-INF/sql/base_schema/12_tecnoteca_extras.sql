--
-- Put here what the PM decides that should be added to the empty database
--

INSERT INTO "LookUp" ("IdClass","Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'AlfrescoCategory', 1, 'Document', true, 'A');
INSERT INTO "LookUp" ("IdClass","Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'AlfrescoCategory', 2, 'Image', false, 'A');
