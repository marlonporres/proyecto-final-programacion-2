# Implementación posterior a la auditoría — 8 puntos

Fecha: 30 de octubre de 2026.

## Hecho y comprobado

- Android valida estado OK y baseDatos DISPONIBLE en la respuesta de salud, exige HTTP 200 y rechaza cuerpo vacío, JSON ajeno y HTML.
- Se añadieron seis regresiones de repositorio y cuatro de transporte HTTP real mediante Retrofit a un servidor efímero local. Resultado: **33 pruebas aprobadas**, cero fallos/errores; assembleDebug y lintDebug correctos. Estas pruebas no simulan una ejecución en teléfono por ngrok.
- ngrok **3.39.11-msix-stable** instalado desde Microsoft Store. Windows bloqueó la descarga ZIP directa; se utilizó la alternativa oficial de Store manteniendo la protección activa.
- API incluye scripts/ngrok.cmd y ngrok.ps1, con comprobación de salud local/pública, PID propio, URL de Android y logs ignorados en target.
- scripts/verificar-entrega.ps1 registra la consulta pública de un ID real sin atribuir falsamente el POST a Android. docs/verificar_orden_android.sql permite comparar cabecera y líneas directamente en SQL.
- SoloVerificar se evalúa antes de iniciar servicios, firewall, ADB o API. Se probó con servicios detenidos y no los inició.
- El cierre deja de buscar por puerto 8080 y de detener daemons/ADB ajenos; identifica procesos por las rutas del proyecto y el túnel registrado.
- Los tres proyectos incluyen GUIA_ESTUDIO_8_PUNTOS.md, DEMOSTRACION_8_PUNTOS.md y ENTREGA_REPOSITORIOS.md. Los materiales anteriores de cinco puntos llevan aviso histórico. Se corrigieron enlaces locales de los proyectos en el README general.

## Pendiente de cuenta/equipo y de prueba presencial

1. Configurar el token privado de la cuenta ngrok en consola local. `ngrok config check` confirma que aún no existe configuración en el equipo. Nunca enviar el token al chat ni publicarlo en Git.
2. Autorizar el inicio de MSSQLSERVER. El inicio directo fue denegado y se solicitó la elevación oficial de Windows; en la última comprobación el servicio seguía detenido.
3. Compartir las URL de tres repositorios nuevos para configurar remotos y publicar por push normal. La búsqueda en GitHub encontró proyectos previos, no los tres nuevos independientes; no se sobrescribió ninguno.
4. Conectar el teléfono/emulador, crear una orden por la URL pública y comparar el ID/número y sus detalles en SQL. Esta evidencia sigue pendiente; no se marcan esos puntos como completados.

No se ejecutaron migraciones ni ventas ni se borraron registros comerciales en esta implementación. Los materiales de estudio reflejan los cambios hechos y mantienen separadas las comprobaciones pendientes.
## Actualización posterior: publicación y pruebas

Los tres repositorios ya están publicados y main remoto fue comprobado contra el commit local. Java conserva el historial anterior sin force push.

SQL Server y la API local están disponibles. La API terminó 70 pruebas aprobadas, incluidas nueve con SQL real, sin fallos, errores ni omisiones. También se verificó una prueba local de POST 201, persistencia mediante consulta SQL y cancelación de una orden QA con inventario intacto. Los identificadores y detalles de la evidencia se conservan localmente en target, excluido de Git; no se publican datos comerciales.

Android conserva 33 pruebas aprobadas. La prueba anterior procede de la computadora, no del teléfono; queda pendiente Android por el túnel público.

El usuario autorizó expresamente la exposición temporal de la API actual. ngrok está instalado, pero el intento de autenticación requiere configurar el authtoken real de la cuenta: guardar el texto TU_TOKEN no lo sustituye. La configuración estructuralmente válida no garantiza autenticación ni un túnel activo.

Esta actualización sustituye los pendientes iniciales de publicación e inicio de SQL. El siguiente paso es configurar la cuenta ngrok y realizar la demostración desde teléfono.