package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.*;
import com.redabysslucia.dragonrise_reforge.entities.JAS39EEntity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.DSHKEntity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.M2Entity;
import com.redabysslucia.dragonrise_reforge.entities.hmg.qjz89Entity;
import com.redabysslucia.dragonrise_reforge.entities.special.CyborgTankEntity;
import com.redabysslucia.dragonrise_reforge.entities.utils.TestShipEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
                    .sized(4.0f, 2.9f)
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


    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(name, () -> entityTypeBuilder.build(name));
    }

}