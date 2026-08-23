package com.example.client.recipe;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.display.FurnaceRecipeDisplay;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.ShapedCraftingRecipeDisplay;
import net.minecraft.recipe.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.recipe.display.SmithingRecipeDisplay;
import net.minecraft.recipe.display.StonecutterRecipeDisplay;
import net.minecraft.util.context.ContextParameterMap;

import java.util.*;

public class RecipeIndexer {
    private static final Map<Item, List<RecipeDisplayEntry>> recipesByOutput = new HashMap<>();
    private static final Map<Item, List<RecipeDisplayEntry>> recipesByIngredient = new HashMap<>();
    private static boolean isInitialized = false;

    public static void initialize() {
        recipesByOutput.clear();
        recipesByIngredient.clear();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ContextParameterMap contextMap = SlotDisplayContexts.createParameters(client.world);

        // In Singleplayer, we can access the server's recipe manager directly to get all recipes, even locked ones.
        if (client.getServer() != null) {
            net.minecraft.recipe.ServerRecipeManager srm = client.getServer().getRecipeManager();
            for (net.minecraft.recipe.RecipeEntry<?> entry : srm.values()) {
                srm.forEachRecipeDisplay(entry.id(), display -> {
                    indexRecipe(display, contextMap);
                });
            }
        } else {
            // In Multiplayer, fallback to the client recipe book (only contains recipes the server has sent).
            for (RecipeResultCollection collection : client.player.getRecipeBook().getOrderedResults()) {
                for (RecipeDisplayEntry entry : collection.getAllRecipes()) {
                    indexRecipe(entry, contextMap);
                }
            }
        }
        
        isInitialized = true;
    }

    private static void indexRecipe(RecipeDisplayEntry entry, ContextParameterMap contextMap) {
        RecipeDisplay display = entry.display();
        
        List<SlotDisplay> inputSlots = new ArrayList<>();
        SlotDisplay outputSlot = null;

        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            inputSlots.addAll(shaped.ingredients());
            outputSlot = shaped.result();
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            inputSlots.addAll(shapeless.ingredients());
            outputSlot = shapeless.result();
        } else if (display instanceof FurnaceRecipeDisplay furnace) {
            inputSlots.add(furnace.ingredient());
            outputSlot = furnace.result();
        } else if (display instanceof StonecutterRecipeDisplay stonecutter) {
            inputSlots.add(stonecutter.input());
            outputSlot = stonecutter.result();
        } else if (display instanceof SmithingRecipeDisplay smithing) {
            inputSlots.add(smithing.template());
            inputSlots.add(smithing.base());
            inputSlots.add(smithing.addition());
            outputSlot = smithing.result();
        }

        if (outputSlot != null) {
            List<ItemStack> outputs = outputSlot.getStacks(contextMap);
            for (ItemStack stack : outputs) {
                if (!stack.isEmpty()) {
                    recipesByOutput.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(entry);
                }
            }
        }

        for (SlotDisplay inputSlot : inputSlots) {
            if (inputSlot == null) continue;
            List<ItemStack> inputs = inputSlot.getStacks(contextMap);
            for (ItemStack stack : inputs) {
                if (!stack.isEmpty()) {
                    recipesByIngredient.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(entry);
                }
            }
        }
    }

    public static List<RecipeDisplayEntry> getRecipesForOutput(Item item) {
        if (!isInitialized) initialize();
        return recipesByOutput.getOrDefault(item, Collections.emptyList());
    }

    public static List<RecipeDisplayEntry> getRecipesForIngredient(Item item) {
        if (!isInitialized) initialize();
        return recipesByIngredient.getOrDefault(item, Collections.emptyList());
    }
    
    public static void reset() {
        isInitialized = false;
        recipesByOutput.clear();
        recipesByIngredient.clear();
    }
}
