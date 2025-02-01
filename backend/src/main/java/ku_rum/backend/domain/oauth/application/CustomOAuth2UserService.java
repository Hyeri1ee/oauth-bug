package ku_rum.backend.domain.oauth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import ku_rum.backend.domain.department.domain.Department;
import ku_rum.backend.domain.department.domain.repository.DepartmentRepository;
import ku_rum.backend.domain.oauth.domain.OAuthAttributes;
import ku_rum.backend.domain.oauth.domain.UserProfile;
import ku_rum.backend.domain.oauth.util.TokenValidator;
import ku_rum.backend.domain.user.application.UserService;
import ku_rum.backend.domain.user.domain.User;
import ku_rum.backend.domain.user.domain.repository.UserRepository;
import ku_rum.backend.global.security.jwt.CustomUserDetails;
import ku_rum.backend.global.security.jwt.JwtTokenProvider;
import ku_rum.backend.global.security.jwt.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final HttpServletRequest request;
    private final DepartmentRepository departmentRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2User oAuth2User;
        if (registrationId.equals("naver")){
            oAuth2User = new DefaultOAuth2UserServiceNaver().loadUser(userRequest);
        } else {
            oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        }

        // Get original attributes
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.debug("Original OAuth2 attributes: {}", attributes);

        // Extract user profile using OAuthAttributes enum
        UserProfile userProfile = OAuthAttributes.extract(registrationId, attributes);

        // Update or create user
        User user = updateOrPrepareUser(userProfile);

        // Create authentication
        Authentication authentication = createAuthentication(user);

        // Generate token
        TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);
        logTokenClaims(tokenResponse.accessToken());

        // Prepare attributes for OAuth2User
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("id", userProfile.getId());
        customAttributes.put("name", userProfile.getUsername());
        customAttributes.put("email", userProfile.getEmail());
        customAttributes.put("accessToken", tokenResponse.accessToken());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                customAttributes,
                "id"  // Using id as the name attribute key consistently
        );
    }

    
    private Authentication createAuthentication(User user) {
        return new UsernamePasswordAuthenticationToken(
                CustomUserDetails.of(
                        user.getId(),
                        user.getNickname(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                        user.getPassword()
                ),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private void logTokenClaims(String accessToken) {
        try {
            Jws<Claims> jwsClaims = jwtTokenProvider.getClaimsFromToken(accessToken);
            log.debug("Access Token Claims: {}", jwsClaims.getBody());
        } catch (Exception e) {
            log.error("Failed to parse Access Token: {}", e.getMessage());
        }
    }

    private User updateOrPrepareUser(UserProfile userProfile) {
        return userRepository.findUserByEmail(userProfile.getEmail())
                .map(existingUser -> {
                    log.debug("Updating existing user: {}", existingUser.getEmail());
                    // Update existing user properties if needed
                    return existingUser;
                })
                .orElseGet(() -> {
                    log.debug("Creating new user with email: {}", userProfile.getEmail());
                    return userProfile.toEntity();
                });
    }

}