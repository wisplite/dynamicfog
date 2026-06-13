package com.wisplite.dynamicfog.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;

public class FogGeneratorRenderer implements BlockEntityRenderer<FogGeneratorBlockEntity> {
    public FogGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        // Constructor required by the engine
    }

    @Override
    public AABB getRenderBoundingBox(FogGeneratorBlockEntity entity) {
        int offsetX = entity.getOffsetX();
        int offsetY = entity.getOffsetY();
        int offsetZ = entity.getOffsetZ();
        int radiusX = entity.getRadiusX();
        int radiusY = entity.getRadiusY();
        int radiusZ = entity.getRadiusZ();

        return new AABB(offsetX - radiusX, offsetY - radiusY, offsetZ - radiusZ, offsetX + radiusX + 1, offsetY + radiusY + 1, offsetZ + radiusZ + 1);
    }

    @Override
    public void render(FogGeneratorBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Only draw if the player enabled the checkbox in the GUI
        if (!entity.getRenderDebugBounds()) return;

        poseStack.pushPose();

        int offsetX = entity.getOffsetX();
        int offsetY = entity.getOffsetY();
        int offsetZ = entity.getOffsetZ();
        int radiusX = entity.getRadiusX();
        int radiusY = entity.getRadiusY();
        int radiusZ = entity.getRadiusZ();

        // Match FogGeneratorClientTicker: center at block pos + offset, extend ±radius per axis
        AABB box = new AABB(
            offsetX - radiusX, offsetY - radiusY, offsetZ - radiusZ,
            offsetX + radiusX + 1, offsetY + radiusY + 1, offsetZ + radiusZ + 1
        );

        // Grab a vertex buffer meant for drawing solid lines
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());

        // Draw the wireframe outline. 
        // 255, 0, 0, 255 represents Red, Green, Blue, Alpha (Solid Red)
        LevelRenderer.renderLineBox(poseStack, buffer, box, 1.0F, 0.0F, 0.0F, 1.0F);

        poseStack.popPose();
    }
}
