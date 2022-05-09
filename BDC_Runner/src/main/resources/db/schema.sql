CREATE TABLE IF NOT EXISTS containers
(
    id             SERIAL PRIMARY KEY,
    container_name TEXT UNIQUE,
    image_name     TEXT,
    username       TEXT,
    password       TEXT
);

CREATE TABLE IF NOT EXISTS configuration
(
    id    SERIAL PRIMARY KEY,
    key   TEXT UNIQUE NOT NULL,
    value TEXT
);