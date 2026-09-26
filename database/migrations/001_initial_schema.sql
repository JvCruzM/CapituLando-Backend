-- CapituLando
-- Migration 001: schema inicial
-- PostgreSQL / Supabase

CREATE TABLE public.profiles (
    id UUID PRIMARY KEY
        REFERENCES auth.users(id)
        ON DELETE CASCADE,

    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    bio TEXT,
    avatar_image TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE public.stories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    profile_id UUID NOT NULL
        REFERENCES public.profiles(id)
        ON DELETE CASCADE,

    title VARCHAR(150) NOT NULL,
    synopsis TEXT,
    description TEXT,
    genre VARCHAR(100),

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    cover_image TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT stories_status_check
        CHECK (status IN (
            'DRAFT',
            'IN_PROGRESS',
            'COMPLETED'
        ))
);


CREATE TABLE public.races (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    story_id UUID NOT NULL
        REFERENCES public.stories(id)
        ON DELETE CASCADE,

    name VARCHAR(100) NOT NULL,
    description TEXT,
    appearance TEXT,
    culture TEXT,
    habitat TEXT,

    race_image TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE public.characters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    story_id UUID NOT NULL
        REFERENCES public.stories(id)
        ON DELETE CASCADE,

    race_id UUID
        REFERENCES public.races(id)
        ON DELETE SET NULL,

    name VARCHAR(100) NOT NULL,
    description TEXT,
    appearance TEXT,
    personality TEXT,
    age INTEGER,
    gender VARCHAR(50),
    background TEXT,

    character_image TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT characters_age_check
        CHECK (age IS NULL OR age >= 0)
);


CREATE TABLE public.creatures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    story_id UUID NOT NULL
        REFERENCES public.stories(id)
        ON DELETE CASCADE,

    race_id UUID
        REFERENCES public.races(id)
        ON DELETE SET NULL,

    name VARCHAR(100) NOT NULL,
    description TEXT,
    appearance TEXT,
    behavior TEXT,
    habitat TEXT,

    creature_image TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE public.items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    story_id UUID NOT NULL
        REFERENCES public.stories(id)
        ON DELETE CASCADE,

    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    description TEXT,
    appearance TEXT,
    origin TEXT,

    item_image TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- Índices para as principais chaves estrangeiras

CREATE INDEX stories_profile_id_idx
    ON public.stories(profile_id);

CREATE INDEX races_story_id_idx
    ON public.races(story_id);

CREATE INDEX characters_story_id_idx
    ON public.characters(story_id);

CREATE INDEX characters_race_id_idx
    ON public.characters(race_id);

CREATE INDEX creatures_story_id_idx
    ON public.creatures(story_id);

CREATE INDEX creatures_race_id_idx
    ON public.creatures(race_id);

CREATE INDEX items_story_id_idx
    ON public.items(story_id);