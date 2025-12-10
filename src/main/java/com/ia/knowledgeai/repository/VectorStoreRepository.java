package com.ia.knowledgeai.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ia.knowledgeai.domain.Chunk;

@Repository
public interface VectorStoreRepository extends JpaRepository<Chunk, UUID> {
}
