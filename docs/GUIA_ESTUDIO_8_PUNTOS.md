# Estudio y defensa: integración Android + API REST (8 puntos)

Actualizado el 30 de octubre de 2026. Esta guía corresponde al PDF «Tarea final - Integración de aplicación Android con API REST», de tres páginas. El documento anterior de cinco puntos corresponde únicamente a la fase histórica de escritorio.

## Qué se evalúa

| Criterio | Puntos | Demostración necesaria |
| --- | ---: | --- |
| API REST | 2 | POST con datos válidos, respuesta y guardado en SQL |
| Android | 2 | Formulario, construcción del JSON, Retrofit y resultado visible |
| Integración | 1.5 | La misma orden viaja desde el teléfono hasta la base |
| Escritorio | 0.5 | Código actualizado y coherente con el sistema |
| Repositorios | 1 | Tres enlaces reales accesibles al ingeniero |
| Demostración y explicación | 1 | ngrok y explicación del flujo completo |
| Total | 8 | Código, enlaces y operación en vivo |

## La explicación central

«Android recoge cliente, productos y cantidades. Retrofit convierte la solicitud a JSON y hace POST a la URL HTTPS de ngrok. El túnel lleva la solicitud a Spring Boot en localhost:8080. El controlador valida el cuerpo, el servicio aplica reglas y el DAO guarda cabecera y detalles juntos en SQL Server. Si termina correctamente, la API responde 201 Created con el número de orden. Android muestra el resultado.»

El escritorio usa JDBC directo a la misma base; la app Android utiliza HTTP y no contiene claves SQL. Tres repositorios permiten compilar y revisar cada aplicación por separado.

## Archivos que debes poder explicar

- Android: `ApiService` define `@POST("ordenes-venta")`; `OrdenRequest` contiene clienteId, observaciones y detalles; `OrdenFormViewModel` construye la petición; `OrdenRepository` la envía; `OrdenFormActivity` muestra éxito o error.
- Conexión Android: `SaludResponse` exige estado OK y baseDatos DISPONIBLE. `OrdenRepository.checkSalud` rechaza cuerpo vacío, estado inesperado y respuestas de error. Un HTTP 200 aislado no demuestra una base disponible.
- API: `OrdenesController` expone `/api/v1/ordenes-venta`, valida JSON y devuelve 201/Location; `OrdenesApiService` carga cliente/productos/precios; `OrdenVentaService` aplica reglas; `OrdenVentaDAOImpl` abre la transacción y confirma o revierte.
- Base: OrdenVenta guarda la cabecera; DetalleOrdenVenta guarda sus líneas. El ID/número de la respuesta permite comprobar que se guardó la misma orden.

## Preguntas para practicar

**¿Qué hace ngrok?** Publica temporalmente el servidor local mediante una URL pública. No sustituye Spring Boot ni SQL Server. Para esta actividad no se exige hosting de producción.

**¿Qué es POST?** La petición utilizada para crear el recurso. Envía JSON en el cuerpo; la ruta identifica el conjunto de órdenes.

**¿Por qué 201 y no solo un mensaje?** 201 expresa que se creó un recurso; el cuerpo devuelve su identificación y Location indica dónde consultarlo.

**¿Qué valida la API?** Cliente y producto existentes, cantidades enteras positivas, líneas no vacías, productos activos, disponibilidad y límites del texto. Android ayuda al usuario, pero el servidor vuelve a validar.

**¿Quién calcula los precios y el total?** El servidor consulta los precios válidos y usa BigDecimal con IVA académico de 12 %. El teléfono muestra una previsualización.

**¿Qué ocurre si falla una línea?** El DAO revierte la transacción para evitar una cabecera sin sus detalles.

**¿Crear una orden descuenta inventario?** No. La salida física ocurre al confirmar el despacho desde escritorio. Es una regla del sistema, no un requisito adicional del POST.

**¿Cómo pruebo la persistencia?** Comparo ID/número, cliente, productos, cantidades y total de Android/respuesta con una consulta directa a SQL Server; vuelvo a consultar después.

**¿Por qué cambia la URL pública?** Depende de la configuración de la cuenta/túnel. Antes de presentar siempre se verifica la URL vigente y se configura Android con `/api/v1/` y barra final.

**¿Qué hago ante error o espera agotada?** Muestro un mensaje y conservo los datos; si el resultado de crear es incierto, consulto las órdenes antes de reenviar para evitar duplicados.

**¿Qué prueban las pruebas unitarias?** Reglas, formatos y contratos aislados. No sustituyen la creación desde un teléfono a través de ngrok y la consulta SQL.

## Ensayo personal

Explica el flujo en un minuto; señala en código dónde se construye el JSON, dónde está POST y dónde se hace commit/rollback. Después realiza el recorrido del documento DEMOSTRACION_8_PUNTOS.md. Prioriza estos requisitos antes de explicar fotografías u otras funciones adicionales.

## Practicar con una guía interactiva

Abrir [guia-interactiva/README.md](guia-interactiva/README.md) y seguir las instrucciones. Es un material educativo con datos ficticios; el guion de entrega sigue requiriendo operar las aplicaciones reales y verificar la persistencia.
