package com.dictionary.app.Controllers;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Services.PhraseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/phrases")
public class PhraseController {
    @Autowired
    private PhraseService phraseService;


}
