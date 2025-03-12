package com.redabyss.ralmod.RalModRegist;

import com.redabyss.ralmod.RalMod;
import com.redabyss.ralmod.RalModServer.BlockEntities.gl6BE;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RalMod.MODID);

    public static final RegistryObject<BlockEntityType<gl6BE>> GL6_BE =
            BLOCK_ENTITIES.register("gl6_be", () ->
                    BlockEntityType.Builder.of(
                            gl6BE::new,
                            Blocks.gl6.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {BLOCK_ENTITIES.register(eventBus);}
}