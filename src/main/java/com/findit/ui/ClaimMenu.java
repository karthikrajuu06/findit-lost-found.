package com.findit.ui;

import com.findit.model.*;
import com.findit.service.ClaimService;
import java.util.List;
import java.util.Scanner;

public class ClaimMenu {

    private final Scanner scanner;
    private final ClaimService claimService;

    public ClaimMenu(Scanner scanner, ClaimService claimService) {
        this.scanner = scanner;
        this.claimService = claimService;
    }

    public void show(User user) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Claims ---");
            System.out.println("1. Submit a claim on a found item");
            System.out.println("2. My claims");
            System.out.println("3. Claims on my found items");
            System.out.println("4. Approve a claim");
            System.out.println("5. Reject a claim");
            System.out.println("6. Mark my found item as returned");
            if (user.getRole() == Role.ADMIN) {
                System.out.println("7. View all claims (Admin)");
            }
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> submit(user);
                    case "2" -> printClaims(claimService.getMyClaims(user));
                    case "3" -> printClaims(claimService.getClaimsForMyItems(user));
                    case "4" -> {
                        claimService.approve(user, readId("Claim ID: "));
                        System.out.println("Claim approved. Item marked CLAIMED.");
                    }
                    case "5" -> {
                        claimService.reject(user, readId("Claim ID: "));
                        System.out.println("Claim rejected.");
                    }
                    case "6" -> {
                        claimService.markReturned(user, readId("Found item ID: "));
                        System.out.println("Item marked RETURNED.");
                    }
                    case "7" -> {
                        if (user.getRole() == Role.ADMIN) {
                            printClaims(claimService.getAllClaims(user));
                        } else {
                            System.out.println("Access denied.");
                        }
                    }
                    case "0" -> back = true;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException | SecurityException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void submit(User user) {
        int itemId = readId("Found item ID you want to claim: ");
        System.out.println("Describe proof (e.g. unique marks, contents, photo on card):");
        String proof = scanner.nextLine();
        Claim claim = claimService.submitClaim(user, itemId, proof);
        System.out.println("Claim submitted. Claim ID: " + claim.getId() + " (PENDING)");
    }

    private int readId(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }
    }

    private void printClaims(List<Claim> claims) {
        if (claims.isEmpty()) {
            System.out.println("No claims found.");
            return;
        }
        claims.forEach(System.out::println);
    }
}