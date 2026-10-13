package com.redabysslucia.dragonrise_reforge.network;

import com.redabysslucia.dragonrise_reforge.network.message.OwnBombMessage;
import com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage;
import com.redabysslucia.dragonrise_reforge.network.message.RemoteSeatSwitchMessage;
import com.redabysslucia.dragonrise_reforge.network.message.RemoteSeatSyncMessage;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 1.21.1 使用 payload 体系替代 Forge 的 SimpleChannel。
 * 所有消息都注册到同一个 PROTOCOL_VERSION 下。
 *
 * <p>合并 1.20.1 时这里取 NeoForge 的 payload 写法（Forge 的
 * {@code SimpleChannel#registerMessage} 在 1.21.1 已不存在）。
 * 消息集合以本分支实际存在的类为准：
 * <ul>
 *   <li>{@code RemoteSeatSwitchMessage} / {@code RemoteSeatSyncMessage}：1.20.1 新增的遥控座位消息，本分支已有。</li>
 *   <li>{@code SetFireControlMessage} / {@code ToggleTakeoverMessage}：随 1.20.1 的间接瞄准射击系统整体移除
 *       （提交「新版本提交准备2」），不再注册。</li>
 * </ul>
 */
public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(R6DroneControlMessage.TYPE, R6DroneControlMessage.STREAM_CODEC, R6DroneControlMessage::handle);
        registrar.playToServer(RemoteSeatSwitchMessage.TYPE, RemoteSeatSwitchMessage.STREAM_CODEC, RemoteSeatSwitchMessage::handle);
        registrar.playToClient(OwnBombMessage.TYPE, OwnBombMessage.STREAM_CODEC, OwnBombMessage::handle);
        registrar.playToClient(RemoteSeatSyncMessage.TYPE, RemoteSeatSyncMessage.STREAM_CODEC, RemoteSeatSyncMessage::handle);
    }
}
