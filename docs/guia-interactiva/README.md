## Página publicada

https://ventas-umg-por-dentro.marlonporres75.chatgpt.site

El enlace de Sites conserva acceso privado del propietario. Esta carpeta permite abrir la misma guía sin cuenta: index.html o abrir-guia.cmd.

# Ventas por dentro — guía interactiva

Sitio educativo estático del proyecto de Programación II. La simulación utiliza datos ficticios en memoria; no contiene credenciales, no llama a la API comercial y no guarda ventas reales.

## Abrir

Abrir `index.html` en un navegador moderno. Funciona sin instalación ni compilación y sin conexión a servicios externos. Para un servidor local, desde esta carpeta ejecutar `python -m http.server 4177  --bind 127.0.0.1` y abrir `http://127.0.0.1:4177/`.

Para alojarlo, publicar el contenido de esta carpeta en un alojamiento estático. La copia del repositorio de escritorio contiene esos archivos directamente dentro de `docs/guia-interactiva/`.

## Interacciones

- Cliente, producto y cantidad con subtotal, IVA y total consistentes.
- Ocho etapas después del inicio, reproducción, pausa, anterior, siguiente y reinicio.
- Escenarios ngrok detenido, SQL no disponible y cantidad inválida.
- Componentes y capas de Android, API y escritorio.
- Consulta manual del mismo registro desde escritorio.
- Tres preguntas con explicación y repaso.

## Límites

La animación simplifica transporte y tiempos para enseñar el concepto. Los dominios, identificadores, clientes y órdenes mostrados son ficticios. Retroceder o reiniciar modifica solo el ejercicio en memoria. Esta web no sustituye las aplicaciones Java/Android ni demuestra una ejecución real a través de ngrok.

Las responsabilidades se contrastaron con los DTO, controladores, servicios y DAO del proyecto. Para el túnel: [documentación oficial de ngrok](https://github.com/ngrok/ngrok-docs/blob/main/share-localhost/tunnels.mdx).

## Validación

El registro final de comprobaciones se entrega junto a la copia del proyecto. Las pruebas recorren la simulación, errores, importes, capas, consulta de escritorio, cuestionario, teclado, navegación móvil y anchos de 320 a 1440 px. La comprobación en dispositivo físico se distingue de la emulación de navegador.

## Capturas y registro

Ver [VALIDACION.md](VALIDACION.md) y la carpeta capturas/. La URL de Sites, cuando se publique, conserva acceso privado del propietario; para compartir el código y abrirlo sin cuenta, basta esta carpeta estática.
