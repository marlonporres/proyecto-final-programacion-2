# Auditoría de cumplimiento: Android + API REST

**Fecha:** 30 de octubre de 2026. **Resultado:** implementación alineada y compilable; entrega completa todavía pendiente. No se certifica una nota de 8/8 sin el recorrido funcional requerido.

## Fuente y alcance

Se leyeron y renderizaron las tres páginas de `C:\Users\marlo\Downloads\Programación II - Tarea Final_ Integración Android + API REST.pdf`. Este documento evalúa **8 puntos**, no 15. SHA-256: `27290BF1189AB335013C86DF9975F018D3E985983BAEB90ED7B71B82FA628DEF`.

Se auditaron las versiones actuales de `ventas-api-rest`, `ventas-android` y `ventas-escritorio`, sus configuraciones, pruebas, documentación, Git y lanzadores del directorio padre. La carpeta original `proyecto-final-programacion-2` ya no existe. Los resultados del checkpoint anterior no se utilizaron como evidencia actual.

La revisión ejecutó compilaciones y pruebas sin integración SQL. No inició SQL Server, la API ni túneles, aplicó migraciones, creó ventas, instaló APK, hizo commits ni publicó repositorios. Únicamente se añade este informe; las compilaciones generan sus salidas habituales ignoradas.

## Matriz de la rúbrica actual

| Criterio | Puntos | Evidencia actual | Estado y cierre requerido |
| --- | ---: | --- | --- |
| API REST | 2.0 | POST `/api/v1/ordenes-venta`; JSON validado; respuesta 201 con cuerpo y Location; cabecera y detalles dentro de transacción JDBC; manejo de errores. 61 pruebas aprobadas. | Implementado y probado por partes. Confirmar POST real y persistencia de la misma orden en SQL. |
| Aplicación Android | 2.0 | Formulario, DTO, Retrofit POST, URL HTTP/HTTPS configurable y mensajes de éxito/error. APK compilado; 23 pruebas aprobadas. | Implementado y probado por partes. Ejecutar el formulario en teléfono o emulador contra el túnel público. |
| Integración completa | 1.5 | Contratos compatibles y núcleo compartido coherente. Las pruebas HTTP simulan servicios y las pruebas SQL quedaron omitidas. | **Sin evidencia actual del recorrido completo** Android → ngrok → API → SQL → respuesta. |
| Proyecto de escritorio | 0.5 | Programa actualizado, compilable; 42 pruebas aprobadas. Las 51 clases del núcleo coinciden por hash con API. | Conforme en fuente y construcción; comprobar la orden de Android desde la base y, como apoyo, desde escritorio. |
| Repositorios | 1.0 | Tres carpetas con Git local independiente, README y exclusiones. Todos están en main, sin commits y sin remotos. | **Pendiente:** publicar y entregar tres enlaces reales accesibles al ingeniero. Carpetas locales no satisfacen la entrega de enlaces. |
| Demostración y explicación | 1.0 | Hay guion local con Wi-Fi/USB/emulador. No hay instrucciones ngrok en los README/guiones revisados ni túnel activo comprobado. | **Pendiente:** preparar ngrok y ensayar la presentación pública con explicación de cada salto. |
| **Total** | **8.0** | La tabla no asigna una calificación simulada. | Cerrar evidencia, publicación y demostración. |

## Resultados reproducidos hoy

| Proyecto | Comprobación | Aprobadas | Omitidas | Fallos/errores |
| --- | --- | ---: | ---: | ---: |
| API REST | Maven verify, sin integración SQL | 61 | 9 | 0/0 |
| Escritorio | Maven clean verify, sin integración SQL | 42 | 23 | 0/0 |
| Android | assembleDebug + testDebugUnitTest + lintDebug, offline | 23 | 0 | 0/0 |

Android lint: **0 errores y 143 advertencias**. Entre ellas: 59 SetTextI18n, 36 UnusedResources, 21 HardcodedText y 7 SmallSp. No equivalen automáticamente a pérdida de puntos, pero conviene revisar legibilidad y mensajes.

Los conteos se comprobaron directamente en los XML de `ventas-api-rest/target/surefire-reports`, `ventas-escritorio/target/surefire-reports` y `ventas-android/app/build/test-results/testDebugUnitTest`. Lint está en `ventas-android/app/build/reports/lint-results-debug.xml`.

Herramientas/comandos comprobados:

```powershell
# En ventas-api-rest
& 'C:\Users\marlo\.m2\apache-maven-3.9.9\bin\mvn.cmd' -o '-Dmaven.repo.local=C:\Users\marlo\.m2\repository' '-Dventas.api.it=false' verify

# En ventas-escritorio
& 'C:\Users\marlo\.m2\apache-maven-3.9.9\bin\mvn.cmd' -o '-Dmaven.repo.local=C:\Users\marlo\.m2\repository' '-Dventas.it=false' '-Dventas.api.it=false' clean verify

# En ventas-android
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug --offline --no-daemon
```

Durante la inspección SQL Server estaba **Stopped**; no había listeners detectados en 8080/4040. `ngrok` no se encontró en PATH. Esto no demuestra que no exista un ejecutable en otra carpeta. ADB no mostró dispositivos y no se encontraron AVD disponibles. No se modificó ese estado para la auditoría.

## Hallazgos prioritarios

### P1-01. Falta ngrok en la preparación de la entrega

La página 2 del PDF exige levantar el túnel público y la página 3 asigna un punto a la demostración con ngrok. El README maestro y los README de API/Android explican redes locales; el guion maestro tampoco incorpora el túnel. Wi-Fi, USB y emulador son alternativas de desarrollo, pero no documentan ni prueban el recorrido público solicitado.

**Acción:** instalar o localizar ngrok, configurar la cuenta localmente, iniciar el túnel al puerto 8080 y utilizar `https://DOMINIO_REAL/api/v1/` en Android. Conservar la barra final y verificar el dominio vigente antes de presentar. La API local puede seguir escuchando HTTP; el cliente utiliza la URL HTTPS pública del túnel. Instrucciones oficiales para Windows: https://ngrok.com/download/windows . El token es privado y no se incluye en repositorios ni capturas.

### P1-02. No se han entregado tres repositorios remotos

`git status`, `git log` y `git remote -v` confirman main sin commits ni remotos en los tres proyectos. El README maestro conserva una URL de ejemplo en su sección de publicación. No hay tres enlaces reales para acreditar el punto de repositorios.

**Acción:** revisar contenido, crear el commit inicial de cada proyecto, publicar en tres repositorios y comprobar desde otra sesión que el ingeniero pueda acceder. Incluir los tres enlaces en la entrega. Los lanzadores/README del directorio padre no pertenecen a ninguno de esos repositorios; la guía necesaria debe quedar disponible dentro de un repositorio entregado o como material complementario identificable.

### P1-03. Falta evidencia actual de integración completa

`ventas-api-rest/src/test/java/.../api/controlador/ContratoHttpTest.java` usa un servicio simulado; comprueba HTTP, no una escritura SQL real. Las pruebas SQL opcionales llaman al servicio de dominio y no representan al teléfono ni al túnel. Las pruebas Android también utilizan dobles de repositorio o verifican serialización/formato.

**Acción:** crear una orden nueva e identificable desde Android usando ngrok, capturar el POST y su respuesta, anotar ID/número y verificar esa misma cabecera y sus detalles en SQL Server. Volver a consultarla para demostrar persistencia. Mostrar además un error de validación. Las pruebas instrumentadas no son obligatorias por el PDF; sí lo es la demostración funcional.

### P1-04. Guiones y matriz antiguos pueden orientar mal la exposición

`ventas-escritorio/docs/alineacion_rubrica.md:5` corresponde al PDF anterior de cinco puntos. `docs/guion_entrega.md:41` y `docs/defensa_proyecto.md:36` excluyen Android/API de aquella fase. Son documentos históricos válidos para su entrega anterior, pero no constituyen la rúbrica actual de ocho puntos.

**Acción:** rotular su alcance histórico y preparar un guion actual que siga los ocho pasos del checklist del PDF, con ngrok y las tres URLs de repositorios. Actualizar fechas y cifras que se presentarán como actuales.

### P2-01. Probar conexión puede aceptar un servidor equivocado

`ventas-android/app/src/main/java/gt/edu/umg/ventas/movil/data/network/ApiService.java:21` define salud como `Call<Void>`. `.../data/repository/OrdenRepository.java:28` acepta cualquier respuesta 2xx. `.../ui/ConexionActivity.java:140` muestra siempre «Conectado · 200 OK».

Por inspección, una respuesta HTML u otro cuerpo con 200 sería aceptada. No se reprodujo en dispositivo; no es un fallo del POST demostrado, sino un defecto de diagnóstico que puede confundir la presentación.

**Acción:** deserializar una respuesta de salud y exigir `estado=OK` y `baseDatos=DISPONIBLE`; manejar cuerpo inesperado, error HTTP y fallo de red; probar 200 con JSON correcto, HTML, estado inválido y 503.

### P2-02. Los enlaces locales no sirven como enlaces de entrega

El README maestro utiliza `file:///c:/Users/marlo/...` para los proyectos. Esas rutas no permiten al ingeniero navegar desde GitHub.

**Acción:** usar enlaces relativos dentro de cada repositorio y URLs reales para enlazar repositorios distintos. Probar los enlaces desde GitHub antes de entregar.

### P2-03. Los lanzadores tienen efectos amplios

`detener-demostracion.ps1:16–33` reúne cualquier dueño del puerto 8080 y procesos Gradle/Kotlin que pueden pertenecer a otros proyectos; también elimina puentes ADB globales. `iniciar-demostracion.ps1` evalúa SoloVerificar después de operaciones que pueden iniciar servicios, modificar firewall o abrir API.

**Acción:** acotar los procesos a PID/rutas del proyecto y aclarar los efectos de cada modo. Ninguno de estos lanzadores se ejecutó en la auditoría. Esto es un riesgo operativo, no un criterio adicional de la rúbrica.

### P2-04. Un error después del commit puede dejar resultado incierto

`ventas-api-rest/src/main/java/gt/edu/umg/ventas/api/servicio/OrdenesApiService.java:111–112` guarda la orden y después recarga la respuesta. Si falla la consulta posterior, el cliente podría recibir un error aunque la orden ya exista. Un reintento sin comprobación puede duplicar la venta.

**Acción:** añadir una regresión de fallo posterior al guardado y definir recuperación/consulta antes de reenviar; evaluar idempotencia si hace falta. Robustez adicional útil, sin exigir una arquitectura avanzada para esta actividad.

### P3. Acabado y trazabilidad de imágenes

El manifiesto actual cubre 24 productos y el JAR API contiene 20 archivos de imagen. Esto supera el checkpoint anterior; su funcionamiento HTTP/SQL/teléfono no se verificó en esta revisión. `ventas-api-rest/docs/imagenes/README.md` menciona `fuentes_wikimedia.json`, pero el archivo no está presente. Restaurar el registro de fuentes o corregir la documentación. Las fotografías y una interfaz avanzada no tienen puntuación propia en esta rúbrica.

## Aspectos que sí están bien implementados

- POST en `OrdenesController.java:28` con 201, respuesta y Location.
- Cliente/producto positivos, detalles obligatorios, cantidades enteras positivas, observaciones de longitud limitada, control de productos existentes/activos y líneas duplicadas.
- Precios, fecha, operador y totales determinados por el servidor; IVA con BigDecimal.
- Cabecera y líneas persistidas juntas: `OrdenVentaDAOImpl.java:31,75,78` configura transacción, commit y rollback.
- Android construye el DTO correcto y consume POST con Retrofit; maneja éxito/error y bloquea envíos mientras guarda.
- Corregidos los fallos antiguos de filtro COMPLETADA, callbacks fuera de orden y conservación del borrador mediante SavedStateHandle.
- Los tres proyectos compilan de manera independiente. Las clases del núcleo coinciden entre escritorio y API.
- Configuraciones privadas están ignoradas; el wrapper JAR de Android es versionable. No se encontró exposición de las credenciales locales en los archivos publicables revisados. Todavía no hay historial remoto que auditar.

No se requieren JWT, login, CORS para cliente nativo, servidor de producción, dominio propio, pagos ni arquitectura avanzada por este PDF. Las mejoras extra no sustituyen el POST y la evidencia de integración.

## Plan de cierre verificable

1. Resolver P2-01 para confiar en el diagnóstico de conexión.
2. Preparar y documentar ngrok. Con API local en 8080 y ngrok configurado, el comando de túnel es `ngrok http 8080`.
3. Ejecutar la prueba real siguiente y guardar evidencia sin credenciales.
4. Publicar los tres repositorios y comprobar su acceso.
5. Actualizar el guion a la rúbrica de ocho puntos y ensayar la explicación individual.
6. Resolver enlaces locales y riesgos de lanzadores; después, pulir advertencias y trazabilidad de imágenes.

### Checklist de la demostración pendiente

- [ ] API y SQL Server iniciados; `/api/v1/salud` comprueba la base real.
- [ ] Mostrar POST `/api/v1/ordenes-venta`, su JSON y códigos de respuesta.
- [ ] ngrok expone 8080 y muestra una URL pública HTTPS.
- [ ] Android utiliza esa URL pública con `/api/v1/`, no la IP LAN.
- [ ] Crear una orden identificable desde el formulario de Android.
- [ ] Ver la petición POST y una respuesta 201 con ID/número en la inspección del túnel u otra evidencia HTTP.
- [ ] Consultar SQL Server y comprobar la misma cabecera, cliente, líneas, cantidades y total.
- [ ] Volver a consultar la orden y mostrar un error controlado de validación.
- [ ] Explicar Android → ngrok → controlador → servicio/DAO → base → respuesta.
- [ ] Entregar los tres enlaces accesibles al ingeniero.

Estos casilleros permanecen sin marcar porque la auditoría no ejecutó ese recorrido. El criterio de cierre es la evidencia, no la cantidad de funcionalidades adicionales.