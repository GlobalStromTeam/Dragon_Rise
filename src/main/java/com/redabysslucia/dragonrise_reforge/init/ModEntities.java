package com.redabysslucia.dragonrise_reforge.init;

import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.*;
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

      public static final RegistryObject<EntityType<M1A2SEPV2Entity>> M1A2SEPV2 = register("m1a2sepv2",
              EntityType.Builder.of(M1A2SEPV2Entity::new, MobCategory.MISC)
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

        public static final RegistryObject<EntityType<T80Entity>> T80 = register("t80",
                EntityType.Builder.of(T80Entity::new, MobCategory.MISC)
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

        public static final RegistryObject<EntityType<SX1Entity>> SX1 = register("sx1",
            EntityType.Builder.of(SX1Entity::new, MobCategory.MISC)
                    .setTrackingRange(255)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
        );

    public static final RegistryObject<EntityType<STRV103Entity>> STRV103 = register("strv103",
            EntityType.Builder.<STRV103Entity>of(STRV103Entity::new, MobCategory.MISC)
                    .setTrackingRange(255)
                    .setUpdateInterval(2)
                    .fireImmune()
                    .sized(2.0f, 1.7f)
    );



        private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> entityTypeBuilder) {
                return REGISTRY.register(name, () -> entityTypeBuilder.build(name));
        }

}