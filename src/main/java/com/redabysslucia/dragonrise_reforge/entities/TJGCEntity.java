package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.vehicle.subdata.EngineType;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.init.ModDamageTypes;
import com.atsuishio.superbwarfare.init.ModSounds;
import com.atsuishio.superbwarfare.network.NetworkRegistry;
import com.atsuishio.superbwarfare.network.message.receive.ClientIndicatorMessage;
import com.atsuishio.superbwarfare.tools.DamageHandler;
import com.atsuishio.superbwarfare.tools.SeekTool;
import com.redabysslucia.dragonrise_reforge.entities.utils.DragonriseVehicleBase;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.VariableEngineVehicle;
import lombok.val;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;

import static com.atsuishio.superbwarfare.event.ClientEventHandler.zoomVehicle;
import static com.atsuishio.superbwarfare.tools.ParticleTool.sendParticle;

public class TJGCEntity extends VariableEngineVehicle {

    /**
     * 展开导弹发射架的武器键（与 tjgc.json 的 Weapons / 座位武器表键名一致）。
     * 选中这些武器时发射架展开（animation.tjgc.missile_on），其余（Laser / Bomb）收拢。
     */
    private static final Set<String> MISSILE_RACK_WEAPONS = Set.of(
            "Rocket", "BigRocket", "AAMissile", "ATMissile", "BigATMissile");

    public TJGCEntity(EntityType<TJGCEntity> type, Level world) {
        super(type, world);
        this.setEngineTypeList(List.of(EngineType.AIRCRAFT, EngineType.HELICOPTER));
    }

    // ---------------- 动画状态机：起落架 / 发射架 ----------------
    // 基类 DragonriseVehicleBase 负责驱动状态机（升空收、落地放；切换武器展开/收拢发射架），
    // 这里只提供 tjgc 动画文件里的命名与展开条件。

    /** 起落架收起 = animation.tjgc.bay_off */
    @Override
    protected String gearRetractAnimation(String prefix) {
        return prefix + ".bay_off";
    }

    /** 起落架放下 = animation.tjgc.bay_on */
    @Override
    protected String gearExtendAnimation(String prefix) {
        return prefix + ".bay_on";
    }

    /** 当前是否有座位选中了需要发射架的武器 */
    @Override
    protected boolean isMissileRackDeployed() {
        for (var passenger : getPassengers()) {
            int seatIndex = getSeatIndex(passenger);
            if (seatIndex < 0) continue;

            String weaponName = getGunName(seatIndex);
            if (weaponName != null && MISSILE_RACK_WEAPONS.contains(weaponName)) {
                return true;
            }
        }
        return false;
    }


    public void hitBlock(Vec3 pos, GunData gunData, Entity shooter) {
        if (level() instanceof ServerLevel serverLevel) {
            if (gunData.compute().getExplosionRadius() > 0) {
                findNearEntity(pos, gunData, shooter);
                sendParticle(serverLevel, ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 24, 0, 0, 0, 0.2, true);
                sendParticle(serverLevel, ParticleTypes.LAVA, pos.x, pos.y, pos.z, 8, 0, 0, 0, 0.4, true);
            } else {
                sendParticle(serverLevel, ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 4, 0, 0, 0, 0.05, true);
                sendParticle(serverLevel, ParticleTypes.LAVA, pos.x, pos.y, pos.z, 2, 0, 0, 0, 0.15, true);
            }
        }
    }

    public void hitEntity(Vec3 pos, GunData gunData, Entity shooter) {
        if (this.level() instanceof ServerLevel serverLevel) {
            if (gunData.compute().getExplosionRadius() > 0) {
                findNearEntity(pos, gunData, shooter);
                sendParticle(serverLevel, ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 24, 0, 0, 0, 0.2, true);
                sendParticle(serverLevel, ParticleTypes.LAVA, pos.x, pos.y, pos.z, 8, 0, 0, 0, 0.4, true);
            } else {
                sendParticle(serverLevel, ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 4, 0, 0, 0, 0.05, true);
                sendParticle(serverLevel, ParticleTypes.LAVA, pos.x, pos.y, pos.z, 2, 0, 0, 0, 0.15, true);
            }
        }
    }

    public void findNearEntity(Vec3 vec, GunData gunData, Entity shooter) {
        double aoeDamage = gunData.compute().getExplosionDamage();
        double range = gunData.compute().getExplosionRadius();

        if (level() instanceof ServerLevel serverLevel) {
            List<Entity> entities = new SeekTool.Builder(this)
                    .withinRange(vec, range)
                    .notItsVehicle()
                    .baseFilter()
                    .smokeFilter()
                    .noVehicle()
                    .differentTeam()
                    .build();

            for (var e : entities) {
                double dis = vec.distanceTo(e.getEyePosition());

                for (float i = 0; i < dis; i += 0.2f) {
                    Vec3 toVec = vec.vectorTo(e.getEyePosition()).normalize();
                    Vec3 pos = vec.add(toVec.scale(i));
                    sendParticle(serverLevel, ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0, true);
                }

                sendParticle(serverLevel, ParticleTypes.LAVA, e.getX(), e.getEyeY(), e.getZ(), 4, 0, 0, 0, 0.15, true);

                float damage = (float) (aoeDamage - Mth.clamp(dis / range, 0, 0.75) * aoeDamage);

                DamageHandler.doDamage(e, ModDamageTypes.causeLaserDamage(this.level().registryAccess(), this, shooter), damage);

                if (shooter instanceof ServerPlayer player) {
                    var holder = Holder.direct(ModSounds.INDICATION.get());
                    player.connection.send(new ClientboundSoundPacket(holder, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1f, 1f, player.level().random.nextLong()));
                    NetworkRegistry.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new ClientIndicatorMessage(0, 5));
                }
            }
        }
    }


    @Override
    public float getWheelMaxHealth() {
        return 100;
    }

    @Override
    public float getEngineMaxHealth() {
        return 150;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Component firstPersonAmmoComponent(GunData data, Player player) {
        var name = data.compute().getName();
        if (name == null || name.isBlank()) return Component.empty();

        return Component.translatable(name, (int) (25 + data.heat.get()) + " " + "°C");
    }

    public boolean shouldShowMissileOn(VehicleEntity vehicle, int... missileWeaponIndices) {
        for (var passenger : vehicle.getPassengers()) {
            int seatIndex = vehicle.getSeatIndex(passenger);
            if (seatIndex < 0) continue;

            int currentWeaponIndex = vehicle.getSelectedWeapon(seatIndex);
            boolean matches = false;
            for (int index : missileWeaponIndices) {
                if (currentWeaponIndex == index) {
                    matches = true;
                    break;
                }
            }
            if (!matches) continue;

            var gunData = vehicle.getGunData(seatIndex);
            if (gunData != null && (gunData.ammo.get() > 0 || gunData.backupAmmoCount.get() > 0)) {
                return true;
            }
        }
        return false;
    }
@Override
    public double getMouseSensitivity() {
        return zoomVehicle ? 0.1 : 0.25;
    }


}
