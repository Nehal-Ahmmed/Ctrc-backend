-- CTRC schema8 : editing your own reports
-- Run this after schema5.sql and schema7_images.sql.

set sql_safe_updates = 0;

-- ---------------------------------------------------------------------------
-- STEP 1 : remember when a report was last edited, so the app can mark it
-- ---------------------------------------------------------------------------

alter table report
  add column updated_at timestamp null;

-- ---------------------------------------------------------------------------
-- STEP 2 : editing can change evidence_type, and that changes how many
-- upvotes the report needs. This trigger recalculates the status on every
-- update, so an edit can never leave a stale badge behind.
--
-- It is a BEFORE trigger because a trigger on report is not allowed to run
-- another update on report. Setting new.status changes the row on its way in.
-- ---------------------------------------------------------------------------

drop trigger if exists trg_report_before_update;

delimiter //

create trigger trg_report_before_update before update on report
for each row
begin
    set new.status = case
                       when new.downvote_count > new.upvote_count then 'disputed'
                       when new.upvote_count >= fn_required_votes(new.evidence_type) then 'verified'
                       else 'unverified'
                     end;
end //

delimiter ;

-- ---------------------------------------------------------------------------
-- STEP 3 : check
-- ---------------------------------------------------------------------------

select report_id, title, evidence_type, upvote_count, downvote_count, status,
       if(updated_at is null, 'not edited', 'edited') as edited
  from report
 order by report_id;

show triggers like 'report';
