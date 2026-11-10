package com.findit.model;

import java.time.LocalDate;

public class FoundItem extends Item {

    public FoundItem(int id, String name, Category category, String description,
                     String location, LocalDate date, String imagePath, int reportedBy) {
        super(id, name, category, description, location, date, imagePath, reportedBy);
    }

    @Override
    public String getType() { return "FOUND"; }
}