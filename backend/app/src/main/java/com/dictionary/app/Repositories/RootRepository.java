package com.dictionary.app.Repositories;

import com.dictionary.app.Models.WordRoot;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RootRepository extends JpaRepository<WordRoot, Integer> {
    Optional<WordRoot> findByName(String rootName);
}
