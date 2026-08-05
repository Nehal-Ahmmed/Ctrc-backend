set sql_safe_updates = 0;

set @needs_up = (
    select count(*) = 0 from information_schema.columns
     where table_schema = database() and table_name = 'comment'
       and column_name = 'upvote_count'
);
set @ddl = if(@needs_up,
              'alter table comment add column upvote_count int not null default 0',
              'select ''comment.upvote_count already present'' as skipped');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

set @needs_down = (
    select count(*) = 0 from information_schema.columns
     where table_schema = database() and table_name = 'comment'
       and column_name = 'downvote_count'
);
set @ddl = if(@needs_down,
              'alter table comment add column downvote_count int not null default 0',
              'select ''comment.downvote_count already present'' as skipped');
prepare stmt from @ddl; execute stmt; deallocate prepare stmt;

create table if not exists comment_vote (
    comment_vote_id bigint auto_increment primary key,
    user_id    bigint not null,
    comment_id bigint not null,
    vote_type  varchar(10) not null,
    voted_at   timestamp default current_timestamp,
    foreign key (user_id)    references `user`(user_id),
    foreign key (comment_id) references comment(comment_id) on delete cascade,
    unique key uq_comment_vote_user (user_id, comment_id),
    constraint chk_comment_vote_type check (vote_type in ('up', 'down'))
);

drop trigger if exists trg_comment_vote_insert;
drop trigger if exists trg_comment_vote_delete;

delimiter //

create trigger trg_comment_vote_insert after insert on comment_vote
for each row
begin
    update comment
       set upvote_count   = upvote_count   + if(new.vote_type = 'up', 1, 0),
           downvote_count = downvote_count + if(new.vote_type = 'down', 1, 0)
     where comment_id = new.comment_id;
end //

create trigger trg_comment_vote_delete after delete on comment_vote
for each row
begin
    update comment
       set upvote_count   = greatest(upvote_count   - if(old.vote_type = 'up', 1, 0), 0),
           downvote_count = greatest(downvote_count - if(old.vote_type = 'down', 1, 0), 0)
     where comment_id = old.comment_id;
end //

delimiter ;

update comment c
   set c.upvote_count = (select count(*) from comment_vote v
                          where v.comment_id = c.comment_id and v.vote_type = 'up'),
       c.downvote_count = (select count(*) from comment_vote v
                            where v.comment_id = c.comment_id and v.vote_type = 'down');

select 'comment.upvote_count' as piece,
       if(count(*) > 0, 'ok', 'MISSING') as state
  from information_schema.columns
 where table_schema = database() and table_name = 'comment' and column_name = 'upvote_count'
union all
select 'comment.downvote_count',
       if(count(*) > 0, 'ok', 'MISSING')
  from information_schema.columns
 where table_schema = database() and table_name = 'comment' and column_name = 'downvote_count'
union all
select 'comment_vote table',
       if(count(*) > 0, 'ok', 'MISSING')
  from information_schema.tables
 where table_schema = database() and table_name = 'comment_vote'
union all
select 'trg_comment_vote_insert',
       if(count(*) > 0, 'ok', 'MISSING')
  from information_schema.triggers
 where trigger_schema = database() and trigger_name = 'trg_comment_vote_insert'
union all
select 'trg_comment_vote_delete',
       if(count(*) > 0, 'ok', 'MISSING')
  from information_schema.triggers
 where trigger_schema = database() and trigger_name = 'trg_comment_vote_delete';
