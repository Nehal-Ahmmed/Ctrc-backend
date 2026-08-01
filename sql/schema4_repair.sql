-- Repair / verification script for an already-deployed database.
--
-- Background: the original feed query only touched `report`, `location` and
-- `comment`. The newer query also reads `vote`, `saved_report` and `user`, so a
-- database that was created from an early version of schema.sql (where
-- saved_report sits at the very bottom, after the CREATE INDEX block) will now
-- fail with "bad SQL grammar" -> "Table ... doesn't exist".
--
-- Everything below is safe to run repeatedly on an existing database.

-- ---------------------------------------------------------------------------
-- STEP 1 — check what is actually there. Run this first.
-- Every row must report EXISTS.
-- ---------------------------------------------------------------------------
select 'user'         as table_name,
       if(count(*) > 0, 'EXISTS', '### MISSING ###') as status
from information_schema.tables
where table_schema = database() and table_name = 'user'
union all
select 'location',  if(count(*) > 0, 'EXISTS', '### MISSING ###')
from information_schema.tables
where table_schema = database() and table_name = 'location'
union all
select 'report',    if(count(*) > 0, 'EXISTS', '### MISSING ###')
from information_schema.tables
where table_schema = database() and table_name = 'report'
union all
select 'comment',   if(count(*) > 0, 'EXISTS', '### MISSING ###')
from information_schema.tables
where table_schema = database() and table_name = 'comment'
union all
select 'vote',      if(count(*) > 0, 'EXISTS', '### MISSING ###')
from information_schema.tables
where table_schema = database() and table_name = 'vote'
union all
select 'saved_report', if(count(*) > 0, 'EXISTS', '### MISSING ###')
from information_schema.tables
where table_schema = database() and table_name = 'saved_report';

-- Columns the newer queries depend on. Both must report EXISTS.
select 'report.expires_at' as column_name,
       if(count(*) > 0, 'EXISTS', '### MISSING ###') as status
from information_schema.columns
where table_schema = database() and table_name = 'report' and column_name = 'expires_at'
union all
select 'user.image_url', if(count(*) > 0, 'EXISTS', '### MISSING ###')
from information_schema.columns
where table_schema = database() and table_name = 'user' and column_name = 'image_url';

-- ---------------------------------------------------------------------------
-- STEP 2 — create anything the check above flagged as missing.
-- These use IF NOT EXISTS, so running them when the table is already there
-- changes nothing.
-- ---------------------------------------------------------------------------

create table if not exists vote (
    vote_id bigint auto_increment primary key,
    user_id bigint not null,
    report_id bigint null,
    sub_report_id bigint null,
    vote_type varchar(10) not null,
    voted_at timestamp default current_timestamp,
    foreign key (user_id) references user(user_id),
    foreign key (report_id) references report(report_id),
    constraint chk_vote_type check (vote_type in ('up', 'down'))
);

create table if not exists saved_report (
    saved_report_id bigint auto_increment primary key,
    user_id bigint not null,
    report_id bigint not null,
    saved_at timestamp default current_timestamp,
    foreign key (user_id) references user(user_id),
    foreign key (report_id) references report(report_id),
    unique (user_id, report_id)
);

-- `expires_at` is what hides stale incidents from the feed and the map.
-- Uncomment if STEP 1 reported it missing.
-- alter table report add column expires_at timestamp null;

-- ---------------------------------------------------------------------------
-- STEP 3 — indexes that make the map queries cheap.
-- MySQL has no "create index if not exists", so ignore error 1061
-- (Duplicate key name) if an index is already present.
-- ---------------------------------------------------------------------------

-- Bounding-box scans for the route corridor endpoint.
create index idx_location_lat_lng on location (latitude, longitude);

-- Feed and map filter on this constantly.
create index idx_report_expires_at on report (expires_at);

-- The per-viewer joins for "did I vote / did I save this".
create index idx_saved_report_user on saved_report (user_id, report_id);
