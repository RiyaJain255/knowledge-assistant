package com.example.knowledge.document;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.knowledge.config.AppProperties;

/** Keeps the original uploaded files on disk under generated names (user-supplied names are never used as paths). */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path root;

    public FileStorageService(AppProperties props) {
        this.root = Path.of(props.storage().dir()).toAbsolutePath().normalize();
    }

    public Path store(MultipartFile file, long userId, String extension) {
        try {
            Path dir = root.resolve(String.valueOf(userId));
            Files.createDirectories(dir);
            Path target = dir.resolve(UUID.randomUUID() + "." + extension);
            file.transferTo(target);
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store uploaded file", e);
        }
    }

    public void deleteQuietly(String storagePath) {
        if (storagePath == null) {
            return;
        }
        try {
            Path path = Path.of(storagePath).toAbsolutePath().normalize();
            if (path.startsWith(root)) { // never delete outside the storage root
                Files.deleteIfExists(path);
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Could not delete file {}: {}", storagePath, e.getMessage());
        }
    }
}
