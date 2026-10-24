package br.com.ajudante;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class AjudanteMod implements ModInitializer {
    public static final String MOD_ID = "o_ajudante";
    public static final Identifier OPEN_SCREEN = new Identifier(MOD_ID, "open_screen");
    public static final Identifier SUBMIT_ITEM = new Identifier(MOD_ID, "submit_item");
    private static final Set<UUID> PENDING = new HashSet<>();
    private static int ticksUntilSpawn = randomInterval();

    private static int randomInterval() {
        return ThreadLocalRandom.current().nextInt(12000, 24001); // 10–20 minutos
    }

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (--ticksUntilSpawn <= 0) {
                spawnForRandomPlayer(server);
                ticksUntilSpawn = randomInterval();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SUBMIT_ITEM, (server, player, handler, buf, responseSender) -> {
            String itemId = buf.readString(128);
            server.execute(() -> resolveRequest(player, itemId));
        });
    }

    private static void spawnForRandomPlayer(MinecraftServer server) {
        var players = server.getPlayerManager().getPlayerList();
        if (players.isEmpty()) return;
        ServerPlayerEntity player = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        if (player.isSpectator() || PENDING.contains(player.getUuid())) return;

        World world = player.getWorld();
        Vec3d look = player.getRotationVec(1.0F);
        double x = player.getX() + look.x * 2.5;
        double z = player.getZ() + look.z * 2.5;
        double y = player.getY();
        VillagerEntity villager = net.minecraft.entity.EntityType.VILLAGER.create(world);
        if (villager == null) return;
        villager.refreshPositionAndAngles(x, y, z, player.getYaw() + 180, 0);
        villager.setCustomName(Text.literal("O Ajudante"));
        villager.setCustomNameVisible(true);
        villager.setAiDisabled(true);
        world.spawnEntity(villager);
        PENDING.add(player.getUuid());
        ServerPlayNetworking.send(player, OPEN_SCREEN, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.empty());
    }

    private static void resolveRequest(ServerPlayerEntity player, String itemId) {
        if (!PENDING.remove(player.getUuid())) return;
        Identifier id = Identifier.tryParse(itemId);
        var item = id == null ? null : Registries.ITEM.getOrEmpty(id).orElse(null);
        if (item == null || item == net.minecraft.item.Items.AIR) {
            player.sendMessage(Text.literal("O Ajudante: esse ID de item não existe."), false);
            removeNamedVillager(player);
            return;
        }

        // Decisão individual para cada pedido: 50% de chance de aceitar.
        boolean accepts = ThreadLocalRandom.current().nextBoolean();
        if (accepts) {
            ItemStack stack = new ItemStack(item);
            if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
            player.sendMessage(Text.literal("O Ajudante: hoje é seu dia!"), false);
            player.playSound(SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        } else {
            player.sendMessage(Text.literal("O Ajudante: você não deveria ter pedido isso..."), false);
            player.playSound(SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 1.0F, 0.65F);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
            // Punição agressiva, sem execução instantânea: dá tempo de reagir no multiplayer.
            player.damage(player.getDamageSources().magic(), 8.0F);
        }
        removeNamedVillager(player);
    }

    private static void removeNamedVillager(ServerPlayerEntity player) {
        for (Entity entity : player.getWorld().getEntitiesByClass(Entity.class,
                player.getBoundingBox().expand(12.0), e -> e instanceof VillagerEntity
                        && e.hasCustomName() && Text.literal("O Ajudante").equals(e.getCustomName()))) {
            entity.discard();
        }
    }
}
