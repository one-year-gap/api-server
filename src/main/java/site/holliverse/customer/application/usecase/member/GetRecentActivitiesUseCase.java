package site.holliverse.customer.application.usecase.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.holliverse.customer.persistence.entity.ProductViewHistory;
import site.holliverse.customer.persistence.repository.ProductViewHistoryRepository;
import site.holliverse.shared.alert.AlertOwner;
import site.holliverse.shared.logging.SystemLogEvent;

import java.util.List;

@Service
@Profile("customer")
public class GetRecentActivitiesUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetRecentActivitiesUseCase.class);

    private final ProductViewHistoryRepository productViewHistoryRepository;
    private final ObjectMapper objectMapper;

    public GetRecentActivitiesUseCase(ProductViewHistoryRepository productViewHistoryRepository,
                                      ObjectMapper objectMapper) {
        this.productViewHistoryRepository = productViewHistoryRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    @SystemLogEvent("customer.member.recent-activities")
    @AlertOwner("hy") 
    public RecentActivityResult execute(Long memberId, int limit) { //limit으로 상위 N개만 조회 
        Pageable pageable = PageRequest.of(0, limit);

        List<ProductViewHistory> histories =
                productViewHistoryRepository.findRecentByMemberId(memberId, pageable);

        List<RecentActivityResult.ActivityItem> items = histories.stream()
                .map(history -> new RecentActivityResult.ActivityItem(
                        history.getProductId(),
                        history.getProductName(),
                        history.getProductType(),
                        deserializeTags(history.getTags()),
                        history.getViewedAt()
                ))
                .toList();

        return new RecentActivityResult(items);
    }

    private List<String> deserializeTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) { //역직렬화 실패시 warn 로그 출력 후 빈 리스트 반환
            log.warn("상품 조회 히스토리 태그 역직렬화에 실패했습니다. tagsJson={}", tagsJson, e);
            return List.of();
        }
    }
}

