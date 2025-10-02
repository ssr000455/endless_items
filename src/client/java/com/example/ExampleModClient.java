package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import static com.example.ExampleMod.STREET_BLOCK;
import static com.example.ExampleMod.TEST_STONE;
import static com.example.ExampleMod.SIMPLE_SWORD;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(STREET_BLOCK, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(TEST_STONE, RenderLayer.getCutout());

        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof InventoryScreen) {
                ScreenEvents.afterRender(screen).register((scr, context, mouseX, mouseY, delta) -> {
                    context.drawTextWithShadow(
                        client.textRenderer,
                        Text.literal("无尽物品模组"),
                        scr.width - 100,
                        10,
                        0xFFFFFF
                    );
                });
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            renderModHud(drawContext);
        });
    }

    private void renderModHud(net.minecraft.client.gui.DrawContext drawContext) {
        PlayerEntity player = net.minecraft.client.MinecraftClient.getInstance().player;
        if (player != null && (player.getMainHandStack().getItem() == SIMPLE_SWORD || 
            player.getOffHandStack().getItem() == SIMPLE_SWORD)) {
            int x = 10;
            int y = 10;
            drawContext.drawTextWithShadow(
                net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal("§c无尽之剑效果激活§r"),
                x, y, 0xFFFFFF
            );
            drawContext.drawTextWithShadow(
                net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                Text.literal("§a无敌 §e飞行 §b缓降"),
                x, y + 12, 0xFFFFFF
            );
        }
    }
}
