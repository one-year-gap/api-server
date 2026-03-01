package site.holliverse.admin.nlp.util;

import org.springframework.stereotype.Component;

@Component
public class KeywordNormalizer {

    /**
     * 원문 텍스트를 DB 비교용(alias_norm) 정규화 텍스트로 변환
     */
    public String normalize(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        // 1. 소문자 통일 (예: U+tv -> u+tv, VIP -> vip)
        String normalized = rawText.toLowerCase();

        // 2. 특수문자 및 공백 제거 (한글, 영문 소문자, 숫자만 남김)
        // [^a-z0-9가-힣] : 이 조건에 맞지 않는 모든 문자(공백 포함)를 ""로 지워버림
        normalized = normalized.replaceAll("[^a-z0-9가-힣]", "");

        return normalized;
    }
}