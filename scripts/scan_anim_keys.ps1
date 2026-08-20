param()
$animDir = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
$results = @()
Get-ChildItem $animDir -Recurse -Filter "*.animation.json" | ForEach-Object {
    $txt = [System.IO.File]::ReadAllText($_.FullName)
    $a = $null
    try { $a = $txt | ConvertFrom-Json } catch { $results += "$($_.Name): PARSE ERROR $($_.Exception.Message)"; return }
    foreach ($k in $a.animations.PSObject.Properties.Name) {
        if ($k -notmatch '^animation\.[^.]+\.(fire|idle|gear_up|gear_down|engine_on|engine_off|open|out|hoe_up|lock_turret|splash_on|splash_off|deploy|walk|run|run2|die|start)$' -and $k -notmatch '^animation\.[^.]+\.fire\.\d+$' -and $k -notmatch '^animation\.[^.]+\.idle\.\d+$') {
            $results += "$($_.Name): $k"
        }
    }
}
Write-Output "---- NON-STANDARD KEYS ----"
if ($results.Count -eq 0) { Write-Output "none" } else { $results }
