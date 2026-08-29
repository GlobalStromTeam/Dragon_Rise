package com.redabysslucia.dragonrise_reforge.events;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.projectile.GuidedBombEntity;
import com.redabysslucia.dragonrise_reforge.network.ModNetwork;
import com.redabysslucia.dragonrise_reforge.network.message.OwnBombMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

/**
 * 通用（服务端侧）事件：制导炸弹在服务端生成时，把炸弹实体 UUID 通知给发射者玩家。
 * 客户端炸弹实体的 owner(entityData) 因 superb BVR 同步机制不会到达客户端，
 * 客户端无法用 getOwner() 判断归属，必须由服务端直接告知。
 * （注册在 FORGE 总线且不限 Dist，纯服务器与单机集成服务器均生效）
 */
@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvent {
    private static final Logger LOGGER = Dragonrise_reforge.LOGGER;

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof GuidedBombEntity bomb)) return;

        if (bomb.getOwner() instanceof ServerPlayer sp) {
            LOGGER.info("[BombHud] server: notify player {} bomb {}", sp.getGameProfile().getName(), bomb.getUUID());
            ModNetwork.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> sp), new OwnBombMessage(bomb.getUUID()));
        }
    }
}
