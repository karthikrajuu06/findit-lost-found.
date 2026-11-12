package com.findit.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {

    private final int id;
    private final int userId;
    private final String message;
    private boolean read;
    private final LocalDateTime createdAt;

    /** For a new notification. */
    public Notification(int id, int userId, String message) {
        this(id, userId, message, false, LocalDateTime.now());
    }

    /** For a notification loaded from the database. */
    public Notification(int id, int userId, String message,
                        boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void markRead() { this.read = true; }

    @Override
    public String toString() {
        String time = createdAt.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
        return (read ? "   " : "[NEW] ") + time + " - " + message;
    }
}