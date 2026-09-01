param()
$animDir = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
$genericSlots = @("cannon","machine_gun","missile","auto_cannon","a_a_missile","passenger_machine_gun","driver_a_a_missile","subcannon","100_m_m__cannon","main_machine_gun","main","coax","station","machine_gun2")
Get-ChildItem $animDir -Recurse -Filter "*.animation.json" | ForEach-Object {
    $a = Get-Content $_.FullName -Raw | ConvertFrom-Json
    foreach ($key in $a.animations.PSObject.Properties.Name) {
        if ($key -notmatch '\.fire$') { continue }
        $prefix = $key -replace '^animation\.([^.]+)\.fire$', '$1'
        if ($genericSlots -contains $prefix) { continue }
        $bones = $a.animations.$key.bones.PSObject.Properties.Name -join ","
        $len = $a.animations.$key.animation_length
        Write-Output ("{0} : {1} (len={2}, bones={3})" -f $_.Name, $key, $len, $bones)
    }
}
