-- Clear existing rows first so this file can be re-run, keys off during the wipe
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM saved_report;
DELETE FROM vote;
DELETE FROM comment;
DELETE FROM sub_report;
DELETE FROM report;
DELETE FROM location;
DELETE FROM user;

SET FOREIGN_KEY_CHECKS = 1;

-- 13 registered accounts, early ones plain text and demo ones bcrypt hashed
INSERT INTO user (user_id, name, password, email, address, image_url, created_at) VALUES
    (1,'Nehal','123456','nh@gmail.com','Chittagong',NULL,'2026-07-16 12:11:08'),
    (2,'Nehal','123456','nh2005@gmail.com','',NULL,'2026-07-16 12:12:59'),
    (3,'Nehal Ahmmed','123456','nehal@gmail.com','GEC Circle, Chittagong','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde','2026-07-16 14:27:28'),
    (4,'Mehedi Hasan','123456','mehedi@gmail.com','Mirpur, Dhaka','https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d','2026-07-16 14:27:29'),
    (5,'Masud','123456','masud@gmail.com','',NULL,'2026-07-22 13:18:53'),
    (6,'Tanvir Ahmed','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','tanvir@ctrcdemo.test','Raozan, Chattogram',NULL,'2026-08-02 13:15:46'),
    (7,'Sadia Islam','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','sadia@ctrcdemo.test','Pahartali, Raozan',NULL,'2026-08-02 13:15:46'),
    (8,'Rakibul Hasan','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','rakib@ctrcdemo.test','CUET Campus, Raozan',NULL,'2026-08-02 13:15:46'),
    (9,'Nusrat Jahan','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','nusrat@ctrcdemo.test','Noapara, Raozan',NULL,'2026-08-02 13:15:46'),
    (10,'Imran Chowdhury','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','imran@ctrcdemo.test','Hathazari, Chattogram',NULL,'2026-08-02 13:15:46'),
    (11,'Farhana Akter','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','farhana@ctrcdemo.test','Baizid, Chattogram',NULL,'2026-08-02 13:15:46'),
    (12,'Shahriar Kabir','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','shahriar@ctrcdemo.test','Oxygen More, Chattogram',NULL,'2026-08-02 13:15:46'),
    (13,'Mim Akter','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','mim@ctrcdemo.test','Feni Sadar, Feni',NULL,'2026-08-02 13:15:46');

-- 25 geographic points across Chattogram and Dhaka used by the reports below
INSERT INTO location (location_id, longitude, latitude, address, city) VALUES
    (1,91.821900,22.359200,'GEC Circle, CDA Avenue','Chittagong'),
    (2,90.387200,23.756100,'Farmgate, Dhaka','Dhaka'),
    (3,91.970500,22.462700,'CUET Main Gate, Raozan','Chattogram'),
    (4,91.944500,22.488500,'Pahartali Bazar, Raozan','Chattogram'),
    (5,91.930000,22.510000,'Kaptai Road, Raozan','Chattogram'),
    (6,91.960000,22.475000,'Noapara Bus Stand, Raozan','Chattogram'),
    (7,91.809000,22.506400,'Hathazari Bus Stand','Chattogram'),
    (8,91.860000,22.397000,'Baizid Bostami Road','Chattogram'),
    (9,91.834000,22.382000,'Oxygen More','Chattogram'),
    (10,91.666700,22.616700,'Sitakunda Bazar, N1 Highway','Chattogram'),
    (11,91.716700,22.533300,'Kumira, N1 Highway','Chattogram'),
    (12,91.566700,22.766700,'Mirsharai, N1 Highway','Chattogram'),
    (13,91.397600,23.015900,'Mohipal Flyover, Feni','Feni'),
    (14,91.290000,23.220000,'Chauddagram, N1 Highway','Cumilla'),
    (15,91.160000,23.430000,'Padua Bazar, Cumilla','Cumilla'),
    (16,90.720000,23.530000,'Daudkandi Toll Plaza','Cumilla'),
    (17,90.600000,23.530000,'Meghna Bridge, Gazaria','Munshiganj'),
    (18,90.530000,23.700000,'Kanchpur Bridge, Narayanganj','Narayanganj'),
    (19,90.435000,23.710000,'Jatrabari Flyover, Dhaka','Dhaka'),
    (20,91.932500,22.508000,'Kaptai Road, 300m before the crash','Chattogram'),
    (21,91.927000,22.512500,'Kaptai Road, past the crash','Chattogram'),
    (22,91.400500,23.013000,'Mohipal, service road side','Feni'),
    (23,91.394000,23.018500,'Mohipal, Dhaka bound approach','Feni'),
    (24,91.970500,22.462700,'Near CUET Main Gate, Raozan','Chattogram'),
    (25,91.809000,22.506400,'Hathazari Bus Stand','Chattogram');

-- 20 incident reports with their category, evidence type and vote tallies
INSERT INTO report (report_id, user_id, location_id, title, description, category, upvote_count, downvote_count, expires_at, created_at, evidence_type, status, image_url, updated_at, deleted_at) VALUES
    (1,1,1,'Heavy Waterlogging at GEC Circle','Water has accumulated up to knee level near GEC intersection due to continuous rain since morning. Vehicles are moving extremely slowly.','Waterlogging',1,0,'2026-07-16 17:27:29','2026-07-16 14:27:29','seen','unverified',NULL,NULL,NULL),
    (2,6,3,'Deep pothole at CUET main gate','A large pothole has opened right at the main gate turn. Two rickshaws have already tipped over this morning.','Road damage',4,0,'2026-08-05 16:59:20','2026-08-04 12:35:46','seen','verified','https://picsum.photos/seed/ctrcpothole/900/600',NULL,NULL),
    (3,7,4,'Heavy jam at Pahartali Bazar','Bazar day crowd has spilled onto the road, buses are barely moving in either direction.','Traffic jam',3,0,'2026-08-05 16:59:20','2026-08-04 12:50:46','seen','verified',NULL,NULL,NULL),
    (4,8,5,'Truck and CNG collision on Kaptai Road','A sand truck hit a CNG near the Kaptai Road turn. One lane is completely blocked.','Accident',6,0,'2026-08-05 16:59:20','2026-08-04 12:20:46','seen','verified','https://picsum.photos/seed/ctrccrash/900/600',NULL,NULL),
    (5,9,6,'Cars are not moving at Noapara','Long line of stopped vehicles ahead of me. I cannot see what is going on from here.','Unknown',2,0,'2026-08-05 16:59:20','2026-08-04 13:03:46','guessed','unverified',NULL,NULL,NULL),
    (6,10,7,'Water on the road near Hathazari stand','Heard from a bus driver that water has collected up to knee level after last night rain.','Waterlogging',2,0,'2026-08-05 16:59:20','2026-08-04 11:15:46','heard','unverified','https://picsum.photos/seed/ctrcwater/900/600',NULL,NULL),
    (7,11,8,'Road closed for pipeline work','WASA has dug up half the road for a pipeline. Only one lane is open and traffic is being held.','Road block',3,0,'2026-08-05 16:59:20','2026-08-04 10:15:46','seen','verified',NULL,NULL,NULL),
    (8,12,9,'Gridlock at Oxygen More','Very slow moving traffic in all four directions.','Traffic jam',1,4,'2026-08-05 16:59:20','2026-08-05 09:15:46','seen','disputed',NULL,NULL,NULL),
    (9,13,10,'Bus accident near Sitakunda Bazar','A Dhaka bound bus has gone off the shoulder just past the bazar. Police are on site.','Accident',4,0,'2026-08-05 16:59:20','2026-08-04 12:15:46','seen','verified',NULL,NULL,NULL),
    (10,6,11,'Broken road surface at Kumira','The highway surface has cracked badly on the Dhaka bound side, vehicles are swerving.','Road damage',2,0,'2026-08-05 16:59:20','2026-08-05 08:15:46','seen','verified',NULL,NULL,NULL),
    (11,7,12,'Long queue at Mirsharai','Traffic is backed up for about two kilometres on the highway.','Traffic jam',5,0,'2026-08-05 16:59:20','2026-08-04 12:40:46','seen','verified',NULL,NULL,NULL),
    (12,8,13,'Two truck crash at Mohipal Flyover','Two trucks have collided at the foot of the flyover. Both Dhaka bound lanes are blocked.','Accident',7,0,'2026-08-05 16:59:20','2026-08-04 12:25:46','seen','verified','https://picsum.photos/seed/ctrcfeni/900/600',NULL,NULL),
    (13,9,14,'Highway blocked at Chauddagram','My cousin called and said the highway is blocked there because of a local protest.','Road block',5,0,'2026-08-05 16:59:20','2026-08-04 11:45:46','heard','verified',NULL,NULL,NULL),
    (14,10,15,'Waterlogging at Padua Bazar','Rain water has collected across the highway near Padua Bazar. Small cars are struggling.','Waterlogging',3,0,'2026-08-05 16:59:20','2026-08-04 11:15:46','seen','verified',NULL,NULL,NULL),
    (15,11,16,'Toll plaza queue at Daudkandi','Only two toll booths are open so the queue is very long today.','Traffic jam',4,0,'2026-08-05 16:59:20','2026-08-04 12:55:46','seen','verified','https://picsum.photos/seed/ctrctoll/900/600',NULL,NULL),
    (16,12,17,'Everything stopped on Meghna Bridge','Nothing is moving on the bridge. No idea what has happened up ahead.','Unknown',3,0,'2026-08-05 16:59:20','2026-08-04 13:00:46','guessed','unverified',NULL,NULL,NULL),
    (17,13,18,'Accident at Kanchpur Bridge','A covered van has overturned on the approach road. Traffic is being diverted.','Accident',5,0,'2026-08-05 16:59:20','2026-08-04 12:05:46','seen','verified',NULL,NULL,NULL),
    (18,6,19,'Jam under Jatrabari Flyover','Usual evening jam is worse than normal today, barely moving.','Traffic jam',6,0,'2026-08-05 16:59:20','2026-08-04 12:45:46','seen','verified','https://picsum.photos/seed/ctrcdhaka/900/600',NULL,NULL),
    (19,9,24,'Slow traffic near CUET this morning','Vehicles are crawling on the campus road since early morning.','Traffic jam',0,0,'2026-08-05 16:59:20','2026-08-05 02:00:00','seen','unverified',NULL,NULL,NULL),
    (20,10,25,'Road digging near Hathazari stand','One lane has been dug up for repair work, traffic is being held.','Road damage',0,0,'2026-08-05 16:59:20','2026-08-05 04:59:20','seen','unverified',NULL,NULL,NULL);

-- 7 follow-up updates filed against existing reports
INSERT INTO sub_report (sub_report_id, user_id, report_id, location_id, description, dist_from_parent, upvote_count, downvote_count, created_at, evidence_type, category, image_url) VALUES
    (1,2,1,2,'Severe water logging observed at Farmgate metro rail entrance.',242.350,1,0,'2026-07-16 14:27:29','heard','Waterlogging',NULL),
    (2,6,4,20,'Queue now reaches back to the CUET side. Take the village road if you can.',339.718,0,0,'2026-08-04 12:40:46','seen','Accident',NULL),
    (3,7,4,21,'Recovery truck has arrived, one lane may open soon.',415.022,0,0,'2026-08-04 13:00:46','seen','Accident',NULL),
    (4,9,4,20,'Someone at the tea stall said an ambulance already left.',339.718,0,0,'2026-08-04 13:05:46','heard','Accident',NULL),
    (5,13,12,22,'Traffic is being sent through the service road, very slow.',438.261,0,0,'2026-08-04 12:45:46','seen','Accident',NULL),
    (6,10,12,23,'Queue on the Dhaka bound side is about three kilometres now.',468.321,0,0,'2026-08-04 12:57:46','seen','Traffic jam','https://picsum.photos/seed/ctrcupdate/900/600'),
    (7,11,12,22,'Heard on the radio that one lane will open within the hour.',438.261,0,0,'2026-08-04 13:10:46','heard','Accident',NULL);

-- 11 comments written on reports
INSERT INTO comment (comment_id, user_id, report_id, sub_report_id, content, created_at, upvote_count, downvote_count) VALUES
    (1,1,1,NULL,'It has been like this for the last 2 hours. Avoid GEC route if possible.','2026-07-16 14:27:29',0,0),
    (2,2,NULL,1,'The drainage system seems blocked.','2026-07-16 14:27:29',0,0),
    (3,7,2,NULL,'Went through there an hour back, it is worse than it looks in the photo.','2026-08-02 12:45:46',0,0),
    (4,8,2,NULL,'CUET transport office has been informed.','2026-08-02 12:55:46',0,0),
    (5,9,4,NULL,'Is the road open now? I have to reach Raozan by six.','2026-08-02 12:50:46',0,0),
    (6,6,4,NULL,'Still blocked as of ten minutes ago. Use the Noapara side road.','2026-08-02 13:05:46',0,0),
    (7,11,12,NULL,'Left Chattogram at two, stuck here since three. Nothing is moving.','2026-08-02 12:35:46',0,0),
    (8,10,12,NULL,'Police have opened the service road, it is slow but it moves.','2026-08-02 13:03:46',0,0),
    (9,12,8,NULL,'It is normal evening traffic here, not really an incident.','2026-08-02 11:15:46',0,0),
    (10,13,13,NULL,'Confirmed, my bus has been standing here for forty minutes.','2026-08-02 12:30:46',0,0),
    (11,7,16,NULL,'Same here, no idea what is ahead. Someone up front please report.','2026-08-02 13:07:46',0,0);

-- 71 up and down votes cast on reports and updates
INSERT INTO vote (vote_id, user_id, report_id, sub_report_id, vote_type, voted_at) VALUES
    (1,1,1,NULL,'up','2026-07-16 14:27:29'),
    (2,2,NULL,1,'up','2026-07-16 14:27:29'),
    (3,10,2,NULL,'up','2026-08-02 13:15:46'),
    (4,9,2,NULL,'up','2026-08-02 13:15:46'),
    (5,8,2,NULL,'up','2026-08-02 13:15:46'),
    (6,7,2,NULL,'up','2026-08-02 13:15:46'),
    (10,11,3,NULL,'up','2026-08-02 13:15:46'),
    (11,8,3,NULL,'up','2026-08-02 13:15:46'),
    (12,6,3,NULL,'up','2026-08-02 13:15:46'),
    (13,11,4,NULL,'up','2026-08-02 13:15:46'),
    (14,10,4,NULL,'up','2026-08-02 13:15:46'),
    (15,13,4,NULL,'up','2026-08-02 13:15:46'),
    (16,9,4,NULL,'up','2026-08-02 13:15:46'),
    (17,7,4,NULL,'up','2026-08-02 13:15:46'),
    (18,6,4,NULL,'up','2026-08-02 13:15:46'),
    (20,8,5,NULL,'up','2026-08-02 13:15:46'),
    (21,6,5,NULL,'up','2026-08-02 13:15:46'),
    (23,11,6,NULL,'up','2026-08-02 13:15:46'),
    (24,12,6,NULL,'up','2026-08-02 13:15:46'),
    (26,10,7,NULL,'up','2026-08-02 13:15:46'),
    (27,12,7,NULL,'up','2026-08-02 13:15:46'),
    (28,6,7,NULL,'up','2026-08-02 13:15:46'),
    (29,13,8,NULL,'up','2026-08-02 13:15:46'),
    (30,9,8,NULL,'down','2026-08-02 13:15:46'),
    (31,8,8,NULL,'down','2026-08-02 13:15:46'),
    (32,7,8,NULL,'down','2026-08-02 13:15:46'),
    (33,6,8,NULL,'down','2026-08-02 13:15:46'),
    (37,11,9,NULL,'up','2026-08-02 13:15:46'),
    (38,10,9,NULL,'up','2026-08-02 13:15:46'),
    (39,7,9,NULL,'up','2026-08-02 13:15:46'),
    (40,6,9,NULL,'up','2026-08-02 13:15:46'),
    (44,13,10,NULL,'up','2026-08-02 13:15:46'),
    (45,8,10,NULL,'up','2026-08-02 13:15:46'),
    (47,10,11,NULL,'up','2026-08-02 13:15:46'),
    (48,13,11,NULL,'up','2026-08-02 13:15:46'),
    (49,9,11,NULL,'up','2026-08-02 13:15:46'),
    (50,8,11,NULL,'up','2026-08-02 13:15:46'),
    (51,6,11,NULL,'up','2026-08-02 13:15:46'),
    (54,11,12,NULL,'up','2026-08-02 13:15:46'),
    (55,10,12,NULL,'up','2026-08-02 13:15:46'),
    (56,13,12,NULL,'up','2026-08-02 13:15:46'),
    (57,9,12,NULL,'up','2026-08-02 13:15:46'),
    (58,7,12,NULL,'up','2026-08-02 13:15:46'),
    (59,12,12,NULL,'up','2026-08-02 13:15:46'),
    (60,6,12,NULL,'up','2026-08-02 13:15:46'),
    (61,10,13,NULL,'up','2026-08-02 13:15:46'),
    (62,13,13,NULL,'up','2026-08-02 13:15:46'),
    (63,8,13,NULL,'up','2026-08-02 13:15:46'),
    (64,7,13,NULL,'up','2026-08-02 13:15:46'),
    (65,6,13,NULL,'up','2026-08-02 13:15:46'),
    (68,11,14,NULL,'up','2026-08-02 13:15:46'),
    (69,7,14,NULL,'up','2026-08-02 13:15:46'),
    (70,12,14,NULL,'up','2026-08-02 13:15:46'),
    (71,10,15,NULL,'up','2026-08-02 13:15:46'),
    (72,13,15,NULL,'up','2026-08-02 13:15:46'),
    (73,8,15,NULL,'up','2026-08-02 13:15:46'),
    (74,6,15,NULL,'up','2026-08-02 13:15:46'),
    (78,11,16,NULL,'up','2026-08-02 13:15:46'),
    (79,9,16,NULL,'up','2026-08-02 13:15:46'),
    (80,7,16,NULL,'up','2026-08-02 13:15:46'),
    (81,10,17,NULL,'up','2026-08-02 13:15:46'),
    (82,8,17,NULL,'up','2026-08-02 13:15:46'),
    (83,7,17,NULL,'up','2026-08-02 13:15:46'),
    (84,12,17,NULL,'up','2026-08-02 13:15:46'),
    (85,6,17,NULL,'up','2026-08-02 13:15:46'),
    (88,11,18,NULL,'up','2026-08-02 13:15:46'),
    (89,10,18,NULL,'up','2026-08-02 13:15:46'),
    (90,13,18,NULL,'up','2026-08-02 13:15:46'),
    (91,9,18,NULL,'up','2026-08-02 13:15:46'),
    (92,8,18,NULL,'up','2026-08-02 13:15:46'),
    (93,7,18,NULL,'up','2026-08-02 13:15:46');

-- 3 bookmarked reports
INSERT INTO saved_report (saved_report_id, user_id, report_id, saved_at) VALUES
    (1,3,4,'2026-08-02 13:15:46'),
    (2,3,12,'2026-08-02 13:15:46'),
    (3,3,15,'2026-08-02 13:15:46');
