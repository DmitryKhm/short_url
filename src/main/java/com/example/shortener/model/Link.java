package com.example.shortener.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "links", indexes = {
    @Index(name = "idx_code", columnList = "shortCode", unique = true)
})
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 36)
    private String ownerUuid;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false, length = 64, unique = true)
    private String shortCode;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Integer maxClicks; // null = unlimited
    private Integer clickCount = 0;

    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(String ownerUuid) { this.ownerUuid = ownerUuid; }

    public String getLongUrl() { return longUrl; }
    public void setLongUrl(String longUrl) { this.longUrl = longUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Integer getMaxClicks() { return maxClicks; }
    public void setMaxClicks(Integer maxClicks) { this.maxClicks = maxClicks; }

    public Integer getClickCount() { return clickCount; }
    public void setClickCount(Integer clickCount) { this.clickCount = clickCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
