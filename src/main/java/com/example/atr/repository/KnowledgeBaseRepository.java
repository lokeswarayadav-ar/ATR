package com.example.atr.repository;

import com.example.atr.model.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, String> {

    @NativeQuery("SELECT generate_rag_response(:query)")
    String generateAutomaticTicketResponse(@Param("query") String query);

}
