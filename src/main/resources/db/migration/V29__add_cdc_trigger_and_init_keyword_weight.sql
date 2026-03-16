-- 1. 상담 데이터 실시간 감지(CDC)를 위한 LISTEN/NOTIFY 트리거 및 함수 설정
-- (1) 알림을 쏘는 공용 함수 (Trigger Function)
CREATE OR REPLACE FUNCTION notify_support_case_event()
RETURNS TRIGGER AS $$
BEGIN
    -- NEW는 방금 INSERT/UPDATE된 새로운 데이터 행(Row)을 의미
    PERFORM pg_notify('support_case_channel', NEW.case_id::text);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- (2) support_case 테이블 전용 트리거 부착
DROP TRIGGER IF EXISTS trigger_support_case_notify ON support_case;
CREATE TRIGGER trigger_support_case_notify
AFTER INSERT OR UPDATE ON support_case
FOR EACH ROW
EXECUTE FUNCTION notify_support_case_event();


-- 2. 비즈니스 키워드(business_keyword) 이탈 가중치(negative_weight) 초기화
-- (1) 이탈 위험도 HIGH (가중치 20점)
-- 대상: 번호이동 문의(41), 위약금 조회(44), 회선이전(45)
UPDATE business_keyword
SET negative_weight = 20
WHERE business_keyword_id IN (41, 44, 45);

-- (2) 이탈 위험도 MEDIUM (가중치 10점)
-- 대상: 개인정보침해신고(9), 일시정지(18), 통화품질(20), 명의 도용(38), 속도변경 요청(46), 앱오류(56)
UPDATE business_keyword
SET negative_weight = 10
WHERE business_keyword_id IN (9, 18, 20, 38, 46, 56);