package com.redabyss.ralmod;

import com.redabyss.ralmod.RalModRegist.BlockEntities;
import com.redabyss.ralmod.RalModRegist.Blocks;
import com.redabyss.ralmod.RalModRegist.CreativeTabs;
import com.redabyss.ralmod.RalModRegist.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RalMod.MODID)
public class RalMod {
    public static final String MODID = "ralmod";
    private static final Logger LOGGER = LogManager.getLogger();

    public RalMod() {
        IEventBus bus= FMLJavaModLoadingContext.get().getModEventBus();
        Items.register(bus);
        Blocks.register(bus);
        BlockEntities.register(bus);
        CreativeTabs.register(bus);
    }
}