-- CapituLando
-- Migration 004: integridade entre entidades da mesma Story

ALTER TABLE public.locations
    ADD CONSTRAINT locations_story_id_id_key
    UNIQUE (story_id, id);

ALTER TABLE public.locations
    ADD CONSTRAINT locations_story_parent_fkey
    FOREIGN KEY (story_id, parent_location_id)
    REFERENCES public.locations (story_id, id)
    ON DELETE NO ACTION;


ALTER TABLE public.organizations
    ADD CONSTRAINT organizations_story_id_id_key
    UNIQUE (story_id, id);

ALTER TABLE public.organizations
    ADD CONSTRAINT organizations_story_headquarters_fkey
    FOREIGN KEY (story_id, headquarters_location_id)
    REFERENCES public.locations (story_id, id)
    ON DELETE NO ACTION;


ALTER TABLE public.chapters
    ADD CONSTRAINT chapters_story_id_id_key
    UNIQUE (story_id, id);


ALTER TABLE public.events
    ADD CONSTRAINT events_story_chapter_fkey
    FOREIGN KEY (story_id, chapter_id)
    REFERENCES public.chapters (story_id, id)
    ON DELETE NO ACTION;

ALTER TABLE public.events
    ADD CONSTRAINT events_story_location_fkey
    FOREIGN KEY (story_id, location_id)
    REFERENCES public.locations (story_id, id)
    ON DELETE NO ACTION;


ALTER TABLE public.characters
    ADD CONSTRAINT characters_story_id_id_key
    UNIQUE (story_id, id);


ALTER TABLE public.character_organizations
    ADD COLUMN story_id UUID NOT NULL;

ALTER TABLE public.character_organizations
    ADD CONSTRAINT character_org_story_character_fkey
    FOREIGN KEY (story_id, character_id)
    REFERENCES public.characters (story_id, id)
    ON DELETE NO ACTION;

ALTER TABLE public.character_organizations
    ADD CONSTRAINT character_org_story_organization_fkey
    FOREIGN KEY (story_id, organization_id)
    REFERENCES public.organizations (story_id, id)
    ON DELETE NO ACTION;


CREATE INDEX locations_story_parent_idx
    ON public.locations (story_id, parent_location_id);

CREATE INDEX organizations_story_headquarters_idx
    ON public.organizations (story_id, headquarters_location_id);

CREATE INDEX events_story_chapter_idx
    ON public.events (story_id, chapter_id);

CREATE INDEX events_story_location_idx
    ON public.events (story_id, location_id);

CREATE INDEX character_org_story_character_idx
    ON public.character_organizations (story_id, character_id);

CREATE INDEX character_org_story_organization_idx
    ON public.character_organizations (story_id, organization_id);