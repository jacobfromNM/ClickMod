package com.jacobfromnm.click;

// Minecraft Imports...
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

// Java Imports...
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Server-side tick handler for managing CreepingEntity spawning logic.
 * 
 * @author Jacobfromnm
 */
@Mod.EventBusSubscriber
public class ServerTickHandler {
    private static final Map<UUID, Integer> playerClickCounters = new HashMap<>();
    private static int tickCounter = 0;

    /**
     * Server tick event handler to manage CreepingEntity spawning logic.
     * 
     * @param event The level tick event.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (event.level.isClientSide)
            return;

        // Limit checks to the value in config (default ~10 seconds)
        tickCounter++;
        if (tickCounter < ClickConfig.SPAWN_TICK_INTERVAL.get())
            return; // Not yet...
        tickCounter = 0;

        ServerLevel serverLevel = (ServerLevel) event.level;

        for (Player player : serverLevel.players()) {

            // Only trigger if conditions are met
            if (shouldSpawnEntity(player)) {

                int count = playerClickCounters.getOrDefault(player.getUUID(), 0) + 1;
                playerClickCounters.put(player.getUUID(), count);

                if (ClickConfig.ENABLE_LOGGING.get()) {
                    System.out.println("[Click Mod] Click counter for " + player.getName().getString() + ": " + count);
                }

                // Play click sound each time conditions are met
                serverLevel.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(ClickMod.MODID, "click_001")),
                        SoundSource.HOSTILE,
                        1.0F,
                        0.8F + serverLevel.random.nextFloat() * 0.4F);

                // If we've reached the configured number of clicks, spawn the entity
                if (count >= ClickConfig.CLICKS_BEFORE_SPAWN.get()) {
                    if (ClickConfig.ENABLE_LOGGING.get())
                        System.out.println("[Click Mod] Conditions met, attempting to spawn Entity...");

                    // Spawn CreepingEntity...
                    CreepingEntity entity = ClickMod.CREEPING_ENTITY.get().create(serverLevel);
                    if (entity != null) {
                        Vec3 spawnPos = findValidSpawnPos(serverLevel, player.position(), 5, 10, entity);

                        if (spawnPos != null) {
                            entity.setPos(spawnPos);
                            serverLevel.addFreshEntity(entity);
                            playerClickCounters.put(player.getUUID(), 0); // reset counter
                            player.displayClientMessage(Component.literal("§4The demon king Paimon approaches..."),
                                    true);

                            if (ClickConfig.ENABLE_LOGGING.get())
                                System.out.println("[Click Mod] Spawned Entity at " + spawnPos);
                        } else {
                            if (ClickConfig.ENABLE_LOGGING.get())
                                System.out.println("[Click Mod] Failed to find valid spawn position.");
                        }
                    }
                }
            } else {
                if (ClickConfig.ENABLE_LOGGING.get())
                    System.out.println("[Click Mod] Did not roll for spawn, no entity spawned.");
            }
        }
    }

    /**
     * Find a valid spawn position for the entity around the player.
     * 
     * @param level     The server level.
     * @param playerPos The player's position.
     * @param minDist   The minimum distance from the player.
     * @param maxDist   The maximum distance from the player.
     * @param entity    The entity to spawn.
     * @return A valid spawn position as a Vec3, or a fallback position if none
     *         found.
     */
    private static Vec3 findValidSpawnPos(ServerLevel level, Vec3 playerPos, double minDist, double maxDist,
            Entity entity) {
        for (int i = 0; i < 50; i++) { // up to 50 tries for a good spot
            double distance = minDist + Math.random() * (maxDist - minDist);
            double angle = Math.random() * Math.PI * 2;
            double dx = Math.cos(angle) * distance;
            double dz = Math.sin(angle) * distance;

            // Base candidate position near player (same Y as player)
            BlockPos pos = new BlockPos(playerPos.x + dx, playerPos.y, playerPos.z + dz);

            // Check if player is underground by looking at sky light...
            boolean isUnderground = level.getBrightness(LightLayer.SKY,
                    new BlockPos(playerPos.x, playerPos.y, playerPos.z)) <= 2;

            if (!isUnderground) {
                // On surface: adjust to terrain ground height
                pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
            }
            // else: leave Y as-is, so we stay at cave depth...

            // Validate the position...
            if (level.isEmptyBlock(pos) &&
                    level.noCollision(entity.getType().getAABB(pos.getX(), pos.getY(), pos.getZ()))) {

                if (ClickConfig.ENABLE_LOGGING.get())
                    System.out.println("[Click Mod] Found a safe spawn position at " + pos);

                return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            }
        }

        // Fallback: right behind player if no safe position was found
        if (ClickConfig.ENABLE_LOGGING.get())
            System.out.println("[Click Mod] Unable to find a safe spawn position, spawning directly behind player");

        return playerPos.add(0, 0, -1); // 1 block behind (relative to world Z)
    }

    /**
     * Determine if the CreepingEntity should spawn based on player conditions.
     * 
     * @param player The player to check conditions for.
     * @return True if the entity should spawn, false otherwise.
     */
    public static boolean shouldSpawnEntity(Player player) {
        Level level = player.level;
        Random random = new Random();

        // Only run server-side bro!
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        // Grab the player position and the time of day...
        BlockPos pos = player.blockPosition();
        long dayTime = level.getDayTime() % 24000;
        boolean isNightTime = dayTime > 13000 && dayTime < 23000;
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        boolean isUnderground = skyLight <= 2; // occluded from sun (e.g., caves, indoors)

        if (ClickConfig.ENABLE_LOGGING.get())
            System.out.println("[Click Mod] Time: " + dayTime +
                    " | Night: " + isNightTime +
                    " | Sky Light: " + skyLight +
                    " | Underground: " + isUnderground);

        // If it's night OR you're underground OR the 0.01% chance triggers, go for it.
        double chance = ClickConfig.SPAWN_CHANCE.get();
        return (((isNightTime || isUnderground) && random.nextFloat() < chance) || (random.nextFloat() < 0.001));
    }
}
