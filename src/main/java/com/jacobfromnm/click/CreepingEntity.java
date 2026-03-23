package com.jacobfromnm.click;

// Java Imports...
import javax.annotation.Nullable;

// Minecraft imports...
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundTag;

/**
 * Yo! CreepingEntity is a custom hostile mob that seeks out and follows
 * players for a brief amount of time.
 * 
 * It has configurable behaviors such as causing damage, playing sounds, and
 * despawning after a set time.
 * 
 * It extends PathfinderMob to leverage Minecraft's built-in pathfinding and AI
 * systems.
 */
public class CreepingEntity extends PathfinderMob {
    private int lifeTicks = ClickConfig.ENTITY_LIFETIME_TICKS.get(); // 5 seconds (Default)
    private Player targetPlayer;
    private boolean playedClickSound = false;
    private boolean producedSmokeOnSpawn = false;
    private int skinIndex = -1;

    /**
     * Constructor for CreepingEntity.
     * 
     * @param type  The entity type.
     * @param level The level in which the entity exists.
     */
    public CreepingEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoAi(false); // Keep AI enabled so it can pathfind
        this.noPhysics = false; // Let it walk normally
    }

    /**
     * Creates and returns the attribute supplier for CreepingEntity.
     * 
     * @return The attribute supplier builder with configured attributes.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 8.0) // Will be overridden on spawn based on
                                                    // config...

                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MAX_HEALTH, 20.0D);
    }

    @Override
    public void onAddedToWorld() {
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ClickConfig.ATTACK_DAMAGE.get());
    }

    /**
     * Registers the AI goals for the CreepingEntity.
     */
    @Override
    protected void registerGoals() {
        if (ClickConfig.ENTITY_CAUSES_DAMAGE.get()) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        }

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /**
     * Called every tick to update the entity's state.
     * Handles movement toward the player, sound effects, and despawning.
     */
    @Override
    public void tick() {
        super.tick();

        // Find and move toward nearest player
        if (targetPlayer == null || targetPlayer.isRemoved()) {
            targetPlayer = this.level.getNearestPlayer(this, 512);
        }

        if (targetPlayer != null) {
            this.getNavigation().moveTo(targetPlayer, 1.0D); // Walk speed
            if (this.tickCount % 50 == 0 && ClickConfig.ENABLE_LOGGING.get()) {
                System.out.println("[Click Mod] Moving toward the Player. Current position: " + this.position());
            }
        }

        // Play ambient sound once when spawned
        if (!playedClickSound) {
            playedClickSound = true;
            if (ClickConfig.PLAY_AMBIENT_SOUND.get()) {
                this.level.playSound(
                        null,
                        this.getX(), this.getY(), this.getZ(),
                        ForgeRegistries.SOUND_EVENTS
                                .getValue(new ResourceLocation(ClickMod.MODID, "ambiance")),
                        SoundSource.HOSTILE,
                        1.0F,
                        1.0F);
            }
        }

        // Produce smoke particles once on spawn
        if (!producedSmokeOnSpawn) {
            producedSmokeOnSpawn = true;
            this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0,
                    0.0);
        }

        // De-spawn after lifetime expires (set in config)
        lifeTicks--;
        if (lifeTicks <= 0) {
            if (ClickConfig.ENABLE_LOGGING.get())
                System.out.println("[Click Mod] Life timer expired, despawning entity");
            // Display smoke particles on despawn...
            this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0,
                    0.0);
            this.discard(); // Remove from world
        }
    }

    /**
     * Handles attacking the target entity.
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        if (!ClickConfig.ENTITY_CAUSES_DAMAGE.get()) {
            return false;
        }

        if (ClickConfig.ENABLE_LOGGING.get()) {
            System.out.println("[Click Mod] Attacked the player. Damage amount: "
                    + this.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
        }
        return super.doHurtTarget(target);
    }

    /**
     * Suppresses the hurt sound when the entity takes damage.
     */
    @Override
    protected void playHurtSound(DamageSource source) {
        // Suppress hurt sound
    }

    /**
     * Prevents the entity from being pushed by other entities.
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * Prevents the entity from being pushed by other entities.
     */
    @Override
    protected void doPush(Entity entityIn) {
        // Do nothing
    }

    /**
     * Makes the entity invulnerable to all damage sources.
     */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true; // Can't be damaged
    }

    /**
     * Prevents the entity from being collided with.
     */
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    /**
     * Prevents the fire animation from being displayed.
     */
    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    /**
     * Returns the skin index for the entity.
     * This is randomly assigned once per entity instance.
     * The logic for this lives in the entity renderer.
     * 
     * @return The skin index (0, 1, or 2).
     */
    public int getSkinIndex() {
        if (skinIndex == -1) {
            skinIndex = this.getRandom().nextInt(3); // Lock it once per entity.
        }

        return skinIndex;
    }

    /**
     * Called when the entity is spawned into the world.
     * Used here to dynamically set attributes based on config values.
     */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        // Apply the config value dynamically at spawn time:
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(ClickConfig.ATTACK_DAMAGE.get());
        }

        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

}
