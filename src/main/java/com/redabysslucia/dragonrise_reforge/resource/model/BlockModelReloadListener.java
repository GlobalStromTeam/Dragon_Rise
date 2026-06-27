package com.redabysslucia.dragonrise_reforge.resource.model;

public class BlockModelReloadListener extends BasicModelReloadListener {
    public static final BlockModelReloadListener INSTANCE = new BlockModelReloadListener();

    private BlockModelReloadListener() {
        super("block");
    }
}