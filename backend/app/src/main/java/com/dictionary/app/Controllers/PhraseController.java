package com.dictionary.app.Controllers;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Services.PhraseResponse;
import com.dictionary.app.Services.PhraseService;
import com.dictionary.app.Services.RootService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/phrases")
public class PhraseController {
    @Autowired
    private PhraseService phraseService;
    @Autowired
    private RootService rootService;

    @GetMapping("/{rootId}/phrases")
    public ResponseEntity<?> getPhrasesForRoot(@PathVariable Integer rootId) {
        WordRoot root = rootService.findById(rootId);
        if (root == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Root not found");
        }

        // Fetch direct and related phrases
        List<Phrase> directPhrases = phraseService.getDirectPhrasesForRoot(rootId);
        List<Phrase> relatedPhrases = phraseService.getRelatedPhrasesForRoot(rootId);

        // Filter out related phrases that are already in direct phrases
        List<Phrase> filteredRelatedPhrases = phraseService.getFilteredRelatedPhrases(directPhrases, relatedPhrases);

        // Return the response
        return ResponseEntity.ok(new PhraseResponse(directPhrases, filteredRelatedPhrases, root));
    }

    @GetMapping("/{rootId}/processed-phrases")
    public ResponseEntity<?> getProcessedPhrasesForRoot(@PathVariable Integer rootId) {
        WordRoot root = rootService.findById(rootId);
        if (root == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Root not found");
        }

        // Fetch direct and related phrases
        List<Phrase> directPhrases = phraseService.getDirectPhrasesForRoot(rootId);
        List<Phrase> relatedPhrases = phraseService.getRelatedPhrasesForRoot(rootId);

        // Process phrases to include hyperlinks
        List<Phrase> processedDirectPhrases = phraseService.processPhrasesWithHyperlinks(directPhrases);
        List<Phrase> processedRelatedPhrases = phraseService.processPhrasesWithHyperlinks(relatedPhrases);

        return ResponseEntity.ok(new PhraseResponse(processedDirectPhrases, processedRelatedPhrases, root));
    }


}
