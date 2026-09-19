# Sistema de Facturación y Ventas - MVC + DAO

Sistema de facturación desarrollado en Java con interfaz gráfica Java Swing y persistencia en Microsoft SQL Server mediante JDBC. La solución implementa el diagrama de clases del sistema de facturación y aplica el patrón arquitectónico **MVC + DAO** con separación estricta en capas.

---

## 🏛️ Arquitectura del Sistema (MVC + DAO)

El flujo de control y responsabilidades se distribuye en capas unidireccionales:

$$\text{Vista} \longrightarrow \text{Controlador} \longrightarrow \text{Servicio} \longrightarrow \text{DAO} \longrightarrow \text{Base de Datos (SQL Server)}$$

1. **Capa Vista (`gt.edu.umg.facturacion.vista`)**:
   - `FrmContenedorPadre`: Entorno MDI principal con menús de Ventas y Catálogos.
   - `PantallaFacturacion`: Formulario principal de facturación (`<<boundary>>`), implementa `solicitarEmision()` y `mostrarFactura(Factura)`.
   - `FrmFacturaFiltro`: Cuadro de búsqueda para clientes y productos.
   - Interfaz estilizada con tema oscuro moderno mediante **FlatLaf Dark**.

2. **Capa Controlador (`gt.edu.umg.facturacion.controlador`)**:
   - `FacturaController`: Orquesta la interacción entre `PantallaFacturacion` y `FacturaService`. Implementa `crear()`, `emitir()` y `consultar()`.
   - `FacturaFiltroController`: Controla la búsqueda y selección dinámica de entidades.
   - `ContenedorPadreController`: Gestiona la apertura y foco de los formularios MDI.

3. **Capa Servicio (`gt.edu.umg.facturacion.servicio`)**:
   - `FacturaService`: Centraliza las reglas de negocio:
     - Validación de cantidades estrictamente mayores a cero.
     - Bloqueo de productos inactivos.
     - Verificación de disponibilidad de stock en `Inventario`.
     - Descuento automático de existencias al emitir la factura.
     - Reposición automática de inventario al anular facturas emitidas o pagadas.
     - Transición de estados (`BORRADOR` $\rightarrow$ `EMITIDA` $\rightarrow$ `PAGADA` / `ANULADA`).

4. **Capa de Acceso a Datos - DAO (`gt.edu.umg.facturacion.dao`)**:
   - `ConexionBD`: Centraliza la conexión JDBC con SQL Server, implementando `try-with-resources`.
   - `FacturaDAO` / `FacturaDAOImpl`: Persistencia transaccional de facturas, líneas de detalle y pagos.
   - `ClienteDAO` / `ClienteDAOImpl`: Operaciones sobre clientes.
   - `ProductoDAO` / `ProductoDAOImpl`: Catálogo y actualización de inventario.
   - `UsuarioDAO` / `UsuarioDAOImpl` y `CategoriaDAO` / `CategoriaDAOImpl`.

5. **Capa Modelo de Dominio (`gt.edu.umg.facturacion.modelo`)**:
   - `Factura`, `DetalleFactura`, `Producto`, `Inventario`, `Cliente`, `Usuario`, `Pago`, `Categoria`.
   - Enumeraciones: `EstadoFactura` (`BORRADOR`, `EMITIDA`, `PAGADA`, `ANULADA`) y `MetodoPago` (`EFECTIVO`, `TARJETA`, `TRANSFERENCIA`).
   - Todos los cálculos monetarios y cantidades se manejan con **`BigDecimal`** (precisión exacta, sin pérdidas por coma flotante de `double`).

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Java 25 (OpenJDK / Oracle JDK 25 LTS).
- **Gestor de Dependencias y Construcción**: Apache Maven 3.9+.
- **Interfaz Gráfica**: Java Swing + [FlatLaf 3.4.1](https://www.formdev.com/flatlaf/).
- **Base de Datos**: Microsoft SQL Server (Transact-SQL).
- **Driver JDBC**: `com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11`.
- **Framework de Pruebas**: JUnit 5 (Jupiter 5.10.2).
- **IDE Recomendado**: Apache NetBeans 21+ / IntelliJ IDEA / VS Code.

---

## 📋 Requisitos Previos

1. **Java Development Kit (JDK 25)** instalado y configurado en la variable de entorno `JAVA_HOME`.
2. **Apache Maven 3.9+** instalado.
3. **Microsoft SQL Server** con:
   - Protocolo **TCP/IP** habilitado en el puerto `1433` (SQL Server Configuration Manager).
   - Modo de **Autenticación Mixta** (SQL Server and Windows Authentication).

---

## 🗄️ Configuración de SQL Server

1. Abra **SQL Server Management Studio (SSMS)** o Azure Data Studio.
2. Ejecute el archivo [`script_base_datos.sql`](script_base_datos.sql) incluido en la raíz del proyecto.
   - Crea la base de datos `FacturacionDB`.
   - Crea las tablas con claves foráneas, restricciones `CHECK` y tipos `DECIMAL(12,2)`.
   - Inserta datos semilla de prueba (categorías, inventarios, productos, clientes y usuarios).
3. **Configuración de Credenciales**:
   - Por defecto, `ConexionBD.java` se conecta a `127.0.0.1:1433`, base de datos `FacturacionDB`, usuario `sa` y contraseña `12345`.
   - Puede sobreescribir las credenciales sin modificar el código definiendo variables de entorno:
     ```powershell
     $env:DB_HOST="localhost"
     $env:DB_PORT="1433"
     $env:DB_NAME="FacturacionDB"
     $env:DB_USER="sa"
     $env:DB_PASSWORD="TuPasswordSeguro"
     ```

---

## 🚀 Forma de Ejecutar

### Desde Línea de Comandos (Maven)

1. **Compilar el proyecto:**
   ```powershell
   mvn clean compile
   ```

2. **Ejecutar pruebas unitarias:**
   ```powershell
   mvn test
   ```

3. **Iniciar la aplicación:**
   ```powershell
   mvn exec:java
   ```

### Desde Apache NetBeans

1. Abra NetBeans y seleccione **File $\rightarrow$ Open Project**.
2. Seleccione la carpeta `proyecto-final-programacion-2` (NetBeans reconocerá el proyecto Maven de inmediato).
3. Haga clic derecho en el proyecto y seleccione **Run** (o presione `F6`).

---

## 🔄 Flujo Principal del Sistema

1. **Inicio**: Se abre el contenedor MDI principal y se despliega automáticamente la **Pantalla de Facturación** en estado `BORRADOR`.
2. **Seleccionar o Crear Cliente**:
   - Ingrese el NIT del cliente (o use el botón `🔍 Buscar` para abrir el filtro). Si no se ingresa, se asume *Consumidor Final (CF)*.
3. **Seleccionar Producto e Indicar Cantidad**:
   - Ingrese el código del producto (ej: `FER-001`) o haga clic en `🔍` para consultar el catálogo con stock disponible.
   - Ingrese la cantidad deseada y el descuento (si aplica).
4. **Agregar Producto a la Factura**:
   - Haga clic en `➕ Agregar Producto`.
   - El sistema valida disponibilidad de inventario y agrega la fila calculando subtotal, IVA (12%) y total en tiempo real con `BigDecimal`.
5. **Emitir Factura**:
   - Presione `🚀 Emitir Factura`. El sistema valida que existan detalles, descuenta las existencias del inventario en SQL Server, cambia el estado a `EMITIDA` y persiste el documento.
6. **Registrar Pago**:
   - Seleccione el método (`EFECTIVO`, `TARJETA`, `TRANSFERENCIA`), ingrese el monto y referencia, y presione `💳 Registrar Pago`. Al cubrir el total, el estado pasa a `PAGADA`.
7. **Consultar y Anular Factura**:
   - Puede consultar cualquier factura por su número correlativo (ej: `FAC-1001`) con `🔎 Consultar Factura`.
   - El botón `❌ Anular Factura` cancela la factura y devuelve el stock al inventario automáticamente.

---

## 👥 Integrantes / Autor

- **Marlon Porres** - Programación II, Universidad Mariano Gálvez de Guatemala.
