package com.findit.repository;

import com.findit.model.Category;
import com.findit.model.FoundItem;
import java.time.LocalDate;

public class FoundItemRepository extends ItemRepository<FoundItem> {

    public FoundItemRepository() {
        super("found_items", "found_item_id", "date_found");
    }

    @Override
    protected FoundItem create(int id, String name, Category category,
                               String description, String location,
                               LocalDate date, String imagePath, int reportedBy) {
        return new FoundItem(id, name, category, description, location,
                date, imagePath, reportedBy);
    }
}