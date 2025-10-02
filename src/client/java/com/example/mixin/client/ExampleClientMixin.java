package com.example.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.time.LocalTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ExampleClientMixin {
    @Inject(at = @At("TAIL"), method = "joinWorld")
    private void onJoinWorld(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String greeting = getRealTimeGreeting();
            client.player.sendMessage(Text.literal(greeting), false);
        }
    }

    private String getRealTimeGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour >= 5 && hour < 12) {
            return "上午好呀！今天过得怎么样？祝你游戏愉快！";
        } else if (hour >= 12 && hour < 18) {
            return "下午好呀！享受美好的游戏时光吧！";
        } else if (hour >= 18 && hour < 22) {
            return "晚上好呀！愿你在游戏中度过愉快的夜晚！";
        } else {
            return "夜深了，注意休息哦！游戏虽好，不要熬夜～";
        }
    }
}