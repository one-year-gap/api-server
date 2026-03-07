-- 기존 FAMILY_CARE(가족케어) 중 보안/인증 관련 서비스를 SECURITY로 분리 (성능 최적화 Join 방식)
UPDATE addon_service
SET addon_type = 'SECURITY'
FROM product
WHERE addon_service.product_id = product.product_id
  AND product.product_code IN (
    'ADDON_FAM_001', -- 딥페이크안심보호
    'ADDON_FAM_003', -- 스팸차단후후
    'ADDON_FAM_007', -- 스마트안티피싱
    'ADDON_FAM_008', -- 휴대폰번호 보호서비스
    'ADDON_FAM_009', -- 스마트피싱보호
    'ADDON_FAM_010', -- 오토콜
    'ADDON_FAM_011', -- 원키퍼
    'ADDON_FAM_012', -- 금융사고안심팩
    'ADDON_FAM_013', -- 로그인플러스
    'ADDON_FAM_014', -- 전화번호안심로그인
    'ADDON_FAM_015', -- 간편결제매니저
    'ADDON_FAM_016', -- 모션키
    'ADDON_FAM_017', -- 유비키인증서
    'ADDON_FAM_018', -- 모바일안전결제(ISP)
    'ADDON_FAM_019', -- USIM 스마트인증(라온시큐어)
    'ADDON_FAM_020', -- 스마트공동인증(드림시큐리티)
    'ADDON_FAM_021', -- 스마트 안전결제
    'ADDON_FAM_022'  -- MyOTP
);