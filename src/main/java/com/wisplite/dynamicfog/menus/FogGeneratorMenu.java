package com.wisplite.dynamicfog.menus;

import com.wisplite.dynamicfog.DynamicFog;
import com.wisplite.dynamicfog.blocks.FogGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public class FogGeneratorMenu extends AbstractContainerMenu {
    private final BlockPos blockPos;
    private final ContainerLevelAccess access;
    private final int radiusX;
    private final int radiusY;
    private final int radiusZ;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;
    private final int fogDepth;
    private final int checksPerTick;

    public FogGeneratorMenu(int id, Inventory inventory, BlockPos blockPos, ContainerLevelAccess access, int radiusX, int radiusY, int radiusZ, int offsetX, int offsetY, int offsetZ, int fogDepth, int checksPerTick) {
        super(DynamicFog.FOG_GENERATOR_MENU.get(), id);
        this.blockPos = blockPos;
        this.access = access;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.fogDepth = fogDepth;
        this.checksPerTick = checksPerTick;
    }

    public FogGeneratorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        super(DynamicFog.FOG_GENERATOR_MENU.get(), id);
        this.radiusX = extraData.readInt();
        this.radiusY = extraData.readInt();
        this.radiusZ = extraData.readInt();
        this.offsetX = extraData.readInt();
        this.offsetY = extraData.readInt();
        this.offsetZ = extraData.readInt();
        this.fogDepth = extraData.readInt();
        this.checksPerTick = extraData.readInt();
        this.blockPos = extraData.readBlockPos();
        this.access = ContainerLevelAccess.NULL;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
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

    public int getOffsetX() {
        return this.offsetX;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public int getOffsetZ() {
        return this.offsetZ;
    }

    public int getFogDepth() {
        return this.fogDepth;
    }

    public int getChecksPerTick() {
        return this.checksPerTick;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, DynamicFog.FOG_GENERATOR_BLOCK.get());
    }
    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    public FogGeneratorBlockEntity getBlockEntity() {
        return this.access.evaluate((level, pos) -> level.getBlockEntity(pos) instanceof FogGeneratorBlockEntity blockEntity ? blockEntity : null, null);
    }
}
