package com.example.viegymapp.security.jwt;

import com.example.viegymapp.service.Impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null
                    && jwtUtils.validateJwtToken(jwt)
                    && SecurityContextHolder.getContext().getAuthentication() == null
            ) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // First try to get JWT from Authorization header (for mobile/SPA clients)
        String jwt = jwtUtils.getJwtFromHeader(request);
        
        if (jwt != null && !jwt.isEmpty()) {
            logger.debug("[AuthTokenFilter] Token found in Authorization header");
            return jwt;
        }
        
        // If not found in header, try cookies (for web clients)
        jwt = jwtUtils.getJwtFromCookies(request);
        
        if (jwt != null && !jwt.isEmpty()) {
            logger.debug("[AuthTokenFilter] Token found in cookie");
        } else {
            logger.debug("[AuthTokenFilter] No token found in header or cookie for path: {}", request.getRequestURI());
        }
        
        return jwt;
    }
}
