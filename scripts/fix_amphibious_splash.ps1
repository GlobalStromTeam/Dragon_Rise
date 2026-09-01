param()
# Fix amphibious vehicle splash animations:
# 1. zsl10: add Animation config pointing to its bedrock animation file (has splash_on/off with FLB bone)
# 2. ztd05: move top-level animation file into bedrock/vehicle/, rename flbon/flboff keys
# 3. zbd05: add animation.zbd05.splash_on/off keys (copied from ztd05 flbon/flboff) to its bedrock animation file
$dataDir = "D:\minecraft\modp\GScode\src\main\resources\data\dragonrise_reforge\sbw\vehicles"
$animBase = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
$utf8 = New-Object System.Text.UTF8Encoding($false)

# ---- 1. zsl10 ----
$zslFile = "$dataDir\zsl10.json"
$zsl = [System.IO.File]::ReadAllText($zslFile) | ConvertFrom-Json
$zsl | Add-Member -NotePropertyName Animation -NotePropertyValue "dragonrise_reforge:animations/bedrock/vehicle/zsl10.animation.json" -Force
[System.IO.File]::WriteAllText($zslFile, ($zsl | ConvertTo-Json -Depth 100), $utf8)
Write-Output "zsl10 : Animation set"

# ---- 2. ztd05: move + rename keys ----
$ztdTop = "$animBase\ztd05.animation.json"
$ztdBed = "$animBase\bedrock\vehicle\ztd05.animation.json"
if (Test-Path $ztdTop) {
    $txt = [System.IO.File]::ReadAllText($ztdTop)
    $a = $txt | ConvertFrom-Json
    $newAnim = [ordered]@{}
    foreach ($p in $a.animations.PSObject.Properties) {
        $nk = switch ($p.Name) {
            "flbon"  { "animation.ztd05.splash_on" }
            "flboff" { "animation.ztd05.splash_off" }
            default  { $p.Name }
        }
        $newAnim[$nk] = $p.Value
    }
    $root = [ordered]@{ "format_version" = $a.format_version; "animations" = [pscustomobject]$newAnim }
    [System.IO.File]::WriteAllText($ztdBed, ($root | ConvertTo-Json -Depth 100), $utf8)
    Remove-Item $ztdTop -Force
    Write-Output "ztd05 : moved to bedrock/vehicle/ with splash_on/splash_off keys"
} else {
    Write-Output "ztd05 : top-level file missing"
}
$ztdCfg = [System.IO.File]::ReadAllText("$dataDir\ztd05.json") | ConvertFrom-Json
$ztdCfg.Animation = "dragonrise_reforge:animations/bedrock/vehicle/ztd05.animation.json"
[System.IO.File]::WriteAllText("$dataDir\ztd05.json", ($ztdCfg | ConvertTo-Json -Depth 100), $utf8)
Write-Output "ztd05 : Animation config fixed"

# ---- 3. zbd05: add splash keys copied from ztd05 flbon/flboff ----
$zbdBed = "$animBase\bedrock\vehicle\zbd05.animation.json"
if (Test-Path $ztdBed) {
    $ztdA = [System.IO.File]::ReadAllText($ztdBed) | ConvertFrom-Json
    $zbdA = [System.IO.File]::ReadAllText($zbdBed) | ConvertFrom-Json
    $newAnim = [ordered]@{}
    foreach ($p in $zbdA.animations.PSObject.Properties) { $newAnim[$p.Name] = $p.Value }
    $newAnim["animation.zbd05.splash_on"] = $ztdA.animations."animation.ztd05.splash_on"
    $newAnim["animation.zbd05.splash_off"] = $ztdA.animations."animation.ztd05.splash_off"
    $root = [ordered]@{ "format_version" = $zbdA.format_version; "animations" = [pscustomobject]$newAnim }
    [System.IO.File]::WriteAllText($zbdBed, ($root | ConvertTo-Json -Depth 100), $utf8)
    Write-Output "zbd05 : splash_on/splash_off keys added"
} else {
    Write-Output "zbd05 : source ztd05 bedrock file missing"
}
$zbdCfg = [System.IO.File]::ReadAllText("$dataDir\zbd05.json") | ConvertFrom-Json
$zbdCfg.Animation = "dragonrise_reforge:animations/bedrock/vehicle/zbd05.animation.json"
[System.IO.File]::WriteAllText("$dataDir\zbd05.json", ($zbdCfg | ConvertTo-Json -Depth 100), $utf8)
Write-Output "zbd05 : Animation config fixed"
