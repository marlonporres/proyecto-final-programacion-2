# Verificación de la implementación

Última verificación: 29 de septiembre de 2026.

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
