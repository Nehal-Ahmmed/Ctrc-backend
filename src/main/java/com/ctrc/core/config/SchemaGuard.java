package com.ctrc.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaGuard.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaGuard(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        editing();
        softDelete();
        commentVoting();
    }

    private void editing() {
        column("report", "updated_at", "schema8_edit_repair.sql",
                "alter table report add column updated_at timestamp null");

        boolean hasRequiredVotes = routine("fn_required_votes", "schema8_edit_repair.sql",
                "create function fn_required_votes(evidence varchar(20)) "
                + "returns int deterministic "
                + "begin "
                + "  return case evidence "
                + "           when 'seen'  then 2 "
                + "           when 'heard' then 4 "
                + "           else 5 "
                + "         end; "
                + "end");

        if (!hasRequiredVotes) {
            log.warn("schema guard: skipping trg_report_before_update, "
                    + "fn_required_votes is not available");
            return;
        }

        trigger("trg_report_before_update", "schema8_edit_repair.sql",
                "create trigger trg_report_before_update before update on report "
                + "for each row "
                + "begin "
                + "  set new.status = case "
                + "                     when new.downvote_count > new.upvote_count then 'disputed' "
                + "                     when new.upvote_count >= fn_required_votes(new.evidence_type) then 'verified' "
                + "                     else 'unverified' "
                + "                   end; "
                + "end");
    }

    private void softDelete() {
        column("report", "deleted_at", "schema9_delete.sql",
                "alter table report add column deleted_at timestamp null");
    }

    private void commentVoting() {
        column("comment", "upvote_count", "schema10_comment_votes.sql",
                "alter table comment add column upvote_count int not null default 0");
        column("comment", "downvote_count", "schema10_comment_votes.sql",
                "alter table comment add column downvote_count int not null default 0");

        boolean hasTable = table("comment_vote", "schema10_comment_votes.sql",
                "create table comment_vote ("
                + "  comment_vote_id bigint auto_increment primary key,"
                + "  user_id    bigint not null,"
                + "  comment_id bigint not null,"
                + "  vote_type  varchar(10) not null,"
                + "  voted_at   timestamp default current_timestamp,"
                + "  foreign key (user_id)    references `user`(user_id),"
                + "  foreign key (comment_id) references comment(comment_id) on delete cascade,"
                + "  unique key uq_comment_vote_user (user_id, comment_id),"
                + "  constraint chk_comment_vote_type check (vote_type in ('up', 'down'))"
                + ")");

        if (!hasTable) return;

        trigger("trg_comment_vote_insert", "schema10_comment_votes.sql",
                "create trigger trg_comment_vote_insert after insert on comment_vote "
                + "for each row "
                + "begin "
                + "  update comment "
                + "     set upvote_count   = upvote_count   + if(new.vote_type = 'up', 1, 0), "
                + "         downvote_count = downvote_count + if(new.vote_type = 'down', 1, 0) "
                + "   where comment_id = new.comment_id; "
                + "end");

        trigger("trg_comment_vote_delete", "schema10_comment_votes.sql",
                "create trigger trg_comment_vote_delete after delete on comment_vote "
                + "for each row "
                + "begin "
                + "  update comment "
                + "     set upvote_count   = greatest(upvote_count   - if(old.vote_type = 'up', 1, 0), 0), "
                + "         downvote_count = greatest(downvote_count - if(old.vote_type = 'down', 1, 0), 0) "
                + "   where comment_id = old.comment_id; "
                + "end");
    }

    private boolean column(String table, String column, String migration, String ddl) {
        return ensure(table + "." + column, migration, ddl, () -> exists(
                "select count(*) from information_schema.columns "
                + "where table_schema = database() and table_name = ? and column_name = ?",
                table, column));
    }

    private boolean table(String table, String migration, String ddl) {
        return ensure(table + " table", migration, ddl, () -> exists(
                "select count(*) from information_schema.tables "
                + "where table_schema = database() and table_name = ?",
                table));
    }

    private boolean routine(String routine, String migration, String ddl) {
        return ensure(routine, migration, ddl, () -> exists(
                "select count(*) from information_schema.routines "
                + "where routine_schema = database() and routine_name = ?",
                routine));
    }

    private boolean trigger(String trigger, String migration, String ddl) {
        return ensure(trigger, migration, ddl, () -> exists(
                "select count(*) from information_schema.triggers "
                + "where trigger_schema = database() and trigger_name = ?",
                trigger));
    }

    private boolean ensure(String name, String migration, String ddl, Check present) {
        try {
            if (present.isTrue()) return true;

            log.warn("schema guard: {} is missing, creating it now", name);
            jdbcTemplate.execute(ddl);
            log.info("schema guard: {} created", name);
            return true;
        } catch (Exception e) {
            
            log.error("schema guard: could not create {} — run sql/{} against this "
                    + "database by hand. Cause: {}", name, migration, e.getMessage());
            return false;
        }
    }

    private boolean exists(String sql, Object... params) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, params);
        return count != null && count > 0;
    }

    @FunctionalInterface
    private interface Check {
        boolean isTrue() throws Exception;
    }
}
