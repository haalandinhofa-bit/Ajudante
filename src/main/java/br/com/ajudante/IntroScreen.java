package br.com.ajudante;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IntroScreen extends Screen {
    private static final Identifier LOGO = new Identifier(AjudanteMod.MOD_ID, "textures/gui/babiesfords-intro.png");
    private int ticksShown;

    public IntroScreen() {
        super(Text.literal("BabiesFords"));
    }

    @Override
    public void tick() {
        ticksShown++;
        if (ticksShown >= 80 && client != null) {
            client.setScreen(new TitleScreen());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int width = this.width;
        int height = this.height;
        context.fill(0, 0, width, height, 0xFF101820);
        int imageWidth = Math.min(width, height * 2);
        int imageHeight = Math.min(height, width / 2);
        imageWidth = Math.max(1, imageWidth);
        imageHeight = Math.max(1, imageHeight);
        context.drawTexture(LOGO, (width - imageWidth) / 2, (height - imageHeight) / 2,
                0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Carregando..."), width / 2, height - 24, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
