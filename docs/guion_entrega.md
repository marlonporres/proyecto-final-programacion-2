# Demostración y defensa del Sistema de Ventas

## Preparación

Inicie SQL Server y configure database.properties. Las instalaciones existentes anteriores al despacho pendiente requieren actualizar_despacho_pendiente.sql, sin recrear ni borrar la base. Ejecute Maven con ventas.it=true para incluir las ocho pruebas de integración.

El catálogo incluye 24 productos de computación. TEC-001 vale Q95.00 antes del IVA; una instalación nueva tiene 20 unidades, pero anote siempre la existencia real S antes de vender. Las cargas del catálogo no reponen unidades consumidas.

## Operación de demostración desde la interfaz

1. En Catálogos registre o seleccione un cliente.
2. En Inventario consulte TEC-001 en Bodega Central y anote S; debe disponer de al menos cinco unidades.
3. Cree una orden de cinco unidades y pulse Confirmar / Guardar Orden. Subtotal Q475.00, IVA Q57.00, total Q532.00. Anote el OV. El stock sigue en S.
4. Abra Despachos, seleccione OV y bodega, y pulse Generar orden de despacho. Anote DSP: queda PENDIENTE, con cinco solicitadas y cero despachadas. El stock sigue en S.
5. Cierre y reabra el módulo. Seleccione OV y compruebe que aparece el mismo DSP PENDIENTE, con la bodega fijada y generación deshabilitada.
6. Pulse Confirmar despacho y acepte. DSP pasa a CONFIRMADO, la orden a COMPLETADA y el stock a S−5. Con S=20 quedan 15; con S=19 quedan 14.
7. Abra Facturación de órdenes despachadas, cargue OV y emita. Anote FAC. Total Q532.00 y stock todavía S−5.
8. Consulte órdenes por fecha y abra Ver operación: OV, DSP, productos, cantidades, bodega, SALIDA y FAC deben estar relacionados.
9. Reinicie realmente la aplicación y consulte FAC por número. Deben conservarse detalles, precios y total.
10. Si el docente lo solicita, use verificar_operacion.sql con OV para mostrar registros. Esta consulta es solo lectura; no se necesita editar SQL para completar la venta.

## Casos negativos

- Cantidad superior al disponible: rechazo sin modificar stock.
- Orden guardada o DSP pendiente: no puede facturarse.
- Reabrir un DSP generado: no se genera otro; se confirma el mismo.
- Stock consumido por otra venta antes de confirmar: rollback completo; DSP permanece PENDIENTE y entregado cero.
- Después de una reposición por Registrar entrada puede reintentarse ese DSP.
- DSP ya confirmado: no se vuelve a descontar.
- OV ya facturada: no se emite una segunda factura, aunque se anule la primera.
- Precio cambiado en catálogo: la factura conserva el de la orden.

## Defensa del grupo

Utilice defensa_proyecto.md: contiene preguntas con respuestas, ubicación de clases, checklist de los seis criterios de la rúbrica y un ensayo individual. Todos los integrantes deben presentarse y poder responder sobre cualquier módulo.

Android/API no forman parte de los cinco puntos. La sesión admin es académica y no constituye un login interactivo.

## Qué se verificó y qué debe ensayar el grupo

El ensayo automatizado EnsayoInterfaz acciona menús, selecciones y botones Swing con JDBC real, sustituye las respuestas de los diálogos modales y comprueba persistencia y stock. Recupera el despacho al reabrir su ventana y la factura con un contenedor/controlador nuevos. Se renderizan las pantallas para inspección.

Esto no es una prueba humana de usabilidad ni un reinicio del proceso del sistema operativo. El grupo debe realizar los pasos anteriores manualmente, cerrar y relanzar la aplicación y practicar la explicación individual para completar su preparación presencial.
