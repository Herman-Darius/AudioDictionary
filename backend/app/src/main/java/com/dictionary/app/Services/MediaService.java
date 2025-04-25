package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.WordRepository;
import com.dictionary.app.Security.MediaProperties;
import lombok.Data;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@Data
public class MediaService {
    private final MediaProperties audioProperties;
    private final PhraseRepository phraseRepository;
    private final WordRepository wordRepository;
    private final MediaProperties mediaProperties;

    public ResponseEntity<FileSystemResource> getAudioForWord(String wordName) {
        Optional<Word> wordOptional = wordRepository.findByWordName(wordName);
        if (wordOptional.isEmpty() || wordOptional.get().getAudioFile() == null) {
            return ResponseEntity.notFound().build();
        }

        File audioFile = new File(audioProperties.getAudioDir() + wordOptional.get().getAudioFile());
        if (!audioFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(new FileSystemResource(audioFile));
    }

    public boolean checkIfAudioFileExists(int phraseId) {
        Optional<Phrase> phraseOptional = phraseRepository.findById(phraseId);
        if (phraseOptional.isEmpty() || phraseOptional.get().getAudioFile() == null) {
            return false;
        }

        File audioFile = new File(audioProperties.getAudioDir() + phraseOptional.get().getAudioFile());
        return audioFile.exists();
    }

    public ResponseEntity<FileSystemResource> getAudioForPhrase(int phraseId) {
        Optional<Phrase> phraseOptional = phraseRepository.findById(phraseId);
        if (phraseOptional.isEmpty() || phraseOptional.get().getAudioFile() == null) {
            return ResponseEntity.notFound().build();
        }

        File audioFile = new File(audioProperties.getAudioDir() + phraseOptional.get().getAudioFile());
        if (!audioFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(new FileSystemResource(audioFile));
    }

    public ResponseEntity<FileSystemResource> getImageFile(String fileName) {
        File imageFile = new File(mediaProperties.getImageDir(), fileName);
        if (!imageFile.exists()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(new FileSystemResource(imageFile));
    }

    public ResponseEntity<String> saveFiles(MultipartFile[] files, String targetDir, String type) {
        try {
            for (MultipartFile file : files) {
                File destinationFile = new File(targetDir + file.getOriginalFilename());
                file.transferTo(destinationFile);
            }
            return ResponseEntity.ok(type.substring(0, 1).toUpperCase() + type.substring(1) + " files uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload " + type + " files: " + e.getMessage());
        }
    }
}
