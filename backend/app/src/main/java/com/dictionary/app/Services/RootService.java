package com.dictionary.app.Services;

import com.dictionary.app.Utils.SearchUtils;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Repositories.RootRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Data
public class RootService {
    @Autowired
    private RootRepository rootRepository;

    public List<Map<String, String>> searchRootsByPrefix(String prefix) {
        String normalizedPrefix = SearchUtils.normalize(prefix);
        List<WordRoot> matchingRoots = rootRepository.findByNormalizedNameStartingWithIgnoreCase(normalizedPrefix);

        List<Map<String, String>> response = new ArrayList<>();
        for (WordRoot root : matchingRoots) {
            Map<String, String> rootData = new HashMap<>();
            rootData.put("root", root.getName());
            rootData.put("rootDefinition", root.getDefinition());
            response.add(rootData);
        }

        return response;
    }

    public WordRoot getRootByName(String name) {
        String normalized = SearchUtils.normalize(name);
        return rootRepository.findByNormalizedNameIgnoreCase(normalized);
    }

    @Scheduled(initialDelay = 12000, fixedDelay = Long.MAX_VALUE)
    public void normalizeAllRootsOnce() {
        List<WordRoot> roots = rootRepository.findAll();

        for (WordRoot root : roots) {
            root.setNormalizedName(SearchUtils.normalize(root.getName()));
        }

        rootRepository.saveAll(roots);
        System.out.println("Root normalization completed.");
    }
}

