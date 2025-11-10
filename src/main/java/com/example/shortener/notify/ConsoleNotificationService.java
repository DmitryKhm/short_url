package com.example.shortener.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsoleNotificationService implements NotificationService {
    private final Logger logger = LoggerFactory.getLogger(ConsoleNotificationService.class);

    @Override
    public void notifyUser(String userUuid, String subject, String message) {
        logger.info("[NOTIFY] user={} sub='{}' msg='{}'", userUuid, subject, message);
    }
}
