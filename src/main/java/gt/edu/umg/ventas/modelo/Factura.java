package gt.edu.umg.ventas.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad principal de Factura que reúne el cliente opcional,
 * el usuario que la registra, sus líneas de detalle y los pagos asociados.
 */
public class Factura {

    private long idFactura;
    private String numero;
    private LocalDateTime fechaHora;
    private EstadoFactura estado;
    private String observaciones;
    private Cliente cliente; // Opcional (0..1)
    private Usuario usuario; // Obligatorio (1)
    private OrdenVenta orden;
    private final List<DetalleFactura> detalles;
    private final List<Pago> pagos;

    public Factura() {
        this.fechaHora = LocalDateTime.now();
        this.estado = EstadoFactura.BORRADOR;
        this.detalles = new ArrayList<>();
        this.pagos = new ArrayList<>();
    }

    public Factura(long idFactura, String numero, LocalDateTime fechaHora, EstadoFactura estado, 
                   String observaciones, Cliente cliente, Usuario usuario) {
        this.idFactura = idFactura;
        this.numero = numero;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
        this.estado = estado != null ? estado : EstadoFactura.BORRADOR;
        this.observaciones = observaciones;
        this.cliente = cliente;
        this.usuario = usuario;
        this.detalles = new ArrayList<>();
        this.pagos = new ArrayList<>();
    }

    /**
     * Agrega un nuevo detalle a la factura a partir de un producto y cantidad.
     *
     * @param producto Producto a facturar
     * @param cantidad Cantidad a adquirir
     * @throws IllegalArgumentException si el producto es nulo o la cantidad es menor o igual a cero
     * @throws IllegalStateException si la factura no está en estado BORRADOR
     */
    public void agregarDetalle(Producto producto, BigDecimal cantidad) {
        if (this.estado != EstadoFactura.BORRADOR) {
            throw new IllegalStateException("Solo se pueden agregar detalles a facturas en estado BORRADOR.");
        }
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        DetalleFactura detalle = new DetalleFactura();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecioVenta());
        detalle.setPorcentajeImpuesto(new BigDecimal("12.00")); // 12% IVA por defecto
        detalle.setDescuento(BigDecimal.ZERO);

        this.detalles.add(detalle);
    }

    /**
     * Elimina un detalle de la lista de detalles de la factura.
     *
     * @param detalle Detalle a eliminar
     * @throws IllegalStateException si la factura no está en estado BORRADOR
     */
    public void eliminarDetalle(DetalleFactura detalle) {
        if (this.estado != EstadoFactura.BORRADOR) {
            throw new IllegalStateException("Solo se pueden eliminar detalles de facturas en estado BORRADOR.");
        }
        this.detalles.remove(detalle);
    }

    /**
     * Calcula el subtotal sumando los subtotales de cada detalle.
     *
     * @return Subtotal monetario en BigDecimal (escala 2)
     */
    public BigDecimal calcularSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleFactura d : detalles) {
            if (d != null) {
                subtotal = subtotal.add(d.calcularSubtotal());
            }
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el impuesto (IVA) sumando el impuesto generado por cada detalle.
     * Impuesto = subtotalDetalle * (porcentajeImpuesto / 100)
     *
     * @return Monto de impuesto en BigDecimal (escala 2)
     */
    public BigDecimal calcularImpuesto() {
        BigDecimal impuestoTotal = BigDecimal.ZERO;
        BigDecimal cien = new BigDecimal("100.00");
        for (DetalleFactura d : detalles) {
            if (d != null) {
                BigDecimal sub = d.calcularSubtotal();
                BigDecimal pct = d.getPorcentajeImpuesto() != null ? d.getPorcentajeImpuesto() : new BigDecimal("12.00");
                BigDecimal impuestoDetalle = sub.multiply(pct).divide(cien, 4, RoundingMode.HALF_UP);
                impuestoTotal = impuestoTotal.add(impuestoDetalle);
            }
        }
        return impuestoTotal.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el total a pagar: subtotal + impuesto.
     *
     * @return Total monetario en BigDecimal (escala 2)
     */
    public BigDecimal calcularTotal() {
        return calcularSubtotal().add(calcularImpuesto()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Registra un pago y actualiza el estado a PAGADA si el monto total pagado
     * cubre o supera el total de la factura.
     *
     * @param pago Pago a registrar
     * @throws IllegalArgumentException si el pago es nulo o su monto es menor o igual a cero
     * @throws IllegalStateException si la factura está ANULADA
     */
    public void registrarPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }
        if (pago.getMonto() == null || pago.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero.");
        }
        if (this.estado == EstadoFactura.ANULADA) {
            throw new IllegalStateException("No se pueden registrar pagos a una factura ANULADA.");
        }

        this.pagos.add(pago);

        // Si la factura está EMITIDA y el total acumulado cubre la factura, pasa a PAGADA
        BigDecimal totalPagado = obtenerTotalPagado();
        if (totalPagado.compareTo(calcularTotal()) >= 0 && this.estado == EstadoFactura.EMITIDA) {
            this.estado = EstadoFactura.PAGADA;
        }
    }

    /**
     * Obtiene el acumulado total de los pagos registrados.
     *
     * @return Total pagado en BigDecimal
     */
    public BigDecimal obtenerTotalPagado() {
        BigDecimal acumulado = BigDecimal.ZERO;
        for (Pago p : pagos) {
            if (p != null && p.getMonto() != null) {
                acumulado = acumulado.add(p.getMonto());
            }
        }
        return acumulado.setScale(2, RoundingMode.HALF_UP);
    }

    // --- GETTERS Y SETTERS ---

    public long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(long idFactura) {
        this.idFactura = idFactura;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetalleFactura> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public void agregarDetalleDirecto(DetalleFactura detalle) {
        if (detalle != null) {
            this.detalles.add(detalle);
        }
    }

    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    public void agregarPagoDirecto(Pago pago) {
        if (pago != null) {
            this.pagos.add(pago);
        }
    }

    public OrdenVenta getOrden() {
        return orden;
    }

    public void setOrden(OrdenVenta orden) {
        this.orden = orden;
    }
}
