set sql_safe_updates = 0;

alter table report
  add column evidence_type varchar(20) not null default 'seen',
  add column status varchar(20) not null default 'unverified';

alter table sub_report
  add column evidence_type varchar(20) not null default 'heard',
  add column category varchar(50) null;

drop function if exists fn_required_votes;

delimiter //

create function fn_required_votes(evidence varchar(20))
returns int
deterministic
begin
    return case evidence
             when 'seen'  then 2
             when 'heard' then 4
             else 5
           end;
end //

delimiter ;

update sub_report s
  join report r on r.report_id = s.report_id
   set s.category = r.category
 where s.category is null;

update report r
   set upvote_count = (select count(*) from vote v
                        where v.report_id = r.report_id and v.vote_type = 'up'),
       downvote_count = (select count(*) from vote v
                          where v.report_id = r.report_id and v.vote_type = 'down');

update sub_report s
   set upvote_count = (select count(*) from vote v
                        where v.sub_report_id = s.sub_report_id and v.vote_type = 'up'),
       downvote_count = (select count(*) from vote v
                          where v.sub_report_id = s.sub_report_id and v.vote_type = 'down');

update report
   set status = case
                  when downvote_count > upvote_count then 'disputed'
                  when upvote_count >= fn_required_votes(evidence_type) then 'verified'
                  else 'unverified'
                end;

drop trigger if exists trg_vote_insert;
drop trigger if exists trg_vote_delete;
drop trigger if exists trg_subreport_insert;

delimiter //

create trigger trg_vote_insert after insert on vote
for each row
begin
    if new.report_id is not null then
        update report
           set upvote_count   = upvote_count   + if(new.vote_type = 'up', 1, 0),
               downvote_count = downvote_count + if(new.vote_type = 'down', 1, 0),
               expires_at     = if(new.vote_type = 'up' and expires_at is not null,
                                   now() + interval 3 hour, expires_at)
         where report_id = new.report_id;

        update report
           set status = case
                          when downvote_count > upvote_count then 'disputed'
                          when upvote_count >= fn_required_votes(evidence_type) then 'verified'
                          else 'unverified'
                        end
         where report_id = new.report_id;
    else
        update sub_report
           set upvote_count   = upvote_count   + if(new.vote_type = 'up', 1, 0),
               downvote_count = downvote_count + if(new.vote_type = 'down', 1, 0)
         where sub_report_id = new.sub_report_id;
    end if;
end //

create trigger trg_vote_delete after delete on vote
for each row
begin
    if old.report_id is not null then
        update report
           set upvote_count   = upvote_count   - if(old.vote_type = 'up', 1, 0),
               downvote_count = downvote_count - if(old.vote_type = 'down', 1, 0)
         where report_id = old.report_id;

        update report
           set status = case
                          when downvote_count > upvote_count then 'disputed'
                          when upvote_count >= fn_required_votes(evidence_type) then 'verified'
                          else 'unverified'
                        end
         where report_id = old.report_id;
    else
        update sub_report
           set upvote_count   = upvote_count   - if(old.vote_type = 'up', 1, 0),
               downvote_count = downvote_count - if(old.vote_type = 'down', 1, 0)
         where sub_report_id = old.sub_report_id;
    end if;
end //

create trigger trg_subreport_insert after insert on sub_report
for each row
begin
    if new.evidence_type = 'seen' and new.category is not null then
        update report
           set category = new.category
         where report_id = new.report_id and category = 'Unknown';
    end if;
end //

delimiter ;

drop procedure if exists sp_nearby_reports;

delimiter //

create procedure sp_nearby_reports(
    in p_lat decimal(9,6),
    in p_lng decimal(9,6),
    in p_radius int)
begin
    select r.report_id,
           r.title,
           r.category,
           r.status,
           r.evidence_type,
           st_distance_sphere(point(l.longitude, l.latitude),
                              point(p_lng, p_lat)) as distance_m
      from report r
      join location l on l.location_id = r.location_id
     where (r.expires_at is null or r.expires_at > now())
       and st_distance_sphere(point(l.longitude, l.latitude),
                              point(p_lng, p_lat)) <= p_radius
     order by distance_m;
end //

delimiter ;

select report_id, title, category, evidence_type, status,
       upvote_count, downvote_count
  from report
 order by report_id;

show triggers;

select fn_required_votes('seen')    as seen_needs,
       fn_required_votes('heard')   as heard_needs,
       fn_required_votes('guessed') as guessed_needs;

call sp_nearby_reports(22.4621, 91.9729, 15000);
