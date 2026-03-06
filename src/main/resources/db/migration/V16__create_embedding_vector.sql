-- 1. pgvector 확장 모듈 활성화 (DB당 최초 1회 실행 필요)
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. product 테이블에 임베딩 텍스트 및 벡터 컬럼 추가
ALTER TABLE product 
ADD COLUMN embedding_text TEXT,           -- 임베딩의 근거가 되는 원문 텍스트
ADD COLUMN embedding_vector VECTOR(1536);          -- OpenAI text-embedding-3-small 규격 (1536차원)