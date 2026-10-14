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
 *
 * <p><b>凡是会被 {@code PacketDistributor.sendToServer/sendToPlayer} 发出的 payload
 * 都必须在这里登记方向</b>，否则 NeoForge 会在发送时抛
 * {@code UnsupportedOperationException: Payload ... may not be sent to the server!}
 * 并导致客户端崩溃。
 *
 * <p>说明：
 * <ul>
 *   <li>{@code RemoteSeatSwitchMessage} / {@code RemoteSeatSyncMessage}：1.20.1 新增的遥控座位消息。</li>
 *   <li>{@code SetFireControlMessage} / {@code ToggleTakeoverMessage}：随 1.20.1 的间接瞄准射击系统
 *       整体移除（提交「新版本提交准备2」），不再注册。</li>
 *   <li>{@code EngineChangeModeMessage}：{@code ClickEvent} 在按下「切换发动机模式」键时发出；
 *       合并时一度漏注册，导致按键即崩，现补上。</li>
 *   <li>{@code DeployerSettingsPacket} 属于内嵌的 dragon_vehicle_deployer，
 *       由它自己的 {@code ModNetwork} 注册。</li>
 * </ul>
 */
public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // 客户端 -> 服务端
        registrar.playToServer(EngineChangeModeMessage.TYPE, EngineChangeModeMessage.STREAM_CODEC, EngineChangeModeMessage::handle);
        registrar.playToServer(R6DroneControlMessage.TYPE, R6DroneControlMessage.STREAM_CODEC, R6DroneControlMessage::handle);
        registrar.playToServer(RemoteSeatSwitchMessage.TYPE, RemoteSeatSwitchMessage.STREAM_CODEC, RemoteSeatSwitchMessage::handle);

        // 服务端 -> 客户端
        registrar.playToClient(OwnBombMessage.TYPE, OwnBombMessage.STREAM_CODEC, OwnBombMessage::handle);
        registrar.playToClient(RemoteSeatSyncMessage.TYPE, RemoteSeatSyncMessage.STREAM_CODEC, RemoteSeatSyncMessage::handle);
    }
}
