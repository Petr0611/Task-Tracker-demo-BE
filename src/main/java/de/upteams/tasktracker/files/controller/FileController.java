package de.upteams.tasktracker.files.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Oleg Mordkovich
 * {@code @date} 23.10.2025
 */

public interface FileController {
    ResponseEntity<String> uploadAvatar(
            MultipartFile file,
            String userId
    );
}
