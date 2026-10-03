# Preparación de la defensa — Sistema de Ventas

Esta guía prepara la explicación del grupo; no acredita asistencia ni dominio individual. Cada integrante debe practicar el recorrido completo y responder sobre cualquier parte, no solamente sobre su módulo.

## Demostración de 8 a 10 minutos

1. Abra el sistema y registre o seleccione un cliente. Muestre un producto de computación y anote su existencia inicial S en la bodega elegida.
2. Confirme/guarde una orden de cinco unidades: muestre número OV, cliente, detalles, precio e IVA. El stock debe seguir en S.
3. En Despachos seleccione la orden y bodega. Genere la orden de despacho: muestre el DSP PENDIENTE y cantidades despachadas en cero. El stock continúa en S.
4. Cierre y reabra Despachos. Recupere el mismo DSP: demuestra persistencia de la generación, no una reconstrucción temporal.
5. Confirme el despacho. La orden queda COMPLETADA, el DSP CONFIRMADO y la existencia S−5.
6. Cargue la orden despachada en Facturación y emita. Muestre el FAC y su total; el stock sigue en S−5.
7. Consulte la operación por fecha y abra Ver operación: identifique OV, DSP, producto, bodega, SALIDA y FAC.
8. Reinicie realmente la aplicación y consulte la factura por número. Si el docente lo pide, consulte los registros con verificar_operacion.sql; no modifique la base para completar la venta.

Ejemplo monetario con TEC-001: 5 × Q95.00 = Q475.00; IVA Q57.00; total Q532.00. No presuponga existencia inicial 20: si hay 19, al confirmar deben quedar 14.

## Preguntas que todos deben poder responder

| Pregunta | Respuesta que debe poder explicar |
|---|---|
| ¿Por qué guardar la orden no rebaja stock? | La orden confirma la solicitud comercial; no se ha entregado mercadería. La salida ocurre únicamente al confirmar el despacho. |
| ¿Qué diferencia hay entre generar y confirmar el despacho? | Generar persiste encabezado/detalles PENDIENTES, con cero entregado. Confirmar registra entrega completa, SALIDAS y stock, y completa la orden en una transacción. |
| ¿Generar reserva inventario? | No. La columna reservada permanece en cero en este alcance. Se valida disponibilidad otra vez al confirmar porque otras ventas pueden consumirla. |
| ¿Qué pasa si falta stock en la segunda línea? | Rollback de toda la confirmación: la primera rebaja y su movimiento también se revierten. El DSP y la orden siguen pendientes; se puede abastecer y reintentar el mismo DSP. |
| ¿Cómo evitan confirmar dos veces? | Bloqueos de la orden y el despacho, validación del estado PENDIENTE, actualización condicional y una única relación DSP por orden. |
| ¿Cómo evitan stock negativo con ventas simultáneas? | UPDATE condicional exige existencia disponible suficiente. Si no actualiza una fila, se rechaza y revierte; las líneas se procesan por ID de producto para mantener un orden consistente de bloqueos. |
| ¿Dónde se guarda el stock físico? | ExistenciaInventario: una fila única por producto y bodega. MovimientoInventario registra la trazabilidad de entradas y salidas. |
| ¿Qué relaciona la factura con la operación? | factura.id_orden es una FK y UNIQUE. La orden relaciona cliente, usuario y detalles; Despacho también referencia esa misma orden. |
| ¿Se puede facturar una orden pendiente o un despacho pendiente? | No. Se requiere orden COMPLETADA y despacho CONFIRMADO, sin factura anterior. |
| ¿Por qué usan BigDecimal? | Permite cálculos decimales monetarios con redondeo explícito HALF_UP a dos decimales. Orden y factura aplican IVA académico del 12%. |
| ¿Qué pasa si cambia el precio del catálogo? | Se conserva el precio unitario del detalle de la orden. Emitir recarga ese origen persistido; no toma el precio actual del producto. |
| ¿Se puede emitir otra factura si la primera fue anulada? | No en este alcance: existe una factura por orden, incluso anulada. Anular no devuelve mercancía ni revierte stock. |
| ¿Qué hace cada capa? | Vista presenta y recibe acciones; controlador coordina; servicio aplica reglas y transacciones; DAO consulta/persiste; modelo representa entidades y valores; util contiene sesión, moneda y diálogos. |
| ¿Cómo se demuestra persistencia? | Cerrando/reabriendo el módulo para el DSP pendiente y reiniciando la aplicación para recuperar la factura desde SQL Server. |
| ¿La aplicación tiene login, Android o API? | La sesión académica usa admin persistido. No hay login interactivo. Android/API son una fase posterior, fuera de los cinco puntos de esta entrega. |

## Ubicación del código para la explicación

- Modelo: OrdenVenta, DetalleOrdenVenta, Despacho, DetalleDespacho, Factura, Producto, Cliente y enumeraciones en modelo.
- Confirmar/guardar orden: FrmOrdenVenta → OrdenVentaController → OrdenVentaService → OrdenVentaDAOImpl.
- Generar/confirmar despacho: FrmDespacho → DespachoController → DespachoService; DespachoDAOImpl recupera el documento guardado.
- Salida física: InventarioService.registrarSalida y MovimientoInventario.
- Emisión posterior: FacturaController → FacturaService → FacturaDAOImpl.
- Consulta de relaciones: FrmConsultaOrdenesVenta → ConsultaOrdenVentaController → ConsultaOperacionService.
- Base: script_base_datos_ventas.sql; actualización no destructiva: actualizar_despacho_pendiente.sql.
- Evidencia automatizada: IntegracionRealDbTest y EnsayoInterfaz en src/test/java/gt/edu/umg/ventas/integracion.

## Checklist de los cinco puntos

La matriz detallada contra el PDF «Entrega Final del Proyecto de escritorio - Programación II», revisado el 2 de octubre, está en `alineacion_rubrica.md`. La suite actual ejecuta 50 pruebas, incluidas 14 de SQL Server y recuperación de la operación desde otro proceso Java.

| Criterio | Evidencia a mostrar |
|---|---|
| Modelo y BD — 0.75 | PK/FK/UNIQUE, tablas de encabezado/detalle y registros de la venta nueva. |
| POO y organización — 0.75 | Una entidad con atributos privados, el recorrido por capas y una regla implementada en servicio. |
| Interfaz y mantenimientos — 0.50 | Menús, cliente, catálogo de computación, inventario, generación/confirmación y consulta. |
| Flujo completo — 1.50 | Venta nueva íntegra desde la interfaz, sin completar registros manualmente por SQL. |
| Reglas y consistencia — 0.75 | Stock sin cambio al guardar/generar; S−5 al confirmar; rechazo de stock insuficiente y duplicados. |
| Presentación y dominio — 0.75 | Todos presentes y capaces de explicar código, relaciones, consultas y proceso. |

## Ensayo individual obligatorio para cerrar la preparación humana

- Cada integrante ejecuta la demostración sin ayuda y anota OV, DSP y FAC.
- Otro integrante pide explicar una clase, una FK y el rollback sin consultar esta guía.
- Cambien quién hace cada paso. Ningún tema queda reservado exclusivamente a una persona.
- Registren nombre, fecha y resultado de cada ensayo. Si alguien no puede completar el flujo o explicar una regla, repitan ese ejercicio antes de la revisión.

La evidencia técnica automatizada puede verificarse con Maven. El dominio del grupo solo queda validado después de estos ensayos humanos; no se garantiza una calificación.
