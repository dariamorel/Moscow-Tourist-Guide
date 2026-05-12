CREATE SCHEMA IF NOT EXISTS tour_guide;

CREATE TABLE tour_guide.users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tour_guide.tags (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE tour_guide.sights (
    id BIGINT PRIMARY KEY,
    img TEXT NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    short_desc TEXT NOT NULL,
    full_desc TEXT NOT NULL,
    timetable TEXT,
    addr TEXT,
    phone TEXT,
    subway TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tour_guide.sight_to_tag (
    sight_id BIGINT NOT NULL REFERENCES tour_guide.sights(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tour_guide.tags(id) ON DELETE CASCADE,
    PRIMARY KEY (sight_id, tag_id)
);

CREATE TABLE tour_guide.time_between_sights (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sight1_id BIGINT NOT NULL REFERENCES tour_guide.sights(id) ON DELETE CASCADE,
    sight2_id BIGINT NOT NULL REFERENCES tour_guide.sights(id) ON DELETE CASCADE,
    travel_time INTERVAL NOT NULL,
    distance_meters INTEGER,
    UNIQUE(sight1_id, sight2_id)
);

CREATE TABLE tour_guide.routes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES tour_guide.users(id) ON DELETE CASCADE,
    status TEXT NOT NULL CHECK (status IN ('planned', 'in_progress', 'completed', 'declined')),
    total_time INTERVAL,
    date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Элементы маршрута (заменяет массивы order и planned_order)
CREATE TABLE tour_guide.route_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES tour_guide.routes(id) ON DELETE CASCADE,
    sight_id BIGINT NOT NULL REFERENCES tour_guide.sights(id) ON DELETE CASCADE,
    stop_order INTEGER NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('planned', 'visited', 'skipped')),
    planned_arrival TIMESTAMPTZ,
    actual_arrival TIMESTAMPTZ,
    UNIQUE(route_id, stop_order),
    UNIQUE(route_id, sight_id)
);

CREATE TABLE tour_guide.achievements (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    icon_url TEXT,
    required_count INTEGER NOT NULL DEFAULT 1,
    tag TEXT
);

CREATE TABLE tour_guide.user_achievements (
    user_id BIGINT NOT NULL REFERENCES tour_guide.users(id) ON DELETE CASCADE,
    achievement_id BIGINT NOT NULL REFERENCES tour_guide.achievements(id) ON DELETE CASCADE,
    progress INTEGER NOT NULL DEFAULT 0,
    unlocked_at TIMESTAMPTZ,
    PRIMARY KEY (user_id, achievement_id)
);

CREATE INDEX idx_routes_user_id ON tour_guide.routes(user_id);
CREATE INDEX idx_routes_status ON tour_guide.routes(status);
CREATE INDEX idx_route_items_route_id ON tour_guide.route_items(route_id);
CREATE INDEX idx_sight_to_tag_tag_id ON tour_guide.sight_to_tag(tag_id);
CREATE INDEX idx_user_achievements_user_id ON tour_guide.user_achievements(user_id);
CREATE INDEX idx_sights_lat_lon ON tour_guide.sights(lat, lon);
