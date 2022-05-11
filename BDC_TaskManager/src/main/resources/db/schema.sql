CREATE TABLE IF NOT EXISTS users_data
(
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    role     INT  NOT NULL
);