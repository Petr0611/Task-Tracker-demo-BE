package de.upteams.tasktracker.files.controller;

import de.upteams.tasktracker.files.uploading.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
        validateFile(file);
        String url = fileService.uploadAvatar(file, userId);
        return ResponseEntity.ok(url);
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        long size = file.getSize();

        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid content format");
        }
        if (size > 5 * 1024 * 1024) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size is too large");
        }
    }
}
