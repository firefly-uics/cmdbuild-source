INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.running','FlowStatus', 1, 'Running', true, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'open.not_running.suspended','FlowStatus', 2, 'Suspended', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.completed','FlowStatus', 3, 'Completed', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.terminated','FlowStatus', 4, 'Terminated', false, 'A');
INSERT INTO "LookUp" ("IdClass","Code", "Type", "Number", "Description", "IsDefault", "Status") VALUES ('"LookUp"'::regclass,'closed.aborted','FlowStatus', 5, 'Aborted', false, 'A');
