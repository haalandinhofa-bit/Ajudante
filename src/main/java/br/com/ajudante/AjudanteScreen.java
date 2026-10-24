package br.com.ajudante;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class AjudanteScreen extends Screen {
    private TextFieldWidget itemField;

    public AjudanteScreen() { super(Text.literal("O Ajudante")); }

    @Override
    protected void init() {
        int center = width / 2;
        itemField = new TextFieldWidget(textRenderer, center - 100, height / 2 - 15, 200, 20,
                Text.literal("ID do item"));
        itemField.setMaxLength(128);
        itemField.setPlaceholder(Text.literal("minecraft:diamond"));
        addDrawableChild(itemField);
        addDrawableChild(ButtonWidget.builder(Text.literal("Confirmar"), button -> submit())
                .dimensions(center - 50, height / 2 + 15, 100, 20).build());
        setInitialFocus(itemField);
    }

    private void submit() {
        String id = itemField.getText().trim();
        if (id.isEmpty()) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(id, 128);
        ClientPlayNetworking.send(AjudanteMod.SUBMIT_ITEM, buf);
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, "O Ajudante — qual item você deseja?", width / 2, height / 2 - 45, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Digite um ID, por exemplo minecraft:diamond", width / 2, height / 2 - 30, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() { return false; }
}
