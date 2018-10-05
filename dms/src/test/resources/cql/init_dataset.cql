CREATE TABLE IF NOT EXISTS incomming_message (id uuid, message varchar, PRIMARY KEY(id));
CREATE TABLE IF NOT EXISTS pseudonym_mapping (id uuid, transaction_id varchar, pseudonym varchar, PRIMARY KEY(id));
