package com.example.client.search;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchEngine {
    private static final List<Item> allItems = new ArrayList<>();
    private static final Map<Item, String> searchCache = new HashMap<>();
    private static String lastQuery = "";
    private static List<Item> lastResults = new ArrayList<>();

    public static void initialize() {
        allItems.clear();
        searchCache.clear();
        
        Registries.ITEM.forEach(item -> {
            if (item != net.minecraft.item.Items.AIR) {
                allItems.add(item);
                // Pre-compute lowercase names to make search ultra-fast
                searchCache.put(item, item.getName().getString().toLowerCase());
            }
        });
        
        lastQuery = "";
        lastResults = new ArrayList<>(allItems);
    }

    public static List<Item> search(String query) {
        if (allItems.isEmpty()) {
            initialize();
        }

        query = query.trim().toLowerCase();
        
        // Return cache if query hasn't changed
        if (query.equals(lastQuery)) {
            return lastResults;
        }

        List<Item> results = new ArrayList<>();
        
        if (query.isEmpty()) {
            results.addAll(allItems);
        } else {
            // Optimization: if the new query starts with the old query, we can just filter the previous results
            Iterable<Item> itemsToSearch = (query.startsWith(lastQuery) && !lastQuery.isEmpty()) ? lastResults : allItems;
            
            for (Item item : itemsToSearch) {
                String name = searchCache.getOrDefault(item, "");
                if (name.contains(query)) {
                    results.add(item);
                }
            }
        }

        lastQuery = query;
        lastResults = results;
        return results;
    }
}
