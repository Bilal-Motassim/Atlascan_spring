package com.atlascan_spring.ocr;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Scanner;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Value("${ocr.api.key}")
    private String apiKey;

    @Value("${ocr.ip.address}")
    private String ocrIpAddress;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> extractOcrData(@RequestParam("image") MultipartFile imageFile) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String ocrApiUrl = "http://"+ocrIpAddress+":5000/extract";
            HttpPost uploadFile = new HttpPost(ocrApiUrl);
            uploadFile.setHeader("x-api-key", apiKey);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("image", imageFile.getInputStream(),
                    ContentType.MULTIPART_FORM_DATA, imageFile.getOriginalFilename());

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();
            InputStream responseStream = responseEntity.getContent();
            String result = new Scanner(responseStream, "UTF-8").useDelimiter("\\A").next();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity.ok().headers(headers).body(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
