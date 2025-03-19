package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.WordRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
@Data
public class AudioService {
    private final String uploadDir = "C:/Users/Herman Darius-Razvan/Desktop/Licenta-Aplicatie-Mobile/backend/uploads/audio_files/";

    @Autowired
    private final PhraseRepository phraseRepository;
    @Autowired
    private final WordRepository wordRepository;

    public ResponseEntity<FileSystemResource> getAudioForWord(String wordName) {
        // 🔹 Find word in the database
        Optional<Word> wordOptional = wordRepository.findByWordName(wordName);

        if (wordOptional.isEmpty() || wordOptional.get().getAudioFile() == null) {
            return ResponseEntity.notFound().build();
        }

        // 🔹 Get actual stored filename
        String audioFileName = wordOptional.get().getAudioFile();
        File audioFile = new File(uploadDir + audioFileName);

        if (!audioFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 🔹 Return audio file as response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(new FileSystemResource(audioFile));
    }
    public boolean checkIfAudioFileExists(int phraseId) {
        // Retrieve the phrase by ID
        Optional<Phrase> phraseOptional = phraseRepository.findById(phraseId);

        if (phraseOptional.isEmpty() || phraseOptional.get().getAudioFile() == null) {
            return false; // No audio file associated
        }

        // Get the audio file name
        String audioFileName = phraseOptional.get().getAudioFile();
        File audioFile = new File(uploadDir + audioFileName);

        // Check if the file exists in the directory
        return audioFile.exists();
    }

    // Method to retrieve the audio file for the phrase
    public ResponseEntity<FileSystemResource> getAudioForPhrase(int phraseId) {
        // Retrieve the phrase by ID
        Optional<Phrase> phraseOptional = phraseRepository.findById(phraseId);

        if (phraseOptional.isEmpty() || phraseOptional.get().getAudioFile() == null) {
            return ResponseEntity.notFound().build(); // No audio file associated
        }

        // Get the audio file name
        String audioFileName = phraseOptional.get().getAudioFile();
        File audioFile = new File(uploadDir + audioFileName);

        // Check if the file exists in the directory
        if (!audioFile.exists()) {
            return ResponseEntity.notFound().build(); // Audio file doesn't exist
        }

        // Return the audio file as a response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                .body(new FileSystemResource(audioFile));
    }
}
