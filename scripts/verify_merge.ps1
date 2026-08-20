param()
$tanks = @("t80","vt4a1","vt4b","ztz99a","2s25m","2s38","m1a2sepv2","m10booker","ztq15","syy651","hyr0","cyborg_tank","ztz99bh","hj8","bmpt72")
$base = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
$errors = @()
foreach ($t in $tanks) {
    $file = "$base\bedrock\vehicle\$t.animation.json"
    if (-not (Test-Path $file)) { $file = "$base\$t.animation.json" }
    if (-not (Test-Path $file)) { $errors += "$t : file missing"; continue }
    $bytes = [System.IO.File]::ReadAllBytes($file)
    if ($bytes[0] -eq 0xEF) { $errors += "$t : HAS BOM!" }
    try {
        $a = Get-Content $file -Raw | ConvertFrom-Json
        $keys = $a.animations.PSObject.Properties.Name
        $fire = if ($t -eq "hj8") { "animation.missile.fire" } else { "animation.cannon.fire" }
        if ($keys -notcontains $fire) {
            $errors += "$t : missing $fire"
        } else {
            $boneCount = $a.animations.$fire.bones.PSObject.Properties.Name.Count
            $len = $a.animations.$fire.animation_length
            Write-Output "$t : $fire bones=$boneCount len=$len OK"
        }
    } catch {
        $errors += "$t : parse error $($_.Exception.Message)"
    }
}
Write-Output "---- ERRORS ----"
if ($errors.Count -eq 0) { Write-Output "none" } else { $errors }
