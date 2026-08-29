package com.rhythm.dragon_vehicle_deployer.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.rhythm.dragon_vehicle_deployer.DragonVehicleDeployer;
import com.rhythm.dragon_vehicle_deployer.block.entity.VehicleDeployerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import javax.annotation.Nullable;

public class VehicleDeployerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    private static final VoxelShape SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    public VehicleDeployerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(TRIGGERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(VehicleDeployerBlock::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VehicleDeployerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, DragonVehicleDeployer.VEHICLE_DEPLOYER_BLOCK_ENTITY.get(), VehicleDeployerBlockEntity::tick);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean charged = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
        boolean triggered = state.getValue(TRIGGERED);

        if (charged && !triggered) {
            world.setBlock(pos, state.setValue(TRIGGERED, Boolean.TRUE), 4);
            if (world.getBlockEntity(pos) instanceof VehicleDeployerBlockEntity blockEntity) {
                blockEntity.deploy(state);
            }
        } else if (!charged && triggered) {
            world.setBlock(pos, state.setValue(TRIGGERED, Boolean.FALSE), 4);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) return ItemInteractionResult.SUCCESS;

        if (!(world.getBlockEntity(pos) instanceof VehicleDeployerBlockEntity blockEntity)) {
            return ItemInteractionResult.FAIL;
        }

        // OP sneak+right-click: open config GUI
        if (player.isShiftKeyDown() && player.hasPermissions(2)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(blockEntity);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // 普通右键：写入载具信息（需要创造模式和容器物品）
        if (!player.isCreative()) return ItemInteractionResult.FAIL;

        var heldStack = player.getItemInHand(hand);
        Item containerItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("superbwarfare", "container"));
        if (containerItem == null || heldStack.getItem() != containerItem) {
            player.displayClientMessage(Component.translatable("des.superbwarfare.vehicle_deployer.fail").withStyle(ChatFormatting.RED), true);
            return ItemInteractionResult.FAIL;
        }

        blockEntity.writeEntityInfo(stack);
        player.displayClientMessage(Component.translatable("des.superbwarfare.vehicle_deployer.success").withStyle(ChatFormatting.GREEN), true);

        return ItemInteractionResult.SUCCESS;
    }
}
