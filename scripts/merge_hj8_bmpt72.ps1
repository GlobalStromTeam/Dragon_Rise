param()
# hj8: merge animation.hj8.fire into animation.missile.fire (slot name "Missile")
# bmpt72: create animation.cannon.fire from main_cannon_fire + cannon1.fire + cannon2.fire
$base = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Find-File([string]$t) {
    $f = "$base\bedrock\vehicle\$t.animation.json"
    if (-not (Test-Path $f)) { $f = "$base\$t.animation.json" }
    return $f
}

# ---- hj8 ----
$hjFile = Find-File "hj8"
$hj = [System.IO.File]::ReadAllText($hjFile) | ConvertFrom-Json
$hjAnim = $hj.animations
$target = "animation.missile.fire"
$src = "animation.hj8.fire"
$bones = [ordered]@{}
$len = 0.0
$loop = "hold_on_last_frame"
if ($hjAnim.PSObject.Properties.Name -contains $target) {
    $c = $hjAnim.$target
    foreach ($p in $c.bones.PSObject.Properties) { $bones[$p.Name] = $p.Value }
    if ($null -ne $c.animation_length) { $len = [double]$c.animation_length }
    if ($null -ne $c.loop) { $loop = $c.loop }
}
$conflicts = @()
foreach ($p in $hjAnim.$src.bones.PSObject.Properties) {
    if ($bones.Contains($p.Name)) { $conflicts += $p.Name } else { $bones[$p.Name] = $p.Value }
}
$srcLen = [double]$hjAnim.$src.animation_length
$len = [Math]::Max($len, $srcLen)
$new = [ordered]@{ loop = $loop; animation_length = $len; bones = [pscustomobject]$bones }
$props = $hjAnim.PSObject.Properties
$newAnim = [ordered]@{}
foreach ($p in $props) { $newAnim[$p.Name] = $p.Value }
$newAnim[$target] = [pscustomobject]$new
$root = [ordered]@{ "format_version" = $hj.format_version; "animations" = [pscustomobject]$newAnim }
[System.IO.File]::WriteAllText($hjFile, ($root | ConvertTo-Json -Depth 100), $utf8)
Write-Output "hj8 : merged $src -> $target (len $len, conflicts: $($conflicts -join ','))"

# ---- bmpt72 ----
$bmFile = Find-File "bmpt72"
$bm = [System.IO.File]::ReadAllText($bmFile) | ConvertFrom-Json
$bmAnim = $bm.animations
$bones2 = [ordered]@{}
$maxLen = 0.0
foreach ($srcKey in @("animation.bmpt72.main_cannon_fire", "animation.cannon1.fire", "animation.cannon2.fire")) {
    if (-not ($bmAnim.PSObject.Properties.Name -contains $srcKey)) { continue }
    $s = $bmAnim.$srcKey
    foreach ($p in $s.bones.PSObject.Properties) {
        if (-not $bones2.Contains($p.Name)) { $bones2[$p.Name] = $p.Value }
    }
    if ($null -ne $s.animation_length) { $maxLen = [Math]::Max($maxLen, [double]$s.animation_length) }
}
$new2 = [ordered]@{ loop = "hold_on_last_frame"; animation_length = $maxLen; bones = [pscustomobject]$bones2 }
$props2 = $bmAnim.PSObject.Properties
$newAnim2 = [ordered]@{}
foreach ($p in $props2) { $newAnim2[$p.Name] = $p.Value }
$newAnim2["animation.cannon.fire"] = [pscustomobject]$new2
$root2 = [ordered]@{ "format_version" = $bm.format_version; "animations" = [pscustomobject]$newAnim2 }
[System.IO.File]::WriteAllText($bmFile, ($root2 | ConvertTo-Json -Depth 100), $utf8)
Write-Output "bmpt72 : created animation.cannon.fire from main_cannon_fire+cannon1+cannon2 (len $maxLen, bones: $($bones2.Keys -join ','))"
