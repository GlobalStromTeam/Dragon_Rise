# Add the entity imports that ModEntities.java lost during conflict resolution.
#
# The pre-merge HEAD of this file (8ec38856) was mid-port and its import list was
# incomplete, so rebuild-header.ps1 (fix-imports.ps1) restored 74 imports but many
# entity classes referenced by the 186 registrations were never listed. javac reports
# them as "cannot find symbol / class <Name>".
#
# This script harvests those names from the compile log, resolves each one to its real
# package under src/main/java, and inserts the missing imports.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$wt = "E:\Dragon_Rise_1211"
$p = Join-Path $wt "src\main\java\com\redabysslucia\dragonrise_reforge\init\ModEntities.java"
$srcRoot = Join-Path $wt "src\main\java"
$log = Join-Path $wt "mb4.log"
$utf8 = New-Object System.Text.UTF8Encoding($false)

$lines = Get-Content $log
$names = @{}
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match 'ModEntities\.java:\d+') {
        for ($j = $i + 1; $j -lt [Math]::Min($i + 5, $lines.Count); $j++) {
            foreach ($m in [regex]::Matches($lines[$j], '\b([A-Z][A-Za-z0-9_]*)\b')) {
                $names[$m.Groups[1].Value] = 1
            }
        }
    }
}
Write-Output ("candidate symbols from log: {0}" -f $names.Count)

$cur = [System.IO.File]::ReadAllText($p)
$existing = @{}
foreach ($m in [regex]::Matches($cur, '(?m)^import\s+([A-Za-z0-9_.]+);')) {
    $existing[$m.Groups[1].Value] = 1
}

$toAdd = New-Object System.Collections.Generic.List[string]
foreach ($n in ($names.Keys | Sort-Object)) {
    if ($n -in @('ModEntities','Entity','EntityType','MobCategory','DeferredHolder','DeferredRegister','Registries','Builder')) { continue }
    $f = Get-ChildItem $srcRoot -Recurse -Filter "$n.java" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $f) { continue }
    $fq = ($f.FullName -replace [regex]::Escape($srcRoot + '\'), '') -replace '\\', '.' -replace '\.java$', ''
    if ($existing.ContainsKey($fq)) { continue }
    if ($toAdd -contains $fq) { continue }
    $toAdd.Add($fq)
}
Write-Output ("imports to add: {0}" -f $toAdd.Count)

# rebuild: package, blank, sorted imports, blank, body
$curLines = $cur -split "`r?`n"
$bodyStart = -1
for ($i = 0; $i -lt $curLines.Count; $i++) {
    if ($curLines[$i] -match '^(public\s+)?(final\s+)?class\s') { $bodyStart = $i; break }
}
$pkg = ($curLines | Where-Object { $_ -match '^package\s' } | Select-Object -First 1)

$allImports = New-Object System.Collections.Generic.List[string]
foreach ($k in $existing.Keys) { $allImports.Add($k) }
foreach ($k in $toAdd) { $allImports.Add($k) }
$sorted = $allImports | Sort-Object -Unique

$out = New-Object System.Collections.Generic.List[string]
$out.Add($pkg)
$out.Add('')
foreach ($ln in $sorted) { $out.Add("import $ln;") }
$out.Add('')
for ($i = $bodyStart; $i -lt $curLines.Count; $i++) { $out.Add($curLines[$i]) }

[System.IO.File]::WriteAllLines($p, $out, $utf8)
Write-Output ("total imports now: {0}" -f $sorted.Count)
