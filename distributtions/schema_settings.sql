CREATE TABLE IF NOT EXISTS configuration
(
    id
    SERIAL
    PRIMARY
    KEY,
    module_name
    TEXT,
    key
    TEXT,
    value
    TEXT,
    CHECK
(
    module_name
    IS
    NOT
    NULL
    OR
    key
    IS
    NOT
    NULL
)
    );

CREATE UNIQUE INDEX key_index ON configuration (key) WHERE (module_name IS NULL);