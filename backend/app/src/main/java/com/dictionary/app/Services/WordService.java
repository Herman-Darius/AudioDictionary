package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.WordRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Data
public class WordService {
    @Autowired
    private final WordRepository wordRepository;
    @Autowired
    private final PhraseRepository phraseRepository;

    public ResponseEntity<?> searchWords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Search query cannot be empty."));
        }
        List<Word> words = wordRepository.findByWordNameContainingIgnoreCase(query);
        if (words.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No words found containing: " + query));
        }
        return ResponseEntity.ok(words);
    }
    
    public ResponseEntity<?> getWordsByLetter(char letter) {
        if (!Character.isLetter(letter)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid letter format."));
        }

        List<Word> words = wordRepository.findByWordNameStartingWithIgnoreCase(String.valueOf(letter));
        if (words.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "No words found starting with letter: " + letter));
        }
        return ResponseEntity.ok(words);
    }

    /**
     * Retrieve phrases for a given word, and find all phrases containing the word.
     */
    public ResponseEntity<?> getPhrasesForWord(Integer wordId) {
        Optional<Word> wordOpt = wordRepository.findById(wordId);
        if (wordOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Word not found with ID: " + wordId));
        }

        Word selectedWord = wordOpt.get();
        List<Phrase> directPhrases = phraseRepository.findByWordId(wordId);
        List<Phrase> relatedPhrases = phraseRepository.findByContentContainingIgnoreCase(selectedWord.getWordName());

        // Format related phrases by making occurrences of the word clickable
        List<Map<String, Object>> formattedPhrases = relatedPhrases.stream().map(phrase -> {
            Map<String, Object> phraseMap = new HashMap<>();
            phraseMap.put("id", phrase.getId());
            phraseMap.put("audioFile", phrase.getAudioFile());
            phraseMap.put("content", phrase.getContent().replaceAll("(?i)\\b" + selectedWord.getWordName() + "\\b",
                    "<a href='/api/words/" + selectedWord.getWordName() + "'>" + selectedWord.getWordName() + "</a>"));
            phraseMap.put("word", phrase.getWord().getWordName());
            return phraseMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("word", selectedWord);
        response.put("directPhrases", directPhrases);
        response.put("relatedPhrases", formattedPhrases);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Word> getWordById(Integer wordId) {
        // Use Optional to safely handle missing word
        Optional<Word> wordOpt = wordRepository.findById(wordId);

        if (wordOpt.isPresent()) {
            return ResponseEntity.ok(wordOpt.get());  // Return 200 OK with the word object
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)  // Return 404 Not Found if the word is not present
                    .body(null);
        }
    }

    public ResponseEntity<?> getWordByName(String wordName) {
        // Handle empty or invalid word name
        if (wordName == null || wordName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Word name cannot be empty"));
        }

        // Search for the word in the repository
        Word word = wordRepository.findByWordNameIgnoreCase(wordName);

        // If no word is found, return a 404 response
        if (word == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Word not found"));
        }

        // Return the word if found
        return ResponseEntity.ok(word);
    }

    public List<Word> searchWordsByRoot(String query) {
        List<Word> words = wordRepository.findByWordNameContainingIgnoreCase(query);

        for (Word word : words) {
            List<Word> relatedWords = wordRepository.findByRoot(word.getRoot());
            word.setRelatedWords(relatedWords);
        }

        return words;
    }
}
