ALTER TABLE users
ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE users ADD CONSTRAINT users_token_version_nonnegative CHECK (token_version >= 0);