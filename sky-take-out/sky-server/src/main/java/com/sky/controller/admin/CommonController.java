package com.sky.controller.admin;

import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Value("${sky.upload-dir:D:/uploads}")
    private String uploadDir;

    @Value("${sky.file-host:http://localhost:8080}")
    private String fileHost;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("file 涓嶈兘涓虹┖");
        }

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }

            String newName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(newName);
            file.transferTo(target.toFile());

            return Result.success(fileHost + "/uploads/" + newName);
        } catch (Exception e) {
            return Result.error("涓婁紶澶辫触: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("name") String name) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = root.resolve(name).normalize();
        if (!filePath.startsWith(root) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(filePath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + Objects.requireNonNull(resource.getFilename()) + "\"")
                .body(resource);
    }
}
