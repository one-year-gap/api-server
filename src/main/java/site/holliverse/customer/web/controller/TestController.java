package site.holliverse.customer.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("customer")
@RequestMapping("/api/customer")
public class TestController {
    @GetMapping("test")
    public String testController() {
        return "customer server";
    }
}
