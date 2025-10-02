package com.example.mixin;

import com.example.ExampleMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
    
    @Inject(at = @At("HEAD"), method = "startServer")
    private void onServerStart(CallbackInfo ci) {
        ExampleMod.LOGGER.info("无尽物品模组服务器启动完成");
    }
}
