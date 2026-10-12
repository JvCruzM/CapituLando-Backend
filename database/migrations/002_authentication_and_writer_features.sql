-- CapituLando
-- Migration 002: authentication and writer features
-- PostgreSQL / Supabase
CREATE TABLE
    auth_tokens (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
        token_hash VARCHAR(64) NOT NULL,
        purpose VARCHAR(30) NOT NULL,
        expires_at TIMESTAMPTZ NOT NULL,
        consumed_at TIMESTAMPTZ,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT auth_tokens_token_hash_key UNIQUE (token_hash)
    );

CREATE INDEX auth_tokens_user_purpose_idx ON auth_tokens (user_id, purpose);

CREATE INDEX auth_tokens_expires_at_idx ON auth_tokens (expires_at);

CREATE TABLE
    character_relationships (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        source_character_id UUID NOT NULL,
        target_character_id UUID NOT NULL,
        relationship_type VARCHAR(80) NOT NULL,
        level INTEGER NOT NULL DEFAULT 3,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT uq_character_relationship_source_target_type UNIQUE (
            source_character_id,
            target_character_id,
            relationship_type
        ),
        CONSTRAINT character_relationship_source_story_fkey FOREIGN KEY (story_id, source_character_id) REFERENCES characters (story_id, id) ON DELETE NO ACTION,
        CONSTRAINT character_relationship_target_story_fkey FOREIGN KEY (story_id, target_character_id) REFERENCES characters (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX character_relationships_story_id_idx ON character_relationships (story_id);

CREATE TABLE
    event_characters (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        event_id UUID NOT NULL,
        character_id UUID NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT uq_event_character UNIQUE (event_id, character_id),
        CONSTRAINT event_character_event_story_fkey FOREIGN KEY (story_id, event_id) REFERENCES events (story_id, id) ON DELETE NO ACTION,
        CONSTRAINT event_character_character_story_fkey FOREIGN KEY (story_id, character_id) REFERENCES characters (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX event_characters_story_id_idx ON event_characters (story_id);

CREATE TABLE
    goals (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        profile_id UUID NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
        story_id UUID REFERENCES stories (id) ON DELETE SET NULL,
        title VARCHAR(180) NOT NULL,
        description TEXT,
        unit VARCHAR(50) NOT NULL,
        current_value INTEGER NOT NULL DEFAULT 0,
        target_value INTEGER NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT goals_values_check CHECK (
            current_value >= 0
            AND target_value > 0
        )
    );

CREATE INDEX goals_profile_id_idx ON goals (profile_id);

CREATE INDEX goals_story_id_idx ON goals (story_id);

CREATE TABLE
    ideas (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        profile_id UUID NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
        story_id UUID REFERENCES stories (id) ON DELETE SET NULL,
        content TEXT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE INDEX ideas_profile_id_idx ON ideas (profile_id);

CREATE INDEX ideas_story_id_idx ON ideas (story_id);

CREATE TABLE
    world_rules (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        title VARCHAR(150) NOT NULL,
        category VARCHAR(80) NOT NULL,
        description TEXT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE INDEX world_rules_story_id_idx ON world_rules (story_id);