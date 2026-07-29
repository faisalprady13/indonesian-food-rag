package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.UserDto;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.model.UserSetting;
import org.myspring.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;

    @Transactional
    public User createUser(User newUser) {
        newUser.setRole("USER");

        UserSetting setting = UserSetting.builder()
                .appTheme("dark")
                .user(newUser)
                .build();
        newUser.setUserSetting(setting);

        return userRepository.save(newUser);
    }

    @Transactional
    public User updateUser(Long id, UserDto userDto) throws UserNotFound {
        User user = findUserOrThrow(id);
        user.update(userDto.fullname());
        userRepository.save(user);
        return user;
    }

    @Transactional
    public User updateProfilePic(Long id, UserDto userDto, MultipartFile file) throws IOException, UserNotFound {
        User user = findUserOrThrow(id);
        String url = cloudinaryService.upload(file);
        user.update(userDto.fullname(), url);
        userRepository.save(user);
        return user;
    }

    @Transactional
    public void deleteUser(Long id, String username) throws UserNotFound {
        User user = findUserOrThrow(id);
        if (user.getUsername().equals(username)) {
            userRepository.delete(user);
        }
    }

    public Long getCurrentUserId() throws UnauthorizedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserPrincipal(User user))) {
            throw new UnauthorizedException("Invalid authentication principal");
        }

        return user.getId();
    }


    private User findUserOrThrow(Long id) throws UserNotFound {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFound("UserId " + id + " is not found"));
    }
}
