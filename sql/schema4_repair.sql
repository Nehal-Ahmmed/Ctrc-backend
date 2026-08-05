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

select 'report.expires_at' as column_name,
       if(count(*) > 0, 'EXISTS', '### MISSING ###') as status
from information_schema.columns
where table_schema = database() and table_name = 'report' and column_name = 'expires_at'
union all
select 'user.image_url', if(count(*) > 0, 'EXISTS', '### MISSING ###')
from information_schema.columns
where table_schema = database() and table_name = 'user' and column_name = 'image_url';

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

create index idx_location_lat_lng on location (latitude, longitude);

create index idx_report_expires_at on report (expires_at);

create index idx_saved_report_user on saved_report (user_id, report_id);
