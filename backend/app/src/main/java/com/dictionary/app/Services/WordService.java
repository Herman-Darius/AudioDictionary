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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class WordService {
    @Autowired
    private final WordRepository wordRepository;
    @Autowired
    private final PhraseRepository phraseRepository;


    public ResponseEntity<?> searchWordsNew(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Search query cannot be empty."));
        }

        // Use startingWith instead of containing
        List<Word> words = wordRepository.findByWordNameStartingWithIgnoreCase(query);
        if (words.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No words found starting with: " + query));
        }

        // Group words by their root name
        Map<String, List<Word>> groupedByRoot = words.stream()
                .collect(Collectors.groupingBy(word -> word.getRoot().getName()));

        List<String> formattedResults = new ArrayList<>();

        // Process each group of words with the same root
        for (Map.Entry<String, List<Word>> entry : groupedByRoot.entrySet()) {
            String rootName = entry.getKey();
            List<Word> groupWords = entry.getValue();

            // Build a list of affix information for each word in the group
            List<AffixInfo> infos = new ArrayList<>();
            for (Word w : groupWords) {
                boolean isPrefix = false;
                String affix = "";
                // Check if the word starts with the root; if so, the extra part is the suffix
                if (w.getWordName().startsWith(rootName)) {
                    affix = w.getWordName().substring(rootName.length());
                }
                // Otherwise, if it ends with the root then treat the extra part as a prefix
                else if (w.getWordName().endsWith(rootName)) {
                    affix = w.getWordName().substring(0, w.getWordName().length() - rootName.length());
                    isPrefix = true;
                }
                infos.add(new AffixInfo(w, affix, isPrefix));
            }

            // Choose a base word – preferably one that starts with the root (suffix variant) and with the smallest affix
            AffixInfo baseInfo = infos.stream()
                    .filter(info -> !info.affix.isEmpty() && !info.isPrefix)
                    .min(Comparator.comparingInt(info -> info.affix.length()))
                    .orElse(infos.get(0));

            // Collect unique affixes from the rest (skip the base word)
            Set<String> otherAffixes = infos.stream()
                    .filter(info -> info.word != baseInfo.word && !info.affix.isEmpty())
                    .map(info -> info.isPrefix ? info.affix + "-" : "-" + info.affix)
                    .collect(Collectors.toSet());

            String result = baseInfo.word.getWordName();
            if (!otherAffixes.isEmpty()) {
                result += " (" + String.join(", ", otherAffixes) + ")";
            }
            formattedResults.add(result);
        }

        return ResponseEntity.ok(formattedResults);
    }
    private static class AffixInfo {
        Word word;
        String affix;
        boolean isPrefix;

        AffixInfo(Word word, String affix, boolean isPrefix) {
            this.word = word;
            this.affix = affix;
            this.isPrefix = isPrefix;
        }
    }



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
    /*public ResponseEntity<?> getPhrasesForWord(Integer wordId) {
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
    }*/

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

    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    public ResponseEntity<?> searchWordsWithRoots(String query) {
        List<Word> words = wordRepository.findByWordNameStartingWithIgnoreCase(query);

        Map<String, Map<String, Object>> groupedWords = new HashMap<>();
        for (Word word : words) {
            String rootName = word.getRoot().getName();
            String suffix = word.getSuffix();

            groupedWords.putIfAbsent(rootName, new HashMap<>());
            groupedWords.get(rootName).putIfAbsent("baseWord", word.getWordName());

            List<String> suffixes = (List<String>) groupedWords.get(rootName).getOrDefault("suffixes", new ArrayList<>());
            suffixes.add("-" + suffix);
            groupedWords.get(rootName).put("suffixes", suffixes);
        }

        return ResponseEntity.ok(groupedWords);
    }

}
