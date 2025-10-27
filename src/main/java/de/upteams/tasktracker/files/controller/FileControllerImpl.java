package de.upteams.tasktracker.files.controller;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.files.uploading.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Oleg Mordkovich
 * {@code @date} 23.10.2025
 */
@RestController
@RequestMapping("/api/v1/avatars")
public class FileControllerImpl implements FileController {
    private final FileService fileService;

    public FileControllerImpl(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    @Operation(
            summary = "Upload new user avatar",
            description = "Upload user avatar image to Server. (PNG or JPG only! max 5 MB). If user already has an avatar, the old one will be deleted after successful upload"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "https://cdn.example.com/avatars/upload/user123@mail.com_avatar1.png"))),
            @ApiResponse(responseCode = "400", description = "Invalid file or parameters",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - login required",
                    content = @Content),
            @ApiResponse(responseCode = "415", description = "Unsupported media type (only JPG/PNG allowed)",
                    content = @Content),
            @ApiResponse(responseCode = "413", description = "File too large (>5 MB)",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)

    })
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")

    public ResponseEntity<String> uploadAvatar(
            @Parameter(description = "Avatar image file (JPG or PNG, max 5MB)",
                    required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "User email", example = "user@example.com")
            @RequestParam String email
    ) {
        validateFile(file);
        String url = fileService.uploadAvatar(file, email);
        return ResponseEntity.ok(url);
    }

    @Override
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Delete existing user avatar",
            description = "Removes the user's avatar from cloud and resets avatarUrl in the database."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avatar deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User or avatar not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - login required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> deleteAvatar(@RequestParam String email) {
        try {
            fileService.deleteUserAvatar(email);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete avatar");
        }
    }


    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        long size = file.getSize();

        if (file.isEmpty()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new RestApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Invalid content format! Only PNG and JPG files are allowed.");
        }
        if (size > 5 * 1024 * 1024) {
            throw new RestApiException(HttpStatus.PAYLOAD_TOO_LARGE, "File size is too large");
        }
    }
}
