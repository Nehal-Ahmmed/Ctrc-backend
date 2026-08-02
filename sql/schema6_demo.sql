-- CTRC schema6 : demo data
-- Run this AFTER schema5.sql, on the same database the app actually talks to.
-- Safe to run once. Running it twice will duplicate the reports.

set sql_safe_updates = 0;

-- ---------------------------------------------------------------------------
-- Your own account. Put the email you log into the app with here, so the
-- saved reports at the end land on your profile. If the email is not found
-- it falls back to the newest account in the table.
-- ---------------------------------------------------------------------------

set @my_email = 'nehal@gmail.com';

set @me = coalesce(
    (select user_id from user where email = @my_email limit 1),
    (select user_id from user order by user_id desc limit 1));

-- ---------------------------------------------------------------------------
-- STEP 1 : demo users
-- The password below is a bcrypt hash. If it does not let you log in as these
-- users, it does not matter, they only need to exist as report authors.
-- ---------------------------------------------------------------------------

insert into user (name, password, email, address) values
('Tanvir Ahmed',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'tanvir@ctrcdemo.test',   'Raozan, Chattogram'),
('Sadia Islam',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'sadia@ctrcdemo.test',    'Pahartali, Raozan'),
('Rakibul Hasan',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'rakib@ctrcdemo.test',    'CUET Campus, Raozan'),
('Nusrat Jahan',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'nusrat@ctrcdemo.test',   'Noapara, Raozan'),
('Imran Chowdhury',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'imran@ctrcdemo.test',    'Hathazari, Chattogram'),
('Farhana Akter',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'farhana@ctrcdemo.test',  'Baizid, Chattogram'),
('Shahriar Kabir',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'shahriar@ctrcdemo.test', 'Oxygen More, Chattogram'),
('Mim Akter',         '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'mim@ctrcdemo.test',      'Feni Sadar, Feni');

set @u_tanvir   = (select user_id from user where email = 'tanvir@ctrcdemo.test');
set @u_sadia    = (select user_id from user where email = 'sadia@ctrcdemo.test');
set @u_rakib    = (select user_id from user where email = 'rakib@ctrcdemo.test');
set @u_nusrat   = (select user_id from user where email = 'nusrat@ctrcdemo.test');
set @u_imran    = (select user_id from user where email = 'imran@ctrcdemo.test');
set @u_farhana  = (select user_id from user where email = 'farhana@ctrcdemo.test');
set @u_shahriar = (select user_id from user where email = 'shahriar@ctrcdemo.test');
set @u_mim      = (select user_id from user where email = 'mim@ctrcdemo.test');

-- ---------------------------------------------------------------------------
-- STEP 2 : locations
-- First block is around CUET / Raozan / Chattogram for the nearby feature.
-- Second block follows the Chattogram to Dhaka highway for the route feature.
-- ---------------------------------------------------------------------------

insert into location (longitude, latitude, address, city) values
(91.970500, 22.462700, 'CUET Main Gate, Raozan',            'Chattogram'),
(91.944500, 22.488500, 'Pahartali Bazar, Raozan',           'Chattogram'),
(91.930000, 22.510000, 'Kaptai Road, Raozan',               'Chattogram'),
(91.960000, 22.475000, 'Noapara Bus Stand, Raozan',         'Chattogram'),
(91.809000, 22.506400, 'Hathazari Bus Stand',               'Chattogram'),
(91.860000, 22.397000, 'Baizid Bostami Road',               'Chattogram'),
(91.834000, 22.382000, 'Oxygen More',                       'Chattogram'),
(91.666700, 22.616700, 'Sitakunda Bazar, N1 Highway',       'Chattogram'),
(91.716700, 22.533300, 'Kumira, N1 Highway',                'Chattogram'),
(91.566700, 22.766700, 'Mirsharai, N1 Highway',             'Chattogram'),
(91.397600, 23.015900, 'Mohipal Flyover, Feni',             'Feni'),
(91.290000, 23.220000, 'Chauddagram, N1 Highway',           'Cumilla'),
(91.160000, 23.430000, 'Padua Bazar, Cumilla',              'Cumilla'),
(90.720000, 23.530000, 'Daudkandi Toll Plaza',              'Cumilla'),
(90.600000, 23.530000, 'Meghna Bridge, Gazaria',            'Munshiganj'),
(90.530000, 23.700000, 'Kanchpur Bridge, Narayanganj',      'Narayanganj'),
(90.435000, 23.710000, 'Jatrabari Flyover, Dhaka',          'Dhaka');

set @l_cuet       = (select location_id from location where address = 'CUET Main Gate, Raozan'       limit 1);
set @l_pahartali  = (select location_id from location where address = 'Pahartali Bazar, Raozan'      limit 1);
set @l_kaptai     = (select location_id from location where address = 'Kaptai Road, Raozan'          limit 1);
set @l_noapara    = (select location_id from location where address = 'Noapara Bus Stand, Raozan'    limit 1);
set @l_hathazari  = (select location_id from location where address = 'Hathazari Bus Stand'          limit 1);
set @l_baizid     = (select location_id from location where address = 'Baizid Bostami Road'          limit 1);
set @l_oxygen     = (select location_id from location where address = 'Oxygen More'                  limit 1);
set @l_sitakunda  = (select location_id from location where address = 'Sitakunda Bazar, N1 Highway'  limit 1);
set @l_kumira     = (select location_id from location where address = 'Kumira, N1 Highway'           limit 1);
set @l_mirsharai  = (select location_id from location where address = 'Mirsharai, N1 Highway'        limit 1);
set @l_feni       = (select location_id from location where address = 'Mohipal Flyover, Feni'        limit 1);
set @l_chauddagram= (select location_id from location where address = 'Chauddagram, N1 Highway'      limit 1);
set @l_padua      = (select location_id from location where address = 'Padua Bazar, Cumilla'         limit 1);
set @l_daudkandi  = (select location_id from location where address = 'Daudkandi Toll Plaza'         limit 1);
set @l_meghna     = (select location_id from location where address = 'Meghna Bridge, Gazaria'       limit 1);
set @l_kanchpur   = (select location_id from location where address = 'Kanchpur Bridge, Narayanganj' limit 1);
set @l_jatrabari  = (select location_id from location where address = 'Jatrabari Flyover, Dhaka'     limit 1);

-- ---------------------------------------------------------------------------
-- STEP 3 : reports
-- upvote_count, downvote_count and status are left alone on purpose. The
-- votes inserted in step 5 fire trg_vote_insert, which fills them in.
-- ---------------------------------------------------------------------------

insert into report (user_id, location_id, title, description, category, evidence_type, expires_at, created_at) values
(@u_tanvir,   @l_cuet,       'Deep pothole at CUET main gate',        'A large pothole has opened right at the main gate turn. Two rickshaws have already tipped over this morning.', 'Road damage',  'seen',    now() + interval 12 hour, now() - interval 40 minute),
(@u_sadia,    @l_pahartali,  'Heavy jam at Pahartali Bazar',          'Bazar day crowd has spilled onto the road, buses are barely moving in either direction.',                     'Traffic jam',  'seen',    now() + interval 12 hour, now() - interval 25 minute),
(@u_rakib,    @l_kaptai,     'Truck and CNG collision on Kaptai Road','A sand truck hit a CNG near the Kaptai Road turn. One lane is completely blocked.',                            'Accident',     'seen',    now() + interval 12 hour, now() - interval 55 minute),
(@u_nusrat,   @l_noapara,    'Cars are not moving at Noapara',        'Long line of stopped vehicles ahead of me. I cannot see what is going on from here.',                          'Unknown',      'guessed', now() + interval 12 hour, now() - interval 12 minute),
(@u_imran,    @l_hathazari,  'Water on the road near Hathazari stand','Heard from a bus driver that water has collected up to knee level after last night rain.',                     'Waterlogging', 'heard',   now() + interval 12 hour, now() - interval 2 hour),
(@u_farhana,  @l_baizid,     'Road closed for pipeline work',         'WASA has dug up half the road for a pipeline. Only one lane is open and traffic is being held.',                'Road block',   'seen',    now() + interval 12 hour, now() - interval 3 hour),
(@u_shahriar, @l_oxygen,     'Gridlock at Oxygen More',               'Very slow moving traffic in all four directions.',                                                             'Traffic jam',  'seen',    now() + interval 12 hour, now() - interval 4 hour),
(@u_mim,      @l_sitakunda,  'Bus accident near Sitakunda Bazar',     'A Dhaka bound bus has gone off the shoulder just past the bazar. Police are on site.',                          'Accident',     'seen',    now() + interval 12 hour, now() - interval 1 hour),
(@u_tanvir,   @l_kumira,     'Broken road surface at Kumira',         'The highway surface has cracked badly on the Dhaka bound side, vehicles are swerving.',                        'Road damage',  'seen',    now() + interval 12 hour, now() - interval 5 hour),
(@u_sadia,    @l_mirsharai,  'Long queue at Mirsharai',               'Traffic is backed up for about two kilometres on the highway.',                                                'Traffic jam',  'seen',    now() + interval 12 hour, now() - interval 35 minute),
(@u_rakib,    @l_feni,       'Two truck crash at Mohipal Flyover',    'Two trucks have collided at the foot of the flyover. Both Dhaka bound lanes are blocked.',                      'Accident',     'seen',    now() + interval 12 hour, now() - interval 50 minute),
(@u_nusrat,   @l_chauddagram,'Highway blocked at Chauddagram',        'My cousin called and said the highway is blocked there because of a local protest.',                           'Road block',   'heard',   now() + interval 12 hour, now() - interval 90 minute),
(@u_imran,    @l_padua,      'Waterlogging at Padua Bazar',           'Rain water has collected across the highway near Padua Bazar. Small cars are struggling.',                      'Waterlogging', 'seen',    now() + interval 12 hour, now() - interval 2 hour),
(@u_farhana,  @l_daudkandi,  'Toll plaza queue at Daudkandi',         'Only two toll booths are open so the queue is very long today.',                                               'Traffic jam',  'seen',    now() + interval 12 hour, now() - interval 20 minute),
(@u_shahriar, @l_meghna,     'Everything stopped on Meghna Bridge',   'Nothing is moving on the bridge. No idea what has happened up ahead.',                                          'Unknown',      'guessed', now() + interval 12 hour, now() - interval 15 minute),
(@u_mim,      @l_kanchpur,   'Accident at Kanchpur Bridge',           'A covered van has overturned on the approach road. Traffic is being diverted.',                                 'Accident',     'seen',    now() + interval 12 hour, now() - interval 70 minute),
(@u_tanvir,   @l_jatrabari,  'Jam under Jatrabari Flyover',           'Usual evening jam is worse than normal today, barely moving.',                                                 'Traffic jam',  'seen',    now() + interval 12 hour, now() - interval 30 minute);

set @r_cuet        = (select report_id from report where location_id = @l_cuet        limit 1);
set @r_pahartali   = (select report_id from report where location_id = @l_pahartali   limit 1);
set @r_kaptai      = (select report_id from report where location_id = @l_kaptai      limit 1);
set @r_noapara     = (select report_id from report where location_id = @l_noapara     limit 1);
set @r_hathazari   = (select report_id from report where location_id = @l_hathazari   limit 1);
set @r_baizid      = (select report_id from report where location_id = @l_baizid      limit 1);
set @r_oxygen      = (select report_id from report where location_id = @l_oxygen      limit 1);
set @r_sitakunda   = (select report_id from report where location_id = @l_sitakunda   limit 1);
set @r_kumira      = (select report_id from report where location_id = @l_kumira      limit 1);
set @r_mirsharai   = (select report_id from report where location_id = @l_mirsharai   limit 1);
set @r_feni        = (select report_id from report where location_id = @l_feni        limit 1);
set @r_chauddagram = (select report_id from report where location_id = @l_chauddagram limit 1);
set @r_padua       = (select report_id from report where location_id = @l_padua       limit 1);
set @r_daudkandi   = (select report_id from report where location_id = @l_daudkandi   limit 1);
set @r_meghna      = (select report_id from report where location_id = @l_meghna      limit 1);
set @r_kanchpur    = (select report_id from report where location_id = @l_kanchpur    limit 1);
set @r_jatrabari   = (select report_id from report where location_id = @l_jatrabari   limit 1);

-- ---------------------------------------------------------------------------
-- STEP 4 : sub reports, so two incidents have a real thread under them
-- Their parents are already Accident, so trg_subreport_insert has nothing to
-- promote here. The two Unknown reports are deliberately left with no witness
-- so you can promote one live in front of your teacher.
-- ---------------------------------------------------------------------------

insert into location (longitude, latitude, address, city) values
(91.932500, 22.508000, 'Kaptai Road, 300m before the crash', 'Chattogram'),
(91.927000, 22.512500, 'Kaptai Road, past the crash',        'Chattogram'),
(91.400500, 23.013000, 'Mohipal, service road side',         'Feni'),
(91.394000, 23.018500, 'Mohipal, Dhaka bound approach',      'Feni');

set @l_kaptai_a = (select location_id from location where address = 'Kaptai Road, 300m before the crash' limit 1);
set @l_kaptai_b = (select location_id from location where address = 'Kaptai Road, past the crash'        limit 1);
set @l_feni_a   = (select location_id from location where address = 'Mohipal, service road side'         limit 1);
set @l_feni_b   = (select location_id from location where address = 'Mohipal, Dhaka bound approach'      limit 1);

insert into sub_report (user_id, report_id, location_id, description, evidence_type, category, created_at) values
(@u_tanvir,   @r_kaptai, @l_kaptai_a, 'Queue now reaches back to the CUET side. Take the village road if you can.', 'seen',  'Accident',    now() - interval 35 minute),
(@u_sadia,    @r_kaptai, @l_kaptai_b, 'Recovery truck has arrived, one lane may open soon.',                        'seen',  'Accident',    now() - interval 15 minute),
(@u_nusrat,   @r_kaptai, @l_kaptai_a, 'Someone at the tea stall said an ambulance already left.',                    'heard', 'Accident',    now() - interval 10 minute),
(@u_mim,      @r_feni,   @l_feni_a,   'Traffic is being sent through the service road, very slow.',                  'seen',  'Accident',    now() - interval 30 minute),
(@u_imran,    @r_feni,   @l_feni_b,   'Queue on the Dhaka bound side is about three kilometres now.',                'seen',  'Traffic jam', now() - interval 18 minute),
(@u_farhana,  @r_feni,   @l_feni_a,   'Heard on the radio that one lane will open within the hour.',                 'heard', 'Accident',    now() - interval 5 minute);

update sub_report s
  join location l1 on l1.location_id = s.location_id
  join report r     on r.report_id   = s.report_id
  join location l2  on l2.location_id = r.location_id
   set s.dist_from_parent = st_distance_sphere(point(l1.longitude, l1.latitude),
                                               point(l2.longitude, l2.latitude))
 where s.dist_from_parent is null;

-- ---------------------------------------------------------------------------
-- STEP 5 : votes
-- Every insert here fires trg_vote_insert, which is what actually fills in
-- upvote_count, downvote_count and status. Nothing is counted by hand.
-- ---------------------------------------------------------------------------

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_cuet, 'up' from user u
 where u.email in ('sadia@ctrcdemo.test','rakib@ctrcdemo.test','nusrat@ctrcdemo.test','imran@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_pahartali, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','rakib@ctrcdemo.test','farhana@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_kaptai, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','sadia@ctrcdemo.test','nusrat@ctrcdemo.test','imran@ctrcdemo.test','farhana@ctrcdemo.test','mim@ctrcdemo.test');

-- Only two upvotes on a guess, which needs five. Stays "Not verified yet".
insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_noapara, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','rakib@ctrcdemo.test');

-- Hearsay needs four upvotes and only has two, so this one stays unverified
-- even though it has as many votes as some verified reports.
insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_hathazari, 'up' from user u
 where u.email in ('farhana@ctrcdemo.test','shahriar@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_baizid, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','imran@ctrcdemo.test','shahriar@ctrcdemo.test');

-- More downvotes than upvotes, so this one flips to "Disputed".
insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_oxygen, 'up' from user u
 where u.email in ('mim@ctrcdemo.test');
insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_oxygen, 'down' from user u
 where u.email in ('tanvir@ctrcdemo.test','sadia@ctrcdemo.test','rakib@ctrcdemo.test','nusrat@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_sitakunda, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','sadia@ctrcdemo.test','imran@ctrcdemo.test','farhana@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_kumira, 'up' from user u
 where u.email in ('rakib@ctrcdemo.test','mim@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_mirsharai, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','rakib@ctrcdemo.test','nusrat@ctrcdemo.test','imran@ctrcdemo.test','mim@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_feni, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','sadia@ctrcdemo.test','nusrat@ctrcdemo.test','imran@ctrcdemo.test','farhana@ctrcdemo.test','shahriar@ctrcdemo.test','mim@ctrcdemo.test');

-- Hearsay, but five people backed it up, so it does reach "Verified".
insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_chauddagram, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','sadia@ctrcdemo.test','rakib@ctrcdemo.test','imran@ctrcdemo.test','mim@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_padua, 'up' from user u
 where u.email in ('sadia@ctrcdemo.test','farhana@ctrcdemo.test','shahriar@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_daudkandi, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','rakib@ctrcdemo.test','imran@ctrcdemo.test','mim@ctrcdemo.test');

-- A guess with three upvotes, still two short of the five it needs.
insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_meghna, 'up' from user u
 where u.email in ('sadia@ctrcdemo.test','nusrat@ctrcdemo.test','farhana@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_kanchpur, 'up' from user u
 where u.email in ('tanvir@ctrcdemo.test','sadia@ctrcdemo.test','rakib@ctrcdemo.test','imran@ctrcdemo.test','shahriar@ctrcdemo.test');

insert into vote (user_id, report_id, vote_type)
select u.user_id, @r_jatrabari, 'up' from user u
 where u.email in ('sadia@ctrcdemo.test','rakib@ctrcdemo.test','nusrat@ctrcdemo.test','imran@ctrcdemo.test','farhana@ctrcdemo.test','mim@ctrcdemo.test');

-- ---------------------------------------------------------------------------
-- STEP 6 : comments
-- ---------------------------------------------------------------------------

insert into comment (user_id, report_id, sub_report_id, content, created_at) values
(@u_sadia,    @r_cuet,        null, 'Went through there an hour back, it is worse than it looks in the photo.', now() - interval 30 minute),
(@u_rakib,    @r_cuet,        null, 'CUET transport office has been informed.',                                 now() - interval 20 minute),
(@u_nusrat,   @r_kaptai,      null, 'Is the road open now? I have to reach Raozan by six.',                      now() - interval 25 minute),
(@u_tanvir,   @r_kaptai,      null, 'Still blocked as of ten minutes ago. Use the Noapara side road.',            now() - interval 10 minute),
(@u_farhana,  @r_feni,        null, 'Left Chattogram at two, stuck here since three. Nothing is moving.',         now() - interval 40 minute),
(@u_imran,    @r_feni,        null, 'Police have opened the service road, it is slow but it moves.',              now() - interval 12 minute),
(@u_shahriar, @r_oxygen,      null, 'It is normal evening traffic here, not really an incident.',                 now() - interval 2 hour),
(@u_mim,      @r_chauddagram, null, 'Confirmed, my bus has been standing here for forty minutes.',                now() - interval 45 minute),
(@u_sadia,    @r_meghna,      null, 'Same here, no idea what is ahead. Someone up front please report.',          now() - interval 8 minute);

-- ---------------------------------------------------------------------------
-- STEP 7 : a few reports saved to your own account
-- ---------------------------------------------------------------------------

insert into saved_report (user_id, report_id)
select @me, r.report_id
  from report r
 where r.report_id in (@r_kaptai, @r_feni, @r_daudkandi)
   and @me is not null
   and not exists (select 1 from saved_report s
                    where s.user_id = @me and s.report_id = r.report_id);

-- ---------------------------------------------------------------------------
-- STEP 8 : keep the demo data alive until Friday
--
-- sp_refresh_demo slides the demo reports forward so the feed never shows a
-- three day old accident, and pushes their expiry out so nothing disappears
-- while you are presenting.
--
-- sp_add_daily_demo drops one genuinely new report in every morning, so the
-- app has something fresh each day instead of the same frozen list.
-- ---------------------------------------------------------------------------

drop procedure if exists sp_refresh_demo;
drop procedure if exists sp_add_daily_demo;

delimiter //

create procedure sp_refresh_demo()
begin
    update report r
      join user u on u.user_id = r.user_id
       set r.created_at = r.created_at + interval 1 day
     where u.email like '%@ctrcdemo.test'
       and r.created_at < now() - interval 20 hour;

    update sub_report s
      join user u on u.user_id = s.user_id
       set s.created_at = s.created_at + interval 1 day
     where u.email like '%@ctrcdemo.test'
       and s.created_at < now() - interval 20 hour;

    update report r
      join user u on u.user_id = r.user_id
       set r.expires_at = now() + interval 12 hour
     where u.email like '%@ctrcdemo.test'
       and (r.expires_at is null or r.expires_at < now() + interval 6 hour);
end //

create procedure sp_add_daily_demo()
begin
    declare v_user bigint;
    declare v_loc bigint;
    declare v_slot int;

    set v_slot = dayofmonth(now()) % 4;

    select user_id into v_user from user
     where email like '%@ctrcdemo.test'
     order by rand() limit 1;

    insert into location (longitude, latitude, address, city)
    values (case v_slot when 0 then 91.9705 when 1 then 91.8090 when 2 then 91.3976 else 90.7200 end,
            case v_slot when 0 then 22.4627 when 1 then 22.5064 when 2 then 23.0159 else 23.5300 end,
            case v_slot when 0 then 'Near CUET Main Gate, Raozan'
                        when 1 then 'Hathazari Bus Stand'
                        when 2 then 'Mohipal, Feni'
                        else 'Daudkandi Toll Plaza' end,
            case v_slot when 0 then 'Chattogram' when 1 then 'Chattogram'
                        when 2 then 'Feni' else 'Cumilla' end);
    set v_loc = last_insert_id();

    insert into report (user_id, location_id, title, description, category, evidence_type, expires_at)
    values (v_user, v_loc,
            case v_slot when 0 then 'Slow traffic near CUET this morning'
                        when 1 then 'Road digging near Hathazari stand'
                        when 2 then 'Queue building up at Mohipal'
                        else 'Toll booth queue at Daudkandi' end,
            case v_slot when 0 then 'Vehicles are crawling on the campus road since early morning.'
                        when 1 then 'One lane has been dug up for repair work, traffic is being held.'
                        when 2 then 'Dhaka bound queue is growing, allow extra time.'
                        else 'Only two booths open again today, expect a long wait.' end,
            case v_slot when 1 then 'Road damage' when 3 then 'Traffic jam' else 'Traffic jam' end,
            'seen',
            now() + interval 12 hour);
end //

delimiter ;

-- Events. These need the MySQL event scheduler switched on, see the notes.
drop event if exists ev_demo_keepalive;
drop event if exists ev_demo_daily;

create event ev_demo_keepalive
on schedule every 1 hour
    starts current_timestamp
    ends '2026-08-08 00:00:00'
do call sp_refresh_demo();

create event ev_demo_daily
on schedule every 1 day
    starts '2026-08-03 08:00:00'
    ends '2026-08-08 00:00:00'
do call sp_add_daily_demo();

-- ---------------------------------------------------------------------------
-- STEP 9 : check what landed
-- ---------------------------------------------------------------------------

select r.report_id, r.title, r.category, r.evidence_type, r.status,
       r.upvote_count, r.downvote_count,
       (select count(*) from sub_report s where s.report_id = r.report_id) as updates,
       l.city
  from report r
  join location l on l.location_id = r.location_id
 order by r.report_id;

select 'event scheduler' as item, @@event_scheduler as value;

show events;
