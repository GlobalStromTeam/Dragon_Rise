package com.redabysslucia.dragonrise_reforge.items;

import com.atsuishio.superbwarfare.init.ModSounds;
import com.redabysslucia.dragonrise_reforge.entities.IedEntity;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.redabysslucia.dragonrise_reforge.client.model.item.IedItemModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * 土制遥控炸弹：手感与官方遥控 C4 一致（视线方向抛出、20 tick 冷却、消耗 1 个），
 * 生成的 IedEntity 为遥控型（放置期间静音），只能被男孩牌手机引爆。
 * 手持/背包显示 GeckoLib 3D 模型（geo/ied.geo.json）。
 */
public class IedItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public IedItem() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            IedEntity entity = new IedEntity(ModEntities.IED.get(), level);
            entity.setPos(player.getX() + 0.25 * player.getLookAngle().x,
                    player.getEyeY() - 0.2f + 0.25 * player.getLookAngle().y,
                    player.getZ() + 0.25 * player.getLookAngle().z);
            entity.setDeltaMovement(0.5 * player.getLookAngle().x,
                    0.5 * player.getLookAngle().y,
                    0.5 * player.getLookAngle().z);
            entity.setOwnerUUID(player.getUUID());
            level.addFreshEntity(entity);
        }

        if (player instanceof ServerPlayer) {
            player.level().playSound(null, player.blockPosition(), ModSounds.C4_THROW.get(), SoundSource.PLAYERS, 1f, 1f);
        }

        player.getCooldowns().addCooldown(this, 20);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<IedItem> itemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new GeoItemRenderer<>(new IedItemModel());
                }
                return this.itemRenderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
