package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhraseResponse {

    private List<Phrase> directPhrases;   // Directly related phrases to the root
    private List<Phrase> relatedPhrases;  // Related phrases where root appears in content
    private WordRoot root;                // The root object associated with these phrases
}
