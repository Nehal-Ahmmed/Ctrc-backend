-- Remove existing tables in reverse dependency order so this file can be re-run
DROP TABLE IF EXISTS comment_vote;
DROP TABLE IF EXISTS saved_report;
DROP TABLE IF EXISTS vote;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS incident_group;
DROP TABLE IF EXISTS sub_report;
DROP TABLE IF EXISTS report;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS user;


-- Registered accounts, email is unique so it can be used to log in
CREATE TABLE user (
    user_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    address    VARCHAR(255),
    image_url  VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Every geographic point in the system, shared by reports and sub-reports
CREATE TABLE location (
    location_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    longitude   DECIMAL(9,6) NOT NULL,
    latitude    DECIMAL(9,6) NOT NULL,
    address     VARCHAR(255),
    city        VARCHAR(100)
);


-- The main incident report filed by a user at one location
CREATE TABLE report (
    report_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    location_id    BIGINT NOT NULL,
    title          VARCHAR(150) NOT NULL,
    description    TEXT,
    category       VARCHAR(50) NOT NULL,
    upvote_count   INT DEFAULT 0,
    downvote_count INT DEFAULT 0,
    expires_at     TIMESTAMP NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES user(user_id),
    FOREIGN KEY (location_id) REFERENCES location(location_id)
);


-- A follow-up update filed by another user against an existing report
CREATE TABLE sub_report (
    sub_report_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL,
    report_id        BIGINT NOT NULL,
    location_id      BIGINT NOT NULL,
    description      TEXT,
    dist_from_parent DECIMAL(8,3),
    upvote_count     INT DEFAULT 0,
    downvote_count   INT DEFAULT 0,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)     REFERENCES user(user_id),
    FOREIGN KEY (report_id)   REFERENCES report(report_id),
    FOREIGN KEY (location_id) REFERENCES location(location_id)
);


-- Groups reports that describe the same real-world incident
CREATE TABLE incident_group (
    group_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id   BIGINT NOT NULL,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (report_id) REFERENCES report(report_id)
);


-- Comments, attached to either a report or a sub-report but never both
CREATE TABLE comment (
    comment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    report_id     BIGINT NULL,
    sub_report_id BIGINT NULL,
    content       TEXT NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)       REFERENCES user(user_id),
    FOREIGN KEY (report_id)     REFERENCES report(report_id),
    FOREIGN KEY (sub_report_id) REFERENCES sub_report(sub_report_id),
    CONSTRAINT chk_comment_target CHECK (
        (report_id IS NOT NULL AND sub_report_id IS NULL) OR
        (report_id IS NULL     AND sub_report_id IS NOT NULL)
    )
);


-- Up/down votes on reports and sub-reports, one vote per user per target
CREATE TABLE vote (
    vote_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    report_id     BIGINT NULL,
    sub_report_id BIGINT NULL,
    vote_type     VARCHAR(10) NOT NULL,
    voted_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)       REFERENCES user(user_id),
    FOREIGN KEY (report_id)     REFERENCES report(report_id),
    FOREIGN KEY (sub_report_id) REFERENCES sub_report(sub_report_id),
    CONSTRAINT chk_vote_target CHECK (
        (report_id IS NOT NULL AND sub_report_id IS NULL) OR
        (report_id IS NULL     AND sub_report_id IS NOT NULL)
    ),
    CONSTRAINT chk_vote_type CHECK (vote_type IN ('up', 'down'))
);


-- Bookmarks, so a user can save a report and find it again later
CREATE TABLE saved_report (
    saved_report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    report_id       BIGINT NOT NULL,
    saved_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)   REFERENCES user(user_id),
    FOREIGN KEY (report_id) REFERENCES report(report_id),
    UNIQUE KEY uq_saved_report_user (user_id, report_id)
);


-- Up/down votes on comments, deleted automatically when the comment is deleted
CREATE TABLE comment_vote (
    comment_vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    comment_id      BIGINT NOT NULL,
    vote_type       VARCHAR(10) NOT NULL,
    voted_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)    REFERENCES user(user_id),
    FOREIGN KEY (comment_id) REFERENCES comment(comment_id) ON DELETE CASCADE,
    UNIQUE KEY uq_comment_vote_user (user_id, comment_id),
    CONSTRAINT chk_comment_vote_type CHECK (vote_type IN ('up', 'down'))
);


-- Added how the reporter knew about the incident, and the verification status
ALTER TABLE report
    ADD COLUMN evidence_type VARCHAR(20) NOT NULL DEFAULT 'seen',
    ADD COLUMN status        VARCHAR(20) NOT NULL DEFAULT 'unverified';

-- Added photo support for reports
ALTER TABLE report
    ADD COLUMN image_url VARCHAR(500) NULL;

-- Added edit tracking, so a report can be marked as edited
ALTER TABLE report
    ADD COLUMN updated_at TIMESTAMP NULL;

-- Added soft delete, so a removed report is hidden instead of erased
ALTER TABLE report
    ADD COLUMN deleted_at TIMESTAMP NULL;

-- Added evidence type and category to sub-reports as well
ALTER TABLE sub_report
    ADD COLUMN evidence_type VARCHAR(20) NOT NULL DEFAULT 'heard',
    ADD COLUMN category      VARCHAR(50) NULL;

-- Added photo support for sub-reports
ALTER TABLE sub_report
    ADD COLUMN image_url VARCHAR(500) NULL;

-- Added vote tallies on comments
ALTER TABLE comment
    ADD COLUMN upvote_count   INT NOT NULL DEFAULT 0,
    ADD COLUMN downvote_count INT NOT NULL DEFAULT 0;


-- Indexes that speed up the feed, the map search and the saved list
CREATE INDEX idx_report_user       ON report(user_id);
CREATE INDEX idx_report_location   ON report(location_id);
CREATE INDEX idx_report_expires_at ON report(expires_at);
CREATE INDEX idx_location_lat_lng  ON location(latitude, longitude);
CREATE INDEX idx_subreport_report  ON sub_report(report_id);
CREATE INDEX idx_comment_report    ON comment(report_id);
CREATE INDEX idx_comment_subreport ON comment(sub_report_id);
CREATE INDEX idx_saved_report_user ON saved_report(user_id, report_id);

-- Stops the same user voting twice on the same report or sub-report
CREATE UNIQUE INDEX idx_vote_user_report    ON vote(user_id, report_id);
CREATE UNIQUE INDEX idx_vote_user_subreport ON vote(user_id, sub_report_id);
