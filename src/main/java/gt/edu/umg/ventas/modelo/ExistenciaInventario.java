package gt.edu.umg.ventas.modelo;
public class ExistenciaInventario {
    private Integer id;
    private Producto producto;
    private Bodega bodega;
    private int existenciaActual;
    private int existenciaReservada;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Bodega getBodega() { return bodega; }
    public void setBodega(Bodega bodega) { this.bodega = bodega; }
    public int getExistenciaActual() { return existenciaActual; }
    public void setExistenciaActual(int existenciaActual) { this.existenciaActual = existenciaActual; }
    public int getExistenciaReservada() { return existenciaReservada; }
    public void setExistenciaReservada(int existenciaReservada) { this.existenciaReservada = existenciaReservada; }
    
    public int getExistenciaDisponible() {
        return existenciaActual - existenciaReservada;
    }
}
