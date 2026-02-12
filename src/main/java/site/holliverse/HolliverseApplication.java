package site.holliverse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "com.holliverse.shared"
)
public class HolliverseApplication {

    public static void main(String[] args) {
        SpringApplication.run(HolliverseApplication.class, args);
    }

}
