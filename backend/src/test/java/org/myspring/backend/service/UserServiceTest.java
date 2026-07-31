package org.myspring.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.UserDto;
import org.myspring.backend.dto.response.CloudinaryUploadResponse;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.repository.UserRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadUserByUsername_returnsUserPrincipal_whenUserExists() {
        User user = User.builder().id(1L).username("johndoe").build();
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("johndoe");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(((UserPrincipal) result).user()).isEqualTo(user);
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFound_whenUserDoesNotExist() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("ghost"));
    }

    @Test
    void createUser_setsRoleAndAttachesDefaultDarkThemeUserSettingAndSaves() {
        User newUser = User.builder().username("chef").email("chef@example.com").build();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(newUser);

        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getUserSetting()).isNotNull();
        assertThat(result.getUserSetting().getAppTheme()).isEqualTo("dark");
        assertThat(result.getUserSetting().getUser()).isEqualTo(result);
        verify(userRepository).save(newUser);
    }

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
    void updateProfilePic_uploadsFileAndUpdatesFullnameImageUrlAndPublicId_whenNoPreviousImage() throws IOException, UserNotFound {
        User user = User.builder().id(1L).fullname("Old Name").build();
        UserDto userDto = new UserDto(1L, "New Name");
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cloudinaryService.upload(file))
                .thenReturn(new CloudinaryUploadResponse("https://example.com/new-profile.png", "profile-images/new"));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfilePic(1L, userDto, file);

        assertThat(result.getFullname()).isEqualTo("New Name");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/new-profile.png");
        assertThat(result.getProfileImagePublicId()).isEqualTo("profile-images/new");
        verify(userRepository).save(user);
        verify(cloudinaryService, never()).delete(any());
    }

    @Test
    void updateProfilePic_deletesOldImage_whenUserAlreadyHasProfileImage() throws IOException, UserNotFound {
        User user = User.builder().id(1L).fullname("Old Name")
                .imageUrl("https://example.com/old-profile.png")
                .profileImagePublicId("profile-images/old")
                .build();
        UserDto userDto = new UserDto(1L, "New Name");
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cloudinaryService.upload(file))
                .thenReturn(new CloudinaryUploadResponse("https://example.com/new-profile.png", "profile-images/new"));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfilePic(1L, userDto, file);

        assertThat(result.getProfileImagePublicId()).isEqualTo("profile-images/new");
        verify(cloudinaryService).delete("profile-images/old");
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
    void deleteUser_deletesUser_whenUsernameMatchesAndNoProfileImage() throws UserNotFound, IOException {
        User user = User.builder().id(1L).username("johndoe").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, "johndoe");

        verify(userRepository).delete(user);
        verify(cloudinaryService, never()).delete(any());
    }

    @Test
    void deleteUser_deletesProfileImage_whenUserHasOne() throws UserNotFound, IOException {
        User user = User.builder().id(1L).username("johndoe").profileImagePublicId("profile-images/old").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, "johndoe");

        verify(cloudinaryService).delete("profile-images/old");
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_doesNotDelete_whenUsernameDoesNotMatch() throws UserNotFound, IOException {
        User user = User.builder().id(1L).username("johndoe").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, "someoneelse");

        verify(userRepository, never()).delete(any());
        verify(cloudinaryService, never()).delete(any());
    }

    @Test
    void deleteUser_throwsUserNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.deleteUser(999L, "ghost"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getCurrentUserId_returnsUserId_whenPrincipalIsAuthenticatedUser() throws UnauthorizedException {
        User user = User.builder().id(42L).username("johndoe").build();
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        Long result = userService.getCurrentUserId();

        assertThat(result).isEqualTo(42L);
    }

    @Test
    void getCurrentUserId_throwsUnauthorized_whenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class, () -> userService.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_throwsUnauthorized_whenNotAuthenticated() {
        User user = User.builder().id(42L).username("johndoe").build();
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));

        assertThrows(UnauthorizedException.class, () -> userService.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_throwsUnauthorized_whenPrincipalIsNotUserPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("someUsername", null, java.util.List.of()));

        assertThrows(UnauthorizedException.class, () -> userService.getCurrentUserId());
    }
}