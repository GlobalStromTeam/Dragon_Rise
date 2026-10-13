# Resolve ModEntities.java: keep the newer 1.20.1 entity list, convert Forge types to NeoForge.
#
# The 1.20.1 side registers 186 entities against this branch's 163 and is a strict
# superset (it adds missile_shell / signal_flare / star_shell / spg9 / zsd89a /
# zsd89a_covered / ied / chomaho5 / k1a1 / ik_locust / ik_fightcat and more), so the
# declaration lists are taken from there. Forge-only types are then rewritten:
#
#   RegistryObject<EntityType<X>>  -> DeferredHolder<EntityType<?>, EntityType<X>>
#   new ResourceLocation(a,b)      -> ResourceLocation.fromNamespaceAndPath(a,b)
#
# Import block is merged by hand (NeoForge registry imports + the new entity classes).
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$p = "E:\Dragon_Rise_1211\src\main\java\com\redabysslucia\dragonrise_reforge\init\ModEntities.java"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$lines = [System.IO.File]::ReadAllLines($p)

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
for ($b = 0; $b -lt $blocks.Count; $b++) {
    $blk = $blocks[$b]
    for ($i = $blk.Mid + 1; $i -lt $blk.End; $i++) { $out.Add($lines[$i]) }
    $next = if ($b + 1 -lt $blocks.Count) { $blocks[$b + 1].Start } else { $lines.Count }
    for ($i = $blk.End + 1; $i -lt $next; $i++) { $out.Add($lines[$i]) }
}
$text = ($out -join "`n")

# Forge -> NeoForge
$n1 = ([regex]::Matches($text, 'RegistryObject<EntityType<')).Count
$text = $text -replace 'RegistryObject<EntityType<([A-Za-z0-9_]+)>>', 'DeferredHolder<EntityType<?>, EntityType<$1>>'
$text = $text -replace 'new ResourceLocation\(', 'ResourceLocation.fromNamespaceAndPath('
# NeoForge registry imports
$text = $text -replace 'import net\.minecraftforge\.event\.entity\.EntityAttributeCreationEvent;', 'import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;'
$text = $text -replace 'import net\.minecraftforge\.eventbus\.api\.SubscribeEvent;', 'import net.neoforged.bus.api.SubscribeEvent;'
$text = $text -replace 'import net\.minecraftforge\.registries\.DeferredRegister;', 'import net.neoforged.neoforge.registries.DeferredRegister;'
$text = $text -replace 'import net\.minecraftforge\.registries\.ForgeRegistries;', 'import net.neoforged.neoforge.registries.NeoForgeRegistries;'
$text = $text -replace 'import net\.minecraftforge\.registries\.RegistryObject;\r?\n', ''

[System.IO.File]::WriteAllText($p, $text, $utf8)
Write-Output ("RegistryObject<EntityType<...>> replaced: {0}" -f $n1)
Write-Output ("remaining markers: {0}" -f ([regex]::Matches($text, '<<<<<<<')).Count)
Write-Output ("remaining RegistryObject: {0}" -f ([regex]::Matches($text, 'RegistryObject')).Count)
Write-Output ("remaining net.minecraftforge: {0}" -f ([regex]::Matches($text, 'net\.minecraftforge')).Count)
