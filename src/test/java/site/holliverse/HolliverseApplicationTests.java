package site.holliverse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import site.holliverse.customer.persistence.repository.AddonRepository;
import site.holliverse.customer.persistence.repository.InternetRepository;
import site.holliverse.customer.persistence.repository.IptvRepository;
import site.holliverse.customer.persistence.repository.MobilePlanRepository;
import site.holliverse.customer.persistence.repository.ProductRepository;
import site.holliverse.customer.persistence.repository.TabWatchPlanRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class HolliverseApplicationTests {

    @MockitoBean
    private ProductRepository productRepository;
    @MockitoBean
    private MobilePlanRepository mobilePlanRepository;
    @MockitoBean
    private InternetRepository internetRepository;
    @MockitoBean
    private IptvRepository iptvRepository;
    @MockitoBean
    private AddonRepository addonRepository;
    @MockitoBean
    private TabWatchPlanRepository tabWatchPlanRepository;

    @Test
    void contextLoads() {
    }
}
