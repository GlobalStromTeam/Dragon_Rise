package com.rhythm.dragon_vehicle_deployer.network;

import com.rhythm.dragon_vehicle_deployer.block.entity.VehicleDeployerBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DeployerSettingsPacket {
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

    public static void encode(DeployerSettingsPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.spawnIntervalSeconds);
        buf.writeBoolean(msg.autoSpawnEnabled);
        buf.writeInt(msg.idleClearTimeoutSeconds);
    }

    public static DeployerSettingsPacket decode(FriendlyByteBuf buf) {
        return new DeployerSettingsPacket(
                buf.readBlockPos(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readInt()
        );
    }

    public static void handle(DeployerSettingsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!player.hasPermissions(2)) return;

            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 64.0) return;

            var level = player.level();
            if (!(level.getBlockEntity(msg.pos) instanceof VehicleDeployerBlockEntity blockEntity)) return;

            blockEntity.spawnIntervalSeconds = Mth.clamp(msg.spawnIntervalSeconds, 5, 3600);
            blockEntity.autoSpawnEnabled = msg.autoSpawnEnabled;
            blockEntity.idleClearTimeoutSeconds = Mth.clamp(msg.idleClearTimeoutSeconds, 0, 36000);
            blockEntity.setChanged();

            player.displayClientMessage(
                    Component.translatable("gui.dragonrise_reforge.save_success").withStyle(ChatFormatting.GREEN), true);
        });
        ctx.get().setPacketHandled(true);
    }
}
