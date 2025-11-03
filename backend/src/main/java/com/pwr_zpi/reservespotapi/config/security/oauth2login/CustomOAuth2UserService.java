package com.pwr_zpi.reservespotapi.config.security.oauth2login;

import com.pwr_zpi.reservespotapi.entities.picture.Picture;
import com.pwr_zpi.reservespotapi.entities.users.AuthProvider;
import com.pwr_zpi.reservespotapi.entities.users.Role;
import com.pwr_zpi.reservespotapi.entities.users.User;
import com.pwr_zpi.reservespotapi.entities.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo;

        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.name())) {
            userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        } else {
            throw new OAuth2AuthenticationException("Login with " + registrationId + " is not supported yet.");
        }

        if (!StringUtils.hasText(userInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (
                    user.getProvider() != null &&
                    !user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase())) &&
                    user.getProvider() != AuthProvider.LOCAL //ensures that local user can log in with oauth2
            ) {
                throw new OAuth2AuthenticationException(
                        "You're signed up with " + user.getProvider() + " account. Please use your " +
                                user.getProvider() + " account to login.");
            }

            user = updateExistingUser(user, userInfo, registrationId);
        } else {
            user = registerNewUser(userRequest, userInfo);
        }

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());

        return new DefaultOAuth2User(
                user.getAuthorities(),
                attributes,
                "name"
        );
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo) {
        Picture userPicture = Picture.builder()
                .url(userInfo.getImageUrl())
                .uploadedAt(LocalDateTime.now())
                .build();

        User user = User.builder()
                .name(userInfo.getName())
                .email(userInfo.getEmail())
                .oauthProviderId(userInfo.getId())
                .provider(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase()))
                .role(Role.CLIENT)
                .picture(userPicture)
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo, String provider) {
        existingUser.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        existingUser.setOauthProviderId(userInfo.getId());
        return userRepository.save(existingUser);
    }
}