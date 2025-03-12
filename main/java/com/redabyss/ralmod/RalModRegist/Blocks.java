package com.redabyss.ralmod.RalModRegist;

import com.redabyss.ralmod.RalMod;
import com.redabyss.ralmod.RalModServer.BlockEntities.gl6BE;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Blocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RalMod.MODID);

    public static final RegistryObject<Block> gl6 = BLOCKS.register(
            "gl6",
            () -> new BaseEntityBlock(BlockBehaviour
                    .Properties.of()
                    .strength(3F)
                    .lightLevel(value ->15 ))
            {
                @Override
                public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
                    return new gl6BE(pos,state);
                }
                @Override
                public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
                    return createTickerHelper(type,BlockEntities.GL6_BE.get(),gl6BE::tick);
                }
                @Override
                public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
                    if (level.getBlockEntity(pos) instanceof gl6BE be) {
                        gl6BE.tick(level, pos, state, be); // 触发方块实体更新
                    }
                }
            }
    );
    public static void register(IEventBus eventBus) {BLOCKS.register(eventBus);}
}
