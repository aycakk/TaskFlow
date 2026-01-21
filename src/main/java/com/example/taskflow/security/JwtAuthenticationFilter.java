package com.example.taskflow.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 1) Header yoksa veya Bearer değilse -> devam et
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        // 2) Zaten login olmuşsa tekrar uğraşma
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3) Token -> username
            String username = jwtService.extractUsername(token);

            // 4) DB’den user çek
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5) Token valid mi?
            if (jwtService.isTokenValid(token, userDetails)) {

                // principal = userDetails (en doğrusu)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (JwtException | IllegalArgumentException e) {
            // Token bozuk / expired / yanlış imza vs.
            // Şimdilik sadece auth set etmiyoruz ve request'e devam ediyoruz.
            // İstersen burada 401 de döndürebiliriz.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
