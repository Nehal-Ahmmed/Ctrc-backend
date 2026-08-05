set sql_safe_updates = 0;

alter table report
  add column image_url varchar(500) null;

alter table sub_report
  add column image_url varchar(500) null;

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

select report_id, title, category, status,
       if(image_url is null, 'no photo', 'has photo') as photo
  from report
 order by report_id;
