package com.findit.service;

import com.findit.model.Notification;
import com.findit.model.User;
import com.findit.repository.NotificationRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void notifyUser(int userId, String message) {
        repo.save(new Notification(repo.generateId(), userId, message));
    }

    public List<Notification> getMine(User user) {
        List<Notification> list = new ArrayList<>(repo.findByUser(user.getId()));
        Collections.reverse(list);   // newest first
        return list;
    }

    public long countUnread(User user) {
        return repo.findByUser(user.getId()).stream()
                .filter(n -> !n.isRead())
                .count();
    }

    public void markAllRead(User user) {
        repo.findByUser(user.getId()).forEach(Notification::markRead);
    }
}