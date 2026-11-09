package com.findit.repository;

import com.findit.model.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemRepository<T extends Item> {

    private final List<T> items = new ArrayList<>();
    private int nextId = 1;

    public int generateId() {
        return nextId++;
    }

    public void save(T item) {
        items.add(item);
    }

    public Optional<T> findById(int id) {
        for (T item : items) {
            if (item.getId() == id) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public List<T> findAll() {
        return new ArrayList<>(items);
    }

    public List<T> findByUser(int userId) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            if (item.getReportedBy() == userId) {
                result.add(item);
            }
        }
        return result;
    }

    public void delete(T item) {
        items.remove(item);
    }
}