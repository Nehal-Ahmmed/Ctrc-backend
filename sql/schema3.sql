-- Mock data generation script for testing spatial queries (5km, 10km, 15km)
-- Assumed Base Location (Center): Latitude: 22.4621, Longitude: 91.9729 (CUET, Chittagong)

-- 1. Insert Mock Users
INSERT INTO user (name, password, email, address) VALUES 
('Alice Reporter', 'hashed_pass', 'alice_cuet@test.com', 'Kazi Nazrul Hall, CUET'),
('Bob Helper', 'hashed_pass', 'bob_cuet@test.com', 'Pahartali Bazar, Raozan'),
('Charlie Observer', 'hashed_pass', 'charlie_cuet@test.com', 'CUET Gate');

-- 2. Insert Mock Locations 
-- Let's use variables for safety.

SET @user1 = (SELECT user_id FROM user WHERE email = 'alice_cuet@test.com' LIMIT 1);
SET @user2 = (SELECT user_id FROM user WHERE email = 'bob_cuet@test.com' LIMIT 1);
SET @user3 = (SELECT user_id FROM user WHERE email = 'charlie_cuet@test.com' LIMIT 1);

-- Point A: ~3 km North (Inside 5km)
INSERT INTO location (latitude, longitude, address, city) VALUES (22.4891, 91.9729, '3km North of CUET', 'Chittagong');
SET @loc_3km = LAST_INSERT_ID();

-- Point B: ~5 km East (On the edge of 5km)
INSERT INTO location (latitude, longitude, address, city) VALUES (22.4621, 92.0217, '5km East of CUET', 'Chittagong');
SET @loc_5km = LAST_INSERT_ID();

-- Point C: ~10 km South-West (Outside 5km, inside 15km)
INSERT INTO location (latitude, longitude, address, city) VALUES (22.3991, 91.9046, '10km SW of CUET', 'Chittagong');
SET @loc_10km = LAST_INSERT_ID();

-- Point D: ~15 km North-East (Outside 5km and 10km)
INSERT INTO location (latitude, longitude, address, city) VALUES (22.5612, 92.0704, '15km NE of CUET', 'Chittagong');
SET @loc_15km = LAST_INSERT_ID();

-- 3. Insert Reports at these locations
INSERT INTO report (user_id, location_id, title, description, category, upvote_count, downvote_count) VALUES 
(@user1, @loc_3km, 'Pothole near CUET', 'Dangerous pothole on the main road', 'Road Hazard', 12, 1),
(@user2, @loc_5km, 'Accident near Pahartali', 'Two cars crashed, blocking traffic', 'Accident', 45, 0),
(@user3, @loc_10km, 'Waterlogging at 10km', 'Heavy rain caused waterlogging', 'Weather', 5, 2),
(@user1, @loc_15km, 'Traffic Jam far away', 'Huge gridlock', 'Traffic', 20, 3);

-- 4. Insert some Comments to test the comment aggregation feature
-- Let's add comments to the first report (3km away)
SET @report_3km = (SELECT report_id FROM report WHERE title = 'Pothole near CUET' LIMIT 1);
SET @report_5km = (SELECT report_id FROM report WHERE title = 'Accident near Pahartali' LIMIT 1);

INSERT INTO comment (user_id, report_id, content) VALUES 
(@user2, @report_3km, 'I saw this too, it is very deep!'),
(@user3, @report_3km, 'Thanks for reporting, driving carefully.'),
(@user1, @report_5km, 'Is the road clear now?');

-- 5. (Optional) Run this select to verify distances manually from the DB
-- SELECT title, ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(91.9729, 22.4621)) AS distance_in_meters
-- FROM report r JOIN location l ON r.location_id = l.location_id;
