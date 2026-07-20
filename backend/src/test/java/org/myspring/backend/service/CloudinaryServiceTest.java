package org.myspring.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void upload_returnsSecureUrlFromCloudinaryResponse() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/profile-images/profile.png"));

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);
        String url = cloudinaryService.upload(file);

        assertThat(url).isEqualTo("https://res.cloudinary.com/demo/image/upload/profile-images/profile.png");
    }

    @Test
    void upload_sendsFileBytesToProfileImagesFolder() throws IOException {
        byte[] content = "image-bytes".getBytes();
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", content);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://example.com/profile.png"));

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);
        cloudinaryService.upload(file);

        ArgumentCaptor<Object> fileCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Map> optionsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(fileCaptor.capture(), optionsCaptor.capture());
        assertThat((byte[]) fileCaptor.getValue()).isEqualTo(content);
        assertThat(optionsCaptor.getValue()).containsEntry("folder", "profile-images");
    }

    @Test
    void upload_propagatesIOException_whenUploadFails() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", "image-bytes".getBytes());
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any(Map.class))).thenThrow(new IOException("upload failed"));

        CloudinaryService cloudinaryService = new CloudinaryService(cloudinary);

        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> cloudinaryService.upload(file));
    }
}