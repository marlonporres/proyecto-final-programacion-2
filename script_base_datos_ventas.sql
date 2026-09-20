CREATE DATABASE SistemaVentas;
GO
USE SistemaVentas;
GO

CREATE TABLE Usuario (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BIT DEFAULT 1
);

CREATE TABLE Cliente (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nit VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    telefono VARCHAR(20)
);

CREATE TABLE Categoria (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE Producto (
    id INT IDENTITY(1,1) PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    precio DECIMAL(18,2) NOT NULL CHECK (precio >= 0),
    id_categoria INT FOREIGN KEY REFERENCES Categoria(id)
);

CREATE TABLE Bodega (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(200),
    activa BIT DEFAULT 1
);

CREATE TABLE ExistenciaInventario (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_producto INT NOT NULL FOREIGN KEY REFERENCES Producto(id),
    id_bodega INT NOT NULL FOREIGN KEY REFERENCES Bodega(id),
    existencia_actual INT NOT NULL DEFAULT 0 CHECK (existencia_actual >= 0),
    existencia_reservada INT NOT NULL DEFAULT 0 CHECK (existencia_reservada >= 0),
    UNIQUE(id_producto, id_bodega)
);

CREATE TABLE OrdenVenta (
    id INT IDENTITY(1,1) PRIMARY KEY,
    numero_orden VARCHAR(20) NOT NULL UNIQUE,
    fecha DATETIME NOT NULL,
    id_cliente INT NOT NULL FOREIGN KEY REFERENCES Cliente(id),
    id_usuario INT NOT NULL FOREIGN KEY REFERENCES Usuario(id),
    total DECIMAL(18,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(255)
);

CREATE TABLE DetalleOrdenVenta (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_orden INT NOT NULL FOREIGN KEY REFERENCES OrdenVenta(id),
    id_producto INT NOT NULL FOREIGN KEY REFERENCES Producto(id),
    cantidad INT NOT NULL CHECK (cantidad >= 0),
    precio_unitario DECIMAL(18,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(18,2) NOT NULL CHECK (subtotal >= 0)
);

CREATE TABLE Despacho (
    id INT IDENTITY(1,1) PRIMARY KEY,
    numero_despacho VARCHAR(20) NOT NULL UNIQUE,
    id_orden INT NOT NULL FOREIGN KEY REFERENCES OrdenVenta(id),
    id_bodega INT NOT NULL FOREIGN KEY REFERENCES Bodega(id),
    fecha_despacho DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL
);

CREATE TABLE DetalleDespacho (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_despacho INT NOT NULL FOREIGN KEY REFERENCES Despacho(id),
    id_producto INT NOT NULL FOREIGN KEY REFERENCES Producto(id),
    cantidad_solicitada INT NOT NULL CHECK (cantidad_solicitada >= 0),
    cantidad_despachada INT NOT NULL CHECK (cantidad_despachada >= 0)
);

CREATE TABLE MovimientoInventario (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_producto INT NOT NULL FOREIGN KEY REFERENCES Producto(id),
    id_bodega INT NOT NULL FOREIGN KEY REFERENCES Bodega(id),
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    fecha DATETIME NOT NULL,
    referencia VARCHAR(100)
);

CREATE TABLE Factura (
    id INT IDENTITY(1,1) PRIMARY KEY,
    numero_factura VARCHAR(20) NOT NULL UNIQUE,
    fecha DATETIME NOT NULL,
    id_cliente INT NOT NULL FOREIGN KEY REFERENCES Cliente(id),
    id_orden INT NULL FOREIGN KEY REFERENCES OrdenVenta(id),
    id_usuario INT NOT NULL FOREIGN KEY REFERENCES Usuario(id),
    total DECIMAL(18,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(20) NOT NULL,
    observaciones VARCHAR(255)
);

CREATE TABLE DetalleFactura (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_factura INT NOT NULL FOREIGN KEY REFERENCES Factura(id),
    id_producto INT NOT NULL FOREIGN KEY REFERENCES Producto(id),
    cantidad INT NOT NULL CHECK (cantidad >= 0),
    precio_unitario DECIMAL(18,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(18,2) NOT NULL CHECK (subtotal >= 0)
);

CREATE TABLE MetodoPago (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

CREATE TABLE Pago (
    id INT IDENTITY(1,1) PRIMARY KEY,
    id_factura INT NOT NULL FOREIGN KEY REFERENCES Factura(id),
    id_metodo_pago INT NOT NULL FOREIGN KEY REFERENCES MetodoPago(id),
    monto DECIMAL(18,2) NOT NULL CHECK (monto >= 0),
    fecha DATETIME NOT NULL,
    referencia VARCHAR(100)
);

INSERT INTO Usuario (username, password, rol) VALUES ('admin', 'admin123', 'ADMIN');
INSERT INTO Bodega (nombre, ubicacion) VALUES ('Bodega Central', 'Ciudad');
INSERT INTO Categoria (nombre) VALUES ('Electronicos'), ('Muebles');
INSERT INTO Cliente (nit, nombre, direccion) VALUES ('CF', 'Consumidor Final', 'Ciudad');
GO
