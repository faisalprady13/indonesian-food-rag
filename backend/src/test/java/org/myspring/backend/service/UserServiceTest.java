package org.myspring.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.UserDto;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUser_updatesFullnameAndSaves() throws UserNotFound {
        User user = User.builder().id(1L).fullname("Old Name").build();
        UserDto userDto = new UserDto(1L, "New Name");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.updateUser(1L, userDto);

        assertThat(result.getFullname()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_throwsUserNotFound_whenUserDoesNotExist() {
        UserDto userDto = new UserDto(999L, "Ghost");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.updateUser(999L, userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfilePic_uploadsFileAndUpdatesFullnameAndImageUrl() throws IOException, UserNotFound {
        User user = User.builder().id(1L).fullname("Old Name").imageUrl("old-url").build();
        UserDto userDto = new UserDto(1L, "New Name");
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cloudinaryService.upload(file)).thenReturn("https://example.com/new-profile.png");

        User result = userService.updateProfilePic(1L, userDto, file);

        assertThat(result.getFullname()).isEqualTo("New Name");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/new-profile.png");
        verify(userRepository).save(user);
    }

    @Test
    void updateProfilePic_throwsUserNotFound_whenUserDoesNotExist() {
        UserDto userDto = new UserDto(999L, "Ghost");
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.updateProfilePic(999L, userDto, file));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_deletesUser_whenUsernameMatches() throws UserNotFound {
        User user = User.builder().id(1L).username("johndoe").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, "johndoe");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_doesNotDelete_whenUsernameDoesNotMatch() throws UserNotFound {
        User user = User.builder().id(1L).username("johndoe").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, "someoneelse");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_throwsUserNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.deleteUser(999L, "ghost"));
        verify(userRepository, never()).delete(any());
    }
}