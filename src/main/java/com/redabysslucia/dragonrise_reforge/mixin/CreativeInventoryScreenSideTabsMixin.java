package com.redabysslucia.dragonrise_reforge.mixin;

import com.redabysslucia.dragonrise_reforge.client.gui.VehicleCountryTabs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/**
 * 把国家筛选侧栏接进原版创意物品栏。
 *
 * <ul>
 *   <li>{@code render} 尾：画左侧竖排 tab（框用 side_tabs.png，原版 tabs.png 派生）；</li>
 *   <li>{@code mouseClicked} 头：命中侧栏就换国家，重填一次物品列表并吃掉这次点击；</li>
 *   <li>{@code selectTab} / {@code refreshSearchResults} / {@code refreshCurrentTabContents} 尾：
 *       原版刚把 items 填满，紧接着按当前国家裁一刀。</li>
 * </ul>
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenSideTabsMixin {

    // 本工程的 refmap 只输出方法映射，字段靠 aliases 直接钉 SRG 名（与 RemoteAircraftCameraMixin 等同一写法）
    @Shadow(aliases = {"f_98507_"})
    private static CreativeModeTab selectedTab;

    @Shadow(aliases = {"f_98510_"})
    private EditBox searchBox;

    @Shadow(aliases = {"f_98508_"})
    private float scrollOffs;

    /** 未选中的侧栏 tab：画在面板之前，交叠的 4px 由面板盖住。 */
    @Inject(method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V", at = @At("HEAD"))
    private void dragonrise$renderSideTabsBehind(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        if (!VehicleCountryTabs.isActive(selectedTab)) {
            return;
        }
        VehicleCountryTabs.renderBehind(self, guiGraphics);
    }

    /** 选中的那枚 + tooltip：画在整个界面之后，压在面板上方。 */
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
    private void dragonrise$renderActiveTabOnTop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        if (!VehicleCountryTabs.isActive(selectedTab)) {
            return;
        }
        VehicleCountryTabs.renderSelectedOnTop(self, guiGraphics, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"), cancellable = true)
    private void dragonrise$clickSideTab(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        if (!VehicleCountryTabs.isActive(selectedTab)) {
            return;
        }
        String code = VehicleCountryTabs.hitTest(self, mouseX, mouseY);
        if (code == null) {
            return;
        }
        VehicleCountryTabs.setCurrent(code);
        // 清掉搜索框，否则会出现「搜索词还在、列表却是整页」的错位
        this.searchBox.setValue("");
        this.dragonrise$refillItems();
        cir.setReturnValue(true);
    }

    @Inject(method = "refreshSearchResults()V", at = @At("TAIL"))
    private void dragonrise$filterAfterSearch(CallbackInfo ci) {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        VehicleCountryTabs.applyFilter(self.getMenu(), VehicleCountryTabs.isActive(selectedTab));
    }

    @Inject(method = "selectTab(Lnet/minecraft/world/item/CreativeModeTab;)V", at = @At("TAIL"))
    private void dragonrise$filterAfterSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        VehicleCountryTabs.applyFilter(self.getMenu(), VehicleCountryTabs.isActive(selectedTab));
    }

    @Inject(method = "refreshCurrentTabContents(Ljava/util/Collection;)V", at = @At("TAIL"))
    private void dragonrise$filterAfterRefreshTab(Collection<ItemStack> stacks, CallbackInfo ci) {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        VehicleCountryTabs.applyFilter(self.getMenu(), VehicleCountryTabs.isActive(selectedTab));
    }

    /** 用当前标签页的完整条目重填一遍，再按国家裁。 */
    private void dragonrise$refillItems() {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        CreativeModeInventoryScreen.ItemPickerMenu menu = self.getMenu();
        menu.items.clear();
        menu.items.addAll(selectedTab.getDisplayItems());
        VehicleCountryTabs.applyFilter(menu, true);
        this.scrollOffs = 0.0F;
        menu.scrollTo(0.0F);
    }
}
