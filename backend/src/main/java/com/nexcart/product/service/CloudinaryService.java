package com.nexcart.product.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nexcart.exception.ImageDeleteException;
import com.nexcart.exception.ImageUploadException;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, Object> upload(MultipartFile file) {

        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "nexcart/products"
                    )
            );

        } catch (IOException e) {
            throw new ImageUploadException(
                    "Failed to upload image!",
                    e
            );
        }
    }

    public void delete(String publicId) {

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

        } catch (IOException e) {
            throw new ImageDeleteException(
                    "Failed to delete image!",
                    e
            );
        }
    }
}