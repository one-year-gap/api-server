package site.holliverse.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import site.holliverse.auth.filter.LoginFilter;
import site.holliverse.auth.handler.LoginFailureHandler;
import site.holliverse.auth.handler.LoginSuccessHandler;
import site.holliverse.auth.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
// 인증/인가 필터 체인 구성 클래스
public class SecurityConfig {

    // JWT 기반 요청 인증 복원 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    // Spring Security 인증 매니저 빈 등록
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    // JSON 로그인 요청을 처리하는 커스텀 로그인 필터 빈
    public LoginFilter loginFilter(AuthenticationManager authenticationManager,
                                   LoginSuccessHandler loginSuccessHandler,
                                   LoginFailureHandler loginFailureHandler) {
        LoginFilter filter = new LoginFilter(authenticationManager);
        // 로그인 성공 시 JWT 응답을 내려주는 핸들러
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        // 로그인 실패 시 401 응답을 내려주는 핸들러
        filter.setAuthenticationFailureHandler(loginFailureHandler);
        return filter;
    }

    @Bean
    // 보안 정책 및 필터 순서 구성
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFilter loginFilter) throws Exception {
        return http
                // JWT API 서버 설정: 기본 폼 로그인/세션 인증 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 엔드포인트
                        .requestMatchers("/auth/signup", "/auth/login", "/auth/refresh", "/actuator/health", "/actuator/info").permitAll()
                        // 그 외 API는 인증 필요
                        .anyRequest().authenticated()
                )
                // 로그인 필터를 UsernamePasswordAuthenticationFilter 위치에 배치
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                // 일반 API 요청에서는 JWT 필터로 인증 복원
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    // 비밀번호 해싱 인코더(BCrypt)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
