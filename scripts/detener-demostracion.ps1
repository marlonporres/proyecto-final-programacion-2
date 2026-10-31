[CmdletBinding(SupportsShouldProcess=$true)]
param([string]$Raiz=(Split-Path -Parent (Split-Path -Parent $PSScriptRoot)),[switch]$DetenerSql)
$ErrorActionPreference='Stop'
$script:demoRoot=[IO.Path]::GetFullPath($Raiz).TrimEnd('\')
$script:stateDir=Join-Path $script:demoRoot 'ventas-escritorio\target\demostracion'
$script:statePath=Join-Path $script:stateDir 'procesos.json'
. (Join-Path $PSScriptRoot 'DemostracionComun.ps1')
$entries=@(Read-DemoState)
$all=@(Get-CimInstance Win32_Process)
foreach($entry in $entries) {
    $current=Get-Process -Id $entry.id -ErrorAction SilentlyContinue
    if(-not $current){continue}
    if($current.StartTime.ToUniversalTime().Ticks -ne [long]$entry.inicioTicks){Write-Warning "PID $($entry.id) fue reutilizado; se conserva.";continue}
    # El arbol se calcula antes de cerrar el padre para no perder procesos Maven/Java hijos.
    $ids=New-Object 'System.Collections.Generic.List[int]'
    $ids.Add([int]$entry.id)
    for($i=0;$i -lt $ids.Count;$i++) {
        foreach($child in $all | Where-Object {$_.ParentProcessId -eq $ids[$i]}) {
            if(-not $ids.Contains([int]$child.ProcessId)){$ids.Add([int]$child.ProcessId)}
        }
    }
    if($PSCmdlet.ShouldProcess("$($entry.tipo), PID $($entry.id) y sus hijos identificados",'Detener proceso de demostracion')) {
        for($i=$ids.Count-1;$i -ge 0;$i--){Stop-Process -Id $ids[$i] -ErrorAction SilentlyContinue}
    }
}
if(-not $WhatIfPreference -and (Test-Path $script:statePath)) {
    $remaining=@($entries | Where-Object { $p=Get-Process -Id $_.id -ErrorAction SilentlyContinue; $p -and $p.StartTime.ToUniversalTime().Ticks -eq [long]$_.inicioTicks })
    @{raiz=$script:demoRoot;procesos=@($remaining)} | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $script:statePath -Encoding UTF8
}
if($DetenerSql -and $PSCmdlet.ShouldProcess('MSSQLSERVER','Detener SQL Server de este equipo')){Stop-Service MSSQLSERVER}
Write-Host 'Cierre procesado. Solo se consideran procesos registrados de la demostracion; ADB, Gradle global y firewall se conservan.'
if(-not $DetenerSql){Write-Host 'SQL Server permanece encendido. -DetenerSql permite apagarlo expresamente.'}
if(-not $entries.Count){Write-Host 'No hay procesos registrados. Ejecute primero iniciar-demostracion.cmd para registrar los servicios usados.'}
