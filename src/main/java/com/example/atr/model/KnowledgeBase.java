package com.example.atr.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "kb")
@Data
public class KnowledgeBase {

    @Id
    private String id;
    private String url;
    private String title;
    private String text;

}
