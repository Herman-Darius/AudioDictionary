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

                // Check if the root exists, if not, create it
                if (!rootRepository.findByName(rootName).isPresent()) {
                    WordRoot root = new WordRoot();
                    root.setName(rootName);

                    // ADD THIS: Import definition (column index 1 assumed)
                    if (row.getCell(1) != null) {
                        root.setDefinition(row.getCell(1).getStringCellValue().trim());
                    }

                    rootRepository.save(root);
                }
            }

            // Import words using root + prefix/suffix
            Sheet wordSheet = workbook.getSheet("Words");
            for (Row row : wordSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String rootName = row.getCell(0).getStringCellValue().trim();
                String suffix = row.getCell(1) != null ? row.getCell(1).getStringCellValue().trim() : "";
                String prefix = row.getCell(2) != null ? row.getCell(2).getStringCellValue().trim() : "";
                String audioFile = row.getCell(3) != null ? row.getCell(3).getStringCellValue().trim() : "";
                String phrases = row.getCell(4) != null ? row.getCell(4).getStringCellValue().trim() : "";

                if (rootName.isEmpty()) {
                    return ResponseEntity.badRequest().body("Root cannot be empty!");
                }

                Optional<WordRoot> optionalRoot = rootRepository.findByName(rootName);
                if (!optionalRoot.isPresent()) {
                    return ResponseEntity.badRequest().body("Root not found: " + rootName);
                }

                WordRoot root = optionalRoot.get();
                String fullWord = prefix + rootName + suffix; // Construct the word

                Word word = new Word();
                word.setWordName(fullWord);
                word.setAudioFile(audioFile);
                word.setRoot(root);
                wordRepository.save(word);
            }

            // Import phrases and bind them to ROOT instead of WORD
            Sheet phraseSheet = workbook.getSheet("Phrases");
            for (Row row : phraseSheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                String rootName = row.getCell(0).getStringCellValue().trim();
                String phraseText = row.getCell(1).getStringCellValue().trim();
                String explication = row.getCell(2).getStringCellValue().trim();
                String audioFile = row.getCell(3) != null ? row.getCell(3).getStringCellValue().trim() : "";

                if (rootName.isEmpty() || phraseText.isEmpty() || explication.isEmpty()) {
                    continue; // Skip invalid rows
                }

                Optional<WordRoot> optionalRoot = rootRepository.findByName(rootName);
                if (!optionalRoot.isPresent()) {
                    return ResponseEntity.badRequest().body("Root not found for phrase: " + rootName);
                }

                WordRoot root = optionalRoot.get();

                Phrase phrase = new Phrase();
                phrase.setContent("<b><i>" + phraseText + "</i></b>");
                phrase.setExplication(explication);
                phrase.setRoot(root); // Bind to ROOT instead of WORD
                phrase.setAudioFile(audioFile);
                phraseRepository.save(phrase);
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
