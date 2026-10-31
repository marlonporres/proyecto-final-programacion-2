param([Parameter(Mandatory=$true)][string]$Raiz,[ValidateSet('api','escritorio')][string]$Tipo,[switch]$Recompilar)
$ErrorActionPreference='Stop'
try {
    if($Tipo -eq 'api') {
        $scriptApi=Join-Path $Raiz 'ventas-api-rest\scripts\api.ps1'
        if($Recompilar -or -not(Test-Path (Join-Path $Raiz 'ventas-api-rest\target\sistema-ventas-api-1.0-SNAPSHOT.jar'))) { & $scriptApi -EnLinea } else { & $scriptApi -SinCompilar }
    } else {
        $scriptDesktop=Join-Path $Raiz 'ventas-escritorio\scripts\demostracion.ps1'
        if($Recompilar){ & $scriptDesktop -EnLinea }else{ & $scriptDesktop }
    }
    if(-not $?){exit 1}
} catch { Write-Error $_; exit 1 }
