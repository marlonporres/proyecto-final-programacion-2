# Validación de la guía web

30 de octubre de 2026. Sitio estático revisado en Chromium mediante navegador automatizado y capturas inspeccionadas.

- 19 grupos funcionales aprobados: navegación, teclado, precios y totales, guardado simulado, consulta explícita de escritorio, mensajes, componentes/capas, tres fallos, cantidades, reproducción/pausa/anterior/siguiente/reinicio, cuestionario, enlaces, menú móvil y movimiento reducido.
- Cero errores JavaScript y cero solicitudes a la API o base comercial.
- Sin desbordamiento horizontal a 1440, 768, 390 y 320 px.
- Revisión visual final a 1440, 390 y 320 px: etiquetas de conexiones sin solapamientos y controles del laboratorio de al menos 44 px.
- El caso normal llega al paso 8. ngrok detenido se interrumpe en 2; SQL caído en 5; cantidad inválida en 1. Los fallos no muestran una orden guardada.
- Datos ficticios coherentes: dos mouse de Q95.00 producen subtotal Q190.00, IVA Q22.80 y total Q212.80. Otros productos y cantidades recalculan los importes.
- La consulta de escritorio recupera el mismo registro ficticio solo después del guardado y de pulsar Consultar.

## Cómo repetir la prueba manual

1. Abrir index.html; elegir cliente/producto/cantidad y recorrer las ocho etapas usando Siguiente o reproducción.
2. Abrir Ver el mensaje y comprobar que cambian JSON, ruta y respuesta 201.
3. Consultar la base desde el escritorio simulado y comparar datos.
4. Elegir cada fallo y comprobar que se detiene sin confirmar guardado.
5. Corregir la cantidad y repetir; explorar capas de Android/API/escritorio.
6. Contestar las tres preguntas, revisar explicaciones y reiniciar el repaso.
7. Reducir la ventana, usar menú/teclado y activar movimiento reducido del sistema.

## Alcance

Se emularon tamaños móviles en navegador; no se verificó un teléfono físico. La guía explica el sistema y no sustituye su demostración Android → ngrok → API → SQL real. Las capturas y JSON adjuntos corresponden al sitio educativo local; no acreditan datos comerciales ni uso real del proyecto Java.
