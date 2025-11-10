package com.example.shortener.repo;

import com.example.shortener.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByShortCode(String shortCode);
    List<Link> findByExpiresAtBefore(Instant time);
    List<Link> findByOwnerUuid(String ownerUuid);
}
