package com.dictionary.app.Services;

import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Repositories.RootRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Data
@AllArgsConstructor
public class RootService {
    private static RootRepository rootRepository;

    public ResponseEntity<?> searchRootStartingWith(String query){
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Search query cannot be empty."));
        }
        List<WordRoot> roots = rootRepository.findByNameStartingWithIgnoreCase(query);
        if (roots.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No words found containing: " + query));
        }
        return ResponseEntity.ok(roots);
    }


    public WordRoot findById(Integer rootId) {
        return rootRepository.findById(rootId).orElse(null);
    }
}
