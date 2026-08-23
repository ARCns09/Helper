package com.example.client;

import com.example.client.config.HelperConfig;
import net.fabricmc.api.ClientModInitializer;

public class HelperClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HelperConfig.load();
		
		net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof com.example.client.accessor.HelperWidgetAccessor accessor) {
				net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.allowMouseClick(screen).register((screen1, event) -> {
					com.example.client.ui.ItemPanelWidget widget = accessor.helper$getWidget();
					if (widget != null && HelperConfig.getInstance().enableSidePanel) {
						if (widget.mouseClicked(event.x(), event.y(), event.button())) return false;
					}
					return true;
				});
				net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.allowMouseScroll(screen).register((screen1, mouseX, mouseY, horizontalAmount, verticalAmount) -> {
					com.example.client.ui.ItemPanelWidget widget = accessor.helper$getWidget();
					if (widget != null && HelperConfig.getInstance().enableSidePanel) {
						if (widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return false;
					}
					return true;
				});
				net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterRender(screen).register((screen1, context, mouseX, mouseY, tickDelta) -> {
					com.example.client.ui.ItemPanelWidget widget = accessor.helper$getWidget();
					if (widget != null && HelperConfig.getInstance().enableSidePanel && screen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> handled) {
						com.example.client.mixin.HandledScreenAccessor hsAccessor = (com.example.client.mixin.HandledScreenAccessor) handled;
						int panelX = hsAccessor.getX() + hsAccessor.getBackgroundWidth();
						widget.updateBounds(screen.width, screen.height, panelX, hsAccessor.getY(), hsAccessor.getBackgroundHeight());
						widget.render(context, mouseX, mouseY, tickDelta);
					}
				});
			}
		});
	}
}