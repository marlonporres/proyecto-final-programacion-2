-- ============================================================================
-- SCRIPT DE BASE DE DATOS: SistemaVentas
-- Microsoft SQL Server - Programación II (UMG)
-- Alineación completa con implementación MVC + DAO (Ventas e Inventario)
-- ============================================================================

-- 1. CREACIÓN SEGURA DE LA BASE DE DATOS
IF DB_ID('SistemaVentas') IS NULL
BEGIN
    CREATE DATABASE SistemaVentas;
END
GO

USE SistemaVentas;
GO

-- ============================================================================
-- 2. TABLAS BASE Y CATÁLOGOS
-- ============================================================================

-- 2.1 TABLA USUARIO
-- Soporta tanto UsuarioDAOImpl (id_usuario, nombre_usuario) como OrdenVenta/Factura (id, username)
IF OBJECT_ID('dbo.usuario', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.usuario (
        id_usuario BIGINT IDENTITY(1,1) PRIMARY KEY,
        nombre VARCHAR(150) NOT NULL,
        nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(255) NULL,
        rol VARCHAR(50) NOT NULL,
        activo BIT NOT NULL DEFAULT 1,
        id AS id_usuario,
        username AS nombre_usuario
    );
END
GO

-- 2.2 TABLA CLIENTE
-- Soporta ClienteDAOImpl (id_cliente, correo), FacturaDAOImpl y OrdenVentaDAOImpl (id)
IF OBJECT_ID('dbo.cliente', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.cliente (
        id_cliente BIGINT IDENTITY(1,1) PRIMARY KEY,
        nit VARCHAR(20) NOT NULL UNIQUE,
        nombre VARCHAR(150) NOT NULL,
        direccion VARCHAR(255) NULL,
        telefono VARCHAR(20) NULL,
        correo VARCHAR(100) NULL,
        id AS id_cliente
    );
END
GO

-- 2.3 TABLA CATEGORIA
-- Soporta CategoriaDAOImpl (id_categoria, activa) y referencias por id
IF OBJECT_ID('dbo.categoria', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.categoria (
        id_categoria BIGINT IDENTITY(1,1) PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL,
        descripcion VARCHAR(255) NULL,
        activa BIT NOT NULL DEFAULT 1,
        id AS id_categoria
    );
END
GO

-- 2.4 TABLA INVENTARIO (Stock legacy requerido por ProductoDAOImpl)
IF OBJECT_ID('dbo.inventario', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.inventario (
        id_inventario BIGINT IDENTITY(1,1) PRIMARY KEY,
        existencia DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (existencia >= 0),
        stock_minimo DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (stock_minimo >= 0),
        actualizado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        id AS id_inventario
    );
END
GO

-- 2.5 TABLA PRODUCTO
-- Soporta ProductoDAOImpl (id_producto, precio_venta, categoria_id, inventario_id)
-- y referencias del módulo nuevo (id, precio, id_categoria)
IF OBJECT_ID('dbo.producto', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.producto (
        id_producto BIGINT IDENTITY(1,1) PRIMARY KEY,
        codigo VARCHAR(50) NOT NULL UNIQUE,
        nombre VARCHAR(150) NOT NULL,
        descripcion VARCHAR(255) NULL,
        precio_venta DECIMAL(12,2) NOT NULL CHECK (precio_venta >= 0),
        activo BIT NOT NULL DEFAULT 1,
        categoria_id BIGINT NOT NULL,
        inventario_id BIGINT NULL,
        id AS id_producto,
        precio AS precio_venta,
        id_categoria AS categoria_id,
        CONSTRAINT FK_producto_categoria FOREIGN KEY (categoria_id) REFERENCES dbo.categoria(id_categoria),
        CONSTRAINT FK_producto_inventario FOREIGN KEY (inventario_id) REFERENCES dbo.inventario(id_inventario)
    );
END
GO

-- 2.6 TABLA BODEGA
IF OBJECT_ID('dbo.Bodega', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Bodega (
        id INT IDENTITY(1,1) PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL,
        ubicacion VARCHAR(200) NULL,
        activa BIT NOT NULL DEFAULT 1,
        id_bodega AS id
    );
END
GO

-- ============================================================================
-- 3. MÓDULO DE INVENTARIO Y STOCK MULTI-BODEGA
-- ============================================================================

-- 3.1 TABLA EXISTENCIA_INVENTARIO
IF OBJECT_ID('dbo.ExistenciaInventario', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.ExistenciaInventario (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_producto BIGINT NOT NULL,
        id_bodega INT NOT NULL,
        existencia_actual INT NOT NULL DEFAULT 0 CHECK (existencia_actual >= 0),
        existencia_reservada INT NOT NULL DEFAULT 0 CHECK (existencia_reservada >= 0),
        CONSTRAINT UQ_existencia_producto_bodega UNIQUE (id_producto, id_bodega),
        CONSTRAINT FK_existencia_producto FOREIGN KEY (id_producto) REFERENCES dbo.producto(id_producto),
        CONSTRAINT FK_existencia_bodega FOREIGN KEY (id_bodega) REFERENCES dbo.Bodega(id)
    );
END
GO

-- 3.2 TABLA MOVIMIENTO_INVENTARIO
IF OBJECT_ID('dbo.MovimientoInventario', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.MovimientoInventario (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_producto BIGINT NOT NULL,
        id_bodega INT NOT NULL,
        tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'RESERVA', 'LIBERACION')),
        cantidad INT NOT NULL CHECK (cantidad > 0),
        fecha DATETIME2 NOT NULL,
        referencia VARCHAR(100) NULL,
        CONSTRAINT FK_movimiento_producto FOREIGN KEY (id_producto) REFERENCES dbo.producto(id_producto),
        CONSTRAINT FK_movimiento_bodega FOREIGN KEY (id_bodega) REFERENCES dbo.Bodega(id)
    );
END
GO

-- ============================================================================
-- 4. MÓDULO DE ORDEN DE VENTA Y DESPACHO
-- ============================================================================

-- 4.1 TABLA ORDEN_VENTA
IF OBJECT_ID('dbo.OrdenVenta', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrdenVenta (
        id INT IDENTITY(1,1) PRIMARY KEY,
        numero_orden VARCHAR(50) NOT NULL UNIQUE,
        fecha DATETIME2 NOT NULL,
        id_cliente BIGINT NOT NULL,
        id_usuario BIGINT NOT NULL,
        total DECIMAL(18,2) NOT NULL DEFAULT 0.00 CHECK (total >= 0),
        estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'COMPLETADA', 'CANCELADA')),
        observaciones VARCHAR(500) NULL,
        id_orden AS id,
        cliente_id AS id_cliente,
        usuario_id AS id_usuario,
        CONSTRAINT FK_orden_cliente FOREIGN KEY (id_cliente) REFERENCES dbo.cliente(id_cliente),
        CONSTRAINT FK_orden_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuario(id_usuario)
    );
END
GO

-- 4.2 TABLA DETALLE_ORDEN_VENTA
IF OBJECT_ID('dbo.DetalleOrdenVenta', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.DetalleOrdenVenta (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_orden INT NOT NULL,
        id_producto BIGINT NOT NULL,
        cantidad INT NOT NULL CHECK (cantidad > 0),
        precio_unitario DECIMAL(18,2) NOT NULL CHECK (precio_unitario >= 0),
        subtotal DECIMAL(18,2) NOT NULL CHECK (subtotal >= 0),
        CONSTRAINT FK_detalle_orden FOREIGN KEY (id_orden) REFERENCES dbo.OrdenVenta(id),
        CONSTRAINT FK_detalle_orden_producto FOREIGN KEY (id_producto) REFERENCES dbo.producto(id_producto)
    );
END
GO

-- 4.3 TABLA DESPACHO
IF OBJECT_ID('dbo.Despacho', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Despacho (
        id INT IDENTITY(1,1) PRIMARY KEY,
        numero_despacho VARCHAR(50) NOT NULL UNIQUE,
        id_orden INT NOT NULL,
        id_bodega INT NOT NULL,
        fecha_despacho DATETIME2 NOT NULL,
        estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'ANULADO')),
        id_despacho AS id,
        CONSTRAINT FK_despacho_orden FOREIGN KEY (id_orden) REFERENCES dbo.OrdenVenta(id),
        CONSTRAINT FK_despacho_bodega FOREIGN KEY (id_bodega) REFERENCES dbo.Bodega(id)
    );
END
GO

-- 4.4 TABLA DETALLE_DESPACHO
IF OBJECT_ID('dbo.DetalleDespacho', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.DetalleDespacho (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_despacho INT NOT NULL,
        id_producto BIGINT NOT NULL,
        cantidad_solicitada INT NOT NULL CHECK (cantidad_solicitada > 0),
        cantidad_despachada INT NOT NULL CHECK (cantidad_despachada >= 0),
        CONSTRAINT FK_detalle_despacho FOREIGN KEY (id_despacho) REFERENCES dbo.Despacho(id),
        CONSTRAINT FK_detalle_despacho_producto FOREIGN KEY (id_producto) REFERENCES dbo.producto(id_producto),
        CONSTRAINT CK_detalle_despacho_cantidades CHECK (cantidad_despachada <= cantidad_solicitada)
    );
END
GO

-- ============================================================================
-- 5. MÓDULO DE FACTURACIÓN Y PAGO
-- ============================================================================

-- 5.1 TABLA FACTURA
IF OBJECT_ID('dbo.factura', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.factura (
        id_factura BIGINT IDENTITY(1,1) PRIMARY KEY,
        numero VARCHAR(50) NOT NULL UNIQUE,
        fecha_hora DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR' CHECK (estado IN ('BORRADOR', 'EMITIDA', 'PAGADA', 'ANULADA')),
        observaciones VARCHAR(500) NULL,
        cliente_id BIGINT NULL,
        usuario_id BIGINT NOT NULL,
        id_orden INT NULL,
        total DECIMAL(18,2) NOT NULL DEFAULT 0.00 CHECK (total >= 0),
        id AS id_factura,
        numero_factura AS numero,
        fecha AS fecha_hora,
        id_cliente AS cliente_id,
        id_usuario AS usuario_id,
        CONSTRAINT FK_factura_cliente FOREIGN KEY (cliente_id) REFERENCES dbo.cliente(id_cliente),
        CONSTRAINT FK_factura_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuario(id_usuario),
        CONSTRAINT FK_factura_orden FOREIGN KEY (id_orden) REFERENCES dbo.OrdenVenta(id)
    );
END
GO

-- 5.2 TABLA DETALLE_FACTURA
IF OBJECT_ID('dbo.detalle_factura', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.detalle_factura (
        id_detalle BIGINT IDENTITY(1,1) PRIMARY KEY,
        factura_id BIGINT NOT NULL,
        producto_id BIGINT NOT NULL,
        cantidad DECIMAL(12,2) NOT NULL CHECK (cantidad > 0),
        precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
        porcentaje_impuesto DECIMAL(5,2) NOT NULL DEFAULT 12.00 CHECK (porcentaje_impuesto >= 0),
        descuento DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (descuento >= 0),
        subtotal DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
        id AS id_detalle,
        id_factura AS factura_id,
        id_producto AS producto_id,
        CONSTRAINT FK_detalle_factura FOREIGN KEY (factura_id) REFERENCES dbo.factura(id_factura),
        CONSTRAINT FK_detalle_producto FOREIGN KEY (producto_id) REFERENCES dbo.producto(id_producto)
    );
END
GO

-- 5.3 TABLA METODOPAGO (Catálogo UML)
IF OBJECT_ID('dbo.MetodoPago', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.MetodoPago (
        id INT IDENTITY(1,1) PRIMARY KEY,
        codigo VARCHAR(30) NOT NULL UNIQUE,
        nombre VARCHAR(50) NOT NULL
    );
END
GO

-- 5.4 TABLA PAGO
IF OBJECT_ID('dbo.pago', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.pago (
        id_pago BIGINT IDENTITY(1,1) PRIMARY KEY,
        factura_id BIGINT NOT NULL,
        fecha_hora DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        monto DECIMAL(12,2) NOT NULL CHECK (monto >= 0),
        metodo VARCHAR(30) NOT NULL CHECK (metodo IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
        referencia VARCHAR(100) NULL,
        id_metodo_pago INT NULL,
        id AS id_pago,
        id_factura AS factura_id,
        fecha AS fecha_hora,
        CONSTRAINT FK_pago_factura FOREIGN KEY (factura_id) REFERENCES dbo.factura(id_factura),
        CONSTRAINT FK_pago_metodo FOREIGN KEY (id_metodo_pago) REFERENCES dbo.MetodoPago(id)
    );
END
GO

-- ============================================================================
-- 6. DATOS SEMILLA MÍNIMOS (IDEMPOTENTES)
-- ============================================================================

-- 6.1 Catálogo Métodos de Pago
IF NOT EXISTS (SELECT 1 FROM dbo.MetodoPago WHERE codigo = 'EFECTIVO')
    INSERT INTO dbo.MetodoPago (codigo, nombre) VALUES ('EFECTIVO', 'Efectivo');
IF NOT EXISTS (SELECT 1 FROM dbo.MetodoPago WHERE codigo = 'TARJETA')
    INSERT INTO dbo.MetodoPago (codigo, nombre) VALUES ('TARJETA', 'Tarjeta de Crédito/Débito');
IF NOT EXISTS (SELECT 1 FROM dbo.MetodoPago WHERE codigo = 'TRANSFERENCIA')
    INSERT INTO dbo.MetodoPago (codigo, nombre) VALUES ('TRANSFERENCIA', 'Transferencia Bancaria');

-- 6.2 Usuario Administrador
IF NOT EXISTS (SELECT 1 FROM dbo.usuario WHERE nombre_usuario = 'admin')
    INSERT INTO dbo.usuario (nombre, nombre_usuario, password, rol, activo)
    VALUES ('Administrador del Sistema', 'admin', 'admin123', 'ADMINISTRADOR', 1);

-- 6.3 Cliente Consumidor Final
IF NOT EXISTS (SELECT 1 FROM dbo.cliente WHERE nit = 'CF')
    INSERT INTO dbo.cliente (nit, nombre, direccion, telefono, correo)
    VALUES ('CF', 'Consumidor Final', 'Ciudad de Guatemala', '00000000', 'cf@sistema.gt');

IF NOT EXISTS (SELECT 1 FROM dbo.cliente WHERE nit = '1234567-8')
    INSERT INTO dbo.cliente (nit, nombre, direccion, telefono, correo)
    VALUES ('1234567-8', 'Distribuidora El Éxito, S.A.', '12 Calle 5-43 Zona 10', '22334455', 'contacto@elexito.gt');

-- 6.4 Categorías
IF NOT EXISTS (SELECT 1 FROM dbo.categoria WHERE nombre = 'Tecnología')
    INSERT INTO dbo.categoria (nombre, descripcion, activa)
    VALUES ('Tecnología', 'Equipos de cómputo, componentes y periféricos', 1);

IF NOT EXISTS (SELECT 1 FROM dbo.categoria WHERE nombre = 'Ferretería')
    INSERT INTO dbo.categoria (nombre, descripcion, activa)
    VALUES ('Ferretería', 'Herramientas y suministros de ferretería', 1);

-- 6.5 Inventario Legacy (para ProductoDAOImpl)
IF NOT EXISTS (SELECT 1 FROM dbo.inventario WHERE id_inventario = 1)
    INSERT INTO dbo.inventario (existencia, stock_minimo) VALUES (100.00, 10.00);
IF NOT EXISTS (SELECT 1 FROM dbo.inventario WHERE id_inventario = 2)
    INSERT INTO dbo.inventario (existencia, stock_minimo) VALUES (50.00, 5.00);

-- 6.6 Productos
IF NOT EXISTS (SELECT 1 FROM dbo.producto WHERE codigo = 'TEC-001')
    INSERT INTO dbo.producto (codigo, nombre, descripcion, precio_venta, activo, categoria_id, inventario_id)
    VALUES ('TEC-001', 'Mouse Óptico Inalámbrico', 'Mouse 2.4GHz 1600 DPI receptor USB', 95.00, 1, 1, 1);

IF NOT EXISTS (SELECT 1 FROM dbo.producto WHERE codigo = 'FER-001')
    INSERT INTO dbo.producto (codigo, nombre, descripcion, precio_venta, activo, categoria_id, inventario_id)
    VALUES ('FER-001', 'Martillo Industrial 16oz', 'Martillo con mango ergonómico de fibra', 75.50, 1, 2, 2);

-- 6.7 Bodega Central
IF NOT EXISTS (SELECT 1 FROM dbo.Bodega WHERE nombre = 'Bodega Central')
    INSERT INTO dbo.Bodega (nombre, ubicacion, activa)
    VALUES ('Bodega Central', 'Ciudad de Guatemala - Zona Central', 1);

-- 6.8 Existencias Iniciales Multi-Bodega
IF NOT EXISTS (SELECT 1 FROM dbo.ExistenciaInventario WHERE id_producto = 1 AND id_bodega = 1)
    INSERT INTO dbo.ExistenciaInventario (id_producto, id_bodega, existencia_actual, existencia_reservada)
    VALUES (1, 1, 100, 0);

IF NOT EXISTS (SELECT 1 FROM dbo.ExistenciaInventario WHERE id_producto = 2 AND id_bodega = 1)
    INSERT INTO dbo.ExistenciaInventario (id_producto, id_bodega, existencia_actual, existencia_reservada)
    VALUES (2, 1, 50, 0);

PRINT 'Base de datos SistemaVentas y tablas verificadas/inicializadas exitosamente.';
GO

-- ============================================================================
-- 7. NOTA DE ARQUITECTURA Y MIGRACIÓN OPCIONAL (INVENTARIO DEFINITIVO)
-- ============================================================================
-- En la arquitectura definitiva, el inventario se gestiona mediante:
--   - dbo.Bodega
--   - dbo.ExistenciaInventario
--   - dbo.MovimientoInventario
-- La tabla dbo.inventario y la columna dbo.producto.inventario_id son legadas y
-- el código Java (Producto.java, ProductoDAOImpl.java, FacturaDAOImpl.java) ya no las utiliza.
--
-- Si el administrador desea eliminar limpiamente la columna y tabla legadas, puede ejecutar:
--
-- ALTER TABLE dbo.producto DROP CONSTRAINT FK_producto_inventario;
-- ALTER TABLE dbo.producto DROP COLUMN inventario_id;
-- DROP TABLE dbo.inventario;
-- ============================================================================

