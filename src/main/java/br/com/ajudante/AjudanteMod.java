package br.com.ajudante;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
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
    public static final Identifier TIMER_UPDATE = new Identifier(MOD_ID, "timer_update");

    private static final Set<UUID> PENDING = new HashSet<>();
    private static int ticksUntilSpawn = randomInterval();
    private static int totalIntervalTicks = ticksUntilSpawn;
    private static int syncTicks;
    private static UUID activeHelperId;
    private static UUID activePlayerId;

    private static int randomInterval() {
        return ThreadLocalRandom.current().nextInt(8 * 60 * 20, 15 * 60 * 20 + 1);
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                sendTimer(handler.player, activeHelperId == null ? ticksUntilSpawn : -1, totalIntervalTicks));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (activeHelperId == null) {
                if (ticksUntilSpawn > 0) ticksUntilSpawn--;
                if (ticksUntilSpawn <= 0 && spawnForRandomPlayer(server)) {
                    // Timer stays hidden while the Ajudante is present.
                    broadcastTimer(server, -1, totalIntervalTicks);
                }
            }

            if (++syncTicks >= 20) {
                syncTicks = 0;
                if (activeHelperId == null) broadcastTimer(server, ticksUntilSpawn, totalIntervalTicks);
                else broadcastTimer(server, -1, totalIntervalTicks);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SUBMIT_ITEM, (server, player, handler, buf, responseSender) -> {
            String itemId = buf.readString(128);
            server.execute(() -> resolveRequest(player, itemId));
        });
    }

    private static boolean spawnForRandomPlayer(MinecraftServer server) {
        var players = server.getPlayerManager().getPlayerList().stream()
                .filter(player -> !player.isSpectator() && !PENDING.contains(player.getUuid()))
                .toList();
        if (players.isEmpty()) return false;

        ServerPlayerEntity player = players.get(ThreadLocalRandom.current().nextInt(players.size()));
        World world = player.getWorld();
        Vec3d look = player.getRotationVec(1.0F);
        double x = player.getX() + look.x * 2.5;
        double z = player.getZ() + look.z * 2.5;
        double y = player.getY();

        VillagerEntity villager = net.minecraft.entity.EntityType.VILLAGER.create(world);
        if (villager == null) return false;
        villager.refreshPositionAndAngles(x, y, z, player.getYaw() + 180, 0);
        villager.setCustomName(Text.literal("O Ajudante"));
        villager.setCustomNameVisible(true);
        villager.setAiDisabled(true);
        if (!world.spawnEntity(villager)) return false;

        activeHelperId = villager.getUuid();
        activePlayerId = player.getUuid();
        PENDING.add(player.getUuid());
        ServerPlayNetworking.send(player, OPEN_SCREEN, PacketByteBufs.empty());
        return true;
    }

    private static void resolveRequest(ServerPlayerEntity player, String itemId) {
        if (!PENDING.remove(player.getUuid()) || activePlayerId == null
                || !activePlayerId.equals(player.getUuid())) return;

        Identifier id = Identifier.tryParse(itemId);
        var item = id == null ? null : Registries.ITEM.getOrEmpty(id).orElse(null);
        if (item == null || item == Items.AIR) {
            player.sendMessage(Text.literal("O Ajudante: esse ID de item não existe."), false);
            finishEncounter(player.getServer());
            return;
        }

        ItemStack requestedStack = new ItemStack(item);
        Rarity rarity = item.getRarity(requestedStack);
        int acceptChance = switch (rarity) {
            case COMMON -> 50;
            case UNCOMMON -> 35;
            case RARE -> 15;
            case EPIC -> 5;
        };
        boolean accepts = ThreadLocalRandom.current().nextInt(100) < acceptChance;

        if (accepts) {
            ItemStack stack = new ItemStack(item);
            if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
            player.sendMessage(Text.literal("O Ajudante: hoje é seu dia!"), false);
            player.playSound(SoundEvents.ENTITY_VILLAGER_YES, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        } else {
            player.sendMessage(Text.literal("O Ajudante recusou! A raridade do item diminuiu suas chances..."), false);
            player.playSound(SoundEvents.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 1.0F, 0.65F);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
            player.damage(player.getDamageSources().magic(), 8.0F);
        }
        finishEncounter(player.getServer());
    }

    private static void finishEncounter(MinecraftServer server) {
        if (server != null && activeHelperId != null) {
            for (var world : server.getWorlds()) {
                Entity entity = world.getEntity(activeHelperId);
                if (entity != null) {
                    entity.discard();
                    break;
                }
            }
        }
        if (activePlayerId != null) PENDING.remove(activePlayerId);
        activeHelperId = null;
        activePlayerId = null;
        totalIntervalTicks = randomInterval();
        ticksUntilSpawn = totalIntervalTicks;
        if (server != null) broadcastTimer(server, ticksUntilSpawn, totalIntervalTicks);
    }

    private static void sendTimer(ServerPlayerEntity player, int remainingTicks, int totalTicks) {
        var buf = PacketByteBufs.create();
        buf.writeInt(remainingTicks < 0 ? -1 : Math.max(0, (remainingTicks + 19) / 20));
        buf.writeInt(Math.max(1, (totalTicks + 19) / 20));
        ServerPlayNetworking.send(player, TIMER_UPDATE, buf);
    }

    private static void broadcastTimer(MinecraftServer server, int remainingTicks, int totalTicks) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendTimer(player, remainingTicks, totalTicks);
        }
    }
}
