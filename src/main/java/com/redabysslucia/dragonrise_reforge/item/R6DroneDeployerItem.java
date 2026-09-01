package com.redabysslucia.dragonrise_reforge.item;

import com.atsuishio.superbwarfare.item.misc.AbstractDeployerItem;
import com.redabysslucia.dragonrise_reforge.entities.special.R6DroneEntity;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * 侦察无人车部署器：右键方块部署 R6DroneEntity。
 * 部署后用 superbwarfare 的 monitor（监控平板）右键无人车完成链接并遥控。
 */
public class R6DroneDeployerItem extends AbstractDeployerItem {

    public R6DroneDeployerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Entity spawnDeployedEntity(Level level, Player player) {
        return new R6DroneEntity(ModEntities.R6_DRONE.get(), level);
    }
}
