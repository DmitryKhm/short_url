package com.example.shortener.sched;

import com.example.shortener.service.ShortenerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanupScheduler {
    private final ShortenerService service;
    public CleanupScheduler(ShortenerService service) { this.service = service; }

    @Scheduled(fixedRate = 60_000)
    public void cleanup() {
        service.deleteExpired();
    }
}
