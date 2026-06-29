package com.orderflow.order.utils.cloudinary;

public record CloudinaryUploadResult(
        String publicId,
        String secureUrl
) {}