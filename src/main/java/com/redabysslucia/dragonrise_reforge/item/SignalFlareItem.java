package com.redabysslucia.dragonrise_reforge.item;

import com.atsuishio.superbwarfare.item.IDyeableSmokeItem;
import com.redabysslucia.dragonrise_reforge.entities.special.SignalFlareEntity;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 信号棒物品：右键蓄力（弓动作），松开掷出 SignalFlareEntity。
 * 烟雾颜色/时长由 NBT 控制：与染料合成染色（IDyeableSmokeItem 同款机制）。
 * NBT：Color(int RGB)、SmokeSeconds(float)。
 */
public class SignalFlareItem extends Item implements IDyeableSmokeItem {

    public static final String TAG_COLOR_LOCAL = "Color";
    public static final String TAG_SMOKE_SECONDS = "SmokeSeconds";

    public SignalFlareItem(Properties properties) {
        super(properties);
    }

    // ---- 蓄力投掷 ----

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (!(living instanceof Player player)) return;
        int usedTicks = this.getUseDuration(stack) - timeLeft;
        // 至少蓄力 5 tick 才掷出
        if (usedTicks < 5) return;

        if (!level.isClientSide()) {
            float power = Math.min(usedTicks / 20.0f, 1.5f);
            // 位置：玩家眼睛前方
            Vec3 look = player.getLookAngle();
            Vec3 pos = player.getEyePosition(1.0f).add(look.scale(0.3));
            SignalFlareEntity flare = new SignalFlareEntity(ModEntities.SIGNAL_FLARE.get(), level);
            flare.setPos(pos.x, pos.y - 0.1, pos.z);

            // 从物品 NBT 带属性
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                if (tag.contains(TAG_COLOR_LOCAL)) flare.setSmokeColor(tag.getInt(TAG_COLOR_LOCAL));
                if (tag.contains(TAG_SMOKE_SECONDS)) flare.setSmokeSeconds(tag.getFloat(TAG_SMOKE_SECONDS));
            }
            flare.launch(look, power);
            level.addFreshEntity(flare);
        }

        // 消耗一个
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    // ---- 染色（仿 SBW IDyeableSmokeItem）----

    @Override
    public void setColor(ItemStack stack, int color) {
        stack.getOrCreateTag().putInt(TAG_COLOR_LOCAL, color);
    }

    @Override
    public int getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_COLOR_LOCAL)) {
            return tag.getInt(TAG_COLOR_LOCAL);
        }
        return 0xFFFFFF; // 默认白烟（不染色）
    }

    // ---- 提示 ----

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        // 只显示（颜色）与（时间）两项，不附加通用说明
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_COLOR_LOCAL)) {
            tooltipComponents.add(Component.translatable("des.dragonrise_reforge.signal_flare.color")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (tag != null && tag.contains(TAG_SMOKE_SECONDS)) {
            tooltipComponents.add(Component.translatable("des.dragonrise_reforge.signal_flare.time",
                    String.format("%.0f", tag.getFloat(TAG_SMOKE_SECONDS))).withStyle(ChatFormatting.GRAY));
        }
    }
}
