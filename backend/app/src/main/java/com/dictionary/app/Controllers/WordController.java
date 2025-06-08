package com.dictionary.app.Controllers;

import com.dictionary.app.DTOs.AddWordWithPhrasesDTO;
import com.dictionary.app.DTOs.UpdateWordWithPhrasesDTO;
import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
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


    @GetMapping("/searchByName")
    public ResponseEntity<?> getWordByName(@RequestParam String wordName) {
        return wordService.getWordByName(wordName);
    }


    @GetMapping("/search-by-root")
    public List<Word> searchWordsByRoot(@RequestParam String query) {
        return wordService.searchWordsByRoot(query);
    }

    @GetMapping("/search-root-by-word")
    public List<Map<String, String>> searchRootByWord(@RequestParam String query) {
        return wordService.searchRootsFromWords(query);
    }

    @GetMapping("/by-word")
    public ResponseEntity<WordRoot> getRootByWord(@RequestParam String wordName) {
        WordRoot root = wordService.getRootByWordName(wordName);

        if (root == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(root);
    }

    @GetMapping("all")
    public ResponseEntity<?> getAllWords() {
        return wordService.getAllWords();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Word> getWordById(@PathVariable Integer id) {
        return wordService.getWordById(id);
    }
    @PutMapping("/update-with-phrases")
    public ResponseEntity<?> updateWithPhrases(
            @RequestBody UpdateWordWithPhrasesDTO dto) {
        return wordService.updateWordWithPhrases(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWord(@PathVariable Integer id) {
        boolean removed = wordService.deleteWordById(id);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Word not found: " + id));
        }
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
    @PutMapping("/add-with-phrases")
    public ResponseEntity<?> addWithPhrases(@RequestBody AddWordWithPhrasesDTO dto)
    {
        return wordService.addWordWithPhrases(dto);
    }
}
