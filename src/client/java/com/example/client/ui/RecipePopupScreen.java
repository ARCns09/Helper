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

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        if (recipes.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, "No recipes found.", this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        RecipeEntry<?> entry = recipes.get(currentIndex);
        context.drawCenteredTextWithShadow(this.textRenderer, entry.id().toString(), this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Recipe " + (currentIndex + 1) + " of " + recipes.size(), this.width / 2, 35, 0xAAAAAA);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        context.drawCenteredTextWithShadow(this.textRenderer, "Crafting Grid", centerX - 50, centerY - 40, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Output", centerX + 50, centerY - 40, 0xFFFFFF);

        context.fill(centerX - 70, centerY - 20, centerX - 30, centerY + 20, 0x55FFFFFF);
        context.fill(centerX + 30, centerY - 10, centerX + 70, centerY + 30, 0x55FFFFFF);

        if (this.client != null && this.client.world != null) {
            context.drawItem(net.minecraft.item.ItemStack.EMPTY, centerX + 42, centerY + 2);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, "Press ESC to close, Left/Right arrows to navigate.", this.width / 2, this.height - 20, 0x888888);
    }

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
