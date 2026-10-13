# Migrate three 1.20.1-era entities to the 1.21.1 / NeoForge entity API.
#
# Two signatures changed between 1.20.1 and 1.21.1:
#
#   protected void defineSynchedData()
#     -> protected void defineSynchedData(SynchedEntityData.Builder builder)
#        and this.entityData.define(X, v) becomes builder.define(X, v)
#
#   public Packet<ClientGamePacketListener> getAddEntityPacket()
#     -> public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity)
#        and NetworkHooks.getEntitySpawningPacket(this) (removed in 1.21.1)
#        becomes new ClientboundAddEntityPacket(this, entity)
#
# The ClusterChargeEntity in this same project already uses the 1.21.1 form and is the
# reference for the packet construction.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$wt = "E:\Dragon_Rise_1211"
$utf8 = New-Object System.Text.UTF8Encoding($false)

$targets = @(
    'src\main\java\com\redabysslucia\dragonrise_reforge\entities\projectile\MissileShellEntity.java',
    'src\main\java\com\redabysslucia\dragonrise_reforge\entities\special\SignalFlareEntity.java',
    'src\main\java\com\redabysslucia\dragonrise_reforge\entities\special\StarShellEntity.java'
)

foreach ($rel in $targets) {
    $p = Join-Path $wt $rel
    if (-not (Test-Path $p)) { Write-Output "MISSING: $rel"; continue }
    $t = [System.IO.File]::ReadAllText($p)
    $o = $t

    # 1) defineSynchedData signature + body
    $t = $t -replace 'protected void defineSynchedData\(\)\s*\{', 'protected void defineSynchedData(SynchedEntityData.Builder builder) {'
    $t = $t -replace 'this\.entityData\.define\(', 'builder.define('

    # 2) getAddEntityPacket signature + body
    $t = $t -replace 'public (@NotNull )?Packet<ClientGamePacketListener> getAddEntityPacket\(\) \{', 'public $1Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {'
    $t = $t -replace 'return NetworkHooks\.getEntitySpawningPacket\(this\);', 'return new ClientboundAddEntityPacket(this, serverEntity);'

    # 3) imports
    $t = $t -replace '(?m)^import net\.minecraftforge\.network\.NetworkHooks;\r?\n', ''
    if ($t -match 'new ClientboundAddEntityPacket' -and $t -notmatch 'import net\.minecraft\.network\.protocol\.game\.ClientboundAddEntityPacket;') {
        $t = $t -replace '(?m)^(import net\.minecraft\.network\.protocol\.Packet;)', "import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;`r`n`$1"
    }
    if ($t -match 'ServerEntity serverEntity' -and $t -notmatch 'import net\.minecraft\.server\.level\.ServerEntity;') {
        $t = $t -replace '(?m)^(import net\.minecraft\.world\.entity\.Entity;)', "import net.minecraft.server.level.ServerEntity;`r`n`$1"
    }
    if ($t -notmatch 'import net\.minecraft\.network\.syncher\.SynchedEntityData;' -and $t -match 'SynchedEntityData\.Builder') {
        $t = $t -replace '(?m)^(import net\.minecraft\.network\.protocol\.Packet;)', "import net.minecraft.network.syncher.SynchedEntityData;`r`n`$1"
    }

    if ($t -ne $o) {
        [System.IO.File]::WriteAllText($p, $t, $utf8)
        Write-Output ("migrated {0}" -f (Split-Path $rel -Leaf))
    } else {
        Write-Output ("no change {0}" -f (Split-Path $rel -Leaf))
    }
}

Write-Output ""
Write-Output "=== remaining NetworkHooks across project ==="
Get-ChildItem (Join-Path $wt "src") -Recurse -Filter *.java -ErrorAction SilentlyContinue |
    Select-String -Pattern 'NetworkHooks' |
    ForEach-Object { "  {0}:{1}" -f (Split-Path $_.Path -Leaf), $_.LineNumber }
