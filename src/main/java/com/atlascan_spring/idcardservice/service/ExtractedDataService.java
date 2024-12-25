package com.atlascan_spring.idcardservice.service;

import com.atlascan_spring.idcardservice.entity.ExtractedData;
import com.atlascan_spring.idcardservice.repository.ExtractedDataRepository;
import com.atlascan_spring.security.entities.User;
import com.atlascan_spring.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExtractedDataService {

    @Autowired
    private ExtractedDataRepository repository;
    @Autowired
    private UserService userService;

    public ExtractedData saveExtractedData(ExtractedData data, String userEmail) {
        repository.save(data);
        User user = userService.getUserIdByEmail(userEmail);
        user.setIdcard(data);
        userService.save(user);
        return repository.save(data);
    }
    public Optional<ExtractedData> getExtractedDataByUserEmail(String email) {
        return repository.findByUserEmail(email);
    }
}
