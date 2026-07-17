package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.UserDto;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
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
    public User updateUser(UserDto userDto) throws UserNotFound {
        User user = userRepository.findById(userDto.id()).orElseThrow(() -> new UserNotFound("UserId " + userDto.id() + " is not found"));
        user.update(userDto.fullname());
        userRepository.save(user);
        return user;
    }

    @Transactional
    public User updateProfilePic(UserDto userDto, MultipartFile file) throws IOException, UserNotFound {
        User user = userRepository.findById(userDto.id()).orElseThrow(() -> new UserNotFound("UserId " + userDto.id() + " is not found"));
        String url = cloudinaryService.upload(file);
        user.update(userDto.fullname(), url);
        userRepository.save(user);
        return user;
    }
}
