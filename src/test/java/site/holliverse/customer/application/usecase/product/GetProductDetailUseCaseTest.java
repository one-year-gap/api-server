package site.holliverse.customer.application.usecase.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import site.holliverse.customer.application.usecase.GetProductDetailUseCase;
import site.holliverse.customer.application.usecase.ProductDetailResult;
import site.holliverse.customer.persistence.entity.Addon;
import site.holliverse.customer.persistence.entity.Internet;
import site.holliverse.customer.persistence.entity.Iptv;
import site.holliverse.customer.persistence.entity.MobilePlan;
import site.holliverse.customer.persistence.entity.Product;
import site.holliverse.customer.persistence.entity.TabWatchPlan;
import site.holliverse.customer.persistence.repository.AddonRepository;
import site.holliverse.customer.persistence.repository.InternetRepository;
import site.holliverse.customer.persistence.repository.IptvRepository;
import site.holliverse.customer.persistence.repository.MobilePlanRepository;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.TabWatchPlanRepository;
import site.holliverse.shared.domain.model.AddonType;
import site.holliverse.shared.domain.model.ProductType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductDetailUseCaseTest {

    @Mock
    private ProductRepository productRepository;
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
    private GetProductDetailUseCase getProductDetailUseCase;

    // -- Helper Methods --
    private static Product createProduct(Long productId, ProductType productType) {
        return Product.builder()
                .productId(productId)
                .productCode("CODE-" + productId)
                .name("테스트상품이름")
                .price(10000)
                .salePrice(8000)
                .productType(productType)
                .discountType("할인")
                .build();
    }

    private static MobilePlan createMobilePlan(Long productId) {
        return MobilePlan.builder()
                .productId(productId)
                .dataAmount("100GB")
                .tetheringSharingData("20GB")
                .benefitBrands("넷플릭스")
                .benefitVoiceCall("무제한")
                .benefitSms("200건")
                .benefitSignatureFamilyDiscount("가족할인")
                .build();
    }

    private static Internet createInternet(Long productId) {
        return Internet.builder()
                .productId(productId)
                .planTitle("기가인터넷")
                .speed("500Mbps")
                .benefits("와이파이 무료")
                .build();
    }

    private static Iptv createIptv(Long productId) {
        return Iptv.builder()
                .productId(productId)
                .planTitle("UHD 200채널")
                .channelCount(200)
                .benefits("실시간 재방송")
                .build();
    }

    private static Addon createAddon(Long productId) {
        return Addon.builder()
                .productId(productId)
                .addonType(AddonType.FAMILY_CARE)
                .description("가족 케어")
                .build();
    }

    private static TabWatchPlan createTabWatchPlan(Long productId) {
        return TabWatchPlan.builder()
                .productId(productId)
                .dataAmount("1GB")
                .build();
    }

    @Nested
    @DisplayName("존재하지 않는 상품")
    class ProductNotFound {

        @Test
        @DisplayName("해당 planId가 없으면 예외를 던진다")
        void throwsWhenProductNotFound() {
            Long planId = 999L;
            when(productRepository.findById(planId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getProductDetailUseCase.execute(planId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 상품을 찾을 수 없습니다: 999");

            verify(productRepository).findById(planId);
        }
    }

    @Nested
    @DisplayName("ProductType별 상세 조회 매핑")
    class DetailByProductType {

        @Test
        @DisplayName("MOBILE_PLAN이면 product와 mobilePlan이 채워진 결과를 반환한다")
        void mobilePlan_returnsResultWithProductAndMobilePlan() {
            //given
            Long planId = 1L;
            Product product = createProduct(planId, ProductType.MOBILE_PLAN);
            MobilePlan mobilePlan = createMobilePlan(planId);
            when(productRepository.findById(planId)).thenReturn(Optional.of(product));
            when(mobilePlanRepository.findById(planId)).thenReturn(Optional.of(mobilePlan));

            //when
            ProductDetailResult result = getProductDetailUseCase.execute(planId);

            //then
            assertThat(result.product().productId()).isEqualTo(planId);
            assertThat(result.mobilePlan().get().dataAmount()).isEqualTo("100GB");
            assertThat(result.internet()).isEmpty();
            assertThat(result.iptv()).isEmpty();
            assertThat(result.addon()).isEmpty();
            assertThat(result.tabWatchPlan()).isEmpty();
        }

        @Test
        @DisplayName("INTERNET이면 product와 internet이 채워진 결과를 반환한다")
        void internet_returnsResultWithProductAndInternet() {
            Long planId = 2L;
            Product product = createProduct(planId, ProductType.INTERNET);
            Internet internet = createInternet(planId);
            when(productRepository.findById(planId)).thenReturn(Optional.of(product));
            when(internetRepository.findById(planId)).thenReturn(Optional.of(internet));

            ProductDetailResult result = getProductDetailUseCase.execute(planId);

            assertThat(result.product().productId()).isEqualTo(planId);
            assertThat(result.internet()).isPresent();
            assertThat(result.internet().get().planTitle()).isEqualTo("기가인터넷");
            assertThat(result.internet().get().speed()).isEqualTo("500Mbps");
            assertThat(result.mobilePlan()).isEmpty();
        }

        @Test
        @DisplayName("IPTV이면 product와 iptv가 채워진 결과를 반환한다")
        void iptv_returnsResultWithProductAndIptv() {
            Long planId = 3L;
            Product product = createProduct(planId, ProductType.IPTV);
            Iptv iptv = createIptv(planId);
            when(productRepository.findById(planId)).thenReturn(Optional.of(product));
            when(iptvRepository.findById(planId)).thenReturn(Optional.of(iptv));

            ProductDetailResult result = getProductDetailUseCase.execute(planId);

            assertThat(result.product().productId()).isEqualTo(planId);
            assertThat(result.iptv()).isPresent();
            assertThat(result.iptv().get().planTitle()).isEqualTo("UHD 200채널");
            assertThat(result.iptv().get().channelCount()).isEqualTo(200);
            assertThat(result.mobilePlan()).isEmpty();
        }

        @Test
        @DisplayName("ADDON이면 product와 addon이 채워진 결과를 반환한다")
        void addon_returnsResultWithProductAndAddon() {
            Long planId = 4L;
            Product product = createProduct(planId, ProductType.ADDON);
            Addon addon = createAddon(planId);
            when(productRepository.findById(planId)).thenReturn(Optional.of(product));
            when(addonRepository.findById(planId)).thenReturn(Optional.of(addon));

            ProductDetailResult result = getProductDetailUseCase.execute(planId);

            assertThat(result.product().productId()).isEqualTo(planId);
            assertThat(result.addon()).isPresent();
            assertThat(result.addon().get().addonType()).isEqualTo(AddonType.FAMILY_CARE);
            assertThat(result.addon().get().description()).isEqualTo("가족 케어");
            assertThat(result.mobilePlan()).isEmpty();
        }

        @Test
        @DisplayName("TAB_WATCH_PLAN이면 product와 tabWatchPlan이 채워진 결과를 반환한다")
        void tabWatchPlan_returnsResultWithProductAndTabWatchPlan() {
            Long planId = 5L;
            Product product = createProduct(planId, ProductType.TAB_WATCH_PLAN);
            TabWatchPlan tabWatchPlan = createTabWatchPlan(planId);
            when(productRepository.findById(planId)).thenReturn(Optional.of(product));
            when(tabWatchPlanRepository.findById(planId)).thenReturn(Optional.of(tabWatchPlan));

            ProductDetailResult result = getProductDetailUseCase.execute(planId);

            assertThat(result.product().productId()).isEqualTo(planId);
            assertThat(result.tabWatchPlan()).isPresent();
            assertThat(result.tabWatchPlan().get().dataAmount()).isEqualTo("1GB");
            assertThat(result.mobilePlan()).isEmpty();
        }

        @Test
        @DisplayName("상품만 있고 상세가 없으면 product만 채워지고 상세 Optional은 empty다")
        void whenDetailNotFound_returnsEmptyOptionals() {
            Long planId = 1L;
            Product product = createProduct(planId, ProductType.MOBILE_PLAN);
            when(productRepository.findById(planId)).thenReturn(Optional.of(product));
            when(mobilePlanRepository.findById(planId)).thenReturn(Optional.empty());

            ProductDetailResult result = getProductDetailUseCase.execute(planId);

            assertThat(result.product()).isNotNull();
            assertThat(result.product().productId()).isEqualTo(planId);
            assertThat(result.mobilePlan()).isEmpty();
        }
    }
}
