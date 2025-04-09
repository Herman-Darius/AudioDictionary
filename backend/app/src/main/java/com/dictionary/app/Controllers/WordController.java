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
        return wordService.searchWordsNew(query);
    }

    @GetMapping("/letter/{letter}")
    public ResponseEntity<?> getWordsByLetter(@PathVariable char letter) {
        return wordService.getWordsByLetter(letter);
    }



    @GetMapping("/name/{wordName}")
    public ResponseEntity<?> getWordByName(@PathVariable String wordName) {
        // Call the service to get the word by name
        return wordService.getWordByName(wordName);
    }

    @GetMapping("/search-by-root")
    public List<Word> searchWordsByRoot(@RequestParam String query) {
        return wordService.searchWordsByRoot(query);
    }

    @GetMapping("/all")
    public List<Word> getAllWords() {
        return wordService.getAllWords();
    }

}
