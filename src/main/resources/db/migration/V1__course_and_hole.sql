-- V1__course_hole_tees.sql

-- Courses
CREATE TABLE course (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  location VARCHAR(160) NOT NULL,
  holes SMALLINT NOT NULL CHECK (holes IN (9,18)),
  par_total SMALLINT NOT NULL CHECK (par_total BETWEEN 27 AND 90),
  owner_username VARCHAR(100) NOT NULL, -- from JWT sub (e.g., "declan1")
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Holes (no yards here; yards are per tee set)
CREATE TABLE hole (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  number SMALLINT NOT NULL CHECK (number BETWEEN 1 AND 18),
  par SMALLINT NOT NULL CHECK (par BETWEEN 3 AND 6),
  stroke_index SMALLINT NOT NULL CHECK (stroke_index BETWEEN 1 AND 18),

  CONSTRAINT uq_hole_number UNIQUE (course_id, number),
  CONSTRAINT uq_hole_si     UNIQUE (course_id, stroke_index)
);

-- Tee sets for a course (e.g., White, Yellow, Red, Championship)
CREATE TABLE tee_set (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL REFERENCES course(id) ON DELETE CASCADE,
  name VARCHAR(50) NOT NULL,                      -- "White", "Yellow", "Red", etc.
  color VARCHAR(30),                              -- optional UI hint (e.g., "white" or "#ffffff")
  gender VARCHAR(20),                             -- optional: "Men", "Women", "Senior", etc.
  rating NUMERIC(4,1),                            -- optional USGA/R&A course rating
  slope  SMALLINT CHECK (slope BETWEEN 55 AND 155), -- optional
  total_yards INT CHECK (total_yards BETWEEN 1000 AND 9000),
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now(),

  CONSTRAINT uq_tee_name_per_course UNIQUE (course_id, name)
);

-- Yardage per hole per tee set
CREATE TABLE tee_hole (
  id BIGSERIAL PRIMARY KEY,
  tee_set_id BIGINT NOT NULL REFERENCES tee_set(id) ON DELETE CASCADE,
  hole_id    BIGINT NOT NULL REFERENCES hole(id)    ON DELETE CASCADE,
  yards INT NOT NULL CHECK (yards BETWEEN 10 AND 1000),

  CONSTRAINT uq_tee_hole UNIQUE (tee_set_id, hole_id)
);

-- Helpful indexes
CREATE INDEX idx_hole_course     ON hole(course_id);
CREATE INDEX idx_teeset_course   ON tee_set(course_id);
CREATE INDEX idx_teehole_teeset  ON tee_hole(tee_set_id);