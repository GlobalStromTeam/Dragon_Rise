package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.tools.ParticleTool;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.IVehicleBackground;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.redabysslucia.dragonrise_reforge.utils.EngineParticleUtil;
import com.redabysslucia.dragonrise_reforge.utils.GeoBasedParticleUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@SuppressWarnings("removal")
public class T80Entity extends VehicleEntity implements IVehicleBackground {

	public T80Entity(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}


	@Override
	public void vehicleShoot(LivingEntity living, UUID uuid, Vec3 targetPos) {
		if (living != null) {
			Level level = living.level();
			if (level instanceof ServerLevel && living == getFirstPassenger() && getWeaponIndex(0) == 0) {
				ParticleTool.spawnBigCannonMuzzleParticles(getShootVec(living, 1f), getShootPos(living, 1f), (ServerLevel) level, this);
			}
		}
		super.vehicleShoot(living, uuid, targetPos);
	}

	@Override
	public void tick() {
		super.tick();
		if (tickCount % 1 == 0 && hasPlayerOperator()) {
            GeoBasedParticleUtil.spawnParticlesFromManualPosition(this, 0, 21.4995, 64.7884);
		}
	}

	/**
	 * 检查是否有玩家在操作车辆
	 * @return 如果有玩家在操作返回true，否则返回false
	 */
	private boolean hasPlayerOperator() {
		// 检查是否有乘客
		if (!this.getPassengers().isEmpty()) {
			// 检查第一个乘客是否是玩家
			return this.getPassengers().get(0) instanceof net.minecraft.world.entity.player.Player;
		}
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getBackgroundTexture() {
		var mc = net.minecraft.client.Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return null;
		
		var seatIndex = getSeatIndex(player);
		// 一号位背景
		if (seatIndex == 0) {
			return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos1.png");
		}
		// 二号位背景
		else if (seatIndex == 1) {
			return new ResourceLocation(Dragonrise_reforge.MODID, "textures/overlay/vehicle/hud/testcroos2.png");
		}
		return null;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderBackground() {
		// 只在一号位和二号位显示背景
		var mc = net.minecraft.client.Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return false;
		
		var seatIndex = getSeatIndex(player);
		// 一号位是索引0，二号位是索引1
		return seatIndex == 0 || seatIndex == 1;
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public float getBackgroundAlpha() {
		// 控制背景透明度 (0.0f - 1.0f)
		return 1.0f;
	}
}
