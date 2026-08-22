package com.example.client.mixin;

import com.example.client.accessor.HelperWidgetAccessor;
import com.example.client.config.HelperConfig;
import com.example.client.ui.ItemPanelWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.lwjgl.glfw.GLFW;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen implements HelperWidgetAccessor {

    @Shadow protected int x;
    @Shadow protected int y;
    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected net.minecraft.screen.slot.Slot focusedSlot;

    @Unique
    private ItemPanelWidget helper$itemPanelWidget;

    @Override
    public ItemPanelWidget helper$getWidget() {
        return this.helper$itemPanelWidget;
    }

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void helper$onInit(CallbackInfo ci) {
        if (HelperConfig.getInstance().enableSidePanel) {
            int panelX = this.x + this.backgroundWidth;
            this.helper$itemPanelWidget = new ItemPanelWidget(this.width, this.height, panelX, this.y, this.backgroundHeight);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void helper$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.helper$itemPanelWidget != null && HelperConfig.getInstance().enableSidePanel) {
            this.helper$itemPanelWidget.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void helper$onMouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (this.helper$itemPanelWidget != null && HelperConfig.getInstance().enableSidePanel) {
            if (this.helper$itemPanelWidget.mouseClicked(click, doubleClick)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void helper$onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (this.helper$itemPanelWidget != null && HelperConfig.getInstance().enableSidePanel) {
            if (this.helper$itemPanelWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void helper$onKeyPressed(net.minecraft.client.input.KeyInput keyInput, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = keyInput.key();
        int modifiers = keyInput.modifiers();

        // Let search field consume keys first
        if (this.helper$itemPanelWidget != null && HelperConfig.getInstance().enableSidePanel) {
            if (this.helper$itemPanelWidget.keyPressed(keyInput)) {
                cir.setReturnValue(true);
                return;
            }
        }
        
        // Toggle side panel with Ctrl+O
        if (keyCode == GLFW.GLFW_KEY_O && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            HelperConfig.getInstance().enableSidePanel = !HelperConfig.getInstance().enableSidePanel;
            HelperConfig.save();
            if (HelperConfig.getInstance().enableSidePanel && this.helper$itemPanelWidget == null) {
                int panelX = this.x + this.backgroundWidth;
                this.helper$itemPanelWidget = new ItemPanelWidget(this.width, this.height, panelX, this.y, this.backgroundHeight);
            }
            cir.setReturnValue(true);
            return;
        }

        // Don't process R/U when search is focused (user is typing)
        if (this.helper$itemPanelWidget != null && this.helper$itemPanelWidget.isSearchFocused()) {
            return;
        }

        // Recipe lookups
        if (keyCode == GLFW.GLFW_KEY_R || keyCode == GLFW.GLFW_KEY_U) {
            net.minecraft.item.Item hoveredItem = null;
            if (this.helper$itemPanelWidget != null && HelperConfig.getInstance().enableSidePanel) {
                double mouseX = this.client.mouse.getX() * this.width / (double)this.client.getWindow().getFramebufferWidth();
                double mouseY = this.client.mouse.getY() * this.height / (double)this.client.getWindow().getFramebufferHeight();
                hoveredItem = this.helper$itemPanelWidget.getHoveredItem(mouseX, mouseY);
            }
            if (hoveredItem == null && this.focusedSlot != null && this.focusedSlot.hasStack()) {
                hoveredItem = this.focusedSlot.getStack().getItem();
            }

            if (hoveredItem != null) {
                java.util.List<net.minecraft.recipe.RecipeEntry<?>> recipes;
                if (keyCode == GLFW.GLFW_KEY_R) {
                    recipes = com.example.client.recipe.RecipeIndexer.getRecipesForOutput(hoveredItem);
                } else {
                    recipes = com.example.client.recipe.RecipeIndexer.getRecipesForIngredient(hoveredItem);
                }
                
                this.client.setScreen(new com.example.client.ui.RecipePopupScreen(this, recipes));
                cir.setReturnValue(true);
            }
        }
    }

    // Override charTyped to intercept character input for the search bar.
    // Since HandledScreen doesn't override charTyped, we implement it here.
    // The mixin merges this into HandledScreen, overriding the inherited default.
    @Override
    public boolean charTyped(CharInput charInput) {
        if (this.helper$itemPanelWidget != null && HelperConfig.getInstance().enableSidePanel) {
            if (this.helper$itemPanelWidget.charTyped(charInput)) {
                return true;
            }
        }
        return super.charTyped(charInput);
    }
}
