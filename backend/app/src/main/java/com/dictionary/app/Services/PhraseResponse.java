package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhraseResponse {

    private List<Phrase> directPhrases;   // Directly related phrases
    private List<Phrase> relatedPhrases;  // Related phrases where word appears in content
    private Word word;                    // The word object associated with these phrases
}
