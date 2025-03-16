package com.dictionary.app.Repositories;

import com.dictionary.app.Models.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Integer> {
    List<Word> findByWordNameContainingIgnoreCase(String word);
    List<Word> findByWordNameStartingWithIgnoreCase(String word);

    Word findByWordNameIgnoreCase(String wordName);
}
