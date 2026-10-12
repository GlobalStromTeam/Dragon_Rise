package com.redabysslucia.dragonrise_reforge.items;

import com.redabysslucia.dragonrise_reforge.entities.IedEntity;
import com.redabysslucia.dragonrise_reforge.init.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.redabysslucia.dragonrise_reforge.client.model.item.PhoneItemModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

import java.util.List;

/**
 * 男孩牌手机：官方起爆器的换皮，但引爆流程不同 ——
 * 右键后：① 玩家处播放 ied.ogg；② 该玩家布置的每个 IED 处也播放同一段铃声；
 * ③ 给每个 IED 装 202 tick 引信，铃声播完由实体自行引爆并补爆炸音效。
 * 只扫我们自己的 IedEntity（官方起爆器那边也已用 mixin 隔离）。
 * 手持/背包显示 GeckoLib 3D 模型（geo/phone.geo.json）。
 */
public class PhoneItem extends Item implements GeoItem {

    /** ied.ogg 实测 10.12 秒 = 202 tick */
    public static final int RING_TICKS = 202;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PhoneItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), ModSounds.IED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

            List<IedEntity> bombs = level.getEntitiesOfClass(IedEntity.class,
                    player.getBoundingBox().inflate(256.0), e -> e.getOwner() == player);
            for (IedEntity bomb : bombs) {
                level.playSound(null, bomb.blockPosition(), ModSounds.IED.get(), SoundSource.BLOCKS, 2.0F, 1.0F);
                bomb.armFuse(RING_TICKS);
            }
        }

        player.getCooldowns().addCooldown(this, RING_TICKS);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GeoItemRenderer<PhoneItem> itemRenderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.itemRenderer == null) {
                    this.itemRenderer = new GeoItemRenderer<>(new PhoneItemModel());
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
