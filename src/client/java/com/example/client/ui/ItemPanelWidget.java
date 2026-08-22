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
    
    // Grid positioning (computed dynamically)
    private int gridX, gridY;
    private final int itemSize = 18; 
    private int columns;
    private int rows;
    private int screenWidth, screenHeight;
    
    // Search field
    private TextFieldWidget searchField;
    private int searchBoxX, searchBoxY, searchBoxW, searchBoxH;
    
    private List<Item> allItems;
    private List<Item> filteredItems;
    
    private int page = 0;
    
    // Navigation header bounds
    private int headerY;
    private int navLeftX, navRightX, navBtnW, navBtnH;
    
    public ItemPanelWidget(int screenWidth, int screenHeight, int guiRight, int guiTop, int guiHeight) {
        this.client = MinecraftClient.getInstance();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Grid starts right after inventory, fills to screen edge
        // Leave 2px margin on sides
        this.gridX = guiRight + 4;
        int availWidth = screenWidth - this.gridX - 2;
        this.columns = Math.max(1, availWidth / itemSize);
        
        // Header at top of panel area (2px from screen top)
        this.headerY = 2;
        this.navBtnW = 12;
        this.navBtnH = 12;
        
        // Grid starts below header
        this.gridY = this.headerY + navBtnH + 4;
        
        // Search bar at bottom: centered, 20px from bottom
        this.searchBoxW = Math.min(200, screenWidth - 20);
        this.searchBoxH = 16;
        this.searchBoxX = (screenWidth - searchBoxW) / 2;
        this.searchBoxY = screenHeight - searchBoxH - 4;
        
        // Available rows between grid top and search bar
        int availHeight = this.searchBoxY - 4 - this.gridY;
        this.rows = Math.max(1, availHeight / itemSize);
        
        // Navigation button positions
        this.navLeftX = this.gridX;
        this.navRightX = this.gridX + (this.columns * itemSize) - navBtnW;
        
        // Create search field
        TextRenderer textRenderer = client.textRenderer;
        searchField = new TextFieldWidget(textRenderer, searchBoxX + 2, searchBoxY + 3, searchBoxW - 4, searchBoxH - 6, Text.literal("Search"));
        searchField.setDrawsBackground(false);
        searchField.setPlaceholder(Text.literal("Search EMI..."));
        searchField.setChangedListener(this::onSearchChanged);
        searchField.setMaxLength(256);
        
        // Load all items except air
        allItems = new ArrayList<>();
        Registries.ITEM.forEach(item -> {
            if (item != net.minecraft.item.Items.AIR) {
                allItems.add(item);
            }
        });
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
    
    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);             // top
        context.fill(x, y + h - 1, x + w, y + h, color);     // bottom
        context.fill(x, y + 1, x + 1, y + h - 1, color);     // left
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color); // right
    }
    
    private int getMaxPage() {
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return 0;
        return Math.max(0, (filteredItems.size() - 1) / itemsPerPage);
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return;
        
        int maxPage = getMaxPage();
        if (page > maxPage) page = maxPage;
        
        int panelW = columns * itemSize;
        
        // ── Search Bar (bottom center) ──
        context.fill(searchBoxX, searchBoxY, searchBoxX + searchBoxW, searchBoxY + searchBoxH, 0xDD000000);
        drawBorder(context, searchBoxX, searchBoxY, searchBoxW, searchBoxH, 0xFF666666);
        searchField.render(context, mouseX, mouseY, delta);
        
        // ── Panel Background ──
        int panelTop = headerY;
        int panelBot = gridY + (rows * itemSize) + 2;
        context.fill(gridX - 2, panelTop, gridX + panelW + 2, panelBot, 0x90000000);
        
        // ── Navigation Header ──
        // Left arrow button
        boolean leftHover = mouseX >= navLeftX && mouseX < navLeftX + navBtnW && mouseY >= headerY && mouseY < headerY + navBtnH;
        context.fill(navLeftX, headerY, navLeftX + navBtnW, headerY + navBtnH, leftHover ? 0xA0FFFFFF : 0x60000000);
        drawBorder(context, navLeftX, headerY, navBtnW, navBtnH, 0xFF888888);
        context.drawCenteredTextWithShadow(client.textRenderer, "\u25C0", navLeftX + navBtnW / 2, headerY + 2, page > 0 ? 0xFFFFFF : 0x666666);
        
        // Right arrow button
        boolean rightHover = mouseX >= navRightX && mouseX < navRightX + navBtnW && mouseY >= headerY && mouseY < headerY + navBtnH;
        context.fill(navRightX, headerY, navRightX + navBtnW, headerY + navBtnH, rightHover ? 0xA0FFFFFF : 0x60000000);
        drawBorder(context, navRightX, headerY, navBtnW, navBtnH, 0xFF888888);
        context.drawCenteredTextWithShadow(client.textRenderer, "\u25B6", navRightX + navBtnW / 2, headerY + 2, page < maxPage ? 0xFFFFFF : 0x666666);
        
        // Page text between arrows
        String pageText = (page + 1) + " / " + (maxPage + 1);
        int centerX = navLeftX + navBtnW + ((navRightX - (navLeftX + navBtnW)) / 2);
        context.drawCenteredTextWithShadow(client.textRenderer, pageText, centerX, headerY + 2, 0xFFFFFF);
        
        // ── Item Grid with checkerboard ──
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = gridX;
        int currentY = gridY;
        
        // Draw checkerboard backgrounds first
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = gridX + col * itemSize;
                int slotY = gridY + row * itemSize;
                // Alternating light/dark
                boolean dark = (row + col) % 2 == 0;
                context.fill(slotX, slotY, slotX + itemSize, slotY + itemSize, dark ? 0x30000000 : 0x18FFFFFF);
            }
        }
        
        // Draw items
        currentX = gridX;
        currentY = gridY;
        for (int i = start; i < end; i++) {
            Item item = filteredItems.get(i);
            ItemStack stack = item.getDefaultStack();
            
            // Draw item icon centered in slot
            int iconX = currentX + 1;
            int iconY = currentY + 1;
            context.drawItem(stack, iconX, iconY);
            
            // Hover highlight
            if (mouseX >= currentX && mouseX < currentX + itemSize && mouseY >= currentY && mouseY < currentY + itemSize) {
                context.fill(currentX, currentY, currentX + itemSize, currentY + itemSize, 0x50FFFFFF);
            }
            
            currentX += itemSize;
            if ((i - start + 1) % columns == 0) {
                currentX = gridX;
                currentY += itemSize;
            }
        }
        
        // ── Tooltip for hovered item ──
        Item hovered = getHoveredItem(mouseX, mouseY);
        if (hovered != null) {
            ItemStack hoveredStack = hovered.getDefaultStack();
            context.drawItemTooltip(client.textRenderer, hoveredStack, mouseX, mouseY);
        }
    }
    
    public boolean mouseClicked(Click click, boolean doubleClick) {
        double mouseX = click.x();
        double mouseY = click.y();
        
        // Handle search bar click
        if (mouseX >= searchBoxX && mouseX <= searchBoxX + searchBoxW && mouseY >= searchBoxY && mouseY <= searchBoxY + searchBoxH) {
            searchField.setFocused(true);
            searchField.mouseClicked(click, doubleClick);
            return true;
        } else {
            searchField.setFocused(false);
        }
        
        // Handle page navigation clicks
        if (mouseX >= navLeftX && mouseX < navLeftX + navBtnW && mouseY >= headerY && mouseY < headerY + navBtnH) {
            if (page > 0) page--;
            return true;
        }
        if (mouseX >= navRightX && mouseX < navRightX + navBtnW && mouseY >= headerY && mouseY < headerY + navBtnH) {
            if (page < getMaxPage()) page++;
            return true;
        }
        
        // Handle clicking an item in the grid → open recipe viewer
        Item clickedItem = getHoveredItem(mouseX, mouseY);
        if (clickedItem != null && click.button() == 0) {
            // Left click → show recipes that OUTPUT this item (like pressing R)
            java.util.List<net.minecraft.recipe.RecipeEntry<?>> recipes =
                com.example.client.recipe.RecipeIndexer.getRecipesForOutput(clickedItem);
            if (!recipes.isEmpty() && client.currentScreen != null) {
                client.setScreen(new RecipePopupScreen(client.currentScreen, recipes));
            }
            return true;
        }
        
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelW = columns * itemSize;
        int panelBot = gridY + (rows * itemSize);
        if (mouseX >= gridX - 2 && mouseX <= gridX + panelW + 2 && mouseY >= headerY && mouseY <= panelBot) {
            if (verticalAmount > 0 && page > 0) {
                page--;
                return true;
            } else if (verticalAmount < 0 && page < getMaxPage()) {
                page++;
                return true;
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
        int panelW = columns * itemSize;
        int panelH = rows * itemSize;
        if (mouseX < gridX || mouseX >= gridX + panelW || mouseY < gridY || mouseY >= gridY + panelH) {
            return null;
        }
        
        int col = (int)((mouseX - gridX) / itemSize);
        int row = (int)((mouseY - gridY) / itemSize);
        
        if (col < 0 || col >= columns || row < 0 || row >= rows) return null;
        
        int itemsPerPage = columns * rows;
        int index = page * itemsPerPage + row * columns + col;
        
        if (index >= 0 && index < filteredItems.size()) {
            return filteredItems.get(index);
        }
        return null;
    }
    
    public boolean isSearchFocused() {
        return searchField.isFocused();
    }
}
