
CREATE SCHEMA IF NOT EXISTS url_shortener_db;

CREATE TABLE IF NOT EXISTS auth_user (
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT auth_user_primary_key PRIMARY KEY(username)
);

CREATE TABLE IF NOT EXISTS shortened_url (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    username VARCHAR(255) NOT NULL,
    original_url TEXT NOT NULL,
    short_code VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    visit_times BIGINT NOT NULL,
    FOREIGN KEY(username) REFERENCES auth_user(username),
    CONSTRAINT shortened_url_short_code_unique UNIQUE(short_code)
);