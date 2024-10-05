package io.teamchallenge.project.bazario.config;

import io.teamchallenge.project.bazario.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final var token = getTokenFromRequest(request);
        if (token != null && tokenProvider.validateToken(token)) {
            log.debug("token: {}", token);
            final var username = tokenProvider.getUsername(token);
            final var user = userService.loadUserByUsername(username);

            final var authenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource()
                    .buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request){
        final var bearerToken = request.getHeader("Authorization");

        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }
}
