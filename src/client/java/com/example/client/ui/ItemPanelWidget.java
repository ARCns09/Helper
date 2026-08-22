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

import java.util.ArrayList;
import java.util.List;

public class ItemPanelWidget {
    private final MinecraftClient client;
    
    private int gridX, gridY, gridWidth, gridHeight;
    private final int itemSize = 18; 
    private int columns;
    private int rows;
    
    private TextFieldWidget searchField;
    private List<Item> allItems;
    private List<Item> filteredItems;
    
    private int page = 0;
    
    // Header navigation bounds
    private int leftArrowX, rightArrowX, arrowY, arrowWidth, arrowHeight;
    
    public ItemPanelWidget(int screenWidth, int screenHeight, int guiRight, int guiTop, int guiHeight) {
        this.client = MinecraftClient.getInstance();
        
        this.gridX = guiRight + 6;
        this.gridY = 8;
        this.gridWidth = screenWidth - this.gridX - 6;
        this.gridHeight = screenHeight - 48; // Leave space for bottom search bar
        
        this.columns = Math.max(1, this.gridWidth / itemSize);
        this.rows = Math.max(1, (this.gridHeight - 20) / itemSize); // 20px for header
        
        // Navigation bounds
        this.arrowWidth = 14;
        this.arrowHeight = 14;
        this.arrowY = this.gridY;
        this.leftArrowX = this.gridX;
        this.rightArrowX = this.gridX + (this.columns * itemSize) - this.arrowWidth;
        
        int searchWidth = 240;
        int searchX = (screenWidth - searchWidth) / 2;
        int searchY = screenHeight - 24;
        
        TextRenderer textRenderer = client.textRenderer;
        searchField = new TextFieldWidget(textRenderer, searchX + 4, searchY + 4, searchWidth - 8, 12, Text.literal("Search"));
        searchField.setDrawsBackground(false);
        searchField.setPlaceholder(Text.literal("Search EMI..."));
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
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return;
        
        int maxPage = Math.max(0, (filteredItems.size() - 1) / itemsPerPage);
        if (page > maxPage) page = maxPage;
        
        // Render Search Bar background (EMI style)
        int searchWidth = 240;
        int searchX = (client.getWindow().getScaledWidth() - searchWidth) / 2;
        int searchY = client.getWindow().getScaledHeight() - 24;
        context.fill(searchX, searchY, searchX + searchWidth, searchY + 18, 0xCC000000); // Dark background
        drawBorder(context, searchX, searchY, searchWidth, 18, 0xFF555555); // Gray border
        
        searchField.render(context, mouseX, mouseY, delta);
        
        // Render Right Panel Top Navigation
        boolean leftHover = mouseX >= leftArrowX && mouseX < leftArrowX + arrowWidth && mouseY >= arrowY && mouseY < arrowY + arrowHeight;
        boolean rightHover = mouseX >= rightArrowX && mouseX < rightArrowX + arrowWidth && mouseY >= arrowY && mouseY < arrowY + arrowHeight;
        
        context.fill(leftArrowX, arrowY, leftArrowX + arrowWidth, arrowY + arrowHeight, leftHover ? 0x80FFFFFF : 0x40000000);
        context.fill(rightArrowX, arrowY, rightArrowX + arrowWidth, arrowY + arrowHeight, rightHover ? 0x80FFFFFF : 0x40000000);
        
        context.drawCenteredTextWithShadow(client.textRenderer, "<", leftArrowX + arrowWidth / 2, arrowY + 3, 0xFFFFFF);
        context.drawCenteredTextWithShadow(client.textRenderer, ">", rightArrowX + arrowWidth / 2, arrowY + 3, 0xFFFFFF);
        
        String pageText = "Page " + (page + 1) + " of " + (maxPage + 1);
        int pageTextX = leftArrowX + arrowWidth + ((rightArrowX - (leftArrowX + arrowWidth)) / 2);
        context.drawCenteredTextWithShadow(client.textRenderer, pageText, pageTextX, arrowY + 3, 0xFFFFFF);
        
        // Render Grid
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = gridX;
        int currentY = gridY + 20; // Below header
        
        for (int i = start; i < end; i++) {
            Item item = filteredItems.get(i);
            ItemStack stack = item.getDefaultStack();
            
            context.drawItem(stack, currentX, currentY);
            if (mouseX >= currentX && mouseX < currentX + 16 && mouseY >= currentY && mouseY < currentY + 16) {
                // Highlight hovered item (EMI style light highlight)
                context.fill(currentX, currentY, currentX + 16, currentY + 16, 0x40FFFFFF);
            }
            
            currentX += itemSize;
            if ((i - start + 1) % columns == 0) {
                currentX = gridX;
                currentY += itemSize;
            }
        }
    }
    
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double mouseX = click.x();
        double mouseY = click.y();
        
        // Handle search bar focus
        int searchWidth = 240;
        int searchX = (client.getWindow().getScaledWidth() - searchWidth) / 2;
        int searchY = client.getWindow().getScaledHeight() - 24;
        
        if (mouseX >= searchX && mouseX <= searchX + searchWidth && mouseY >= searchY && mouseY <= searchY + 18) {
            searchField.setFocused(true);
            searchField.mouseClicked(click, doubleClick);
            return true;
        } else {
            searchField.setFocused(false);
        }
        
        // Handle page navigation clicks
        if (mouseX >= leftArrowX && mouseX < leftArrowX + arrowWidth && mouseY >= arrowY && mouseY < arrowY + arrowHeight) {
            if (page > 0) page--;
            return true;
        }
        
        if (mouseX >= rightArrowX && mouseX < rightArrowX + arrowWidth && mouseY >= arrowY && mouseY < arrowY + arrowHeight) {
            int itemsPerPage = columns * rows;
            int maxPage = (filteredItems.size() - 1) / itemsPerPage;
            if (page < maxPage) page++;
            return true;
        }
        
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= gridX && mouseX <= gridX + (columns * itemSize) && mouseY >= gridY && mouseY <= gridY + gridHeight) {
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
        if (mouseX < gridX || mouseX >= gridX + (columns * itemSize) || mouseY < gridY + 20 || mouseY >= gridY + 20 + (rows * itemSize)) {
            return null;
        }
        
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return null;
        
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = gridX;
        int currentY = gridY + 20;
        
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
