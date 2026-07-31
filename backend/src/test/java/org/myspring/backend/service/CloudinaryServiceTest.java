package org.myspring.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.response.CloudinaryUploadResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Test
    void upload_returnsSecureUrlAndPublicIdFromCloudinaryResponse() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any(Map.class)))
                .thenReturn(Map.of(
                        "secure_url", "https://res.cloudinary.com/demo/image/upload/profile-images/profile.png",
                        "public_id", "profile-images/profile"
                ));

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);
        CloudinaryUploadResponse result = cloudinaryService.upload(file);

        assertThat(result.url()).isEqualTo("https://res.cloudinary.com/demo/image/upload/profile-images/profile.png");
        assertThat(result.publicId()).isEqualTo("profile-images/profile");
    }

    @Test
    void upload_sendsFileBytesToProfileImagesFolder() throws IOException {
        byte[] content = "image-bytes".getBytes();
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", content);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://example.com/profile.png", "public_id", "profile-images/profile"));

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);
        cloudinaryService.upload(file);

        ArgumentCaptor<Object> fileCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Map> optionsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(fileCaptor.capture(), optionsCaptor.capture());
        assertThat((byte[]) fileCaptor.getValue()).isEqualTo(content);
        assertThat(optionsCaptor.getValue()).containsEntry("folder", "profile-images");
        assertThat(optionsCaptor.getValue()).containsKey("transformation");
    }

    @Test
    void upload_propagatesIOException_whenUploadFails() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any(Map.class))).thenThrow(new IOException("upload failed"));

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);

        Assertions.assertThrows(IOException.class, () -> cloudinaryService.upload(file));
    }

    @Test
    void delete_destroysImageByPublicId() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);
        cloudinaryService.delete("profile-images/profile");

        ArgumentCaptor<Map> optionsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).destroy(ArgumentMatchers.eq("profile-images/profile"), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue()).isEmpty();
    }

    @Test
    void delete_propagatesIOException_whenDestroyFails() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(any(), any(Map.class))).thenThrow(new IOException("destroy failed"));

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);

        Assertions.assertThrows(IOException.class,
                () -> cloudinaryService.delete("profile-images/profile"));
    }
}