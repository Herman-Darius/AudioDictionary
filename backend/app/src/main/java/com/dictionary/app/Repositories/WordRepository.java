package com.dictionary.app.Repositories;

import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Integer> {
    List<Word> findByWordNameContainingIgnoreCase(String word);
    List<Word> findByWordNameStartingWithIgnoreCase(String word);

    Word findByWordNameIgnoreCase(String wordName);
    List<Word> findByRoot(WordRoot root);
    Optional<Word> findByWordName(String wordName);
}
