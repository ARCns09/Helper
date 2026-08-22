package com.example.client.mixin;

import com.example.client.config.HelperConfig;
import com.example.client.ui.ItemPanelWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void helper$onCharTyped(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof HandledScreen) {
            ItemPanelWidget widget = HandledScreenAccessor.getHelperWidget((HandledScreen<?>)(Object)this);
            if (widget != null && HelperConfig.getInstance().enableSidePanel) {
                if (widget.charTyped(chr, modifiers)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
