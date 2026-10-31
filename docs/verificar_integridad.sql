-- Diagnóstico de solo lectura. Los listados de inconsistencias deben devolver cero filas.
USE SistemaVentas;
GO
SET NOCOUNT ON;

SELECT COUNT(*) AS tablas FROM sys.tables WHERE schema_id=SCHEMA_ID('dbo');
SELECT COUNT(*) AS productos_activos FROM dbo.producto WHERE activo=1;
SELECT p.codigo,p.nombre,b.nombre AS bodega,e.existencia_actual,e.existencia_reservada,
       e.existencia_actual-e.existencia_reservada AS disponible
FROM dbo.ExistenciaInventario e
JOIN dbo.producto p ON p.id_producto=e.id_producto
JOIN dbo.Bodega b ON b.id=e.id_bodega
WHERE p.codigo='TEC-001';

SELECT 'FK' AS tipo,name,is_disabled,is_not_trusted FROM sys.foreign_keys
WHERE is_disabled=1 OR is_not_trusted=1
UNION ALL
SELECT 'CHECK',name,is_disabled,is_not_trusted FROM sys.check_constraints
WHERE is_disabled=1 OR is_not_trusted=1;

SELECT * FROM dbo.ExistenciaInventario
WHERE existencia_actual<0 OR existencia_reservada<0 OR existencia_reservada>existencia_actual;

SELECT o.numero_orden,o.estado,d.numero_despacho,d.estado AS estado_despacho,f.numero AS factura
FROM dbo.OrdenVenta o
LEFT JOIN dbo.Despacho d ON d.id_orden=o.id
LEFT JOIN dbo.factura f ON f.id_orden=o.id
WHERE NOT EXISTS (SELECT 1 FROM dbo.DetalleOrdenVenta od WHERE od.id_orden=o.id)
   OR (d.estado='CONFIRMADO' AND o.estado<>'COMPLETADA')
   OR (f.id_factura IS NOT NULL AND (ISNULL(d.estado,'')<>'CONFIRMADO' OR f.total<>o.total));

SELECT d.numero_despacho,dd.id_producto,dd.cantidad_solicitada,dd.cantidad_despachada
FROM dbo.Despacho d
JOIN dbo.DetalleDespacho dd ON dd.id_despacho=d.id
LEFT JOIN dbo.DetalleOrdenVenta od ON od.id_orden=d.id_orden AND od.id_producto=dd.id_producto
WHERE od.id IS NULL OR dd.cantidad_solicitada<>od.cantidad
   OR (d.estado='PENDIENTE' AND dd.cantidad_despachada<>0)
   OR (d.estado='CONFIRMADO' AND dd.cantidad_despachada<>dd.cantidad_solicitada);

SELECT d.numero_despacho,dd.id_producto,dd.cantidad_despachada,COUNT(m.id) AS movimientos,
       COALESCE(SUM(m.cantidad),0) AS salida_registrada
FROM dbo.Despacho d
JOIN dbo.DetalleDespacho dd ON dd.id_despacho=d.id
LEFT JOIN dbo.MovimientoInventario m ON m.id_producto=dd.id_producto AND m.id_bodega=d.id_bodega
    AND m.tipo_movimiento='SALIDA' AND m.referencia=CONCAT('Despacho ',d.numero_despacho)
GROUP BY d.numero_despacho,d.estado,dd.id_producto,dd.cantidad_despachada
HAVING (d.estado='CONFIRMADO' AND (COUNT(m.id)<>1 OR COALESCE(SUM(m.cantidad),0)<>dd.cantidad_despachada))
    OR (d.estado='PENDIENTE' AND COUNT(m.id)<>0);
