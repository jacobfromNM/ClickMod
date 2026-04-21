package com.jacobfromnm.click;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClickConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Click Configuration"))
                .setSavingRunnable(ClickConfig::save);

        ConfigEntryBuilder eb = builder.entryBuilder();

        ConfigCategory stats = builder.getOrCreateCategory(Component.literal("Entity Stats"));
        stats.addEntry(eb.startBooleanToggle(Component.literal("Entity Causes Damage"), ClickConfig.ENTITY_CAUSES_DAMAGE.get())
                .setDefaultValue(true)
                .setSaveConsumer(v -> ClickConfig.ENTITY_CAUSES_DAMAGE.set(v))
                .build());
        stats.addEntry(eb.startDoubleField(Component.literal("Attack Damage"), ClickConfig.ATTACK_DAMAGE.get())
                .setDefaultValue(8.0)
                .setMin(1.0).setMax(20.0)
                .setSaveConsumer(v -> ClickConfig.ATTACK_DAMAGE.set(v))
                .build());
        stats.addEntry(eb.startBooleanToggle(Component.literal("Flicker"), ClickConfig.FLICKER.get())
                .setDefaultValue(true)
                .setSaveConsumer(v -> ClickConfig.FLICKER.set(v))
                .build());

        ConfigCategory spawn = builder.getOrCreateCategory(Component.literal("Spawn Settings"));
        spawn.addEntry(eb.startDoubleField(Component.literal("Spawn Chance"), ClickConfig.SPAWN_CHANCE.get())
                .setDefaultValue(0.025)
                .setMin(0.0).setMax(1.0)
                .setSaveConsumer(v -> ClickConfig.SPAWN_CHANCE.set(v))
                .build());
        spawn.addEntry(eb.startIntField(Component.literal("Spawn Tick Interval"), ClickConfig.SPAWN_TICK_INTERVAL.get())
                .setDefaultValue(200)
                .setMin(1).setMax(12000)
                .setSaveConsumer(v -> ClickConfig.SPAWN_TICK_INTERVAL.set(v))
                .build());
        spawn.addEntry(eb.startIntField(Component.literal("Entity Lifetime (ticks)"), ClickConfig.ENTITY_LIFETIME_TICKS.get())
                .setDefaultValue(200)
                .setMin(20).setMax(800)
                .setSaveConsumer(v -> ClickConfig.ENTITY_LIFETIME_TICKS.set(v))
                .build());
        spawn.addEntry(eb.startIntField(Component.literal("Clicks Before Spawn"), ClickConfig.CLICKS_BEFORE_SPAWN.get())
                .setDefaultValue(3)
                .setMin(1).setMax(20)
                .setSaveConsumer(v -> ClickConfig.CLICKS_BEFORE_SPAWN.set(v))
                .build());

        ConfigCategory sounds = builder.getOrCreateCategory(Component.literal("Sound Settings"));
        sounds.addEntry(eb.startBooleanToggle(Component.literal("Play Ambient Sound"), ClickConfig.PLAY_AMBIENT_SOUND.get())
                .setDefaultValue(true)
                .setSaveConsumer(v -> ClickConfig.PLAY_AMBIENT_SOUND.set(v))
                .build());

        ConfigCategory logging = builder.getOrCreateCategory(Component.literal("Logging"));
        logging.addEntry(eb.startBooleanToggle(Component.literal("Enable Logging"), ClickConfig.ENABLE_LOGGING.get())
                .setDefaultValue(false)
                .setSaveConsumer(v -> ClickConfig.ENABLE_LOGGING.set(v))
                .build());

        return builder.build();
    }
}
