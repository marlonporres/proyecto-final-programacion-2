param()
$ErrorActionPreference='Stop'
$repo=Split-Path -Parent $PSScriptRoot
$testRoot=Join-Path $repo ('target\prueba-lanzadores-'+[guid]::NewGuid().ToString('N'))
$stateDir=Join-Path $testRoot 'ventas-escritorio\target\demostracion'
New-Item -ItemType Directory -Force -Path $stateDir | Out-Null
$registry=Join-Path $stateDir 'procesos.json'
$tracked=$null;$control=$null
try {
    $tracked=Start-Process powershell.exe -ArgumentList @('-NoProfile','-Command','Start-Sleep -Seconds 120') -WindowStyle Hidden -PassThru
    $control=Start-Process powershell.exe -ArgumentList @('-NoProfile','-Command','Start-Sleep -Seconds 120') -WindowStyle Hidden -PassThru
    @{raiz=$testRoot;procesos=@(@{id=$tracked.Id;inicioTicks=$tracked.StartTime.ToUniversalTime().Ticks;tipo='qa'})} | ConvertTo-Json -Depth 5 | Set-Content $registry -Encoding UTF8
    & (Join-Path $PSScriptRoot 'detener-demostracion.ps1') -Raiz $testRoot -WhatIf
    if(-not(Get-Process -Id $tracked.Id -ErrorAction SilentlyContinue)){throw 'WhatIf detuvo un proceso'}
    & (Join-Path $PSScriptRoot 'detener-demostracion.ps1') -Raiz $testRoot
    Start-Sleep -Milliseconds 250
    if(Get-Process -Id $tracked.Id -ErrorAction SilentlyContinue){throw 'No se detuvo el proceso registrado'}
    if(-not(Get-Process -Id $control.Id -ErrorAction SilentlyContinue)){throw 'Se detuvo el control no registrado'}
    @{raiz=$testRoot;procesos=@(@{id=$control.Id;inicioTicks=($control.StartTime.ToUniversalTime().Ticks-1);tipo='qa-fecha-ajena'})} | ConvertTo-Json -Depth 5 | Set-Content $registry -Encoding UTF8
    & (Join-Path $PSScriptRoot 'detener-demostracion.ps1') -Raiz $testRoot
    if(-not(Get-Process -Id $control.Id -ErrorAction SilentlyContinue)){throw 'No se respeto la identidad del PID'}
    Write-Host 'APROBADO: WhatIf conserva, cierre detiene registrado, conserva control y protege PID con fecha distinta.'
} finally {
    foreach($own in @($tracked,$control)){if($own){Stop-Process -Id $own.Id -ErrorAction SilentlyContinue}}
}