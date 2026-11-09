package com.findit.model;

import java.time.LocalDate;

public abstract class Item {

    private final int id;
    private String name;
    private Category category;
    private String description;
    private String location;
    private LocalDate date;
    private String imagePath;
    private ItemStatus status;
    private final int reportedBy;

    protected Item(int id, String name, Category category, String description,
                   String location, LocalDate date, String imagePath, int reportedBy) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.location = location;
        this.date = date;
        this.imagePath = imagePath;
        this.reportedBy = reportedBy;
        this.status = ItemStatus.OPEN;
    }

    public abstract String getType();

    public int getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public String getImagePath() { return imagePath; }
    public ItemStatus getStatus() { return status; }
    public int getReportedBy() { return reportedBy; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(ItemStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "[" + getType() + " #" + id + "] " + name
                + " | " + category
                + " | Location: " + location
                + " | Date: " + date
                + " | Status: " + status
                + (description.isEmpty() ? "" : " | " + description);
    }
}