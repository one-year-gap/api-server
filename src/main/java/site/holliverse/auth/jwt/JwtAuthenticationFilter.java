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
import site.holliverse.shared.domain.model.MemberStatus;
import site.holliverse.shared.security.CustomUserDetails;

import java.io.IOException;

/**
 * 요청의 Bearer Access Token을 읽어 SecurityContext를 채우는 필터.
 * <p>
 * 주의:
 * - 인증 컨텍스트에는 Access Token만 허용한다.
 * - Refresh Token은 인증 토큰으로 사용하지 않는다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.isValid(token) && jwtTokenProvider.isAccessToken(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = jwtTokenProvider.parseClaims(token);

            String memberId = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            String status = claims.get("status", String.class);

            CustomUserDetails principal = new CustomUserDetails(
                    Long.parseLong(memberId),
                    email,
                    null,
                    role,
                    status != null ? MemberStatus.valueOf(status) : MemberStatus.ACTIVE
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /** Authorization 헤더에서 Bearer 토큰 문자열만 추출한다. */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
