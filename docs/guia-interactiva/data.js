/* Contenido educativo. Todos los identificadores y mensajes del ejercicio son ficticios. */
window.GUIDE_DATA = {
  steps: [
    {
      label: "Prepara la orden",
      title: "La venta comienza en el teléfono",
      text: "Elige un cliente, un producto y una cantidad. Esta simulación usa datos ficticios y no envía nada a nuestra base real.",
      active: "android",
      paths: [],
      codeTitle: "Datos de ejemplo del ejercicio",
      code: "Cliente: Ana López\nProducto: Mouse inalámbrico\nCantidad: 2\nPrecio de referencia: Q95.00 por unidad"
    },
    {
      label: "Android prepara",
      title: "La pantalla se convierte en un mensaje",
      text: "Al guardar, Android reúne los datos de la orden. Retrofit y Gson los convierten a JSON: viajan los datos, mientras la aplicación permanece en el teléfono.",
      active: "android",
      paths: [],
      codeTitle: "Cuerpo JSON de ejemplo",
      code: '{\n  "clienteId": 1,\n  "observaciones": "Ejercicio educativo",\n  "detalles": [\n    { "productoId": 1, "cantidad": 2 }\n  ]\n}'
    },
    {
      label: "URL pública",
      title: "El mensaje llega a ngrok por Internet",
      text: "Android hace una petición POST a la dirección HTTPS pública. El servicio de ngrok recibe el mensaje y lo dirige hacia el túnel de nuestra computadora.",
      active: "cloud",
      paths: [{ id: "phone-cloud", reverse: false }],
      codeTitle: "Dirección ilustrativa y operación",
      code: "POST https://tu-tunel.ngrok.example/api/v1/ordenes-venta\nContent-Type: application/json\n\nEl dominio es ilustrativo; la demostración utiliza la URL real que entrega ngrok."
    },
    {
      label: "Agente local",
      title: "El túnel entrega la petición a la computadora",
      text: "El agente ngrok mantiene la conexión con el servicio público y reenvía el mensaje a la API en localhost:8080. Spring Boot sigue ejecutándose en nuestra computadora.",
      active: "agent",
      paths: [
        { id: "cloud-agent", reverse: false },
        { id: "agent-api", reverse: false }
      ],
      codeTitle: "Destino local del agente",
      code: "ngrok http 8080\n\nDestino de esta petición:\nhttp://localhost:8080/api/v1/ordenes-venta\n\nAquí localhost significa esta computadora."
    },
    {
      label: "API valida",
      title: "Las reglas de venta viven en la API",
      text: "La API comprueba cliente, productos y cantidades. Consulta los precios oficiales y calcula subtotal, IVA y total. Si los datos son inválidos, devuelve un error y no guarda la orden.",
      active: "api",
      paths: [],
      codeTitle: "Capas que procesan la orden",
      code: "OrdenesController\n  → OrdenesApiService\n  → OrdenVentaService\n  → OrdenVentaDAOImpl\n\nEjemplo: Q190.00 + Q22.80 de IVA = Q212.80"
    },
    {
      label: "Base de datos",
      title: "La orden queda guardada en SQL Server",
      text: "El DAO usa JDBC para guardar la cabecera y sus productos en una transacción. Se confirma todo junto o se revierte. La información permanente queda en SQL Server.",
      active: "sql",
      paths: [{ id: "api-sql", reverse: false }],
      codeTitle: "Guardado simplificado; no se ejecuta SQL real",
      code: "INICIAR TRANSACCIÓN\n  Guardar cabecera en OrdenVenta\n  Guardar líneas en DetalleOrdenVenta\nCONFIRMAR si todo fue correcto\nREVERTIR si alguna parte falló\n\nCrear la orden no descuenta inventario."
    },
    {
      label: "API responde",
      title: "El servidor confirma la creación",
      text: "Con el guardado confirmado, la API prepara una respuesta HTTP 201 Created. El JSON identifica la orden y devuelve el total calculado por el servidor.",
      active: "api",
      paths: [{ id: "api-sql", reverse: true }],
      codeTitle: "Respuesta simplificada con datos ficticios",
      code: 'HTTP/1.1 201 Created\nContent-Type: application/json\n\n{\n  "id": 123,\n  "numeroOrden": "OV-EJEMPLO",\n  "estado": "PENDIENTE",\n  "total": "212.80"\n}'
    },
    {
      label: "Mensaje de vuelta",
      title: "La respuesta regresa por el mismo puente",
      text: "El resultado sale de la API, pasa por el agente local y el servicio público de ngrok, y llega al teléfono. ngrok transporta también la respuesta.",
      active: "android",
      paths: [
        { id: "agent-api", reverse: true },
        { id: "cloud-agent", reverse: true },
        { id: "phone-cloud", reverse: true }
      ],
      codeTitle: "Recorrido de regreso",
      code: "API en la computadora\n  → agente ngrok local\n  → servicio público de ngrok\n  → Retrofit en Android\n\nRespuesta: HTTP 201 + JSON de la orden"
    },
    {
      label: "Resultado visible",
      title: "Android muestra la orden creada",
      text: "Android interpreta el JSON y actualiza su interfaz nativa. La orden sigue en la base aunque cierres la pantalla. El escritorio podrá verla cuando consulte o refresque los datos.",
      active: "android",
      paths: [],
      codeTitle: "Qué significa el resultado",
      code: "Mensaje al usuario: Orden creada\nNúmero ilustrativo: OV-EJEMPLO\nEstado: PENDIENTE\n\nInterfaz: Java + XML en el teléfono\nDatos permanentes: SQL Server en la computadora"
    }
  ],
  components: {
    android: {
      label: "Aplicación Android",
      where: "En el teléfono",
      role: "Permite elegir cliente y productos, preparar la orden y mostrar el resultado. Su interfaz es nativa: Java y XML.",
      input: "Lo que escribe o selecciona el usuario, y las respuestas de la API.",
      output: "Peticiones HTTP con JSON hacia la URL pública configurada.",
      layers: [
        { name: "Activity + XML", description: "Dibujan la pantalla y reciben las acciones del usuario." },
        { name: "ViewModel", description: "Conserva el borrador y construye OrdenRequest con los datos de la venta." },
        { name: "Repository", description: "Coordina la consulta o el envío y comunica éxito o error." },
        { name: "Retrofit + Gson", description: "Realizan la petición HTTP y convierten objetos Java y JSON." }
      ]
    },
    cloud: {
      label: "Servicio público de ngrok",
      where: "En Internet",
      role: "Ofrece una dirección HTTPS pública y conecta las peticiones con el agente local. No ejecuta nuestras reglas de venta.",
      input: "La petición enviada por Android a la URL pública.",
      output: "La petición por el túnel hacia la computadora y la respuesta de vuelta al teléfono.",
      layers: [
        { name: "URL pública HTTPS", description: "Es la dirección configurada en Android para alcanzar la API local durante la demostración." },
        { name: "Túnel", description: "Enlaza el servicio público con el agente que ejecutamos en la computadora." }
      ]
    },
    agent: {
      label: "Agente ngrok",
      where: "En nuestra computadora",
      role: "Mantiene una conexión hacia el servicio público y reenvía las peticiones al puerto 8080 de la misma computadora.",
      input: "Los mensajes que llegan por el túnel desde ngrok público.",
      output: "Peticiones hacia Spring Boot y sus respuestas de regreso al túnel.",
      layers: [
        { name: "Proceso local", description: "Se inicia con ngrok http 8080 y debe permanecer activo durante la demostración." },
        { name: "Destino localhost:8080", description: "Apunta a la API local. localhost siempre se refiere al dispositivo que utiliza esa dirección." }
      ]
    },
    api: {
      label: "API REST",
      where: "En nuestra computadora, con Spring Boot en el puerto 8080",
      role: "Recibe y valida los datos, determina los importes y coordina el guardado de la orden. Devuelve un resultado que Android puede interpretar.",
      input: "JSON con clienteId, observaciones y líneas de producto/cantidad.",
      output: "Instrucciones a la base mediante JDBC y respuestas HTTP/JSON al teléfono.",
      layers: [
        { name: "OrdenesController", description: "Recibe POST /api/v1/ordenes-venta y valida el formato del cuerpo." },
        { name: "OrdenesApiService", description: "Obtiene cliente, productos, precios y operador; prepara la respuesta." },
        { name: "OrdenVentaService", description: "Aplica las reglas de la orden y sus cálculos." },
        { name: "OrdenVentaDAOImpl", description: "Guarda cabecera y detalles con JDBC, confirmando o revirtiendo la transacción." }
      ]
    },
    sql: {
      label: "SQL Server",
      where: "En nuestra computadora; base SistemaVentas",
      role: "Conserva los registros del sistema. La orden guardada permanece disponible aunque cierres las interfaces o ngrok.",
      input: "Instrucciones SQL de los DAO de la API o del escritorio.",
      output: "Registros consultados y confirmación o error de las operaciones.",
      layers: [
        { name: "OrdenVenta", description: "Guarda la cabecera: número, fecha, cliente, estado y total." },
        { name: "DetalleOrdenVenta", description: "Guarda los productos, cantidades y precios de cada orden." },
        { name: "Transacción", description: "Hace que cabecera y líneas se guarden juntas, evitando una venta incompleta." }
      ]
    },
    desktop: {
      label: "Aplicación de escritorio",
      where: "En nuestra computadora, con Java Swing",
      role: "Administra el sistema y consulta la misma base directamente. Al cargar o refrescar una consulta puede ver la orden creada desde Android.",
      input: "Acciones del operador y datos consultados en SQL Server.",
      output: "Consultas y operaciones hacia SQL Server mediante JDBC, sin pasar por ngrok.",
      layers: [
        { name: "Vista Swing", description: "Muestra formularios de clientes, inventario, órdenes, despachos y facturas." },
        { name: "Controlador y servicio", description: "Coordinan las acciones y aplican las reglas del sistema." },
        { name: "DAO + JDBC", description: "Consultan y escriben directamente en la base compartida." }
      ]
    }
  },
  quiz: [
    {
      question: "¿Dónde queda guardada permanentemente la orden?",
      options: ["En la pantalla del teléfono", "En SQL Server, dentro de la computadora", "En la dirección pública de ngrok"],
      correct: 1,
      explanation: "SQL Server conserva la cabecera y las líneas. Android muestra el resultado y ngrok transporta los mensajes."
    },
    {
      question: "¿Qué parte valida los productos y determina los precios oficiales?",
      options: ["La API REST", "El túnel ngrok", "El navegador de Internet"],
      correct: 0,
      explanation: "La API aplica las reglas de venta y consulta los precios del catálogo. Android puede mostrar un cálculo previo, pero el servidor decide los importes finales."
    },
    {
      question: "¿Para qué usamos ngrok en esta demostración?",
      options: ["Para mover el código Android a Internet", "Para sustituir la base de datos", "Para conectar una URL pública con la API que sigue en nuestra computadora"],
      correct: 2,
      explanation: "ngrok enlaza su servicio público con un agente local. Spring Boot sigue ejecutándose en la computadora y recibe allí la petición."
    }
  ]
};
