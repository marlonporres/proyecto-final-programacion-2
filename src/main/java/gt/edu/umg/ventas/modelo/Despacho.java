package gt.edu.umg.ventas.modelo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Despacho {
    private Integer id;
    private String numeroDespacho;
    private OrdenVenta orden;
    private Bodega bodega;
    private LocalDateTime fechaDespacho;
    private EstadoDespacho estado;
    private List<DetalleDespacho> detalles = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNumeroDespacho() { return numeroDespacho; }
    public void setNumeroDespacho(String numeroDespacho) { this.numeroDespacho = numeroDespacho; }
    public OrdenVenta getOrden() { return orden; }
    public void setOrden(OrdenVenta orden) { this.orden = orden; }
    public Bodega getBodega() { return bodega; }
    public void setBodega(Bodega bodega) { this.bodega = bodega; }
    public LocalDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDateTime fechaDespacho) { this.fechaDespacho = fechaDespacho; }
    public EstadoDespacho getEstado() { return estado; }
    public void setEstado(EstadoDespacho estado) { this.estado = estado; }
    public List<DetalleDespacho> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleDespacho> detalles) { this.detalles = detalles; }
}
