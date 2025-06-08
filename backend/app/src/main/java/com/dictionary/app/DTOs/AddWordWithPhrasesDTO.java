package com.dictionary.app.DTOs;

import lombok.Data;

import java.util.List;

@Data
public class AddWordWithPhrasesDTO {
    private String wordName;
    private String definition;
    private String rootName;

    private List<PhraseDTO> phrases;
}