package com.redabysslucia.dragonrise_reforge.item;

import com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity;
import com.redabysslucia.dragonrise_reforge.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

public class HJ8Deployer extends Item {

    public HJ8Deployer() {
        super(new Properties().rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack stack = pContext.getItemInHand();
            BlockPos clickedPos = pContext.getClickedPos();
            Direction direction = pContext.getClickedFace();
            Player player = pContext.getPlayer();
            if (player == null) {
                return InteractionResult.PASS;
            }

            BlockState blockstate = level.getBlockState(clickedPos);
            BlockPos pos;
            if (blockstate.getCollisionShape(level, clickedPos).isEmpty()) {
                pos = clickedPos;
            } else {
                pos = clickedPos.relative(direction);
            }

            HJ8Entity HJ8Entity = new HJ8Entity(ModEntities.HJ8.get(), level);
            HJ8Entity.setPos((double) pos.getX() + 0.5, pos.getY() + 1, (double) pos.getZ() + 0.5);
            double yOffset = this.getYOffset(level, pos, !Objects.equals(clickedPos, pos) && direction == Direction.UP, HJ8Entity.getBoundingBox());
            HJ8Entity.moveTo((double) pos.getX() + 0.5, pos.getY() + yOffset, (double) pos.getZ() + 0.5);
            level.addFreshEntity(HJ8Entity);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.gameEvent(pContext.getPlayer(), GameEvent.ENTITY_PLACE, clickedPos);

            return InteractionResult.CONSUME;
        }
    }

    public double getYOffset(LevelReader pLevel, BlockPos pPos, boolean pShouldOffsetYMore, AABB pBox) {
        AABB aabb = new AABB(pPos);
        if (pShouldOffsetYMore) {
            aabb = aabb.expandTowards(0, -1, 0);
        }

        Iterable<VoxelShape> iterable = pLevel.getCollisions(null, aabb);
        return 1 + Shapes.collide(Direction.Axis.Y, pBox, iterable, pShouldOffsetYMore ? -2 : -1);
    }

    @Override
    @ParametersAreNonnullByDefault
    public @NotNull InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
        if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else if (!(pLevel instanceof ServerLevel)) {
            return InteractionResultHolder.success(itemstack);
        } else {
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (!(pLevel.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
                return InteractionResultHolder.pass(itemstack);
            } else if (pLevel.mayInteract(pPlayer, blockpos) && pPlayer.mayUseItemAt(blockpos, blockhitresult.getDirection(), itemstack)) {
                HJ8Entity HJ8Entity = new HJ8Entity(ModEntities.HJ8.get(), pLevel);
                HJ8Entity.setPos((double) blockpos.getX() + 0.5, blockpos.getY(), (double) blockpos.getZ() + 0.5);
                pLevel.addFreshEntity(HJ8Entity);

                if (!pPlayer.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                pPlayer.awardStat(Stats.ITEM_USED.get(this));
                pLevel.gameEvent(pPlayer, GameEvent.ENTITY_PLACE, HJ8Entity.position());
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        }
    }
}
