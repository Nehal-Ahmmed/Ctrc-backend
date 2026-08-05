set sql_safe_updates = 0;

set @needs_column = (
    select count(*) = 0
      from information_schema.columns
     where table_schema = database()
       and table_name   = 'report'
       and column_name  = 'updated_at'
);

set @ddl = if(@needs_column,
              'alter table report add column updated_at timestamp null',
              'select ''report.updated_at already present'' as skipped');

prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

delimiter //

create function if not exists fn_required_votes(evidence varchar(20))
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

select 'report.updated_at' as piece,
       if(count(*) > 0, 'ok', 'MISSING') as state
  from information_schema.columns
 where table_schema = database()
   and table_name   = 'report'
   and column_name  = 'updated_at'
union all
select 'fn_required_votes',
       if(count(*) > 0, 'ok', 'MISSING')
  from information_schema.routines
 where routine_schema = database()
   and routine_name   = 'fn_required_votes'
union all
select 'trg_report_before_update',
       if(count(*) > 0, 'ok', 'MISSING')
  from information_schema.triggers
 where trigger_schema = database()
   and trigger_name   = 'trg_report_before_update';
