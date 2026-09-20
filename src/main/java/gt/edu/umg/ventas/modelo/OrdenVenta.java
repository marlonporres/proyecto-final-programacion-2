package gt.edu.umg.ventas.modelo;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenVenta {
    private Integer id;
    private String numeroOrden;
    private LocalDateTime fecha;
    private Cliente cliente;
    private Usuario usuario;
    private EstadoOrdenVenta estado;
    private String observaciones;
    private BigDecimal total = BigDecimal.ZERO;
    private List<DetalleOrdenVenta> detalles = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public EstadoOrdenVenta getEstado() { return estado; }
    public void setEstado(EstadoOrdenVenta estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public List<DetalleOrdenVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleOrdenVenta> detalles) { this.detalles = detalles; calcularTotal(); }

    public void agregarDetalle(DetalleOrdenVenta detalle) {
        this.detalles.add(detalle);
        calcularTotal();
    }
    public void eliminarDetalle(DetalleOrdenVenta detalle) {
        this.detalles.remove(detalle);
        calcularTotal();
    }
    public BigDecimal calcularSubtotal() {
        return detalles.stream().map(DetalleOrdenVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal calcularImpuesto() {
        // Ejemplo: 12% IVA
        return calcularSubtotal().multiply(new BigDecimal("0.12"));
    }
    public void calcularTotal() {
        this.total = calcularSubtotal(); // Puede sumar impuesto si aplica
    }
    public void confirmar() { this.estado = EstadoOrdenVenta.COMPLETADA; }
    public void anular() { this.estado = EstadoOrdenVenta.CANCELADA; }
}
