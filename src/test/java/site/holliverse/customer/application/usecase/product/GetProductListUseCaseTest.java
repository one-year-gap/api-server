package site.holliverse.customer.application.usecase.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import site.holliverse.customer.application.usecase.dto.ProductSummaryDto;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.repository.AddonRepository;
import site.holliverse.customer.persistence.repository.InternetRepository;
import site.holliverse.customer.persistence.repository.IptvRepository;
import site.holliverse.customer.persistence.repository.MobilePlanRepository;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.SubscriptionRepository;
import site.holliverse.customer.persistence.repository.TabWatchPlanRepository;
import site.holliverse.shared.domain.model.ProductType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductListUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private MobilePlanRepository mobilePlanRepository;
    @Mock
    private InternetRepository internetRepository;
    @Mock
    private IptvRepository iptvRepository;
    @Mock
    private AddonRepository addonRepository;
    @Mock
    private TabWatchPlanRepository tabWatchPlanRepository;

    @InjectMocks
    private GetProductListUseCase getProductListUseCase;

    private static Product productWithId(Long id) {
        Product p = org.mockito.Mockito.mock(Product.class);
        when(p.getProductId()).thenReturn(id);
        return p;
    }

    @Nested
    @DisplayName("category 검증")
    class CategoryValidation {

        @Test
        @DisplayName("카테고리가 비어있으면 카테고리는 필수입니다 메시지를 던진다.")
        void throwsWhenCategoryIsNull() {
            //given
            String invalidCategory = " ";
            //when & then
            assertThatThrownBy(() -> getProductListUseCase.execute(invalidCategory, 0, 10, 0))
                    .isInstanceOf(IllegalArgumentException.class) //기존 에러타입 변경
                    .hasMessageContaining("카테고리는 필수입니다.");
            verify(productRepository, never()).findByProductType(any(), any());
        }

        @Test
        @DisplayName("category가 지원하지 않는 카테고리일 경우, 예외가 발생한다")
        void throwsWhenCategoryIsNotSupported() {

            // given: 지원하지 않는 임의의 카테고리 키워드
            String unknownCategory = "invalid-type";
            int page = 0;
            int size = 10;
            // When & Then: 예외 발생 여부와 메시지를 함께 검증
            assertThatThrownBy(() -> getProductListUseCase.execute(unknownCategory, page, size, 0))
                    .isInstanceOf(IllegalArgumentException.class) // 현재 코드 스펙에 맞춤
                    .hasMessageContaining("지원하지 않는 카테고리입니다: " + unknownCategory);

            // 추가 검증: 예외가 발생했으므로 DB 조회(Repository)가 절대 호출되지 않아야 함
            verify(productRepository, never()).findByProductType(any(), any());
        }
    }

    @Nested
    @DisplayName("카테고리별 조회 - 타입별 상세 repo 호출 검증")
    class CategoryPerDetailRepo {

        @Test
        @DisplayName("mobile 조회 시 MobilePlanRepository만 호출")
        void mobile_callsOnlyMobilePlanRepository() {
            //given: 테스트 데이터 및 Mock 환경 설정
            Product p = productWithId(1L);
            Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

            when(productRepository.findByProductType(eq(ProductType.MOBILE_PLAN), any(Pageable.class))).thenReturn(page);// eq 감싸기

            when(mobilePlanRepository.findByProductIdIn(anyList())).thenReturn(List.of());

            //when: 실제 로직 실행
            getProductListUseCase.execute("mobile", 0, 10, 0);

            //then: 상세 repo 호출 여부 검증
            verify(mobilePlanRepository).findByProductIdIn(List.of(1L));
            verify(internetRepository, never()).findByProductIdIn(any());
        }

        @Test
        @DisplayName("internet 조회 시 InternetRepository만 호출")
        void internet_callsOnlyInternetRepository() {
            //given: 테스트 데이터 및 Mock 환경 설정
            Product p = productWithId(2L);
            Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

            when(productRepository.findByProductType(eq(ProductType.INTERNET), any(Pageable.class))).thenReturn(page);
            when(internetRepository.findByProductIdIn(anyList())).thenReturn(List.of());

            //when: 실제 로직 실행
            getProductListUseCase.execute("internet", 0, 10, 0);

            //then: 상세 repo 호출 여부 검증
            verify(internetRepository).findByProductIdIn(List.of(2L));
            verify(mobilePlanRepository, never()).findByProductIdIn(any());
        }

        @Test
        @DisplayName("iptv 조회 시 IptvRepository만 호출")
        void iptv_callsOnlyIptvRepository() {
            //given: 테스트 데이터 및 Mock 환경 설정
            Product p = productWithId(3L);
            Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

            when(productRepository.findByProductType(eq(ProductType.IPTV), any(Pageable.class))).thenReturn(page);
            when(iptvRepository.findByProductIdIn(anyList())).thenReturn(List.of());

            //when: 실제 로직 실행
            getProductListUseCase.execute("iptv", 0, 10, 0);

            //then: 상세 repo 호출 여부 검증
            verify(iptvRepository).findByProductIdIn(List.of(3L));
            verify(mobilePlanRepository, never()).findByProductIdIn(any());
        }

        @Test
        @DisplayName("add-on 조회 시 AddonRepository만 호출")
        void addon_callsOnlyAddonRepository() {
            //given: 테스트 데이터 및 Mock 환경 설정
            Product p = productWithId(4L);
            Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

            when(productRepository.findByProductType(eq(ProductType.ADDON), any(Pageable.class))).thenReturn(page);
            when(addonRepository.findByProductIdIn(anyList())).thenReturn(List.of());

            //when: 실제 로직 실행
            getProductListUseCase.execute("add-on", 0, 10, 0);

            //then: 상세 repo 호출 여부 검증
            verify(addonRepository).findByProductIdIn(List.of(4L));
            verify(mobilePlanRepository, never()).findByProductIdIn(any());
        }

        @Test
        @DisplayName("tab-watch 조회 시 TabWatchPlanRepository만 호출")
        void tabWatch_callsOnlyTabWatchPlanRepository() {
            //given: 테스트 데이터 및 Mock 환경 설정
            Product p = productWithId(5L);
            Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

            when(productRepository.findByProductType(eq(ProductType.TAB_WATCH_PLAN), any(Pageable.class))).thenReturn(page);
            when(tabWatchPlanRepository.findByProductIdIn(anyList())).thenReturn(List.of());

            //when: 실제 로직 실행
            getProductListUseCase.execute("tab-watch", 0, 10, 0);

            //then: 상세 repo 호출 여부 검증
            verify(tabWatchPlanRepository).findByProductIdIn(List.of(5L));
            verify(mobilePlanRepository, never()).findByProductIdIn(any());
        }
    }

    @Nested
    @DisplayName("페이징 처리 검증 ")
    class Paging {

        @Test
        @DisplayName("특정 페이지와 사이즈를 요청하면 Repository에 정확한 Pageable이 전달된다")
        void shouldRequestCorrectPageable() {
            //given: 2번 페이지, 페이지당 20개 요청
            int requestPage = 2;
            int requestSize = 20;
            Page<Product> page = new PageImpl<>(List.of(), PageRequest.of(requestPage, requestSize), 0);
            when(productRepository.findByProductType(eq(ProductType.MOBILE_PLAN), any(Pageable.class)))
                    .thenReturn(page);

            //when: UseCase 실행
            getProductListUseCase.execute("mobile", requestPage, requestSize, 0);

            //then: ArgumentCaptor를 통해 전달된 Pageable 검증
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(productRepository).findByProductType(eq(ProductType.MOBILE_PLAN), pageableCaptor.capture());

            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(requestPage);
            assertThat(captured.getPageSize()).isEqualTo(requestSize);
        }

        @Test
        @DisplayName("반환 결과의 메타데이터(전체 개수등)가 정확해야한다.")
        void shouldReturnCorrectMetadata() {
            // given: 전체 데이터 100개 중 첫 페이지(10개) 반환 상황 시뮬레이션
            Product p = productWithId(1L);
            Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 100);

            when(productRepository.findByProductType(any(), any())).thenReturn(page);
            when(mobilePlanRepository.findByProductIdIn(anyList())).thenReturn(List.of());

            //when: 실행
            ProductListResult result = getProductListUseCase.execute("mobile", 0, 10, 0);

            //then: 결과의 Page 객체 상태 검증 (반환 타입은 DTO)
            assertThat(result.products().getTotalElements()).isEqualTo(100);
            assertThat(result.products().getContent()).hasSize(1);
            assertThat(result.products().getContent().get(0)).isInstanceOf(ProductSummaryDto.class);
            assertThat(result.products().getContent().get(0).productId()).isEqualTo(1L);
            assertThat(result.products().getNumber()).isZero();
        }
    }


    @Nested
    @DisplayName("예외 상황 및 성능 최적화 검증")
    class NPlusOnePrevention {

        @Test
        @DisplayName("상세 정보 조회 시 In 쿼리를 사용하여 N+1 문제를 방지해야 한다")
        @SuppressWarnings("unchecked")
        void shouldPreventNPlusOneProblemUsingInQuery(){
            //given: 2개의 상품이 조회된 상황
            Product p1 = productWithId(1L);
            Product p2 = productWithId(2L);
            Page<Product> page = new PageImpl<>(List.of(p1, p2));

            when(productRepository.findByProductType(eq(ProductType.INTERNET), any()))
                    .thenReturn(page);
            when(internetRepository.findByProductIdIn(anyList())).thenReturn(List.of());

            //when: 실행
            getProductListUseCase.execute("internet", 0, 10, 0);

            //then: 개별 조회가 아닌 ID 리스트를 통한 한번의 호출인지 검증
            ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
            verify(internetRepository).findByProductIdIn(captor.capture());

            //호출된 id 목록이 페이지의 상품 id들과 일치하는지 확인
            assertThat(captor.getValue()).containsExactlyInAnyOrder(1L, 2L);

        }
    }

    @Nested
    @DisplayName("인기 상품(best) 조회")
    class BestProductIds {


        @Test
        @DisplayName("bestCount > 0 이면 해당 카테고리로 인기 상품 ID를 N개 조회하고 결과에 포함한다")
        void whenBestCountPositive_callsSubscriptionRepositoryAndReturnsBestIds() {
            int bestCount = 5;
            List<Long> popularIds = List.of(10L, 20L, 30L);
            Page<Product> page = new PageImpl<>(List.of(productWithId(1L)), PageRequest.of(0, 10), 1);

            when(productRepository.findByProductType(eq(ProductType.MOBILE_PLAN), any(Pageable.class))).thenReturn(page);
            when(mobilePlanRepository.findByProductIdIn(anyList())).thenReturn(List.of());
            when(subscriptionRepository.findTopPopularProductIdsByProductType(eq(ProductType.MOBILE_PLAN), any(Pageable.class)))
                    .thenReturn(popularIds);

            ProductListResult result = getProductListUseCase.execute("mobile", 0, 10, bestCount);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(subscriptionRepository).findTopPopularProductIdsByProductType(eq(ProductType.MOBILE_PLAN), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(bestCount);
            assertThat(result.bestProductIds()).isEqualTo(popularIds);
        }

    }
}
