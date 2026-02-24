package site.holliverse.admin.query.config;

import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import site.holliverse.admin.query.dao.AdminSupportStatDao;

@Profile("admin")
@Configuration
public class AdminQueryConfiguration {

    @Bean
    public AdminSupportStatDao adminSupportStatDao(DSLContext dsl) {
        return new AdminSupportStatDao(dsl); // 여기서 직접 생성해서 넘김
    }
}