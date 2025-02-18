package com.dictionary.app.Repositories;

import com.dictionary.app.Models.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {
}
