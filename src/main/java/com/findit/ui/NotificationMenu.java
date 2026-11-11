package com.findit.ui;

import com.findit.model.Notification;
import com.findit.model.User;
import com.findit.service.NotificationService;
import java.util.List;

public class NotificationMenu {

    private final NotificationService notificationService;

    public NotificationMenu(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void show(User user) {
        List<Notification> list = notificationService.getMine(user);
        System.out.println("\n--- Notifications ---");
        if (list.isEmpty()) {
            System.out.println("No notifications.");
            return;
        }
        list.forEach(System.out::println);
        notificationService.markAllRead(user);
    }
}