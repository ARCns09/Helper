package com.example.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeDisplayEntry;
import net.minecraft.recipe.display.*;
import net.minecraft.text.Text;
import net.minecraft.util.context.ContextParameterMap;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class RecipePopupScreen extends Screen {
    private final Screen parent;
    private final List<RecipeDisplayEntry> allRecipes;
    private ContextParameterMap contextMap;
    
    // Grouping
    private final Map<String, List<RecipeDisplayEntry>> categories = new LinkedHashMap<>();
    private final List<String> categoryKeys = new ArrayList<>();
    private int currentCategoryIndex = 0;
    
    private int pageIndex = 0;
    private final int recipesPerPage = 3;
    
    // Window bounds
    private int x, y, width, height;
    
    public RecipePopupScreen(Screen parent, List<RecipeDisplayEntry> recipes) {
        super(Text.literal("Recipes"));
        this.parent = parent;
        this.allRecipes = recipes;
        
        // Group recipes by category
        for (RecipeDisplayEntry entry : recipes) {
            String title = getCategoryName(entry.display());
            categories.computeIfAbsent(title, k -> new ArrayList<>()).add(entry);
        }
        categoryKeys.addAll(categories.keySet());
    }
    
    private String getCategoryName(RecipeDisplay display) {
        if (display instanceof ShapedCraftingRecipeDisplay || display instanceof ShapelessCraftingRecipeDisplay) return "Crafting";
        else if (display instanceof FurnaceRecipeDisplay) return "Furnace";
        else if (display instanceof StonecutterRecipeDisplay) return "Stonecutter";
        else if (display instanceof SmithingRecipeDisplay) return "Smithing";
        return "Recipe";
    }
    
    private ItemStack getCategoryIcon(String category) {
        return switch (category) {
            case "Crafting" -> new ItemStack(Items.CRAFTING_TABLE);
            case "Furnace" -> new ItemStack(Items.FURNACE);
            case "Stonecutter" -> new ItemStack(Items.STONECUTTER);
            case "Smithing" -> new ItemStack(Items.SMITHING_TABLE);
            default -> new ItemStack(Items.CRAFTING_TABLE);
        };
    }

    @Override
    protected void init() {
        super.init();
        if (client != null && client.world != null) {
            contextMap = SlotDisplayContexts.createParameters(client.world);
        }
        this.width = 150;
        this.height = 166;
        this.x = (this.client.getWindow().getScaledWidth() - this.width) / 2;
        this.y = (this.client.getWindow().getScaledHeight() - this.height) / 2;
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    private void drawSlot(DrawContext context, SlotDisplay slot, int x, int y) {
        // Draw standard slot background (gray with darker borders for 3D effect)
        context.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        context.fill(x, y, x + 17, y + 1, 0xFF373737);
        context.fill(x, y, x + 1, y + 17, 0xFF373737);
        context.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        context.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        
        if (slot == null || contextMap == null) return;
        
        List<ItemStack> stacks = slot.getStacks(contextMap);
        if (stacks == null || stacks.isEmpty()) return;
        
        int index = (int)((System.currentTimeMillis() / 1000) % stacks.size());
        ItemStack stack = stacks.get(index);
        
        if (!stack.isEmpty()) {
            context.drawItem(stack, x + 1, y + 1);
        }
    }
    
    private void drawOutput(DrawContext context, SlotDisplay output, int x, int y) {
        // Output slot is usually bigger, 26x26
        context.fill(x, y, x + 26, y + 26, 0xFF8B8B8B);
        context.fill(x, y, x + 25, y + 1, 0xFF373737);
        context.fill(x, y, x + 1, y + 25, 0xFF373737);
        context.fill(x + 1, y + 25, x + 26, y + 26, 0xFFFFFFFF);
        context.fill(x + 25, y + 1, x + 26, y + 26, 0xFFFFFFFF);
        
        if (output == null || contextMap == null) return;
        List<ItemStack> stacks = output.getStacks(contextMap);
        if (stacks == null || stacks.isEmpty()) return;
        
        int index = (int)((System.currentTimeMillis() / 1000) % stacks.size());
        ItemStack stack = stacks.get(index);
        
        if (!stack.isEmpty()) {
            // Draw slightly centered in the 26x26 box (which is 16x16 item) -> x+5, y+5
            context.drawItem(stack, x + 5, y + 5);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw parent screen in background
        parent.render(context, -1000, -1000, delta);
        
        // Dim overlay
        context.fill(0, 0, this.client.getWindow().getScaledWidth(), this.client.getWindow().getScaledHeight(), 0x80000000);

        if (categoryKeys.isEmpty()) return;

        // Draw Tabs (on the left)
        int tabX = x - 28;
        int tabY = y + 10;
        for (int i = 0; i < categoryKeys.size(); i++) {
            boolean active = (i == currentCategoryIndex);
            int tw = 28;
            int th = 28;
            
            if (active) {
                // Active tab connects to main window
                context.fill(tabX - 2, tabY + i * 30, tabX + tw + 2, tabY + i * 30 + th, 0xFFC6C6C6);
                drawBorder(context, tabX - 3, tabY + i * 30 - 1, tw + 5, th + 2, 0xFF000000);
                drawBorder(context, tabX - 2, tabY + i * 30, tw + 4, th, 0xFFFFFFFF);
            } else {
                // Inactive tab
                context.fill(tabX, tabY + i * 30, tabX + tw, tabY + i * 30 + th, 0xFF8B8B8B);
                drawBorder(context, tabX - 1, tabY + i * 30 - 1, tw + 2, th + 2, 0xFF000000);
                drawBorder(context, tabX, tabY + i * 30, tw, th, 0xFFFFFFFF);
            }
            
            // Draw icon
            ItemStack icon = getCategoryIcon(categoryKeys.get(i));
            context.drawItem(icon, tabX + 6, tabY + i * 30 + 6);
        }

        // Draw EMI-style Window Background
        context.fill(x, y, x + width, y + height, 0xFFC6C6C6);
        drawBorder(context, x - 1, y - 1, width + 2, height + 2, 0xFF000000);
        drawBorder(context, x, y, width, height, 0xFFFFFFFF);
        
        // Header inner fill
        context.fill(x + 2, y + 2, x + width - 2, y + 14, 0xFF8B8B8B);
        
        String catName = categoryKeys.get(currentCategoryIndex);
        List<RecipeDisplayEntry> currentRecipes = categories.get(catName);
        int maxPages = (int)Math.ceil(currentRecipes.size() / (double)recipesPerPage);
        if (pageIndex >= maxPages) pageIndex = maxPages - 1;
        if (pageIndex < 0) pageIndex = 0;

        // Title and Page text
        context.drawCenteredTextWithShadow(this.textRenderer, catName, x + width / 2, y + 4, 0xFFFFFFFF);
        String pageText = "Page " + (pageIndex + 1) + " of " + maxPages;
        context.drawCenteredTextWithShadow(this.textRenderer, pageText, x + width / 2, y + 16, 0xFF404040);

        // Navigation Arrows
        context.fill(x + 4, y + 16, x + 14, y + 26, 0xFF8B8B8B); // Left button
        context.drawTextWithShadow(this.textRenderer, "<", x + 6, y + 17, pageIndex > 0 ? 0xFFFFFFFF : 0xFF555555);
        
        context.fill(x + width - 14, y + 16, x + width - 4, y + 26, 0xFF8B8B8B); // Right button
        context.drawTextWithShadow(this.textRenderer, ">", x + width - 12, y + 17, pageIndex < maxPages - 1 ? 0xFFFFFFFF : 0xFF555555);

        // Draw Recipes
        int startIdx = pageIndex * recipesPerPage;
        int endIdx = Math.min(startIdx + recipesPerPage, currentRecipes.size());
        
        int cardY = y + 28;
        for (int i = startIdx; i < endIdx; i++) {
            RecipeDisplay display = currentRecipes.get(i).display();
            drawRecipeCard(context, display, x + 4, cardY, width - 8, 42);
            cardY += 44;
        }

        // Draw tooltips on top
        cardY = y + 28;
        for (int i = startIdx; i < endIdx; i++) {
            RecipeDisplay display = currentRecipes.get(i).display();
            drawTooltips(context, mouseX, mouseY, x + 4, cardY, display);
            cardY += 44;
        }
        
        // Tab tooltips
        for (int i = 0; i < categoryKeys.size(); i++) {
            int bx = tabX - 2;
            int by = tabY + i * 30;
            if (mouseX >= bx && mouseX <= bx + 28 && mouseY >= by && mouseY <= by + 28) {
                context.drawTooltip(this.textRenderer, Text.literal(categoryKeys.get(i)), mouseX, mouseY);
            }
        }
    }
    
    private void drawRecipeCard(DrawContext context, RecipeDisplay display, int cx, int cy, int cw, int ch) {
        // Card background
        context.fill(cx, cy, cx + cw, cy + ch, 0xFFC6C6C6);
        drawBorder(context, cx, cy, cw, ch, 0xFF8B8B8B); // Inset border
        
        int arrowX = cx + cw - 50;
        int arrowY = cy + (ch / 2) - 4;
        // Draw simple arrow
        context.drawText(this.textRenderer, "->", arrowX, arrowY, 0xFF555555, false);
        
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            int w = shaped.width();
            int h = shaped.height();
            List<SlotDisplay> ingredients = shaped.ingredients();
            
            // Center the 3x3 grid vertically if it's smaller
            int gridStartX = cx + 4;
            int gridStartY = cy + 4;
            
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    int idx = r * w + c;
                    if (idx < ingredients.size()) {
                        drawSlot(context, ingredients.get(idx), gridStartX + c * 18, gridStartY + r * 18);
                    }
                }
            }
            drawOutput(context, shaped.result(), cx + cw - 32, cy + 8);
            
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            List<SlotDisplay> ingredients = shapeless.ingredients();
            int gridStartX = cx + 4;
            int gridStartY = cy + 4;
            for (int i = 0; i < ingredients.size(); i++) {
                int r = i / 3;
                int c = i % 3;
                drawSlot(context, ingredients.get(i), gridStartX + c * 18, gridStartY + r * 18);
            }
            drawOutput(context, shapeless.result(), cx + cw - 32, cy + 8);
            
        } else if (display instanceof FurnaceRecipeDisplay furnace) {
            drawSlot(context, furnace.ingredient(), cx + 22, cy + 4);
            context.drawText(this.textRenderer, "^", cx + 26, cy + 24, 0xFFFF5555, false); // fire icon
            drawOutput(context, furnace.result(), cx + cw - 32, cy + 8);
            
        } else if (display instanceof StonecutterRecipeDisplay stone) {
            drawSlot(context, stone.input(), cx + 22, cy + 12);
            drawOutput(context, stone.result(), cx + cw - 32, cy + 8);
            
        } else if (display instanceof SmithingRecipeDisplay smith) {
            drawSlot(context, smith.template(), cx + 4, cy + 12);
            drawSlot(context, smith.base(), cx + 22, cy + 12);
            drawSlot(context, smith.addition(), cx + 40, cy + 12);
            drawOutput(context, smith.result(), cx + cw - 32, cy + 8);
        }
    }
    
    private void drawTooltips(DrawContext context, int mouseX, int mouseY, int cx, int cy, RecipeDisplay display) {
        if (display instanceof ShapedCraftingRecipeDisplay shaped) {
            int w = shaped.width();
            int h = shaped.height();
            List<SlotDisplay> ingredients = shaped.ingredients();
            for (int r = 0; r < h; r++) {
                for (int c = 0; c < w; c++) {
                    int idx = r * w + c;
                    if (idx < ingredients.size()) {
                        checkSlotHover(context, ingredients.get(idx), mouseX, mouseY, cx + 4 + c * 18, cy + 4 + r * 18);
                    }
                }
            }
            checkOutputHover(context, shaped.result(), mouseX, mouseY, cx + width - 8 - 32, cy + 8);
        } else if (display instanceof ShapelessCraftingRecipeDisplay shapeless) {
            List<SlotDisplay> ingredients = shapeless.ingredients();
            for (int i = 0; i < ingredients.size(); i++) {
                int r = i / 3;
                int c = i % 3;
                checkSlotHover(context, ingredients.get(i), mouseX, mouseY, cx + 4 + c * 18, cy + 4 + r * 18);
            }
            checkOutputHover(context, shapeless.result(), mouseX, mouseY, cx + width - 8 - 32, cy + 8);
        } else if (display instanceof FurnaceRecipeDisplay furnace) {
            checkSlotHover(context, furnace.ingredient(), mouseX, mouseY, cx + 22, cy + 4);
            checkOutputHover(context, furnace.result(), mouseX, mouseY, cx + width - 8 - 32, cy + 8);
        } else if (display instanceof StonecutterRecipeDisplay stone) {
            checkSlotHover(context, stone.input(), mouseX, mouseY, cx + 22, cy + 12);
            checkOutputHover(context, stone.result(), mouseX, mouseY, cx + width - 8 - 32, cy + 8);
        } else if (display instanceof SmithingRecipeDisplay smith) {
            checkSlotHover(context, smith.template(), mouseX, mouseY, cx + 4, cy + 12);
            checkSlotHover(context, smith.base(), mouseX, mouseY, cx + 22, cy + 12);
            checkSlotHover(context, smith.addition(), mouseX, mouseY, cx + 40, cy + 12);
            checkOutputHover(context, smith.result(), mouseX, mouseY, cx + width - 8 - 32, cy + 8);
        }
    }
    
    private void checkSlotHover(DrawContext context, SlotDisplay slot, int mx, int my, int x, int y) {
        if (mx >= x && mx < x + 18 && my >= y && my < y + 18) {
            if (slot != null && contextMap != null) {
                List<ItemStack> stacks = slot.getStacks(contextMap);
                if (stacks != null && !stacks.isEmpty()) {
                    int index = (int)((System.currentTimeMillis() / 1000) % stacks.size());
                    context.drawItemTooltip(this.textRenderer, stacks.get(index), mx, my);
                }
            }
        }
    }
    
    private void checkOutputHover(DrawContext context, SlotDisplay output, int mx, int my, int x, int y) {
        if (mx >= x && mx < x + 26 && my >= y && my < y + 26) {
            if (output != null && contextMap != null) {
                List<ItemStack> stacks = output.getStacks(contextMap);
                if (stacks != null && !stacks.isEmpty()) {
                    int index = (int)((System.currentTimeMillis() / 1000) % stacks.size());
                    context.drawItemTooltip(this.textRenderer, stacks.get(index), mx, my);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
        double mouseX = click.x();
        double mouseY = click.y();
        
        if (categoryKeys.isEmpty()) return super.mouseClicked(click, doubleClick);
        
        // Check Tabs
        int tabX = x - 28;
        int tabY = y + 10;
        for (int i = 0; i < categoryKeys.size(); i++) {
            int bx = tabX - 2;
            int by = tabY + i * 30;
            if (mouseX >= bx && mouseX <= bx + 28 && mouseY >= by && mouseY <= by + 28) {
                currentCategoryIndex = i;
                pageIndex = 0; // reset page
                return true;
            }
        }
        
        // Check arrows
        String catName = categoryKeys.get(currentCategoryIndex);
        List<RecipeDisplayEntry> currentRecipes = categories.get(catName);
        int maxPages = (int)Math.ceil(currentRecipes.size() / (double)recipesPerPage);
        
        if (mouseX >= x + 4 && mouseX <= x + 14 && mouseY >= y + 16 && mouseY <= y + 26) {
            if (pageIndex > 0) pageIndex--;
            return true;
        }
        if (mouseX >= x + width - 14 && mouseX <= x + width - 4 && mouseY >= y + 16 && mouseY <= y + 26) {
            if (pageIndex < maxPages - 1) pageIndex++;
            return true;
        }

        return super.mouseClicked(click, doubleClick);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (categoryKeys.isEmpty()) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        
        String catName = categoryKeys.get(currentCategoryIndex);
        List<RecipeDisplayEntry> currentRecipes = categories.get(catName);
        int maxPages = (int)Math.ceil(currentRecipes.size() / (double)recipesPerPage);
        
        if (verticalAmount > 0 && pageIndex > 0) {
            pageIndex--;
            return true;
        } else if (verticalAmount < 0 && pageIndex < maxPages - 1) {
            pageIndex++;
            return true;
        }
        
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput keyInput) {
        int keyCode = keyInput.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            this.client.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyInput);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
