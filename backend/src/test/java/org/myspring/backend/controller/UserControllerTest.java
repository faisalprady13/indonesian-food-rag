package org.myspring.backend.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.UserDto;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void updateUser_delegatesToUpdateUser_whenFileIsNull() throws IOException, UserNotFound {
        UserDto userDto = new UserDto(1L, "New Name");
        User updated = User.builder().id(1L).fullname("New Name").build();
        when(userService.updateUser(userDto)).thenReturn(updated);

        ResponseEntity<User> result = userController.updateUser(userDto, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(updated);
        verify(userService).updateUser(userDto);
        verify(userService, never()).updateProfilePic(any(), any());
    }

    @Test
    void updateUser_delegatesToUpdateUser_whenFileIsEmpty() throws IOException, UserNotFound {
        UserDto userDto = new UserDto(1L, "New Name");
        MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
        User updated = User.builder().id(1L).fullname("New Name").build();
        when(userService.updateUser(userDto)).thenReturn(updated);

        ResponseEntity<User> result = userController.updateUser(userDto, emptyFile);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(updated);
        verify(userService).updateUser(userDto);
        verify(userService, never()).updateProfilePic(any(), any());
    }

    @Test
    void updateUser_delegatesToUpdateProfilePic_whenFileProvided() throws IOException, UserNotFound {
        UserDto userDto = new UserDto(1L, "New Name");
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        User updated = User.builder().id(1L).fullname("New Name").imageUrl("https://example.com/profile.png").build();
        when(userService.updateProfilePic(userDto, file)).thenReturn(updated);

        ResponseEntity<User> result = userController.updateUser(userDto, file);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(updated);
        verify(userService).updateProfilePic(userDto, file);
        verify(userService, never()).updateUser(any());
    }

    @Test
    void updateUser_propagatesUserNotFound_fromService() throws IOException, UserNotFound {
        UserDto userDto = new UserDto(999L, "Ghost");
        when(userService.updateUser(userDto)).thenThrow(new UserNotFound("UserId 999 is not found"));

        assertThrows(UserNotFound.class, () -> userController.updateUser(userDto, null));
    }
}