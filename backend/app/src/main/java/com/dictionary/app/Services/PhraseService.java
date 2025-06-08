package com.dictionary.app.Services;

import com.dictionary.app.DTOs.PhraseDTO;
import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.WordRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
public class PhraseService {
    @Autowired
    private PhraseRepository phraseRepository;

    @Autowired
    private WordRepository wordRepository;

    private static final Logger log = LoggerFactory.getLogger(PhraseService.class);

    public List<Phrase> getDirectPhrasesForRoot(Integer rootId) {
        return phraseRepository.findByRootId(rootId);
    }

    public List<Phrase> getRelatedPhrasesByRootWords(Integer rootId) {
        List<Word> words = wordRepository.findByRootId(rootId);

        Set<String> exactWordSet = words.stream()
                .map(Word::getWordName)
                .collect(Collectors.toSet());

        List<Phrase> allPhrases = phraseRepository.findAll();
        List<Phrase> relatedPhrases = new ArrayList<>();

        for (Phrase phrase : allPhrases) {
            String[] splitWords = phrase.getContent().split("\\s+");

            for (String w : splitWords) {
                String cleaned = w.replaceAll("[^a-zA-Z]", "").toLowerCase(); // Strip punctuation
                if (exactWordSet.contains(cleaned)) {
                    relatedPhrases.add(phrase);
                    break;
                }
            }
        }

        return relatedPhrases;
    }

    public List<Phrase> getFilteredRelatedPhrases(List<Phrase> directPhrases, List<Phrase> relatedPhrases) {
        Set<Integer> directPhraseIds = directPhrases.stream()
                .map(Phrase::getId)
                .collect(Collectors.toSet());

        return relatedPhrases.stream()
                .filter(relatedPhrase -> !directPhraseIds.contains(relatedPhrase.getId()))
                .collect(Collectors.toList());
    }

    public List<Phrase> getPhrasesForRoot(int rootId) {
        return phraseRepository.findByRootId(rootId);
    }

    public List<Phrase> getPhrasesWithLinkedWords(Integer rootId) {
        List<Word> allWords = wordRepository.findAll();
        List<Phrase> phrases = phraseRepository.findByRootId(rootId);

        for (Phrase phrase : phrases) {
            String content = phrase.getContent();

            for (Word word : allWords) {
                if (word.getRoot() == null) continue;

                // Link the word to the root page
                String link = "<a href='app://root/" + word.getRoot().getId() + "'>" + word.getWordName() + "</a>";

                content = content.replaceAll("(?i)\\b" + Pattern.quote(word.getWordName()) + "\\b", link);
            }

            phrase.setContent(content);
        }

        return phrases;
    }

    public ResponseEntity<?> getPhrasesByWordId(Integer wordId) {
        Optional<Word> wordOptional = wordRepository.findById(wordId);

        if (wordOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Word not found for ID: " + wordId));
        }

        List<PhraseDTO> phrases = phraseRepository.findByWord_Id(wordId)
                .stream()
                .map(PhraseDTO::from)
                .toList();

        return ResponseEntity.ok(phrases);
    }


    @Transactional
    public boolean deletePhraseById(Integer id) {
        if (!phraseRepository.existsById(id)) {
            log.warn("Delete requested for non-existent Phrase[id={}]", id);
            return false;
        }
        log.info("Deleting Phrase[id={}]", id);
        phraseRepository.deleteById(id);
        log.info("Deleted Phrase[id={}]", id);
        return true;
    }
}
