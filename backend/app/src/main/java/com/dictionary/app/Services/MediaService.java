package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.WordRepository;
import com.dictionary.app.Security.MediaProperties;
import lombok.Data;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
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


    //Sa fac pentru admin app
    /** true if the word has an imageFile set *and* the file actually exists on disk */
    public boolean checkIfWordImageExists(int wordId) {
        Optional<Word> opt = wordRepository.findById(wordId);
        if (opt.isEmpty()) return false;

        String imgFile = opt.get().getImageFile();
        if (imgFile == null) return false;

        File f = new File(mediaProperties.getImageDir(), imgFile);
        return f.exists();
    }

    /** true if the word has an audioFile set *and* the file actually exists on disk */
    public boolean checkIfWordAudioExists(int wordId) {
        Optional<Word> opt = wordRepository.findById(wordId);
        if (opt.isEmpty()) return false;

        String audio = opt.get().getAudioFile();
        if (audio == null) return false;

        File f = new File(audioProperties.getAudioDir(), audio);
        return f.exists();
    }
    /**
     * Upload and save a single audio file for a Word.
     * The file is renamed to the DB’s audioFile name (or if none exists, to wordName.ext).
     * Also updates the Word.audioFile column if it was previously null.
     */
    public ResponseEntity<String> saveWordAudio(int wordId, MultipartFile file) {
        var opt = wordRepository.findById(wordId);
        if (opt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Word not found: " + wordId);

        Word w = opt.get();
        String ext      = getExtension(file.getOriginalFilename());
        String filename = w.getWordName().replaceAll("\\s+","_") + ext;

        try {
            File dest = new File(audioProperties.getAudioDir(), filename);
            file.transferTo(dest);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save audio: " + ex.getMessage());
        }

        w.setAudioFile(filename);
        wordRepository.save(w);
        return ResponseEntity.ok(filename);
    }


    /**
     * Upload and save a single image file for a Word.
     * Behaves analogously to saveWordAudio.
     */
    public ResponseEntity<String> saveWordImage(int wordId, MultipartFile file) {
        var opt = wordRepository.findById(wordId);
        if (opt.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Word not found: " + wordId);

        Word w = opt.get();
        String ext      = getExtension(file.getOriginalFilename());
        String filename = w.getWordName().replaceAll("\\s+","_") + ext;

        try {
            File dest = new File(mediaProperties.getImageDir(), filename);
            file.transferTo(dest);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save image: " + ex.getMessage());
        }

        w.setImageFile(filename);
        wordRepository.save(w);
        return ResponseEntity.ok(filename);
    }

    /**
     * Upload and save a single audio file for a Phrase.
     * Always renames it to "{wordName}_{index}{ext}" and saves that name to the DB.
     */
    public ResponseEntity<String> savePhraseAudio(int phraseId, MultipartFile file) {
        Optional<Phrase> opt = phraseRepository.findById(phraseId);
        if (opt.isEmpty())
            return ResponseEntity.notFound().build();

        Phrase p = opt.get();
        String wordName = p.getWord()
                .getWordName()
                .replaceAll("\\s+","_");

        List<Phrase> siblings =
                phraseRepository.findByWord_IdOrderByIdAsc(p.getWord().getId());
        int idx = 1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(phraseId)) {
                idx = i + 1;
                break;
            }
        }

        String ext = getExtension(file.getOriginalFilename());
        String filename = String.format("%s_%d%s", wordName, idx, ext);

        try {
            File dest = new File(audioProperties.getAudioDir(), filename);
            file.transferTo(dest);
        } catch (IOException ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save phrase audio: " + ex.getMessage());
        }

        p.setAudioFile(filename);
        phraseRepository.save(p);

        return ResponseEntity.ok(filename);
    }

    private String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i) : "";
    }

}
