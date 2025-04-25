package com.dictionary.app.Controllers;

import com.dictionary.app.Security.MediaProperties;
import com.dictionary.app.Services.MediaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class MediaController {
    private final MediaService mediaService;
    private final MediaProperties mediaProperties;

    public MediaController(MediaService mediaService, MediaProperties audioProperties) {
        this.mediaService = mediaService;
        this.mediaProperties = audioProperties;
        ensureUploadDirectoryExists();
    }

    private void ensureUploadDirectoryExists() {
        File dir = new File(mediaProperties.getAudioDir());
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Created upload dir: " + created + " -> " + mediaProperties.getAudioDir());
        }
    }

    @PostMapping("/audio/upload")
    public ResponseEntity<String> uploadAudioFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            for (MultipartFile file : files) {
                File destinationFile = new File(mediaProperties.getAudioDir() + file.getOriginalFilename());
                file.transferTo(destinationFile);
                System.out.println("File saved at: " + destinationFile.getAbsolutePath());
            }
            return ResponseEntity.ok("Files uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/audio/play")
    public ResponseEntity<FileSystemResource> getAudioFile(@RequestParam("word") String wordName) {
        return mediaService.getAudioForWord(wordName);
    }

    @GetMapping("audio/checkPhraseAudio/{phraseId}")
    public ResponseEntity<Boolean> checkIfPhraseAudioExists(@PathVariable int phraseId) {
        return ResponseEntity.ok(mediaService.checkIfAudioFileExists(phraseId));
    }

    @GetMapping("audio/phrases/{phraseId}")
    public ResponseEntity<FileSystemResource> getPhraseAudio(@PathVariable int phraseId) {
        return mediaService.getAudioForPhrase(phraseId);
    }
    @GetMapping("/media/images/{fileName:.+}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String fileName) {
        return mediaService.getImageFile(fileName);
    }

    @PostMapping("/media/images/upload")
    public ResponseEntity<String> uploadImages(@RequestParam("files") MultipartFile[] files) {
        return mediaService.saveFiles(files, mediaProperties.getImageDir(), "image");
    }
}
