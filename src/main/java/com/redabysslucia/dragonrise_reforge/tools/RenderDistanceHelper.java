package com.redabysslucia.dragonrise_reforge.tools;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderDistanceHelper {
    private static long GUI_RENDER_TIMESTAMP = -1;

    public static void markGuiRenderTimestamp() {
        GUI_RENDER_TIMESTAMP = System.currentTimeMillis();
    }

    public static boolean isInGui() {
        return System.currentTimeMillis() - GUI_RENDER_TIMESTAMP < 100L;
    }

    public static boolean shouldRenderLOD(PoseStack poseStack, double distance) {
        if (isInGui()) return false;
        int globalLODDistance = 32;
        if (distance < globalLODDistance) return false;
        var matrix = poseStack.last().pose();
        double viewDistance = matrix.m30() * matrix.m30() + matrix.m31() * matrix.m31() + matrix.m32() * matrix.m32();
        return viewDistance >= distance * distance;
    }
}