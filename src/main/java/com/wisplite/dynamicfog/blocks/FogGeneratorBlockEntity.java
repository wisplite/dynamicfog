package com.wisplite.dynamicfog.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import team.lodestar.lodestone.systems.rendering.VFXBuilders.WorldVFXBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleTypes;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;

import java.awt.Color;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.wisplite.dynamicfog.DynamicFog;
import com.wisplite.dynamicfog.menus.FogGeneratorMenu;

public class FogGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    private int radiusX;
    private int radiusY;
    private int radiusZ;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private int fogDepth = 3;
    private int checksPerTick = 30;

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DynamicFog.MODID);

    public static final Supplier<BlockEntityType<FogGeneratorBlockEntity>> FOG_GENERATOR_BE =
        BLOCK_ENTITY_TYPES.register("fog_generator",
            () -> BlockEntityType.Builder.of(
                FogGeneratorBlockEntity::new,
                DynamicFog.FOG_GENERATOR_BLOCK.get()  // the BLOCK, not the entity type
            ).build(null)
        );

    public FogGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(FOG_GENERATOR_BE.get(), pos, state);  // BlockEntityType here
    }

    public void setRadii(int x, int y, int z) {
        this.radiusX = x;
        this.radiusY = y;
        this.radiusZ = z;
    }

    public void setOffsets(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    public int getOffsetX() {
        return this.offsetX;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public int getOffsetZ() {
        return this.offsetZ;
    }

    public int getRadiusX() {
        return this.radiusX;
    }

    public int getRadiusY() {
        return this.radiusY;
    }

    public int getRadiusZ() {
        return this.radiusZ;
    }

    public void setFogDepth(int fogDepth) {
        this.fogDepth = fogDepth;
    }

    public void setChecksPerTick(int checksPerTick) {
        this.checksPerTick = checksPerTick;
    }

    public int getFogDepth() {
        return this.fogDepth;
    }

    public int getChecksPerTick() {
        return this.checksPerTick;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Fog Generator");
    }

    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.radiusX);
        buffer.writeInt(this.radiusY);
        buffer.writeInt(this.radiusZ);
        buffer.writeInt(this.offsetX);
        buffer.writeInt(this.offsetY);
        buffer.writeInt(this.offsetZ);
        buffer.writeInt(this.fogDepth);
        buffer.writeInt(this.checksPerTick);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("radiusX", this.radiusX);
        tag.putInt("radiusY", this.radiusY);
        tag.putInt("radiusZ", this.radiusZ);
        tag.putInt("offsetX", this.offsetX);
        tag.putInt("offsetY", this.offsetY);
        tag.putInt("offsetZ", this.offsetZ);
        tag.putInt("fogDepth", this.fogDepth);
        tag.putInt("checksPerTick", this.checksPerTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.radiusX = tag.getInt("radiusX");
        this.radiusY = tag.getInt("radiusY");
        this.radiusZ = tag.getInt("radiusZ");
        this.offsetX = tag.getInt("offsetX");
        this.offsetY = tag.getInt("offsetY");
        this.offsetZ = tag.getInt("offsetZ");
        this.fogDepth = tag.contains("fogDepth") ? tag.getInt("fogDepth") : 3;
        this.checksPerTick = tag.contains("checksPerTick") ? tag.getInt("checksPerTick") : 30;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FogGeneratorMenu(id, inventory, this.getBlockPos(), ContainerLevelAccess.create(this.level, this.getBlockPos()), this.radiusX, this.radiusY, this.radiusZ, this.offsetX, this.offsetY, this.offsetZ, this.fogDepth, this.checksPerTick);
    }
    
    public static void clientTick(Level level, BlockPos pos, BlockState state, FogGeneratorBlockEntity entity) {
        if (entity.getRadiusX() <= 0 && entity.getRadiusY() <= 0 && entity.getRadiusZ() <= 0) {
            return;
        }

        int cx = pos.getX() + entity.getOffsetX();
        int cy = pos.getY() + entity.getOffsetY();
        int cz = pos.getZ() + entity.getOffsetZ();

        int minX = cx - entity.getRadiusX(); int maxX = cx + entity.getRadiusX();
        int minY = cy - entity.getRadiusY(); int maxY = cy + entity.getRadiusY();
        int minZ = cz - entity.getRadiusZ(); int maxZ = cz + entity.getRadiusZ();

        RandomSource random = level.getRandom();
        
        int fogDepth = entity.getFogDepth();
        int checksPerTick = entity.getChecksPerTick();

        for (int i = 0; i < checksPerTick; i++) {
            int x = 0, y = 0, z = 0;

            // Randomly pick one of the 6 outer boundary faces to check
            int face = random.nextInt(6);
            switch (face) {
                case 0: // -X Wall
                    x = Mth.nextInt(random, minX, Math.min(minX + fogDepth, maxX));
                    y = Mth.nextInt(random, minY, maxY);
                    z = Mth.nextInt(random, minZ, maxZ);
                    break;
                case 1: // +X Wall
                    x = Mth.nextInt(random, Math.max(maxX - fogDepth, minX), maxX);
                    y = Mth.nextInt(random, minY, maxY);
                    z = Mth.nextInt(random, minZ, maxZ);
                    break;
                case 2: // -Y Floor
                    x = Mth.nextInt(random, minX, maxX);
                    y = Mth.nextInt(random, minY, Math.min(minY + fogDepth, maxY));
                    z = Mth.nextInt(random, minZ, maxZ);
                    break;
                case 3: // +Y Ceiling
                    x = Mth.nextInt(random, minX, maxX);
                    y = Mth.nextInt(random, Math.max(maxY - fogDepth, minY), maxY);
                    z = Mth.nextInt(random, minZ, maxZ);
                    break;
                case 4: // -Z Wall
                    x = Mth.nextInt(random, minX, maxX);
                    y = Mth.nextInt(random, minY, maxY);
                    z = Mth.nextInt(random, minZ, Math.min(minZ + fogDepth, maxZ));
                    break;
                case 5: // +Z Wall
                    x = Mth.nextInt(random, minX, maxX);
                    y = Mth.nextInt(random, minY, maxY);
                    z = Mth.nextInt(random, Math.max(maxZ - fogDepth, minZ), maxZ);
                    break;
            }

            BlockPos targetPos = new BlockPos(x, y, z);
            BlockState targetState = level.getBlockState(targetPos);

            if (!targetState.isSolidRender(level, targetPos)) continue;

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = targetPos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);

                if (!neighborState.isSolidRender(level, neighborPos)) {
                    float maxScale = 1.5f;
                    double surfaceOffset = 0.5 + maxScale / 2.0 + 0.25;
                    
                    double px = targetPos.getX() + 0.5 + (dir.getStepX() * surfaceOffset);
                    double py = targetPos.getY() + 0.5 + (dir.getStepY() * surfaceOffset);
                    double pz = targetPos.getZ() + 0.5 + (dir.getStepZ() * surfaceOffset);

                    px += (random.nextDouble() - 0.5) * 0.4;
                    py += (random.nextDouble() - 0.5) * 0.4;
                    pz += (random.nextDouble() - 0.5) * 0.4;

                    Color fogColor = new Color(240, 240, 240);
                    WorldParticleBuilder.create(LodestoneParticleTypes.SMOKE_PARTICLE.get())
                        // SCALE: Start at 1.5 blocks wide, and smoothly shrink to 0.0 over its lifetime
                        .setScaleData(GenericParticleData.create(1.5f, 0.0f).build())
                        
                        // TRANSPARENCY: Start invisible (0.0), fade up to a dense 0.6 in the middle, fade back to 0.0 at the end
                        .setTransparencyData(GenericParticleData.create(0.0f, 0.1f, 0.0f).build())
                        
                        // COLOR: Stay the same color the whole time (you can make it transition from blue to green if you want!)
                        .setColorData(ColorParticleData.create(fogColor, fogColor).build())
                        
                        // Set a long lifetime so they overlap and form a dense wall (200 ticks = 10 seconds)
                        .setLifetime(200)

                        .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE.withDepthFade())
                        
                        // Give it a tiny bit of random drifting motion
                        .addMotion(
                            (random.nextDouble() - 0.5) * 0.025,
                            (random.nextDouble() - 0.5) * 0.03,
                            (random.nextDouble() - 0.5) * 0.025
                        )
                        
                        // Fire the particle into the world!
                        .spawn(level, px, py, pz);
                }
            }
        }
    }
}