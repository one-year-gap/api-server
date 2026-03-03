DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'db_migrator') THEN
        CREATE ROLE db_migrator WITH LOGIN PASSWORD 'db_migrator_pw';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'app_customer') THEN
        CREATE ROLE app_customer WITH LOGIN PASSWORD 'app_customer_pw';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'app_admin') THEN
        CREATE ROLE app_admin WITH LOGIN PASSWORD 'app_admin_pw';
    END IF;
END
$$;

ALTER SCHEMA public OWNER TO db_migrator;
GRANT ALL ON SCHEMA public TO db_migrator;

GRANT CONNECT ON DATABASE holliverse TO db_migrator;
GRANT CONNECT ON DATABASE holliverse TO app_customer;
GRANT CONNECT ON DATABASE holliverse TO app_admin;

GRANT USAGE ON SCHEMA public TO app_customer;
GRANT USAGE ON SCHEMA public TO app_admin;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_customer;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_customer;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_admin;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE db_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_customer;
ALTER DEFAULT PRIVILEGES FOR ROLE db_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO app_customer;

ALTER DEFAULT PRIVILEGES FOR ROLE db_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_admin;
ALTER DEFAULT PRIVILEGES FOR ROLE db_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO app_admin;
