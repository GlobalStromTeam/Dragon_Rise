# Restore the import sections that the conflict resolution dropped.
#
# fix-modentities.ps1 / fix-moditems.ps1 / fix-modentityrenderers.ps1 replaced the
# whole conflict block with one side's text. For ModEntities that block also carried
# the entire import list (plus the package declaration), so the resolved file ended up
# with only 5 imports and no `package` line.
#
# This script rebuilds the header as:
#   package <pkg>;
#   <the NeoForge/Minecraft imports from the resolved file>
#   <every import from the pre-merge HEAD>
# then re-attaches the body (everything from the first non-import, non-blank line that
# starts the class declaration).
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$wt = "E:\Dragon_Rise_1211"
$baseSha = '8ec38856'   # pre-merge tip of 1.21.1/main
$utf8 = New-Object System.Text.UTF8Encoding($false)

$targets = @(
    'src/main/java/com/redabysslucia/dragonrise_reforge/init/ModEntities.java',
    'src/main/java/com/redabysslucia/dragonrise_reforge/init/ModItems.java',
    'src/main/java/com/redabysslucia/dragonrise_reforge/init/ModEntityRenderers.java'
)

foreach ($rel in $targets) {
    $p = Join-Path $wt ($rel -replace '/','\')
    if (-not (Test-Path $p)) { Write-Output "MISSING $rel"; continue }

    $cur = [System.IO.File]::ReadAllText($p)
    $curLines = $cur -split "`r?`n"

    # The class body starts at the first line containing the class declaration.
    $bodyStart = -1
    for ($i = 0; $i -lt $curLines.Count; $i++) {
        if ($curLines[$i] -match '^(public\s+)?(final\s+)?class\s') { $bodyStart = $i; break }
    }
    if ($bodyStart -lt 0) { Write-Output "no class decl in $rel"; continue }

    # NeoForge/Minecraft imports still present in the resolved file
    $kept = @()
    foreach ($ln in $curLines[0..($bodyStart-1)]) {
        if ($ln -match '^import\s') { $kept += $ln.Trim() }
    }

    # imports from the pre-merge HEAD
    $headText = (& git -C $wt show "${baseSha}:$rel" 2>&1) -join "`n"
    $headImports = [regex]::Matches($headText, '(?m)^import\s+[A-Za-z0-9_.]+;') | ForEach-Object { $_.Value }

    # package declaration
    $pkg = [regex]::Match($headText, '(?m)^package\s+[A-Za-z0-9_.]+;').Value
    if (-not $pkg) { $pkg = [regex]::Match($cur, '(?m)^package\s+[A-Za-z0-9_.]+;').Value }
    if (-not $pkg) { Write-Output "no package for $rel"; continue }

    # merge: NeoForge imports first, then HEAD imports, deduped
    $seen = @{}
    $imports = New-Object System.Collections.Generic.List[string]
    foreach ($ln in ($kept + $headImports)) {
        $k = $ln.Trim()
        if ($k -eq '' -or $seen.ContainsKey($k)) { continue }
        $seen[$k] = 1
        $imports.Add($k)
    }
    $imports = $imports | Sort-Object

    $out = New-Object System.Collections.Generic.List[string]
    $out.Add($pkg)
    $out.Add('')
    foreach ($ln in $imports) { $out.Add($ln) }
    $out.Add('')
    for ($i = $bodyStart; $i -lt $curLines.Count; $i++) { $out.Add($curLines[$i]) }

    [System.IO.File]::WriteAllLines($p, $out, $utf8)
    Write-Output ("rebuilt header: {0}  (imports {1} -> {2})" -f (Split-Path $rel -Leaf), $kept.Count, $imports.Count)
}
