package com.dictionary.app.Repositories;

import com.dictionary.app.Models.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhraseRepository extends JpaRepository<Phrase, Integer> {
    List<Phrase> findByRootId(Integer wordId);

    List<Phrase> findByContentContainingIgnoreCase(String word);


}
