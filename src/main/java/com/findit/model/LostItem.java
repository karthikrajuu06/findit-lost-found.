package com.findit.model;

import java.time.LocalDate;

public class LostItem extends Item {

    public LostItem(int id, String name, Category category, String description,
                    String location, LocalDate date, String imagePath, int reportedBy) {
        super(id, name, category, description, location, date, imagePath, reportedBy);
    }

    @Override
    public String getType() { return "LOST"; }
}