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
    public ResponseEntity<?> saveExtractedData(@RequestBody ExtractedData data) {
        ExtractedData savedData = service.saveExtractedData(data);
        return ResponseEntity.ok(savedData);
    }

    @GetMapping("/extract/{id}")
    public ResponseEntity<?> getExtractedData(@PathVariable Long  id) {
        Optional<ExtractedData> data = service.getExtractedDataById(id);
        if (data.isPresent()) {
            return ResponseEntity.ok(data.get());
        }
        return ResponseEntity.notFound().build();
    }
}
