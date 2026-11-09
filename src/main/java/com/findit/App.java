package com.findit;

import com.findit.model.Role;
import com.findit.model.User;
import com.findit.repository.UserRepository;
import com.findit.service.UserService;
import java.util.Optional;
import java.util.Scanner;

public class App {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService =
            new UserService(new UserRepository());
    private static User loggedInUser = null;

    public static void main(String[] args) {
        System.out.println("=== Welcome to FindIt ===");
        boolean running = true;
        while (running) {
            if (loggedInUser == null) {
                running = guestMenu();
            } else {
                userMenu();
            }
        }
        System.out.println("Goodbye!");
    }

    private static boolean guestMenu() {
        System.out.println("\n1. Register\n2. Login\n3. Exit");
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> register();
            case "2" -> login();
            case "3" -> { return false; }
            default -> System.out.println("Invalid choice.");
        }
        return true;
    }

    private static void register() {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password (min 6 chars): ");
        String password = scanner.nextLine();
        System.out.print("Phone (10 digits): ");
        String phone = scanner.nextLine();

        try {
            User user = userService.register(name, email, password, phone);
            System.out.println("Registered successfully! Your ID: " + user.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> result = userService.login(email, password);
        if (result.isPresent()) {
            loggedInUser = result.get();
            System.out.println("Welcome, " + loggedInUser.getName() + "!");
        } else {
            System.out.println("Invalid email or password.");
        }
    }

    private static void userMenu() {
        System.out.println("\n--- Menu (" + loggedInUser.getRole() + ") ---");
        System.out.println("1. View my profile");
        System.out.println("2. Update phone");
        System.out.println("3. Logout");
        if (loggedInUser.getRole() == Role.ADMIN) {
            System.out.println("4. View all users (Admin)");
        }
        System.out.print("Choose: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> System.out.println(loggedInUser);
            case "2" -> updatePhone();
            case "3" -> {
                loggedInUser = null;
                System.out.println("Logged out.");
            }
            case "4" -> {
                if (loggedInUser.getRole() == Role.ADMIN) {
                    userService.getAllUsers().forEach(System.out::println);
                } else {
                    System.out.println("Access denied.");
                }
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void updatePhone() {
        System.out.print("New phone: ");
        String phone = scanner.nextLine();
        try {
            userService.updatePhone(loggedInUser, phone);
            System.out.println("Phone updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }
}