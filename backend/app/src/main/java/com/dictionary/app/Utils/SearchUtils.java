package com.dictionary.app.Utils;

public class SearchUtils {
    public static String normalize(String word) {
        return word.toLowerCase()
                .replace('ș', 's')
                .replace('ş', 's')
                .replace('ț', 't')
                .replace('ţ', 't')
                .replace('ă', 'a')
                .replace('î', 'i')
                .replace('â', 'a')
                .replace('ď', 'd')
                .replace('Ď', 'd')
                .replace('ę', 'e')
                .replace('Ę', 'e')
                .replace('í', 'i')
                .replace('Í', 'i')
                .replace('ň', 'n')
                .replace('Ň', 'n')
                .replace('ǫ', 'o')
                .replace('Ǫ', 'o')
                .replace('ó', 'o')
                .replace('ť', 't')
                .replace('Ť', 't')
                .replace('ú', 'u')
                .replace('Ú', 'u')
                .replace("ὶ", "i")
                .replace("cὶ", "ci");

    }
}
