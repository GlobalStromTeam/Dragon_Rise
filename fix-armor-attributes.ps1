# Port the armour attribute-modifier override to the Minecraft 1.21.1 API.
#
# 1.20.1 / Forge style present in these 15 files:
#
#   @Override
#   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
#       Multimap<Attribute, AttributeModifier> map = super.getDefaultAttributeModifiers(slot);
#       UUID uuid = new UUID(slot.toString().hashCode(), 0);
#       if (slot == EquipmentSlot.CHEST) {
#           map = HashMultimap.create(map);
#           map.put(ModAttributes.BULLET_RESISTANCE.get(), new AttributeModifier(uuid, Mod.ATTRIBUTE_MODIFIER,
#                   <factor>, AttributeModifier.Operation.ADDITION));
#       }
#       return map;
#   }
#
# 1.21.1 / NeoForge:
#   - the override takes only the ItemStack and returns the ItemAttributeModifiers component
#   - AttributeModifier is keyed by ResourceLocation, not UUID
#   - the slot is expressed as an EquipmentSlotGroup
#   - Operation.ADDITION was renamed ADD_VALUE
#
# Verified against the SBW 0.8.9.2 jar this branch compiles against:
#   Mod.ATTRIBUTE_MODIFIER                -> net.minecraft.resources.ResourceLocation
#   ModAttributes.BULLET_RESISTANCE       -> DeferredHolder<Attribute, ? extends Attribute>
#
# The 0.5 factor and the durability scaling are preserved exactly. The slot condition is
# replaced by this.getEquipmentSlot() driven EquipmentSlotGroup, which yields the same
# slot for every one of these classes (they each declare a single ArmorItem.Type).
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$wt = "E:\Dragon_Rise_1211"
$armorDir = Join-Path $wt "src\main\java\com\redabysslucia\dragonrise_reforge\item\armor"
$utf8 = New-Object System.Text.UTF8Encoding($false)

$pattern = '(?s)    @Override\r?\n    public Multimap<Attribute, AttributeModifier> getAttributeModifiers\(EquipmentSlot slot, ItemStack stack\) \{.*?\r?\n    \}'

$replacement = @'
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers(stack);
        double factor = 0.5 * Math.max(0, 1 - (double) stack.getDamageValue() / stack.getMaxDamage());
        return base.withModifierAdded(
                ModAttributes.BULLET_RESISTANCE.get(),
                new AttributeModifier(Mod.ATTRIBUTE_MODIFIER, factor, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(this.getEquipmentSlot()));
    }
'@

$done = 0
$missed = @()
foreach ($f in (Get-ChildItem $armorDir -Recurse -Filter *.java)) {
    $text = [System.IO.File]::ReadAllText($f.FullName)
    if ($text -notmatch 'getAttributeModifiers\(EquipmentSlot') { continue }

    $new = [regex]::Replace($text, $pattern, $replacement.TrimEnd("`r", "`n"), 1)
    if ($new -eq $text) { $missed += $f.Name; continue }

    # imports: add the 1.21.1 attribute component types, drop the ones no longer used
    if ($new -notmatch 'import net\.minecraft\.world\.item\.component\.ItemAttributeModifiers;') {
        $new = $new -replace '(?m)^(import net\.minecraft\.world\.item\.ItemStack;)', "import net.minecraft.world.item.component.ItemAttributeModifiers;`r`n`$1"
    }
    if ($new -notmatch 'import net\.minecraft\.world\.entity\.ai\.attributes\.EquipmentSlotGroup;') {
        $new = $new -replace '(?m)^(import net\.minecraft\.world\.entity\.EquipmentSlot;)', "import net.minecraft.world.entity.ai.attributes.EquipmentSlotGroup;`r`n`$1"
    }
    # these three are no longer referenced after the rewrite
    $new = $new -replace '(?m)^import com\.google\.common\.collect\.HashMultimap;\r?\n', ''
    $new = $new -replace '(?m)^import com\.google\.common\.collect\.Multimap;\r?\n', ''
    $new = $new -replace '(?m)^import java\.util\.UUID;\r?\n', ''
    # net.minecraft.world.entity.EquipmentSlot may now be unused; keep only if still referenced
    if ($new -notmatch 'EquipmentSlot\b(?!Group)' -or ([regex]::Matches($new, 'EquipmentSlot(?!Group)')).Count -le 1) {
        $new = $new -replace '(?m)^import net\.minecraft\.world\.entity\.EquipmentSlot;\r?\n', ''
    }

    [System.IO.File]::WriteAllText($f.FullName, $new, $utf8)
    $done++
}

Write-Output ("rewritten: {0}" -f $done)
if ($missed.Count -gt 0) { Write-Output ("not matched: {0}" -f ($missed -join ', ')) }
