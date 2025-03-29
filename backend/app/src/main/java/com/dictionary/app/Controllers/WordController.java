package com.dictionary.app.Controllers;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Services.PhraseResponse;
import com.dictionary.app.Services.PhraseService;
import com.dictionary.app.Services.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
public class WordController {
    @Autowired
    private WordService wordService;

    @Autowired
    private PhraseService phraseService;

    @GetMapping("/search")
    public ResponseEntity<?> searchWords(@RequestParam String query) {
        System.out.println(query);
        return wordService.searchWords(query);
    }

    @GetMapping("/letter/{letter}")
    public ResponseEntity<?> getWordsByLetter(@PathVariable char letter) {
        return wordService.getWordsByLetter(letter);
    }

    @GetMapping("/{wordId}/phrases")
    public ResponseEntity<?> getPhrasesForWord(@PathVariable Integer wordId) {
        Word word = wordService.getWordById(wordId).getBody();
        if (word == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Word not found");
        }

        // Fetch direct and related phrases
        List<Phrase> directPhrases = phraseService.getDirectPhrasesForWord(wordId);
        List<Phrase> relatedPhrases = phraseService.getRelatedPhrasesForWord(wordId, word.getWordName());

        // Filter out related phrases that are already in direct phrases
        List<Phrase> filteredRelatedPhrases = phraseService.getFilteredRelatedPhrases(directPhrases, relatedPhrases);

        // Return the response
        return ResponseEntity.ok(new PhraseResponse(directPhrases, filteredRelatedPhrases, word));
    }

    @GetMapping("/searchByName")
    public ResponseEntity<?> getWordByName(@RequestParam String wordName) {
        // Call the service to get the word by name
        return wordService.getWordByName(wordName);
    }

    @GetMapping("/{wordId}/processed-phrases")
    public ResponseEntity<?> getProcessedPhrasesForWord(@PathVariable Integer wordId) {
        Word word = wordService.getWordById(wordId).getBody();
        if (word == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Word not found");
        }

        // Fetch direct and related phrases
        List<Phrase> directPhrases = phraseService.getDirectPhrasesForWord(wordId);
        List<Phrase> relatedPhrases = phraseService.getRelatedPhrasesForWord(wordId, word.getWordName());

        // Process phrases to include hyperlinks
        List<Phrase> processedDirectPhrases = phraseService.processPhrasesWithHyperlinks(directPhrases);
        List<Phrase> processedRelatedPhrases = phraseService.processPhrasesWithHyperlinks(relatedPhrases);

        return ResponseEntity.ok(new PhraseResponse(processedDirectPhrases, processedRelatedPhrases, word));
    }
    @GetMapping("/search-by-root")
    public List<Word> searchWordsByRoot(@RequestParam String query) {
        return wordService.searchWordsByRoot(query);
    }

}
