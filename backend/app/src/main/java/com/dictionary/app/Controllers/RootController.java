package com.dictionary.app.Controllers;

import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Services.RootService;
import com.dictionary.app.Services.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roots")
public class RootController {
    @Autowired
    private RootService rootService;

    @GetMapping("/search-root-by-word")
    public List<Map<String, String>> searchRootByWord(@RequestParam String query) {
        return rootService.searchRootsByPrefix(query);
    }

    @GetMapping("/name/{name}")
    public WordRoot getRootByName(@PathVariable String name) {
        return rootService.getRootByName(name);
    }
}
