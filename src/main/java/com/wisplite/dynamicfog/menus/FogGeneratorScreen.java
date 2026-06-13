package com.wisplite.dynamicfog.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.wisplite.dynamicfog.DynamicFog;
import com.wisplite.dynamicfog.payloads.FogGeneratorUpdatePayload;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;

public class FogGeneratorScreen extends AbstractContainerScreen<FogGeneratorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/demo_background.png");
    
    private EditBox radiusX;
    private EditBox radiusY;
    private EditBox radiusZ;
    private EditBox offsetX;
    private EditBox offsetY;
    private EditBox offsetZ;
    private EditBox fogDepth;
    private EditBox checksPerTick;
    private Checkbox renderDebugBounds;
    private int xCenter;
    private int yCenter;
    
    public FogGeneratorScreen(FogGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.imageWidth = 248;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();

        this.xCenter = this.leftPos + this.imageWidth / 2;
        this.yCenter = this.topPos + this.imageHeight / 2;

        int shift = -30; // Shift all vertical positions up by 30px

        this.offsetX = new EditBox(this.font, this.xCenter - 55, this.yCenter - 30 + shift, 30, 15, Component.literal("X"));
        this.offsetY = new EditBox(this.font, this.xCenter - 15, this.yCenter - 30 + shift, 30, 15, Component.literal("Y"));
        this.offsetZ = new EditBox(this.font, this.xCenter + 25, this.yCenter - 30 + shift, 30, 15, Component.literal("Z"));

        this.offsetX.setValue(String.valueOf(this.getMenu().getOffsetX()));
        this.offsetY.setValue(String.valueOf(this.getMenu().getOffsetY()));
        this.offsetZ.setValue(String.valueOf(this.getMenu().getOffsetZ()));

        this.addRenderableWidget(this.offsetX);
        this.addRenderableWidget(this.offsetY);
        this.addRenderableWidget(this.offsetZ);

        this.radiusX = new EditBox(this.font, this.xCenter - 55, this.yCenter + 10 + shift, 30, 15, Component.literal("X"));
        this.radiusY = new EditBox(this.font, this.xCenter - 15, this.yCenter + 10 + shift, 30, 15, Component.literal("Y"));
        this.radiusZ = new EditBox(this.font, this.xCenter + 25, this.yCenter + 10 + shift, 30, 15, Component.literal("Z"));

        this.radiusX.setValue(String.valueOf(this.getMenu().getRadiusX()));
        this.radiusY.setValue(String.valueOf(this.getMenu().getRadiusY()));
        this.radiusZ.setValue(String.valueOf(this.getMenu().getRadiusZ()));

        this.addRenderableWidget(this.radiusX);
        this.addRenderableWidget(this.radiusY);
        this.addRenderableWidget(this.radiusZ);

        this.fogDepth = new EditBox(this.font, this.xCenter - 55, this.yCenter + 50 + shift, 30, 15, Component.literal("Depth"));
        this.checksPerTick = new EditBox(this.font, this.xCenter + 5, this.yCenter + 50 + shift, 30, 15, Component.literal("Checks"));

        this.fogDepth.setValue(String.valueOf(this.getMenu().getFogDepth()));
        this.checksPerTick.setValue(String.valueOf(this.getMenu().getChecksPerTick()));

        this.addRenderableWidget(this.fogDepth);
        this.addRenderableWidget(this.checksPerTick);

        this.renderDebugBounds = Checkbox.builder(Component.literal("Show debug bounds"), this.font)
            .pos(this.xCenter - 55, this.yCenter + 70 + shift)
            .selected(this.getMenu().getRenderDebugBounds())
            .build();
        this.addRenderableWidget(this.renderDebugBounds);

        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
            try {
                int radiusX = Integer.parseInt(this.radiusX.getValue());
                int radiusY = Integer.parseInt(this.radiusY.getValue());
                int radiusZ = Integer.parseInt(this.radiusZ.getValue());
                int offsetX = Integer.parseInt(this.offsetX.getValue());
                int offsetY = Integer.parseInt(this.offsetY.getValue());
                int offsetZ = Integer.parseInt(this.offsetZ.getValue());
                int fogDepth = Integer.parseInt(this.fogDepth.getValue());
                int checksPerTick = Integer.parseInt(this.checksPerTick.getValue());

                net.minecraft.core.BlockPos pos = this.getMenu().getBlockPos();
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new FogGeneratorUpdatePayload(pos, radiusX, radiusY, radiusZ, offsetX, offsetY, offsetZ, fogDepth, checksPerTick, this.renderDebugBounds.selected())
                );

            } catch (NumberFormatException e) {
                e.printStackTrace();
                this.minecraft.player.sendSystemMessage(Component.literal("Invalid input"));
            }

            this.minecraft.player.closeContainer();
        }).bounds(this.xCenter + 25, this.yCenter + 90 + shift, 30, 15).build());
    }
    
    @Override
    public void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        int shift = -30;
        graphics.drawString(this.font, Component.literal("Radius:"), this.xCenter - 55, this.yCenter - 0 + shift, 0x404040, false);
        graphics.drawString(this.font, Component.literal("Offset:"), this.xCenter - 55, this.yCenter - 40 + shift, 0x404040, false);
        graphics.drawString(this.font, Component.literal("Fog Depth:"), this.xCenter - 55, this.yCenter + 40 + shift, 0x404040, false);
        graphics.drawString(this.font, Component.literal("Checks/Tick:"), this.xCenter + 5, this.yCenter + 40 + shift, 0x404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
    }
}
