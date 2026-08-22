package com.example.client.mixin;

import com.example.client.accessor.HelperWidgetAccessor;
import com.example.client.config.HelperConfig;
import com.example.client.ui.ItemPanelWidget;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.CharInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParentElement.class)
public interface ScreenMixin {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    default void helper$onCharTyped(CharInput charInput, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof HandledScreen) {
            ItemPanelWidget widget = ((HelperWidgetAccessor) this).helper$getWidget();
            if (widget != null && HelperConfig.getInstance().enableSidePanel) {
                if (widget.charTyped(charInput)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
