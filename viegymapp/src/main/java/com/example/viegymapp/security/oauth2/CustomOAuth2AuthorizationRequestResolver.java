package com.example.viegymapp.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        // Save mobileState parameter to session before OAuth starts
        String mobileState = request.getParameter("mobileState");
        if (mobileState != null && !mobileState.isEmpty()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("mobileState", mobileState);
        }
        
        return defaultResolver.resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        // Save mobileState parameter to session before OAuth starts
        String mobileState = request.getParameter("mobileState");
        if (mobileState != null && !mobileState.isEmpty()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("mobileState", mobileState);
        }
        
        return defaultResolver.resolve(request, clientRegistrationId);
    }
}
