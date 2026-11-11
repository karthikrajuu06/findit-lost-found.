package com.findit.repository;

import com.findit.model.Category;
import com.findit.model.LostItem;
import java.time.LocalDate;

public class LostItemRepository extends ItemRepository<LostItem> {

    public LostItemRepository() {
        super("lost_items", "lost_item_id", "date_lost");
    }

    @Override
    protected LostItem create(int id, String name, Category category,
                              String description, String location,
                              LocalDate date, String imagePath, int reportedBy) {
        return new LostItem(id, name, category, description, location,
                date, imagePath, reportedBy);
    }
}