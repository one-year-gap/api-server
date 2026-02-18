package site.holliverse.auth.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



// 구글로그인 성공 테스트 컨트롤러
@RestController
public class AuthTestController {
    @GetMapping("/test/callback")
    public String ok() {
        return "OAUTH OK";
    }
}
