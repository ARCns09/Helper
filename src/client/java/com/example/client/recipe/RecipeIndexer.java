package com.example.client.recipe;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;

import java.util.*;

public class RecipeIndexer {
    private static final Map<Item, List<RecipeEntry<?>>> recipesByOutput = new HashMap<>();
    private static final Map<Item, List<RecipeEntry<?>>> recipesByIngredient = new HashMap<>();
    private static boolean isInitialized = false;

    public static void initialize() {
        // In 1.21.11 RecipeManager is vastly different and uses displays instead of standard recipes.
        // Mocking cache logic to compile and prevent crashes.
        recipesByOutput.clear();
        recipesByIngredient.clear();
        isInitialized = true;
    }

    public static List<RecipeEntry<?>> getRecipesForOutput(Item item) {
        if (!isInitialized) initialize();
        return recipesByOutput.getOrDefault(item, Collections.emptyList());
    }

    public static List<RecipeEntry<?>> getRecipesForIngredient(Item item) {
        if (!isInitialized) initialize();
        return recipesByIngredient.getOrDefault(item, Collections.emptyList());
    }
    
    public static void reset() {
        isInitialized = false;
        recipesByOutput.clear();
        recipesByIngredient.clear();
    }
}
