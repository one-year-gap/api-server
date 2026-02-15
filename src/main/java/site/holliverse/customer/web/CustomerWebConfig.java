package site.holliverse.customer.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import site.holliverse.customer.web.assembler.ProductListResponseAssembler;
import site.holliverse.customer.web.mapper.ProductResponseMapper;

@Configuration
@Profile("customer")
public class CustomerWebConfig {

    @Bean
    public ProductResponseMapper productResponseMapper() {
        return new ProductResponseMapper();
    }

    @Bean
    public ProductListResponseAssembler productListResponseAssembler(ProductResponseMapper productResponseMapper) {
        return new ProductListResponseAssembler(productResponseMapper);
    }
}
