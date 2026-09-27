-- CapituLando
-- Migration 003: Locations, Organizations, Chapters and Events
-- PostgreSQL / Supabase

CREATE TABLE public.locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id UUID NOT NULL REFERENCES public.stories(id) ON DELETE CASCADE,
    parent_location_id UUID REFERENCES public.locations(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(80),
    description TEXT,
    climate VARCHAR(100),
    location_image TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE public.organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id UUID NOT NULL REFERENCES public.stories(id) ON DELETE CASCADE,
    headquarters_location_id UUID REFERENCES public.locations(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(100),
    description TEXT,
    organization_image TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE public.character_organizations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    character_id UUID NOT NULL REFERENCES public.characters(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    role VARCHAR(100),
    joined_at VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_character_organization UNIQUE (character_id, organization_id)
);

CREATE TABLE public.chapters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id UUID NOT NULL REFERENCES public.stories(id) ON DELETE CASCADE,
    order_index INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    word_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chapters_status_check CHECK (status IN ('DRAFT', 'IN_REVIEW', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT uq_story_chapter_order UNIQUE (story_id, order_index)
);

CREATE TABLE public.events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id UUID NOT NULL REFERENCES public.stories(id) ON DELETE CASCADE,
    chapter_id UUID REFERENCES public.chapters(id) ON DELETE SET NULL,
    location_id UUID REFERENCES public.locations(id) ON DELETE SET NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    chronological_date VARCHAR(100),
    sequence_order INTEGER,
    event_image TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Foreign Key Indexes
CREATE INDEX locations_story_id_idx ON public.locations(story_id);
CREATE INDEX locations_parent_location_id_idx ON public.locations(parent_location_id);
CREATE INDEX organizations_story_id_idx ON public.organizations(story_id);
CREATE INDEX organizations_headquarters_idx ON public.organizations(headquarters_location_id);
CREATE INDEX character_org_character_idx ON public.character_organizations(character_id);
CREATE INDEX character_org_organization_idx ON public.character_organizations(organization_id);
CREATE INDEX chapters_story_id_idx ON public.chapters(story_id);
CREATE INDEX events_story_id_idx ON public.events(story_id);
CREATE INDEX events_chapter_id_idx ON public.events(chapter_id);
CREATE INDEX events_location_id_idx ON public.events(location_id);
