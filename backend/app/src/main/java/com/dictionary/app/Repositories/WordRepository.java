package com.dictionary.app.Repositories;

import com.dictionary.app.Models.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Integer> {
}
