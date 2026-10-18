# Sistema de Ventas — Proyecto Final de Programación II

Aplicación de escritorio para una tienda de productos de computación, desarrollada con **Java Swing, JDBC y SQL Server**. Integra catálogos, inventario por bodega, órdenes de venta, despachos, facturación y consulta de la operación completa.

**Accesos rápidos:** [Preparación de la base](#preparación-de-la-base-de-datos) · [Ejecutar en Windows](#ejecutar-en-windows) · [Demostración](#recorrido-para-la-demostración) · [Arquitectura](#arquitectura) · [Pruebas y evidencia](#pruebas-y-evidencia).

## Cambio en caliente solicitado por el ingeniero

### [Abrir informe del cambio en caliente en clientes](https://drive.google.com/file/d/11uArrGy9h7B7cHDLe28EFtkR2DecaD90/view?usp=sharing)

Informe del **17 de octubre de 2026** sobre la administración de **múltiples teléfonos por cliente con número y tipo**. Compara el commit `7ed6da9` con los cambios locales de esa revisión y explica las clases modificadas, el recorrido **Vista → Controlador → Servicio → DAO → Base de datos**, la verificación realizada y los puntos pendientes para la defensa técnica.

## Entrega y documento de defensa

### [Abrir el documento de defensa y explicación del proyecto](https://docs.google.com/document/d/1lGquripMr4y55byKVBEs98gh69SZ7zLL6HuliuXVjfM/edit)

El documento de defensa complementa el código con la explicación del proyecto y las ayudas para la exposición. Este repositorio reúne la implementación, el instalador de la base de datos, las pruebas y la documentación técnica.

**Repositorio:** [marlonporres/proyecto-final-programacion-2](https://github.com/marlonporres/proyecto-final-programacion-2).

| Material | Contenido |
| --- | --- |
| [Guía de defensa local](docs/defensa_proyecto.md) | Explicación por integrante, preguntas y respuestas para la exposición. |
| [Alineación con la rúbrica](docs/alineacion_rubrica.md) | Correspondencia entre los criterios del ingeniero y la implementación. |
| [Guion de demostración](docs/guion_entrega.md) | Recorrido práctico para presentar el sistema. |
| [Registro de verificación](docs/verificacion_implementacion.md) | Resultados de pruebas, revisiones y evidencias técnicas. |
| [Catálogo de computación](docs/catalogo_computacion.md) | Productos ficticios, categorías y precios de referencia. |

## Cambios recientes en clientes

La primera ampliación incorporó varios números por cliente. La versión actual añade su **tipo** y una capa de servicio, conservando la relación **un cliente → muchos teléfonos**, sin campos fijos como `telefono1`, `telefono2` o `telefono3`.

| Área | Implementación actual |
| --- | --- |
| Modelo | [Cliente](src/main/java/gt/edu/umg/ventas/modelo/Cliente.java) contiene una `List<Telefono>`. La nueva clase [Telefono](src/main/java/gt/edu/umg/ventas/modelo/Telefono.java) representa identificadores, número y tipo. |
| Servicio y controlador | [ClienteService](src/main/java/gt/edu/umg/ventas/servicio/ClienteService.java) concentra validaciones y depuración; [ClienteController](src/main/java/gt/edu/umg/ventas/controlador/ClienteController.java) delega las operaciones en el servicio. |
| Interfaz | [FrmClientes](src/main/java/gt/edu/umg/ventas/vista/FrmClientes.java) permite añadir, quitar y mostrar teléfonos con los tipos **Personal, Trabajo, Casa u Otro**. Reingresar el mismo número permite cambiar su tipo sin duplicarlo. |
| Persistencia | [ClienteDAOImpl](src/main/java/gt/edu/umg/ventas/dao/ClienteDAOImpl.java) guarda y consulta número y tipo en `dbo.cliente_telefono`. Al actualizar, reemplaza la colección dentro de la transacción del cliente. |
| Compatibilidad | El primer número se conserva en `cliente.telefono` como principal. Los métodos que reciben o devuelven cadenas siguen disponibles; los números sin tipo usan **Personal**. |

La lista no tiene un máximo fijo de teléfonos; el límite de **20 caracteres se aplica a cada número**. El servicio conserva NIT y nombre obligatorios, control de NIT duplicado y límites de longitud de los datos del cliente.

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

1. En SQL Server, ejecutar [script_base_datos_ventas.sql](script_base_datos_ventas.sql). El esquema actual crea `SistemaVentas` y **15 tablas**, incluida `cliente_telefono`, con sus relaciones, restricciones e índices. Incluye un usuario académico admin, un cliente CF y 24 productos ficticios de computación en seis categorías.
2. Copiar [database.properties.example](src/main/resources/database.properties.example) como `src/main/resources/database.properties`.
3. Completar los datos de conexión locales:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=SistemaVentas;encrypt=false
db.user=su_usuario_sql
db.password=su_clave_local
```

`database.properties` está excluido de Git: **no publicar credenciales**. La configuración se carga desde el classpath; después de cambiarla, recompilar y reiniciar. La sesión utiliza el usuario admin de la base; no existe una pantalla de inicio de sesión.

### Si ya existe una base

- **Clientes y teléfonos:** en el primer uso del DAO de clientes, la aplicación comprueba el esquema. Si falta `cliente_telefono`, la crea y copia los teléfonos anteriores como **Personal**; si la tabla existe sin `tipo`, agrega esa columna con el mismo valor para los registros existentes. Esta adaptación requiere permisos para crear o modificar tablas cuando sean necesarios; no es una migración general del sistema.
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
mvn compile exec:java
```

La clase principal es `gt.edu.umg.ventas.SistemaVentas`. Los generadores Python históricos no forman parte de la construcción; no ejecutarlos sobre los formularios actuales.

## Recorrido para la demostración

### Cambio en caliente de clientes

1. Abrir **Catálogos → Clientes** y registrar un cliente o seleccionar uno existente.
2. Introducir un número, elegir **Personal**, pulsar **Añadir Teléfono** y agregar otro de tipo **Trabajo**.
3. Guardar y volver a seleccionar el cliente para comprobar que aparecen ambos números con sus tipos. Cerrar y abrir la aplicación para demostrar su recuperación desde SQL Server.
4. Para cambiar un tipo, reingresar el mismo número, seleccionar otro tipo y pulsar **Añadir Teléfono**. Para eliminar un número, seleccionarlo en la lista y pulsar **Quitar**; guardar los cambios.

Estos pasos son el guion de la demostración; su ejecución completa con la nueva clasificación sigue pendiente de verificación.

### Venta completa

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
| `servicio` | Validaciones y coordinación de operaciones; inventario y despacho coordinan sus transacciones. |
| `dao` | Sentencias SQL, acceso JDBC y mapeo de resultados; clientes guarda su cabecera y teléfonos en una transacción propia. |
| `modelo` | Entidades y proyecciones de consulta, incluidos `Cliente` y `Telefono`. |
| `util` | Sesión, moneda, diálogos y actualización de consultas abiertas. |

**Flujo del módulo de clientes:**

```text
FrmClientes → ClienteController → ClienteService → ClienteDAO / ClienteDAOImpl → SQL Server
```

Las sentencias SQL se concentran en los DAO. Los servicios de inventario y despacho conservan la coordinación de sus transacciones y pasan una conexión compartida a los DAO que participan en ellas.

`ExistenciaInventario` es la fuente del stock físico por producto y bodega; `MovimientoInventario` registra entradas y salidas. La factura vuelve a validar la orden al persistir; las restricciones de la base respaldan los controles contra duplicados. Las entradas también revalidan producto y bodega en la transacción para rechazar selecciones desactivadas después de abrir el formulario.

## Pruebas y evidencia

| Fecha | Verificación realizada | Resultado y alcance |
| --- | --- | --- |
| **17 de octubre de 2026** | Prueba focal `testClienteControllerValidacionesYMultiplesTelefonos`. | **1 prueba aprobada**, sin fallos, errores ni omisiones. Comprueba validaciones, múltiples teléfonos y su presentación con tipos mediante un DAO simulado. |
| **3 de octubre de 2026** | Última verificación completa documentada en el [registro de verificación](docs/verificacion_implementacion.md). | **50 pruebas aprobadas:** 36 sin SQL Server y 14 de integración. Es evidencia histórica anterior a los cambios recientes. |

La prueba focal del 17 de octubre no verifica la migración del esquema, la persistencia real de los tipos en SQL Server ni la interacción visual. La suite completa todavía debe ejecutarse sobre los cambios actuales; el resultado histórico no acredita esa nueva ejecución.

```powershell
# Prueba focal de clientes, con DAO simulado y sin SQL Server.
mvn '-Dventas.it=false' '-Dtest=DespachoServiceTest#testClienteControllerValidacionesYMultiplesTelefonos' test

# Pruebas sin SQL Server; las de integración quedan deshabilitadas.
mvn clean test

# Suite completa: requiere SQL Server, esquema y conexión configurados.
mvn '-Dventas.it=true' clean verify
```

La suite incluye casos de flujo hasta factura, precios históricos, stock insuficiente y reversión, duplicados, confirmaciones concurrentes, fechas, selecciones desactivadas y recuperación en otro proceso Java. El ensayo Swing acciona menús y botones reales con respuestas de prueba para los diálogos. Las pruebas de integración crean sus propios datos y limpian únicamente esos registros; con integración habilitada, una conexión inaccesible hace fallar la suite. Los reportes de cada ejecución se generan en `target/surefire-reports` y no se versionan.

Para revisar la base sin modificar registros: [verificar_operacion.sql](docs/verificar_operacion.sql) y [verificar_integridad.sql](docs/verificar_integridad.sql).

## Alcance académico

Esta entrega implementa el sistema de escritorio con despachos completos y facturación posterior. Android, API, reservas de stock, entregas parciales entre bodegas y devoluciones físicas quedan fuera del alcance actual. La columna de reservas permanece en cero; los borradores de factura son temporales y las facturas emitidas se guardan en SQL Server.

**Pendientes del cambio en caliente:** actualmente se puede guardar un cliente sin teléfonos; si se exige al menos uno, falta aplicar esa regla. La vista ofrece los cuatro tipos solicitados, pero el servicio y la base no restringen estrictamente otros valores recibidos fuera de esa interfaz. También queda por demostrar el guardado, la consulta y la edición de los tipos con SQL Server y la interfaz real.

Antes de entregar, ejecutar la verificación completa sobre la versión actual, ensayar la demostración y la defensa entre los integrantes, publicar los cambios correspondientes y comprobar que el ingeniero puede abrir los dos documentos enlazados al inicio.
