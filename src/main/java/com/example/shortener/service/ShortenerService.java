package com.example.shortener.service;

import com.example.shortener.model.Link;
import com.example.shortener.notify.NotificationService;
import com.example.shortener.repo.LinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;

@Service
public class ShortenerService {
    private final LinkRepository repo;
    private final NotificationService notifier;
    private final int ttlMinutes;
    private final int shortLen;
    private final Random random = new Random();

    public ShortenerService(LinkRepository repo, NotificationService notifier,
                            @Value("${link.ttl.minutes}") int ttlMinutes,
                            @Value("${link.short.length}") int shortLen) {
        this.repo = repo;
        this.notifier = notifier;
        this.ttlMinutes = ttlMinutes;
        this.shortLen = shortLen;
    }

    public Link createShortLink(String ownerUuid, String longUrl, Integer maxClicks) {
        try {
            int attempts = 0;
            String code;
            do {
                code = generateCode(ownerUuid, longUrl, attempts++);
            } while (repo.findByShortCode(code).isPresent());

            Link link = new Link();
            link.setOwnerUuid(ownerUuid);
            link.setLongUrl(longUrl);
            link.setShortCode(code);
            Instant now = Instant.now();
            link.setCreatedAt(now);
            link.setExpiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES));
            link.setMaxClicks(maxClicks);
            link.setClickCount(0);
            link.setActive(true);

            repo.save(link);
            return link;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateCode(String ownerUuid, String longUrl, int nonce) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String input = ownerUuid + "|" + longUrl + "|" + System.currentTimeMillis() + "|" + nonce + "|" + random.nextInt();
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        String base62 = base62(hash);
        return base62.substring(0, Math.min(shortLen, base62.length()));
    }

    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private String base62(byte[] input) {
        long val = 0;
        for (int i = 0; i < Math.min(8, input.length); i++) {
            val = (val << 8) | (input[i] & 0xff);
        }
        if (val == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (val > 0) {
            sb.append(BASE62[(int)(val % 62)]);
            val /= 62;
        }
        return sb.reverse().toString();
    }

    @Transactional
    public Optional<String> handleRedirect(String code) {
        Optional<Link> opt = repo.findByShortCode(code);
        if (opt.isEmpty()) return Optional.empty();
        Link link = opt.get();

        if (!link.isActive() || link.getExpiresAt().isBefore(Instant.now())) {
            link.setActive(false);
            repo.save(link);
            notifier.notifyUser(link.getOwnerUuid(), "Link expired", "Link " + code + " expired and is now disabled.");
            return Optional.empty();
        }

        Integer max = link.getMaxClicks();
        int nextCount = link.getClickCount() + 1;
        if (max != null && nextCount > max) {
            link.setActive(false);
            repo.save(link);
            notifier.notifyUser(link.getOwnerUuid(), "Click limit reached", "Link " + code + " reached click limit and disabled.");
            return Optional.empty();
        }

        link.setClickCount(nextCount);
        if (max != null && nextCount == max) {
            notifier.notifyUser(link.getOwnerUuid(), "Click limit reached", "Link " + code + " reached click limit and will be disabled.");
            link.setActive(false);
        }
        repo.save(link);
        return Optional.of(link.getLongUrl());
    }

    public void deleteExpired() {
        Instant now = Instant.now();
        repo.findByExpiresAtBefore(now).forEach(link -> {
            if (link.isActive()) {
                link.setActive(false);
                repo.save(link);
                notifier.notifyUser(link.getOwnerUuid(), "Link expired (cleanup)", "Link " + link.getShortCode() + " expired and was removed.");
            } else {
                repo.delete(link);
            }
        });
    }
}
