package com.wisplite.dynamicfog.payloads;

import com.wisplite.dynamicfog.DynamicFog;
import com.wisplite.dynamicfog.blocks.FogGeneratorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FogGeneratorUpdatePayload(BlockPos pos, IntTriple radii, IntTriple offsets, int fogDepth, int checksPerTick, boolean renderDebugBounds) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FogGeneratorUpdatePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(DynamicFog.MODID, "update_fog_generator"));

    public static final StreamCodec<FriendlyByteBuf, FogGeneratorUpdatePayload> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, FogGeneratorUpdatePayload::pos,
        IntTriple.CODEC, FogGeneratorUpdatePayload::radii,
        IntTriple.CODEC, FogGeneratorUpdatePayload::offsets,
        ByteBufCodecs.INT, FogGeneratorUpdatePayload::fogDepth,
        ByteBufCodecs.INT, FogGeneratorUpdatePayload::checksPerTick,
        ByteBufCodecs.BOOL, FogGeneratorUpdatePayload::renderDebugBounds,
        FogGeneratorUpdatePayload::new
    );

    public FogGeneratorUpdatePayload(BlockPos pos, int radiusX, int radiusY, int radiusZ, int offsetX, int offsetY, int offsetZ, int fogDepth, int checksPerTick, boolean renderDebugBounds) {
        this(pos, new IntTriple(radiusX, radiusY, radiusZ), new IntTriple(offsetX, offsetY, offsetZ), fogDepth, checksPerTick, renderDebugBounds);
    }

    public int radiusX() {
        return this.radii.x();
    }

    public int radiusY() {
        return this.radii.y();
    }

    public int radiusZ() {
        return this.radii.z();
    }

    public int offsetX() {
        return this.offsets.x();
    }

    public int offsetY() {
        return this.offsets.y();
    }

    public int offsetZ() {
        return this.offsets.z();
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            if (level.isLoaded(this.pos) && level.getBlockEntity(this.pos) instanceof FogGeneratorBlockEntity blockEntity) {
                blockEntity.setRadii(this.radiusX(), this.radiusY(), this.radiusZ());
                blockEntity.setOffsets(this.offsetX(), this.offsetY(), this.offsetZ());
                blockEntity.setFogDepth(this.fogDepth());
                blockEntity.setChecksPerTick(this.checksPerTick());
                blockEntity.setRenderDebugBounds(this.renderDebugBounds());
                blockEntity.setChanged();
                level.sendBlockUpdated(this.pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);

                if (level instanceof ServerLevel serverLevel) {
                    ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(blockEntity);
                    for (ServerPlayer trackingPlayer : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(this.pos), false)) {
                        trackingPlayer.connection.send(packet);
                    }
                }
            }
        });
    }

    private record IntTriple(int x, int y, int z) {
        private static final StreamCodec<FriendlyByteBuf, IntTriple> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, IntTriple::x,
            ByteBufCodecs.INT, IntTriple::y,
            ByteBufCodecs.INT, IntTriple::z,
            IntTriple::new
        );
    }
}
