package com.ist.leave_ms.service;

import java.io.InputStream;
import java.util.Collections;

import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ist.leave_ms.model.RoleType;
import com.ist.leave_ms.model.User;
import com.ist.leave_ms.repository.UserRepository;
import com.microsoft.graph.requests.GraphServiceClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import okhttp3.Request;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final Environment environment;
    private final GraphServiceClient<Request> graphClient;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Validate email domain in production
        if (isProductionEnvironment() && !email.endsWith("@ist.com")) {
            throw new UsernameNotFoundException("Only @ist.com emails are allowed in production");
        }

        // Fetch or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUserFromOAuth(email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "", // No password for OAuth users
                Collections.singletonList(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name())));
    }

    private User createUserFromOAuth(String email) {
        User user = new User();
        user.setEmail(email);

        try {
            com.microsoft.graph.models.User graphUser = graphClient.users(email).buildRequest().get();
            if (graphUser != null) {
                user.setName(graphUser.givenName + " " + graphUser.surname);
                try {
                    var photoContent = graphClient.users(email).photo().content().buildRequest().get();
                    if (photoContent != null) {
                        InputStream photo = photoContent;
                        String fileName = user.getId() + ".jpg";
                        String photoUrl = fileStorageService.storeProfilePicture(photo, fileName);
                        user.setProfilePictureUrl(photoUrl);
                    } else {
                        user.setProfilePictureUrl("https://default-avatar-url.com");
                    }
                } catch (Exception e) {
                    user.setProfilePictureUrl("https://default-avatar-url.com");
                }
            } else {
                // Fallback to defaults if graphUser is null
                user.setName(email.split("@")[0]);
                user.setProfilePictureUrl("https://default-avatar-url.com");
            }
        } catch (Exception e) {
            // Fallback to defaults
            user.setName(email.split("@")[0]);
            user.setProfilePictureUrl("https://default-avatar-url.com");
        }

        user.setRole(RoleType.STAFF);
        return userRepository.save(user);
    }

    private boolean isProductionEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}