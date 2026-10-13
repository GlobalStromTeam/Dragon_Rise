# Convert Forge package references that the 1.20.1 merge brought in back to NeoForge.
#
# The merge auto-resolved many files by taking the 1.20.1 side wholesale, so Forge
# packages leaked in even though this branch targets NeoForge 1.21.1. NeoForge keeps
# net.minecraftforge.* as deprecated relocation shims for some of these, but the event
# bus / FML / capability classes genuinely live under net.neoforged on 1.21.1.
#
# Only import lines are rewritten (regex anchored to start-of-line) so that
# net.neoforged.neoforge.* imports are never touched.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Stop'
$wt = "E:\Dragon_Rise_1211"
$utf8 = New-Object System.Text.UTF8Encoding($false)

$rules = @(
    @{ From = '(?m)^import net\.minecraftforge\.api\.distmarker\.';        To = 'import net.neoforged.api.distmarker.' },
    @{ From = '(?m)^import net\.minecraftforge\.eventbus\.api\.';          To = 'import net.neoforged.bus.api.' },
    @{ From = '(?m)^import net\.minecraftforge\.fml\.common\.';            To = 'import net.neoforged.fml.common.' },
    @{ From = '(?m)^import net\.minecraftforge\.fml\.event\.';             To = 'import net.neoforged.fml.event.' },
    @{ From = '(?m)^import net\.minecraftforge\.fml\.javafmlmod\.';        To = 'import net.neoforged.fml.javafmlmod.' },
    @{ From = '(?m)^import net\.minecraftforge\.registries\.DeferredRegister;'; To = 'import net.neoforged.neoforge.registries.DeferredRegister;' },
    @{ From = '(?m)^import net\.minecraftforge\.registries\.RegistryObject;';   To = 'import net.neoforged.neoforge.registries.DeferredHolder;' },
    @{ From = '(?m)^import net\.minecraftforge\.registries\.ForgeRegistries;';  To = 'import net.neoforged.neoforge.registries.NeoForgeRegistries;' },
    @{ From = '(?m)^import net\.minecraftforge\.common\.capabilities\.ForgeCapabilities;'; To = 'import net.neoforged.neoforge.capabilities.Capabilities;' },
    @{ From = '(?m)^import net\.minecraftforge\.client\.extensions\.common\.IClientItemExtensions;'; To = 'import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;' },
    @{ From = '(?m)^import net\.minecraftforge\.client\.event\.';          To = 'import net.neoforged.neoforge.client.event.' }
)

$files = Get-ChildItem (Join-Path $wt "src") -Recurse -Filter *.java -ErrorAction SilentlyContinue
$changed = @()
foreach ($f in $files) {
    $t = [System.IO.File]::ReadAllText($f.FullName)
    $o = $t
    foreach ($r in $rules) { $t = $t -replace $r.From, $r.To }
    if ($t -ne $o) {
        [System.IO.File]::WriteAllText($f.FullName, $t, $utf8)
        $changed += $f.Name
    }
}
Write-Output ("files converted: {0}" -f $changed.Count)
$changed | Sort-Object | ForEach-Object { "  $_" }

# Body-level references that imports alone do not fix
Write-Output ""
Write-Output "=== remaining RegistryObject usages (need type rewrite) ==="
Get-ChildItem (Join-Path $wt "src") -Recurse -Filter *.java -ErrorAction SilentlyContinue |
    Select-String -Pattern 'RegistryObject' |
    ForEach-Object { "  {0}:{1}: {2}" -f (Split-Path $_.Path -Leaf), $_.LineNumber, $_.Line.Trim() }
