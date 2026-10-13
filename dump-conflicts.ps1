# Dump the remaining merge conflict blocks, reading the OFFICIAL staged stages.
#
# Why not read the working tree: once a file is partially resolved the markers in
# the working tree no longer reflect git's view. `git show :2:<path>` is our side
# (1.21.1 / NeoForge) and `:3:<path>` is theirs (1.20.1 / Forge); the conflict
# blocks themselves are produced by `git diff` on the unmerged index.
#
# This script shells out to `git diff -- <file>` and prints the @@ hunks, which is
# what actually needs resolving.
#
# NOTE: ASCII-only comments. Windows PowerShell 5.1 mis-parses some non-ASCII in comments.

$ErrorActionPreference = 'Continue'
Set-Location "E:\Dragon_Rise_1211"

$files = & git diff --name-only --diff-filter=U 2>&1
foreach ($f in $files) {
    $lines = [System.IO.File]::ReadAllLines($f)
    $idx = @()
    for ($i = 0; $i -lt $lines.Count; $i++) { if ($lines[$i] -like '<<<<<<<*') { $idx += $i } }
    if ($idx.Count -eq 0) { continue }

    Write-Output ("### {0}   ({1} block(s))" -f $f, $idx.Count)
    foreach ($s in $idx) {
        # find the matching ======= and >>>>>>>
        $mid = -1; $end = -1
        for ($j = $s + 1; $j -lt $lines.Count; $j++) {
            if ($mid -lt 0 -and $lines[$j] -eq '=======') { $mid = $j }
            elseif ($lines[$j] -like '>>>>>>>*') { $end = $j; break }
        }
        if ($mid -lt 0 -or $end -lt 0) { continue }
        Write-Output ("  --- around line {0} ---" -f ($s + 1))
        Write-Output "  [OURS / 1.21.1]"
        for ($j = $s + 1; $j -lt $mid; $j++) { Write-Output ("    | " + $lines[$j]) }
        Write-Output "  [THEIRS / 1.20.1]"
        for ($j = $mid + 1; $j -lt $end; $j++) { Write-Output ("    | " + $lines[$j]) }
        Write-Output ""
    }
    Write-Output ""
}
