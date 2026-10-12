-- CapituLando
-- Migration 001: schema inicial
-- PostgreSQL / Supabase
CREATE TABLE
    users (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        email VARCHAR(320) NOT NULL,
        password_hash VARCHAR(100) NOT NULL,
        email_verified BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE UNIQUE INDEX users_email_lower_uidx ON users (LOWER(email));

CREATE TABLE
    profiles (
        id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
        username VARCHAR(50) NOT NULL,
        display_name VARCHAR(100) NOT NULL,
        bio TEXT,
        avatar_image TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT profiles_username_key UNIQUE (username)
    );

CREATE UNIQUE INDEX profiles_username_lower_uidx ON profiles (LOWER(username));

CREATE TABLE
    stories (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        profile_id UUID NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
        title VARCHAR(150) NOT NULL,
        synopsis TEXT,
        description TEXT,
        genre VARCHAR(100),
        status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
        cover_image TEXT,
        progress_percentage INTEGER NOT NULL DEFAULT 0,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT stories_status_check CHECK (status IN ('DRAFT', 'IN_PROGRESS', 'COMPLETED')),
        CONSTRAINT stories_progress_percentage_check CHECK (progress_percentage BETWEEN 0 AND 100)
    );

CREATE INDEX stories_profile_id_idx ON stories (profile_id);

CREATE TABLE
    races (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        name VARCHAR(100) NOT NULL,
        description TEXT,
        appearance TEXT,
        culture TEXT,
        habitat TEXT,
        race_image TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT races_story_id_id_key UNIQUE (story_id, id)
    );

CREATE TABLE
    characters (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        race_id UUID,
        name VARCHAR(100) NOT NULL,
        description TEXT,
        appearance TEXT,
        personality TEXT,
        age INTEGER,
        gender VARCHAR(50),
        background TEXT,
        character_image TEXT,
        role VARCHAR(50),
        mbti VARCHAR(8),
        motivation TEXT,
        notes TEXT,
        birth_date DATE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT characters_age_check CHECK (
            age IS NULL
            OR age >= 0
        ),
        CONSTRAINT characters_story_id_id_key UNIQUE (story_id, id),
        CONSTRAINT characters_story_race_fkey FOREIGN KEY (story_id, race_id) REFERENCES races (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX characters_story_id_idx ON characters (story_id);

CREATE INDEX characters_race_id_idx ON characters (race_id);

CREATE TABLE
    creatures (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        race_id UUID,
        name VARCHAR(100) NOT NULL,
        description TEXT,
        appearance TEXT,
        behavior TEXT,
        habitat TEXT,
        creature_image TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT creatures_story_race_fkey FOREIGN KEY (story_id, race_id) REFERENCES races (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX creatures_story_id_idx ON creatures (story_id);

CREATE INDEX creatures_race_id_idx ON creatures (race_id);

CREATE TABLE
    items (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        name VARCHAR(100) NOT NULL,
        type VARCHAR(50),
        description TEXT,
        appearance TEXT,
        origin TEXT,
        item_image TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE INDEX items_story_id_idx ON items (story_id);

CREATE TABLE
    locations (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        parent_location_id UUID,
        name VARCHAR(150) NOT NULL,
        type VARCHAR(80),
        description TEXT,
        climate VARCHAR(100),
        region VARCHAR(100),
        location_image TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT locations_story_id_id_key UNIQUE (story_id, id),
        CONSTRAINT locations_story_parent_fkey FOREIGN KEY (story_id, parent_location_id) REFERENCES locations (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX locations_story_id_idx ON locations (story_id);

CREATE INDEX locations_story_parent_idx ON locations (story_id, parent_location_id);

CREATE TABLE
    organizations (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        headquarters_location_id UUID,
        name VARCHAR(150) NOT NULL,
        type VARCHAR(100),
        description TEXT,
        organization_image TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT organizations_story_id_id_key UNIQUE (story_id, id),
        CONSTRAINT organizations_story_headquarters_fkey FOREIGN KEY (story_id, headquarters_location_id) REFERENCES locations (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX organizations_story_id_idx ON organizations (story_id);

CREATE INDEX organizations_story_headquarters_idx ON organizations (story_id, headquarters_location_id);

CREATE TABLE
    character_organizations (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        character_id UUID NOT NULL,
        organization_id UUID NOT NULL,
        role VARCHAR(100),
        joined_at VARCHAR(100),
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT uq_character_organization UNIQUE (character_id, organization_id),
        CONSTRAINT character_org_story_character_fkey FOREIGN KEY (story_id, character_id) REFERENCES characters (story_id, id) ON DELETE NO ACTION,
        CONSTRAINT character_org_story_organization_fkey FOREIGN KEY (story_id, organization_id) REFERENCES organizations (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX character_org_story_character_idx ON character_organizations (story_id, character_id);

CREATE INDEX character_org_story_organization_idx ON character_organizations (story_id, organization_id);

CREATE TABLE
    chapters (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        order_index INTEGER NOT NULL,
        title VARCHAR(200) NOT NULL,
        content TEXT,
        word_count INTEGER NOT NULL DEFAULT 0,
        status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
        published_at TIMESTAMPTZ,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT chapters_status_check CHECK (
            status IN ('DRAFT', 'IN_REVIEW', 'PUBLISHED', 'ARCHIVED')
        ),
        CONSTRAINT uq_story_chapter_order UNIQUE (story_id, order_index),
        CONSTRAINT chapters_story_id_id_key UNIQUE (story_id, id)
    );

CREATE INDEX chapters_story_id_idx ON chapters (story_id);

CREATE TABLE
    events (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
        story_id UUID NOT NULL REFERENCES stories (id) ON DELETE CASCADE,
        chapter_id UUID,
        location_id UUID,
        title VARCHAR(200) NOT NULL,
        description TEXT,
        chronological_date VARCHAR(100),
        sequence_order INTEGER,
        event_image TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT events_story_id_id_key UNIQUE (story_id, id),
        CONSTRAINT events_story_chapter_fkey FOREIGN KEY (story_id, chapter_id) REFERENCES chapters (story_id, id) ON DELETE NO ACTION,
        CONSTRAINT events_story_location_fkey FOREIGN KEY (story_id, location_id) REFERENCES locations (story_id, id) ON DELETE NO ACTION
    );

CREATE INDEX events_story_id_idx ON events (story_id);

CREATE INDEX events_story_chapter_idx ON events (story_id, chapter_id);

CREATE INDEX events_story_location_idx ON events (story_id, location_id);