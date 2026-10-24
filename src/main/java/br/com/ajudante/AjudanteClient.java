package br.com.ajudante;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class AjudanteClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(AjudanteMod.OPEN_SCREEN, (client, handler, buf, responseSender) ->
                client.execute(() -> MinecraftClient.getInstance().setScreen(new AjudanteScreen())));
    }
}
