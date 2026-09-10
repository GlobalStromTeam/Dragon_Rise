package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.client.renderer.entity.IkFightcatRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.IkLocustRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Mi24vRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.F15eRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Mv3SupplyRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Mv3ArmedRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Ural4320SupplyRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Ural4320Renderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Ural4320Zu23Renderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Brdm2Renderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.ChallengerDsRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Mv3Renderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Ztz96aRenderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.T72b3Renderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Aavc7c1Renderer;






import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Aav7a1Renderer;


import com.redabysslucia.dragonrise_reforge.client.renderer.entity.Aav7a1SbmRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.MarkvRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.*;
import com.redabysslucia.dragonrise_reforge.client.renderer.ammo.NukerBombRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.atmg.HJ8Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.atmg.R9M133Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.hmg.DSHKRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.hmg.M2Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.hmg.qjz89Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.AAshellRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Agm65CustomRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Aim120Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Aim9Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.AirBomb500kgRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.AntiTopWireGuideMissileRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Gbu12Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Gbu24Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Gb250Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Gb500Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.JdamErRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Kd88aRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Kh29Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Ls6IrRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Ls6Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Pl10Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Pl12Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.Pl15Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.R73Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.projectile.R77Renderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.special.CyborgTankRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.special.ClusterChargeRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.special.R6DroneRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.special.SignalFlareRenderer;
import com.redabysslucia.dragonrise_reforge.client.renderer.entity.special.StarShellRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
                event.registerEntityRenderer(ModEntities.ZTZ99A.get(), ZTZ99ASbmRenderer::new);
                event.registerEntityRenderer(ModEntities.ZTZ99AH.get(), ZTZ99AHRenderer::new);
                event.registerEntityRenderer(ModEntities.ZTZ99BH.get(), ZTZ99BHRenderer::new);
                event.registerEntityRenderer(ModEntities.ZTZ59A.get(), ZTZ59ARenderer::new);
                event.registerEntityRenderer(ModEntities.VT4A1.get(), VT4A1Renderer::new);
                event.registerEntityRenderer(ModEntities.VT4B.get(), VT4BRenderer::new);
                event.registerEntityRenderer(ModEntities.ZTQ15.get(), ZTQ15Renderer::new);
                event.registerEntityRenderer(ModEntities.CSK181.get(), CSK181Renderer::new);
                event.registerEntityRenderer(ModEntities.HUMVEE.get(), HumveeRenderer::new);
                event.registerEntityRenderer(ModEntities.M1A2SEPV2.get(), M1A2SEPV2Renderer::new);
                event.registerEntityRenderer(ModEntities.AMX56.get(), AMX56Renderer::new);
                event.registerEntityRenderer(ModEntities.CV90.get(), CV90Renderer::new);
                event.registerEntityRenderer(ModEntities.JAS39E.get(), JAS39ERenderer::new);
                event.registerEntityRenderer(ModEntities.FA18E.get(), FA18ERenderer::new);
                event.registerEntityRenderer(ModEntities.KA50.get(), KA50Renderer::new);
                event.registerEntityRenderer(ModEntities.T80.get(), T80Renderer::new);
                event.registerEntityRenderer(ModEntities.T90MH.get(), T90MHRenderer::new);
                event.registerEntityRenderer(ModEntities.S2S38.get(), S2S38Renderer::new);
                event.registerEntityRenderer(ModEntities.AA625E.get(), AA625ERenderer::new);
                event.registerEntityRenderer(ModEntities.Z10A.get(), Z10ARenderer::new);
                event.registerEntityRenderer(ModEntities.Z9.get(), Z9Renderer::new);
                event.registerEntityRenderer(ModEntities.HYR0.get(), HYR0Renderer::new);
                event.registerEntityRenderer(ModEntities.J8.get(), J8Renderer::new);
                event.registerEntityRenderer(ModEntities.J10.get(), J10Renderer::new);
                event.registerEntityRenderer(ModEntities.J10C.get(), J10CRenderer::new);
                event.registerEntityRenderer(ModEntities.J11.get(), J11Renderer::new);
                event.registerEntityRenderer(ModEntities.JF17.get(), JF17Renderer::new);
                event.registerEntityRenderer(ModEntities.J15T.get(), J15TRenderer::new);
                event.registerEntityRenderer(ModEntities.J16.get(), J16Renderer::new);
                event.registerEntityRenderer(ModEntities.J35.get(), J35Renderer::new);
                event.registerEntityRenderer(ModEntities.REFALE.get(), REFALERenderer::new);
                event.registerEntityRenderer(ModEntities.REFALEAA.get(), REFALEAARenderer::new);
                event.registerEntityRenderer(ModEntities.Z20.get(), Z20Renderer::new);
                event.registerEntityRenderer(ModEntities.Z10ME.get(), Z10MERenderer::new);
                event.registerEntityRenderer(ModEntities.ZBL08.get(), ZBL08Renderer::new);
                event.registerEntityRenderer(ModEntities.CM34.get(), CM34Renderer::new);
                event.registerEntityRenderer(ModEntities.motuo.get(), motuoRenderer::new);
                event.registerEntityRenderer(ModEntities.ZLT_11.get(), ZLT11Renderer::new);
                event.registerEntityRenderer(ModEntities.ZBD04A.get(), ZBD04ARenderer::new);
                event.registerEntityRenderer(ModEntities.PANZER4.get(), PANZER4Renderer::new);
                event.registerEntityRenderer(ModEntities.PAK40.get(), Pak40Renderer::new);
                event.registerEntityRenderer(ModEntities.SX1.get(), SX1Renderer::new);
                event.registerEntityRenderer(ModEntities.FAVA.get(), FAVARenderer::new);
                event.registerEntityRenderer(ModEntities.STRV103.get(), STRV103Renderer::new);
                event.registerEntityRenderer(ModEntities.F14.get(), F14Renderer::new);
                event.registerEntityRenderer(ModEntities.SU24.get(), SU24SbmRenderer::new);
                event.registerEntityRenderer(ModEntities.SU24M.get(), SU24MSbmRenderer::new);
                event.registerEntityRenderer(ModEntities.TYPE100.get(), TYPE100Renderer::new);
                event.registerEntityRenderer(ModEntities.BMP3.get(), BMP3Renderer::new);
                event.registerEntityRenderer(ModEntities.M3A3.get(), M3A3Renderer::new);
                event.registerEntityRenderer(ModEntities.J20.get(), J20Renderer::new);
                event.registerEntityRenderer(ModEntities.WLSC.get(), WLSCRenderer::new);
                event.registerEntityRenderer(ModEntities.J20VTOL.get(), J20VTOLRenderer::new);
                event.registerEntityRenderer(ModEntities.AV8B.get(), AV8BRenderer::new);
                event.registerEntityRenderer(ModEntities.Q5.get(), Q5Renderer::new);
                event.registerEntityRenderer(ModEntities.MK19.get(), MK19Renderer::new);
                event.registerEntityRenderer(ModEntities.ZU23.get(), ZU23Renderer::new);
                event.registerEntityRenderer(ModEntities.BMPT72.get(), BMPT72Renderer::new);
                event.registerEntityRenderer(ModEntities.T80B.get(), T80BRenderer::new);
                event.registerEntityRenderer(ModEntities.EC665.get(), EC665Renderer::new);
                event.registerEntityRenderer(ModEntities.AH64.get(), AH64Renderer::new);
                event.registerEntityRenderer(ModEntities.UH60.get(), UH60Renderer::new);
                event.registerEntityRenderer(ModEntities.SHIELD.get(), SHIELDRenderer::new);
                event.registerEntityRenderer(ModEntities.AMMO_SUPPLY_STATION.get(), AmmoSupplyStationRenderer::new);
                event.registerEntityRenderer(ModEntities.GENERATOR.get(), GeneratorRenderer::new);
                event.registerEntityRenderer(ModEntities.TOYOTASEIKI.get(), TOYOTASEIKIRenderer::new);
                event.registerEntityRenderer(ModEntities.CYBORG_TANK.get(), CyborgTankRenderer::new);
                event.registerEntityRenderer(ModEntities.PROJECT640.get(), Project640Renderer::new);
                event.registerEntityRenderer(ModEntities.TEST.get(), TESTRenderer::new);
                event.registerEntityRenderer(ModEntities.SPACEBAG.get(), SpacebagRenderer::new);
                event.registerEntityRenderer(ModEntities.CAMEL.get(), CamelRenderer::new);
                event.registerEntityRenderer(ModEntities.qjz89.get(), qjz89Renderer::new);
                event.registerEntityRenderer(ModEntities.DSHK.get(), DSHKRenderer::new);
                event.registerEntityRenderer(ModEntities.M2.get(), M2Renderer::new);
                event.registerEntityRenderer(ModEntities.T3476.get(), T3476Renderer::new);
                event.registerEntityRenderer(ModEntities.IS2.get(), IS2Renderer::new);
                event.registerEntityRenderer(ModEntities.KV1.get(), KV1Renderer::new);
                event.registerEntityRenderer(ModEntities.TIGER.get(), TigerRenderer::new);
                event.registerEntityRenderer(ModEntities.CHURCHILL_VII.get(), ChurchillVIIRenderer::new);
                event.registerEntityRenderer(ModEntities.COMET.get(), CometRenderer::new);
                event.registerEntityRenderer(ModEntities.MAUS.get(), MausRenderer::new);
                event.registerEntityRenderer(ModEntities.T3485.get(), T3485Renderer::new);
                event.registerEntityRenderer(ModEntities.PERSHING.get(), PershingRenderer::new);
                event.registerEntityRenderer(ModEntities.AC130.get(), AC130Renderer::new);
                event.registerEntityRenderer(ModEntities.TUNGUSKA.get(), TunguskaRenderer::new);
                event.registerEntityRenderer(ModEntities.WLHGZU23.get(), WLHGZU23Renderer::new);
                event.registerEntityRenderer(ModEntities.syy651.get(), syy651Renderer::new);
                event.registerEntityRenderer(ModEntities.HJ8.get(), HJ8Renderer::new);
                event.registerEntityRenderer(ModEntities.BMD4M.get(), BMD4MRenderer::new);
                event.registerEntityRenderer(ModEntities.NPDS114.get(), NPDS114Renderer::new);
                event.registerEntityRenderer(ModEntities.NPDS514.get(), NPDS514Renderer::new);
                event.registerEntityRenderer(ModEntities.NPDS810.get(), NPDS810Renderer::new);
                event.registerEntityRenderer(ModEntities.SD905.get(), SD905Renderer::new);
                event.registerEntityRenderer(ModEntities.PZBJY.get(), PZBJYRenderer::new);
                event.registerEntityRenderer(ModEntities.M10BOOKER.get(), M10BookerRenderer::new);
                event.registerEntityRenderer(ModEntities.NH90.get(), NH90Renderer::new);
                event.registerEntityRenderer(ModEntities.R2S25M.get(), R2S25MRenderer::new);
                event.registerEntityRenderer(ModEntities.R9M133.get(), R9M133Renderer::new);
                event.registerEntityRenderer(ModEntities.M4A2.get(), M4A2Renderer::new);
                event.registerEntityRenderer(ModEntities.F4U.get(), F4URenderer::new);
                event.registerEntityRenderer(ModEntities.M4A2105.get(), M4A2105Renderer::new);
                event.registerEntityRenderer(ModEntities.M3Stuart.get(), M3StuartRenderer::new);
                event.registerEntityRenderer(ModEntities.LVT.get(), LVTRenderer::new);
                event.registerEntityRenderer(ModEntities.type97.get(), type97Renderer::new);
                event.registerEntityRenderer(ModEntities.type97Q.get(), type97QRenderer::new);
                event.registerEntityRenderer(ModEntities.type3.get(), type3Renderer::new);
                event.registerEntityRenderer(ModEntities.AKM.get(), AKMRenderer::new);
                event.registerEntityRenderer(ModEntities.ZSU234.get(), ZSU234Renderer::new);
                event.registerEntityRenderer(ModEntities.AASHELL.get(), AAshellRenderer::new);
                event.registerEntityRenderer(ModEntities.AIM120.get(), Aim120Renderer::new);
                event.registerEntityRenderer(ModEntities.AGM65.get(), Agm65CustomRenderer::new);
                event.registerEntityRenderer(ModEntities.GBU12.get(), Gbu12Renderer::new);
                event.registerEntityRenderer(ModEntities.AIM9.get(), Aim9Renderer::new);
                event.registerEntityRenderer(ModEntities.PL12.get(), Pl12Renderer::new);
                event.registerEntityRenderer(ModEntities.PL15.get(), Pl15Renderer::new);
                event.registerEntityRenderer(ModEntities.R77.get(), R77Renderer::new);
                event.registerEntityRenderer(ModEntities.PL10.get(), Pl10Renderer::new);
                event.registerEntityRenderer(ModEntities.R73.get(), R73Renderer::new);
                event.registerEntityRenderer(ModEntities.KD88A.get(), Kd88aRenderer::new);
                event.registerEntityRenderer(ModEntities.KH29.get(), Kh29Renderer::new);
                event.registerEntityRenderer(ModEntities.GB500.get(), Gb500Renderer::new);
                event.registerEntityRenderer(ModEntities.GBU24.get(), Gbu24Renderer::new);
                event.registerEntityRenderer(ModEntities.JDAM_ER.get(), JdamErRenderer::new);
                event.registerEntityRenderer(ModEntities.LS6.get(), Ls6Renderer::new);
                event.registerEntityRenderer(ModEntities.LS6_IR.get(), Ls6IrRenderer::new);
                event.registerEntityRenderer(ModEntities.GB250.get(), Gb250Renderer::new);
                event.registerEntityRenderer(ModEntities.AIRBOMB500KG.get(), AirBomb500kgRenderer::new);
                event.registerEntityRenderer(ModEntities.ANTI_TOP_WIRE_GUIDE_MISSILE.get(), AntiTopWireGuideMissileRenderer::new);
                event.registerEntityRenderer(ModEntities.L1A2.get(), L1A2Renderer::new);
                event.registerEntityRenderer(ModEntities.F16C.get(), F16CRenderer::new);
                event.registerEntityRenderer(ModEntities.TJGC.get(), TJGCRenderer::new);
                event.registerEntityRenderer(ModEntities.TERRORIST.get(), TerroristRenderer::new);
                event.registerEntityRenderer(ModEntities.NUKERBOMB.get(), NukerBombRenderer::new);
                event.registerEntityRenderer(ModEntities.AH1F.get(), Ah1fRenderer::new);
                event.registerEntityRenderer(ModEntities.M113.get(), M113Renderer::new);
                event.registerEntityRenderer(ModEntities.LEOPARD2A4.get(), Leopard2a4Renderer::new);
                event.registerEntityRenderer(ModEntities.FLARAKPZ1.get(), Flarakpz1Renderer::new);
                event.registerEntityRenderer(ModEntities.M270.get(), M270Renderer::new);
                event.registerEntityRenderer(ModEntities.M1A1HC.get(), M1a1hcRenderer::new);
                event.registerEntityRenderer(ModEntities.DARKBEAR.get(), DarkbearRenderer::new);
                event.registerEntityRenderer(ModEntities.HUMVEETOW.get(), HumveetowRenderer::new);
                event.registerEntityRenderer(ModEntities.ZTD05.get(), Ztd05Renderer::new);
                event.registerEntityRenderer(ModEntities.ZBD05.get(), Zbd05Renderer::new);
                event.registerEntityRenderer(ModEntities.ZSL10.get(), Zsl10Renderer::new);

                event.registerEntityRenderer(ModEntities.MARKV.get(), MarkvRenderer::new);

                event.registerEntityRenderer(ModEntities.AAV7A1.get(), Aav7a1SbmRenderer::new);
                event.registerEntityRenderer(ModEntities.AAV7A1.get(), Aav7a1Renderer::new);
                event.registerEntityRenderer(ModEntities.AAVC7C1.get(), Aavc7c1Renderer::new);
                event.registerEntityRenderer(ModEntities.T72B3.get(), T72b3Renderer::new);
                event.registerEntityRenderer(ModEntities.ZTZ96A.get(), Ztz96aRenderer::new);
                event.registerEntityRenderer(ModEntities.MV3.get(), Mv3Renderer::new);
                event.registerEntityRenderer(ModEntities.CHALLENGER_DS.get(), ChallengerDsRenderer::new);
                event.registerEntityRenderer(ModEntities.BRDM2.get(), Brdm2Renderer::new);
                event.registerEntityRenderer(ModEntities.CLUSTER_CHARGE.get(), ClusterChargeRenderer::new);
                event.registerEntityRenderer(ModEntities.R6_DRONE.get(), R6DroneRenderer::new);
                event.registerEntityRenderer(ModEntities.ATTACK_DRONE.get(), R6DroneRenderer::new);
                event.registerEntityRenderer(ModEntities.SIGNAL_FLARE.get(), SignalFlareRenderer::new);
                event.registerEntityRenderer(ModEntities.STAR_SHELL.get(), StarShellRenderer::new);
                event.registerEntityRenderer(ModEntities.URAL4320_ZU23.get(), Ural4320Zu23Renderer::new);
                event.registerEntityRenderer(ModEntities.URAL4320.get(), Ural4320Renderer::new);
                event.registerEntityRenderer(ModEntities.URAL4320_SUPPLY.get(), Ural4320SupplyRenderer::new);
                event.registerEntityRenderer(ModEntities.MV3_ARMED.get(), Mv3ArmedRenderer::new);
                event.registerEntityRenderer(ModEntities.MV3_SUPPLY.get(), Mv3SupplyRenderer::new);
                event.registerEntityRenderer(ModEntities.F15E.get(), F15eRenderer::new);
                event.registerEntityRenderer(ModEntities.MI24V.get(), Mi24vRenderer::new);
                event.registerEntityRenderer(ModEntities.IK_LOCUST.get(), IkLocustRenderer::new);
                event.registerEntityRenderer(ModEntities.IK_FIGHTCAT.get(), IkFightcatRenderer::new);
        }
}