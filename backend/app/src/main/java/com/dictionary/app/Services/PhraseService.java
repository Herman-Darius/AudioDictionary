package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Repositories.PhraseRepository;
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

    public List<Phrase> getDirectPhrasesForWord(Integer wordId) {
        return phraseRepository.findByWordId(wordId);  // Directly associated phrases by wordId
    }

    // Fetch Related Phrases (where the word appears in content)
    public List<Phrase> getRelatedPhrasesForWord(Integer wordId, String wordName) {
        return phraseRepository.findByContentContainingIgnoreCase(wordName);  // Matching the word in content
    }

    // Combine and filter related phrases to exclude direct phrases
    public List<Phrase> getFilteredRelatedPhrases(List<Phrase> directPhrases, List<Phrase> relatedPhrases) {
        // Create a set of direct phrase IDs for easy lookup
        Set<Integer> directPhraseIds = directPhrases.stream()
                .map(Phrase::getId)
                .collect(Collectors.toSet());

        // Filter out related phrases whose IDs match direct phrase IDs
        return relatedPhrases.stream()
                .filter(relatedPhrase -> !directPhraseIds.contains(relatedPhrase.getId()))
                .collect(Collectors.toList());
    }

    public List<Phrase> processPhrasesWithHyperlinks(List<Phrase> phrases) {
        List<Word> allWords = wordRepository.findAll(); // Get all words from the DB

        for (Phrase phrase : phrases) {
            String content = phrase.getContent();

            for (Word word : allWords) {
                if (content.contains(word.getWordName())) {
                    String hyperlink = "<a href='/word/" + word.getId() + "'>" + word.getWordName() + "</a>";
                    content = content.replaceAll("(?i)\\b" + word.getWordName() + "\\b", hyperlink); // Case-insensitive replacement
                }
            }

            phrase.setContent(content);
        }
        return phrases;
    }

}
