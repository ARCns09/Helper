package com.example.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ItemPanelWidget {
    private final MinecraftClient client;
    
    // Grid bounds
    private int gridX, gridY, gridWidth, gridHeight;
    private final int itemSize = 18; 
    private int columns;
    private int rows;
    
    private TextFieldWidget searchField;
    private List<Item> allItems;
    private List<Item> filteredItems;
    
    private int page = 0;
    
    public ItemPanelWidget(int screenWidth, int screenHeight, int guiRight, int guiTop, int guiHeight) {
        this.client = MinecraftClient.getInstance();
        
        // Calculate Grid Bounds (Fill remaining horizontal space on right, leave margin for search bar at bottom)
        this.gridX = guiRight + 4;
        this.gridY = 4;
        this.gridWidth = screenWidth - this.gridX - 4;
        this.gridHeight = screenHeight - 40; // Leave 40px at bottom for search bar
        
        this.columns = Math.max(1, this.gridWidth / itemSize);
        this.rows = Math.max(1, this.gridHeight / itemSize);
        
        // Center the search field at the bottom of the screen
        int searchWidth = 200;
        int searchX = (screenWidth - searchWidth) / 2;
        int searchY = screenHeight - 24;
        
        TextRenderer textRenderer = client.textRenderer;
        searchField = new TextFieldWidget(textRenderer, searchX, searchY, searchWidth, 16, Text.literal("Search"));
        searchField.setChangedListener(this::onSearchChanged);
        
        allItems = new ArrayList<>();
        Registries.ITEM.forEach(allItems::add);
        filteredItems = new ArrayList<>(allItems);
    }
    
    private void onSearchChanged(String query) {
        String lowerQuery = query.toLowerCase();
        filteredItems.clear();
        for (Item item : allItems) {
            if (item.getName().getString().toLowerCase().contains(lowerQuery)) {
                filteredItems.add(item);
            }
        }
        page = 0;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render search field
        searchField.render(context, mouseX, mouseY, delta);
        
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return;
        
        int maxPage = Math.max(0, (filteredItems.size() - 1) / itemsPerPage);
        if (page > maxPage) page = maxPage;
        
        // Render Page Indicator above the grid
        String pageText = (page + 1) + " / " + (maxPage + 1);
        int textWidth = client.textRenderer.getWidth(pageText);
        context.drawTextWithShadow(client.textRenderer, pageText, gridX + gridWidth - textWidth, gridY + gridHeight, 0xFFFFFF);
        
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = gridX;
        int currentY = gridY;
        
        for (int i = start; i < end; i++) {
            Item item = filteredItems.get(i);
            ItemStack stack = item.getDefaultStack();
            
            context.drawItem(stack, currentX, currentY);
            if (mouseX >= currentX && mouseX < currentX + 16 && mouseY >= currentY && mouseY < currentY + 16) {
                // Highlight hovered item
                context.fill(currentX, currentY, currentX + 16, currentY + 16, 0x80FFFFFF);
            }
            
            currentX += itemSize;
            if ((i - start + 1) % columns == 0) {
                currentX = gridX;
                currentY += itemSize;
            }
        }
    }
    
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (searchField.mouseClicked(click, doubleClick)) {
            return true;
        }
        
        if (click.button() == 0 && (click.x() < searchField.getX() || click.x() > searchField.getX() + searchField.getWidth() || click.y() < searchField.getY() || click.y() > searchField.getY() + searchField.getHeight())) {
            searchField.setFocused(false);
        }
        
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= gridX && mouseX <= gridX + gridWidth && mouseY >= gridY && mouseY <= gridY + gridHeight) {
            if (verticalAmount > 0 && page > 0) {
                page--;
                return true;
            } else if (verticalAmount < 0) {
                int itemsPerPage = columns * rows;
                int maxPage = (filteredItems.size() - 1) / itemsPerPage;
                if (page < maxPage) {
                    page++;
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean keyPressed(KeyInput keyInput) {
        if (searchField.isFocused()) {
            return searchField.keyPressed(keyInput);
        }
        return false;
    }
    
    public boolean charTyped(CharInput charInput) {
        if (searchField.isFocused()) {
            return searchField.charTyped(charInput);
        }
        return false;
    }

    public Item getHoveredItem(double mouseX, double mouseY) {
        if (mouseX < gridX || mouseX > gridX + gridWidth || mouseY < gridY || mouseY > gridY + gridHeight) {
            return null;
        }
        
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return null;
        
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = gridX;
        int currentY = gridY;
        
        for (int i = start; i < end; i++) {
            if (mouseX >= currentX && mouseX < currentX + 16 && mouseY >= currentY && mouseY < currentY + 16) {
                return filteredItems.get(i);
            }
            currentX += itemSize;
            if ((i - start + 1) % columns == 0) {
                currentX = gridX;
                currentY += itemSize;
            }
        }
        return null;
    }
}
