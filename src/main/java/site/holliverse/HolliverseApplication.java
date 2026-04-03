package site.holliverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(
        scanBasePackages = "site.holliverse"
)
public class HolliverseApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolliverseApplication.class, args);
    }

}
