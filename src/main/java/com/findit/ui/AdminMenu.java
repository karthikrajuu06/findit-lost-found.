package com.findit.ui;

import com.findit.model.*;
import com.findit.service.ClaimService;
import com.findit.service.ItemService;
import com.findit.service.UserService;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserService userService;
    private final ItemService itemService;
    private final ClaimService claimService;

    public AdminMenu(Scanner scanner, UserService userService,
                     ItemService itemService, ClaimService claimService) {
        this.scanner = scanner;
        this.userService = userService;
        this.itemService = itemService;
        this.claimService = claimService;
    }

    public void show(User admin) {
        if (admin.getRole() != Role.ADMIN) {
            System.out.println("Access denied.");
            return;
        }

        boolean back = false;
        while (!back) {
            System.out.println("\n--- Admin Panel ---");
            System.out.println("1. View all users");
            System.out.println("2. View all lost reports");
            System.out.println("3. View all found reports");
            System.out.println("4. View all claims");
            System.out.println("5. Delete a report");
            System.out.println("6. Platform statistics");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> print(userService.getAllUsers());
                    case "2" -> print(itemService.getAllLost());
                    case "3" -> print(itemService.getAllFound());
                    case "4" -> print(claimService.getAllClaims(admin));
                    case "5" -> deleteReport(admin);
                    case "6" -> statistics(admin);
                    case "0" -> back = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException | SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void deleteReport(User admin) {
        System.out.print("Type (L = lost, F = found): ");
        String type = scanner.nextLine().trim().toUpperCase();
        System.out.print("Item ID: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }

        if (type.equals("L")) {
            itemService.deleteLost(admin, id);
        } else if (type.equals("F")) {
            itemService.deleteFound(admin, id);
        } else {
            throw new IllegalArgumentException("Type must be L or F.");
        }
        System.out.println("Report deleted.");
    }

    private void statistics(User admin) {
        long openLost = itemService.getAllLost().stream()
                .filter(i -> i.getStatus() == ItemStatus.OPEN).count();
        long openFound = itemService.getAllFound().stream()
                .filter(i -> i.getStatus() == ItemStatus.OPEN).count();
        long returned = itemService.getAllFound().stream()
                .filter(i -> i.getStatus() == ItemStatus.RETURNED).count();

        List<Claim> claims = claimService.getAllClaims(admin);
        long pending = claims.stream().filter(c -> c.getStatus() == ClaimStatus.PENDING).count();
        long approved = claims.stream().filter(c -> c.getStatus() == ClaimStatus.APPROVED).count();
        long rejected = claims.stream().filter(c -> c.getStatus() == ClaimStatus.REJECTED).count();

        System.out.println("\n=== Platform Statistics ===");
        System.out.println("Users registered      : " + userService.getAllUsers().size());
        System.out.println("Lost reports (open)   : " + openLost + " / " + itemService.getAllLost().size());
        System.out.println("Found reports (open)  : " + openFound + " / " + itemService.getAllFound().size());
        System.out.println("Items returned        : " + returned);
        System.out.println("Claims pending        : " + pending);
        System.out.println("Claims approved       : " + approved);
        System.out.println("Claims rejected       : " + rejected);
    }

    private void print(List<?> list) {
        if (list.isEmpty()) {
            System.out.println("Nothing to show.");
            return;
        }
        list.forEach(System.out::println);
    }
}