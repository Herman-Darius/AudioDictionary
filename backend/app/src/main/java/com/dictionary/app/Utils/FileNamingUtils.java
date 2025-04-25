package com.dictionary.app.Utils;

public class FileNamingUtils {

    public static String generateWordAudioFileName(String wordName) {
        return sanitize(wordName) + ".mp3";
    }

    public static String generateWordImageFileName(String wordName) {
        return sanitize(wordName) + ".jpg";
    }

    public static String generatePhraseAudioFileName(String wordName, int index) {
        return sanitize(wordName) + "_" + index + ".mp3";
    }

    private static String sanitize(String input) {
        return input.trim().toLowerCase().replaceAll("\\s+", "_");
    }
}