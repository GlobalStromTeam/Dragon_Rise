param()
# Merge per-tank specific fire animation bones into animation.cannon.fire
# (game plays "animation.cannon.fire" because weapon slot name is "Cannon")
$tanks = @("t80","vt4a1","vt4b","ztz99a","2s25m","2s38","m1a2sepv2","m10booker","ztq15","syy651","hyr0","cyborg_tank","tunguska","ztz99bh")
$base = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$warnings = @()

foreach ($t in $tanks) {
    $file = "$base\bedrock\vehicle\$t.animation.json"
    if (-not (Test-Path $file)) { $file = "$base\$t.animation.json" }
    if (-not (Test-Path $file)) { $warnings += "$t : animation file not found"; continue }

    $json = [System.IO.File]::ReadAllText($file)
    $a = $json | ConvertFrom-Json
    $animations = $a.animations

    $specificKey = $null
    foreach ($k in $animations.PSObject.Properties.Name) {
        if ($k -eq "animation.$t.fire") { $specificKey = $k; break }
    }
    if ($null -eq $specificKey) {
        $badKey = "$t.animation.fire"
        if ($animations.PSObject.Properties.Name -contains $badKey) {
            $renamed = "animation.$t.fire"
            $props = $animations.PSObject.Properties
            $newAnims = [ordered]@{}
            foreach ($p in $props) {
                $nk = if ($p.Name -eq $badKey) { $renamed } else { $p.Name }
                $newAnims[$nk] = $p.Value
            }
            $animations = [pscustomobject]$newAnims
            $specificKey = $renamed
            Write-Output "$t : renamed bad key $badKey -> $renamed"
        } else {
            $warnings += "$t : no specific key animation.$t.fire"
            continue
        }
    }

    $cannonKey = "animation.cannon.fire"
    $specific = $animations.$specificKey
    $specificBones = $specific.bones.PSObject.Properties.Name

    if ($null -eq $specificBones -or $specificBones.Count -eq 0) {
        $warnings += "$t : specific key $specificKey has no bones, skipped"
        continue
    }

    $cannonBones = [ordered]@{}
    $cannonLen = 0.15
    $cannonLoop = "hold_on_last_frame"
    $hasCannon = $animations.PSObject.Properties.Name -contains $cannonKey
    if ($hasCannon) {
        $c = $animations.$cannonKey
        foreach ($p in $c.bones.PSObject.Properties) { $cannonBones[$p.Name] = $p.Value }
        if ($null -ne $c.animation_length) { $cannonLen = $c.animation_length }
        if ($null -ne $c.loop) { $cannonLoop = $c.loop }
    }

    $conflicts = @()
    foreach ($b in $specificBones) {
        if ($cannonBones.Contains($b)) { $conflicts += $b } else { $cannonBones[$b] = $specific.bones.$b }
    }
    $specificLen = if ($null -ne $specific.animation_length) { $specific.animation_length } else { 0.0 }
    $newLen = [Math]::Max([double]$cannonLen, [double]$specificLen)

    $newCannon = [ordered]@{}
    $newCannon["loop"] = $cannonLoop
    $newCannon["animation_length"] = [double]$newLen
    $newCannon["bones"] = [pscustomobject]$cannonBones

    $props = $animations.PSObject.Properties
    $newAnims2 = [ordered]@{}
    foreach ($p in $props) { $newAnims2[$p.Name] = $p.Value }
    $newAnims2[$cannonKey] = [pscustomobject]$newCannon
    $animations = [pscustomobject]$newAnims2

    $root = [ordered]@{ "format_version" = $a.format_version; "animations" = $animations }
    $outJson = $root | ConvertTo-Json -Depth 100
    [System.IO.File]::WriteAllText($file, $outJson, $utf8)

    $conflictNote = if ($conflicts.Count -gt 0) { "CONFLICT bones kept(cannon): $($conflicts -join ',')" } else { "no conflict" }
    Write-Output "$t : merged $specificKey -> $cannonKey (len $newLen s, $conflictNote)"
}
Write-Output "---- WARNINGS ----"
if ($warnings.Count -eq 0) { Write-Output "none" } else { $warnings }
