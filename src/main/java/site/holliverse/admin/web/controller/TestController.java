package site.holliverse.admin.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin")
public class TestController {
    @GetMapping("/api/test")
    public String testController() {
        return "admin server";
    }
}
