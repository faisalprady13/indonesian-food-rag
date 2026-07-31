package org.myspring.backend.service;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import org.myspring.backend.dto.response.CloudinaryUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public CloudinaryUploadResponse upload(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader()
                .upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "profile-images",
                                "transformation",
                                new Transformation<>()
                                        .width(300)
                                        .height(300)
                                        .crop("fill")
                                        .gravity("face")
                                        .quality("auto")
                                        .fetchFormat("auto")
                        )
                );

        return new CloudinaryUploadResponse(
                uploadResult.get("secure_url").toString(),
                uploadResult.get("public_id").toString()
        );
    }

    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap()
        );
    }
}
