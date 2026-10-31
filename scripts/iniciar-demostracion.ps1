param(
    [string]$Raiz=(Split-Path -Parent (Split-Path -Parent $PSScriptRoot)),
    [switch]$SoloVerificar,[switch]$SinEscritorio,[switch]$SinNgrok,[switch]$Recompilar,
    [ValidateRange(10,180)][int]$TiempoEspera=90
)
$ErrorActionPreference='Stop'
$script:demoRoot=[IO.Path]::GetFullPath($Raiz).TrimEnd('\')
$script:stateDir=Join-Path $script:demoRoot 'ventas-escritorio\target\demostracion'
$script:statePath=Join-Path $script:stateDir 'procesos.json'
. (Join-Path $PSScriptRoot 'DemostracionComun.ps1')
$apiDir=Join-Path $script:demoRoot 'ventas-api-rest'
$desktopDir=Join-Path $script:demoRoot 'ventas-escritorio'
foreach($required in @('ventas-api-rest\scripts\api.ps1','ventas-escritorio\scripts\demostracion.ps1')){if(-not(Test-Path (Join-Path $script:demoRoot $required))){throw "Falta $required en la carpeta de los tres proyectos."}}
$local='http://localhost:8080/api/v1/'
if($SoloVerificar) {
    $service=Get-Service MSSQLSERVER -ErrorAction SilentlyContinue
    Write-Host "SQL Server: $($service.Status)"
    Write-Host "API y SQL disponibles: $(Test-DemoHealth ($local+'salud'))"
    $url=Get-DemoTunnel
    if($url){Write-Host "URL Android: $url"; Write-Host "Salud publica: $(Test-DemoHealth ($url+'salud'))"}else{Write-Host 'ngrok: sin tunel HTTPS hacia localhost:8080.'}
    Write-Host 'Consulta terminada; no se iniciaron procesos ni se cambio la configuracion.'
    return
}
if(-not $env:JAVA_HOME -and (Test-Path 'C:\Program Files\Java\jdk-25.0.3\bin\java.exe')){$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.3'}
New-Item -ItemType Directory -Force -Path $script:stateDir | Out-Null
Read-DemoState | Out-Null
$service=Get-Service MSSQLSERVER
if($service.Status -ne 'Running') {
    try {Start-Service MSSQLSERVER} catch { Start-Process sc.exe -ArgumentList @('start','MSSQLSERVER') -Verb RunAs -WindowStyle Hidden -Wait }
    $service.WaitForStatus('Running',[TimeSpan]::FromSeconds(30))
}
Write-Host '[1/4] SQL Server disponible.'
if(-not(Test-DemoHealth ($local+'salud'))) {
    $listener=@([Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners() | Where-Object {$_.Port -eq 8080})
    if($listener.Count){throw '8080 esta ocupado, pero no responde con API/base disponibles. Revise el servicio; no se detiene un proceso desconocido.'}
    $worker=Start-DemoWorker 'api' -Build:$Recompilar
    $deadline=(Get-Date).AddSeconds($TiempoEspera)
    while(-not(Test-DemoHealth ($local+'salud'))) {
        $worker.Refresh()
        if($worker.HasExited){throw "La API no inicio. Consulte $script:stateDir\api.err.log"}
        if((Get-Date) -gt $deadline){throw 'Tiempo de espera de API agotado. Revise los logs; -Recompilar puede necesitar mas tiempo.'}
        Start-Sleep -Seconds 1
    }
}
Register-DemoJava $apiDir 'api'
Write-Host '[2/4] API y base verificadas. Se reutiliza la API si ya estaba activa.'
$url=$null
if(-not $SinNgrok) {
    $url=Get-DemoTunnel
    if(-not $url) {
        & (Join-Path $apiDir 'scripts\ngrok.ps1')
        $url=Get-DemoTunnel
        if(-not $url){throw 'No se encontro URL HTTPS de ngrok.'}
    }
    if(-not(Test-DemoHealth ($url+'salud'))){throw 'La URL publica no confirma API/base disponibles. No utilice una URL antigua.'}
    # Adoptar solamente el proceso ngrok http 8080 utilizado en esta demostracion.
    Get-CimInstance Win32_Process -Filter "Name = 'ngrok.exe'" | Where-Object {$_.CommandLine -match '\bhttp\s+(https?://)?(localhost:)?8080\b'} | ForEach-Object { Register-DemoProcess $_.ProcessId 'ngrok' }
    $url | Set-Content (Join-Path $script:stateDir 'url-android.txt') -Encoding UTF8
    Write-Host '[3/4] Tunel publico y SQL verificados.'
    Write-Host "URL BASE PARA ANDROID: $url" -ForegroundColor Cyan
    Write-Host 'Inspector de la peticion POST: http://127.0.0.1:4040'
} else {Write-Warning '[3/4] Modo local: no completa el requisito ngrok de la entrega.'}
if(-not $SinEscritorio) {
    $current=@(Get-CimInstance Win32_Process -Filter "Name = 'java.exe' OR Name = 'javaw.exe'" | Where-Object {$_.CommandLine -match [regex]::Escape($desktopDir)})
    if($current.Count){Register-DemoJava $desktopDir 'escritorio';Write-Host '[4/4] Escritorio ya activo.'}
    else {$workerDesktop=Start-DemoWorker 'escritorio' -Build:$Recompilar;Write-Host '[4/4] Inicio del escritorio solicitado. Si no aparece, revisar escritorio.err.log.'}
} else {Write-Host '[4/4] Escritorio omitido por -SinEscritorio.'}
Write-Host "Logs y registro de procesos: $script:stateDir"
Write-Host 'Deje la computadora encendida. Para cerrar use detener-demostracion.cmd.'
