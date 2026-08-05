-- Sign up: create a new account with a hashed password
INSERT INTO user (name, password, email, address, image_url)
VALUES ('Arif Hossain', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'arif@ctrcdemo.test', 'Halishahar, Chattogram', NULL);

-- Log in: find the account that owns an email address
SELECT * FROM user WHERE email = 'tanvir@ctrcdemo.test';

-- Load the signed-in user's own profile
SELECT * FROM user WHERE user_id = 6;

-- Edit profile: change name, address and photo
UPDATE user
   SET name = 'Tanvir Ahmed', address = 'Raozan, Chattogram', image_url = NULL
 WHERE user_id = 6;

-- Change password
UPDATE user
   SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
 WHERE user_id = 6;


-- Store the point where an incident was seen, before the report is filed
INSERT INTO location (longitude, latitude, address, city)
VALUES (91.972900, 22.462100, 'CUET Main Gate', 'Chattogram');

-- Read one stored point back
SELECT * FROM location WHERE location_id = 3;


-- File a new report, expiring in 3 hours unless a time was given
INSERT INTO report (user_id, location_id, title, description, category,
                    evidence_type, image_url, upvote_count, downvote_count, expires_at)
VALUES (6, 3, 'Tree fallen across the road', 'A large tree came down after last night storm.',
        'Road Block', 'seen', NULL, 0, 0,
        COALESCE(NULL, now() + INTERVAL 3 HOUR));

-- Edit a report, allowed only for the user who filed it
UPDATE report
   SET title         = 'Deep pothole at CUET main gate',
       description   = 'A large pothole has opened right at the main gate turn.',
       category      = 'Road damage',
       evidence_type = 'seen',
       image_url     = 'https://picsum.photos/seed/ctrcpothole/900/600',
       updated_at    = now()
 WHERE report_id = 2 AND user_id = 6;

-- Delete a report by marking it, so linked updates and comments survive
UPDATE report SET deleted_at = now() WHERE report_id = 2 AND user_id = 6;

-- Adjust vote tallies directly
UPDATE report SET upvote_count   = upvote_count   + 1 WHERE report_id = 2;
UPDATE report SET downvote_count = downvote_count + 1 WHERE report_id = 2;


-- Load one report with its comment count, the viewer's vote and saved state
SELECT r.*,
       (SELECT count(*) FROM comment c WHERE c.report_id = r.report_id) AS comment_count,
       v.vote_type AS user_vote_type,
       CASE WHEN sv.saved_report_id IS NULL THEN 0 ELSE 1 END AS is_saved
  FROM report r
  LEFT JOIN vote         v  ON v.report_id  = r.report_id AND v.user_id  = 6
  LEFT JOIN saved_report sv ON sv.report_id = r.report_id AND sv.user_id = 6
 WHERE r.report_id = 2 AND r.deleted_at IS NULL;


-- The details page: one report plus its location, author and all counts in a single query
SELECT r.*, l.longitude, l.latitude, l.address AS loc_address, l.city,
       u.name AS author_name, u.image_url AS author_image_url,
       (SELECT count(*) FROM comment c     WHERE c.report_id   = r.report_id) AS comment_count,
       (SELECT count(*) FROM sub_report sr2 WHERE sr2.report_id = r.report_id) AS sub_report_count,
       v.vote_type AS user_vote_type,
       CASE WHEN sv.saved_report_id IS NULL THEN 0 ELSE 1 END AS is_saved
  FROM report r
  JOIN location l ON r.location_id = l.location_id
  LEFT JOIN user         u  ON u.user_id    = r.user_id
  LEFT JOIN vote         v  ON v.report_id  = r.report_id AND v.user_id  = 6
  LEFT JOIN saved_report sv ON sv.report_id = r.report_id AND sv.user_id = 6
 WHERE r.report_id = 2 AND r.deleted_at IS NULL;


-- All reports, newest first
SELECT r.*, l.longitude, l.latitude, l.address AS loc_address, l.city,
       u.name AS author_name, u.image_url AS author_image_url,
       (SELECT count(*) FROM comment c     WHERE c.report_id   = r.report_id) AS comment_count,
       (SELECT count(*) FROM sub_report sr2 WHERE sr2.report_id = r.report_id) AS sub_report_count,
       v.vote_type AS user_vote_type,
       CASE WHEN sv.saved_report_id IS NULL THEN 0 ELSE 1 END AS is_saved
  FROM report r
  JOIN location l ON r.location_id = l.location_id
  LEFT JOIN user         u  ON u.user_id    = r.user_id
  LEFT JOIN vote         v  ON v.report_id  = r.report_id AND v.user_id  = 6
  LEFT JOIN saved_report sv ON sv.report_id = r.report_id AND sv.user_id = 6
 WHERE r.deleted_at IS NULL
 ORDER BY r.created_at DESC
 LIMIT 300;


-- My Reports page: everything the signed-in user has filed
SELECT r.*, l.longitude, l.latitude, l.address AS loc_address, l.city,
       u.name AS author_name, u.image_url AS author_image_url,
       (SELECT count(*) FROM comment c     WHERE c.report_id   = r.report_id) AS comment_count,
       (SELECT count(*) FROM sub_report sr2 WHERE sr2.report_id = r.report_id) AS sub_report_count,
       v.vote_type AS user_vote_type,
       CASE WHEN sv.saved_report_id IS NULL THEN 0 ELSE 1 END AS is_saved
  FROM report r
  JOIN location l ON r.location_id = l.location_id
  LEFT JOIN user         u  ON u.user_id    = r.user_id
  LEFT JOIN vote         v  ON v.report_id  = r.report_id AND v.user_id  = 6
  LEFT JOIN saved_report sv ON sv.report_id = r.report_id AND sv.user_id = 6
 WHERE r.user_id = 6 AND r.deleted_at IS NULL
 ORDER BY r.created_at DESC
 LIMIT 300;


-- Saved Posts page: bookmarked reports, most recently saved first
SELECT r.*, l.longitude, l.latitude, l.address AS loc_address, l.city,
       u.name AS author_name, u.image_url AS author_image_url,
       (SELECT count(*) FROM comment c     WHERE c.report_id   = r.report_id) AS comment_count,
       (SELECT count(*) FROM sub_report sr2 WHERE sr2.report_id = r.report_id) AS sub_report_count,
       v.vote_type AS user_vote_type,
       CASE WHEN sv.saved_report_id IS NULL THEN 0 ELSE 1 END AS is_saved
  FROM report r
  JOIN location l ON r.location_id = l.location_id
  LEFT JOIN user         u  ON u.user_id    = r.user_id
  LEFT JOIN vote         v  ON v.report_id  = r.report_id AND v.user_id  = 6
  LEFT JOIN saved_report sv ON sv.report_id = r.report_id AND sv.user_id = 6
  JOIN saved_report sr ON sr.report_id = r.report_id
 WHERE sr.user_id = 6 AND r.deleted_at IS NULL
 ORDER BY sr.saved_at DESC
 LIMIT 300;


-- Home feed: live reports within 5 km of the user, nearest first
SELECT r.*, l.longitude, l.latitude, l.address AS loc_address, l.city,
       u.name AS author_name, u.image_url AS author_image_url,
       (SELECT count(*) FROM comment c     WHERE c.report_id   = r.report_id) AS comment_count,
       (SELECT count(*) FROM sub_report sr2 WHERE sr2.report_id = r.report_id) AS sub_report_count,
       v.vote_type AS user_vote_type,
       CASE WHEN sv.saved_report_id IS NULL THEN 0 ELSE 1 END AS is_saved
  FROM report r
  JOIN location l ON r.location_id = l.location_id
  LEFT JOIN user         u  ON u.user_id    = r.user_id
  LEFT JOIN vote         v  ON v.report_id  = r.report_id AND v.user_id  = 6
  LEFT JOIN saved_report sv ON sv.report_id = r.report_id AND sv.user_id = 6
 WHERE (r.expires_at IS NULL OR r.expires_at > now())
   AND r.deleted_at IS NULL
   AND ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(91.9729, 22.4621)) <= 5000
 ORDER BY ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(91.9729, 22.4621)) ASC
 LIMIT 300;


-- Same feed narrowed to one category, showing the distance in metres
SELECT r.report_id, r.title, r.category, r.status,
       ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(91.9729, 22.4621)) AS distance_m
  FROM report r
  JOIN location l ON r.location_id = l.location_id
 WHERE (r.expires_at IS NULL OR r.expires_at > now())
   AND r.deleted_at IS NULL
   AND ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(91.9729, 22.4621)) <= 15000
   AND r.category = 'Road damage'
 ORDER BY distance_m ASC
 LIMIT 300;


-- Feed with every filter applied at once, ordered by best score
SELECT r.report_id, r.title, r.status, r.evidence_type,
       r.upvote_count, r.downvote_count,
       (r.upvote_count - r.downvote_count) AS score
  FROM report r
  JOIN location l ON r.location_id = l.location_id
 WHERE (r.expires_at IS NULL OR r.expires_at > now())
   AND r.deleted_at IS NULL
   AND ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(91.9729, 22.4621)) <= 15000
   AND r.status = 'verified'
   AND r.evidence_type = 'seen'
   AND r.created_at >= now() - INTERVAL 360 HOUR
   AND r.image_url IS NOT NULL AND r.image_url <> ''
 ORDER BY (r.upvote_count - r.downvote_count) DESC, r.created_at DESC
 LIMIT 300;


-- Feed ordered by how much discussion each report has attracted
SELECT r.report_id, r.title, r.category,
       (SELECT count(*) FROM comment c     WHERE c.report_id   = r.report_id) AS comment_count,
       (SELECT count(*) FROM sub_report sr2 WHERE sr2.report_id = r.report_id) AS sub_report_count
  FROM report r
  JOIN location l ON r.location_id = l.location_id
 WHERE r.deleted_at IS NULL
 ORDER BY comment_count DESC, r.created_at DESC
 LIMIT 300;


-- Map view: every live report inside the visible rectangle
SELECT r.report_id, r.title, l.latitude, l.longitude
  FROM report r
  JOIN location l ON r.location_id = l.location_id
 WHERE (r.expires_at IS NULL OR r.expires_at > now())
   AND r.deleted_at IS NULL
   AND l.latitude  >= 22.3000 AND l.latitude  <= 22.6000
   AND l.longitude >= 91.7000 AND l.longitude <= 92.1000
 LIMIT 300;


-- Find where the parent report was pinned, needed for the distance below
SELECT location_id FROM report WHERE report_id = 2;

-- Add an update to an existing report, storing how far it is from the original
INSERT INTO sub_report (user_id, report_id, location_id, description,
                        evidence_type, category, image_url, dist_from_parent,
                        upvote_count, downvote_count)
VALUES (7, 2, 4, 'Still blocked, traffic backing up towards Kaptai road.',
        'seen', 'Road damage',
        NULL,
        (SELECT ST_Distance_Sphere(POINT(l1.longitude, l1.latitude),
                                   POINT(l2.longitude, l2.latitude))
           FROM location l1 CROSS JOIN location l2
          WHERE l1.location_id = 4
            AND l2.location_id = 3),
        0, 0);

-- Read one update on its own
SELECT * FROM sub_report WHERE sub_report_id = 1;

-- Sub-report details page: the update with its location, author and parent title
SELECT sr.*, l.longitude, l.latitude, l.address AS loc_address, l.city,
       u.name AS author_name, u.image_url AS author_image_url,
       r.title AS parent_title,
       (SELECT count(*) FROM comment c WHERE c.sub_report_id = sr.sub_report_id) AS comment_count,
       v.vote_type AS user_vote_type
  FROM sub_report sr
  JOIN location l ON sr.location_id = l.location_id
  JOIN report   r ON sr.report_id   = r.report_id
  LEFT JOIN user u ON u.user_id = sr.user_id
  LEFT JOIN vote v ON v.sub_report_id = sr.sub_report_id AND v.user_id = 6
 WHERE sr.sub_report_id = 1;

-- The whole update thread hanging off one report, oldest first
SELECT sr.*, l.longitude, l.latitude, l.address AS loc_address, l.city,
       u.name AS author_name, u.image_url AS author_image_url,
       r.title AS parent_title,
       (SELECT count(*) FROM comment c WHERE c.sub_report_id = sr.sub_report_id) AS comment_count,
       NULL AS user_vote_type
  FROM sub_report sr
  JOIN location l ON sr.location_id = l.location_id
  JOIN report   r ON sr.report_id   = r.report_id
  LEFT JOIN user u ON u.user_id = sr.user_id
 WHERE sr.report_id = 2
 ORDER BY sr.created_at ASC;


-- Post a comment on a report
INSERT INTO comment (user_id, report_id, sub_report_id, content)
VALUES (6, 2, NULL, 'Still there as of ten minutes ago.');

-- Post a comment on an update instead
INSERT INTO comment (user_id, report_id, sub_report_id, content)
VALUES (6, NULL, 1, 'Confirmed, I just passed it.');

-- Comment thread of a report, each row carrying the reader's own vote
SELECT c.*, u.name AS user_name, u.image_url AS user_image_url,
       cv.vote_type AS user_vote_type
  FROM comment c
  JOIN user u ON c.user_id = u.user_id
  LEFT JOIN comment_vote cv ON cv.comment_id = c.comment_id AND cv.user_id = 6
 WHERE c.report_id = 2
 ORDER BY c.created_at ASC;

-- Comment thread of an update
SELECT c.*, u.name AS user_name, u.image_url AS user_image_url,
       cv.vote_type AS user_vote_type
  FROM comment c
  JOIN user u ON c.user_id = u.user_id
  LEFT JOIN comment_vote cv ON cv.comment_id = c.comment_id AND cv.user_id = 6
 WHERE c.sub_report_id = 1
 ORDER BY c.created_at ASC;

-- Check a comment exists before allowing a vote on it
SELECT c.* FROM comment c WHERE c.comment_id = 1;


-- Vote on a report, the trigger updates the tally and the status
INSERT INTO vote (user_id, report_id, sub_report_id, vote_type) VALUES (6, 2, NULL, 'up');

-- Vote on an update
INSERT INTO vote (user_id, report_id, sub_report_id, vote_type) VALUES (6, NULL, 1, 'up');

-- Check whether this user already voted on this report
SELECT * FROM vote WHERE user_id = 6 AND report_id = 2;

-- Check whether this user already voted on this update
SELECT * FROM vote WHERE user_id = 6 AND sub_report_id = 1;

-- Take a vote back, the trigger reverses the tally
DELETE FROM vote WHERE vote_id = 1;

-- List who voted on a report, newest first
SELECT v.*, u.name AS user_name, u.image_url AS user_image_url
  FROM vote v
  JOIN user u ON v.user_id = u.user_id
 WHERE v.report_id = 2
 ORDER BY v.voted_at DESC;

-- List who voted on an update
SELECT v.*, u.name AS user_name, u.image_url AS user_image_url
  FROM vote v
  JOIN user u ON v.user_id = u.user_id
 WHERE v.sub_report_id = 1
 ORDER BY v.voted_at DESC;


-- Vote on a comment
INSERT INTO comment_vote (user_id, comment_id, vote_type) VALUES (6, 1, 'up');

-- Check how this user voted on a comment
SELECT vote_type FROM comment_vote WHERE user_id = 6 AND comment_id = 1;

-- Take a comment vote back
DELETE FROM comment_vote WHERE user_id = 6 AND comment_id = 1;


-- Bookmark a report, ignored silently if it is already saved
INSERT IGNORE INTO saved_report (user_id, report_id) VALUES (6, 2);

-- Remove a bookmark
DELETE FROM saved_report WHERE user_id = 6 AND report_id = 2;

-- List everything this user has bookmarked
SELECT * FROM saved_report WHERE user_id = 6 ORDER BY saved_at DESC;

-- Check whether one report is bookmarked
SELECT COUNT(*) FROM saved_report WHERE user_id = 6 AND report_id = 2;


-- Open an incident group for a newly filed report
INSERT INTO incident_group (report_id, description) VALUES (2, 'Deep pothole at CUET main gate');

-- Call the stored procedure for nearby reports within 15 km
CALL sp_nearby_reports(22.462100, 91.972900, 15000);
