package com.dictionary.app.Controllers;

import com.dictionary.app.Services.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        System.out.println("===============================  S-a făcut upload");
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    "Please provide a non-empty Excel file for upload."
            );
        }

        try {
            return excelService.importFromExcel(file);
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Unexpected error while processing the file: " + e.getMessage());
        }

    }
}