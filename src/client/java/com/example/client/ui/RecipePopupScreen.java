package com.example.client.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.text.Text;

import java.util.List;

public class RecipePopupScreen extends Screen {
    private final Screen parent;
    private final List<RecipeEntry<?>> recipes;
    private int currentIndex = 0;

    public RecipePopupScreen(Screen parent, List<RecipeEntry<?>> recipes) {
        super(Text.literal("Recipes"));
        this.parent = parent;
        this.recipes = recipes;
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw parent screen in background to act like an overlay
        parent.render(context, -1000, -1000, delta);
        
        // Draw dim overlay
        context.fill(0, 0, this.width, this.height, 0x80000000);

        if (recipes.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "No recipes found.", this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        int windowWidth = 160;
        int windowHeight = 120;
        int x = (this.width - windowWidth) / 2;
        int y = (this.height - windowHeight) / 2;

        // Draw EMI-style Window Background (Light Gray with Border)
        context.fill(x, y, x + windowWidth, y + windowHeight, 0xFFC6C6C6);
        drawBorder(context, x - 1, y - 1, windowWidth + 2, windowHeight + 2, 0xFF000000);
        drawBorder(context, x, y, windowWidth, windowHeight, 0xFFFFFFFF);

        // Header
        context.fill(x + 2, y + 2, x + windowWidth - 2, y + 16, 0xFF8B8B8B); // Darker header
        context.drawCenteredTextWithShadow(this.textRenderer, "Crafting", x + windowWidth / 2, y + 5, 0xFFFFFF);

        // Navigation (Top corners)
        context.drawTextWithShadow(this.textRenderer, "<", x + 5, y + 5, currentIndex > 0 ? 0xFFFFFF : 0x888888);
        context.drawTextWithShadow(this.textRenderer, ">", x + windowWidth - 12, y + 5, currentIndex < recipes.size() - 1 ? 0xFFFFFF : 0x888888);
        
        // Page text
        String pageText = "Page " + (currentIndex + 1) + " of " + recipes.size();
        context.drawText(this.textRenderer, pageText, x + (windowWidth - this.textRenderer.getWidth(pageText)) / 2, y + 20, 0x404040, false);

        // Draw 3x3 Grid (Centered on the left)
        int gridX = x + 15;
        int gridY = y + 40;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = gridX + col * 18;
                int slotY = gridY + row * 18;
                context.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF8B8B8B);
                drawBorder(context, slotX, slotY, 18, 18, 0xFFFFFFFF);
            }
        }

        // Draw Arrow
        int arrowX = gridX + 3 * 18 + 10;
        int arrowY = gridY + 18;
        context.drawText(this.textRenderer, "->", arrowX, arrowY + 5, 0x404040, false);

        // Draw Output Slot
        int outX = arrowX + 25;
        int outY = arrowY - 4; // Center with middle row
        context.fill(outX, outY, outX + 26, outY + 26, 0xFF8B8B8B);
        drawBorder(context, outX, outY, 26, 26, 0xFFFFFFFF);
        
        // Draw output item if possible (Placeholder for now)
        if (this.client != null && this.client.world != null) {
            // In a real implementation we would get the RecipeDisplay and draw the slot displays
            // context.drawItem(itemStack, outX + 5, outY + 5);
        }
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
        int windowWidth = 160;
        int windowHeight = 120;
        int x = (this.width - windowWidth) / 2;
        int y = (this.height - windowHeight) / 2;
        
        // Check left arrow click
        if (click.x() >= x && click.x() <= x + 20 && click.y() >= y && click.y() <= y + 16) {
            if (currentIndex > 0) currentIndex--;
            return true;
        }
        
        // Check right arrow click
        if (click.x() >= x + windowWidth - 20 && click.x() <= x + windowWidth && click.y() >= y && click.y() <= y + 16) {
            if (currentIndex < recipes.size() - 1) currentIndex++;
            return true;
        }
        
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput keyInput) {
        int keyCode = keyInput.key();
        if (keyCode == 256) { // ESC
            if (this.client != null) {
                this.client.setScreen(parent);
            }
            return true;
        } else if (keyCode == 262) { // Right Arrow
            if (currentIndex < recipes.size() - 1) currentIndex++;
            return true;
        } else if (keyCode == 263) { // Left Arrow
            if (currentIndex > 0) currentIndex--;
            return true;
        }
        return super.keyPressed(keyInput);
    }
}
