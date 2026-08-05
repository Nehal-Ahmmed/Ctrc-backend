set sql_safe_updates = 0;

alter table report
  add column updated_at timestamp null;

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

select report_id, title, evidence_type, upvote_count, downvote_count, status,
       if(updated_at is null, 'not edited', 'edited') as edited
  from report
 order by report_id;

show triggers like 'report';
