package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.item.misc.DetonatorItem;
import com.redabysslucia.dragonrise_reforge.entities.IedEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 官方起爆器与我们的土制遥控炸弹"互不相通"：官方 {@code DetonatorItem.getC4} 会返回
 * 该玩家拥有的全部 {@code C4Entity}，而 {@link IedEntity} 是它的子类、必然被扫到 ——
 * 这里在返回前把 {@link IedEntity} 摘掉，官方起爆器就只引爆官方遥控 C4。
 * （反向已经是隔离的：男孩牌手机只扫 IedEntity。）
 */
@Mixin(value = DetonatorItem.class, remap = false)
public class DetonatorItemMixin {

    @Inject(method = "getC4", at = @At("RETURN"), cancellable = true, remap = false)
    private static void dragonrise$excludeIed(Player player, Level level, CallbackInfoReturnable<List<Entity>> cir) {
        List<Entity> list = cir.getReturnValue();
        if (list != null && list.removeIf(e -> e instanceof IedEntity)) {
            cir.setReturnValue(list);
        }
    }
}
