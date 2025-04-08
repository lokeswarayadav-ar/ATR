package com.example.atr.service;

import com.example.atr.model.KnowledgeBase;
import com.example.atr.repository.KnowledgeBaseRepository;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseService {
    String baseUrl = "https://knowledgebase.autorabit.com/";

    @Autowired
    KnowledgeBaseRepository knowledgeBaseRepository;

    public void importKnowledgeBase() throws IOException {

        ClassPathResource knowledgeBase = new ClassPathResource("knowledgebase");
        File knowledgeBaseDirectory = knowledgeBase.getFile();

        processDirectory(knowledgeBaseDirectory, 1);
    }

    private int processDirectory(File directory, int idCounter) throws IOException {

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            assert files != null;
            for (File file : files) {
                if (file.isFile()) {

                    KnowledgeBase knowledgeBase = new KnowledgeBase();

                    String fileName = file.getName();
                    String fileNameWithoutExtension = fileName.substring(0, file.getName().lastIndexOf('.'));

                    String content = FileUtils.readFileToString(file, "UTF-8");
                    System.out.println(fileName + ": " + fileNameWithoutExtension +":" +idCounter);

                    knowledgeBase.setId(String.valueOf(idCounter));
                    knowledgeBase.setUrl(baseUrl);
                    knowledgeBase.setTitle(fileNameWithoutExtension);
                    knowledgeBase.setText(content);
                    knowledgeBaseRepository.save(knowledgeBase);

                    idCounter++;
                } else if (file.isDirectory()) {
                    idCounter = processDirectory(file, idCounter);
                }
            }

        } else {
            System.err.println("The directory '" + directory.getName() + "' does not exist.");
        }
        return idCounter;
    }
}
