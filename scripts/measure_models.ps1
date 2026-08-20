param()
function Get-GeoSize([string]$file) {
    $g = Get-Content $file -Raw | ConvertFrom-Json
    $minX = [double]::MaxValue; $minY = [double]::MaxValue; $minZ = [double]::MaxValue
    $maxX = [double]::MinValue; $maxY = [double]::MinValue; $maxZ = [double]::MinValue
    foreach ($geom in $g."minecraft:geometry") {
        foreach ($bone in $geom.bones) {
            if ($bone.cubes) {
                foreach ($cube in $bone.cubes) {
                    $o = $cube.origin; $s = $cube.size
                    $x = $o[0]; $y = $o[1]; $z = $o[2]
                    if ($x -lt $minX) { $minX = $x }
                    if ($y -lt $minY) { $minY = $y }
                    if ($z -lt $minZ) { $minZ = $z }
                    if (($x + $s[0]) -gt $maxX) { $maxX = $x + $s[0] }
                    if (($y + $s[1]) -gt $maxY) { $maxY = $y + $s[1] }
                    if (($z + $s[2]) -gt $maxZ) { $maxZ = $z + $s[2] }
                }
            }
        }
    }
    $w = ($maxX - $minX) / 16.0; $h = ($maxY - $minY) / 16.0; $l = ($maxZ - $minZ) / 16.0
    $cx = ($minX + $maxX) / 2 / 16.0; $cz = ($minZ + $maxZ) / 2 / 16.0
    Write-Output ("长={0:N2} 宽={1:N2} 高={2:N2} 格 | 中心偏移XZ=({3:N2},{4:N2})" -f $l, $w, $h, $cx, $cz)
}
$base = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\models\bedrock\projectile"
Write-Output "== aim9 (missle2) =="
Get-GeoSize "$base\aim9.geo.json"
Write-Output "== aim120 =="
Get-GeoSize "$base\aim120.geo.json"
Write-Output "== agm65 =="
Get-GeoSize "$base\agm65.geo.json"
Write-Output "== gbu_12 =="
Get-GeoSize "$base\gbu_12.geo.json"
