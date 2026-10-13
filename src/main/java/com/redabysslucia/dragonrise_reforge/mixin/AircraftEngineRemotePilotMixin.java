package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleEngineUtils;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.init.ModItems;
import com.atsuishio.superbwarfare.tools.NBTTool;
import com.atsuishio.superbwarfare.item.misc.MonitorItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * 让 SBW 的固定翼引擎 {@code VehicleEngineUtils.aircraftEngine} 接受"遥控驾驶"。
 * <p>
 * SBW 的飞机引擎把两件事硬绑在"载具上有乘员"上：
 * <ol>
 *   <li>{@code getPassengers().isEmpty()} 为真时：清空左右/前后/上下输入、掉功率、空中还会让机头下坠
 *       （见引擎开头的 {@code if (getPassengers().isEmpty() || isInFluidType)} 分支）；</li>
 *   <li>{@code getFirstPassenger() == null} 时：清空油门/舵/减速板输入
 *       （见 {@code if (firstPassenger != null) ... else ...}）。</li>
 * </ol>
 * 无人机的乘员在遥控者手里（手持监控平板），机上没人 —— 于是引擎会把遥控输入全部丢掉，飞机根本飞不起来。
 * <p>
 * 本 mixin <b>只在这个方法内部</b> 把这两处判断放行：遥控者正在用监控平板遥控这台无人机时，
 * 把遥控者当作乘员返回。SBW 其它任何地方（座位、渲染、伤害归属、上下机同步）都不受影响。
 * <p>
 * 重定向的序号按 {@code aircraftEngine} 字节码里的调用顺序：
 * {@code getPassengers()} 只有 1 处（序 0）；{@code getFirstPassenger()} 有 3 处，
 * 序 0/1 是入水损伤归属，序 2 才是油门/舵门控，因此只改序 2。
 */
@Mixin(VehicleEngineUtils.class)
public class AircraftEngineRemotePilotMixin {

    @Redirect(method = "aircraftEngine", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;getPassengers()Ljava/util/List;"))
    private static List<Entity> dr$remotePilotAsPassengers(VehicleEntity vehicle) {
        List<Entity> passengers = vehicle.getPassengers();
        if (!passengers.isEmpty()) {
            return passengers;      // 真有人（例如以后加了座位）：按原样走
        }
        Player pilot = dr$remotePilot(vehicle);
        return pilot == null ? passengers : List.of(pilot);
    }

    @Redirect(method = "aircraftEngine", at = @At(value = "INVOKE", ordinal = 2,
            target = "Lcom/atsuishio/superbwarfare/entity/vehicle/base/VehicleEntity;getFirstPassenger()Lnet/minecraft/world/entity/Entity;"))
    private static Entity dr$remotePilotAsFirstPassenger(VehicleEntity vehicle) {
        Entity real = vehicle.getFirstPassenger();
        return real != null ? real : dr$remotePilot(vehicle);
    }

    /**
     * 遥控中的驾驶员：无人机 + 控制者手持监控平板且处于 Using/Linked 状态。
     * 与 SBW 自己判定"遥控是否生效"的条件一致（见 VehicleMovementMessage / MouseMoveMessage）。
     */
    private static Player dr$remotePilot(VehicleEntity vehicle) {
        if (!(vehicle instanceof DroneEntity drone)) {
            return null;
        }
        Player pilot = drone.getController();
        if (pilot == null) {
            return null;
        }
        ItemStack stack = pilot.getMainHandItem();
        if (!stack.is(ModItems.MONITOR.get())) {
            return null;
        }
        CompoundTag tag = NBTTool.getTag(stack);
        if (!tag.getBoolean(MonitorItem.USING) || !tag.getBoolean(MonitorItem.LINKED)) {
            return null;
        }
        return pilot;
    }
}
