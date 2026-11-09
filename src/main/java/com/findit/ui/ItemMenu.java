package com.findit.ui;

import com.findit.model.*;
import com.findit.service.ItemService;
import java.util.List;
import java.util.Scanner;

public class ItemMenu {

    private final Scanner scanner;
    private final ItemService itemService;

    public ItemMenu(Scanner scanner, ItemService itemService) {
        this.scanner = scanner;
        this.itemService = itemService;
    }

    public void show(User user) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Lost & Found ---");
            System.out.println("1. Report lost item");
            System.out.println("2. Report found item");
            System.out.println("3. Browse lost items");
            System.out.println("4. Browse found items");
            System.out.println("5. Search lost items");
            System.out.println("6. Search found items");
            System.out.println("7. My reports");
            System.out.println("8. Update a report");
            System.out.println("9. Delete a report");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> reportLost(user);
                    case "2" -> reportFound(user);
                    case "3" -> printItems(itemService.searchLost(null, null, null));
                    case "4" -> printItems(itemService.searchFound(null, null, null));
                    case "5" -> search(true);
                    case "6" -> search(false);
                    case "7" -> myReports(user);
                    case "8" -> update(user);
                    case "9" -> delete(user);
                    case "0" -> back = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException | SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void reportLost(User user) {
        System.out.print("Item name: ");
        String name = scanner.nextLine();
        Category category = readCategory(false);
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Where did you lose it? ");
        String location = scanner.nextLine();
        System.out.print("Date lost (yyyy-MM-dd): ");
        String date = scanner.nextLine();
        System.out.print("Image file path (optional): ");
        String image = scanner.nextLine();

        LostItem item = itemService.reportLost(user, name, category, description,
                location, date, image);
        System.out.println("Lost item reported. ID: " + item.getId());
    }

    private void reportFound(User user) {
        System.out.print("Item name: ");
        String name = scanner.nextLine();
        Category category = readCategory(false);
        System.out.print("Description: ");
        String description = scanner.nextLine();
        System.out.print("Where did you find it? ");
        String location = scanner.nextLine();
        System.out.print("Date found (yyyy-MM-dd): ");
        String date = scanner.nextLine();
        System.out.print("Image file path (optional): ");
        String image = scanner.nextLine();

        FoundItem item = itemService.reportFound(user, name, category, description,
                location, date, image);
        System.out.println("Found item reported. ID: " + item.getId());
    }

    private void search(boolean lost) {
        System.out.print("Keyword (Enter to skip): ");
        String keyword = scanner.nextLine();
        Category category = readCategory(true);
        System.out.print("Location (Enter to skip): ");
        String location = scanner.nextLine();

        if (lost) {
            printItems(itemService.searchLost(keyword, category, location));
        } else {
            printItems(itemService.searchFound(keyword, category, location));
        }
    }

    private void myReports(User user) {
        System.out.println("--- My lost items ---");
        printItems(itemService.getMyLost(user));
        System.out.println("--- My found items ---");
        printItems(itemService.getMyFound(user));
    }

    private void update(User user) {
        boolean lost = readIsLost();
        int id = readId();
        System.out.print("New name (Enter to keep): ");
        String name = scanner.nextLine();
        System.out.print("New location (Enter to keep): ");
        String location = scanner.nextLine();
        System.out.print("New description (Enter to keep): ");
        String description = scanner.nextLine();

        if (lost) {
            itemService.updateLost(user, id, name, location, description);
        } else {
            itemService.updateFound(user, id, name, location, description);
        }
        System.out.println("Report updated.");
    }

    private void delete(User user) {
        boolean lost = readIsLost();
        int id = readId();
        if (lost) {
            itemService.deleteLost(user, id);
        } else {
            itemService.deleteFound(user, id);
        }
        System.out.println("Report deleted.");
    }

    // ---------- Input helpers ----------

    private Category readCategory(boolean optional) {
        Category[] all = Category.values();
        for (int i = 0; i < all.length; i++) {
            System.out.println("  " + (i + 1) + ". " + all[i]);
        }
        System.out.print(optional ? "Category number (Enter to skip): " : "Category number: ");
        String input = scanner.nextLine().trim();

        if (optional && input.isEmpty()) {
            return null;
        }
        try {
            int number = Integer.parseInt(input);
            if (number >= 1 && number <= all.length) {
                return all[number - 1];
            }
        } catch (NumberFormatException e) {
            // fall through to the error below
        }
        throw new IllegalArgumentException("Invalid category number.");
    }

    private boolean readIsLost() {
        System.out.print("Type (L = lost, F = found): ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (type.equals("L")) return true;
        if (type.equals("F")) return false;
        throw new IllegalArgumentException("Type must be L or F.");
    }

    private int readId() {
        System.out.print("Item ID: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }
    }

    private void printItems(List<? extends Item> items) {
        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }
        items.forEach(System.out::println);
    }
}