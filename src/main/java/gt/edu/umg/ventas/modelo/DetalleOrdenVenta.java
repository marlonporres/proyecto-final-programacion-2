package gt.edu.umg.ventas.modelo;
import java.math.BigDecimal;
import java.math.RoundingMode;
public class DetalleOrdenVenta {
    private Integer id;
    private OrdenVenta orden;
    private Producto producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal = BigDecimal.ZERO.setScale(2);

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public OrdenVenta getOrden() { return orden; }
    public void setOrden(OrdenVenta orden) { this.orden = orden; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; recalcularSubtotal(); }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; recalcularSubtotal(); }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    private void recalcularSubtotal() {
        if (precioUnitario != null) {
            subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
        }
    }
}
