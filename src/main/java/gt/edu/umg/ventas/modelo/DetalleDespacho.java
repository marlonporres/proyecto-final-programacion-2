package gt.edu.umg.ventas.modelo;
public class DetalleDespacho {
    private Integer id;
    private Despacho despacho;
    private Producto producto;
    private int cantidadSolicitada;
    private int cantidadDespachada;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Despacho getDespacho() { return despacho; }
    public void setDespacho(Despacho despacho) { this.despacho = despacho; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(int cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public int getCantidadDespachada() { return cantidadDespachada; }
    public void setCantidadDespachada(int cantidadDespachada) { this.cantidadDespachada = cantidadDespachada; }
}
