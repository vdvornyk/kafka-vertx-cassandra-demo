CREATE TABLE IF NOT EXISTS testCQLTable (id uuid, value varchar, PRIMARY KEY(id));
INSERT INTO testCQLTable(id, value) values(1690e8da-5bf8-49e8-9583-4dff8a570737,'Cql loaded string');  -- on the end of the line

INSERT INTO testCQLTable(id, value) values(1690e8da-5bf8-49e8-9583-4dff8a570738,'BLA2'); // with " a single ' quote
INSERT INTO testCQLTable(id, value) values(1690e8da-5bf8-49e8-9583-4dff8a570739,'BLA1');
