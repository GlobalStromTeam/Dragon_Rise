# Resolve ModItems.java: keep the newer 1.20.1 item list, convert Forge types to NeoForge.
#
# The 1.20.1 side of the declaration block is a strict superset of this branch's list
# (it adds IED / PHONE / SPG9_DEPLOYER plus jungle07 / ukrainian / chinese_desert /
# russian_soldier armour sets), so the content is taken from there. Its Forge-only
# types are then rewritten to their 1.21.1 equivalents:
#
#   RegistryObject<Item>     -> DeferredHolder<Item, Item>
#   new ResourceLocation(a,b) -> ResourceLocation.fromNamespaceAndPath(a,b)
#
# The import block is merged by hand: NeoForge registry imports from OURS plus the
# two new item classes the 1.20.1 side introduced.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$p = "E:\Dragon_Rise_1211\src\main\java\com\redabysslucia\dragonrise_reforge\init\ModItems.java"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$lines = [System.IO.File]::ReadAllLines($p)

# collect conflict blocks as (start, mid, end) index triples
$blocks = @()
$s = -1; $mid = -1
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].StartsWith('<<<<<<<')) { $s = $i; $mid = -1 }
    elseif ($s -ge 0 -and $lines[$i] -eq '=======') { $mid = $i }
    elseif ($s -ge 0 -and $lines[$i].StartsWith('>>>>>>>')) {
        $blocks += [pscustomobject]@{ Start = $s; Mid = $mid; End = $i }
        $s = -1; $mid = -1
    }
}
Write-Output ("conflict blocks: {0}" -f $blocks.Count)

$out = New-Object System.Collections.Generic.List[string]

# block 0 is the import block -> custom merge
# blocks 1..n are declaration lists -> take THEIRS
for ($b = 0; $b -lt $blocks.Count; $b++) {
    $blk = $blocks[$b]

    if ($b -eq 0) {
        # NeoForge registry imports from OURS, then the two new item classes
        $out.Add('import net.minecraft.world.item.SpawnEggItem;')
        $out.Add('import net.neoforged.bus.api.IEventBus;')
        $out.Add('import net.neoforged.neoforge.registries.DeferredRegister;')
        $out.Add('import net.neoforged.neoforge.registries.DeferredHolder;')
        $out.Add('import com.redabysslucia.dragonrise_reforge.items.IedItem;')
        $out.Add('import com.redabysslucia.dragonrise_reforge.items.PhoneItem;')
    } else {
        for ($i = $blk.Mid + 1; $i -lt $blk.End; $i++) { $out.Add($lines[$i]) }
    }

    # copy the gap up to the next block (or to EOF)
    $next = if ($b + 1 -lt $blocks.Count) { $blocks[$b + 1].Start } else { $lines.Count }
    for ($i = $blk.End + 1; $i -lt $next; $i++) { $out.Add($lines[$i]) }
}

$text = ($out -join "`n")

# Forge -> NeoForge type conversions
$before = ([regex]::Matches($text, 'RegistryObject')).Count
$text = $text -replace 'RegistryObject<Item>', 'DeferredHolder<Item, Item>'
$text = $text -replace 'new ResourceLocation\(', 'ResourceLocation.fromNamespaceAndPath('

[System.IO.File]::WriteAllText($p, $text, $utf8)
Write-Output ("RegistryObject replaced: {0}" -f $before)
Write-Output ("remaining markers: {0}" -f ([regex]::Matches($text, '<<<<<<<')).Count)
Write-Output ("remaining RegistryObject: {0}" -f ([regex]::Matches($text, 'RegistryObject')).Count)
Write-Output ("remaining new ResourceLocation: {0}" -f ([regex]::Matches($text, 'new ResourceLocation')).Count)
