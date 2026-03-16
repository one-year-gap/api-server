-- log-features 등에서 "없으면 생성" 시 새 스냅샷 ID 발급용 시퀀스
CREATE SEQUENCE IF NOT EXISTS feature_snapshot_id_seq;

-- 기존 행과 충돌하지 않도록 시퀀스 현재값을 기존 MAX(feature_snapshot_id) 다음으로 설정
SELECT setval(
    'feature_snapshot_id_seq',
    (SELECT COALESCE(MAX(feature_snapshot_id), 0) + 1 FROM feature_snapshot_store)
);
