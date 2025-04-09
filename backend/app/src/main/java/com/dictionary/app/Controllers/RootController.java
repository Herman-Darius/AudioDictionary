package com.dictionary.app.Controllers;

import com.dictionary.app.Services.RootService;
import com.dictionary.app.Services.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roots")
public class RootController {
    @Autowired
    private RootService rootService;
    @Autowired
    private WordService wordService;

    @GetMapping("/search")
    public ResponseEntity<?> searchRoot(@RequestParam String query) {
        System.out.println(query);
        return rootService.searchRootStartingWith(query);
    }
}
