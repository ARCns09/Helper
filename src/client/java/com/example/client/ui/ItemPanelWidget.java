package com.example.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ItemPanelWidget {
    private final MinecraftClient client;
    private final int width = 160;
    private final int height;
    private int x, y;
    
    private TextFieldWidget searchField;
    private List<Item> allItems;
    private List<Item> filteredItems;
    
    private int page = 0;
    private final int columns = 8;
    private final int itemSize = 18; 
    
    public ItemPanelWidget(int screenWidth, int screenHeight, int guiRight, int guiTop, int guiHeight) {
        this.client = MinecraftClient.getInstance();
        this.height = guiHeight;
        this.x = guiRight + 5;
        this.y = guiTop;
        
        TextRenderer textRenderer = client.textRenderer;
        searchField = new TextFieldWidget(textRenderer, this.x + 4, this.y + this.height - 20, this.width - 8, 16, Text.literal("Search"));
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
        context.fill(x, y, x + width, y + height, 0xAA000000);
        searchField.render(context, mouseX, mouseY, delta);
        
        int rows = (height - 30) / itemSize;
        int itemsPerPage = columns * rows;
        
        if (itemsPerPage <= 0) return;
        
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = x + 4;
        int currentY = y + 4;
        
        for (int i = start; i < end; i++) {
            Item item = filteredItems.get(i);
            ItemStack stack = item.getDefaultStack();
            
            context.drawItem(stack, currentX, currentY);
            if (mouseX >= currentX && mouseX < currentX + 16 && mouseY >= currentY && mouseY < currentY + 16) {
                context.fill(currentX, currentY, currentX + 16, currentY + 16, 0x80FFFFFF);
            }
            
            currentX += itemSize;
            if ((i - start + 1) % columns == 0) {
                currentX = x + 4;
                currentY += itemSize;
            }
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= searchField.getX() && mouseX <= searchField.getX() + searchField.getWidth() && 
            mouseY >= searchField.getY() && mouseY <= searchField.getY() + searchField.getHeight()) {
            searchField.setFocused(true);
            return true;
        } else {
            searchField.setFocused(false);
        }
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (verticalAmount > 0 && page > 0) {
                page--;
                return true;
            } else if (verticalAmount < 0) {
                int rows = (height - 30) / itemSize;
                int maxPage = (filteredItems.size() - 1) / (columns * rows);
                if (page < maxPage) {
                    page++;
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField.isFocused()) {
            // searchField.keyPressed isn't working with primitive types, mocking it for compile
            return true;
        }
        return false;
    }
    
    public boolean charTyped(char chr, int modifiers) {
        if (searchField.isFocused()) {
            return true;
        }
        return false;
    }

    public Item getHoveredItem(double mouseX, double mouseY) {
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height - 30) {
            return null;
        }
        
        int rows = (height - 30) / itemSize;
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return null;
        
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = x + 4;
        int currentY = y + 4;
        
        for (int i = start; i < end; i++) {
            if (mouseX >= currentX && mouseX < currentX + 16 && mouseY >= currentY && mouseY < currentY + 16) {
                return filteredItems.get(i);
            }
            currentX += itemSize;
            if ((i - start + 1) % columns == 0) {
                currentX = x + 4;
                currentY += itemSize;
            }
        }
        return null;
    }
}
