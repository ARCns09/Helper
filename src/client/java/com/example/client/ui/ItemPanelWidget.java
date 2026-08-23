package com.example.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
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
    
    // Grid positioning
    private int gridX, gridY;
    private final int itemSize = 18; 
    private int columns;
    private int rows;
    private int screenWidth, screenHeight;
    
    // Manual search (no TextFieldWidget)
    private String searchQuery = "";
    private boolean searchFocused = false;
    private int searchCursorTick = 0;
    
    // Search bar position (bottom center)
    private int searchBoxX, searchBoxY, searchBoxW, searchBoxH;
    
    private List<Item> filteredItems;
    
    private int page = 0;
    
    // Navigation header
    private int headerY, headerH;
    private int navLeftX, navRightX, navBtnW, navBtnH;
    private int panelW;
    
    // Colors - EMI-like slate blue tint
    private static final int BG_PANEL = 0x66202535;      // Subtle dark slate background
    private static final int BG_HEADER = 0xBB2A2F42;     // Slightly more opaque header
    private static final int BG_SLOT_DARK = 0x22000000;   // Checkerboard dark
    private static final int BG_SLOT_LIGHT = 0x11FFFFFF;  // Checkerboard light
    private static final int BG_SEARCH = 0xDD1A1E2E;     // Search bar background
    private static final int BORDER_COLOR = 0xFF4A4F6A;   // Muted blue-gray border
    private static final int NAV_BTN_BG = 0xAA2A2F42;    // Nav button normal
    private static final int NAV_BTN_HOVER = 0xCC3A4058;  // Nav button hover
    
    public ItemPanelWidget(int screenWidth, int screenHeight, int guiRight, int guiTop, int guiHeight) {
        this.client = MinecraftClient.getInstance();
        updateBounds(screenWidth, screenHeight, guiRight, guiTop, guiHeight);
        updateFilter();
    }
    
    public void updateBounds(int screenWidth, int screenHeight, int guiRight, int guiTop, int guiHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        // Grid starts right after inventory
        this.gridX = guiRight + 4;
        
        // Let's use the full height of the screen for REI/EMI style
        int margin = 5;
        this.headerH = 18;
        this.headerY = margin;
        
        this.gridY = headerY + headerH + 2;
        
        // Calculate max space
        int maxW = screenWidth - this.gridX - margin;
        int maxH = screenHeight - this.gridY - margin;
        
        this.columns = maxW / itemSize;
        if (this.columns < 1) this.columns = 1;
        
        // Leave space for search box at the bottom
        int searchAreaHeight = 24;
        
        this.rows = (maxH - searchAreaHeight) / itemSize;
        if (this.rows < 1) this.rows = 1;
        
        this.panelW = this.columns * itemSize;
        
        this.navBtnW = 16;
        this.navBtnH = 16;
        
        this.searchBoxW = this.panelW;
        this.searchBoxH = 14;
        this.searchBoxX = this.gridX;
        this.searchBoxY = this.gridY + (this.rows * itemSize) + 4;
        
        // Nav button positions
        this.navLeftX = this.gridX;
        this.navRightX = this.gridX + panelW - navBtnW;
        
        
        // Initialize search engine
        updateFilter();
    }
    
    private void updateFilter() {
        filteredItems = com.example.client.search.SearchEngine.search(searchQuery);
        page = 0;
    }
    
    private void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y + 1, x + 1, y + h - 1, color);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }
    
    private int getMaxPage() {
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return 0;
        return Math.max(0, (filteredItems.size() - 1) / itemsPerPage);
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        searchCursorTick++;
        int itemsPerPage = columns * rows;
        if (itemsPerPage <= 0) return;
        
        int maxPage = getMaxPage();
        if (page > maxPage) page = maxPage;
        
        TextRenderer tr = client.textRenderer;
        
        // ── Panel Background (subtle slate tint) ──
        int panelBot = gridY + (rows * itemSize);
        context.fill(gridX - 2, headerY, gridX + panelW + 2, panelBot + 2, BG_PANEL);
        
        // ── Navigation Header Bar ──
        int hx = gridX - 2;
        int hw = panelW + 4;
        context.fill(hx, headerY, hx + hw, headerY + headerH, BG_HEADER);
        drawBorder(context, hx, headerY, hw, headerH, BORDER_COLOR);
        
        // Left arrow button <
        int lbx = gridX + 1;
        int lby = headerY + 1;
        boolean leftHover = mouseX >= lbx && mouseX < lbx + navBtnW && mouseY >= lby && mouseY < lby + navBtnH;
        context.fill(lbx, lby, lbx + navBtnW, lby + navBtnH, leftHover ? NAV_BTN_HOVER : NAV_BTN_BG);
        drawBorder(context, lbx, lby, navBtnW, navBtnH, BORDER_COLOR);
        context.drawCenteredTextWithShadow(tr, "<", lbx + navBtnW / 2, lby + 3, page > 0 ? 0xFFFFFFFF : 0xFF666666);
        
        // Right arrow button >
        int rbx = gridX + panelW - navBtnW - 1;
        int rby = headerY + 1;
        boolean rightHover = mouseX >= rbx && mouseX < rbx + navBtnW && mouseY >= rby && mouseY < rby + navBtnH;
        context.fill(rbx, rby, rbx + navBtnW, rby + navBtnH, rightHover ? NAV_BTN_HOVER : NAV_BTN_BG);
        drawBorder(context, rbx, rby, navBtnW, navBtnH, BORDER_COLOR);
        context.drawCenteredTextWithShadow(tr, ">", rbx + navBtnW / 2, rby + 3, page < maxPage ? 0xFFFFFFFF : 0xFF666666);
        
        // Page text "Page X of Y" centered
        String pageText = "Page " + (page + 1) + " of " + (maxPage + 1);
        int centerX = gridX + panelW / 2;
        context.drawCenteredTextWithShadow(tr, pageText, centerX, headerY + 5, 0xFFFFFFFF);
        
        // Progress bar under page text
        if (maxPage > 0) {
            int barW = Math.min(60, panelW / 2);
            int barX = centerX - barW / 2;
            int barY = headerY + headerH - 3;
            context.fill(barX, barY, barX + barW, barY + 2, 0x40FFFFFF); // Track
            int fillW = (int)(barW * ((double)(page) / maxPage));
            context.fill(barX, barY, barX + fillW, barY + 2, 0xFFFFFFFF); // Fill
        }
        
        // ── Checkerboard Item Grid ──
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = gridX + col * itemSize;
                int slotY = gridY + row * itemSize;
                boolean dark = (row + col) % 2 == 0;
                context.fill(slotX, slotY, slotX + itemSize, slotY + itemSize, dark ? BG_SLOT_DARK : BG_SLOT_LIGHT);
            }
        }
        
        // Draw items
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredItems.size());
        
        int currentX = gridX;
        int currentY = gridY;
        for (int i = start; i < end; i++) {
            Item item = filteredItems.get(i);
            ItemStack stack = item.getDefaultStack();
            
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
        
        // ── Search Bar (bottom center, manual rendering) ──
        context.fill(searchBoxX, searchBoxY, searchBoxX + searchBoxW, searchBoxY + searchBoxH, BG_SEARCH);
        drawBorder(context, searchBoxX, searchBoxY, searchBoxW, searchBoxH, searchFocused ? 0xFFFFFFFF : BORDER_COLOR);
        
        String displayText;
        if (searchQuery.isEmpty() && !searchFocused) {
            displayText = "Search...";
            context.drawTextWithShadow(tr, displayText, searchBoxX + 4, searchBoxY + 3, 0xFF888888);
        } else {
            displayText = searchQuery;
            context.drawTextWithShadow(tr, displayText, searchBoxX + 4, searchBoxY + 3, 0xFFFFFFFF);
            // Blinking cursor
            if (searchFocused && (searchCursorTick / 10) % 2 == 0) {
                int cursorX = searchBoxX + 4 + tr.getWidth(displayText);
                context.fill(cursorX, searchBoxY + 2, cursorX + 1, searchBoxY + searchBoxH - 2, 0xFFFFFFFF);
            }
        }
        
        // ── Tooltip for hovered item (draw last so it's on top) ──
        Item hovered = getHoveredItem(mouseX, mouseY);
        if (hovered != null) {
            ItemStack hoveredStack = hovered.getDefaultStack();
            context.drawItemTooltip(client.textRenderer, hoveredStack, mouseX, mouseY);
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        
        // Handle search bar click
        if (mouseX >= searchBoxX && mouseX <= searchBoxX + searchBoxW && mouseY >= searchBoxY && mouseY <= searchBoxY + searchBoxH) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }
        
        // Handle left arrow
        int lbx = gridX + 1;
        int lby = headerY + 1;
        if (mouseX >= lbx && mouseX < lbx + navBtnW && mouseY >= lby && mouseY < lby + navBtnH) {
            if (page > 0) page--;
            return true;
        }
        
        // Handle right arrow
        int rbx = gridX + panelW - navBtnW - 1;
        int rby = headerY + 1;
        if (mouseX >= rbx && mouseX < rbx + navBtnW && mouseY >= rby && mouseY < rby + navBtnH) {
            if (page < getMaxPage()) page++;
            return true;
        }
        
        // Handle clicking an item → open recipe viewer
        Item clickedItem = getHoveredItem(mouseX, mouseY);
        if (clickedItem != null) {
            if (button == 0) { // Left click: show recipes
                java.util.List<net.minecraft.recipe.RecipeDisplayEntry> recipes =
                    com.example.client.recipe.RecipeIndexer.getRecipesForOutput(clickedItem);
                if (!recipes.isEmpty() && client.currentScreen != null) {
                    client.setScreen(new RecipePopupScreen(client.currentScreen, recipes));
                }
                return true;
            } else if (button == 1) { // Right click: show usages
                java.util.List<net.minecraft.recipe.RecipeDisplayEntry> usages =
                    com.example.client.recipe.RecipeIndexer.getRecipesForIngredient(clickedItem);
                if (!usages.isEmpty() && client.currentScreen != null) {
                    client.setScreen(new RecipePopupScreen(client.currentScreen, usages));
                }
                return true;
            }
        }
        
        return false;
    }
    
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
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
        int keyCode = keyInput.key();
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    updateFilter();
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
                searchFocused = false;
                return true;
            }
            // Consume all other key presses when focused to prevent E closing inventory etc.
            return true;
        }
        return false;
    }
    
    public boolean charTyped(CharInput charInput) {
        if (searchFocused) {
            if (charInput.isValidChar()) {
                searchQuery += charInput.asString();
                updateFilter();
                return true;
            }
        }
        return false;
    }

    public Item getHoveredItem(double mouseX, double mouseY) {
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
        return searchFocused;
    }
}
