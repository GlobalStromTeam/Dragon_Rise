package com.redabysslucia.dragonrise_reforge.network;

import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import net.minecraft.network.FriendlyByteBuf;

public class EngineChangeModeMessage {


//    public VTOLModeMessage(EngineType engineType) {
//        this.engineType = engineType;
//    }

    public static void encode(EngineChangeModeMessage message, FriendlyByteBuf buffer) {
        //buffer.writeEnum(message.engineType);
    }

    public static EngineChangeModeMessage decode(FriendlyByteBuf buffer) {
        return new EngineChangeModeMessage();
    }

    public static void handler(EngineChangeModeMessage message, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            var player = ctx.get().getSender();
            if (player == null) return;
            var vehicle = player.getVehicle();
            if (vehicle instanceof VariableEngineVehicle vtol) {
                // Toggle VTOL mode logic here
                // For example, switch between "VTOL" and "FIXED" modes
                // This is a placeholder; actual implementation depends on VTOLEntity methods
                // String currentMode = vtol.getFlyMode();
                // if (currentMode.equals("VTOL")) {
                //     vtol.setFlyMode("FIXED");
                // } else {
                //     vtol.setFlyMode("VTOL");
                // }
                vtol.toggleChangeMode();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
