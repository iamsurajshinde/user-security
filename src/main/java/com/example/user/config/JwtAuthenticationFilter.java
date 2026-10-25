package com.example.user.config;

import com.example.user.model.User;
import com.example.user.service.JwtService;
import com.example.user.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if(auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);

            String email = null;

            try {
                email = jwtService.extractEmail(token);
            } catch (JwtException | IllegalArgumentException exception) {
                // Invalid tokens are rejected by authorization when no authentication is established.
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserByEmail(userService.normalizeEmail(email));
                if (user != null && jwtService.isValid(token, user)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, null);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
