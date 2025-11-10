package com.example.shortener.notify;

public interface NotificationService {
    void notifyUser(String userUuid, String subject, String message);
}
