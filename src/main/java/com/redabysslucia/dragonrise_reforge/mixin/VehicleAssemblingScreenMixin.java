package com.redabysslucia.dragonrise_reforge.mixin;

import com.atsuishio.superbwarfare.client.screens.VehicleAssemblingScreen;
import com.atsuishio.superbwarfare.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import software.bernie.geckolib.animatable.GeoItem;

@Mixin(value = VehicleAssemblingScreen.class, remap = false)
public abstract class VehicleAssemblingScreenMixin {

    /**
     * 拦截 renderDefaultItemModel 中对 ItemRenderer.renderStatic 的调用。
     * 对于 GeoItem（护甲），用 GeoItemRenderer (BER) 渲染 3D 模型；
     * 其他物品保持原版平坦图标渲染。
     */
    @Redirect(
            method = "renderDefaultItemModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V",
                    remap = false
            )
    )
    private void redirectRenderStatic(ItemRenderer instance, ItemStack stack, ItemDisplayContext displayContext,
                                      int packedLight, int packedOverlay, PoseStack poseStack,
                                      net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                      net.minecraft.world.level.Level level, int seed) {
        if (stack.getItem() instanceof GeoItem && !stack.is(ModItems.CONTAINER.get())) {
            IClientItemExtensions extensions = IClientItemExtensions.of(stack);
            BlockEntityWithoutLevelRenderer ber = extensions.getCustomRenderer();
            if (ber != null) {
                ber.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
                return;
            }
        }
        instance.renderStatic(stack, displayContext, packedLight, packedOverlay, poseStack, bufferSource, level, seed);
    }
}
