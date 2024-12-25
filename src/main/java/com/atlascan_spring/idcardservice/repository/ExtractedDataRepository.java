package com.atlascan_spring.idcardservice.repository;

import com.atlascan_spring.idcardservice.entity.ExtractedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExtractedDataRepository extends JpaRepository<ExtractedData, Long> {
    
}
