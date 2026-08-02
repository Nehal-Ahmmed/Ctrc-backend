-- CTRC schema7 : photos on reports
-- Run this after schema5.sql. Run it on the same database the app talks to.

set sql_safe_updates = 0;

-- ---------------------------------------------------------------------------
-- STEP 1 : one photo per report, same idea as user.image_url
-- The image itself lives on Cloudinary, only the link is stored here.
-- ---------------------------------------------------------------------------

alter table report
  add column image_url varchar(500) null;

alter table sub_report
  add column image_url varchar(500) null;

-- ---------------------------------------------------------------------------
-- STEP 2 : placeholder photos on some of the demo reports, so the feed shows
-- a mix of posts with and without a picture.
--
-- These are stand-in images. Before the presentation, open two or three of
-- these reports in the app and upload a real photo instead.
-- ---------------------------------------------------------------------------

update report r
  join location l on l.location_id = r.location_id
   set r.image_url = case l.address
        when 'CUET Main Gate, Raozan'       then 'https://picsum.photos/seed/ctrcpothole/900/600'
        when 'Kaptai Road, Raozan'          then 'https://picsum.photos/seed/ctrccrash/900/600'
        when 'Hathazari Bus Stand'          then 'https://picsum.photos/seed/ctrcwater/900/600'
        when 'Mohipal Flyover, Feni'        then 'https://picsum.photos/seed/ctrcfeni/900/600'
        when 'Daudkandi Toll Plaza'         then 'https://picsum.photos/seed/ctrctoll/900/600'
        when 'Jatrabari Flyover, Dhaka'     then 'https://picsum.photos/seed/ctrcdhaka/900/600'
       end
 where l.address in ('CUET Main Gate, Raozan', 'Kaptai Road, Raozan',
                     'Hathazari Bus Stand', 'Mohipal Flyover, Feni',
                     'Daudkandi Toll Plaza', 'Jatrabari Flyover, Dhaka');

update sub_report s
  join location l on l.location_id = s.location_id
   set s.image_url = 'https://picsum.photos/seed/ctrcupdate/900/600'
 where l.address = 'Mohipal, Dhaka bound approach';

-- ---------------------------------------------------------------------------
-- STEP 3 : check
-- ---------------------------------------------------------------------------

select report_id, title, category, status,
       if(image_url is null, 'no photo', 'has photo') as photo
  from report
 order by report_id;
