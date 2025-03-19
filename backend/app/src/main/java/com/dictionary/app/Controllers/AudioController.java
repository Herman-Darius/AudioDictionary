package com.dictionary.app.Controllers;

import com.dictionary.app.Models.Word;
import com.dictionary.app.Repositories.WordRepository;
import com.dictionary.app.Services.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/audio")
public class AudioController {
    private final String uploadDir = "C:/Users/Herman Darius-Razvan/Desktop/Licenta-Aplicatie-Mobile/backend/uploads/audio_files/";

    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

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
        return audioService.getAudioForWord(wordName);
    }

    // Endpoint to check if the audio file for a phrase exists
    @GetMapping("/checkPhraseAudio/{phraseId}")
    public ResponseEntity<Boolean> checkIfPhraseAudioExists(@PathVariable int phraseId) {
        boolean audioExists = audioService.checkIfAudioFileExists(phraseId);
        return ResponseEntity.ok(audioExists);
    }

    // Endpoint to play the phrase audio
    @GetMapping("/phrases/{phraseId}")
    public ResponseEntity<FileSystemResource> getPhraseAudio(@PathVariable int phraseId) {
        return audioService.getAudioForPhrase(phraseId);
    }


}
