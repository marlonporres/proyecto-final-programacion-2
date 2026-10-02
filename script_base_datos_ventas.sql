-- Instalación limpia del Sistema de Ventas. No elimina una base existente.
-- Para recrear datos de prueba, ejecute primero docs/recrear_base_pruebas.sql.
IF DB_ID(N'SistemaVentas') IS NULL CREATE DATABASE SistemaVentas;
GO
USE SistemaVentas;
GO
IF OBJECT_ID('dbo.OrdenVenta', 'U') IS NOT NULL
    THROW 50001, 'La base ya contiene tablas. Use una instalación limpia; este script no migra datos.', 1;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

CREATE TABLE dbo.usuario (
    id_usuario BIGINT IDENTITY PRIMARY KEY, nombre NVARCHAR(150) NOT NULL,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE, rol VARCHAR(50) NOT NULL,
    activo BIT NOT NULL DEFAULT 1
);
CREATE TABLE dbo.cliente (
    id_cliente BIGINT IDENTITY PRIMARY KEY, nit VARCHAR(20) NOT NULL UNIQUE,
    nombre NVARCHAR(150) NOT NULL, direccion NVARCHAR(255), telefono VARCHAR(20), correo VARCHAR(100)
);
CREATE TABLE dbo.categoria (
    id_categoria BIGINT IDENTITY PRIMARY KEY, nombre NVARCHAR(100) NOT NULL UNIQUE,
    descripcion NVARCHAR(255), activa BIT NOT NULL DEFAULT 1
);
CREATE TABLE dbo.producto (
    id_producto BIGINT IDENTITY PRIMARY KEY, codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre NVARCHAR(150) NOT NULL, descripcion NVARCHAR(255),
    precio_venta DECIMAL(18,2) NOT NULL CHECK (precio_venta >= 0),
    activo BIT NOT NULL DEFAULT 1, categoria_id BIGINT NOT NULL REFERENCES dbo.categoria(id_categoria)
);
CREATE TABLE dbo.Bodega (
    id INT IDENTITY PRIMARY KEY, nombre NVARCHAR(100) NOT NULL UNIQUE,
    ubicacion NVARCHAR(200), activa BIT NOT NULL DEFAULT 1
);
CREATE TABLE dbo.ExistenciaInventario (
    id INT IDENTITY PRIMARY KEY,
    id_producto BIGINT NOT NULL REFERENCES dbo.producto(id_producto),
    id_bodega INT NOT NULL REFERENCES dbo.Bodega(id),
    existencia_actual INT NOT NULL DEFAULT 0 CHECK (existencia_actual >= 0),
    existencia_reservada INT NOT NULL DEFAULT 0 CHECK (existencia_reservada >= 0),
    CONSTRAINT UQ_existencia UNIQUE (id_producto, id_bodega),
    CONSTRAINT CK_reserva CHECK (existencia_reservada <= existencia_actual)
);
CREATE TABLE dbo.OrdenVenta (
    id INT IDENTITY PRIMARY KEY, numero_orden VARCHAR(50) NOT NULL UNIQUE, fecha DATETIME2 NOT NULL,
    id_cliente BIGINT NOT NULL REFERENCES dbo.cliente(id_cliente),
    id_usuario BIGINT NOT NULL REFERENCES dbo.usuario(id_usuario),
    total DECIMAL(18,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'COMPLETADA', 'CANCELADA')),
    observaciones NVARCHAR(500)
);
CREATE TABLE dbo.DetalleOrdenVenta (
    id INT IDENTITY PRIMARY KEY, id_orden INT NOT NULL REFERENCES dbo.OrdenVenta(id),
    id_producto BIGINT NOT NULL REFERENCES dbo.producto(id_producto),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(18,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(18,2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT UQ_detalle_orden UNIQUE (id_orden, id_producto)
);
CREATE TABLE dbo.Despacho (
    id INT IDENTITY PRIMARY KEY, numero_despacho VARCHAR(50) NOT NULL UNIQUE,
    id_orden INT NOT NULL UNIQUE REFERENCES dbo.OrdenVenta(id),
    id_bodega INT NOT NULL REFERENCES dbo.Bodega(id), fecha_despacho DATETIME2 NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'ANULADO'))
);
CREATE TABLE dbo.DetalleDespacho (
    id INT IDENTITY PRIMARY KEY, id_despacho INT NOT NULL REFERENCES dbo.Despacho(id),
    id_producto BIGINT NOT NULL REFERENCES dbo.producto(id_producto),
    cantidad_solicitada INT NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_despachada INT NOT NULL,
    CONSTRAINT CK_despacho_completo CHECK (cantidad_despachada = 0 OR cantidad_despachada = cantidad_solicitada),
    CONSTRAINT UQ_detalle_despacho UNIQUE (id_despacho, id_producto)
);
CREATE TABLE dbo.MovimientoInventario (
    id INT IDENTITY PRIMARY KEY, id_producto BIGINT NOT NULL REFERENCES dbo.producto(id_producto),
    id_bodega INT NOT NULL REFERENCES dbo.Bodega(id),
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'RESERVA', 'LIBERACION')),
    cantidad INT NOT NULL CHECK (cantidad > 0), fecha DATETIME2 NOT NULL, referencia NVARCHAR(100) NOT NULL
);
CREATE TABLE dbo.factura (
    id_factura BIGINT IDENTITY PRIMARY KEY, numero VARCHAR(50) NOT NULL UNIQUE,
    fecha_hora DATETIME2 NOT NULL, estado VARCHAR(20) NOT NULL CHECK (estado IN ('EMITIDA', 'PAGADA', 'ANULADA')),
    observaciones NVARCHAR(500), cliente_id BIGINT NOT NULL REFERENCES dbo.cliente(id_cliente),
    usuario_id BIGINT NOT NULL REFERENCES dbo.usuario(id_usuario),
    id_orden INT NOT NULL UNIQUE REFERENCES dbo.OrdenVenta(id),
    total DECIMAL(18,2) NOT NULL CHECK (total >= 0)
);
CREATE TABLE dbo.detalle_factura (
    id_detalle BIGINT IDENTITY PRIMARY KEY, factura_id BIGINT NOT NULL REFERENCES dbo.factura(id_factura),
    producto_id BIGINT NOT NULL REFERENCES dbo.producto(id_producto),
    cantidad DECIMAL(12,2) NOT NULL CHECK (cantidad > 0 AND cantidad = FLOOR(cantidad)),
    precio_unitario DECIMAL(18,2) NOT NULL CHECK (precio_unitario >= 0),
    porcentaje_impuesto DECIMAL(5,2) NOT NULL DEFAULT 12 CHECK (porcentaje_impuesto = 12),
    descuento DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (descuento = 0),
    subtotal DECIMAL(18,2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT UQ_detalle_factura UNIQUE (factura_id, producto_id)
);
CREATE TABLE dbo.pago (
    id_pago BIGINT IDENTITY PRIMARY KEY, factura_id BIGINT NOT NULL REFERENCES dbo.factura(id_factura),
    fecha_hora DATETIME2 NOT NULL, monto DECIMAL(18,2) NOT NULL CHECK (monto > 0),
    metodo VARCHAR(30) NOT NULL CHECK (metodo IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    referencia NVARCHAR(100)
);
CREATE INDEX IX_orden_fecha_estado ON dbo.OrdenVenta(fecha, estado);
CREATE INDEX IX_orden_cliente ON dbo.OrdenVenta(id_cliente);
CREATE INDEX IX_orden_usuario ON dbo.OrdenVenta(id_usuario);
CREATE INDEX IX_existencia_bodega ON dbo.ExistenciaInventario(id_bodega);
CREATE INDEX IX_despacho_bodega ON dbo.Despacho(id_bodega);
CREATE INDEX IX_movimiento_producto_bodega ON dbo.MovimientoInventario(id_producto, id_bodega, referencia);
CREATE INDEX IX_pago_factura ON dbo.pago(factura_id);
CREATE INDEX IX_producto_categoria ON dbo.producto(categoria_id);

INSERT dbo.usuario (nombre, nombre_usuario, rol) VALUES (N'Administrador', 'admin', 'ADMINISTRADOR');
INSERT dbo.cliente (nit, nombre, direccion) VALUES ('CF', N'Consumidor Final', N'Guatemala');
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
PRINT 'SistemaVentas instalado con 24 productos de computación y existencias de demostración.';
GO
