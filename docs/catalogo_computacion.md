# Catálogo ficticio de computación

El catálogo de muestra contiene 24 productos, organizados en seis categorías. Las marcas son genéricas, las especificaciones son ilustrativas y los precios en quetzales son ficticios, antes del IVA académico del 12%. No son cotizaciones comerciales.

| Código | Producto | Categoría | Precio sin IVA | Stock inicial nuevo |
|---|---|---|---:|---:|
| TEC-001 | Mouse inalámbrico | Periféricos | Q95.00 | 20 |
| PER-002 | Teclado USB de oficina | Periféricos | Q120.00 | 25 |
| PER-003 | Teclado mecánico compacto | Periféricos | Q285.00 | 12 |
| PER-004 | Combo teclado y mouse inalámbricos | Periféricos | Q185.00 | 18 |
| EQP-001 | Laptop Oficina 15 | Equipos de cómputo | Q3495.00 | 8 |
| EQP-002 | Laptop Estudio 14 | Equipos de cómputo | Q4895.00 | 6 |
| EQP-003 | PC Escritorio Pro | Equipos de cómputo | Q4295.00 | 7 |
| EQP-004 | Mini PC Compacta | Equipos de cómputo | Q2495.00 | 10 |
| MON-001 | Monitor 24 Full HD | Monitores | Q1099.00 | 12 |
| MON-002 | Monitor 27 Full HD | Monitores | Q1449.00 | 8 |
| MON-003 | Monitor 27 QHD | Monitores | Q2399.00 | 6 |
| MON-004 | Soporte articulado para monitor | Monitores | Q220.00 | 15 |
| CMP-001 | Memoria RAM DDR4 8 GB | Componentes | Q185.00 | 24 |
| CMP-002 | Memoria RAM DDR4 16 GB | Componentes | Q335.00 | 18 |
| CMP-003 | SSD SATA 500 GB | Componentes | Q395.00 | 20 |
| CMP-004 | SSD NVMe 1 TB | Componentes | Q695.00 | 14 |
| RED-001 | Router Wi-Fi doble banda | Redes | Q399.00 | 10 |
| RED-002 | Adaptador Wi-Fi USB | Redes | Q159.00 | 22 |
| RED-003 | Switch Gigabit 8 puertos | Redes | Q299.00 | 10 |
| RED-004 | Cable de red Cat 6 de 3 m | Redes | Q45.00 | 40 |
| ACC-001 | Audífonos USB con micrófono | Accesorios | Q225.00 | 20 |
| ACC-002 | Cámara web Full HD | Accesorios | Q295.00 | 15 |
| ACC-003 | Hub USB de 4 puertos | Accesorios | Q145.00 | 25 |
| ACC-004 | Base ajustable para laptop | Accesorios | Q175.00 | 20 |

## Carga y conservación de datos

- Una base nueva creada con `script_base_datos_ventas.sql` incluye este catálogo y Bodega Central.
- Para una base existente con el esquema actualizado, ejecute `docs/cargar_catalogo_computacion.sql`. No vuelva a ejecutar el instalador ni recree la base para agregar estos productos.
- La carga es transaccional e idempotente: solo agrega códigos faltantes y existencias iniciales de los productos recién creados. Repetirla no duplica productos, movimientos ni repone unidades vendidas.
- Las existencias nuevas tienen un movimiento ENTRADA con referencia “Carga inicial catálogo computación”. Se conservan las ventas, los precios y el inventario que ya existían.
- El mouse TEC-001 de la semilla anterior pasa de General a Periféricos; mantiene su ID, precio y stock.
- El antiguo martillo FER-001 queda inactivo. Se conservan su nombre, existencias y relaciones para no alterar ventas históricas; puede seguir apareciendo en consultas de catálogo e inventario como registro inactivo.
- Los bloques delimitados CATALOGO COMPUTACION del instalador y de la carga incremental deben mantenerse iguales; una prueba automatizada verifica esa equivalencia.

## Verificación de esta carga

Se aplicó sobre la base local y se repitió para comprobar idempotencia. Resultado: 23 productos nuevos y 24 productos de computación activos, cuatro por categoría. Los datos anteriores de órdenes y facturas no cambiaron. El mouse y el martillo conservan sus 19 unidades existentes; no se restablecieron a 20. El martillo está inactivo para nuevas órdenes.

Después de la carga se ejecutó `mvn -Dventas.it=true package`: 39 pruebas aprobadas, incluidas las siete de SQL Server, sin fallos ni omisiones. Se verificaron 23 movimientos iniciales nuevos, la orden anterior conservada y cero productos de prueba IT residuales.

Si la aplicación ya estaba abierta, vuelva a abrir Productos o Inventario. La ventana de una nueva orden recarga sus catálogos al activarse.
