> **Aprende visualmente:** la [guía web interactiva](docs/guia-interactiva/README.md) permite seguir una orden con datos ficticios, explorar la arquitectura y probar qué sucede si falla una parte.

**Repositorio de entrega:** [ventas-escritorio](https://github.com/marlonporres/proyecto-final-programacion-2)

> **Entrega vigente: Android + API REST (8 puntos).** Leer [estudio y defensa](docs/GUIA_ESTUDIO_8_PUNTOS.md), [demostración con ngrok](docs/DEMOSTRACION_8_PUNTOS.md) y [enlaces de entrega](docs/ENTREGA_REPOSITORIOS.md). El flujo evaluado es Android → ngrok → API → SQL → respuesta.

# Sistema de Ventas · Escritorio

Aplicación administrativa en **Java 25, Swing, FlatLaf y JDBC** conectada directamente a **SQL Server** (`SistemaVentas`) para gestionar clientes (con múltiples teléfonos tipificados), categorías, productos, bodegas, inventario, órdenes de venta, despachos transaccionales y facturación.

## Documentación académica y de defensa

- [Informe del cambio en caliente en clientes (PDF local)](output/pdf/Informe_cambios_en_caliente_clientes.pdf)
- [Guion de demostración](docs/guion_entrega.md)
- [Defensa técnica del proyecto](docs/defensa_proyecto.md)
- [Alineación con la rúbrica](docs/alineacion_rubrica.md)
- [Catálogo académico de computación (24 productos)](docs/catalogo_computacion.md)

## Requisitos

- **JDK 25** o superior.
- **Maven 3.9+** (en `PATH` o en `~/.m2/apache-maven-3.9.9`).
- **SQL Server** (`MSSQLSERVER`) en ejecución.

## Preparar la base de datos y la conexión

1. En una instalación limpia, ejecutar [script_base_datos_ventas.sql](script_base_datos_ventas.sql) en SQL Server Management Studio para crear `SistemaVentas`, sus 15 tablas comerciales y las semillas iniciales.
2. Copiar la plantilla de configuración:
   ```powershell
   Copy-Item .\src\main\resources\database.properties.example .\src\main\resources\database.properties
   ```
3. Completar `src/main/resources/database.properties` con las credenciales locales de SQL Server (archivo excluido por `.gitignore`).

## Compilar, probar y ejecutar

```powershell
# Ejecutar directamente con el lanzador de Windows:
.\scripts\demostracion.cmd

# Ejecutar pruebas unitarias (sin requerir SQL Server):
mvn '-Dventas.it=false' '-Dventas.api.it=false' test

# Ejecutar suite completa incluyendo pruebas de integración SQL:
mvn '-Dventas.it=true' '-Dventas.api.it=true' verify
```

## Reglas de negocio principales

- **Clientes:** soporte de múltiples teléfonos por cliente (`Personal`, `Trabajo`, `Casa`, `Otro`).
- **Órdenes de venta:** validan disponibilidad y congelan el precio unitario del renglón; **no descuentan ni reservan inventario**.
- **Despachos:** generar un despacho lo deja en estado `PENDIENTE`. **Confirmar el despacho** descuenta existencias en bodega, registra el movimiento de salida (`Kardex`) y marca la orden como `COMPLETADA` dentro de una única transacción JDBC.
- **Facturación:** se emite sobre órdenes despachadas calculando Subtotal e IVA (12 %) con `BigDecimal`.
