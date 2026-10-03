# Sistema de Ventas de Programación II

Aplicación de escritorio en Java Swing y SQL Server para demostrar el proceso completo de ventas. El flujo es:

Cliente → Orden de venta → Despacho pendiente → Despacho confirmado → Salida de inventario → Factura

La orden valida existencias, pero no las modifica. Al confirmar el despacho se rebajan las cantidades y se registra la salida dentro de la misma transacción. La factura es el documento comercial posterior y nunca modifica inventario.

Generar el despacho guarda encabezado y detalles en estado PENDIENTE, con cero unidades despachadas, y no mueve ni reserva stock. Puede cerrarse la ventana y recuperarse el mismo despacho para confirmarlo después.

## Requisitos e instalación

- JDK 25 y Maven 3.9+. Una clonación de GitHub no incluye los JAR de Maven: instale Maven y agregue su carpeta `bin` al `PATH`, o extraiga la distribución completa 3.9.9 en `apache-maven-3.9.9`.
- SQL Server con el servicio iniciado, conexión TCP a localhost:1433 y autenticación SQL configurada.
- Una base limpia denominada SistemaVentas.

En SQL Server Management Studio, ejecute `script_base_datos_ventas.sql`. El script instala tablas, relaciones, restricciones e índices y carga un usuario admin, un cliente CF y 24 productos ficticios de computación en seis categorías. Bodega Central incluye existencias iniciales de 6 a 40 unidades según el producto; TEC-001 empieza con 20.

Si la base ya tiene el esquema actualizado, agregue el catálogo con `docs/cargar_catalogo_computacion.sql`, sin recrearla. Esta carga conserva ventas, precios y existencias anteriores y puede repetirse sin duplicar ni reponer stock. El detalle de productos y precios está en `docs/catalogo_computacion.md`.

Si instaló el esquema antes de separar generación y confirmación, ejecute una vez `docs/actualizar_despacho_pendiente.sql`. Actualiza únicamente la restricción de cantidades, sin borrar ventas, productos, stock ni despachos confirmados. Las instalaciones nuevas ya incluyen ese cambio.

Si conserva el esquema anterior y decidió recrear los datos de prueba, ejecute primero `docs/recrear_base_pruebas.sql`. Ese script borra exclusivamente la base SistemaVentas y sus datos; después debe ejecutar el instalador. El instalador no migra registros anteriores ni elimina automáticamente una base existente.

Cree `src/main/resources/database.properties` a partir de su archivo de ejemplo y configure sus credenciales locales:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=SistemaVentas;encrypt=false
db.user=sa
db.password=su_clave_local
```

Este archivo está excluido de Git. La configuración se carga desde el classpath; no se usan contraseñas embebidas ni variables DB_HOST/DB_PASSWORD. Al cambiar el archivo, reinicie la aplicación. La sesión académica utiliza el usuario admin de la base; no implementa autenticación interactiva.

## Compilación y ejecución

Desde la raíz, en PowerShell con Maven instalado en el `PATH`:

```powershell
mvn clean test
mvn exec:java
```

Si extrajo Maven dentro del proyecto, sustituya `mvn` por `.\apache-maven-3.9.9\bin\mvn.cmd`. Si Maven resuelve una carpeta local incorrecta, indique la ubicación de sus dependencias:

```powershell
mvn '-Dmaven.repo.local=C:\Users\marlo\.m2\repository' test
```

NetBeans reconoce `pom.xml` y ejecuta `gt.edu.umg.ventas.SistemaVentas`. Los generadores Python históricos no forman parte del proceso de construcción; no los ejecute sobre los formularios implementados.

Para la demostración, el comando siguiente revisa Java, la configuración y el servicio SQL Server, compila desde cero y ejecuta todas las pruebas:

```powershell
.\scripts\demostracion.cmd -Modo Verificar
```

Para abrir el sistema:

```powershell
.\scripts\demostracion.cmd
```

El lanzador `.cmd` ejecuta `demostracion.ps1` con una política limitada a ese proceso; no cambia la política de ejecución de Windows. Utiliza Maven local si está completo o `mvn.cmd` del `PATH`. Comprueba el Java de `JAVA_HOME` cuando está configurado, igual que Maven.

El script utiliza las dependencias ya descargadas, sin necesitar Internet. En una computadora nueva, añada `-EnLinea` para permitir que Maven descargue las dependencias. Si Maven resuelve una carpeta incorrecta, use `-RepositorioLocal 'C:\Users\marlo\.m2\repository'`, adaptando la ruta a su usuario. La matriz contra el documento del ingeniero está en `docs/alineacion_rubrica.md`.

## Uso

1. Registre o seleccione un cliente en Catálogos. También puede crear y actualizar categorías y productos. Los catálogos de una nueva orden se recargan al activar su ventana.
2. Consulte inventario por bodega. Para abastecer un producto nuevo, use Registrar entrada con una cantidad entera positiva y una referencia.
3. En Ventas > Nueva Orden de Venta, seleccione productos activos y cantidades disponibles. Guardar confirma la solicitud y la deja PENDIENTE, lista para despacho. Se conserva el precio unitario de ese momento.
4. En Inventario > Despachos, seleccione la orden y una bodega activa que disponga de todas las cantidades. Pulse Generar orden de despacho: se guarda PENDIENTE y el stock no cambia. Esta entrega admite un único despacho completo; no distribuye una orden entre varias bodegas. La bodega queda fijada al generarlo.
5. Pulse Confirmar despacho. La orden pasa a COMPLETADA; el despacho pasa a CONFIRMADO, se actualizan sus cantidades entregadas y se rebaja stock con un movimiento SALIDA por producto. Si una línea falla, se revierte toda la confirmación y el despacho sigue PENDIENTE, disponible para reintentar después de abastecer.
6. En Ventas > Facturación de órdenes despachadas, cargue una orden de la lista y emita el documento. Cliente, productos, cantidades y precios provienen de la orden y no son editables.
7. En Ventas > Consultar Órdenes de Venta, busque por fechas y use Ver operación para comprobar productos, despacho, movimiento y factura. Las consultas abiertas se actualizan después de guardar operaciones.

Orden y factura usan cantidades enteras, BigDecimal, IVA académico de 12% y redondeo HALF_UP a dos decimales. El precio es el valor antes del IVA. No se ofrecen descuentos en este flujo. Los números OV, DSP y FAC utilizan UUID para evitar colisiones entre sesiones.

Existe una factura por orden, incluso si el documento se anula. Los pagos se admiten después de emitir; el estado pasa a PAGADA cuando cubren el total. Anular cancela solo el documento comercial y no representa devolución de mercadería.

## Arquitectura y persistencia

Los paquetes están bajo `gt.edu.umg.ventas`:

- `vista`: formularios Swing y representación de resultados.
- `controlador`: coordinación de acciones de la interfaz.
- `servicio`: reglas de órdenes, inventario, despacho y factura.
- `dao`: consultas y persistencia JDBC.
- `modelo`: entidades y proyecciones de consulta.
- `util`: sesión, moneda y actualización de consultas abiertas.

Los servicios de inventario y despacho comparten una conexión JDBC durante la transacción. La factura bloquea la orden al persistir y comprueba otra vez que esté despachada y no tenga factura. Las restricciones UNIQUE de la base respaldan los controles contra operaciones duplicadas.

ExistenciaInventario es la única fuente de stock físico por producto y bodega. La columna reservada está preparada para futuras reservas y permanece en cero en este alcance. MovimientoInventario registra entradas y salidas; las salidas referencian el número único del despacho. Los borradores de factura son temporales en memoria; las facturas emitidas, pagos y relaciones quedan en SQL Server y se recuperan al reiniciar.

## Pruebas

`mvn test` ejecuta 36 pruebas sin SQL y omite explícitamente las 14 pruebas de SQL Server. Para comprobar las 50 pruebas, primero instale el esquema y configure la conexión; después ejecute:

```powershell
mvn '-Dventas.it=true' test
```

Con ventas.it=true, una base inaccesible hace fallar la suite; no se presenta como una integración exitosa con cero pruebas. Cada caso crea sus propios clientes, categorías, productos y bodegas y limpia únicamente esos datos al terminar.

Los casos cubren persistencia hasta factura, consulta tras reinicio, precios históricos, stock insuficiente con rollback, despacho parcial y repetido, factura duplicada, error de FK al emitir y confirmaciones concurrentes.

También se comprueban las fechas escritas sin salir del campo, días inexistentes, límites inclusivos del rango de fechas, errores de DAO, relaciones completas de inventario y productos desactivados después de seleccionar una línea. Las entradas vuelven a validar producto y bodega en SQL Server dentro de la transacción, rechazando selecciones que otro mantenimiento desactivó sin alterar stock ni movimientos. Una prueba lanza otro proceso Java para recuperar la orden, despacho y factura desde SQL Server, sin compartir el caché de la JVM original.

El ensayo Swing acciona los menús y botones reales con datos propios: cliente, inventario, orden, generación y recuperación del despacho pendiente, confirmación, factura, consulta de trazabilidad y recuperación con un controlador nuevo. Los diálogos reciben respuestas de prueba; la aplicación normal conserva JOptionPane. No es una certificación del dominio del grupo ni un ensayo manual humano.

La demostración manual y las preguntas para preparar la defensa están en `docs/guion_entrega.md`. Las consultas de comprobación de solo lectura están en `docs/verificar_operacion.sql`.

`docs/verificar_integridad.sql` comprueba restricciones, stock, estados, detalles y salidas relacionadas; no modifica registros.

La guía individual, preguntas con respuestas y checklist de la rúbrica están en `docs/defensa_proyecto.md`.

La verificación realizada, los resultados de integración y la ubicación del respaldo previo a la recreación están en `docs/verificacion_implementacion.md`.

## Alcance de esta entrega

Aplicación de escritorio, despachos completos y facturación posterior. Aplicación Android, API, reservas de stock, entregas parciales y devoluciones físicas quedan para una fase posterior.
