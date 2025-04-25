package com.dictionary.app.Services;

import com.dictionary.app.Utils.FileNamingUtils;
import com.dictionary.app.Utils.SearchUtils;
import com.dictionary.app.Models.Phrase;
import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
import com.dictionary.app.Repositories.PhraseRepository;
import com.dictionary.app.Repositories.RootRepository;
import com.dictionary.app.Repositories.WordRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Data
public class ExcelService {
    @Autowired
    private RootRepository rootRepository;
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private PhraseRepository phraseRepository;

    @Transactional
    public ResponseEntity<?> importFromExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            String currentRootName = null;
            WordRoot currentRoot = null;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String root = getCell(row, 0);
                String wordName = getCell(row, 1);
                String wordDef = getCell(row, 2);

                // Set root only if new one is provided in the row
                if (!root.isBlank()) {
                    currentRootName = root;
                    String normalizedRoot = SearchUtils.normalize(currentRootName);

                    // Check for duplicate root
                    WordRoot existingRoot = rootRepository.findByNormalizedNameIgnoreCase(normalizedRoot);
                    if (existingRoot != null) {
                        currentRoot = existingRoot; // Reuse existing root
                    } else {
                        currentRoot = new WordRoot();
                        currentRoot.setName(currentRootName);
                        currentRoot.setNormalizedName(normalizedRoot);
                        currentRoot.setDefinition(""); // Optional: from another column
                        rootRepository.save(currentRoot);
                    }
                }

                if (currentRoot == null || wordName.isBlank()) continue;

                // Check for duplicate word with same root
                boolean wordExists = wordRepository.existsByWordNameIgnoreCaseAndRoot_Id(wordName, currentRoot.getId());
                if (wordExists) continue;

                Word word = new Word();
                word.setWordName(wordName);
                //word.setNormalizedName(SearchUtils.normalize(wordName));
                word.setDefinition(wordDef);
                word.setAudioFile(FileNamingUtils.generateWordAudioFileName(wordName));
                word.setImageFile(FileNamingUtils.generateWordImageFileName(wordName));
                word.setRoot(currentRoot);
                wordRepository.save(word);

                int phraseIndex = 1;

                for (int j = 3; j < row.getLastCellNum(); j += 2) {
                    String phraseContent = getCell(row, j);
                    String phraseDef = getCell(row, j + 1);

                    if (phraseContent.isBlank()) continue;

                    if (phraseRepository.existsByContentAndWord(phraseContent, word)) continue;

                    Phrase phrase = new Phrase();
                    phrase.setContent(phraseContent);
                    phrase.setDefinition(phraseDef);
                    phrase.setAudioFile(FileNamingUtils.generatePhraseAudioFileName(wordName, phraseIndex++));
                    phrase.setRoot(currentRoot);
                    phrase.setWord(word);
                    phraseRepository.save(phrase);
                }

            }

            return ResponseEntity.ok("Excel file imported successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading Excel file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }


    private String getCell(Row row, int index) {
        if (row.getCell(index) == null) return "";
        Cell cell = row.getCell(index);
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue()).trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue()).trim();
            default -> "";
        };
    }
    private List<String> splitCSV(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return Arrays.stream(value.split(",")).map(String::trim).toList();
    }
}
