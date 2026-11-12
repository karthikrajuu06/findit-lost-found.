package com.findit.service;

import com.findit.model.*;
import com.findit.repository.ItemRepository;
import com.findit.util.Validator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ItemService {

    private final ItemRepository<LostItem> lostRepo;
    private final ItemRepository<FoundItem> foundRepo;
    private final NotificationService notificationService;

    public ItemService(ItemRepository<LostItem> lostRepo,
                       ItemRepository<FoundItem> foundRepo,
                       NotificationService notificationService) {
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.notificationService = notificationService;
    }

    // ---------- Report ----------

    public LostItem reportLost(User user, String name, Category category,
                               String description, String location,
                               String dateText, String imagePath) {
        validateReport(name, category, location);
        LostItem draft = new LostItem(0, name.trim(), category,
                clean(description), location.trim(), parseDate(dateText),
                clean(imagePath), user.getId());
        LostItem saved = lostRepo.save(draft);
        notifyFindersOfMatch(saved);
        return saved;
    }

    public FoundItem reportFound(User user, String name, Category category,
                                 String description, String location,
                                 String dateText, String imagePath) {
        validateReport(name, category, location);
        FoundItem draft = new FoundItem(0, name.trim(), category,
                clean(description), location.trim(), parseDate(dateText),
                clean(imagePath), user.getId());
        FoundItem saved = foundRepo.save(draft);
        notifyOwnersOfMatch(saved);
        return saved;
    }

    // ---------- Search / browse ----------

    public List<LostItem> searchLost(String keyword, Category category, String location) {
        return filter(lostRepo.findAll(), keyword, category, location);
    }

    public List<FoundItem> searchFound(String keyword, Category category, String location) {
        return filter(foundRepo.findAll(), keyword, category, location);
    }

    public List<LostItem> getMyLost(User user) {
        return lostRepo.findByUser(user.getId());
    }

    public List<FoundItem> getMyFound(User user) {
        return foundRepo.findByUser(user.getId());
    }

    // ---------- Lookups used by other services ----------

    public Optional<FoundItem> findFoundById(int id) {
        return foundRepo.findById(id);
    }

    public List<FoundItem> getAllFound() {
        return foundRepo.findAll();
    }

    public List<LostItem> getAllLost() {
        return lostRepo.findAll();
    }

    /** Used by ClaimService: changes a found item's status and saves it. */
    public void updateFoundStatus(FoundItem item, ItemStatus status) {
        item.setStatus(status);
        foundRepo.update(item);
    }

    // ---------- Update / delete ----------

    public void updateLost(User user, int id, String name, String location, String description) {
        LostItem item = findAuthorized(lostRepo, user, id);
        applyUpdate(item, name, location, description);
        lostRepo.update(item);
    }

    public void updateFound(User user, int id, String name, String location, String description) {
        FoundItem item = findAuthorized(foundRepo, user, id);
        applyUpdate(item, name, location, description);
        foundRepo.update(item);
    }

    public void deleteLost(User user, int id) {
        lostRepo.delete(findAuthorized(lostRepo, user, id));
    }

    public void deleteFound(User user, int id) {
        foundRepo.delete(findAuthorized(foundRepo, user, id));
    }

    // ---------- Matching ----------

    private void notifyFindersOfMatch(LostItem lost) {
        for (FoundItem found : foundRepo.findAll()) {
            if (found.getStatus() == ItemStatus.OPEN
                    && found.getReportedBy() != lost.getReportedBy()
                    && isPotentialMatch(lost, found)) {
                notificationService.notifyUser(found.getReportedBy(),
                        "A lost item that may match your found '" + found.getName()
                        + "' was reported (Lost #" + lost.getId() + ": " + lost.getName() + ").");
            }
        }
    }

    private void notifyOwnersOfMatch(FoundItem found) {
        for (LostItem lost : lostRepo.findAll()) {
            if (lost.getStatus() == ItemStatus.OPEN
                    && lost.getReportedBy() != found.getReportedBy()
                    && isPotentialMatch(lost, found)) {
                notificationService.notifyUser(lost.getReportedBy(),
                        "A found item that may match your lost '" + lost.getName()
                        + "' was reported (Found #" + found.getId() + ": " + found.getName()
                        + "). Browse found items and submit a claim if it is yours.");
            }
        }
    }

    private boolean isPotentialMatch(Item a, Item b) {
        if (a.getCategory() != b.getCategory()) {
            return false;
        }
        Set<String> wordsOfA = words(a.getName());
        for (String word : words(b.getName())) {
            if (wordsOfA.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> words(String text) {
        return new HashSet<>(Arrays.asList(text.toLowerCase().trim().split("\\s+")));
    }

    // ---------- Private helpers ----------

    private <T extends Item> T findAuthorized(ItemRepository<T> repo, User user, int id) {
        T item = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found."));
        boolean isOwner = item.getReportedBy() == user.getId();
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new SecurityException("You can only modify your own reports.");
        }
        return item;
    }

    private void applyUpdate(Item item, String name, String location, String description) {
        if (Validator.isNotBlank(name)) item.setName(name.trim());
        if (Validator.isNotBlank(location)) item.setLocation(location.trim());
        if (Validator.isNotBlank(description)) item.setDescription(description.trim());
    }

    private <T extends Item> List<T> filter(List<T> items, String keyword,
                                            Category category, String location) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        String loc = location == null ? "" : location.trim().toLowerCase();

        return items.stream()
                .filter(i -> i.getStatus() == ItemStatus.OPEN)
                .filter(i -> kw.isEmpty()
                        || i.getName().toLowerCase().contains(kw)
                        || i.getDescription().toLowerCase().contains(kw))
                .filter(i -> category == null || i.getCategory() == category)
                .filter(i -> loc.isEmpty() || i.getLocation().toLowerCase().contains(loc))
                .toList();
    }

    private void validateReport(String name, Category category, String location) {
        if (!Validator.isNotBlank(name)) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (!Validator.isNotBlank(location)) {
            throw new IllegalArgumentException("Location cannot be empty.");
        }
    }

    private LocalDate parseDate(String text) {
        if (!Validator.isNotBlank(text)) {
            throw new IllegalArgumentException("Date is required (yyyy-MM-dd).");
        }
        try {
            LocalDate date = LocalDate.parse(text.trim());
            if (date.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Date cannot be in the future.");
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date. Use yyyy-MM-dd, e.g. 2026-11-08.");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}