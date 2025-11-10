package com.example.shortener.web;

import com.example.shortener.model.Link;
import com.example.shortener.service.ShortenerService;
import com.example.shortener.repo.LinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ShortenerController {
    private final ShortenerService service;
    private final LinkRepository repo;

    public ShortenerController(ShortenerService service, LinkRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestHeader(value = "X-User-Id", required = false) String userUuid,
                                     @RequestBody Map<String, Object> body) {
        String longUrl = (String) body.get("longUrl");
        Integer maxClicks = body.containsKey("maxClicks") ? (Integer) body.get("maxClicks") : null;
        if (longUrl == null || longUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "longUrl is required"));
        }
        if (userUuid == null || userUuid.isBlank()) {
            userUuid = UUID.randomUUID().toString();
        }
        Link link = service.createShortLink(userUuid, longUrl, maxClicks);
        String shortUrl = "http://localhost:8080/r/" + link.getShortCode();
        return ResponseEntity.ok(Map.of(
                "ownerUuid", userUuid,
                "shortCode", link.getShortCode(),
                "shortUrl", shortUrl,
                "expiresAt", link.getExpiresAt(),
                "maxClicks", link.getMaxClicks()
        ));
    }

    @GetMapping("/r/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code) {
        Optional<String> target = service.handleRedirect(code);
        if (target.isEmpty()) {
            return ResponseEntity.status(410).body("Link not available or expired");
        } else {
            return ResponseEntity.status(302).location(URI.create(target.get())).build();
        }
    }

    @DeleteMapping("/links/{code}")
    public ResponseEntity<?> deleteLink(@RequestHeader("X-User-Id") String userUuid, @PathVariable String code) {
        var opt = repo.findByShortCode(code);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        var link = opt.get();
        if (!link.getOwnerUuid().equals(userUuid)) {
            return ResponseEntity.status(403).body(Map.of("error", "only owner can delete")); 
        }
        repo.delete(link);
        return ResponseEntity.ok(Map.of("result","deleted"));
    }
}
