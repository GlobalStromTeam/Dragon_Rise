package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.entities.Brdm2Entity;


import com.redabysslucia.dragonrise_reforge.entities.ChallengerDsEntity;


import com.redabysslucia.dragonrise_reforge.entities.Mv3Entity;


import com.redabysslucia.dragonrise_reforge.entities.Ztz96aEntity;


import com.redabysslucia.dragonrise_reforge.entities.T72b3Entity;


import com.redabysslucia.dragonrise_reforge.entities.Aavc7c1Entity;




import com.redabysslucia.dragonrise_reforge.entities.Aav7a1Entity;


import com.redabysslucia.dragonrise_reforge.entities.MarkvEntity;


import com.redabysslucia.dragonrise_reforge.entities.Zsl10Entity;


import com.redabysslucia.dragonrise_reforge.entities.Zbd05Entity;


import com.redabysslucia.dragonrise_reforge.entities.Ztd05Entity;


import com.redabysslucia.dragonrise_reforge.entities.ZBL08Entity;


import com.redabysslucia.dragonrise_reforge.entities.HumveetowEntity;


import com.redabysslucia.dragonrise_reforge.entities.DarkbearEntity;


import com.redabysslucia.dragonrise_reforge.entities.M1a1hcEntity;


import com.redabysslucia.dragonrise_reforge.entities.M270Entity;


import com.redabysslucia.dragonrise_reforge.entities.Flarakpz1Entity;


import com.redabysslucia.dragonrise_reforge.entities.Leopard2a4Entity;


import com.redabysslucia.dragonrise_reforge.entities.M113Entity;


import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.*;
import com.redabysslucia.dragonrise_reforge.entities.JAS39EEntity;
import com.redabysslucia.dragonrise_reforge.entities.M10BookerEntity;
import com.redabysslucia.dragonrise_reforge.entities.KV1Entity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.NukerBombEntity;
import com.redabysslucia.dragonrise_reforge.entities.atmg.HJ8Entity;
import com.redabysslucia.dragonrise_reforge.entities.atmg.R9M133Entity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.DSHKEntity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.M2Entity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.qjz89Entity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AAshellEntity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Agm65CustomEntity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Aim120Entity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Aim9Entity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AirBomb500kgEntity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.AntiTopWireGuideMissileEntity;
import com.redabysslucia.dragonrise_reforge.entities.projectile.Gbu12Entity;
import com.redabysslucia.dragonrise_reforge.entities.special.CyborgTankEntity;
import com.redabysslucia.dragonrise_reforge.entities.special.ClusterChargeEntity;
import com.redabysslucia.dragonrise_reforge.entities.Ah1fEntity;
import com.redabysslucia.dragonrise_reforge.entities.TerroristEntity;
import com.redabysslucia.dragonrise_reforge.entities.AmmoSupplyStationEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.TestShipEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Dragonrise_reforge.MODID);
    public static final RegistryObject<EntityType<ZTZ99AEntity>> ZTZ99A = register("ztz99a",
            EntityType.Builder.of(ZTZ99AEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<ZTZ99AHEntity>> ZTZ99AH = register("ztz99ah",
            EntityType.Builder.of(ZTZ99AHEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<ZTZ99BHEntity>> ZTZ99BH = register("ztz99bh",
            EntityType.Builder.of(ZTZ99BHEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<VT4A1Entity>> VT4A1 = register("vt4a1",
            EntityType.Builder.of(VT4A1Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<VT4BEntity>> VT4B = register("vt4b",
            EntityType.Builder.of(VT4BEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<ZTQ15Entity>> ZTQ15 = register("ztq15",
            EntityType.Builder.of(ZTQ15Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<WLSCEntity>> WLSC = register("wlsc",
            EntityType.Builder.of(WLSCEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<M1A2SEPV2Entity>> M1A2SEPV2 = register("m1a2sepv2",
            EntityType.Builder.of(M1A2SEPV2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<ZTZ59AEntity>> ZTZ59A = register("ztz59a",
            EntityType.Builder.of(ZTZ59AEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(3.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<AMX56Entity>> AMX56 = register("amx56",
            EntityType.Builder.of(AMX56Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<EC665Entity>> EC665 = register("ec665",
            EntityType.Builder.of(EC665Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<KA50Entity>> KA50 = register("ka50",
            EntityType.Builder.of(KA50Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Z10AEntity>> Z10A = register("z10a",
            EntityType.Builder.of(Z10AEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Z9Entity>> Z9 = register("z9",
            EntityType.Builder.of(Z9Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Z20Entity>> Z20 = register("z20",
            EntityType.Builder.of(Z20Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<UH60Entity>> UH60 = register("uh60",
            EntityType.Builder.of(UH60Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<BMPT72Entity>> BMPT72 = register("bmpt72",
            EntityType.Builder.of(BMPT72Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<motuoEntity>> motuo = register("motuo",
            EntityType.Builder.of(motuoEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(1f, 1f)
    );

    public static final RegistryObject<EntityType<J10CEntity>> J10C = register("j10c",
            EntityType.Builder.of(J10CEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<HYR0Entity>> HYR0 = register("hyr0",
            EntityType.Builder.of(HYR0Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<T80Entity>> T80 = register("t80",
            EntityType.Builder.of(T80Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<T80BEntity>> T80B = register("t80b",
            EntityType.Builder.of(T80BEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<T90MHEntity>> T90MH = register("t90mh",
            EntityType.Builder.of(T90MHEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<S2S38Entity>> S2S38 = register("2s38",
            EntityType.Builder.of(S2S38Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<AA625EEntity>> AA625E = register("625e",
            EntityType.Builder.of(AA625EEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<J10Entity>> J10 = register("j10",
            EntityType.Builder.of(J10Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<JF17Entity>> JF17 = register("jf17",
            EntityType.Builder.of(JF17Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<J35Entity>> J35 = register("j35",
            EntityType.Builder.of(J35Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<J15TEntity>> J15T = register("j15t",
            EntityType.Builder.of(J15TEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<syy651Entity>> syy651 = register("syy651",
            EntityType.Builder.of(syy651Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<REFALEEntity>> REFALE = register("refale",
            EntityType.Builder.of(REFALEEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<REFALEAAEntity>> REFALEAA = register("refaleaa",
            EntityType.Builder.of(REFALEAAEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<AV8BEntity>> AV8B = register("av8b",
            EntityType.Builder.of(AV8BEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<J8Entity>> J8 = register("j8",
            EntityType.Builder.of(J8Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 2.9f)
    );

    public static final RegistryObject<EntityType<Z10MEEntity>> Z10ME = register("z10me",
            EntityType.Builder.of(Z10MEEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<ZBL08Entity>> ZBL08 = register("zbl08",
            EntityType.Builder.of(ZBL08Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<CM34Entity>> CM34 = register("cm34",
            EntityType.Builder.of(CM34Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<ZLT11Entity>> ZLT_11 = register("zlt11",
            EntityType.Builder.of(ZLT11Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<ZBD04AEntity>> ZBD04A = register("zbd04a",
            EntityType.Builder.of(ZBD04AEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<BMP3Entity>> BMP3 = register("bmp3",
            EntityType.Builder.of(BMP3Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<CSK181Entity>> CSK181 = register("csk181",
            EntityType.Builder.of(CSK181Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<HumveeEntity>> HUMVEE = register("humvee",
            EntityType.Builder.of(HumveeEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<CV90Entity>> CV90 = register("cv90",
            EntityType.Builder.of(CV90Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<M3A3Entity>> M3A3 = register("m3a3",
            EntityType.Builder.of(M3A3Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<PANZER4Entity>> PANZER4 = register("panzer4",
            EntityType.Builder.of(PANZER4Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<JAS39EEntity>> JAS39E = register("jas39e",
            EntityType.Builder.of(JAS39EEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<Aim120Entity>> AIM120 = register("aim120",
            EntityType.Builder.<Aim120Entity>of((type, level) -> new Aim120Entity(type, level), MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );

    public static final RegistryObject<EntityType<Agm65CustomEntity>> AGM65 = register("agm65",
            EntityType.Builder.<Agm65CustomEntity>of((type, level) -> new Agm65CustomEntity(type, level), MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );

    public static final RegistryObject<EntityType<Gbu12Entity>> GBU12 = register("gbu_12",
            EntityType.Builder.<Gbu12Entity>of((type, level) -> new Gbu12Entity(type, level), MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );

    public static final RegistryObject<EntityType<Aim9Entity>> AIM9 = register("aim9",
            EntityType.Builder.<Aim9Entity>of((type, level) -> new Aim9Entity(type, level), MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );

    public static final RegistryObject<EntityType<SX1Entity>> SX1 = register("sx1",
            EntityType.Builder.of(SX1Entity::new, MobCategory.MISC)
                    .setTrackingRange(255)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<FAVAEntity>> FAVA = register("fav_a",
            EntityType.Builder.of(FAVAEntity::new, MobCategory.MISC)
                    .setTrackingRange(255)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<STRV103Entity>> STRV103 = register("strv103",
            EntityType.Builder.of(STRV103Entity::new, MobCategory.MISC)
                    .setTrackingRange(255)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.5f)
    );

    public static final RegistryObject<EntityType<F14Entity>> F14 = register("f14",
            EntityType.Builder.of(F14Entity::new, MobCategory.MISC)
                    .setTrackingRange(255)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(5.0f, 3.5f)
    );

    public static final RegistryObject<EntityType<SU24Entity>> SU24 = register("su24",
            EntityType.Builder.of(SU24Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(5.0f, 3.5f)
    );

    public static final RegistryObject<EntityType<SU24MEntity>> SU24M = register("su24m",
            EntityType.Builder.of(SU24MEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(5.0f, 3.5f)
    );

    public static final RegistryObject<EntityType<TYPE100Entity>> TYPE100 = register("type100",
            EntityType.Builder.of(TYPE100Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<J20Entity>> J20 = register("j20",
            EntityType.Builder.of(J20Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<J20VTOLEntity>> J20VTOL = register("j20vtol",
            EntityType.Builder.of(J20VTOLEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<J16Entity>> J16 = register("j16",
            EntityType.Builder.of(J16Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<Q5Entity>> Q5 = register("q5",
            EntityType.Builder.of(Q5Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<MK19Entity>> MK19 = register("mk19",
            EntityType.Builder.of(MK19Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune().sized(0.5f, 1.35f)
    );

    public static final RegistryObject<EntityType<ZU23Entity>> ZU23 = register("zu23",
            EntityType.Builder.of(ZU23Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune().sized(0.5f, 1.35f)
    );

    public static final RegistryObject<EntityType<SHIELDEntity>> SHIELD = register("shield",
            EntityType.Builder.of(com.redabysslucia.dragonrise_reforge.entities.SHIELDEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune().sized(0.5f, 1.35f)
    );

    public static final RegistryObject<EntityType<AmmoSupplyStationEntity>> AMMO_SUPPLY_STATION = register("ammo_supply_station",
            EntityType.Builder.of(AmmoSupplyStationEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(2.0f, 2.0f)
    );

    public static final RegistryObject<EntityType<GeneratorEntity>> GENERATOR = register("generator",
            EntityType.Builder.of(GeneratorEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(2.0f, 2.0f)
    );

    public static final RegistryObject<EntityType<TOYOTASEIKIEntity>> TOYOTASEIKI = register("toyota_seiki",
            EntityType.Builder.of(com.redabysslucia.dragonrise_reforge.entities.TOYOTASEIKIEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune().sized(0.5f, 1.35f)
    );

    public static final RegistryObject<EntityType<CyborgTankEntity>> CYBORG_TANK = register("cyborg_tank",
            EntityType.Builder.of(CyborgTankEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.5f, 2.4f)
    );

    public static final RegistryObject<EntityType<Project640Entity>> PROJECT640 = register("project640",
            EntityType.Builder.of(Project640Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.5f, 2.4f)
    );

    public static final RegistryObject<EntityType<AH64Entity>> AH64 = register("ah64",
            EntityType.Builder.of(AH64Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.5f, 2.4f)
    );

    public static final RegistryObject<EntityType<J11Entity>> J11 = register("j11",
            EntityType.Builder.of(J11Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.5f, 2.4f)
    );

    public static final RegistryObject<EntityType<TESTEntity>> TEST = register("test",
            EntityType.Builder.of(TESTEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.5f, 2.4f)
    );

    public static final RegistryObject<EntityType<SpacebagEntity>> SPACEBAG = register("spacebag",
            EntityType.Builder.of(SpacebagEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(10)
                    .fireImmune()
                    .sized(40.5f, 20.4f)
    );

    public static final RegistryObject<EntityType<CamelEntity>> CAMEL = register("camel",
            EntityType.Builder.of(CamelEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2f, 2f)
    );

    public static final RegistryObject<EntityType<qjz89Entity>> qjz89 = register("qjz89",
            EntityType.Builder.of(qjz89Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );

    public static final RegistryObject<EntityType<M2Entity>> M2 = register("m2",
            EntityType.Builder.of(M2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );

    public static final RegistryObject<EntityType<DSHKEntity>> DSHK = register("dshk",
            EntityType.Builder.of(DSHKEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );
    public static final RegistryObject<EntityType<T3476Entity>> T3476 = register("t3476",
            EntityType.Builder.of(T3476Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<IS2Entity>> IS2 = register("is2",
            EntityType.Builder.of(IS2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<TigerEntity>> TIGER = register("tiger",
            EntityType.Builder.of(TigerEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<ChurchillVIIEntity>> CHURCHILL_VII = register("churchill_vii",
            EntityType.Builder.of(ChurchillVIIEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<CometEntity>> COMET = register("comet",
            EntityType.Builder.of(CometEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<MausEntity>> MAUS = register("maus",
            EntityType.Builder.of(MausEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.5f, 3.0f)
    );

    public static final RegistryObject<EntityType<T3485Entity>> T3485 = register("t3485",
            EntityType.Builder.of(T3485Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<PershingEntity>> PERSHING = register("pershing",
            EntityType.Builder.of(PershingEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<AC130Entity>> AC130 = register("ac130",
            EntityType.Builder.of(AC130Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(6.0f, 4.0f)
    );

    public static final RegistryObject<EntityType<TunguskaEntity>> TUNGUSKA = register("tunguska",
            EntityType.Builder.of(TunguskaEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<WLHGZU23Entity>> WLHGZU23 = register("wlhgzu23",
            EntityType.Builder.of(WLHGZU23Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<TestShipEntity>> TESTSHIP = register("test_ship",
            EntityType.Builder.of(TestShipEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<HJ8Entity>> HJ8 = register("hj8",
            EntityType.Builder.of(HJ8Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(0.35f, 1f)
    );

    public static final RegistryObject<EntityType<R9M133Entity>> R9M133 = register("9m133",
            EntityType.Builder.of(R9M133Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(0.35f, 1.5f)
    );

    public static final RegistryObject<EntityType<BMD4MEntity>> BMD4M = register("bmd4m",
            EntityType.Builder.of(BMD4MEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<NPDS114Entity>> NPDS114 = register("npds114",
            EntityType.Builder.of(NPDS114Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<NPDS514Entity>> NPDS514 = register("npds514",
            EntityType.Builder.of(NPDS514Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<NPDS810Entity>> NPDS810 = register("npds810",
            EntityType.Builder.of(NPDS810Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<SD905Entity>> SD905 = register("sd905",
            EntityType.Builder.of(SD905Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<M10BookerEntity>> M10BOOKER = register("m10booker",
            EntityType.Builder.of(M10BookerEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(3.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<KV1Entity>> KV1 = register("kv1",
            EntityType.Builder.of(KV1Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(3.0f, 2.7f)
    );

    public static final RegistryObject<EntityType<PZBJYEntity>> PZBJY = register("pzbjy",
            EntityType.Builder.of(PZBJYEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<NH90Entity>> NH90 = register("nh90",
            EntityType.Builder.of(NH90Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<R2S25MEntity>> R2S25M = register("2s25m",
            EntityType.Builder.of(R2S25MEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<M4A2Entity>> M4A2 = register("m4a2",
            EntityType.Builder.of(M4A2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<F4UEntity>> F4U = register("f4u",
            EntityType.Builder.of(F4UEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<M4A2105Entity>> M4A2105 = register("m4a2_105",
            EntityType.Builder.of(M4A2105Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<M3StuartEntity>> M3Stuart = register("m3stuart",
            EntityType.Builder.of(M3StuartEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<LVTEntity>> LVT = register("lvt",
            EntityType.Builder.of(LVTEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<type97Entity>> type97 = register("type97",
            EntityType.Builder.of(type97Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<type97QEntity>> type97Q = register("type97q",
            EntityType.Builder.of(type97QEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<type3Entity>> type3 = register("type3",
            EntityType.Builder.of(type3Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<AirBomb500kgEntity>> AIRBOMB500KG = register("airbomb500kg",
            EntityType.Builder.of(AirBomb500kgEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(0.8f, 0.8f)
    );

    public static final RegistryObject<EntityType<AAshellEntity>> AASHELL = register("aashell",
            EntityType.Builder.of(AAshellEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(0.2f, 0.2f)
    );

    public static final RegistryObject<EntityType<AntiTopWireGuideMissileEntity>> ANTI_TOP_WIRE_GUIDE_MISSILE = register("anti_top_wire_guide_missile",
            EntityType.Builder.<AntiTopWireGuideMissileEntity>of((type, level) -> new AntiTopWireGuideMissileEntity(type, level), MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(0.5f, 0.5f)
    );

    public static final RegistryObject<EntityType<AKMEEntity>> AKM = register("akm",
            EntityType.Builder.of(AKMEEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<ZSU234EEntity>> ZSU234 = register("zsu234",
            EntityType.Builder.of(ZSU234EEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<L1A2Entity>> L1A2 = register("l1a2",
            EntityType.Builder.of(L1A2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<F16CEntity>> F16C = register("f16c",
            EntityType.Builder.of(F16CEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<TJGCEntity>> TJGC = register("tjgc",
            EntityType.Builder.of(TJGCEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );

    public static final RegistryObject<EntityType<TerroristEntity>> TERRORIST = register("terrorist",
            EntityType.Builder.of(TerroristEntity::new, MobCategory.MONSTER)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .sized(0.6f, 2f)
    );

    public static final RegistryObject<EntityType<NukerBombEntity>> NUKERBOMB = register("nukerbomb",
            EntityType.Builder.of(NukerBombEntity::new, MobCategory.MONSTER)
                    .setTrackingRange(64)
                    .setUpdateInterval(3)
                    .sized(0.6f, 2f)
    );

    public static final RegistryObject<EntityType<Ah1fEntity>> AH1F = register("ah1f",
            EntityType.Builder.of(Ah1fEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<M113Entity>> M113 = register("m113",
            EntityType.Builder.of(M113Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Leopard2a4Entity>> LEOPARD2A4 = register("leopard2a4",
            EntityType.Builder.of(Leopard2a4Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Flarakpz1Entity>> FLARAKPZ1 = register("flarakpz1",
            EntityType.Builder.of(Flarakpz1Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<M270Entity>> M270 = register("m270",
            EntityType.Builder.of(M270Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<M1a1hcEntity>> M1A1HC = register("m1a1hc",
            EntityType.Builder.of(M1a1hcEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<DarkbearEntity>> DARKBEAR = register("darkbear",
            EntityType.Builder.of(DarkbearEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<HumveetowEntity>> HUMVEETOW = register("humveetow",
            EntityType.Builder.of(HumveetowEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Pak40Entity>> PAK40 = register("pak40",
            EntityType.Builder.of(Pak40Entity::new, MobCategory.MISC)
                    .setTrackingRange(1024)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .sized(3.0f, 3.0f)
    );

    public static final RegistryObject<EntityType<Ztd05Entity>> ZTD05 = register("ztd05",
            EntityType.Builder.of(Ztd05Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Zbd05Entity>> ZBD05 = register("zbd05",
            EntityType.Builder.of(Zbd05Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Zsl10Entity>> ZSL10 = register("zsl10",
            EntityType.Builder.of(Zsl10Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<MarkvEntity>> MARKV = register("markv",
            EntityType.Builder.of(MarkvEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );

    public static final RegistryObject<EntityType<Aav7a1Entity>> AAV7A1 = register("aav7a1",
            EntityType.Builder.of(Aav7a1Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );
    public static final RegistryObject<EntityType<Aavc7c1Entity>> AAVC7C1 = register("aavc7c1",
            EntityType.Builder.of(Aavc7c1Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );
    public static final RegistryObject<EntityType<T72b3Entity>> T72B3 = register("t72b3",
            EntityType.Builder.of(T72b3Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );
    public static final RegistryObject<EntityType<Ztz96aEntity>> ZTZ96A = register("ztz96a",
            EntityType.Builder.of(Ztz96aEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );
    public static final RegistryObject<EntityType<Mv3Entity>> MV3 = register("mv3",
            EntityType.Builder.of(Mv3Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );
    public static final RegistryObject<EntityType<ChallengerDsEntity>> CHALLENGER_DS = register("challenger_ds",
            EntityType.Builder.of(ChallengerDsEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );
    public static final RegistryObject<EntityType<Brdm2Entity>> BRDM2 = register("brdm2",
            EntityType.Builder.of(Brdm2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(4.0f, 2.9f)
    );


    public static final RegistryObject<EntityType<ClusterChargeEntity>> CLUSTER_CHARGE = register("cluster_charge",
            EntityType.Builder.<ClusterChargeEntity>of((type, level) -> new ClusterChargeEntity(type, level), MobCategory.MISC)
                    .setTrackingRange(10)
                    .setUpdateInterval(Integer.MAX_VALUE)
                    .sized(0.5f, 0.5f)
    );

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(name, () -> entityTypeBuilder.build(Dragonrise_reforge.MODID + ":" + name));
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(TERRORIST.get(), TerroristEntity.createAttributes().build());
    }

}
