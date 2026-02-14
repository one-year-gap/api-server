package site.holliverse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private MobilePlanRepository mobilePlanRepository;
    @MockBean
    private InternetRepository internetRepository;
    @MockBean
    private IptvRepository iptvRepository;
    @MockBean
    private AddonRepository addonRepository;
    @MockBean
    private TabWatchPlanRepository tabWatchPlanRepository;

    @Test
    void contextLoads() {
    }
}
