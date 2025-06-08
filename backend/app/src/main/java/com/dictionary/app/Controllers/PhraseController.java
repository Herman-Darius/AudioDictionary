package com.dictionary.app.Controllers;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Services.PhraseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/phrases")
public class PhraseController {
    @Autowired
    private PhraseService phraseService;


    @GetMapping("/{id}/related-phrases")
    public List<Phrase> getRelatedPhrasesByRoot(@PathVariable Integer id) {
        return phraseService.getRelatedPhrasesByRootWords(id);
    }
    @GetMapping("/{rootId}/phrases")
    public List<Phrase> getPhrases(@PathVariable int rootId) {
        return phraseService.getPhrasesForRoot(rootId);
    }
    @GetMapping("/{rootId}/phrases-with-links")
    public List<Phrase> getPhrasesWithLinks(@PathVariable Integer rootId) {
        return phraseService.getPhrasesWithLinkedWords(rootId);
    }
    @GetMapping("/by-word/{wordId}")
    public ResponseEntity<?> getPhrasesByWordId(@PathVariable Integer wordId) {
        return phraseService.getPhrasesByWordId(wordId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePhrase(@PathVariable Integer id) {
        boolean removed = phraseService.deletePhraseById(id);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Phrase not found"));
        }
        return ResponseEntity.ok().build();
    }

}
