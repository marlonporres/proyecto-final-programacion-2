package gt.edu.umg.ventas.modelo;
import java.time.LocalDateTime;
public class MovimientoInventario {
    private Integer id;
    private Producto producto;
    private Bodega bodega;
    private TipoMovimientoInventario tipo;
    private int cantidad;
    private LocalDateTime fecha;
    private String referencia;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Bodega getBodega() { return bodega; }
    public void setBodega(Bodega bodega) { this.bodega = bodega; }
    public TipoMovimientoInventario getTipo() { return tipo; }
    public void setTipo(TipoMovimientoInventario tipo) { this.tipo = tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
