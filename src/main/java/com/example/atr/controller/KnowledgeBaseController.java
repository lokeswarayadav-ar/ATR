package com.example.atr.controller;

import com.example.atr.service.KnowledgeBaseService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @GetMapping("/import")
    public void importKnowledgeBase() throws IOException {
        knowledgeBaseService.importKnowledgeBase();
    }

}
