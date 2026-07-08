package net.thepir4te.fixmylaggg.client.mixin;

import net.thepir4te.fixmylaggg.manager.FakeFramerateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.Minecraft")
public class MinecraftClientMixin {

    @Shadow public static int fps;

    private static int ourFpsCounter = 0;
    private static int ourRealFps = 0;
    private static long ourFpsResetTime = 0;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onRunTick(boolean renderLevel, CallbackInfo ci) {
        ourFpsCounter++;
        long now = System.nanoTime();
        if (now - ourFpsResetTime > 1_000_000_000L) {
            ourRealFps = ourFpsCounter;
            ourFpsCounter = 0;
            ourFpsResetTime = now;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (ourRealFps > 0) {
            FakeFramerateManager.getInstance().calculateFakeFps(ourRealFps);
        }
        int fakeFps = FakeFramerateManager.getInstance().getCurrentFakeFps();
        if (fakeFps > 0) {
            fps = fakeFps;
        }
    }
}
