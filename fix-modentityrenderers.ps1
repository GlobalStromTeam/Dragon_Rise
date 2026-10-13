# Resolve ModEntityRenderers.java: union the imports, take the newer renderer list.
#
# Block 1 is the big renderer-import list (1.20.1 adds spg9 / zsd89a / zsd89a_covered /
# shuangweixie / k1a1 / chonmaho5 / m1a2sepv1 / lcm / t26b / kv2 / ba10 /
# silverhand_mk_iv / m1126 / m1296 / m1128 / mi24p / ik_locust / ik_fightcat).
# Block 2 is the NeoForge-vs-Forge import tail plus three new renderer imports.
# Both are resolved as "NeoForge imports + every new renderer import", then the whole
# file is deduplicated.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$p = "E:\Dragon_Rise_1211\src\main\java\com\redabysslucia\dragonrise_reforge\init\ModEntityRenderers.java"
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
if ($blocks.Count -ne 2) { Write-Output "unexpected block count, aborting"; exit 1 }

$out = New-Object System.Collections.Generic.List[string]

# text before block 1
for ($i = 0; $i -lt $blocks[0].Start; $i++) { $out.Add($lines[$i]) }

# --- block 1: union of renderer imports (every line that is an import, from BOTH sides)
for ($i = $blocks[0].Start + 1; $i -lt $blocks[0].End; $i++) {
    $ln = $lines[$i]
    if ($ln -eq '=======') { continue }
    if ($ln -match '^\s*import\s') {
        # the OURS line has two imports glued together by the merge; split on 'import'
        $parts = [regex]::Matches($ln, 'import\s+[A-Za-z0-9_.]+;') | ForEach-Object { $_.Value }
        foreach ($q in $parts) { $out.Add($q) }
    }
}

# gap between block 1 and block 2
for ($i = $blocks[0].End + 1; $i -lt $blocks[1].Start; $i++) { $out.Add($lines[$i]) }

# --- block 2: NeoForge imports (ours) + the three new renderer imports (theirs)
$out.Add('import net.neoforged.api.distmarker.Dist;')
$out.Add('import net.neoforged.neoforge.client.event.EntityRenderersEvent;')
$out.Add('import net.neoforged.bus.api.SubscribeEvent;')
$out.Add('import net.neoforged.fml.common.Mod;')
$out.Add('import com.redabysslucia.dragonrise_reforge.client.renderer.entity.special.SignalFlareRenderer;')
$out.Add('import com.redabysslucia.dragonrise_reforge.client.renderer.entity.special.StarShellRenderer;')
$out.Add('import com.redabysslucia.dragonrise_reforge.client.renderer.ammo.IedRenderer;')

# text after block 2
for ($i = $blocks[1].End + 1; $i -lt $lines.Count; $i++) { $out.Add($lines[$i]) }

# dedupe imports, drop blanks that duplicate
$seen = @{}
$final = New-Object System.Collections.Generic.List[string]
foreach ($ln in $out) {
    if ($ln -match '^\s*import\s') {
        $k = $ln.Trim()
        if ($seen.ContainsKey($k)) { continue }
        $seen[$k] = 1
        $final.Add($k)
    } else {
        $final.Add($ln)
    }
}

$text = ($final -join "`n")
[System.IO.File]::WriteAllText($p, $text, $utf8)
Write-Output ("imports kept: {0}" -f $seen.Count)
Write-Output ("remaining markers: {0}" -f ([regex]::Matches($text, '<<<<<<<')).Count)
Write-Output ("remaining net.minecraftforge: {0}" -f ([regex]::Matches($text, 'net\.minecraftforge')).Count)
