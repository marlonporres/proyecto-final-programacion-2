# Sistema de Ventas — Proyecto Final de Programación II

Aplicación de escritorio para una tienda de productos de computación, desarrollada con **Java Swing, JDBC y SQL Server**. Integra catálogos, inventario por bodega, órdenes de venta, despachos, facturación y consulta de la operación completa.

## Entrega y documento de defensa

<!-- Sustituya el destino del enlace siguiente por la URL completa del Google Doc de defensa. Compruebe que el ingeniero tenga permiso de lectura. -->
### [Abrir el documento de defensa y explicación del proyecto](REEMPLAZAR_CON_ENLACE_DE_GOOGLE_DOCS)

El documento de defensa complementa el código con la explicación del proyecto y las ayudas para la exposición. Este repositorio reúne la implementación, el instalador de la base de datos, las pruebas y la documentación técnica.

**Repositorio:** [marlonporres/proyecto-final-programacion-2](https://github.com/marlonporres/proyecto-final-programacion-2).

| Material | Contenido |
| --- | --- |
| [Guía de defensa local](docs/defensa_proyecto.md) | Explicación por integrante, preguntas y respuestas para la exposición. |
| [Alineación con la rúbrica](docs/alineacion_rubrica.md) | Correspondencia entre los criterios del ingeniero y la implementación. |
| [Guion de demostración](docs/guion_entrega.md) | Recorrido práctico para presentar el sistema. |
| [Registro de verificación](docs/verificacion_implementacion.md) | Resultados de pruebas, revisiones y evidencias técnicas. |
| [Catálogo de computación](docs/catalogo_computacion.md) | Productos ficticios, categorías y precios de referencia. |

## Funcionamiento y reglas principales

**Cliente → Orden de venta → Despacho pendiente → Despacho confirmado → Factura**

| Acción | Efecto sobre el inventario físico |
| --- | --- |
| Registrar entrada | Aumenta existencias y registra un movimiento ENTRADA. |
| Guardar una orden | Valida disponibilidad; no descuenta ni reserva existencias. |
| Generar un despacho | Guarda el despacho PENDIENTE con cero unidades entregadas; no cambia existencias. |
| Confirmar un despacho | Descuenta todas las cantidades y registra movimientos SALIDA en una misma transacción. |
| Emitir, pagar o anular una factura | No modifica existencias. |

- Cada orden se despacha completamente desde una sola bodega. Si falla una línea, se revierte toda la confirmación y el despacho permanece pendiente.
- Los precios se conservan en el detalle de la orden: cambiar el catálogo no altera una venta anterior.
- Se utilizan cantidades enteras, `BigDecimal`, IVA académico del 12% y redondeo `HALF_UP` a dos decimales. Los precios son antes de IVA; no se aplican descuentos.
- Una orden admite una sola factura, incluso si se anula. Los pagos se registran después de emitirla y pasa a PAGADA cuando cubren su total. Anular no implica devolver mercadería.
- Los números de orden, despacho y factura utilizan UUID. Las operaciones persistidas se recuperan desde SQL Server al reiniciar.

## Requisitos

- **JDK 25** o superior, con `JAVA_HOME` apuntando al JDK que utilizará Maven.
- **Maven 3.9+**, disponible en el `PATH`. Como alternativa, extraer la distribución completa de Maven 3.9.9 en `apache-maven-3.9.9`; una clonación no incluye sus JAR.
- **SQL Server** iniciado, conexión TCP a `localhost:1433` y autenticación SQL habilitada. El lanzador comprueba el servicio de la instancia predeterminada `MSSQLSERVER`.
- **SQL Server Management Studio** u otra herramienta para ejecutar los scripts SQL.

Las dependencias de FlatLaf, JDBC y JUnit se resuelven mediante [pom.xml](pom.xml). NetBeans puede abrir el proyecto directamente desde ese archivo.

## Preparación de la base de datos

### Instalación nueva

1. En SQL Server, ejecutar [script_base_datos_ventas.sql](script_base_datos_ventas.sql). Crea `SistemaVentas`, sus 14 tablas, relaciones, restricciones e índices; incluye un usuario académico admin, un cliente CF y 24 productos ficticios de computación en seis categorías.
2. Copiar [database.properties.example](src/main/resources/database.properties.example) como `src/main/resources/database.properties`.
3. Completar los datos de conexión locales:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=SistemaVentas;encrypt=false
db.user=su_usuario_sql
db.password=su_clave_local
```

`database.properties` está excluido de Git: **no publicar credenciales**. La configuración se carga desde el classpath; después de cambiarla, recompilar y reiniciar. La sesión utiliza el usuario admin de la base; no existe una pantalla de inicio de sesión.

### Si ya existe una base

- Para añadir los productos al esquema actualizado, usar [cargar_catalogo_computacion.sql](docs/cargar_catalogo_computacion.sql). Conserva ventas, precios y existencias existentes; repetirlo no duplica productos ni repone stock.
- Si el esquema fue instalado antes de separar generación y confirmación del despacho, aplicar [actualizar_despacho_pendiente.sql](docs/actualizar_despacho_pendiente.sql). Ajusta la restricción de cantidades sin borrar registros; las instalaciones nuevas ya incluyen el cambio.
- **No recrear una base con datos que se necesiten conservar.** [recrear_base_pruebas.sql](docs/recrear_base_pruebas.sql) elimina `SistemaVentas` y sus datos. Usarlo únicamente para un reinicio deliberado, con respaldo; después ejecutar el instalador. El instalador no migra automáticamente un esquema antiguo.

## Ejecutar en Windows

Desde la raíz del proyecto, en PowerShell, con la base instalada y la conexión configurada:

```powershell
# Primera ejecución: permite descargar las dependencias de Maven.
.\scripts\demostracion.cmd -EnLinea

# Ejecuciones posteriores: utiliza las dependencias descargadas, sin Internet.
.\scripts\demostracion.cmd

# Verificación completa: recompila y ejecuta también las pruebas con SQL Server.
.\scripts\demostracion.cmd -Modo Verificar
```

Si faltan dependencias al verificar, añadir `-EnLinea`. Si Maven utiliza una ubicación incorrecta, añadir `-RepositorioLocal 'C:\ruta\a\su\repositorio-maven'`.

El lanzador comprueba Java, Maven, la configuración y el servicio SQL Server. Ejecuta PowerShell con una política limitada a ese proceso; no modifica la política de Windows. Para ejecutar directamente con Maven:

```powershell
mvn clean test
mvn compile exec:java
```

La clase principal es `gt.edu.umg.ventas.SistemaVentas`. Los generadores Python históricos no forman parte de la construcción; no ejecutarlos sobre los formularios actuales.

## Recorrido para la demostración

1. **Catálogos:** registrar o seleccionar un cliente; consultar categorías, productos y bodegas.
2. **Inventario:** consultar existencias por bodega. Si se necesita abastecer, registrar una entrada con cantidad entera positiva y referencia.
3. **Ventas → Nueva Orden de Venta:** seleccionar productos activos y cantidades disponibles; guardar la orden PENDIENTE.
4. **Inventario → Despachos:** seleccionar la orden y una bodega con todas las existencias; generar el despacho PENDIENTE y comprobar que el stock sigue igual. Se puede cerrar la ventana y recuperar el despacho.
5. **Confirmar despacho:** verificar la orden COMPLETADA, el despacho CONFIRMADO y los movimientos SALIDA. La bodega queda fijada desde la generación.
6. **Ventas → Facturación de órdenes despachadas:** cargar la orden y emitir la factura; cliente, productos, cantidades y precios provienen de la orden y no son editables.
7. **Ventas → Consultar Órdenes de Venta:** filtrar por fechas y abrir **Ver operación** para revisar orden, despacho, movimientos y factura.
8. Reiniciar la aplicación y recuperar la operación para demostrar persistencia.

Usar las existencias que muestre la base al iniciar, no asumir que siguen iguales a las de instalación. Si hay `S` unidades y se despachan 5, deben quedar `S − 5`; emitir la factura mantiene ese resultado.

## Arquitectura

Los paquetes se encuentran bajo `src/main/java/gt/edu/umg/ventas`:

| Paquete | Responsabilidad |
| --- | --- |
| `vista` | Formularios Swing y presentación de resultados. |
| `controlador` | Coordinación de acciones de la interfaz. |
| `servicio` | Reglas de negocio y transacciones. |
| `dao` | Consultas y persistencia mediante JDBC. |
| `modelo` | Entidades y proyecciones de consulta. |
| `util` | Sesión, moneda, diálogos y actualización de consultas abiertas. |

`ExistenciaInventario` es la fuente del stock físico por producto y bodega; `MovimientoInventario` registra entradas y salidas. Inventario y despacho utilizan una conexión compartida durante cada transacción. La factura vuelve a validar la orden al persistir; las restricciones de la base respaldan los controles contra duplicados. Las entradas también revalidan producto y bodega en la transacción para rechazar selecciones desactivadas después de abrir el formulario.

## Pruebas y evidencia

La **última verificación documentada, del 3 de octubre de 2026**, registró **50 pruebas exitosas: 36 sin SQL Server y 14 de integración**, sin errores ni omisiones en la ejecución completa. Consultar el [registro de verificación](docs/verificacion_implementacion.md) para los comandos y resultados de esa revisión.

```powershell
# Pruebas sin SQL Server; las 14 de integración se omiten explícitamente.
mvn clean test

# Suite completa: requiere SQL Server, esquema y conexión configurados.
mvn '-Dventas.it=true' clean verify
```

Las pruebas cubren el flujo hasta factura, precios históricos, stock insuficiente y reversión, duplicados, confirmaciones concurrentes, fechas, selecciones desactivadas y recuperación en otro proceso Java. El ensayo Swing acciona menús y botones reales con respuestas de prueba para los diálogos. Las pruebas de integración crean sus propios datos y limpian únicamente esos registros; con integración habilitada, una conexión inaccesible hace fallar la suite.

Para revisar la base sin modificar registros: [verificar_operacion.sql](docs/verificar_operacion.sql) y [verificar_integridad.sql](docs/verificar_integridad.sql).

## Alcance académico

Esta entrega implementa el sistema de escritorio con despachos completos y facturación posterior. Android, API, reservas de stock, entregas parciales entre bodegas y devoluciones físicas quedan fuera del alcance actual. La columna de reservas permanece en cero; los borradores de factura son temporales y las facturas emitidas se guardan en SQL Server.

La matriz y las pruebas aportan evidencia técnica; la defensa y el dominio del proyecto deben prepararse entre todos los integrantes. Antes de entregar el enlace del repositorio, publicar los cambios en `main`, sustituir el enlace del documento de defensa y comprobar el acceso del ingeniero a ambos recursos.
