package com.redabysslucia.dragonrise_reforge.client.gui;

import com.atsuishio.superbwarfare.data.vehicle.DefaultVehicleData;
import com.atsuishio.superbwarfare.data.vehicle.VehicleData;
import com.atsuishio.superbwarfare.item.container.ContainerBlockItem;
import com.redabysslucia.dragonrise_reforge.init.ModTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 创意物品栏左侧的国家筛选侧栏。
 *
 * <p>侧栏本身只是“显示 + 命中 + 把当前选中的国家写进静态状态”；
 * 真正把物品列表裁掉的是 {@link #applyFilter}，它由
 * {@code CreativeInventorySideTabsMixin} 在 selectTab / refreshSearchResults
 * / refreshCurrentTabContents 之后调用。</p>
 *
 * <p>国家归属不额外维护表：载具物品是 ContainerBlockItem，NBT 里带
 * {@code BlockEntityTag.EntityType}，用它查 Superb Warfare 的
 * {@code DefaultVehicleData#getContainerIcon()}，图标路径形如
 * {@code textures/gui/vehicle/type/cn_land.png}，前缀就是国家码。
 * 拿不到数据时回退去读模组自带的 {@code data/<ns>/sbw/vehicles/<id>.json}。</p>
 */
public final class VehicleCountryTabs {

    public static final int TAB_W = 32;
    public static final int TAB_H = 26;
    /** 图标显示框：16x10，横向居中、纵向居中；贴的是原图像素，不做预缩放 */
    public static final int ICON_X = 8;
    public static final int ICON_Y = 8;
    public static final int ICON_W = 16;
    public static final int ICON_H = 10;
    /** 第一枚侧栏顶端相对面板顶部的下移量，避开顶部的原版 tab 条 */
    public static final int TOP_INSET = 4;
    /** 侧栏整体右移，让 tab 压住面板左边缘 4px（和原版上挂 tab 压面板一个道理） */
    public static final int RIGHT_SHIFT = 4;

    private static final ResourceLocation FRAME =
            ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/gui/tab/side_tabs.png");

    /** 顺序即竖排顺序：条目多的、常用的在前，靠后的国家会排到第二列。 */
    public static final List<Country> COUNTRIES = List.of(
            new Country("all", "全部载具", 16, 10),
            new Country("cn", "中国", 128, 80),
            new Country("us", "美国", 128, 80),
            new Country("ru", "俄罗斯", 128, 80),
            new Country("eu", "欧洲", 128, 80)
    );

    private static final Pattern CONTAINER_ICON =
            Pattern.compile("\"ContainerIcon\"\\s*:\\s*\"([^\"]+)\"");

    private static final Map<String, Set<String>> BY_ENTITY = new HashMap<>();

    private static String current = "all";

    private static CreativeModeTab mainTab;

    private VehicleCountryTabs() {
    }

    /** srcW/srcH 是这张 png 的真实像素尺寸，贴图时按它算 UV，不做任何预缩放。 */
    public record Country(String code, String displayName, int srcW, int srcH) {
        public ResourceLocation icon() {
            return ResourceLocation.fromNamespaceAndPath("dragonrise_reforge", "textures/gui/tab/tab_" + code + ".png");
        }
    }

    // ------------------------------------------------------------------ 状态

    public static String getCurrent() {
        return current;
    }

    public static void setCurrent(String code) {
        current = code == null ? "all" : code;
        saveState();
    }

    // ------------------------------------------------- 选择状态持久化

    private static boolean bootstrapped;
    private static Path stateFile;

    /**
     * 选择状态的生命周期：本次游戏进程内一直保留（关背包、切标签页、换维度、退世界再进都不重置），
     * 只有在「退出游戏」时才清掉 —— 关闭客户端时由 JVM 关钩子删除状态文件，
     * 所以下次启动一定是「全部」。进程被强杀时钩子不会执行，文件会留下，下次启动原样恢复。
     */
    private static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;
        try {
            stateFile = FMLPaths.CONFIGDIR.get().resolve("dragonrise_reforge_tab_selection.json");
            Runtime.getRuntime().addShutdownHook(new Thread(VehicleCountryTabs::clearState, "dragonrise-tab-state"));
            if (Files.exists(stateFile)) {
                String json = Files.readString(stateFile, StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("selected")) {
                    String saved = obj.get("selected").getAsString();
                    if (COUNTRIES.stream().anyMatch(c -> c.code().equals(saved))) {
                        current = saved;
                    }
                }
            }
        } catch (Throwable ignored) {
            stateFile = null;
        }
    }

    private static void saveState() {
        bootstrap();
        if (stateFile == null) {
            return;
        }
        try {
            Files.createDirectories(stateFile.getParent());
            JsonObject obj = new JsonObject();
            obj.addProperty("selected", current);
            Files.writeString(stateFile, obj.toString(), StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
            // 写不了就退化成纯内存状态
        }
    }

    private static void clearState() {
        try {
            if (stateFile != null) {
                Files.deleteIfExists(stateFile);
            }
        } catch (Throwable ignored) {
            // 忽略
        }
    }

    /** 侧栏只挂在「龍之崛起:坦克」这一个标签页上，二战页 / 物品页都不显示。 */
    public static boolean isActive(@Nullable CreativeModeTab tab) {
        if (tab == null) {
            return false;
        }
        if (mainTab == null) {
            mainTab = getOrNull(ModTabs.MBT_TAB);
        }
        return mainTab != null && mainTab == tab;
    }

    private static CreativeModeTab getOrNull(DeferredHolder<CreativeModeTab, CreativeModeTab> ro) {
        try {
            return ro == null ? null : ro.get();
        } catch (Throwable ignored) {
            return null;
        }
    }

    // -------------------------------------------------------------- 绘制/命中

    /**
     * 一列放几枚：先按面板高度算，如果排出来的列数往左放不下屏幕，
     * 再把每一列压高一点，保证不会跑到窗口外面去。
     */
    public static int capacity(CreativeModeInventoryScreen screen) {
        int byHeight = Math.max(1, (screen.getYSize() - TOP_INSET) / TAB_H);
        int maxCols = Math.max(1, (screen.getGuiLeft() + RIGHT_SHIFT) / TAB_W);
        int needCols = (COUNTRIES.size() + byHeight - 1) / byHeight;
        if (needCols > maxCols) {
            byHeight = (COUNTRIES.size() + maxCols - 1) / maxCols;
        }
        return byHeight;
    }

    public static int tabX(CreativeModeInventoryScreen screen, int index) {
        return screen.getGuiLeft() - TAB_W * (index / capacity(screen) + 1) + RIGHT_SHIFT;
    }

    public static int tabY(CreativeModeInventoryScreen screen, int index) {
        return screen.getGuiTop() + TOP_INSET + (index % capacity(screen)) * TAB_H;
    }

    /**
     * 未选中的 tab：画在面板之前，多出来的 4px 会被面板左框压住，
     * 看上去就是「塞在面板下面」。
     */
    public static void renderBehind(CreativeModeInventoryScreen screen, GuiGraphics gg) {
        bootstrap();
        String cur = current;
        for (int i = 0; i < COUNTRIES.size(); i++) {
            if (!COUNTRIES.get(i).code().equals(cur)) {
                drawTab(screen, gg, i, false);
            }
        }
    }

    /** 选中那枚：最后画，压在面板之上；tooltip 也在这里出。 */
    public static void renderSelectedOnTop(CreativeModeInventoryScreen screen, GuiGraphics gg, int mouseX, int mouseY) {
        String cur = current;
        for (int i = 0; i < COUNTRIES.size(); i++) {
            if (COUNTRIES.get(i).code().equals(cur)) {
                drawTab(screen, gg, i, true);
                break;
            }
        }
        String hovered = hitTest(screen, mouseX, mouseY);
        if (hovered != null) {
            gg.renderTooltip(screen.getMinecraft().font,
                    Component.translatable("gui.dragonrise_reforge.tab." + hovered), mouseX, mouseY);
        }
    }

    private static void drawTab(CreativeModeInventoryScreen screen, GuiGraphics gg, int index, boolean selected) {
        int x = tabX(screen, index);
        int y = tabY(screen, index);
        Country c = COUNTRIES.get(index);
        gg.blit(FRAME, x, y, 0, selected ? TAB_H : 0, TAB_W, TAB_H, TAB_W, TAB_H * 2);
        // 原图整张铺进 16x10 的显示框：uWidth/vHeight 直接用图片自身尺寸
        gg.blit(c.icon(), x + ICON_X, y + ICON_Y, ICON_W, ICON_H,
                0.0F, 0.0F, c.srcW(), c.srcH(), c.srcW(), c.srcH());
    }

    @Nullable
    public static String hitTest(CreativeModeInventoryScreen screen, double mouseX, double mouseY) {
        for (int i = 0; i < COUNTRIES.size(); i++) {
            int x = tabX(screen, i);
            int y = tabY(screen, i);
            if (mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < y + TAB_H) {
                return COUNTRIES.get(i).code();
            }
        }
        return null;
    }

    // -------------------------------------------------------------- 过滤逻辑

    /**
     * 把菜单里的物品列表按当前国家裁一遍。列表已被 {@code selectTab} /
     * {@code refreshSearchResults} 重新填满，所以这里可以放心就地裁。
     */
    public static void applyFilter(CreativeModeInventoryScreen.ItemPickerMenu menu, boolean active) {
        if (!active || menu == null) {
            return;
        }
        String cur = current;
        if ("all".equals(cur)) {
            return;
        }
        List<ItemStack> keep = new ArrayList<>(menu.items.size());
        for (ItemStack stack : menu.items) {
            if (matches(stack, cur)) {
                keep.add(stack);
            }
        }
        if (keep.size() == menu.items.size()) {
            return;
        }
        menu.items.clear();
        menu.items.addAll(keep);
        menu.scrollTo(0.0F);
    }

    public static boolean matches(ItemStack stack, String bucket) {
        if ("all".equals(bucket)) {
            return true;
        }
        return bucketsOf(stack).contains(bucket);
    }

    /** 一辆载具可以同时属于多个桶（红方 = 中国 + 俄罗斯）。 */
    public static Set<String> bucketsOf(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ContainerBlockItem)) {
            return Set.of();
        }
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (tag == null || !tag.contains("EntityType", Tag.TAG_STRING)) {
            return Set.of();
        }
        String entityId = tag.getString("EntityType");
        Set<String> cached = BY_ENTITY.get(entityId);
        if (cached != null) {
            return cached;
        }
        Set<String> resolved = resolveBuckets(entityId);
        BY_ENTITY.put(entityId, resolved);
        return resolved;
    }

    /**
     * 图标前缀 -> 侧栏桶：
     * 中(cn) / 美(us) / 俄(ru) / 欧(gm fr uk se eu) 各归各的；
     * 日(jp)、韩(kr)、蓝方(blue) 并进 美；
     * 红方(red，朝鲜那类) 同时进 中 和 俄；
     * 其余（虚构 dr、工事 defense/civilian/land/aircraft、无图标）只出现在「全部」。
     */
    private static Set<String> resolveBuckets(String entityId) {
        ResourceLocation icon = containerIcon(entityId);
        String nation = nationOf(icon == null ? "" : icon.getPath());
        return switch (nation) {
            case "cn" -> Set.of("cn");
            case "us" -> Set.of("us");
            case "ru", "ussr" -> Set.of("ru");
            case "gm", "fr", "uk", "se", "eu" -> Set.of("eu");
            case "jp", "kr", "blue" -> Set.of("us");
            case "red" -> Set.of("cn", "ru");
            default -> Set.of();
        };
    }

    /** 优先问 Superb Warfare 的载具数据，拿不到再回退读本模组的载具 json。 */
    @Nullable
    private static ResourceLocation containerIcon(String entityId) {
        try {
            Optional<EntityType<?>> type = EntityType.byString(entityId);
            if (type.isPresent()) {
                DefaultVehicleData data = VehicleData.getDefault(type.get());
                if (data != null) {
                    ResourceLocation icon = data.getContainerIcon();
                    if (icon != null) {
                        return icon;
                    }
                }
            }
        } catch (Throwable ignored) {
            // 数据没同步/实体未注册，走 json 回退
        }
        return readIconFromJson(entityId);
    }

    @Nullable
    private static ResourceLocation readIconFromJson(String entityId) {
        try {
            ResourceLocation id = ResourceLocation.parse(entityId);
            ResourceLocation file = ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(), "sbw/vehicles/" + id.getPath() + ".json");
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(file);
            if (resource.isEmpty()) {
                return null;
            }
            String text;
            try (BufferedReader reader = resource.get().openAsReader()) {
                StringBuilder sb = new StringBuilder(4096);
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) > 0) {
                    sb.append(buf, 0, n);
                }
                text = sb.toString();
            }
            Matcher matcher = CONTAINER_ICON.matcher(text);
            if (matcher.find()) {
                return ResourceLocation.parse(matcher.group(1));
            }
        } catch (Throwable ignored) {
            // 读不到就当杂项
        }
        return null;
    }

    /** {@code textures/gui/vehicle/type/cn_land.png} -> {@code cn} */
    public static String nationOf(String iconPath) {
        String file = iconPath;
        int slash = file.lastIndexOf('/');
        if (slash >= 0) {
            file = file.substring(slash + 1);
        }
        int dot = file.lastIndexOf('.');
        if (dot > 0) {
            file = file.substring(0, dot);
        }
        file = file.toLowerCase(Locale.ROOT);
        int underscore = file.indexOf('_');
        String nation = underscore > 0 ? file.substring(0, underscore) : file;
        return nation;
    }
}
