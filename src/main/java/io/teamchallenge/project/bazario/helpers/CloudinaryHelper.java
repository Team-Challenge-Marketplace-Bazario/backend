package io.teamchallenge.project.bazario.helpers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.teamchallenge.project.bazario.exceptions.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
public class CloudinaryHelper {

    private final Cloudinary cloudinary;

    public CloudinaryHelper(@Value("${app.cloudinary}") String credentials) {

        this.cloudinary = new Cloudinary(credentials);
        this.cloudinary.config.secure = true;
    }

    public UploadResult uploadFile(MultipartFile file) {
        final var options = ObjectUtils.asMap(
                "filename", UUID.randomUUID().toString(),
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
        );

        try {
            final var uploadResponse = this.cloudinary.uploader().upload(file.getBytes(), options);

            log.debug("uploadResponse: {}", uploadResponse);

            final var url = (String) uploadResponse.get("url");
            final var publicId = (String) uploadResponse.get("public_id");

            return new UploadResult(url, publicId);
        } catch (Exception e) {
            throw new AppException("Failed to upload file", e);
        }
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Collections.emptyMap());
        } catch (IOException e) {
            throw new AppException("Failed to delete file", e);
        }
    }

    public record UploadResult(String url, String publicId) {

    }
}
