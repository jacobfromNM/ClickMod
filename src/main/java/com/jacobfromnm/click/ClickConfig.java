package com.jacobfromnm.click;

// Forge imports...
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuration class for the Click mod.
 * This class defines various configuration options that can be adjusted by the
 * user.
 */
public class ClickConfig {
        public static final ForgeConfigSpec CONFIG;
        public static final ForgeConfigSpec.BooleanValue ENABLE_LOGGING;
        public static final ForgeConfigSpec.DoubleValue SPAWN_CHANCE;
        public static final ForgeConfigSpec.BooleanValue ENTITY_CAUSES_DAMAGE;
        public static final ForgeConfigSpec.DoubleValue ATTACK_DAMAGE;
        public static final ForgeConfigSpec.IntValue SPAWN_TICK_INTERVAL;
        public static final ForgeConfigSpec.IntValue ENTITY_LIFETIME_TICKS;
        public static final ForgeConfigSpec.IntValue CLICKS_BEFORE_SPAWN;
        public static final ForgeConfigSpec.BooleanValue PLAY_AMBIENT_SOUND;
        public static final ForgeConfigSpec.BooleanValue FLICKER;

        static {
                ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

                builder.push("click_settings");

                builder.push("Logging");
                ENABLE_LOGGING = builder
                                .comment("Enable logging to console for debugging purposes. Default: false")
                                .define("enableLogging", false);
                builder.pop();

                builder.push("Entity Settings");
                SPAWN_CHANCE = builder
                                .comment("Chance (0.0 to 1.0) for a click sound to play when lighting conditions are met (Night time, in a cave, thick tree coverage, etc.). Default: 0.025")
                                .defineInRange("spawnChance", 0.025, 0.0, 1.0);
                builder.comment(""); // <-- adds a blank line

                ENTITY_CAUSES_DAMAGE = builder
                                .comment("Whether the entity can damage the player. Default: true")
                                .define("entityCausesDamage", true);
                builder.comment(""); // <-- adds a blank line

                ATTACK_DAMAGE = builder
                                .comment("Amount of damage the entity deals when attacking. Default: 8.0")
                                .defineInRange("attackDamage", 8.0, 1.0, 20.0);
                builder.comment(""); // <-- adds a blank line

                SPAWN_TICK_INTERVAL = builder
                                .comment("Number of ticks between spawn roll checks. 20 ticks = 1 second. Default: 200 (10 seconds)")
                                .defineInRange("spawnTickInterval", 200, 1, 12000);
                builder.comment(""); // <-- adds a blank line
                ENTITY_LIFETIME_TICKS = builder
                                .comment("Number of ticks before the entity despawns. 20 ticks = 1 second. Default: 200 (10 seconds)")
                                .defineInRange("entityLifetimeTicks", 200, 20, 800);
                builder.comment(""); // <-- adds a blank line
                CLICKS_BEFORE_SPAWN = builder
                                .comment("Number of times the click sound occurs before an entity spawns. Default: 3")
                                .defineInRange("clicksBeforeSpawn", 3, 1, 20);
                builder.comment(""); // <-- adds a blank line
                PLAY_AMBIENT_SOUND = builder
                                .comment("Whether to play the ambient sound when the entity spawns. Default: true")
                                .define("playAmbientSound", true);
                builder.comment(""); // <-- adds a blank line
                FLICKER = builder
                                .comment("Whether the entity flickers sporadically when spawned. Default: true")
                                .define("flicker", true);
                builder.pop(); // Entity Settings
                builder.pop();

                CONFIG = builder.build();
        }
}
