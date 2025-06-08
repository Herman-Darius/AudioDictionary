package com.dictionary.app.Controllers;

import com.dictionary.app.Security.MediaProperties;
import com.dictionary.app.Services.MediaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String fileName) throws IOException {
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        File imageFile = new File(mediaProperties.getImageDir(), fileName);
        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        String mime = Files.probeContentType(imageFile.toPath());
        if (mime == null) {
            mime = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mime)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(new FileSystemResource(imageFile));
    }

    @PostMapping("/media/images/upload")
    public ResponseEntity<String> uploadImages(@RequestParam("files") MultipartFile[] files) {
        return mediaService.saveFiles(files, mediaProperties.getImageDir(), "image");
    }

    //sa fac pentru admin app

    /** Returns 200 + true/false if the word’s image exists */
    @GetMapping("/check/word-image/{wordId}")
    public ResponseEntity<Boolean> checkWordImage(@PathVariable int wordId) {
        return ResponseEntity.ok(mediaService.checkIfWordImageExists(wordId));
    }

    /** Returns 200 + true/false if the word’s audio exists */
    @GetMapping("/check/word-audio/{wordId}")
    public ResponseEntity<Boolean> checkWordAudio(@PathVariable int wordId) {
        return ResponseEntity.ok(mediaService.checkIfWordAudioExists(wordId));
    }

    /** Returns 200 + true/false if the phrase’s audio exists */
    @GetMapping("/check/phrase-audio/{phraseId}")
    public ResponseEntity<Boolean> checkPhraseAudio(@PathVariable int phraseId) {
        return ResponseEntity.ok(mediaService.checkIfAudioFileExists(phraseId));
    }

    // --- SINGLE-FILE UPLOADS ---

    /** Upload or replace a single word image */
    @PostMapping("/upload/word-image/{wordId}")
    public ResponseEntity<String> uploadWordImage(
            @PathVariable int wordId,
            @RequestParam("file") MultipartFile file) {
        return mediaService.saveWordImage(wordId, file);
    }

    /** Upload or replace a single word audio file */
    @PostMapping("/upload/word-audio/{wordId}")
    public ResponseEntity<String> uploadWordAudio(
            @PathVariable int wordId,
            @RequestParam("file") MultipartFile file) {
        return mediaService.saveWordAudio(wordId, file);
    }

    /** Upload or replace a single phrase audio file */
    @PostMapping("/upload/phrase-audio/{phraseId}")
    public ResponseEntity<String> uploadPhraseAudio(
            @PathVariable int phraseId,
            @RequestParam("file") MultipartFile file) {
        return mediaService.savePhraseAudio(phraseId, file);
    }

}
