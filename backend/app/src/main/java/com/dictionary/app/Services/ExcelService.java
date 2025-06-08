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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    private List<String> formulaWarnings = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);

    /**
     * 4) Import from an already‐saved File, with formula‐injection protection
     */
    @Transactional
    public ResponseEntity<?> importFromExcel(MultipartFile file) {
        log.info("Starting import of Excel file “{}” (size: {} bytes)",
                file.getOriginalFilename(), file.getSize());

        List<String> warnings = new ArrayList<>();
        formulaWarnings = warnings; // ensure same list is used in sanitize()

        if (!isValidExcelFile(file, warnings)) {
            return ResponseEntity.badRequest()
                    .body("File validation failed: " + String.join("; ", warnings));
        }

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            String currentRootName = null;
            WordRoot currentRoot   = null;

            // process rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    log.debug("Row {} is empty, skipping", i);
                    continue;
                }

                // fetch & sanitize
                String rootRaw     = getCell(row, 0);
                String wordRaw     = getCell(row, 1);
                String defRaw      = getCell(row, 2);

                String root     = sanitize(rootRaw, i, 0);
                String wordName = sanitize(wordRaw, i, 1);
                String wordDef  = sanitize(defRaw, i, 2);

                log.debug("Row {} → root='{}', word='{}'", i, root, wordName);

                //Root lookup/creation
                if (!root.isBlank()) {
                    currentRootName = root;
                    String norm = SearchUtils.normalize(root);
                    WordRoot existing = rootRepository.findByNormalizedNameIgnoreCase(norm);
                    if (existing != null) {
                        currentRoot = existing;
                        log.debug("Reusing existing root “{}” (id={})", norm, existing.getId());
                    } else {
                        currentRoot = new WordRoot();
                        currentRoot.setName(root);
                        currentRoot.setNormalizedName(norm);
                        currentRoot.setDefinition("");
                        rootRepository.save(currentRoot);
                        log.info("Created new root “{}” (id={})", norm, currentRoot.getId());
                    }
                }

                if (currentRoot == null || wordName.isBlank()) {
                    log.debug("Row {}: no current root or empty word, skipping", i);
                    continue;
                }

                //Word creation
                boolean exists = wordRepository
                        .existsByWordNameIgnoreCaseAndRoot_Id(wordName, currentRoot.getId());
                if (exists) {
                    log.debug("Word “{}” already exists under root id={}, skipping", wordName, currentRoot.getId());
                    continue;
                }
                Word word = new Word();
                word.setWordName(wordName);
                word.setDefinition(wordDef);
                word.setAudioFile(FileNamingUtils.generateWordAudioFileName(wordName));
                word.setImageFile(FileNamingUtils.generateWordImageFileName(wordName));
                word.setRoot(currentRoot);
                wordRepository.save(word);
                log.info("Created word “{}” (id={})", wordName, word.getId());

                //Phrase creation
                int idx = 1;
                for (int c = 3; c < row.getLastCellNum(); c += 2) {
                    String phraseRaw = getCell(row, c);
                    String pDefRaw   = getCell(row, c + 1);

                    String content = sanitize(phraseRaw, i, c);
                    String pdef    = sanitize(pDefRaw, i, c+1);

                    if (content.isBlank()) continue;
                    if (phraseRepository.existsByContentAndWord(content, word)) {
                        log.debug("Phrase “{}” already exists for word id={}, skipping", content, word.getId());
                        continue;
                    }

                    Phrase phrase = new Phrase();
                    phrase.setContent(content);
                    phrase.setDefinition(pdef);
                    phrase.setAudioFile(FileNamingUtils.generatePhraseAudioFileName(wordName, idx++));
                    phrase.setRoot(currentRoot);
                    phrase.setWord(word);
                    phraseRepository.save(phrase);
                    log.info("  → Added phrase “{}” (id={})", content, phrase.getId());
                }
            }

            if (!formulaWarnings.isEmpty()) {
                String errorMsg = "Import blocked: detected potentially dangerous formulas.\n" +
                        String.join("\n", formulaWarnings);
                log.warn("Blocking import due to formula injection: {}", errorMsg);
                return ResponseEntity.badRequest().body(errorMsg);
            }

            return ResponseEntity.ok("Excel imported successfully!");
        } catch (IOException e) {
            log.error("IOException reading Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("Error reading Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during Excel import", e);
            return ResponseEntity.status(500)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    /** Protect against Excel formula‐injection */
    private String sanitize(String s, int row, int col) {
        if (s != null && !s.isBlank()) {
            String t = s.trim();
            if (t.startsWith("=") || t.startsWith("+") ||
                    t.startsWith("-") || t.startsWith("@")) {
                //String msg = String.format("Formula‐injection attempt at row %d, col %d: \"%s\"", row + 1, col + 1, t);
                String msg = String.format("Posibilă tentativă de injectare de formulă la rândul %d, coloana %d: \"%s\"", row + 1, col + 1, t);

                log.warn(msg);
                formulaWarnings.add(msg);
                return "'" + t;
            }
        }
        return s;
    }

    private String getCell(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue()).trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue()).trim();
            default      -> "";
        };
    }
    /**
     * Validates whether the uploaded file is a genuine Excel (.xlsx) file.
     * Protects against file spoofing (e.g. renaming .exe to .xlsx).
     */
    private boolean isValidExcelFile(MultipartFile file, List<String> warnings) {
        String expectedMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String actualMime = file.getContentType();

        if (actualMime == null || !actualMime.equals(expectedMime)) {
            //String msg = String.format("Invalid MIME type: expected '%s', but got '%s'", expectedMime, actualMime);
            String msg = String.format("Tip MIME invalid: se aștepta '%s', dar s-a primit '%s'", expectedMime, actualMime);
            log.warn(msg);
            warnings.add(msg);
            return false;
        }

        try {
            byte[] header = new byte[4];
            file.getInputStream().read(header);
            boolean valid = header[0] == (byte) 0x50 &&
                    header[1] == (byte) 0x4B &&
                    header[2] == (byte) 0x03 &&
                    header[3] == (byte) 0x04;

            if (!valid) {
                //String msg = "File header does not match .xlsx ZIP signature (possible spoofing)";
                String msg = "Headerul fișierului nu corespunde semnăturii ZIP a unui fișier .xlsx (posibil fișier falsificat)";
                log.warn(msg);
                warnings.add(msg);
            }
            return valid;
        } catch (IOException e) {
            //String msg = "Failed to read file header for magic byte check: " + e.getMessage();
            String msg = "Nu s-a putut citi headerul fișierului pentru verificarea semnăturii: " + e.getMessage();
            log.error(msg, e);
            warnings.add(msg);
            return false;
        }
    }
    public ResponseEntity<String> validateExcelFile(MultipartFile file) {
        List<String> warnings = new ArrayList<>();

        boolean isValid = isValidExcelFile(file, warnings);

        if (!isValid || !warnings.isEmpty()) {
            //String message = "Fișierul a fost respins: " + String.join("\n", warnings);
            String message = "Fișierul a fost respins din următoarele motive:\n" + String.join("\n", warnings);
            log.warn("Validation failed: {}", message);
            return ResponseEntity.badRequest().body(message);
        }

        return ResponseEntity.ok("Valid Excel file");
    }

}
