package com.dictionary.app.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class UpdateWordWithPhrasesDTO {
    private WordDTO word;
    private List<PhraseDTO> phrases;

    @Data
    public static class WordDTO {
        private Integer id;
        private String wordName;
        private String definition;
        private String rootName;
    }
}
