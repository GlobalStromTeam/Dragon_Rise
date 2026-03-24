package com.redabysslucia.dragonrise_reforge.entities;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import com.redabysslucia.dragonrise_reforge.entities.utils.NightVisionVehicle;
import com.redabysslucia.dragonrise_reforge.utils.EngineParticleUtil;
import com.redabysslucia.dragonrise_reforge.utils.TurretLightUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
@SuppressWarnings("removal")
public class T80Entity extends NightVisionVehicle {

	public T80Entity(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Override
	public DamageModifier getDamageModifier() {
		return super.getDamageModifier()
				.custom((source, damage) -> getSourceAngle(source, 0.3f) * damage);
	}

	// 用于跟踪需要删除的光源方块
	private int lightRemovalTimer = 0;

	@Override
	public void tick() {
		super.tick();
		if (tickCount % 5 == 0) {
			// 在MainEngine obb框位置生成粒子效果
			EngineParticleUtil.spawnMainEngineParticles(this, level());
		}

		// 处理光源方块删除
		if (lightRemovalTimer > 0) {
			lightRemovalTimer--;
			if (lightRemovalTimer == 0) {
				// 删除光源方块
				TurretLightUtil.handleTurretFireLight(this, level(), 0); // 传入0表示停止开火
			}
		}
	}

	@Override
	public void vehicleShoot(net.minecraft.world.entity.LivingEntity living, java.util.UUID uuid, net.minecraft.world.phys.Vec3 targetPos) {
		super.vehicleShoot(living, uuid, targetPos);
		// 处理炮口火光效果
		TurretLightUtil.handleTurretFireLight(this, level(), 1); // 传入1表示正在开火
		// 设置删除计时器
		lightRemovalTimer = 1; // 1 tick后删除
	}
	@Override
	public ResourceLocation getNightVisionShader() {
		return new ResourceLocation("shaders/post/night-vision-wp.json");
	}
}