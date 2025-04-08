package com.example.atr.controller;

import com.example.atr.service.AutomaticTicketResponseService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutomaticTicketResponseController {

    @Autowired
    private AutomaticTicketResponseService automaticTicketResponseService;

    @PostMapping(value = "generateAutomaticTicketResponse",produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String generateAutomaticTicketResponse(@RequestBody Map<String, String> params) {
        return automaticTicketResponseService.generateAutomaticTicketResponse(params.get("query"));
    }

}
