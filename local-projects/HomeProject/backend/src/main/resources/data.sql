MERGE INTO universities u
USING (
    SELECT 1 AS university_id,
           '삼육대학교' AS university_name,
           'syu.ac.kr' AS school_email_domain,
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

MERGE INTO app_users au
USING (
    SELECT 1 AS user_id,
           1 AS university_id,
           'firebase-eojisung-0001' AS firebase_uid,
           'ejs5565@syu.ac.kr' AS email,
           'eojisung' AS name,
           'eojisung' AS nickname,
           '01055393786' AS phone,
           1 AS is_email_verified,
           'ACTIVE' AS account_status
    FROM dual
) src
ON (au.user_id = src.user_id)
WHEN MATCHED THEN
    UPDATE SET
        au.university_id = src.university_id,
        au.firebase_uid = src.firebase_uid,
        au.email = src.email,
        au.name = src.name,
        au.nickname = src.nickname,
        au.phone = src.phone,
        au.is_email_verified = src.is_email_verified,
        au.account_status = src.account_status
WHEN NOT MATCHED THEN
    INSERT (user_id, university_id, firebase_uid, email, name, nickname, phone, is_email_verified, account_status)
    VALUES (src.user_id, src.university_id, src.firebase_uid, src.email, src.name, src.nickname, src.phone, src.is_email_verified, src.account_status);

-- SERVICE_ZONES 데이터는 초기 시드에서 직접 입력하지 않는다.
-- TAXI_DB에 이미 저장된 zone 데이터를 그대로 조회해 인증 구역으로 활용한다.
