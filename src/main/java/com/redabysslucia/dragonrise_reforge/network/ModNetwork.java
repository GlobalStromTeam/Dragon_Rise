package com.redabysslucia.dragonrise_reforge.network;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("removal")
public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(new ResourceLocation(Dragonrise_reforge.MODID, Dragonrise_reforge.MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static int messageID = 0;

    public static void register() {
        playToServer(EngineChangeModeMessage.class, EngineChangeModeMessage::encode, EngineChangeModeMessage::decode, EngineChangeModeMessage::handler);
        playToServer(com.redabysslucia.dragonrise_reforge.network.message.SetFireControlMessage.class, com.redabysslucia.dragonrise_reforge.network.message.SetFireControlMessage::encode, com.redabysslucia.dragonrise_reforge.network.message.SetFireControlMessage::decode, com.redabysslucia.dragonrise_reforge.network.message.SetFireControlMessage::handle);
        playToServer(com.redabysslucia.dragonrise_reforge.network.message.ToggleTakeoverMessage.class, com.redabysslucia.dragonrise_reforge.network.message.ToggleTakeoverMessage::encode, com.redabysslucia.dragonrise_reforge.network.message.ToggleTakeoverMessage::decode, com.redabysslucia.dragonrise_reforge.network.message.ToggleTakeoverMessage::handle);
        playToServer(com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage.class, com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage::encode, com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage::decode, com.redabysslucia.dragonrise_reforge.network.message.R6DroneControlMessage::handle);
        playToClient(com.redabysslucia.dragonrise_reforge.network.message.OwnBombMessage.class, com.redabysslucia.dragonrise_reforge.network.message.OwnBombMessage::encode, com.redabysslucia.dragonrise_reforge.network.message.OwnBombMessage::decode, com.redabysslucia.dragonrise_reforge.network.message.OwnBombMessage::handle);
    }

    public static <T> void playToClient(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        messageID++;
    }

    /**
     * 注册无参数、向客户端发送的消息
     */
    @SuppressWarnings("unchecked")
    public static <T> void playToClient(T instance, Consumer<Supplier<NetworkEvent.Context>> messageConsumer) {
        var type = (Class<T>) instance.getClass();
        PACKET_HANDLER.registerMessage(messageID, type, (msg, buf) -> {
        }, (buf) -> instance, (msg, ctx) -> messageConsumer.accept(ctx), Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        messageID++;
    }

    public static <T> void playToServer(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        messageID++;
    }

    /**
     * 注册无参数、向服务器发送的消息
     */
    @SuppressWarnings("unchecked")
    public static <T> void playToServer(T instance, Consumer<Supplier<NetworkEvent.Context>> messageConsumer) {
        var type = (Class<T>) instance.getClass();
        PACKET_HANDLER.registerMessage(messageID, type, (msg, buf) -> {
        }, (buf) -> instance, (msg, ctx) -> messageConsumer.accept(ctx), Optional.of(NetworkDirection.PLAY_TO_SERVER));
        messageID++;
    }
}
