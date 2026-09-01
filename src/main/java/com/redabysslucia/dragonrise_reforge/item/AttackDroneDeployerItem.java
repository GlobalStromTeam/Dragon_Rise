package com.redabysslucia.dragonrise_reforge.item;

import com.atsuishio.superbwarfare.item.misc.AbstractDeployerItem;
import com.redabysslucia.dragonrise_reforge.entities.special.AttackDroneEntity;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 攻击无人机部署器：右键方块部署 AttackDroneEntity。
 * 与侦察无人车一致：部署后用 superbwarfare 的 monitor 链接遥控，左键开火。
 */
public class AttackDroneDeployerItem extends AbstractDeployerItem {

    public AttackDroneDeployerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Entity spawnDeployedEntity(Level level, Player player) {
        return new AttackDroneEntity(ModEntities.ATTACK_DRONE.get(), level);
    }
}
