package com.redabysslucia.dragonrise_reforge.item;

import com.redabysslucia.dragonrise_reforge.entities.special.ClusterChargeEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 霰射炸药（Cluster Charge）物品。
 * 放置方式完全参照 superbwarfare 的 EDDItem：只能贴在方块侧面（水平面），
 * 并根据点击点在方块面上的上下/左右位置确定 4 个贴墙角位。
 */
public class ClusterChargeItem extends Item {

    public ClusterChargeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("des.dragonrise_reforge.cluster_charge").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos relative = pos.relative(direction);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player != null && !this.mayPlace(player, direction, stack, relative)) {
            return InteractionResult.FAIL;
        } else {
            // 允许侧面与地板（UP），禁止天花板（DOWN）
            if (direction == Direction.DOWN) return InteractionResult.FAIL;

            Level level = context.getLevel();
            ClusterChargeEntity entity = new ClusterChargeEntity(
                    player,
                    level,
                    relative,
                    direction,
                    this.getCornerFromHit(direction, pos, context.getClickLocation())
            );

            if (entity.survives()) {
                if (!level.isClientSide()) {
                    entity.playPlacementSound();
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
                    level.addFreshEntity(entity);
                }

                stack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide());
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    public boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
        return !player.level().isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, direction, stack);
    }

    public int getCornerFromHit(Direction face, BlockPos pos, Vec3 hitVec) {
        double x = hitVec.x;
        double y = hitVec.y;
        double z = hitVec.z;

        boolean top = y > pos.getY() + 0.5;
        boolean left = switch (face) {
            case WEST -> z < pos.getZ() + 0.5;
            case EAST -> z > pos.getZ() + 0.5;
            case SOUTH -> x < pos.getX() + 0.5;
            case NORTH -> x > pos.getX() + 0.5;
            default -> false;
        };

        if (left && top) {
            return 0;
        } else if (left) {
            return 1;
        } else if (!top) {
            return 2;
        } else {
            return 3;
        }
    }
}
