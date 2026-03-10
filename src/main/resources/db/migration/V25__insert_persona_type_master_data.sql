INSERT INTO persona_type (
    character_name,
    version,
    is_active,
    short_desc,
    character_description,
    tags
) VALUES
(
    'SPACE_SHERLOCK',
    1,
    TRUE,
    '1원 단위의 누수도 허락하지 않는다!',
    '결제일 전날 데이터 10MB 미만일 때 카타르시스를 느끼는 자비 없는 최적화의 달인입니다.',
    ARRAY['분석', '예리함', '최적화']
),
(
    'SPACE_GRAVITY',
    1,
    TRUE,
    '우리는 뭉쳐야 싸고, 흩어지면 비싸다.',
    '명절에 친척 모이면 결합 묶을 궁리부터! 가족 인터넷 끊길까 봐 해지 못하는 든든한 수호자입니다.',
    ARRAY['가족', '중심', '이끌림(인력)', '든든함']
),
(
    'SPACE_OCTOPUS',
    1,
    TRUE,
    '기기는 늘어날수록 좋다! (익익선)',
    '카페 갈 때 노트북, 태블릿, 워치 풀소유는 기본. 데이터 쉐어링은 숨 쉬는 공기 같은 존재입니다.',
    ARRAY['멀티태스킹', '연결', '긱(Geek)']
),
(
    'SPACE_SURFER',
    1,
    TRUE,
    '요금제는 콘텐츠를 담는 그릇일 뿐!',
    '지하철에서 넷플릭스 최신작 무조건 정주행! 통신사 혜택 구독권은 절대 놓치지 않습니다.',
    ARRAY['힙함', '즐거움', '콘텐츠', '속도감']
),
(
    'SPACE_GUARDIAN',
    1,
    TRUE,
    '나의 우주(개인정보)는 내가 지킨다!',
    '인터넷 관련 직업이나 취미를 가져서 보안을 매우 신경 쓰는 철저한 방어형 탐험가입니다.',
    ARRAY['철벽', '안전', '사이버 보안', '듬직함']
),
(
    'SPACE_EXPLORER',
    1,
    TRUE,
    '복잡한 건 질색! 내 요금제는 늘 평화롭다.',
    '우주를 부유하듯 요금제 비교나 변경 없이 현재 상태를 가장 편안하게 여기는 귀여운 평화주의자입니다.',
    ARRAY['평화', '느긋함', '둥둥~', '무념무상']
)
ON CONFLICT (character_name, version)
DO UPDATE SET
    is_active = EXCLUDED.is_active,
    short_desc = EXCLUDED.short_desc,
    character_description = EXCLUDED.character_description,
    tags = EXCLUDED.tags,
    updated_at = NOW();