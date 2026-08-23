package com.example.client.mixin;

import com.example.client.accessor.HelperWidgetAccessor;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow private MinecraftClient client;

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void helper$onChar(long window, CharInput charInput, CallbackInfo ci) {
        if (this.client != null && window == this.client.getWindow().getHandle()) {
            Screen screen = this.client.currentScreen;
            if (screen instanceof HelperWidgetAccessor accessor) {
                if (accessor.helper$getWidget() != null && accessor.helper$getWidget().charTyped(charInput)) {
                    ci.cancel();
                }
            }
        }
    }
}
