param(
    [ValidateSet('Iniciar', 'Verificar')]
    [string]$Modo = 'Iniciar',
    [switch]$EnLinea,
    [string]$RepositorioLocal
)

$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $PSScriptRoot
$mavenProyecto = Join-Path $raizProyecto 'apache-maven-3.9.9\bin\mvn.cmd'
$configuracionProyecto = Join-Path $raizProyecto 'src\main\resources\database.properties'

$arranqueMaven = Get-ChildItem -Path (Join-Path $raizProyecto 'apache-maven-3.9.9\boot\plexus-classworlds-*.jar') -ErrorAction SilentlyContinue
if (-not (Test-Path -LiteralPath $mavenProyecto) -or -not $arranqueMaven) {
    # Los binarios del Maven local no se versionan; una clonacion usa el Maven instalado.
    $mavenInstalado = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($null -eq $mavenInstalado) {
        throw 'Instale Maven 3.9+ y agregue su carpeta bin al PATH, o extraiga Maven completo en apache-maven-3.9.9.'
    }
    $mavenProyecto = $mavenInstalado.Source
}
if (-not (Test-Path -LiteralPath $configuracionProyecto)) {
    throw 'Configure src\main\resources\database.properties a partir de database.properties.example.'
}
if ($env:JAVA_HOME) {
    $javaProyecto = Join-Path $env:JAVA_HOME 'bin\java.exe'
    if (-not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        throw 'JAVA_HOME debe apuntar a un JDK completo, con bin\java.exe y bin\javac.exe.'
    }
} else {
    $javaProyecto = (Get-Command java -ErrorAction Stop).Source
}
$versionProyecto = & $javaProyecto --version
if ($LASTEXITCODE -ne 0 -or $versionProyecto[0] -notmatch '(?:java|openjdk)\s+(\d+)' -or [int]$Matches[1] -lt 25) {
    throw 'Este proyecto requiere JDK 25 o superior. Revise java --version y JAVA_HOME.'
}
$sqlProyecto = Get-Service -Name MSSQLSERVER -ErrorAction Stop
if ($sqlProyecto.Status -ne 'Running') {
    throw 'Inicie SQL Server (MSSQLSERVER) antes de la demostracion. En PowerShell como administrador: Start-Service MSSQLSERVER.'
}

$argumentosProyecto = @()
if (-not $EnLinea) { $argumentosProyecto += '-o' }
if ($RepositorioLocal) {
    $argumentosProyecto += "-Dmaven.repo.local=$([System.IO.Path]::GetFullPath($RepositorioLocal))"
}
if ($Modo -eq 'Verificar') {
    $argumentosProyecto += @('-Dventas.it=true', 'clean', 'verify')
    Write-Host 'Verificando compilacion, pruebas unitarias, SQL Server y flujo Swing...'
} else {
    $argumentosProyecto += @('compile', 'exec:java')
    Write-Host 'Abriendo Sistema de Ventas. Comience en VENTAS > Nueva Orden de Venta.'
}

Push-Location -LiteralPath $raizProyecto
try {
    & $mavenProyecto @argumentosProyecto
    if ($LASTEXITCODE -ne 0) {
        throw "Maven termino con error ($LASTEXITCODE). Consulte la salida; para descargar dependencias use -EnLinea."
    }
    if ($Modo -eq 'Verificar') {
        Write-Host 'VERIFICACION APROBADA. Reportes: target\surefire-reports. Guion: docs\guion_entrega.md.' -ForegroundColor Green
    }
} finally { Pop-Location }
