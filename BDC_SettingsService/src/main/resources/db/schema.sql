CREATE TABLE IF NOT EXISTS configuration
(
    id    SERIAL PRIMARY KEY,
    key   TEXT UNIQUE NOT NULL,
    value TEXT
);