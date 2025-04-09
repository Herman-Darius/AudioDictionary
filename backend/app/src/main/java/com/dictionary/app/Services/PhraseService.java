package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.RootRepository;
import com.dictionary.app.Repositories.WordRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Set;
import java.util.HashSet;

@Service
@Data
public class PhraseService {
    @Autowired
    private PhraseRepository phraseRepository;

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private RootRepository rootRepository;

    public List<Phrase> getDirectPhrasesForRoot(Integer rootId) {
        return phraseRepository.findByRootId(rootId);  // Directly associated phrases by rootId
    }

    // Fetch related phrases where any word with the given root appears
    public List<Phrase> getRelatedPhrasesForRoot(Integer rootId) {
        // Find all words linked to the given root
        List<Word> wordsWithRoot = wordRepository.findByRootId(rootId);
        if (wordsWithRoot.isEmpty()) {
            return List.of();
        }

        // Extract word names from these words
        Set<String> wordNames = wordsWithRoot.stream()
                .map(Word::getWordName)
                .collect(Collectors.toSet());

        // Fetch phrases containing any of these words
        return phraseRepository.findAll().stream()
                .filter(phrase -> wordNames.stream().anyMatch(word -> phrase.getContent().toLowerCase().contains(word.toLowerCase())))
                .collect(Collectors.toList());
    }

    // Filter related phrases to exclude direct phrases
    public List<Phrase> getFilteredRelatedPhrases(List<Phrase> directPhrases, List<Phrase> relatedPhrases) {
        Set<Integer> directPhraseIds = directPhrases.stream()
                .map(Phrase::getId)
                .collect(Collectors.toSet());

        return relatedPhrases.stream()
                .filter(relatedPhrase -> !directPhraseIds.contains(relatedPhrase.getId()))
                .collect(Collectors.toList());
    }

    public List<Phrase> processPhrasesWithHyperlinks(List<Phrase> phrases) {
        List<WordRoot> allRoots = rootRepository.findAll(); // Get all roots from the DB

        for (Phrase phrase : phrases) {
            String content = phrase.getContent();

            for (WordRoot root : allRoots) {
                if (content.contains(root.getName())) {
                    String hyperlink = "<a href='/root/" + root.getId() + "'>" + root.getName() + "</a>";
                    content = content.replaceAll("(?i)\\b" + root.getName() + "\\b", hyperlink); // Case-insensitive replacement
                }
            }

            phrase.setContent(content);
        }
        return phrases;
    }

}
