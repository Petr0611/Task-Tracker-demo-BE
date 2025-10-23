package de.upteams.tasktracker.files.controller;

import de.upteams.tasktracker.files.uploading.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")

    public ResponseEntity<String> uploadAvatar(
            @RequestParam MultipartFile file,
            @RequestParam String userId
    ) {
        String url = fileService.uploadAvatar(file, userId);
        return ResponseEntity.ok(url);
    }
}
