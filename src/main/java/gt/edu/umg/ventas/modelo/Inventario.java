package gt.edu.umg.ventas.modelo;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Inventario {
    private long idInventario;
    private long idProducto;
    private BigDecimal existencia = BigDecimal.ZERO;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private LocalDateTime ultimaActualizacion;

    public Inventario() {}

    public Inventario(long idInventario, BigDecimal existencia, BigDecimal precioUnitario, LocalDateTime ultimaActualizacion) {
        this.idInventario = idInventario;
        this.existencia = existencia != null ? existencia : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public long getIdInventario() { return idInventario; }
    public void setIdInventario(long idInventario) { this.idInventario = idInventario; }
    public long getIdProducto() { return idProducto; }
    public void setIdProducto(long idProducto) { this.idProducto = idProducto; }
    public BigDecimal getExistencia() { return existencia; }
    public void setExistencia(BigDecimal existencia) { this.existencia = existencia; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public LocalDateTime getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    public boolean hayDisponibilidad(BigDecimal cantidad) {
        return this.existencia.compareTo(cantidad) >= 0;
    }

    public void descontar(BigDecimal cantidad) {
        if (hayDisponibilidad(cantidad)) {
            this.existencia = this.existencia.subtract(cantidad);
        }
    }

    public void reponer(BigDecimal cantidad) {
        this.existencia = this.existencia.add(cantidad);
    }
}
