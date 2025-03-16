package com.dictionary.app.Controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/audio")
public class AudioController {
    private final String uploadDir = "C:/Users/Herman Darius-Razvan/Desktop/Licenta-Aplicatie-Mobile/backend/uploads/audio_files/";


    @PostMapping("/upload")
    public ResponseEntity<String> uploadAudioFiles(@RequestParam("files") MultipartFile[] files) {
        System.out.println("The client accessed the audio upload files endpoint ------------------------>");

        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        try {
            for (MultipartFile file : files) {
                File destinationFile = new File(uploadDir + file.getOriginalFilename());

                // Save the file to the specified directory
                file.transferTo(destinationFile);
                System.out.println("File saved at: " + destinationFile.getAbsolutePath());
            }
            return ResponseEntity.ok("Files uploaded successfully!");
        } catch (IOException e) {
            System.out.println("File failed to save. Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error uploading files: " + e.getMessage());
        }
    }

    @GetMapping("/play")
    public ResponseEntity<FileSystemResource> getAudioFile(@RequestParam("word") String wordName) {
        try {
            // Construct the file path based on the word
            File audioFile = new File(uploadDir + wordName + ".mp3");

            if (!audioFile.exists()) {
                return ResponseEntity.notFound().build(); // Return 404 if file doesn't exist
            }

            // Serve the file with proper content type and headers
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg") // Specify the audio MIME type
                    .body(new FileSystemResource(audioFile));

        } catch (Exception e) {
            // Handle any errors (e.g., file not found)
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/phrases/{audioFileName}")
    public ResponseEntity<FileSystemResource> getPhraseAudio(@PathVariable String audioFileName) {
        try {
            File audioFile = new File(uploadDir + audioFileName);

            if (!audioFile.exists()) {
                return ResponseEntity.notFound().build(); // Return 404 if file doesn't exist
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                    .body(new FileSystemResource(audioFile));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }


}
