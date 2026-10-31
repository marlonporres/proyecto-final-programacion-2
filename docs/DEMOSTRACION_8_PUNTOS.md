# Demostración reproducible: los 8 puntos

## Preparación (antes de la exposición)

1. Iniciar SQL Server (MSSQLSERVER). Si Windows pide administración, usar la cuenta autorizada; no recrear la base existente.
2. Desde ventas-api-rest ejecutar `scripts\api.cmd -SinCompilar` después de compilar el proyecto. Dejar el proceso abierto. Verificar http://localhost:8080/api/v1/salud y confirmar `estado=OK`, `baseDatos=DISPONIBLE`.
3. Instalar ngrok por Microsoft Store según https://ngrok.com/download/windows . La descarga directa fue bloqueada por la protección de Windows en este equipo; no desactivar el antivirus.
4. Configurar el token en una consola local con `ngrok config add-authtoken SU_TOKEN_PRIVADO`. Obtenerlo de la cuenta propia; no pegarlo en chats, capturas ni Git. Reiniciar la consola si acaba de instalar ngrok.
5. Desde ventas-api-rest ejecutar `scripts\ngrok.cmd`. El lanzador verifica la salud local y pública y escribe `target/url-publica.txt`. Alternativa manual: `ngrok http 8080`.
6. En Android usar `https://DOMINIO_PUBLICO_REAL/api/v1/`, probar y guardar. La IP Wi-Fi y adb reverse son apoyos para desarrollar; la demostración evaluada usa ngrok.
7. Abrir el inspector http://127.0.0.1:4040 y SSMS. Preparar la consulta `ventas-api-rest/docs/verificar_orden_android.sql`.
8. Tener los tres repositorios publicados y accesibles; completar ENTREGA_REPOSITORIOS.md con sus enlaces reales.

## Guion de 8–10 minutos

| Minuto | Acción visible | Explicación |
| --- | --- | --- |
| 0–1 | Mostrar los tres repositorios y el diagrama del flujo | Escritorio, servidor HTTP y cliente Android tienen responsabilidades separadas |
| 1–2 | Mostrar API, salud y POST en OrdenesController | Endpoint, JSON y validación del servidor |
| 2–3 | Mostrar ngrok activo y la misma URL HTTPS en Android | El teléfono se comunica por el túnel público |
| 3–5 | Crear una orden nueva desde Android con observación `DEMO-NGROK-fecha-hora` | Elegir IDs reales del catálogo, cliente y cantidad disponible; mostrar Guardar y el mensaje de éxito |
| 5–6 | Inspector: mostrar POST, cuerpo JSON y respuesta 201 | Anotar ID/número; ocultar credenciales y otros datos ajenos |
| 6–7 | SSMS: consultar la misma orden y detalles por su número | Comparar cliente, productos, cantidades y total; volver a consultar desde Android |
| 7–8 | Mostrar un caso inválido controlado | Cantidad cero/vacía o superior a disponible; explicar mensaje sin cierre inesperado |
| 8–10 | Explicar controlador → servicio → DAO → transacción | Responder preguntas del material de estudio; escritorio y despacho pueden mostrarse como complemento |

## Registro de evidencia, sin afirmar lo que no se ejecutó

- [ ] URL HTTPS pública probada.
- [ ] Orden creada desde Android (captura o demostración presencial).
- [ ] POST y respuesta 201 con ID/número capturados.
- [ ] Cabecera y líneas verificadas directamente en SQL.
- [ ] Consulta posterior confirma persistencia.
- [ ] Error visible y controlado.
- [ ] Tres enlaces entregados y accesibles al ingeniero.

Después de crear la orden, se puede ejecutar desde API:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\verificar-entrega.ps1 -UrlPublica 'https://DOMINIO_REAL/api/v1/' -OrdenId ID_REAL
```

Ese script guarda una consulta HTTP de la orden en target/evidencia-entrega.json. No certifica por sí solo que el POST provino del teléfono ni sustituye la consulta directa SQL. Completar el registro con las capturas/resultados reales.

Al terminar, cerrar el túnel. El cierre del directorio padre se limita a procesos identificados del proyecto; no elimina los puentes ADB ni los daemons de otros proyectos.

## Encontrar el token: está en tu cuenta, no en una carpeta

Abre https://dashboard.ngrok.com/get-started/your-authtoken e inicia sesión. Copia el authtoken de TU cuenta. `TU_TOKEN` y `SU_TOKEN_PRIVADO` son marcadores, no valores utilizables. El mensaje «saved» confirma que se guardó texto, no que ngrok lo haya autenticado.

En PowerShell puedes ejecutar:

```powershell
$tokenNgrok = Read-Host 'Pega aquí el authtoken REAL copiado del panel de ngrok'
ngrok config add-authtoken $tokenNgrok
Remove-Variable tokenNgrok
ngrok config check
```

No compartas ese token ni capturas que lo muestren. `ngrok.yml` es un archivo, así que no se abre con cd. La instalación de Microsoft Store administra su configuración; no necesitas localizar ni editar el archivo para configurar tu cuenta. El túnel real se prueba ejecutando el lanzador después de confirmar la salud de la API.
