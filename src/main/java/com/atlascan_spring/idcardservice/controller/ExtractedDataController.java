package com.atlascan_spring.idcardservice.controller;

import com.atlascan_spring.idcardservice.entity.ExtractedData;
import com.atlascan_spring.idcardservice.service.ExtractedDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/id-card-info")
public class ExtractedDataController {

    @Autowired
    private ExtractedDataService service;

    @PostMapping("/save")
    public ResponseEntity<?> saveExtractedData(@RequestBody ExtractedData data, @RequestParam String userEmail) {
        ExtractedData savedData = service.saveExtractedData(data, userEmail);
        return ResponseEntity.ok(savedData);
    }

    @GetMapping("/extract/{id}")
    public ResponseEntity<?> getExtractedData(@PathVariable String  userEmail) {
        Optional<ExtractedData> data = service.getExtractedDataByUserEmail(userEmail);
        if (data.isPresent()) {
            return ResponseEntity.ok(data.get());
        }
        return ResponseEntity.notFound().build();
    }
}
