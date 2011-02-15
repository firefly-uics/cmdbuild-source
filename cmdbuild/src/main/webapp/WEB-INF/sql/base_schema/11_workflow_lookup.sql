INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.running','FlowStatus', 1, 'Avviato', true, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.not_running.suspended','FlowStatus', 2, 'Sospeso', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.completed','FlowStatus', 3, 'Completato', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.terminated','FlowStatus', 4, 'Terminato', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.aborted','FlowStatus', 5, 'Interrotto', false, 'A');
