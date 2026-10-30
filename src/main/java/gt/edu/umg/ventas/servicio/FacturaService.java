package gt.edu.umg.ventas.servicio;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** Documento comercial posterior al despacho; nunca modifica existencias. */
public class FacturaService {
    private final FacturaDAO facturaDAO;
    private final OrdenVentaDAO ordenDAO;
    private final Map<Long, Factura> activas = new HashMap<>();
    private final AtomicLong borradores = new AtomicLong();

    public FacturaService() { this(new FacturaDAOImpl(), new OrdenVentaDAOImpl()); }
    public FacturaService(FacturaDAO facturaDAO, OrdenVentaDAO ordenDAO) {
        this.facturaDAO = Objects.requireNonNull(facturaDAO);
        this.ordenDAO = Objects.requireNonNull(ordenDAO);
    }

    public List<OrdenVenta> obtenerOrdenesFacturables() {
        return ordenDAO.obtenerTodos().stream()
                .filter(o -> o.getEstado() == EstadoOrdenVenta.COMPLETADA)
                .filter(o -> ordenDAO.tieneDespachoConfirmado(o.getId()))
                .filter(o -> facturaDAO.buscarPorOrden(o.getId()) == null).toList();
    }

    public Factura crearDesdeOrden(int idOrden, Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() <= 0 || !usuario.isActivo()) {
            throw new IllegalArgumentException("Se requiere un usuario activo.");
        }
        OrdenVenta orden = validarOrden(idOrden);
        Factura factura = copiarOrden(orden, usuario);
        factura.setIdFactura(-borradores.incrementAndGet());
        factura.setNumero("FAC-" + UUID.randomUUID());
        activas.put(factura.getIdFactura(), factura);
        return factura;
    }

    private OrdenVenta validarOrden(int idOrden) {
        OrdenVenta o = ordenDAO.obtener(idOrden);
        if (o == null || o.getEstado() != EstadoOrdenVenta.COMPLETADA
                || !ordenDAO.tieneDespachoConfirmado(idOrden)) {
            throw new IllegalStateException("La factura requiere una orden con despacho confirmado.");
        }
        if (facturaDAO.buscarPorOrden(idOrden) != null) {
            throw new IllegalStateException("La orden ya tiene una factura, incluso si fue anulada.");
        }
        if (o.getCliente() == null || o.getDetalles().isEmpty()) {
            throw new IllegalStateException("La orden no contiene cliente o detalles válidos.");
        }
        return o;
    }

    private Factura copiarOrden(OrdenVenta orden, Usuario usuario) {
        Factura f = new Factura();
        f.setOrden(orden);
        f.setCliente(orden.getCliente());
        f.setUsuario(usuario);
        f.setObservaciones("Facturación de orden " + orden.getNumeroOrden());
        for (DetalleOrdenVenta d : orden.getDetalles()) {
            f.agregarDetalleDirecto(new DetalleFactura(0, d.getProducto(), BigDecimal.valueOf(d.getCantidad()),
                    d.getPrecioUnitario(), new BigDecimal("12.00"), BigDecimal.ZERO));
        }
        return f;
    }

    public Factura emitir(long idFactura) {
        Factura borrador = obtenerFactura(idFactura);
        if (borrador.getEstado() != EstadoFactura.BORRADOR) {
            throw new IllegalStateException("Solo se pueden emitir facturas en borrador.");
        }
        if (borrador.getOrden() == null) throw new IllegalStateException("La factura requiere una orden.");
        Factura f = copiarOrden(validarOrden(borrador.getOrden().getId()), borrador.getUsuario());
        f.setNumero(borrador.getNumero());
        f.setEstado(EstadoFactura.EMITIDA);
        facturaDAO.guardar(f);
        activas.remove(idFactura);
        activas.put(f.getIdFactura(), f);
        return f;
    }

    public Factura registrarPago(long idFactura, Pago pago) {
        Factura actual = obtenerFactura(idFactura);
        if (actual.getEstado() != EstadoFactura.EMITIDA) {
            throw new IllegalStateException("Solo se pueden registrar pagos en facturas emitidas.");
        }
        Factura copia = copiarFactura(actual);
        copia.registrarPago(pago);
        facturaDAO.actualizar(copia);
        activas.put(idFactura, copia);
        return copia;
    }

    public Factura anular(long idFactura) {
        Factura actual = obtenerFactura(idFactura);
        if (actual.getEstado() != EstadoFactura.EMITIDA && actual.getEstado() != EstadoFactura.PAGADA) {
            throw new IllegalStateException("Solo se pueden anular facturas emitidas o pagadas.");
        }
        facturaDAO.anular(idFactura);
        Factura copia = copiarFactura(actual);
        copia.setEstado(EstadoFactura.ANULADA);
        activas.put(idFactura, copia);
        return copia;
    }

    private Factura copiarFactura(Factura actual) {
        Factura f = new Factura(actual.getIdFactura(), actual.getNumero(), actual.getFechaHora(),
                actual.getEstado(), actual.getObservaciones(), actual.getCliente(), actual.getUsuario());
        f.setOrden(actual.getOrden());
        actual.getDetalles().forEach(f::agregarDetalleDirecto);
        actual.getPagos().forEach(f::agregarPagoDirecto);
        return f;
    }

    public Factura consultarPorNumero(String numero) {
        if (numero == null || numero.isBlank()) throw new IllegalArgumentException("Ingrese el número de factura.");
        Factura f = facturaDAO.buscarPorNumero(numero.trim());
        if (f == null) throw new IllegalArgumentException("Factura no encontrada.");
        activas.put(f.getIdFactura(), f);
        return f;
    }

    private Factura obtenerFactura(long id) {
        Factura f = activas.get(id);
        if (f == null) throw new IllegalArgumentException("Consulte o cargue primero la factura.");
        return f;
    }
}


