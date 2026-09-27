package com.redabysslucia.dragonrise_reforge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.redabysslucia.dragonrise_reforge.Dragonrise_reforge;
import com.redabysslucia.dragonrise_reforge.entities.utils.DirectionArmorRule;
import com.redabysslucia.dragonrise_reforge.entities.utils.VehicleDirectionArmor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取所有载具 JSON（{@code data/<命名空间>/sbw/vehicles/*.json}，含本模组与卓越前线官方载具）里
 * {@code DamageModifiers} 数组中的 {@code $...} 脚本型条目，解析出方向抗性规则并交给
 * {@link VehicleDirectionArmor} 使用。
 * <p>
 * JSON 写法与官方完全一致，例如：
 * <pre>
 *   "DamageModifiers": [
 *     "minecraft:lava * 2.667",
 *     "$entity.getSourceAngle(source, 0.25) * damage"
 *   ]
 * </pre>
 * 其它非 {@code $} 条目仍由卓越前线自己处理（它们不走脚本，不受其 copy() 丢字段的影响）。
 */
@Mod.EventBusSubscriber(modid = Dragonrise_reforge.MODID)
public class VehicleDamageModifierLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final String DIRECTORY = "sbw/vehicles";

    public VehicleDamageModifierLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<String, List<DirectionArmorRule>> parsed = new HashMap<>();
        int unsupported = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : data.entrySet()) {
            JsonElement element = entry.getValue();
            if (element == null || !element.isJsonObject()) {
                continue;
            }
            JsonObject json = element.getAsJsonObject();
            JsonElement modifiers = json.get("DamageModifiers");
            if (modifiers == null || !modifiers.isJsonArray()) {
                continue;
            }

            JsonArray array = modifiers.getAsJsonArray();
            List<DirectionArmorRule> list = new ArrayList<>();
            for (JsonElement modifier : array) {
                if (!modifier.isJsonPrimitive() || !modifier.getAsJsonPrimitive().isString()) {
                    continue;   // 对象形式的条目由卓越前线处理
                }
                String script = modifier.getAsString();
                if (!script.trim().startsWith("$")) {
                    continue;
                }
                DirectionArmorRule rule = DirectionArmorRule.parse(script);
                if (rule == null) {
                    unsupported++;
                    LOGGER.warn("不支持的 $ 伤害修正脚本，已忽略：{}（载具 {}）", script, entry.getKey());
                } else {
                    list.add(rule);
                }
            }

            if (!list.isEmpty()) {
                // 键与 EntityType 的注册名一致：namespace:path
                parsed.put(entry.getKey().toString(), list);
            }
        }

        VehicleDirectionArmor.setRules(parsed);
        LOGGER.info("载具方向抗性：已加载 {} 辆载具共 {} 条 $ 脚本修正{}",
                VehicleDirectionArmor.vehicleCount(), VehicleDirectionArmor.ruleCount(),
                unsupported > 0 ? "（另有 " + unsupported + " 条形式不支持，已忽略）" : "");
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new VehicleDamageModifierLoader());
    }
}
