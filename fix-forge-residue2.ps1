# Second-pass Forge -> NeoForge import conversion.
#
# fix-forge-residue.ps1 only rewrote `net.minecraftforge.{api.distmarker,eventbus.api,
# fml.common,fml.event,registries,client.event,common.capabilities}`. The merge also
# dragged in imports from other Forge packages. Each one is mapped explicitly here --
# a blanket `net.minecraftforge.` -> `net.neoforged.neoforge.` rewrite would corrupt the
# already-correct `net.neoforged.neoforge.*` imports by producing `net.neoforged.neoforge.neoforge.*`.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Continue'
$wt = "E:\Dragon_Rise_1211"
$utf8 = New-Object System.Text.UTF8Encoding($false)

$rules = @(
    # fml (non-common subpackages)
    @{ From = '(?m)^import net\.minecraftforge\.fml\.ModList;';          To = 'import net.neoforged.fml.ModList;' },
    @{ From = '(?m)^import net\.minecraftforge\.fml\.loading\.';         To = 'import net.neoforged.fml.loading.' },
    @{ From = '(?m)^import net\.minecraftforge\.fml\.ModContainer;';     To = 'import net.neoforged.fml.ModContainer;' },
    # plain (non-eventbus) Forge events
    @{ From = '(?m)^import net\.minecraftforge\.event\.';                To = 'import net.neoforged.neoforge.event.' },
    # network
    @{ From = '(?m)^import net\.minecraftforge\.network\.';              To = 'import net.neoforged.neoforge.network.' },
    # items / capabilities helpers
    @{ From = '(?m)^import net\.minecraftforge\.items\.';                To = 'import net.neoforged.neoforge.items.' },
    @{ From = '(?m)^import net\.minecraftforge\.capabilities\.';         To = 'import net.neoforged.neoforge.capabilities.' },
    @{ From = '(?m)^import net\.minecraftforge\.common\.';               To = 'import net.neoforged.neoforge.common.' },
    @{ From = '(?m)^import net\.minecraftforge\.util\.';                 To = 'import net.neoforged.neoforge.util.' },
    @{ From = '(?m)^import net\.minecraftforge\.fluids\.';               To = 'import net.neoforged.neoforge.fluids.' },
    @{ From = '(?m)^import net\.minecraftforge\.client\.';               To = 'import net.neoforged.neoforge.client.' },
    @{ From = '(?m)^import net\.minecraftforge\.registries\.';           To = 'import net.neoforged.neoforge.registries.' },
    @{ From = '(?m)^import net\.minecraftforge\.eventbus\.';             To = 'import net.neoforged.bus.' },
    @{ From = '(?m)^import net\.minecraftforge\.api\.';                  To = 'import net.neoforged.api.' }
)

$changed = @()
foreach ($f in (Get-ChildItem (Join-Path $wt "src") -Recurse -Filter *.java -ErrorAction SilentlyContinue)) {
    $t = [System.IO.File]::ReadAllText($f.FullName)
    $o = $t
    foreach ($r in $rules) { $t = $t -replace $r.From, $r.To }
    if ($t -ne $o) { [System.IO.File]::WriteAllText($f.FullName, $t, $utf8); $changed += $f.Name }
}
Write-Output ("changed: {0}" -f $changed.Count)
$changed | Sort-Object -Unique | ForEach-Object { "  $_" }

Write-Output ""
Write-Output "=== any net.minecraftforge left ==="
$left = Get-ChildItem (Join-Path $wt "src") -Recurse -Filter *.java -ErrorAction SilentlyContinue |
    Select-String -Pattern 'net\.minecraftforge'
Write-Output ("  {0} hits" -f ($left | Measure-Object).Count)
$left | ForEach-Object { "  {0}:{1}: {2}" -f (Split-Path $_.Path -Leaf), $_.LineNumber, $_.Line.Trim() }
