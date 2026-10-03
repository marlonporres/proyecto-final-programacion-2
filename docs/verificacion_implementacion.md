# Verificación de la implementación

Última verificación: 3 de octubre de 2026.

## Revisión de código y verificación del 3 de octubre

La revisión partió de `main` en `0f9df22`, coincidente con GitHub, y conservó los cambios locales pendientes. `git fsck --full` no detectó corrupción; únicamente informó dos árboles sin referencia. No fue necesario actualizar desde otra revisión remota.

La suite existente aprobó sus 48 casos. Durante la revisión se detectó que Registrar entrada aceptaba objetos todavía activos en memoria aunque el producto o la bodega ya estuvieran desactivados en SQL Server. Dos pruebas nuevas reprodujeron ambos errores antes de la corrección. La entrada ahora verifica el estado persistido con `HOLDLOCK` dentro de la misma transacción que modifica existencias y crea el movimiento; los casos rechazados conservan stock y movimientos. La consulta de inventario también vacía resultados anteriores si falla su actualización.

Se incorporó `scripts/demostracion.cmd` para ejecutar el script sin cambiar la política permanente de PowerShell. El script comprueba el Java que usa Maven (`JAVA_HOME` cuando está definido), acepta `-RepositorioLocal` y usa Maven del `PATH` si no está completa la distribución local. Los JAR de Maven están excluidos de Git; una clonación necesita instalar Maven o extraer su distribución completa.

Comando de verificación ejecutado:

```powershell
.\scripts\demostracion.cmd -Modo Verificar -RepositorioLocal 'C:\Users\marlo\.m2\repository'
```

Resultado: `BUILD SUCCESS`, **50 pruebas, 0 fallos, 0 errores y 0 omitidas**; 36 sin SQL y 14 con SQL Server, en una compilación limpia con `verify`. Incluye el recorrido Swing, rollback, concurrencia, precios históricos, fechas, relaciones de inventario y recuperación desde otra JVM. Se regeneró `target/sistema-facturacion-1.0-SNAPSHOT.jar`; la evidencia de persistencia contiene `PERSISTENCIA_OK`.

El diagnóstico de solo lectura `verificar_integridad.sql` devolvió cero inconsistencias en restricciones, existencias, relaciones/estados de ventas, detalles y movimientos de salida. La base conserva 14 tablas, 24 productos activos, una orden, un despacho y una factura. TEC-001 conserva 19 unidades disponibles; no quedaron productos, clientes ni bodegas de integración. Evidencia local: `target/integridad.txt` y `target/surefire-reports`.

El usuario inició MSSQLSERVER, que estaba detenido. El entorno restringido requirió indicar el caché Maven del usuario y autorizar su acceso. Los avisos de acceso nativo y API obsoleta de Maven/FlatLaf con Java 25 no impidieron compilación ni pruebas. Se inspeccionaron los renderizados de despacho pendiente y factura del ensayo automatizado.

Esta verificación cubre los casos automatizados y los diagnósticos indicados. No equivale a cobertura del 100% de caminos ni sustituye el ensayo humano de la interfaz y la defensa.

## Cierre previo a la demostración del 3 de octubre

Se revisaron las cinco páginas de «Entrega Final del Proyecto de escritorio - Programación II.pdf» en Downloads y se contrastaron los ocho pasos de Ventas y los seis criterios de la rúbrica. La matriz de evidencia está en alineacion_rubrica.md.

Se corrigieron fechas escritas sin abandonar el campo, rechazo de días inexistentes, resultados antiguos tras un error, recarga de bodegas al volver a Inventario, coordinación de entradas desde el controlador, recuperación completa de relaciones en los DAO de inventario y errores que antes se ocultaban. Guardar la orden verifica de nuevo el estado activo persistido de sus productos dentro de la transacción.

Se ejecutó Maven offline con ventas.it=true y package: 48 pruebas, 0 fallos, 0 errores y 0 omitidas; 36 sin SQL y 12 de integración real. El ensayo Swing conservó stock 20 → 20 → 15 → 15 y factura Q112.00 sobre datos propios. Se revisaron los renderizados de despacho pendiente y factura. Se generó target/sistema-facturacion-1.0-SNAPSHOT.jar.

Una prueba inicia una JVM independiente y recupera OV, DSP CONFIRMADO y FAC EMITIDA con detalles y total coincidentes. La evidencia PERSISTENCIA_OK está en target/persistencia-jvm.txt. Otra prueba valida registros de medianoche y del último instante del día, excluyendo días adyacentes.

Consulta posterior de solo lectura: 14 tablas, 24 productos activos, cero productos/clientes/bodegas IT residuales; cero FK/CHECK deshabilitadas o no confiables; cero filas de stock inválido. Se conservan una orden, un despacho y una factura anteriores. TEC-001 tiene 19 unidades a Q95.00; FER-001 permanece inactivo, con sus 19 unidades históricas. No se recreó la base ni se borraron operaciones existentes durante este cierre.

El servicio MSSQLSERVER estaba detenido y se inició con elevación de Windows. La primera compilación dentro del entorno restringido produjo AccessDeniedException al cerrar un JAR del caché Maven; ejecutada con acceso autorizado al entorno local compiló sin modificar dependencias ni el POM.

El comando scripts/demostracion.ps1 -Modo Verificar realiza la compilación limpia y las pruebas completas. scripts/demostracion.ps1 abre la aplicación; exige JDK 25+, configuración local y MSSQLSERVER iniciado. Ambos usan las dependencias locales salvo que se añada -EnLinea.

Se ejecutó también scripts/demostracion.ps1 -Modo Verificar desde cero: las 48 pruebas pasaron y se regeneró el JAR. El diagnóstico verificar_integridad.sql se ejecutó sobre la base y devolvió cero inconsistencias de restricciones, stock, estados, detalles y salidas. Se comprobó el arranque con scripts/demostracion.ps1 -Modo Iniciar.

El dominio y asistencia de los integrantes requieren el ensayo humano registrado en guion_entrega.md. La evidencia técnica no sustituye esa parte de la rúbrica.

## Cierre técnico de generación/confirmación e interfaz

Se aplicó actualizar_despacho_pendiente.sql sin recrear la base ni borrar datos. La generación guarda DSP y detalles PENDIENTES, con cantidad despachada cero, sin reducir ni reservar stock. La confirmación recupera el documento persistido, valida orden/bodega/productos, rebaja inventario, registra movimientos, actualiza cantidades y completa la orden en una transacción.

Se ejecutó Maven con ventas.it=true y clean package: 41 pruebas aprobadas, cero fallos, cero errores y cero omitidas; 33 pruebas sin SQL y ocho de integración real. El JAR fue regenerado.

La integración incluye recuperación de un DSP pendiente, rechazo de generación duplicada y facturación anticipada, confirmación única concurrente y rollback por stock insuficiente. Después del rollback se abastece y se reintenta el mismo DSP sin crear otro.

EnsayoInterfaz accionó los menús de mantenimientos, inventario y ventas, editó un cliente de prueba y completó orden → generación → recuperación → confirmación → factura → consulta de relaciones → recuperación con un contenedor/controlador nuevo. Verificó stock visible 20 → 20 → 15 → 15 y total Q112.00 para dos productos propios del caso. Los diálogos usaron respuestas de prueba; los eventos Swing y la persistencia JDBC fueron reales. Se inspeccionaron los renderizados de despacho pendiente y factura.

Después de la suite: una orden y un despacho CONFIRMADO anteriores conservados, 24 productos activos, TEC-001 y FER-001 con sus 19 unidades y cero reservas, y cero productos IT residuales. No se eliminaron ventas ajenas a los casos de prueba.

La guía de defensa y el ensayo humano están en defensa_proyecto.md y guion_entrega.md. No se certifica asistencia, dominio de los integrantes ni un ensayo manual con reinicio del proceso. El grupo debe practicar esos pasos antes de la revisión presencial.

## Histórico de la verificación del 28 de septiembre

## Base de pruebas

Se comprobó la conexión a localhost:1433 y se recreó exclusivamente SistemaVentas, con autorización explícita del usuario. Se instalaron las 14 tablas de script_base_datos_ventas.sql.

Antes de eliminar el esquema anterior se creó un respaldo COPY_ONLY con CHECKSUM y se verificó mediante RESTORE VERIFYONLY:

```text
C:\Program Files\Microsoft SQL Server\MSSQL17.MSSQLSERVER\MSSQL\Backup\SistemaVentas_pre_alineacion_20260928_194438_8b7847265edc4cd1ad934d04f60e4a04.bak
```

Los datos anteriores fueron eliminados de la base activa, pero pueden recuperarse desde ese respaldo mediante SQL Server. No restaure sobre la base nueva sin respaldar previamente cualquier operación que necesite conservar.

## Pruebas y empaquetado

Comando ejecutado desde la raíz en PowerShell:

```powershell
.\apache-maven-3.9.9\bin\mvn.cmd -o '-Dmaven.repo.local=C:\Users\marlo\.m2\repository' '-Dventas.it=true' -q package
```

Resultado: 38 pruebas aprobadas, cero fallos, cero errores y cero omitidas. Incluye 31 pruebas sin conexión SQL y siete de integración real. Se generó target/sistema-facturacion-1.0-SNAPSHOT.jar.

La integración comprobó el flujo hasta factura y consulta tras reinicio del servicio Java, precios históricos, persistencia de pagos y anulación sin afectar stock, rollback por stock insuficiente y fallo de FK, rechazo de despachos incompletos/repetidos y facturas duplicadas, y confirmaciones concurrentes.

En la primera ejecución se detectó y corrigió el subtotal nulo al construir un detalle únicamente con cantidad y precio. Una prueba de regresión verifica ahora el cálculo automático y la actualización por cambios de cantidad.

Después de las pruebas se consultó la base: sin órdenes, despachos, facturas ni movimientos residuales; un cliente CF, dos productos y 20 unidades de cada uno en Bodega Central, con cero reservas. Los casos de integración limpian sus propios datos.

## Interfaz y ensayo

Se verificaron automáticamente los controles de facturación y se inspeccionó su renderizado Swing con FlatDarkLaf. Esto no sustituye un ensayo manual completo desde los menús: para preparar la defensa, siga guion_entrega.md y cree una venta nueva desde la interfaz. No se ha afirmado una validación manual de ese recorrido.
