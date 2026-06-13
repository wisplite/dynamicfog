package com.wisplite.dynamicfog.client;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.wisplite.dynamicfog.blocks.FogGeneratorBlockEntity;
import com.wisplite.dynamicfog.blocks.FogGeneratorBlockEntity.CachedSurface;

import java.awt.Color;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import com.wisplite.dynamicfog.particles.DynamicFogParticleTypes;

import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;

public class FogGeneratorClientTicker {
    public static void clientTick(Level level, BlockPos pos, BlockState state, FogGeneratorBlockEntity entity) {
        if (entity.getRadiusX() <= 0 && entity.getRadiusY() <= 0 && entity.getRadiusZ() <= 0) {
            return;
        }

        if (entity.isSurfaceCacheDirty()) {
            entity.recalculateSurfaceCache();
        }

        List<CachedSurface> surfaceCache = entity.getSurfaceCache();
        if (surfaceCache.isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();
        int checksPerTick = entity.getChecksPerTick();

        for (int i = 0; i < checksPerTick; i++) {
            CachedSurface surface = surfaceCache.get(random.nextInt(surfaceCache.size()));
            BlockPos targetPos = surface.blockPos();
            Direction dir = surface.direction();

            float maxScale = 1.5f;
            double surfaceOffset = 0.5 + maxScale / 2.0 + 0.25;

            double px = targetPos.getX() + 0.5 + (dir.getStepX() * surfaceOffset);
            double py = targetPos.getY() + 0.5 + (dir.getStepY() * surfaceOffset);
            double pz = targetPos.getZ() + 0.5 + (dir.getStepZ() * surfaceOffset);

            px += (random.nextDouble() - 0.5) * 0.4;
            py += (random.nextDouble() - 0.5) * 0.4;
            pz += (random.nextDouble() - 0.5) * 0.4;

            float randomXSpeed = (random.nextFloat() - 0.5f) * 0.025f;
            float randomYSpeed = (random.nextFloat() - 0.5f) * 0.025f;
            float randomZSpeed = (random.nextFloat() - 0.5f) * 0.025f;

            float randomScaleBase = 1.2f + (random.nextFloat() * 0.5f);

            Color blockerColor = new Color(255, 255, 255);

            WorldParticleBuilder.create(DynamicFogParticleTypes.FOG.get())
                .setScaleData(GenericParticleData.create(randomScaleBase * 0.8f, 0.0f).build())
                .setTransparencyData(GenericParticleData.create(0.0f, 0.4f, 0.0f).build())
                .setColorData(ColorParticleData.create(blockerColor, blockerColor).build())
                .setLifetime(200)
                .setRenderType(LodestoneWorldParticleRenderType.TRANSPARENT.withDepthFade())
                .enableForcedSpawn()
                .addMotion(randomXSpeed, randomYSpeed, randomZSpeed)
                .spawn(level, px, py, pz);
        }
    }
}
