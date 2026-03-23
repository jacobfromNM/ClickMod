package com.jacobfromnm.click;

// Minecraft Imports...
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;

/**
 * Renderer for the CreepingEntity, handling texture selection and rendering
 * conditions.
 * 
 * @author jacobfromnm
 */
public class CreepingEntityRenderer extends HumanoidMobRenderer<CreepingEntity, HumanoidModel<CreepingEntity>> {
    private static final ResourceLocation ALEX_TEXTURE = new ResourceLocation("click", "textures/entity/alex.png");
    private static final ResourceLocation STEVE_TEXTURE = new ResourceLocation("click", "textures/entity/steve.png");

    /**
     * Constructor for CreepingEntityRenderer.
     * 
     * @param context The entity renderer provider context.
     */
    public CreepingEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }

    /**
     * Render the CreepingEntity if conditions are met (e.g., light level).
     */
    @Override
    public void render(
            CreepingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
            int light) {
        if (shouldRenderEntity(entity)) {
            super.render(entity, yaw, partialTicks, poseStack, buffer, light);
        }
    }

    /**
     * Get the texture location for the CreepingEntity based on its skin index.
     */
    @Override
    public ResourceLocation getTextureLocation(CreepingEntity entity) {
        if (!(entity instanceof CreepingEntity)) {
            if (ClickConfig.ENABLE_LOGGING.get()) {
                System.out.println("[Click Mod] Unexpected entity type in renderer: " + entity.getClass());
            }
            return STEVE_TEXTURE; // Fallback
        }

        int skinIndex = entity.getSkinIndex();

        return switch (skinIndex) {
            case 0 -> STEVE_TEXTURE;
            case 1 -> ALEX_TEXTURE;
            case 2 -> getNearestPlayerSkin(entity);
            default -> STEVE_TEXTURE;
        };
    }

    /**
     * Get the nearest player's skin texture location.
     * 
     * @param entity The CreepingEntity instance.
     * @return The ResourceLocation of the nearest player's skin.
     */
    private ResourceLocation getNearestPlayerSkin(CreepingEntity entity) {
        Player nearestPlayer = entity.level.getNearestPlayer(entity, 64.0);
        if (nearestPlayer instanceof AbstractClientPlayer acp) {
            return acp.getSkinTextureLocation();
        } else {
            Player clientPlayer = Minecraft.getInstance().player;
            if (clientPlayer instanceof AbstractClientPlayer acp) {
                return acp.getSkinTextureLocation();
            }
        }
        return STEVE_TEXTURE;
    }

    /**
     * Override to prevent name tag rendering for CreepingEntity.
     */
    @Override
    protected boolean shouldShowName(CreepingEntity entity) {
        return false;
    }

    /**
     * Determine if the CreepingEntity should be rendered based on light levels and
     * time of day.
     * 
     * @param entity The CreepingEntity instance.
     * @return True if the entity should be rendered, false otherwise.
     */
    private boolean shouldRenderEntity(CreepingEntity entity) {
        if (entity == null || entity.level == null)
            return false;

        int blockLight = entity.level.getBrightness(LightLayer.BLOCK, entity.blockPosition());
        int skyLight = entity.level.getBrightness(LightLayer.SKY, entity.blockPosition());

        long dayTime = entity.level.getDayTime() % 24000;
        boolean isNightTime = dayTime > 13000 && dayTime < 23000;

        int effectiveLight = blockLight;
        if (!isNightTime) {
            effectiveLight = Math.max(blockLight, skyLight);
        }

        // Erratically render the entity based on chance (and the FLICKER config
        // setting), otherwise render it based on the light level...
        if (ClickConfig.FLICKER.get() && Math.random() < 0.7) { // 70% chance per tick to not render regardless of light
                                                                // level
            return false;
        }
        return effectiveLight >= 7;
    }

}
