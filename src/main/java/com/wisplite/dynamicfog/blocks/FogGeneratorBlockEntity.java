package com.wisplite.dynamicfog.blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.wisplite.dynamicfog.DynamicFog;
import com.wisplite.dynamicfog.menus.FogGeneratorMenu;

public class FogGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public record CachedSurface(BlockPos blockPos, Direction direction) {}

    private int radiusX;
    private int radiusY;
    private int radiusZ;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private int fogDepth = 3;
    private int checksPerTick = 30;
    private boolean renderDebugBounds = false;
    private final List<CachedSurface> surfaceCache = new ArrayList<>();
    private boolean surfaceCacheDirty = true;

    public void recalculateSurfaceCache() {
        this.surfaceCache.clear();
        this.surfaceCacheDirty = false;
        if (this.level == null || (this.radiusX <= 0 && this.radiusY <= 0 && this.radiusZ <= 0)) {
            return;
        }

        int cx = this.worldPosition.getX() + this.offsetX;
        int cy = this.worldPosition.getY() + this.offsetY;
        int cz = this.worldPosition.getZ() + this.offsetZ;

        int minX = cx - this.radiusX;
        int maxX = cx + this.radiusX;
        int minY = cy - this.radiusY;
        int maxY = cy + this.radiusY;
        int minZ = cz - this.radiusZ;
        int maxZ = cz + this.radiusZ;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!isInPerimeterShell(x, y, z, minX, maxX, minY, maxY, minZ, maxZ, this.fogDepth)) {
                        continue;
                    }

                    BlockPos targetPos = new BlockPos(x, y, z);
                    BlockState targetState = this.level.getBlockState(targetPos);
                    if (!targetState.isSolidRender(this.level, targetPos)) {
                        continue;
                    }

                    for (Direction direction : Direction.values()) {
                        BlockPos neighborPos = targetPos.relative(direction);
                        BlockState neighborState = this.level.getBlockState(neighborPos);
                        if (!neighborState.isSolidRender(this.level, neighborPos)) {
                            this.surfaceCache.add(new CachedSurface(targetPos, direction));
                        }
                    }
                }
            }
        }
    }

    private static boolean isInPerimeterShell(int x, int y, int z, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, int fogDepth) {
        return x - minX <= fogDepth || maxX - x <= fogDepth
            || y - minY <= fogDepth || maxY - y <= fogDepth
            || z - minZ <= fogDepth || maxZ - z <= fogDepth;
    }

    public boolean isSurfaceCacheDirty() {
        return this.surfaceCacheDirty;
    }

    public List<CachedSurface> getSurfaceCache() {
        return this.surfaceCache;
    }

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
        this.surfaceCacheDirty = true;
    }

    public void setOffsets(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.surfaceCacheDirty = true;
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
        this.surfaceCacheDirty = true;
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

    public boolean getRenderDebugBounds() {
        return this.renderDebugBounds;
    }

    public void setRenderDebugBounds(boolean renderDebugBounds) {
        this.renderDebugBounds = renderDebugBounds;
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
        buffer.writeBoolean(this.renderDebugBounds);
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
        tag.putBoolean("renderDebugBounds", this.renderDebugBounds);
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
        this.renderDebugBounds = tag.getBoolean("renderDebugBounds");
        this.surfaceCacheDirty = true;
        if (this.level != null && this.level.isClientSide) {
            this.recalculateSurfaceCache();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && this.level.isClientSide) {
            this.recalculateSurfaceCache();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FogGeneratorMenu(id, inventory, this.getBlockPos(), ContainerLevelAccess.create(this.level, this.getBlockPos()), this.radiusX, this.radiusY, this.radiusZ, this.offsetX, this.offsetY, this.offsetZ, this.fogDepth, this.checksPerTick, this.renderDebugBounds);
    }
}