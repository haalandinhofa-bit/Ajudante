package br.com.ajudante;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class JumpscareScreen extends Screen {
    private static final Identifier IMAGE = new Identifier(AjudanteMod.MOD_ID, "textures/gui/virus-jumpscare.jpg");
    private int ticksShown;

    public JumpscareScreen() {
        super(Text.literal("O Ajudante"));
    }

    @Override
    public void tick() {
        ticksShown++;
        if (ticksShown >= 40 && client != null) {
            client.setScreen(null);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int width = this.width;
        int height = this.height;
        context.fill(0, 0, width, height, 0xFF000000);
        context.drawTexture(IMAGE, 0, 0, 0.0F, 0.0F, width, height, width, height);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("VOCÊ NÃO DEVERIA TER PEDIDO ISSO"), width / 2, height - 28, 0xFFFF3333);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        // The jumpscare stays on screen briefly.
    }
}
