package br.com.ajudante;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class AjudanteClient implements ClientModInitializer {
    private static int secondsRemaining = -1;
    private static int totalSeconds = 1;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(AjudanteMod.OPEN_SCREEN, (client, handler, buf, responseSender) ->
                client.execute(() -> MinecraftClient.getInstance().setScreen(new AjudanteScreen())));

        ClientPlayNetworking.registerGlobalReceiver(AjudanteMod.TIMER_UPDATE, (client, handler, buf, responseSender) -> {
            int remaining = buf.readInt();
            int total = buf.readInt();
            client.execute(() -> {
                secondsRemaining = remaining;
                totalSeconds = Math.max(1, total);
            });
        });

        HudRenderCallback.EVENT.register((DrawContext context, float tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || secondsRemaining < 0) return;

            int barWidth = 180;
            int barHeight = 8;
            int x = (context.getScaledWindowWidth() - barWidth) / 2;
            int y = 8;
            float progress = Math.max(0.0F, Math.min(1.0F, (float) secondsRemaining / totalSeconds));
            int filledWidth = Math.round(barWidth * progress);

            context.fill(x - 6, y - 5, x + barWidth + 6, y + 30, 0x99000000);
            context.drawCenteredTextWithShadow(client.textRenderer,
                    Text.literal("O Ajudante aparece em " + formatTime(secondsRemaining)),
                    context.getScaledWindowWidth() / 2, y, 0xFFFFFF);
            context.fill(x, y + 14, x + barWidth, y + 14 + barHeight, 0xFF444444);
            if (filledWidth > 0) context.fill(x, y + 14, x + filledWidth, y + 14 + barHeight, 0xFF55D66B);
        });
    }

    private static String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
