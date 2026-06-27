package com.redabysslucia.dragonrise_reforge.resource.model;

public class ProjectileModelReloadListener extends BasicModelReloadListener {
    public static final ProjectileModelReloadListener INSTANCE = new ProjectileModelReloadListener();

    private ProjectileModelReloadListener() {
        super("projectile");
    }
}