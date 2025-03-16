package com.dictionary.app.Services;

import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.RootRepository;
import com.dictionary.app.Repositories.WordRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@Data
public class ExcelService {
    @Autowired
    private final RootRepository rootRepository;

    @Autowired
    private final WordRepository wordRepository;

    @Autowired
    private final PhraseRepository phraseRepository;


    @Transactional
    public ResponseEntity<?> importFromExcel(MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());

            // Import roots
            Sheet rootSheet = workbook.getSheet("Roots");
            for (Row row : rootSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String rootName = row.getCell(0).getStringCellValue().trim();
                if (rootName.isEmpty()) continue;

                // Check if the root exists in the database, if not, create it
                if (!rootRepository.findByName(rootName).isPresent()) {
                    WordRoot root = new WordRoot();
                    root.setName(rootName);
                    rootRepository.save(root);
                }
            }

            // Import words and their phrases
            Sheet wordSheet = workbook.getSheet("Words");
            for (Row row : wordSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String wordName = row.getCell(0).getStringCellValue().trim();
                String audioFile = row.getCell(1).getStringCellValue().trim();
                String rootName = row.getCell(2).getStringCellValue().trim();
                String phrases = row.getCell(3) != null ? row.getCell(3).getStringCellValue().trim() : "";

                if (wordName.isEmpty() || rootName.isEmpty()) {
                    return ResponseEntity.badRequest().body("Word or Root cannot be empty!");
                }

                // Retrieve the corresponding root from the database
                Optional<WordRoot> optionalRoot = rootRepository.findByName(rootName);
                if (!optionalRoot.isPresent()) {
                    return ResponseEntity.badRequest().body("Root not found: " + rootName);
                }

                WordRoot root = optionalRoot.get();
                Word word = new Word();
                word.setWordName(wordName);
                word.setAudioFile(audioFile);
                word.setRoot(root); // Associate the word with the existing root
                wordRepository.save(word);

                // Handle phrases for this word
                if (!phrases.isEmpty()) {
                    String[] phraseArray = phrases.split(",");
                    for (String phraseText : phraseArray) {
                        Phrase phrase = new Phrase();
                        phrase.setContent(phraseText.trim()); // Trim spaces and save the phrase
                        phrase.setWord(word); // Associate the phrase with the word
                        phraseRepository.save(phrase);
                    }
                }
            }

            workbook.close();
            return ResponseEntity.ok("Excel file imported successfully!");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading Excel file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }
}
