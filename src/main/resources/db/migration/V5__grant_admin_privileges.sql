-- 1. 관리자(app_admin)가 public 스키마를 사용할 수 있게 허용
GRANT USAGE ON SCHEMA public TO app_admin;

-- 2. 현재 만들어진 모든 테이블에 대해 조회/수정/삭제 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_admin;

-- 3. 시퀀스(ID 자동 증가) 사용 권한 부여
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_admin;

-- 4. 앞으로 db_migrator가 새로 만들 테이블에도 자동으로 권한이 들어가도록 설정
ALTER DEFAULT PRIVILEGES FOR ROLE db_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE db_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO app_admin;