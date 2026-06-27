package com.redabysslucia.dragonrise_reforge.resource.model;

public class EntityModelReloadListener extends BasicModelReloadListener {
    public static final EntityModelReloadListener INSTANCE = new EntityModelReloadListener();

    private EntityModelReloadListener() {
        super("entity");
    }
}