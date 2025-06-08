package com.dictionary.app.Services;
import com.dictionary.app.DTOs.AddWordWithPhrasesDTO;
import com.dictionary.app.DTOs.PhraseDTO;
import com.dictionary.app.DTOs.UpdateWordWithPhrasesDTO;
import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Repositories.RootRepository;
import com.dictionary.app.Utils.SearchUtils;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.WordRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
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
    @Autowired
    private final RootRepository rootRepository;
    private static final Logger log = LoggerFactory.getLogger(WordService.class);



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

    @Transactional
    public ResponseEntity<?> updateWordWithPhrases(UpdateWordWithPhrasesDTO dto) {
        var wd = dto.getWord();
        log.info("Starting update of Word[id={}, name={}]", wd.getId(), wd.getWordName());

        // 1) Load & update Word
        Optional<Word> wOpt = wordRepository.findById(wd.getId());
        if (wOpt.isEmpty()) {
            log.warn("Word not found: id={}", wd.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Word not found: " + wd.getId()));
        }
        Word w = wOpt.get();
        w.setWordName(wd.getWordName());
        w.setDefinition(wd.getDefinition());

        // 1a) Handle rootName
        String rn = wd.getRootName();
        if (rn != null && !rn.isBlank()) {
            WordRoot root = rootRepository.findByNameIgnoreCase(rn);
            if (root == null) {
                log.info("Creating new Root[name={}]", rn);
                root = new WordRoot();
                root.setName(rn);
                root.setNormalizedName(SearchUtils.normalize(rn));
                root.setDefinition(null);
                rootRepository.save(root);
            } else {
                log.debug("Reusing existing Root[id={}, name={}]", root.getId(), root.getName());
            }
            w.setRoot(root);
        }

        wordRepository.save(w);
        log.info("Saved updates to Word[id={}]", w.getId());

        // 2) Synchronize phrases
        List<Phrase> existing = phraseRepository.findByWord_Id(w.getId());
        List<Integer> incomingIds = dto.getPhrases().stream()
                .map(PhraseDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2a) Delete removed phrases
        for (Phrase p : existing) {
            if (!incomingIds.contains(p.getId())) {
                log.info("Deleting Phrase[id={}] as it was removed in DTO", p.getId());
                phraseRepository.delete(p);
            }
        }

        // 2b) Upsert incoming phrases
        for (PhraseDTO pd : dto.getPhrases()) {
            if (pd.getId() != null) {
                // update
                Optional<Phrase> pOpt = phraseRepository.findById(pd.getId());
                if (pOpt.isPresent()) {
                    Phrase p = pOpt.get();
                    log.info("Updating Phrase[id={}] for Word[id={}]", p.getId(), w.getId());
                    p.setContent(pd.getContent());
                    p.setDefinition(pd.getDefinition());
                    if (pd.getAudioFile() != null) {
                        p.setAudioFile(pd.getAudioFile());
                        log.debug(" Set new audioFile='{}' on Phrase[id={}]", pd.getAudioFile(), p.getId());
                    }
                    phraseRepository.save(p);
                }
            } else {
                // insert new
                log.info("Creating new Phrase for Word[id={}] with content='{}'", w.getId(), pd.getContent());
                Phrase p = new Phrase();
                p.setContent(pd.getContent());
                p.setDefinition(pd.getDefinition());
                p.setAudioFile(pd.getAudioFile());
                p.setWord(w);
                p.setRoot(w.getRoot());
                phraseRepository.save(p);
            }
        }

        log.info("Finished updating Word[id={}] and its phrases", w.getId());
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @Transactional
    public boolean deleteWordById(Integer wordId) {
        if (!wordRepository.existsById(wordId)) {
            log.warn("Attempted to delete non-existent Word[id={}]", wordId);
            return false;
        }

        List<Phrase> phrases = phraseRepository.findByWord_Id(wordId);
        log.info("Deleting Word[id={}] and its {} associated phrase(s)", wordId, phrases.size());

        phraseRepository.deleteAll(phrases);
        log.debug("Deleted {} phrase(s) for Word[id={}]", phrases.size(), wordId);

        wordRepository.deleteById(wordId);
        log.info("Deleted Word[id={}]", wordId);

        return true;
    }

    @Transactional
    public ResponseEntity<?> addWordWithPhrases(AddWordWithPhrasesDTO dto) {
        log.info("Starting creation of Word[name='{}']", dto.getWordName());

        WordRoot root = rootRepository.findByNameIgnoreCase(dto.getRootName());
        if (root == null) {
            log.info("Root[name='{}'] not found — creating new", dto.getRootName());
            root = new WordRoot();
            root.setName(dto.getRootName());
            root = rootRepository.save(root);
            log.debug("Created Root[id={}, name={}]", root.getId(), root.getName());
        } else {
            log.debug("Reusing existing Root[id={}, name={}]", root.getId(), root.getName());
        }

        Word w = new Word();
        w.setWordName(dto.getWordName());
        w.setDefinition(dto.getDefinition());
        w.setRoot(root);
        w = wordRepository.save(w);
        log.info("Created Word[id={}, name='{}']", w.getId(), w.getWordName());

        int idx = 1;
        for (var pDto : dto.getPhrases()) {
            if (pDto.getContent() == null || pDto.getContent().isBlank()) {
                log.debug("Skipping empty Phrase in DTO at index {}", idx - 1);
                continue;
            }

            log.info("Creating Phrase[{}] for Word[id={}]", idx, w.getId());
            Phrase p = new Phrase();
            p.setContent(pDto.getContent());
            p.setDefinition(pDto.getDefinition());
            p.setRoot(root);
            p.setWord(w);

            String key = w.getWordName().replaceAll("\\s+","_");
            String filename = key + "_" + (idx++) + ".mp3";
            p.setAudioFile(filename);
            log.debug("  → audioFile='{}'", filename);

            phraseRepository.save(p);
        }

        log.info("Finished creation of Word[id={}] and {} phrases", w.getId(), idx - 1);
        return ResponseEntity.ok(Map.of("message", "Created word + phrases"));
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
