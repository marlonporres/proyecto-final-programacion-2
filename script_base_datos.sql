-- =============================================================
-- SCRIPT DE BASE DE DATOS: SistemaFacturacion
-- Base de datos para Microsoft SQL Server
-- Programación II - UMG
-- =============================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'FacturacionDB')
BEGIN
    CREATE DATABASE FacturacionDB;
END
GO

USE FacturacionDB;
GO

-- 1. Eliminar tablas previas si existen (respetando orden de dependencias)
IF OBJECT_ID('dbo.pago', 'U') IS NOT NULL DROP TABLE dbo.pago;
IF OBJECT_ID('dbo.detalle_factura', 'U') IS NOT NULL DROP TABLE dbo.detalle_factura;
IF OBJECT_ID('dbo.factura', 'U') IS NOT NULL DROP TABLE dbo.factura;
IF OBJECT_ID('dbo.producto', 'U') IS NOT NULL DROP TABLE dbo.producto;
IF OBJECT_ID('dbo.inventario', 'U') IS NOT NULL DROP TABLE dbo.inventario;
IF OBJECT_ID('dbo.categoria', 'U') IS NOT NULL DROP TABLE dbo.categoria;
IF OBJECT_ID('dbo.cliente', 'U') IS NOT NULL DROP TABLE dbo.cliente;
IF OBJECT_ID('dbo.usuario', 'U') IS NOT NULL DROP TABLE dbo.usuario;
GO

-- 2. Tabla CATEGORIA
CREATE TABLE dbo.categoria (
    id_categoria BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    activa BIT NOT NULL DEFAULT 1
);
GO

-- 3. Tabla INVENTARIO
CREATE TABLE dbo.inventario (
    id_inventario BIGINT IDENTITY(1,1) PRIMARY KEY,
    existencia DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    stock_minimo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    actualizado_en DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT CK_inventario_existencia CHECK (existencia >= 0),
    CONSTRAINT CK_inventario_stock_minimo CHECK (stock_minimo >= 0)
);
GO

-- 4. Tabla PRODUCTO
CREATE TABLE dbo.producto (
    id_producto BIGINT IDENTITY(1,1) PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255) NULL,
    precio_venta DECIMAL(12,2) NOT NULL,
    activo BIT NOT NULL DEFAULT 1,
    categoria_id BIGINT NOT NULL,
    inventario_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT FK_producto_categoria FOREIGN KEY (categoria_id) REFERENCES dbo.categoria(id_categoria),
    CONSTRAINT FK_producto_inventario FOREIGN KEY (inventario_id) REFERENCES dbo.inventario(id_inventario),
    CONSTRAINT CK_producto_precio CHECK (precio_venta >= 0)
);
GO

-- 5. Tabla CLIENTE
CREATE TABLE dbo.cliente (
    id_cliente BIGINT IDENTITY(1,1) PRIMARY KEY,
    nit VARCHAR(20) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(255) NULL,
    telefono VARCHAR(20) NULL,
    correo VARCHAR(100) NULL
);
GO

-- 6. Tabla USUARIO
CREATE TABLE dbo.usuario (
    id_usuario BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    rol VARCHAR(50) NOT NULL,
    activo BIT NOT NULL DEFAULT 1
);
GO

-- 7. Tabla FACTURA
CREATE TABLE dbo.factura (
    id_factura BIGINT IDENTITY(1,1) PRIMARY KEY,
    numero VARCHAR(50) NOT NULL UNIQUE,
    fecha_hora DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    observaciones VARCHAR(500) NULL,
    cliente_id BIGINT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT FK_factura_cliente FOREIGN KEY (cliente_id) REFERENCES dbo.cliente(id_cliente),
    CONSTRAINT FK_factura_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.usuario(id_usuario),
    CONSTRAINT CK_factura_estado CHECK (estado IN ('BORRADOR', 'EMITIDA', 'PAGADA', 'ANULADA'))
);
GO

-- 8. Tabla DETALLE_FACTURA
CREATE TABLE dbo.detalle_factura (
    id_detalle BIGINT IDENTITY(1,1) PRIMARY KEY,
    factura_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad DECIMAL(12,2) NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    porcentaje_impuesto DECIMAL(5,2) NOT NULL DEFAULT 12.00,
    descuento DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT FK_detalle_factura FOREIGN KEY (factura_id) REFERENCES dbo.factura(id_factura),
    CONSTRAINT FK_detalle_producto FOREIGN KEY (producto_id) REFERENCES dbo.producto(id_producto),
    CONSTRAINT CK_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT CK_detalle_precio CHECK (precio_unitario >= 0)
);
GO

-- 9. Tabla PAGO
CREATE TABLE dbo.pago (
    id_pago BIGINT IDENTITY(1,1) PRIMARY KEY,
    factura_id BIGINT NOT NULL,
    fecha_hora DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    monto DECIMAL(12,2) NOT NULL,
    metodo VARCHAR(30) NOT NULL,
    referencia VARCHAR(100) NULL,
    CONSTRAINT FK_pago_factura FOREIGN KEY (factura_id) REFERENCES dbo.factura(id_factura),
    CONSTRAINT CK_pago_metodo CHECK (metodo IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    CONSTRAINT CK_pago_monto CHECK (monto > 0)
);
GO

-- =============================================================
-- DATOS SEMILLA / INICIALES DE PRUEBA
-- =============================================================

-- Categorías
INSERT INTO dbo.categoria (nombre, descripcion, activa) VALUES
('Ferretería', 'Herramientas manuales, eléctricas y consumibles', 1),
('Tecnología', 'Equipos de cómputo y periféricos', 1),
('Papelería', 'Artículos escolares y de oficina', 1);

-- Inventarios iniciales
INSERT INTO dbo.inventario (existencia, stock_minimo) VALUES
(50.00, 5.00),   -- 1: Martillo
(20.00, 2.00),   -- 2: Taladro
(100.00, 10.00), -- 3: Mouse
(30.00, 5.00),   -- 4: Teclado
(80.00, 15.00);  -- 5: Papel Bond

-- Productos enlazados
INSERT INTO dbo.producto (codigo, nombre, descripcion, precio_venta, activo, categoria_id, inventario_id) VALUES
('FER-001', 'Martillo Industrial 16oz', 'Martillo con mango ergonómico de fibra', 75.50, 1, 1, 1),
('FER-002', 'Taladro Percutor 650W', 'Taladro percutor velocidad variable 1/2 pulgada', 450.00, 1, 1, 2),
('TEC-001', 'Mouse Óptico Inalámbrico', 'Mouse 2.4GHz 1600 DPI receptor USB', 95.00, 1, 2, 3),
('TEC-002', 'Teclado Mecánico RGB', 'Teclado mecánico switches azules retroiluminado', 320.00, 1, 2, 4),
('PAP-001', 'Resma Papel Bond Carta 75g', 'Resma de 500 hojas blancas tamaño carta', 38.00, 1, 3, 5);

-- Clientes
INSERT INTO dbo.cliente (nit, nombre, direccion, telefono, correo) VALUES
('CF', 'Consumidor Final', 'Ciudad de Guatemala', '00000000', 'cf@sistema.gt'),
('1234567-8', 'Distribuidora El Éxito, S.A.', '12 Calle 5-43 Zona 10', '22334455', 'contacto@elexito.gt'),
('9876543-2', 'Tecnología Global de Centroamérica', 'Diagonal 6 10-01 Zona 10', '24556677', 'ventas@tecglobal.gt');

-- Usuarios
INSERT INTO dbo.usuario (nombre, nombreUsuario, rol, activo) VALUES
('Administrador del Sistema', 'admin', 'ADMINISTRADOR', 1),
('Marlon Porres', 'mporres', 'SUPERVISOR', 1),
('Cajero Turno Matutino', 'cajero1', 'CAJERO', 1);

PRINT 'Base de datos FacturacionDB creada y poblada exitosamente.';
GO
