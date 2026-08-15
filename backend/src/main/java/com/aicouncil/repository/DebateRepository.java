package com.aicouncil.repository;

import com.aicouncil.entity.Debate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebateRepository extends MongoRepository<Debate, String> {
    List<Debate> findAllByOrderByCreatedAtDesc();
}
