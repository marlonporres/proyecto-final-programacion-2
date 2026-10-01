-- Consulta de solo lectura. Sustituya el número por la orden creada desde Swing.
USE SistemaVentas;
GO
DECLARE @numero_orden VARCHAR(50) = 'OV-REEMPLAZAR';

SELECT o.id, o.numero_orden, o.fecha, c.nit, c.nombre AS cliente,
       o.estado AS estado_orden, o.total AS total_orden,
       d.numero_despacho, d.estado AS estado_despacho, b.nombre AS bodega,
       f.numero AS factura, f.estado AS estado_factura, f.total AS total_factura
FROM dbo.OrdenVenta o
JOIN dbo.cliente c ON c.id_cliente = o.id_cliente
LEFT JOIN dbo.Despacho d ON d.id_orden = o.id
LEFT JOIN dbo.Bodega b ON b.id = d.id_bodega
LEFT JOIN dbo.factura f ON f.id_orden = o.id
WHERE o.numero_orden = @numero_orden;

SELECT p.codigo, p.nombre, od.cantidad AS solicitado, dd.cantidad_despachada,
       od.precio_unitario AS precio_orden, fd.precio_unitario AS precio_factura,
       m.tipo_movimiento, m.cantidad AS salida, m.referencia,
       ei.existencia_actual, ei.existencia_reservada,
       ei.existencia_actual - ei.existencia_reservada AS disponible
FROM dbo.OrdenVenta o
JOIN dbo.DetalleOrdenVenta od ON od.id_orden = o.id
JOIN dbo.producto p ON p.id_producto = od.id_producto
LEFT JOIN dbo.Despacho d ON d.id_orden = o.id
LEFT JOIN dbo.DetalleDespacho dd ON dd.id_despacho = d.id AND dd.id_producto = p.id_producto
LEFT JOIN dbo.MovimientoInventario m ON m.id_producto = p.id_producto AND m.id_bodega = d.id_bodega
    AND m.tipo_movimiento = 'SALIDA' AND m.referencia = CONCAT('Despacho ', d.numero_despacho)
LEFT JOIN dbo.ExistenciaInventario ei ON ei.id_producto = p.id_producto AND ei.id_bodega = d.id_bodega
LEFT JOIN dbo.factura f ON f.id_orden = o.id
LEFT JOIN dbo.detalle_factura fd ON fd.factura_id = f.id_factura AND fd.producto_id = p.id_producto
WHERE o.numero_orden = @numero_orden;
