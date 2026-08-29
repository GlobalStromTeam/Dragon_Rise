package com.rhythm.dragon_vehicle_deployer.network;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import com.rhythm.dragon_vehicle_deployer.block.entity.VehicleDeployerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.NetworkEvent;

public class DeployerSettingsPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DeployerSettingsPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(DragonVehicleDeployer.MODID, "deployer_settings"));

    public static final StreamCodec<FriendlyByteBuf, DeployerSettingsPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos);
                buf.writeInt(packet.spawnIntervalSeconds);
                buf.writeBoolean(packet.autoSpawnEnabled);
                buf.writeInt(packet.idleClearTimeoutSeconds);
            },
            buf -> new DeployerSettingsPacket(
                    buf.readBlockPos(),
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readInt()
            )
    );

    private final BlockPos pos;
    private final int spawnIntervalSeconds;
    private final boolean autoSpawnEnabled;
    private final int idleClearTimeoutSeconds;

    public DeployerSettingsPacket(BlockPos pos, int spawnIntervalSeconds, boolean autoSpawnEnabled, int idleClearTimeoutSeconds) {
        this.pos = pos;
        this.spawnIntervalSeconds = spawnIntervalSeconds;
        this.autoSpawnEnabled = autoSpawnEnabled;
        this.idleClearTimeoutSeconds = idleClearTimeoutSeconds;
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(DeployerSettingsPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;

            if (!serverPlayer.hasPermissions(2)) return;

            if (serverPlayer.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;

            var level = serverPlayer.level();
            if (!(level.getBlockEntity(msg.pos) instanceof VehicleDeployerBlockEntity blockEntity)) return;

            blockEntity.spawnIntervalSeconds = Mth.clamp(msg.spawnIntervalSeconds, 5, 3600);
            blockEntity.autoSpawnEnabled = msg.autoSpawnEnabled;
            blockEntity.idleClearTimeoutSeconds = Mth.clamp(msg.idleClearTimeoutSeconds, 0, 36000);
            blockEntity.setChanged();

            serverPlayer.displayClientMessage(
                    Component.translatable("gui.dragonrise_reforge.save_success").withStyle(ChatFormatting.GREEN), true);
        });
    }
}
