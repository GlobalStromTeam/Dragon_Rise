package com.redabysslucia.dragonrise_reforge.item;

import com.redabysslucia.dragonrise_reforge.entities.special.StarShellEntity;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 信号枪：右键沿视线方向发射一枚 StarShellEntity（照明弹）。
 * 弹药：消耗 SBW「飞行器干扰弹」（superbwarfare:flying_flare_ammo）才能发射，无数量上限。
 * 单发冷却 20 tick（1 秒）防连点刷屏。
 */
public class SignalGunItem extends Item {

    /** 击发冷却 tick */
    private static final int COOLDOWN_TICKS = 20;

    /** SBW 飞行器干扰弹物品 id */
    private static final ResourceLocation SBW_FLARE_AMMO = new ResourceLocation("superbwarfare", "flying_flare_ammo");

    public SignalGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                return InteractionResultHolder.fail(stack);
            }

            // 弹药检查：消耗 1 枚 SBW 飞行器干扰弹（创造模式免消耗）
            if (!player.getAbilities().instabuild) {
                int slot = findAmmoSlot(player);
                if (slot < 0) {
                    player.displayClientMessage(
                            Component.translatable("message.dragonrise_reforge.signal_gun.need_ammo"),
                            true);
                    return InteractionResultHolder.fail(stack);
                }
                player.getInventory().removeItem(slot, 1);
            }

            // 沿视线方向生成照明弹
            Vec3 look = player.getLookAngle();
            StarShellEntity shell = new StarShellEntity(ModEntities.STAR_SHELL.get(), level);
            Vec3 pos = player.getEyePosition(1.0f).add(look.scale(0.6));
            shell.setPos(pos.x, pos.y - 0.15, pos.z);
            shell.launch(look, 1.0f);
            level.addFreshEntity(shell);

            player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.2f, 0.9f);
        } else {
            // 客户端本地判定：没弹药立即提示（不发空枪音效）；服务端仍做权威判定
            if (!player.getAbilities().instabuild && findAmmoSlot(player) < 0) {
                player.displayClientMessage(
                        Component.translatable("message.dragonrise_reforge.signal_gun.need_ammo"),
                        true);
                return InteractionResultHolder.fail(stack);
            }
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.2f, 0.9f, false);
        }
        return InteractionResultHolder.consume(stack);
    }

    /** 在背包里找 1 枚 SBW 飞行器干扰弹的槽位，找不到返回 -1 */
    private static int findAmmoSlot(Player player) {
        Item ammo = ForgeRegistries.ITEMS.getValue(SBW_FLARE_AMMO);
        if (ammo == null) return -1;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(ammo)) {
                return i;
            }
        }
        return -1;
    }
}
