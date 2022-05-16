CREATE TABLE IF NOT EXISTS nodes
(
    id       SERIAL PRIMARY KEY,
    nodeName TEXT UNIQUE,
    username TEXT
);

CREATE TABLE IF NOT EXISTS statuses
(
    id         SERIAL PRIMARY KEY,
    node_index INT REFERENCES nodes (id) UNIQUE NOT NULL,
    status     INT,
    timestamp  BIGINT
);