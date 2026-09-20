package gt.edu.umg.ventas.modelo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdenVentaResumen {
    private Integer id;
    private String numeroOrden;
    private LocalDateTime fecha;
    private String nombreCliente;
    private EstadoOrdenVenta estado;
    private BigDecimal total;
    private String estadoDespacho;
    private String nombreBodega;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public EstadoOrdenVenta getEstado() { return estado; }
    public void setEstado(EstadoOrdenVenta estado) { this.estado = estado; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getEstadoDespacho() { return estadoDespacho; }
    public void setEstadoDespacho(String estadoDespacho) { this.estadoDespacho = estadoDespacho; }
    public String getNombreBodega() { return nombreBodega; }
    public void setNombreBodega(String nombreBodega) { this.nombreBodega = nombreBodega; }
}
