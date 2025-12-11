package com.ia.knowledgeai.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ia.knowledgeai.domain.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
}
