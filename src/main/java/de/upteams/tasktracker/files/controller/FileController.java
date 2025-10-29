package de.upteams.tasktracker.files.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/**
 * @author Oleg Mordkovich
 * {@code @date} 23.10.2025
 */

public interface FileController {
    ResponseEntity<String> uploadAvatar(
            MultipartFile file,
            String email
    );

    ResponseEntity<String> deleteAvatar(String email);

    ResponseEntity<String> uploadAttachment(
            MultipartFile file, String taskId, Principal principal);

    ResponseEntity<Void> deleteAttachment(String taskId, String attachmentId);
}
