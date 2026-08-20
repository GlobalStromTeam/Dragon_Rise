param()
$base = "D:\minecraft\modp\GScode\src\main\resources\assets\dragonrise_reforge\animations"
# 1. t72b3 双文件检查
Write-Output "== t72b3 动画文件 =="
Get-ChildItem $base -Recurse -Filter "t72b3*" | Select-Object FullName, Length
# 2. comet/maus/pershing/t3485 的 fire 键检查
foreach ($t in @("comet","maus","pershing","t3485","t80","su24","ac130","darkbear")) {
    $f = "$base\bedrock\vehicle\$t.animation.json"
    if (-not (Test-Path $f)) { $f = "$base\$t.animation.json" }
    if (Test-Path $f) {
        $a = [System.IO.File]::ReadAllText($f) | ConvertFrom-Json
        $keys = $a.animations.PSObject.Properties.Name -join ", "
        Write-Output "$t : [$keys]"
    } else { Write-Output "$t : no anim file" }
}
# 3. ztz99a2 注册检查
Write-Output "== ztz99a2 注册 =="
Select-String -Path "D:\minecraft\modp\GScode\src\main\java\com\redabysslucia\dragonrise_reforge\init\ModEntities.java" -Pattern "ZTZ99A2|ztz99a2" | ForEach-Object { $_.Line.Trim() }
Write-Output "== ztz99a2 数据文件 =="
Get-ChildItem "D:\minecraft\modp\GScode\src\main\resources\data\dragonrise_reforge\sbw\vehicles" -Filter "*99a2*" -ErrorAction SilentlyContinue | Select-Object Name
