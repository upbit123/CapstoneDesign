MERGE INTO universities u
USING (
    SELECT 1 AS university_id,
           '삼육대학교' AS university_name,
           'syuin.ac.kr' AS school_email_domain,
           37.6425990 AS latitude,
           127.1056750 AS longitude,
           2000 AS boundary_radius_m
    FROM dual
) src
ON (u.university_id = src.university_id)
WHEN MATCHED THEN
    UPDATE SET
        u.university_name = src.university_name,
        u.school_email_domain = src.school_email_domain,
        u.latitude = src.latitude,
        u.longitude = src.longitude,
        u.boundary_radius_m = src.boundary_radius_m
WHEN NOT MATCHED THEN
    INSERT (university_id, university_name, school_email_domain, latitude, longitude, boundary_radius_m)
    VALUES (src.university_id, src.university_name, src.school_email_domain, src.latitude, src.longitude, src.boundary_radius_m);

MERGE INTO service_zones sz
USING (
    SELECT 1 AS zone_id,
           1 AS university_id,
           '삼육대학교 정문' AS zone_name,
           'SCHOOL' AS zone_type,
           37.6428200 AS latitude,
           127.1059700 AS longitude,
           120 AS radius_m,
           1 AS is_active
    FROM dual
    UNION ALL
    SELECT 2 AS zone_id,
           1 AS university_id,
           '삼육대학교 후문' AS zone_name,
           'SCHOOL' AS zone_type,
           37.6415600 AS latitude,
           127.1041400 AS longitude,
           120 AS radius_m,
           1 AS is_active
    FROM dual
    UNION ALL
    SELECT 3 AS zone_id,
           1 AS university_id,
           '화랑대역 1번 출구' AS zone_name,
           'STATION' AS zone_type,
           37.6192200 AS latitude,
           127.0847600 AS longitude,
           150 AS radius_m,
           1 AS is_active
    FROM dual
    UNION ALL
    SELECT 4 AS zone_id,
           1 AS university_id,
           '태릉입구역 6번 출구' AS zone_name,
           'STATION' AS zone_type,
           37.6180100 AS latitude,
           127.0759200 AS longitude,
           150 AS radius_m,
           1 AS is_active
    FROM dual
    UNION ALL
    SELECT 5 AS zone_id,
           1 AS university_id,
           '기숙사 앞 탑승존' AS zone_name,
           'CUSTOM' AS zone_type,
           37.6437300 AS latitude,
           127.1066800 AS longitude,
           80 AS radius_m,
           1 AS is_active
    FROM dual
) src
ON (sz.zone_id = src.zone_id)
WHEN MATCHED THEN
    UPDATE SET
        sz.university_id = src.university_id,
        sz.zone_name = src.zone_name,
        sz.zone_type = src.zone_type,
        sz.latitude = src.latitude,
        sz.longitude = src.longitude,
        sz.radius_m = src.radius_m,
        sz.is_active = src.is_active
WHEN NOT MATCHED THEN
    INSERT (zone_id, university_id, zone_name, zone_type, latitude, longitude, radius_m, is_active)
    VALUES (src.zone_id, src.university_id, src.zone_name, src.zone_type, src.latitude, src.longitude, src.radius_m, src.is_active);
