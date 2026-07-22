package org.myspring.backend.controller;

import org.junit.jupiter.api.Test;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.service.JwtService;
import org.myspring.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void updateUser_delegatesToUpdateUser_whenFileIsNotProvided() throws Exception {
        User updated = User.builder().id(1L).fullname("New Name").build();
        when(userService.updateUser(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(multipart("/api/user/1").param("id", "1").param("fullname", "New Name")
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullname").value("New Name"));

        verify(userService).updateUser(eq(1L), any());
        verify(userService, never()).updateProfilePic(any(), any(), any());
    }

    @Test
    void updateUser_delegatesToUpdateProfilePic_whenFileProvided() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        User updated = User.builder().id(1L).fullname("New Name").imageUrl("https://example.com/profile.png").build();
        when(userService.updateProfilePic(eq(1L), any(), any())).thenReturn(updated);

        mockMvc.perform(multipart("/api/user/1").file(file)
                        .param("id", "1").param("fullname", "New Name")
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullname").value("New Name"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/profile.png"));

        verify(userService).updateProfilePic(eq(1L), any(), any());
        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void updateUser_returnsNotFound_whenServiceThrowsUserNotFound() throws Exception {
        when(userService.updateUser(eq(999L), any())).thenThrow(new UserNotFound("UserId 999 is not found"));

        mockMvc.perform(multipart("/api/user/999").param("id", "999").param("fullname", "Ghost")
                        .with(req -> {
                            req.setMethod("PUT");
                            return req;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_delegatesToServiceAndReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/user/1/johndoe"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L, "johndoe");
    }

    @Test
    void deleteUser_returnsNotFound_whenServiceThrowsUserNotFound() throws Exception {
        doThrow(new UserNotFound("UserId 999 is not found")).when(userService).deleteUser(999L, "ghost");

        mockMvc.perform(delete("/api/user/999/ghost"))
                .andExpect(status().isNotFound());
    }
}
