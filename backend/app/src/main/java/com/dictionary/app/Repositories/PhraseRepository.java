package com.dictionary.app.Repositories;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhraseRepository extends JpaRepository<Phrase, Integer> {
    List<Phrase> findByWord_Id(Integer wordId);
    boolean existsByContentAndWord(String content, Word word);

    List<Phrase> findByRootId(int rootId);

    List<Phrase> findByWord_IdOrderByIdAsc(Integer id);
}
