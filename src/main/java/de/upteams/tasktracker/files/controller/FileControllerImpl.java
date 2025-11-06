package de.upteams.tasktracker.files.controller;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.files.uploading.FileService;
import de.upteams.tasktracker.task.entity.Attachment;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
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

import java.security.Principal;

/**
 * @author Oleg Mordkovich
 * {@code @date} 23.10.2025
 */
@RestController
@RequestMapping("/api/v1/")
public class FileControllerImpl implements FileController {
    private final FileService fileService;
    private final UserService userService;
    private final TaskService taskService;

    public FileControllerImpl(FileService fileService, UserService userService, TaskService taskService) {
        this.fileService = fileService;
        this.userService = userService;
        this.taskService = taskService;
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
            value = "avatars/upload",
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
    @DeleteMapping("avatars/delete")
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

    @Override
    @Operation(
            summary = "Upload a new attachment for a task",
            description = "Uploads a file (PNG, JPG) to the server and links it to a specific task. The file will be stored on cloud and its URL added to the task's attachments. Max file size is 5 MB. "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachment uploaded successfully",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "https://cdn.example.com/attachments/task123_doc.png"))),
            @ApiResponse(responseCode = "400", description = "Invalid file or task ID", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - login required", content = @Content),
            @ApiResponse(responseCode = "415", description = "Unsupported file type", content = @Content),
            @ApiResponse(responseCode = "413", description = "File too large", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })

    @PostMapping(
            value = "tasks/{taskId}/attachments/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")

    public ResponseEntity<String> uploadAttachment(
            @Parameter(description = "Attachment image file (JPG or PNG, max 5MB)",
                    required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "ID of task", example = "002e5ce3-b16c-40da-a830-274fe6882aab")
            @PathVariable String taskId,
            @Parameter(hidden = true) Principal principal
    ) {
        validateFile(file);
        AppUser user = userService.getByEmailOrThrow(principal.getName());
        String url = fileService.uploadAttachment(file, taskId, user);
        Attachment attachment = new Attachment();
        attachment.setUrl(url);
        attachment.setTask(taskService.getOrThrow(taskId));
        taskService.addAttachment(taskId, attachment, user);
        return ResponseEntity.ok(url);
    }

    @Override
    @DeleteMapping("tasks/{taskId}/attachments/{attachmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(
            summary = "Delete attachment from task",
            description = "Deletes an attachment from a task. Removes file from cloud storage and the database reference. Accessible to admins or users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attachment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task or attachment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - login required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteAttachment(
            @Parameter(description = "Task ID", example = "002e5ce3-b16c-40da-a830-274fe6882aab")
            @PathVariable String taskId,
            @Parameter(description = "Attachment ID", example = "c1d5d0e7-3a48-4b6b-b7d4-d8d9bff234f5")
            @PathVariable String attachmentId) {
        try {
            taskService.deleteAttachmentFromTask(taskId, attachmentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete attachment");
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
