package com.findit.repository;

import com.findit.model.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private final List<Notification> notifications = new ArrayList<>();
    private int nextId = 1;

    public int generateId() {
        return nextId++;
    }

    public void save(Notification notification) {
        notifications.add(notification);
    }

    public List<Notification> findByUser(int userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.getUserId() == userId) {
                result.add(n);
            }
        }
        return result;
    }
}