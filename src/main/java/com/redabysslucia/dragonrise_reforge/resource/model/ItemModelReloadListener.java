package com.redabysslucia.dragonrise_reforge.resource.model;

public class ItemModelReloadListener extends BasicModelReloadListener {
    public static final ItemModelReloadListener INSTANCE = new ItemModelReloadListener();

    private ItemModelReloadListener() {
        super("item");
    }
}