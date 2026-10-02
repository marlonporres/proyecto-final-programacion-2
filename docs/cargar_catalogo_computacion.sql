-- Carga aditiva e idempotente. No borra ventas ni reinicia existencias.
-- Ejecutar sobre el esquema actualizado de SistemaVentas.
USE SistemaVentas;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
-- INICIO CATALOGO COMPUTACION
DECLARE @bloqueo INT;
EXEC @bloqueo = sys.sp_getapplock @Resource=N'CatalogoComputacionDemo',
    @LockMode='Exclusive', @LockOwner='Transaction', @LockTimeout=10000;
IF @bloqueo < 0 THROW 50002, 'No se pudo bloquear la carga del catálogo.', 1;

DECLARE @categorias TABLE (nombre NVARCHAR(100), descripcion NVARCHAR(255));
INSERT @categorias VALUES
(N'Equipos de cómputo', N'Computadoras portátiles y de escritorio de demostración'),
(N'Periféricos', N'Mouse, teclados y conjuntos de entrada'),
(N'Monitores', N'Pantallas y soportes para estaciones de trabajo'),
(N'Componentes', N'Memoria RAM y almacenamiento interno'),
(N'Redes', N'Conectividad de red para computadoras'),
(N'Accesorios', N'Audio, video y accesorios de escritorio');
INSERT dbo.categoria (nombre, descripcion)
SELECT s.nombre, s.descripcion FROM @categorias s
WHERE NOT EXISTS (SELECT 1 FROM dbo.categoria c WHERE c.nombre=s.nombre);
IF EXISTS (SELECT 1 FROM dbo.categoria c JOIN @categorias s ON s.nombre=c.nombre WHERE c.activa=0)
    THROW 50003, 'Una categoría del catálogo está inactiva. Revísela antes de cargar productos.', 1;

IF NOT EXISTS (SELECT 1 FROM dbo.Bodega WHERE nombre=N'Bodega Central')
    INSERT dbo.Bodega (nombre, ubicacion) VALUES (N'Bodega Central', N'Guatemala');
DECLARE @id_bodega INT = (SELECT id FROM dbo.Bodega WHERE nombre=N'Bodega Central' AND activa=1);
IF @id_bodega IS NULL THROW 50004, 'Bodega Central debe estar activa.', 1;

DECLARE @catalogo TABLE (
    codigo VARCHAR(50), nombre NVARCHAR(150), descripcion NVARCHAR(255),
    precio DECIMAL(18,2), categoria NVARCHAR(100), unidades INT
);
-- Marcas genéricas, especificaciones y precios ficticios; valores antes de IVA.
INSERT @catalogo VALUES
('TEC-001', N'Mouse inalámbrico', N'Mouse óptico de 1600 DPI con receptor USB', 95.00, N'Periféricos', 20),
('PER-002', N'Teclado USB de oficina', N'Teclado de membrana en español con teclado numérico', 120.00, N'Periféricos', 25),
('PER-003', N'Teclado mecánico compacto', N'Teclado mecánico de 87 teclas con iluminación', 285.00, N'Periféricos', 12),
('PER-004', N'Combo teclado y mouse inalámbricos', N'Conjunto de teclado y mouse con receptor USB compartido', 185.00, N'Periféricos', 18),
('EQP-001', N'Laptop Oficina 15', N'Portátil de 15.6 pulgadas, 8 GB RAM y SSD de 256 GB', 3495.00, N'Equipos de cómputo', 8),
('EQP-002', N'Laptop Estudio 14', N'Portátil de 14 pulgadas, 16 GB RAM y SSD de 512 GB', 4895.00, N'Equipos de cómputo', 6),
('EQP-003', N'PC Escritorio Pro', N'Torre de escritorio, 16 GB RAM y SSD de 1 TB; sin monitor', 4295.00, N'Equipos de cómputo', 7),
('EQP-004', N'Mini PC Compacta', N'Computadora compacta, 8 GB RAM y SSD de 512 GB; sin monitor', 2495.00, N'Equipos de cómputo', 10),
('MON-001', N'Monitor 24 Full HD', N'Pantalla IPS de 24 pulgadas, 1920x1080 y entrada HDMI', 1099.00, N'Monitores', 12),
('MON-002', N'Monitor 27 Full HD', N'Pantalla IPS de 27 pulgadas, 1920x1080 y entrada HDMI', 1449.00, N'Monitores', 8),
('MON-003', N'Monitor 27 QHD', N'Pantalla de 27 pulgadas, 2560x1440 y DisplayPort', 2399.00, N'Monitores', 6),
('MON-004', N'Soporte articulado para monitor', N'Brazo de escritorio VESA para un monitor de hasta 27 pulgadas', 220.00, N'Monitores', 15),
('CMP-001', N'Memoria RAM DDR4 8 GB', N'Módulo DIMM DDR4 de 8 GB y 3200 MHz para escritorio', 185.00, N'Componentes', 24),
('CMP-002', N'Memoria RAM DDR4 16 GB', N'Módulo DIMM DDR4 de 16 GB y 3200 MHz para escritorio', 335.00, N'Componentes', 18),
('CMP-003', N'SSD SATA 500 GB', N'Unidad de estado sólido SATA de 2.5 pulgadas y 500 GB', 395.00, N'Componentes', 20),
('CMP-004', N'SSD NVMe 1 TB', N'Unidad de estado sólido M.2 NVMe de 1 TB', 695.00, N'Componentes', 14),
('RED-001', N'Router Wi-Fi doble banda', N'Router de 2.4 y 5 GHz con cuatro puertos LAN', 399.00, N'Redes', 10),
('RED-002', N'Adaptador Wi-Fi USB', N'Adaptador inalámbrico USB de doble banda', 159.00, N'Redes', 22),
('RED-003', N'Switch Gigabit 8 puertos', N'Switch de escritorio de ocho puertos Ethernet Gigabit', 299.00, N'Redes', 10),
('RED-004', N'Cable de red Cat 6 de 3 m', N'Cable Ethernet Cat 6 con conectores RJ45, longitud de 3 metros', 45.00, N'Redes', 40),
('ACC-001', N'Audífonos USB con micrófono', N'Audífonos de diadema con micrófono para videollamadas', 225.00, N'Accesorios', 20),
('ACC-002', N'Cámara web Full HD', N'Cámara USB de 1080p con micrófono integrado', 295.00, N'Accesorios', 15),
('ACC-003', N'Hub USB de 4 puertos', N'Concentrador USB 3.0 de cuatro puertos', 145.00, N'Accesorios', 25),
('ACC-004', N'Base ajustable para laptop', N'Soporte plegable para portátiles de 13 a 15.6 pulgadas', 175.00, N'Accesorios', 20);

DECLARE @productos_nuevos TABLE (id_producto BIGINT PRIMARY KEY);
INSERT dbo.producto (codigo, nombre, descripcion, precio_venta, categoria_id)
OUTPUT inserted.id_producto INTO @productos_nuevos
SELECT s.codigo, s.nombre, s.descripcion, s.precio, c.id_categoria
FROM @catalogo s JOIN dbo.categoria c ON c.nombre=s.categoria
WHERE NOT EXISTS (SELECT 1 FROM dbo.producto p WHERE p.codigo=s.codigo);

-- Reubica únicamente el mouse de la semilla anterior, sin cambiar precio ni stock.
UPDATE p SET categoria_id=c.id_categoria
FROM dbo.producto p JOIN dbo.categoria anterior ON anterior.id_categoria=p.categoria_id
CROSS JOIN dbo.categoria c
WHERE p.codigo='TEC-001' AND p.nombre=N'Mouse inalámbrico'
    AND anterior.nombre=N'General' AND c.nombre=N'Periféricos';
-- Conserva el martillo y sus relaciones históricas, pero no admite nuevas ventas.
UPDATE p SET activo=0
FROM dbo.producto p JOIN dbo.categoria c ON c.id_categoria=p.categoria_id
WHERE p.codigo='FER-001' AND p.nombre=N'Martillo' AND c.nombre=N'General';
UPDATE c SET activa=0 FROM dbo.categoria c
WHERE c.nombre=N'General' AND c.descripcion=N'Productos de demostración'
    AND NOT EXISTS (SELECT 1 FROM dbo.producto p WHERE p.categoria_id=c.id_categoria AND p.activo=1);

-- Existencia y movimiento inicial únicamente para productos recién insertados.
-- Repetir la carga no repone unidades vendidas ni sobrescribe cambios de catálogo.
INSERT dbo.ExistenciaInventario (id_producto, id_bodega, existencia_actual)
SELECT p.id_producto, @id_bodega, s.unidades
FROM @productos_nuevos n JOIN dbo.producto p ON p.id_producto=n.id_producto
JOIN @catalogo s ON s.codigo=p.codigo;
INSERT dbo.MovimientoInventario (id_producto, id_bodega, tipo_movimiento, cantidad, fecha, referencia)
SELECT p.id_producto, @id_bodega, 'ENTRADA', s.unidades, SYSDATETIME(), N'Carga inicial catálogo computación'
FROM @productos_nuevos n JOIN dbo.producto p ON p.id_producto=n.id_producto
JOIN @catalogo s ON s.codigo=p.codigo;
-- FIN CATALOGO COMPUTACION
    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
