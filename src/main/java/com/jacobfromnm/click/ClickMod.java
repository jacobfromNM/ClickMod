package com.jacobfromnm.click;

// Minecraft and Forge Imports...
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Main mod class.
 */
@Mod("click")
public class ClickMod {

        /**
         * Constructor for the mod. Registers event listeners and config.
         */
        public ClickMod() {
                IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
                SOUND_EVENTS.register(modEventBus);
                ENTITY_TYPES.register(modEventBus);

                // Register config...
                ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClickConfig.CONFIG);

                // Register client-side renderer...
                modEventBus.addListener(this::onClientSetup);

                // Register entity attributes...
                modEventBus.addListener(this::onAttributeCreate);

                // Register tick handlers...
                MinecraftForge.EVENT_BUS.register(ServerTickHandler.class);
                MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
        }

        // Mod ID constant
        public static final String MODID = "click";

        // Deferred registers for sounds...
        public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister
                        .create(ForgeRegistries.SOUND_EVENTS, MODID);

        // "Click" sound event registration
        public static final RegistryObject<SoundEvent> CLICK_001 = SOUND_EVENTS.register("click_001",
                        () -> new SoundEvent(new ResourceLocation(MODID, "click_001")));

        // "Ambience" sound event registration
        public static final RegistryObject<SoundEvent> AMBIANCE = SOUND_EVENTS.register("ambiance",
                        () -> new SoundEvent(new ResourceLocation(MODID, "ambiance")));

        // Deferred register for entity types, aka custom mobs
        public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister
                        .create(ForgeRegistries.ENTITY_TYPES, MODID);

        // CreepingEntity registration
        public static final RegistryObject<EntityType<CreepingEntity>> CREEPING_ENTITY = ENTITY_TYPES.register(
                        "creeping_entity",
                        () -> EntityType.Builder.<CreepingEntity>of(CreepingEntity::new, MobCategory.MONSTER)
                                        .sized(0.6F, 1.8F)
                                        .clientTrackingRange(10)
                                        .build(new ResourceLocation(MODID, "creeping_entity").toString()));

        // Client setup event handler for registering entity renderers
        private void onClientSetup(FMLClientSetupEvent event) {
                EntityRenderers.register(ClickMod.CREEPING_ENTITY.get(), CreepingEntityRenderer::new);

        }

        // Entity attribute creation event handler for custom entities
        private void onAttributeCreate(EntityAttributeCreationEvent event) {
                event.put(CREEPING_ENTITY.get(), CreepingEntity.createAttributes().build());
        }

}
