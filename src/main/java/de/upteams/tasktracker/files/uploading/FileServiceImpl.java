package de.upteams.tasktracker.files.uploading;

import de.upteams.tasktracker.configuration.AwsS3Configuration;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Asynchronous service for uploading files to DigitalOcean Spaces (AWS S3 API).
 * <p>
 * This service uses {@link S3AsyncClient} for non-blocking file uploads.
 * Streams are closed automatically when the operation completes or fails.
 * An {@link ExecutorService} is required for reading the InputStream without blocking SDK event-loop threads.
 * Configuration is provided by {@link AwsS3Configuration}.
 * </p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * CompletableFuture<Boolean> future = fileService.uploadFileAsync(
 *     "folder/file.txt",
 *     new FileInputStream("local.txt"),
 *     Map.of("author", "user"),
 *     "text/plain",
 *     new File("local.txt").length(),
 *     true
 * );
 * future.thenAccept(success -> {
 *     if (success) System.out.println("Uploaded");
 *     else System.err.println("Failed");
 * });
 * }</pre>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    private final S3AsyncClient s3Client;
    private final AwsS3Configuration config;
    private final ExecutorService executorService;
    private final UserService userService;

    /**
     * Asynchronously uploads a file input stream to the configured bucket.
     *
     * @param objectKey     target key (path) for the file in the bucket; must not be null or blank
     * @param inputStream   file content stream; will be closed automatically
     * @param metadata      optional metadata map; may be null or empty
     * @param contentType   optional MIME type (e.g. "image/png"); may be null or blank
     * @param contentLength exact file size in bytes; must be > 0
     * @param isPublicRead  if true, the object will have PUBLIC_READ ACL, otherwise PRIVATE
     * @return CompletableFuture<Boolean> resolving to true if uploadAvatar succeeds, false otherwise
     * @throws IllegalArgumentException if objectKey is null/blank or contentLength is null/≤0
     */
    @Async
    public CompletableFuture<Boolean> uploadFileAsync(
            final String objectKey,
            final @NonNull InputStream inputStream,
            final Map<String, String> metadata,
            final String contentType,
            final Long contentLength,
            boolean isPublicRead) {

        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("Object key cannot be null or empty");
        }
        if (contentLength == null || contentLength <= 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid content length %d for '%s'", contentLength, objectKey)
            );
        }

        final PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(config.getBucketName())
                .key(objectKey)
                .acl(isPublicRead ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.PRIVATE)
                .contentLength(contentLength);

        if (metadata != null && !metadata.isEmpty()) {
            builder.metadata(metadata);
            log.debug("Added metadata for {}: {}", objectKey, metadata);
        }
        if (contentType != null && !contentType.isBlank()) {
            builder.contentType(contentType);
            log.debug("Content-Type set to: {}", contentType);
        }
        final PutObjectRequest request = builder.build();
        final AsyncRequestBody requestBody = AsyncRequestBody.fromInputStream(inputStream, contentLength, executorService);

        log.debug("Starting async uploadAvatar of '{}' to bucket '{}' ({} bytes)", objectKey, config.getBucketName(), contentLength);

        return s3Client.putObject(request, requestBody)
                .thenApply(response -> {
                    log.debug("Async uploadAvatar succeeded for '{}'", objectKey);
                    return true;
                })
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause();
                    if (cause instanceof S3Exception) {
                        log.error("S3 async uploadAvatar failed for '{}': {}", objectKey, cause.getMessage(), cause);
                    } else {
                        log.warn("Async uploadAvatar exception for '{}': {}", objectKey, ex.getMessage(), ex);
                    }
                    return false;
                })
                .whenComplete((result, ex) -> {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        log.warn("Failed to close stream for '{}': {}", objectKey, e.getMessage(), e);
                    }
                });
    }

    @Override
    public String uploadAvatar(MultipartFile file, String email) {
        try {
            if (file == null || file.isEmpty()) {
                throw new RestApiException(HttpStatus.BAD_REQUEST, "File must not be empty");
            }
            if (email == null || email.isBlank()) {
                throw new RestApiException(HttpStatus.BAD_REQUEST, "Email must not be blank");
            }
            AppUser user = userService.getByEmailOrThrow(email);
            String key = String.format("avatars/%s_%s", email, file.getOriginalFilename());
            InputStream inputStream = file.getInputStream();

            //upload to s3
            long size = file.getSize();
            boolean success = uploadFileAsync(
                    key,
                    inputStream,
                    Map.of("uploadedBy", email),
                    file.getContentType(),
                    size,
                    true
            ).join();
            if (!success) {
                throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "File upload failed for key: " + key);
            }
            //creating new URL
            String newAvatarUrl = String.format("%s/%s/%s",
                    config.getEndpoint(),
                    config.getBucketName(),
                    key);
            //remove old avatar
            String oldUrl = user.getAvatarUrl();
            if (oldUrl != null && !oldUrl.isBlank() && isExternalAvatar(oldUrl)) {
                try {
                    String oldKey = extractKeyFromUrl(oldUrl);
                    deleteFile(oldKey);
                    log.info("Deleted old external avatar {}: ", oldKey);
                } catch (Exception e) {
                    log.warn("Failed to delete old external avatar for user {}: {}", email, e.getMessage());
                }
            }
            //update user
            user.setAvatarUrl(newAvatarUrl);
            userService.saveOrUpdate(user);
            log.info("User {} avatar uploaded successfully", email);
            return newAvatarUrl;

        } catch (RestApiException e) {

            throw e;

        } catch (IllegalArgumentException e) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (IOException e) {
            log.error("Error reading avatar file for user {}: {}", email, e.getMessage(), e);
            throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file stream");

        } catch (Exception e) {
            log.error("Unexpected error uploading avatar for {}: {}", email, e.getMessage(), e);
            throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error during upload");
        }
    }

    @Override
    public void deleteUserAvatar(String email) {
        AppUser user = userService.getByEmailOrThrow(email);
        String oldUrl = user.getAvatarUrl();

        if (oldUrl == null || oldUrl.isBlank()) {
            throw new RestApiException(HttpStatus.NOT_FOUND, "User has no avatar to delete");
        }

        if (isExternalAvatar(oldUrl)) {
            try {
                String oldKey = extractKeyFromUrl(oldUrl);
                deleteFile(oldKey);
                log.info("Deleted avatar {} from cloud", oldKey);
            } catch (Exception e) {
                throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete avatar from S3");
            }
        }

        user.setAvatarUrl("");
        userService.saveOrUpdate(user);
    }

    private boolean isExternalAvatar(String avatarUrl) {
        return avatarUrl.startsWith(config.getEndpoint());
    }

    private void deleteFile(String objKey) {
        s3Client.deleteObject(builder -> builder
                .bucket(config.getBucketName())
                .key(objKey)
        );
        log.info("Deleted file from Server {}", objKey);
    }

    private String extractKeyFromUrl(String url) {
        int index = url.indexOf("avatars/");
        if (index == -1) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Cannot extract key from url: " + url);
        }
        return url.substring(index);
    }
}
