package com.dictionary.app.DTOs;

import com.dictionary.app.Models.Phrase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhraseDTO {
    private Integer id;
    private String content;
    private String definition;
    private String audioFile;

    public static PhraseDTO from(Phrase phrase) {
        return new PhraseDTO(
                phrase.getId(),
                phrase.getContent(),
                phrase.getDefinition(),
                phrase.getAudioFile()
        );
    }
}