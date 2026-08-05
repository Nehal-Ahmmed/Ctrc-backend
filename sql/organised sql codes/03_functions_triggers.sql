-- Remove existing routines first, function last because the triggers depend on it
DROP TRIGGER IF EXISTS trg_vote_insert;
DROP TRIGGER IF EXISTS trg_vote_delete;
DROP TRIGGER IF EXISTS trg_subreport_insert;
DROP TRIGGER IF EXISTS trg_report_before_update;
DROP TRIGGER IF EXISTS trg_comment_vote_insert;
DROP TRIGGER IF EXISTS trg_comment_vote_delete;
DROP PROCEDURE IF EXISTS sp_nearby_reports;
DROP FUNCTION IF EXISTS fn_required_votes;


-- Returns how many upvotes a report needs to be verified, weaker evidence needs more
DELIMITER //

CREATE FUNCTION fn_required_votes(evidence VARCHAR(20))
RETURNS INT
DETERMINISTIC
BEGIN
    RETURN CASE evidence
             WHEN 'seen'  THEN 2
             WHEN 'heard' THEN 4
             ELSE 5
           END;
END //

DELIMITER ;


DELIMITER //

-- On a new vote, updates the tally, extends the report life, and recalculates the status
CREATE TRIGGER trg_vote_insert AFTER INSERT ON vote
FOR EACH ROW
BEGIN
    IF new.report_id IS NOT NULL THEN
        UPDATE report
           SET upvote_count   = upvote_count   + IF(new.vote_type = 'up', 1, 0),
               downvote_count = downvote_count + IF(new.vote_type = 'down', 1, 0),
               expires_at     = IF(new.vote_type = 'up' AND expires_at IS NOT NULL,
                                   now() + INTERVAL 3 HOUR, expires_at)
         WHERE report_id = new.report_id;

        UPDATE report
           SET status = CASE
                          WHEN downvote_count > upvote_count THEN 'disputed'
                          WHEN upvote_count >= fn_required_votes(evidence_type) THEN 'verified'
                          ELSE 'unverified'
                        END
         WHERE report_id = new.report_id;
    ELSE
        UPDATE sub_report
           SET upvote_count   = upvote_count   + IF(new.vote_type = 'up', 1, 0),
               downvote_count = downvote_count + IF(new.vote_type = 'down', 1, 0)
         WHERE sub_report_id = new.sub_report_id;
    END IF;
END //


-- On a removed vote, reverses the tally and recalculates the status
CREATE TRIGGER trg_vote_delete AFTER DELETE ON vote
FOR EACH ROW
BEGIN
    IF old.report_id IS NOT NULL THEN
        UPDATE report
           SET upvote_count   = upvote_count   - IF(old.vote_type = 'up', 1, 0),
               downvote_count = downvote_count - IF(old.vote_type = 'down', 1, 0)
         WHERE report_id = old.report_id;

        UPDATE report
           SET status = CASE
                          WHEN downvote_count > upvote_count THEN 'disputed'
                          WHEN upvote_count >= fn_required_votes(evidence_type) THEN 'verified'
                          ELSE 'unverified'
                        END
         WHERE report_id = old.report_id;
    ELSE
        UPDATE sub_report
           SET upvote_count   = upvote_count   - IF(old.vote_type = 'up', 1, 0),
               downvote_count = downvote_count - IF(old.vote_type = 'down', 1, 0)
         WHERE sub_report_id = old.sub_report_id;
    END IF;
END //


-- Editing can change the evidence type, so the status is recalculated on every update
CREATE TRIGGER trg_report_before_update BEFORE UPDATE ON report
FOR EACH ROW
BEGIN
    SET new.status = CASE
                       WHEN new.downvote_count > new.upvote_count THEN 'disputed'
                       WHEN new.upvote_count >= fn_required_votes(new.evidence_type) THEN 'verified'
                       ELSE 'unverified'
                     END;
END //


-- When an eyewitness names the incident, an unknown parent report takes that category
CREATE TRIGGER trg_subreport_insert AFTER INSERT ON sub_report
FOR EACH ROW
BEGIN
    IF new.evidence_type = 'seen' AND new.category IS NOT NULL THEN
        UPDATE report
           SET category = new.category
         WHERE report_id = new.report_id AND category = 'Unknown';
    END IF;
END //


-- Keeps the comment tally in step when a comment vote is cast
CREATE TRIGGER trg_comment_vote_insert AFTER INSERT ON comment_vote
FOR EACH ROW
BEGIN
    UPDATE comment
       SET upvote_count   = upvote_count   + IF(new.vote_type = 'up', 1, 0),
           downvote_count = downvote_count + IF(new.vote_type = 'down', 1, 0)
     WHERE comment_id = new.comment_id;
END //


-- Reverses the comment tally when a comment vote is taken back
CREATE TRIGGER trg_comment_vote_delete AFTER DELETE ON comment_vote
FOR EACH ROW
BEGIN
    UPDATE comment
       SET upvote_count   = GREATEST(upvote_count   - IF(old.vote_type = 'up', 1, 0), 0),
           downvote_count = GREATEST(downvote_count - IF(old.vote_type = 'down', 1, 0), 0)
     WHERE comment_id = old.comment_id;
END //

DELIMITER ;


-- Lists live reports within a radius of a point, nearest first
DELIMITER //

CREATE PROCEDURE sp_nearby_reports(
    IN p_lat    DECIMAL(9,6),
    IN p_lng    DECIMAL(9,6),
    IN p_radius INT)
BEGIN
    SELECT r.report_id,
           r.title,
           r.category,
           r.status,
           r.evidence_type,
           st_distance_sphere(point(l.longitude, l.latitude),
                              point(p_lng, p_lat)) AS distance_m
      FROM report r
      JOIN location l ON l.location_id = r.location_id
     WHERE (r.expires_at IS NULL OR r.expires_at > now())
       AND (r.deleted_at IS NULL)
       AND st_distance_sphere(point(l.longitude, l.latitude),
                              point(p_lng, p_lat)) <= p_radius
     ORDER BY distance_m;
END //

DELIMITER ;
