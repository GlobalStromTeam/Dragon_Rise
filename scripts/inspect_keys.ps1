param()
function Inspect([string]$t, [string]$key) {
    $base = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
    $f = "$base\bedrock\vehicle\$t.animation.json"
    if (-not (Test-Path $f)) { $f = "$base\$t.animation.json" }
    if (-not (Test-Path $f)) { Write-Output "$t : no file"; return }
    $a = [System.IO.File]::ReadAllText($f) | ConvertFrom-Json
    if ($null -ne $a.animations.$key) {
        $bones = $a.animations.$key.bones.PSObject.Properties.Name -join ","
        Write-Output "$t [$key] : len=$($a.animations.$key.animation_length) loop=$($a.animations.$key.loop) bones=$bones"
    } else {
        Write-Output "$t [$key] : NOT FOUND"
    }
}
Inspect "t72b3" "fire"
Inspect "t72b3" "animation.cannon.fire"
Inspect "ztz96a" "抛壳"
Inspect "ztz99a2" "99A"
$j = Get-Content "D:\minecraft\modp\GScode\src\main\resources\data\dragonrise_reforge\sbw\vehicles\ztz96a.json" -Raw | ConvertFrom-Json
Write-Output "ztz96a Seats.Weapons = $($j.Seats.Weapons -join ',')"
$j2 = Get-Content "D:\minecraft\modp\GScode\src\main\resources\data\dragonrise_reforge\sbw\vehicles\ztz99a2.json" -Raw | ConvertFrom-Json -ErrorAction SilentlyContinue
if ($j2) { Write-Output "ztz99a2 Seats.Weapons = $($j2.Seats.Weapons -join ',')" } else { Write-Output "ztz99a2 车辆配置不存在?" }
