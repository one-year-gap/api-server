ALTER TABLE member
    DROP CONSTRAINT IF EXISTS ck_member_info_required;

ALTER TABLE member
    ADD CONSTRAINT ck_member_info_required CHECK (
        role <> 'CUSTOMER'
        OR type <> 'FORM'
        OR (
            phone IS NOT NULL
            AND birth_date IS NOT NULL
            AND gender IS NOT NULL
            AND address_id IS NOT NULL
            AND membership IS NOT NULL
        )
    );
ALTER TABLE member ALTER COLUMN name TYPE VARCHAR(100);