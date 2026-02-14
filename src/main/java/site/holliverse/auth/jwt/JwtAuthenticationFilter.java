package site.holliverse.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.holliverse.shared.persistence.entity.enums.MemberStatusType;
import site.holliverse.shared.security.CustomUserDetails;

import java.io.IOException;

@Component
// 요청마다 JWT를 검사해 SecurityContext에 인증 정보를 세팅하는 필터
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Authorization 헤더에서 Bearer 토큰을 읽기 위한 상수
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Authorization 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2) Access Token만 인증 컨텍스트에 반영(Refresh Token은 인증용으로 사용 금지)
        if (token != null && jwtTokenProvider.isValid(token) && jwtTokenProvider.isAccessToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtTokenProvider.parseClaims(token);

            // 3) 토큰 claims에서 사용자 식별/권한 정보 추출
            String memberId = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            String status = claims.get("status", String.class);

            // 4) @AuthenticationPrincipal에서 사용할 principal 객체 구성
            CustomUserDetails principal = new CustomUserDetails(
                    Long.parseLong(memberId),
                    email,
                    null, // JWT 인증 단계에서는 비밀번호를 보관할 필요가 없다.
                    role,
                    status != null ? MemberStatusType.valueOf(status) : MemberStatusType.ACTIVE
            );

            // 5) 인증 객체를 만들어 SecurityContext에 저장
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6) 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    // Authorization: Bearer {token} 형식에서 실제 토큰 문자열만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
