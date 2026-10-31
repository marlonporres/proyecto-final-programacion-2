function Test-DemoHealth([string]$Url) {
    try {
        $h=Invoke-RestMethod $Url -Headers @{'ngrok-skip-browser-warning'='ventas-demo'} -TimeoutSec 5
        return ($h.estado -eq 'OK' -and $h.baseDatos -eq 'DISPONIBLE')
    } catch { return $false }
}
function Get-DemoTunnel {
    try { $all=(Invoke-RestMethod 'http://127.0.0.1:4040/api/tunnels' -TimeoutSec 3).tunnels } catch { return $null }
    $match=$all | Where-Object { $_.proto -eq 'https' -and $_.public_url -match '^https://' -and $_.config.addr -match '^(https?://)?(localhost|127\.0\.0\.1|\[::1\]):8080/?$' } | Select-Object -First 1
    if($match){return ($match.public_url.TrimEnd('/')+'/api/v1/')}
    return $null
}
function Read-DemoState {
    if(Test-Path -LiteralPath $script:statePath) {
        $data=Get-Content -LiteralPath $script:statePath -Raw | ConvertFrom-Json
        if($data.raiz -ne $script:demoRoot){throw 'El registro pertenece a otra carpeta. No se manipulan esos procesos.'}
        return @($data.procesos)
    }
    return @()
}
function Register-DemoProcess([int]$ProcessId,[string]$Kind) {
    $proc=Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if(-not $proc){return}
    $items=@(Read-DemoState | Where-Object {$_.id -ne $ProcessId})
    $items+=@{id=$ProcessId;inicioTicks=$proc.StartTime.ToUniversalTime().Ticks;tipo=$Kind}
    @{raiz=$script:demoRoot;procesos=@($items)} | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $script:statePath -Encoding UTF8
}
function Register-DemoJava([string]$Folder,[string]$Kind) {
    $escaped=[regex]::Escape($Folder)
    Get-CimInstance Win32_Process -Filter "Name = 'java.exe' OR Name = 'javaw.exe'" | Where-Object {$_.CommandLine -match $escaped} | ForEach-Object { Register-DemoProcess $_.ProcessId $Kind }
}
function Start-DemoWorker([string]$Kind,[switch]$Build) {
    $worker=Join-Path $PSScriptRoot 'ProcesoDemostracion.ps1'
    $args=@('-NoProfile','-ExecutionPolicy','Bypass','-File',('"'+$worker+'"'),'-Raiz',('"'+$script:demoRoot+'"'),'-Tipo',$Kind)
    if($Build){$args+='-Recompilar'}
    $p=Start-Process powershell.exe -ArgumentList $args -WorkingDirectory $script:demoRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $script:stateDir ($Kind+'.out.log')) -RedirectStandardError (Join-Path $script:stateDir ($Kind+'.err.log'))
    Register-DemoProcess $p.Id $Kind
    return $p
}
