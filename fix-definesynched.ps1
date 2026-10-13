# Resolve the two defineSynchedData conflicts (HJ8Entity, R9M133Entity) for NeoForge.
#
# Both files already declare ROUND_STATE (and ROUND_EMPTY / ROUND_LOADED) *outside*
# the conflict block, and both already define roundState()/setRoundState() *inside*
# the block on the 1.20.1 side. So neither side alone is correct:
#   - take OURS   -> NeoForge's SynchedEntityData.Builder signature, but ROUND_STATE
#                    never gets registered, so the launcher state machine breaks.
#   - take THEIRS -> the old no-arg defineSynchedData(), which does not exist on
#                    1.21.1, plus duplicate roundState()/setRoundState() definitions.
# The resolution keeps OURS and adds the ROUND_STATE define.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$wt = "E:\Dragon_Rise_1211"
$utf8 = New-Object System.Text.UTF8Encoding($false)

$targets = @(
    'src\main\java\com\redabysslucia\dragonrise_reforge\entities\atmg\HJ8Entity.java',
    'src\main\java\com\redabysslucia\dragonrise_reforge\entities\atmg\R9M133Entity.java'
)

foreach ($rel in $targets) {
    $p = Join-Path $wt $rel
    $lines = [System.IO.File]::ReadAllLines($p)

    $s = -1; $e = -1; $mid = -1
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i].StartsWith('<<<<<<<')) { $s = $i }
        elseif ($s -ge 0 -and $lines[$i] -eq '=======') { $mid = $i }
        elseif ($s -ge 0 -and $lines[$i].StartsWith('>>>>>>>')) { $e = $i; break }
    }
    if ($s -lt 0 -or $mid -lt 0 -or $e -lt 0) { Write-Output "SKIP (no block): $rel"; continue }

    $out = New-Object System.Collections.Generic.List[string]
    # everything before the marker
    for ($i = 0; $i -lt $s; $i++) { $out.Add($lines[$i]) }

    # the resolved method
    $out.Add('    protected void defineSynchedData(SynchedEntityData.Builder builder) {')
    $out.Add('        super.defineSynchedData(builder);')
    $out.Add('        builder.define(LOADED, false);')
    $out.Add('        builder.define(RELOAD_COOLDOWN, 0);')
    $out.Add('        builder.define(ROUND_STATE, ROUND_EMPTY);')

    # everything after the marker
    for ($i = $e + 1; $i -lt $lines.Count; $i++) { $out.Add($lines[$i]) }

    [System.IO.File]::WriteAllLines($p, $out, $utf8)
    Write-Output ("resolved {0}  (block {1}..{2})" -f (Split-Path $rel -Leaf), ($s + 1), ($e + 1))
}
