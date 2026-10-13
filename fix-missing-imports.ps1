# Add every missing class import that javac reports as "cannot find symbol / class X".
#
# The merge left several files (notably ModEntities.java) referencing classes whose
# imports were dropped or never existed on this branch. javac emits one error per
# reference; the symbol name sits on the line after the code line.
#
# Reading the compiler output is tricky because the messages are GBK and come out
# garbled through the shell. What IS stable: the code line itself contains the
# offending type in angle brackets, e.g.
#     public static final DeferredHolder<EntityType<?>, EntityType<motuoEntity>> motuo = register(...
# so this script pulls the first CamelCase identifier out of the generic argument.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Continue'
$wt = "E:\Dragon_Rise_1211"
$srcRoot = Join-Path $wt "src\main\java"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$log = Join-Path $wt "mb6.log"

$lines = Get-Content $log
# group error lines by file
$byFile = @{}
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match '([A-Za-z_0-9]+)\.java:(\d+):') {
        $file = $Matches[1]
        # the code line is the next non-empty line
        for ($j = $i + 1; $j -lt [Math]::Min($i + 4, $lines.Count); $j++) {
            if ($lines[$j] -match '\S') {
                if (-not $byFile.ContainsKey($file)) { $byFile[$file] = @() }
                $byFile[$file] += $lines[$j]
                break
            }
        }
    }
}

$totalAdded = 0
foreach ($file in $byFile.Keys) {
    # find the real path
    $target = Get-ChildItem $srcRoot -Recurse -Filter "$file.java" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $target) { continue }

    $t = [System.IO.File]::ReadAllText($target.FullName)
    $existing = @{}
    foreach ($m in [regex]::Matches($t, '(?m)^import\s+([A-Za-z0-9_.]+);')) { $existing[$m.Groups[1].Value] = 1 }

    $toAdd = New-Object System.Collections.Generic.List[string]
    foreach ($code in $byFile[$file]) {
        # identifiers inside < ... > that look like class names
        foreach ($m in [regex]::Matches($code, '<\s*([A-Z][A-Za-z0-9_]*)\s*>')) {
            $n = $m.Groups[1].Value
            if ($n -in @('EntityType','DeferredHolder','DeferredRegister','Entity','Builder','T','V')) { continue }
            $f = Get-ChildItem $srcRoot -Recurse -Filter "$n.java" -ErrorAction SilentlyContinue | Select-Object -First 1
            if (-not $f) { continue }
            $fq = ($f.FullName -replace [regex]::Escape($srcRoot + '\'), '') -replace '\\', '.' -replace '\.java$', ''
            if ($existing.ContainsKey($fq)) { continue }
            if (-not $toAdd.Contains($fq)) { $toAdd.Add($fq) }
        }
        # also bare "XxxEntity" tokens in extends clauses
        foreach ($m in [regex]::Matches($code, '\b([A-Z][A-Za-z0-9_]*Entity)\b')) {
            $n = $m.Groups[1].Value
            $f = Get-ChildItem $srcRoot -Recurse -Filter "$n.java" -ErrorAction SilentlyContinue | Select-Object -First 1
            if (-not $f) { continue }
            $fq = ($f.FullName -replace [regex]::Escape($srcRoot + '\'), '') -replace '\\', '.' -replace '\.java$', ''
            if ($existing.ContainsKey($fq)) { continue }
            if (-not $toAdd.Contains($fq)) { $toAdd.Add($fq) }
        }
    }

    if ($toAdd.Count -eq 0) { continue }

    # rebuild header
    $curLines = $t -split "`r?`n"
    $bodyStart = -1
    for ($i = 0; $i -lt $curLines.Count; $i++) {
        if ($curLines[$i] -match '^(public\s+)?(final\s+)?(abstract\s+)?class\s|^@') { $bodyStart = $i; break }
    }
    if ($bodyStart -lt 0) { continue }
    $pkg = ($curLines | Where-Object { $_ -match '^package\s' } | Select-Object -First 1)

    $all = New-Object System.Collections.Generic.List[string]
    foreach ($k in $existing.Keys) { $all.Add($k) }
    foreach ($k in $toAdd) { $all.Add($k) }
    $sorted = $all | Sort-Object -Unique

    $out = New-Object System.Collections.Generic.List[string]
    $out.Add($pkg)
    $out.Add('')
    foreach ($ln in $sorted) { $out.Add("import $ln;") }
    $out.Add('')
    for ($i = $bodyStart; $i -lt $curLines.Count; $i++) { $out.Add($curLines[$i]) }

    [System.IO.File]::WriteAllLines($target.FullName, $out, $utf8)
    Write-Output ("{0}: +{1} imports" -f $file, $toAdd.Count)
    $totalAdded += $toAdd.Count
}
Write-Output ("total added: {0}" -f $totalAdded)
