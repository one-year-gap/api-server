package site.holliverse.shared.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

/**
 * ==========================
 * 캐쉬 컨피그 파일
 *
 * @author nonstop
 * @version 1.0.0
 * @since 2026-04-04
 * ==========================
 */

@Configuration
public class CacheConfig {

    public static final String REGIONAL_METRICS_CACHE = "regionalMetrics";
    public static final String REGIONAL_TOP_PLANS_CACHE = "regionalTopPlans";

    /**
     * 스프링이 사용할 캐시매니저 빈 등록
     * 각 캐시의 만료시간, 최대 크기를 application.yaml에서 주입받는다.
     * 값이 없으면 기본 값으로 설정
     */
    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.regional-metrics.spec:maximumSize=50,expireAfterWrite=5m}") String regionalMetricsSpec,
            @Value("${app.cache.regional-top-plans.spec:maximumSize=10,expireAfterWrite=5m}") String regionalTopPlansSpec
    ) {

        /**
         * 관리자 통계 쪽 전용 캐시 매니저 생성
         * caffeine 정책으로 적용
         * 별도 TTL/사이즈 정책으로 가질 수 잇음.
         */
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        var regionalMetricsCache = new CaffeineCacheManager(REGIONAL_METRICS_CACHE);
        regionalMetricsCache.setCaffeine(Caffeine.from(regionalMetricsSpec));

        var regionalTopPlansCache = new CaffeineCacheManager(REGIONAL_TOP_PLANS_CACHE);
        regionalTopPlansCache.setCaffeine(Caffeine.from(regionalTopPlansSpec));

        /**
         * 스프링에서는 하나의 캐시매니저만 등록해야되기때문에 여러 캐시매니저를 묶어서 반환.
         * 캐시 이름에 따라 적절한 매니저를 찾아주는 역할
         */
        return new CompositeCacheManager(
                regionalMetricsCache,
                regionalTopPlansCache
        );
    }


    /**
     * 여러개의 캐시 매니저를 보관하는 래퍼클래스
     * delegates 는 실제로 위에서 만든 캐시를 보관하는 매니저 목록.
     */
    private static final class CompositeCacheManager implements CacheManager {
        private final List<CacheManager> delegates;

        /**
         * 생성자에서 전달받은 캐시 매니저들을 리스트로 저장.
         */
        private CompositeCacheManager(CacheManager... delegates) {
            this.delegates = List.of(delegates);
        }


        @Override
        public Cache getCache(String name) {
            for (CacheManager delegate : delegates) {
                Cache cache = delegate.getCache(name);
                if (cache != null) {
                    return cache;
                }
            }
            return null;
        }

        /**
         * 현재 등록된 전체 캐시 이름 목록을 합쳐서 반환
         */
        @Override
        public Collection<String> getCacheNames() {
            return delegates.stream()
                    .flatMap(delegate -> delegate.getCacheNames().stream())
                    .toList();
        }
    }
}
