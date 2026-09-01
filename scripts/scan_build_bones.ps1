param()
$dir = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\models\bedrock\vehicle"
$noBuild = @()
$noBuildNoObb = @()
Get-ChildItem $dir -Filter "*.geo.json" | ForEach-Object {
    $a = [System.IO.File]::ReadAllText($_.FullName) | ConvertFrom-Json
    $hasBuild = $false; $hasObb = $false; $hasPlane = $false
    foreach ($g in $a."minecraft:geometry") {
        foreach ($b in $g.bones) {
            $n = $b.name
            if ($n -eq "Build") { $hasBuild = $true }
            if ($n.ToLower().Contains("obb")) { $hasObb = $true }
            if ($n -eq "Plane") { $hasPlane = $true }
        }
    }
    if (-not $hasBuild) {
        $noBuild += ("{0} : Obb={1} Plane={2}" -f $_.BaseName, $hasObb, $hasPlane)
        if (-not $hasObb -and -not $hasPlane) { $noBuildNoObb += $_.BaseName }
    }
}
Write-Output "---- models WITHOUT Build bone ----"
$noBuild
Write-Output "---- no Build, no Obb, no Plane ----"
if ($noBuildNoObb.Count -eq 0) { Write-Output "none" } else { $noBuildNoObb }
