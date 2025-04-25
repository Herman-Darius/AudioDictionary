package com.dictionary.app.Services;

import com.dictionary.app.Utils.SearchUtils;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.WordRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

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

        List<Word> words = wordRepository.findByWordNameStartingWithIgnoreCase(query);

        if (words.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No words found starting with: " + query));
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


    public ResponseEntity<Word> getWordById(Integer wordId) {
        Optional<Word> wordOpt = wordRepository.findById(wordId);
        return wordOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    public ResponseEntity<?> getWordByName(String wordName) {
        if (wordName == null || wordName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Word name cannot be empty"));
        }
        Word word = wordRepository.findByWordNameIgnoreCase(wordName);

        if (word == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Word not found"));
        }

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



    public List<Map<String, String>> searchRootsFromWords(String query) {
        List<Word> words = wordRepository.findByWordNameStartingWithIgnoreCase(query);

        Set<WordRoot> uniqueRoots = new HashSet<>();
        for (Word word : words) {
            if (word.getRoot() != null) {
                uniqueRoots.add(word.getRoot());
            }
        }

        List<Map<String, String>> response = new ArrayList<>();
        for (WordRoot root : uniqueRoots) {
            Map<String, String> rootData = new HashMap<>();
            rootData.put("root", root.getName());
            rootData.put("rootDefinition", root.getDefinition());
            response.add(rootData);
        }

        return response;
    }

    public WordRoot getRootByWordName(String wordName) {
        if (wordName == null || wordName.trim().isEmpty()) return null;


        Word word = wordRepository.findByWordNameIgnoreCase(wordName);

        return word != null ? word.getRoot() : null;
    }

    public ResponseEntity<?> getAllWords() {
        List<Word> words = wordRepository.findAll();
        if (words.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "No words found."));
        }
        return ResponseEntity.ok(words);
    }

    /*
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void normalizeAllWordsOnce() {
        int page = 0;
        int pageSize = 500;

        Page<Word> pageData;

        do {
            pageData = wordRepository.findAll(PageRequest.of(page, pageSize));
            List<Word> batch = pageData.getContent();

            for (Word word : batch) {
                word.setNormalizedName(SearchUtils.normalize(word.getWordName()));
            }

            wordRepository.saveAll(batch);
            page++;

        } while (!pageData.isLast());

        System.out.println("Word normalization completed.");
    }

     */
}
