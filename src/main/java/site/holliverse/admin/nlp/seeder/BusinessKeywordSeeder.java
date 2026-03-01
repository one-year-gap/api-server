package site.holliverse.admin.nlp.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.admin.nlp.dao.BusinessKeywordSeedingDao;
import site.holliverse.admin.nlp.util.KeywordNormalizer;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessKeywordSeeder implements ApplicationRunner {

    private final ObjectMapper objectMapper;
    private final KeywordNormalizer normalizer;
    private final BusinessKeywordSeedingDao seedingDao;

    @Override
    @Transactional // 도중에 에러나면 전체 롤백되도록 트랜잭션
    public void run(ApplicationArguments args) {

        // 1. 이미 DB에 데이터가 있다면 시딩 패스 (서버 켤 때마다 반복 삽입 방지)
        if (seedingDao.hasData()) {
            log.info("[Data Seeding] 비즈니스 키워드 데이터가 이미 존재합니다. Seeding을 건너뜁니다.");
            return;
        }

        log.info("[Data Seeding] 비즈니스 키워드 JSON 데이터를 DB에 삽입합니다...");

        // 2. JSON 파일 읽어오기
        try (InputStream inputStream = new ClassPathResource("business_keyword_alias_map.json").getInputStream()) {

            // JSON 데이터를 Map으로 변환
            Map<String, List<String>> keywordMap = objectMapper.readValue(
                    inputStream, new TypeReference<>() {}
            );

            int index = 1; // 키워드 코드 생성용 번호 (1부터 시작)

            // 3. Map을 돌면서 DB에 순서대로 Insert
            for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
                String keywordName = entry.getKey(); // 예: "모바일"
                List<String> rawAliases = entry.getValue(); // 예: ["휴대폰", "핸드폰", ...]

                // 키워드 코드 자동 생성 (예: BK_001, BK_002 ...)
                String keywordCode = String.format("BK_%03d", index++);

                // 마스터 테이블에 저장하고 ID 받아오기
                Long keywordId = seedingDao.saveKeywordAndGetId(keywordCode, keywordName);

                // "모바일"이라는 카테고리 이름 자체도 검색되도록 별칭 목록에 추가 (중복 방지를 위해 Set 사용)
                Set<String> allAliases = new HashSet<>(rawAliases);
                allAliases.add(keywordName);

                // 별칭 테이블에 정규화해서 저장
                for (String rawAlias : allAliases) {
                    // 원문 텍스트 정규화 진행 (공백, 특수문자 제거)
                    String normalizedAlias = normalizer.normalize(rawAlias);

                    // DB 저장
                    seedingDao.saveAlias(keywordId, rawAlias, normalizedAlias);
                }
            }
            log.info("[Data Seeding] 총 {}개의 비즈니스 키워드 마스터 데이터 세팅 완료!", index - 1);

        } catch (Exception e) {
            log.error("[Data Seeding] JSON Seeding 중 에러 발생: ", e);
            throw new RuntimeException("Data Seeding 실패", e); // 에러 발생 시 스프링 구동 중단
        }
    }
}