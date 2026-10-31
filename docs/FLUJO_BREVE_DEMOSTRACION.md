# Explicación breve y operación de la demostración

## Comandos desde sistema-ventas-proyecto

```powershell
.\iniciar-demostracion.cmd
.\iniciar-demostracion.cmd -SoloVerificar
.\iniciar-demostracion.cmd -SinEscritorio
.\detener-demostracion.cmd -WhatIf
.\detener-demostracion.cmd
```

- Inicio normal: comprueba/inicia SQL Server y API, inicia o reutiliza el túnel HTTPS de ngrok, comprueba salud pública y solicita abrir escritorio. Los procesos auxiliares quedan ocultos; la aplicación Swing se abre normalmente.
- `-SoloVerificar`: consulta estado y URL sin iniciar servicios ni registrar/adoptar procesos.
- `-SinEscritorio`: deja API y ngrok listos para la prueba móvil.
- `-SinNgrok`: apoyo local; no sustituye el túnel requerido para la entrega.
- `-Recompilar`: vuelve a compilar los componentes que el lanzador necesite iniciar. Para forzar una versión nueva de un servicio ya activo, detener primero y volver a iniciar.
- Cierre normal: detiene los procesos registrados del proyecto y sus hijos, comprobando PID y fecha de inicio para evitar confundir un PID reutilizado. Incluye ngrok manual si fue identificado y adoptado por el inicio de la demostración.
- SQL Server sigue encendido al cerrar; `-DetenerSql` permite apagarlo expresamente y puede requerir administración.
- Los daemons globales de Gradle, ADB, otros servidores y reglas del firewall no se modifican.

Los lanzadores de la carpeta padre son accesos a las versiones mantenidas en `ventas-escritorio/scripts`. Desde el repositorio también puede ejecutarse `scripts\iniciar-demostracion.cmd`; debe tener las carpetas hermanas API y Android dentro del directorio del proyecto.

El inicio detecta la URL real de ngrok; no contiene un dominio fijo. Si el token falta o el puerto está ocupado por otro servicio, muestra el problema sin cerrar procesos desconocidos. Con ngrok previamente activo, lo reutiliza; si el inspector 4040 corresponde a otro destino, no lo reemplaza.

Logs, URL y registro local de procesos: `ventas-escritorio/target/demostracion/`. No se versionan. La configuración privada y el token no se imprimen ni se guardan en este material.

## Flujo de explicación: 5–7 minutos

| Paso | Acción | Frase para explicar |
| --- | --- | --- |
| 1. Arquitectura (30 s) | Mostrar los tres repositorios. | «Separamos escritorio, API REST y Android. El teléfono envía HTTP; el escritorio comparte la base mediante JDBC.» |
| 2. Arranque y túnel (1 min) | Ejecutar iniciar-demostracion.cmd; mostrar salud y URL pública. | «SQL Server almacena los datos; Spring Boot recibe solicitudes en 8080. ngrok proporciona la URL HTTPS temporal que usa Android.» |
| 3. Crear desde Android (1–2 min) | Pegar URL con /api/v1/, probar conexión, elegir cliente y producto, guardar una orden identificable. | «Retrofit convierte clienteId, observaciones y detalles a JSON y hace POST a /api/v1/ordenes-venta.» |
| 4. API y validación (1 min) | Mostrar POST y respuesta 201 en el inspector http://127.0.0.1:4040. Mostrar un campo inválido controlado. | «El servidor vuelve a validar, toma los precios oficiales y guarda cabecera y detalles en una transacción. Devuelve éxito o un error comprensible.» |
| 5. Persistencia (1 min) | Consultar la misma orden por su número en SQL Server y volver a abrirla en Android. | «El mismo número y las mismas líneas demuestran que se guardó en la base, no únicamente en la pantalla.» |
| 6. Cierre (30 s) | Ejecutar detener-demostracion.cmd al terminar de mostrar la orden. | «Cerramos el túnel y los procesos identificados de la demostración. SQL queda disponible salvo que pidamos apagarlo.» |

No iniciar el cierre mientras se captura la evidencia o se guarda una orden. No repetir un POST con resultado incierto antes de consultar si se creó.

## Resumen para memorizar

«Android recoge los datos, Retrofit envía JSON por POST a la URL HTTPS de ngrok, el túnel lo entrega a Spring Boot local, el servicio valida y el DAO guarda en SQL Server. La API responde 201 con el número de orden y Android muestra el resultado. Comprobamos ese mismo número directamente en la base.»

## Qué mostrar para los 8 puntos

Tres enlaces accesibles; endpoint POST; formulario y mensaje en Android; túnel público; petición/respuesta; cabecera y detalles en SQL; explicación del flujo. Fotografías, despacho y factura pueden mostrarse después como complemento, sin desplazar la integración requerida.

## Verificación de esta actualización (30 de octubre de 2026)

- Sintaxis de los scripts comprobada y ejecución con Windows PowerShell 5.1.
- CMD de inicio normal probado: API y túnel existentes reutilizados, salud local/pública válidas y proceso Java del escritorio iniciado.
- Segundo inicio comprobado: no duplica el proceso del escritorio.
- SoloVerificar probado sin iniciar servicios ni registrar procesos.
- Cierre probado en procesos auxiliares exclusivos de QA: WhatIf conserva el proceso; cierre real detiene el registrado; conserva un control no registrado y un PID cuya fecha no coincide.
- Simulación del cierre de la demostración actual comprobada. La sesión real se dejó encendida para continuar la prueba desde Android.
- No se certificó aquí un reinicio completo desde todos los servicios apagados ni se apagó SQL Server.