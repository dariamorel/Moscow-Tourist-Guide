DROP SCHEMA IF EXISTS tour_guide CASCADE;

CREATE SCHEMA IF NOT EXISTS tour_guide;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tour_guide.sights(
    id integer PRIMARY KEY,
    img text NOT NULL, 
    lat numeric NOT NULL,
    lon numeric NOT NULL,
    tag text,
    short_desc text NOT NULL,
    desc text NOT NULL,
    timetable text,
    addr text,
    phone text,
    subway text
);

CREATE TABLE tour_guide.time_between_sights(
    id integer PRIMARY KEY,
    sight1 integer NOT NULL,
    sight2 integer NOT NULL,
    total_time time
);

CREATE TABLE tour_guide.routes(
    id integer PRIMARY KEY,
    order integer NOT NULL,
    planned_order integer NOT NULL,
    user_id integer NOT NULL,
    total_time time NOT NULL,
    date timestamptz NOT NULL
);

CREATE TABLE tour_guide.users(
    id integer PRIMARY KEY,
    login text NOT NULL,
    password text NOT NULL
);

CREATE TABLE tour_guide.user_to_achievement(
    id integer PRIMARY KEY,
    user_id integer NOT NULL,
    achievement_id integer NOT NULL
);

CREATE TABLE tour_guide.achievements(
    id integer PRIMARY KEY,
    type integer NOT NULL,
    count integer NOT NULL,
    tag text
);
