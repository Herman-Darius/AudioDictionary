package com.dictionary.app.Services;

import com.dictionary.app.Models.Word;
import com.dictionary.app.Models.WordRoot;
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
    private RootRepository rootRepository;

    @Autowired
    private WordRepository wordRepository;


    @Transactional
    public void importFromExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        // Import roots
        Sheet rootSheet = workbook.getSheet("Roots");
        for (Row row : rootSheet) {
            if(row.getRowNum() == 0) continue; // Skip header row

            String rootName = row.getCell(0).getStringCellValue();

            // Check if the root exists in the database, if not, create it
            Optional<WordRoot> existingRoot = rootRepository.findByName(rootName);
            if (!existingRoot.isPresent()) {
                WordRoot root = new WordRoot();
                root.setName(rootName);
                rootRepository.save(root); // Save new root
            }
        }

        // Import words
        Sheet wordSheet = workbook.getSheet("Words");
        for (Row row : wordSheet) {
            if (row.getRowNum() == 0) continue; // Skip header row

            String wordName = row.getCell(0).getStringCellValue();
            String audioFile = row.getCell(1).getStringCellValue();
            String rootName = row.getCell(2).getStringCellValue();

            if (wordName == null || wordName.isEmpty() || rootName == null || rootName.isEmpty()) {
                continue; // Skip if required values are missing
            }

            // Retrieve the corresponding root from the database
            Optional<WordRoot> optionalRoot = rootRepository.findByName(rootName);
            if (optionalRoot.isPresent()) {
                WordRoot root = optionalRoot.get();
                Word word = new Word();
                word.setWord(wordName);
                word.setAudioFile(audioFile);
                word.setRoot(root); // Associate the word with the existing root
                wordRepository.save(word); // Save the word
            }
        }

        workbook.close(); // Close the workbook to avoid memory leak
    }
}
