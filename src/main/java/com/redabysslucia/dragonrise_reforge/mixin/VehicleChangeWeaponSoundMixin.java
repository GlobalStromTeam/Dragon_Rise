package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.util.RemotePilotHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 武器切换音效播给遥控操作者。
 * <p>
 * SBW 的 {@code VehicleEntity.changeWeapon} 在切换成功后会播一声
 * {@code SoundInfo.Change}：
 * <pre>this.level().playSound(null, this, sound, this.soundSource, 1f, 1f)</pre>
 * 这是播在**载具位置**的 —— 遥控驾驶时操作者往往离无人机几百格，
 * 于是"武器换了、但听不到切换声"（SBW 自己的遥控无人机也有同样毛病）。
 * <p>
 * 这里把这一声改判：若载具<b>离操作者较远</b>，就播在操作者的位置；
 * 离得近（≤16 格）时保持原样，避免机身与操作者各响一声。
 * 音效本身仍取 SBW 解析出来的 {@code SoundInfo.Change}，调用也仍发生在原处。
 * <p>
 * <b>remap</b>：方法选择器 {@code changeWeapon} 是 SBW 自有方法（类级 remap = false），
 * @At 目标 {@code Level.playSound(...)} 是原版方法（单独 remap = true）。
 * handler 内不直接调原版成员（改走 {@link RemotePilotHelper}），否则打包后会 NoSuchMethodError。
 * 接收者类型必须与字节码持有者一致（这里是 {@code Level}）。
 */
@Mixin(value = VehicleEntity.class, remap = false)
public class VehicleChangeWeaponSoundMixin {

    @Redirect(method = "changeWeapon", require = 0,
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void dragonrise$changeWeaponSound(Level level, Player except, Entity entity,
                                              SoundEvent sound, SoundSource source,
                                              float volume, float pitch) {
        // 这条重定向只在 SBW 确实拿到 SoundInfo.Change（非 null）并调用 playSound 时才执行，
        // 所以日志有无可以直接区分"武器数据没配切换音效"和"播了但离机身太远听不到"。
        com.redabysslucia.dragonrise_reforge.Dragonrise_reforge.LOGGER.info(
                "[DR-SWITCH] 切换音效 {} @ {}", sound.getLocation(), entity.getStringUUID());
        RemotePilotHelper.playSoundForController(level, entity, except, entity, sound, source, volume, pitch);
    }
}
