package com.example.atr.service;

import com.example.atr.repository.KnowledgeBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutomaticTicketResponseService {

    @Autowired
    private KnowledgeBaseRepository knowledgeBaseRepository;

    public String generateAutomaticTicketResponse(String query) {
        return knowledgeBaseRepository.generateAutomaticTicketResponse(query);
    }
}
