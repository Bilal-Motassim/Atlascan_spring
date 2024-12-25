package com.atlascan_spring.idcardservice.entity;

import com.atlascan_spring.security.entities.User;
import jakarta.persistence.*;

@Entity
public class ExtractedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String arabicData;
    @Lob
    private String frenchData;


    @OneToOne(mappedBy = "idcard", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

    public ExtractedData() {
    }

    public ExtractedData(String arabicData, String frenchData) {
        this.arabicData = arabicData;
        this.frenchData = frenchData;

    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArabicData() {
        return arabicData;
    }

    public void setArabicData(String arabicData) {
        this.arabicData = arabicData;
    }

    public String getFrenchData() {
        return frenchData;
    }

    public void setFrenchData(String frenchData) {
        this.frenchData = frenchData;
    }
}