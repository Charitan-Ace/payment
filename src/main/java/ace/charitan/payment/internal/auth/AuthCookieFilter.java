package ace.charitan.payment.internal.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Arrays;

@Component
public class AuthCookieFilter extends OncePerRequestFilter {

    @Value("${auth.cookie.name:charitan}")
    private String cookieName;

    @Autowired
    private AuthDetailsService authDetailsService;

    @Autowired
    private JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/ws/") ||
               path.contains("/websocket") ||
               path.contains("/info") ||
               path.contains("/xhr_streaming") ||
               path.contains("/xhr_send");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        String path = request.getRequestURI();
        logger.info(path);

        Cookie[] cookies = request.getCookies();
        System.out.println("Cookies: " + Arrays.toString(cookies));
        if (cookies == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie authCookie = null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                authCookie = cookie;
                break;
            }
        }

        if (authCookie == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.parseJwsPayload(authCookie.getValue());
        String id = claims.get("id", String.class);
        String roleId = claims.get("roleId", String.class);
        String email = claims.get("email", String.class);

        System.out.println("Auth filter id: " + id);
        authDetailsService.setRole(roleId, email);
        var details = authDetailsService.loadUserByUsername(id);

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(token);

        logger.info("Authenticated as " + details.getUsername());

        filterChain.doFilter(request, response);
    }
}
