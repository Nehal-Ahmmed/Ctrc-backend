use ctrcdb;

insert into user (name, password, email, address, image_url) values ('Nehal Ahmmed', '123456', 'nehal@gmail.com', 'GEC Circle, Chittagong', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde');

insert into location (longitude, latitude, address, city) values (91.821900, 22.359200, 'GEC Circle, CDA Avenue', 'Chittagong');

insert into report (user_id, location_id, title, description, category, upvote_count, downvote_count, expires_at) values (1, 1, 'Heavy Waterlogging at GEC Circle', 'Water has accumulated up to knee level near GEC intersection due to continuous rain since morning. Vehicles are moving extremely slowly.', 'Waterlogging', 3, 0, date_add(now(), interval 3 hour));

insert into comment (user_id, report_id, sub_report_id, content) values (1, 1, null, 'It has been like this for the last 2 hours. Avoid GEC route if possible.');

insert into vote (user_id, report_id, sub_report_id, vote_type) values (1, 1, null, 'up');
