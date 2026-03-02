-- address PK 중복(시퀀스 드리프트) 방지:
-- 현재 데이터 최대 address_id 기준으로 identity 시퀀스를 재동기화한다.
DO
$$
DECLARE
    seq_name TEXT;
    max_id BIGINT;
    has_rows BOOLEAN;
BEGIN
    SELECT pg_get_serial_sequence('public.address', 'address_id')
    INTO seq_name;

    IF seq_name IS NULL THEN
        RAISE NOTICE 'address identity sequence not found. skip sequence sync.';
        RETURN;
    END IF;

    SELECT COALESCE(MAX(address_id), 1), COUNT(*) > 0
    INTO max_id, has_rows
    FROM public.address;

    PERFORM setval(seq_name, max_id, has_rows);
END
$$;
