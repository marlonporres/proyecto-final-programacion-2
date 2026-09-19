package gt.edu.umg.facturacion.servicio;

import gt.edu.umg.facturacion.dao.FacturaDAO;
import gt.edu.umg.facturacion.dao.FacturaDAOImpl;
import gt.edu.umg.facturacion.dao.ProductoDAO;
import gt.edu.umg.facturacion.dao.ProductoDAOImpl;
import gt.edu.umg.facturacion.modelo.Cliente;
import gt.edu.umg.facturacion.modelo.DetalleFactura;
import gt.edu.umg.facturacion.modelo.EstadoFactura;
import gt.edu.umg.facturacion.modelo.Factura;
import gt.edu.umg.facturacion.modelo.Pago;
import gt.edu.umg.facturacion.modelo.Producto;
import gt.edu.umg.facturacion.modelo.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Capa de servicio encargada de las reglas de negocio y orquestación
 * del ciclo de vida de una factura (creación, adición de productos,
 * emisión con descuento de inventario, pagos y anulación).
 */
public class FacturaService {

    private final FacturaDAO facturaDAO;
    private final ProductoDAO productoDAO;

    // Almacenamiento en memoria para el ciclo de trabajo de facturas activas
    private final Map<Long, Factura> facturasActivas = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(System.currentTimeMillis() % 1000000);

    public FacturaService() {
        this.facturaDAO = new FacturaDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
    }

    public FacturaService(FacturaDAO facturaDAO, ProductoDAO productoDAO) {
        this.facturaDAO = facturaDAO != null ? facturaDAO : new FacturaDAOImpl();
        this.productoDAO = productoDAO;
    }

    /**
     * Crea una nueva factura en estado BORRADOR con un número correlativo único.
     *
     * @param cliente Cliente opcional (puede ser null para consumidor final genérico)
     * @param usuario Usuario obligatorio que registra la transacción
     * @return Factura instanciada en estado BORRADOR
     * @throws IllegalArgumentException si el usuario es nulo
     */
    public Factura crear(Cliente cliente, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Se requiere un usuario válido para registrar la factura.");
        }

        long id = idGenerator.incrementAndGet();
        String numero = "FAC-" + id;

        Factura factura = new Factura(id, numero, LocalDateTime.now(), EstadoFactura.BORRADOR, "", cliente, usuario);
        facturasActivas.put(id, factura);
        return factura;
    }

    /**
     * Agrega un producto a la factura especificada previa validación de existencia e inventario.
     *
     * @param idFactura ID de la factura activa
     * @param producto Producto a agregar
     * @param cantidad Cantidad solicitada (debe ser > 0)
     * @return Factura actualizada
     */
    public Factura agregarProducto(long idFactura, Producto producto, BigDecimal cantidad) {
        Factura factura = obtenerFactura(idFactura);

        if (factura.getEstado() != EstadoFactura.BORRADOR) {
            throw new IllegalStateException("Solo se pueden agregar productos a una factura en estado BORRADOR.");
        }
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (!producto.isActivo()) {
            throw new IllegalStateException("No se puede agregar el producto '" + producto.getNombre() 
                    + "' porque está inactivo en el sistema.");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser estrictamente mayor a cero.");
        }
        if (producto.getInventario() == null || !producto.getInventario().hayDisponibilidad(cantidad)) {
            BigDecimal disp = producto.getInventario() != null ? producto.getInventario().getExistencia() : BigDecimal.ZERO;
            throw new IllegalStateException("Inventario insuficiente para el producto '" + producto.getNombre() 
                    + "'. Disponible: " + disp + ", Solicitado: " + cantidad);
        }

        factura.agregarDetalle(producto, cantidad);
        return factura;
    }

    /**
     * Emite una factura: valida detalles, valida inventario, descuenta stock,
     * cambia el estado a EMITIDA y persiste la información en la base de datos.
     *
     * @param idFactura ID de la factura
     * @return Factura emitida
     */
    public Factura emitir(long idFactura) {
        Factura factura = obtenerFactura(idFactura);

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalStateException("No se puede emitir una factura ANULADA.");
        }
        if (factura.getEstado() != EstadoFactura.BORRADOR) {
            throw new IllegalStateException("La factura ya fue emitida previamente (Estado: " + factura.getEstado() + ").");
        }
        if (factura.getDetalles().isEmpty()) {
            throw new IllegalStateException("No se puede emitir una factura sin líneas de detalle.");
        }

        // 1. Validar disponibilidad de inventario para todas las líneas
        for (DetalleFactura det : factura.getDetalles()) {
            Producto prod = det.getProducto();
            if (prod == null || prod.getInventario() == null || !prod.getInventario().hayDisponibilidad(det.getCantidad())) {
                BigDecimal disp = (prod != null && prod.getInventario() != null) ? prod.getInventario().getExistencia() : BigDecimal.ZERO;
                String nom = (prod != null) ? prod.getNombre() : "Desconocido";
                throw new IllegalStateException("Stock insuficiente para '" + nom + "'. Disponible: " + disp + ", Requerido: " + det.getCantidad());
            }
        }

        // 2. Descontar inventario de cada producto
        for (DetalleFactura det : factura.getDetalles()) {
            Producto prod = det.getProducto();
            prod.getInventario().descontar(det.getCantidad());
            if (productoDAO != null && prod.getInventario().getIdInventario() > 0) {
                try {
                    productoDAO.actualizarInventario(prod.getInventario().getIdInventario(), prod.getInventario().getExistencia());
                } catch (Exception e) {
                    System.err.println("Advertencia al actualizar inventario en BD: " + e.getMessage());
                }
            }
        }

        // 3. Cambiar estado a EMITIDA
        factura.setEstado(EstadoFactura.EMITIDA);

        // 4. Persistir mediante DAO si está disponible
        if (facturaDAO != null) {
            try {
                facturaDAO.guardar(factura);
            } catch (Exception e) {
                System.err.println("Advertencia al persistir factura mediante DAO: " + e.getMessage());
            }
        }

        return factura;
    }

    /**
     * Registra un pago para una factura emitida. Si la suma de pagos cubre o supera
     * el total de la factura, el estado se actualiza a PAGADA.
     *
     * @param idFactura ID de la factura
     * @param pago Pago a registrar
     * @return Factura actualizada
     */
    public Factura registrarPago(long idFactura, Pago pago) {
        Factura factura = obtenerFactura(idFactura);

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalStateException("No se pueden registrar pagos en una factura ANULADA.");
        }
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }
        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero.");
        }

        factura.registrarPago(pago);

        // Actualizar en base de datos si ya cuenta con ID persistido
        if (facturaDAO != null && factura.getIdFactura() > 0) {
            try {
                facturaDAO.actualizar(factura);
            } catch (Exception e) {
                System.err.println("Advertencia al actualizar pago en DAO: " + e.getMessage());
            }
        }

        return factura;
    }

    /**
     * Anula una factura y repone el inventario de los productos facturados si ya estaba emitida o pagada.
     *
     * @param idFactura ID de la factura a anular
     * @return Factura anulada
     */
    public Factura anular(long idFactura) {
        Factura factura = obtenerFactura(idFactura);

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalStateException("La factura ya se encuentra ANULADA.");
        }

        // Si fue EMITIDA o PAGADA, se debe devolver el inventario
        if (factura.getEstado() == EstadoFactura.EMITIDA || factura.getEstado() == EstadoFactura.PAGADA) {
            for (DetalleFactura det : factura.getDetalles()) {
                Producto prod = det.getProducto();
                if (prod != null && prod.getInventario() != null) {
                    prod.getInventario().reponer(det.getCantidad());
                    if (productoDAO != null && prod.getInventario().getIdInventario() > 0) {
                        try {
                            productoDAO.actualizarInventario(prod.getInventario().getIdInventario(), prod.getInventario().getExistencia());
                        } catch (Exception e) {
                            System.err.println("Advertencia al reponer inventario en BD: " + e.getMessage());
                        }
                    }
                }
            }
        }

        factura.setEstado(EstadoFactura.ANULADA);

        if (facturaDAO != null && factura.getIdFactura() > 0) {
            try {
                facturaDAO.anular(factura.getIdFactura());
            } catch (Exception e) {
                System.err.println("Advertencia al anular factura en DAO: " + e.getMessage());
            }
        }

        return factura;
    }

    /**
     * Consulta una factura por su número correlativo.
     *
     * @param numero Número de factura (ej. FAC-1001)
     * @return Factura encontrada o null
     */
    public Factura consultarPorNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            return null;
        }
        for (Factura f : facturasActivas.values()) {
            if (f.getNumero() != null && f.getNumero().equalsIgnoreCase(numero.trim())) {
                return f;
            }
        }
        if (facturaDAO != null) {
            try {
                Factura f = facturaDAO.buscarPorNumero(numero.trim());
                if (f != null) {
                    facturasActivas.put(f.getIdFactura(), f);
                    return f;
                }
            } catch (Exception e) {
                System.err.println("Advertencia al buscar factura en BD: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Registra o almacena una factura en el mapa activo de memoria.
     *
     * @param factura Factura a almacenar
     */
    public void registrarFacturaActiva(Factura factura) {
        if (factura != null) {
            facturasActivas.put(factura.getIdFactura(), factura);
        }
    }

    private Factura obtenerFactura(long idFactura) {
        Factura f = facturasActivas.get(idFactura);
        if (f == null) {
            throw new IllegalArgumentException("No se encontró la factura con identificador: " + idFactura);
        }
        return f;
    }
}
