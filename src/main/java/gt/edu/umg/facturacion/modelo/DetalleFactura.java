package gt.edu.umg.facturacion.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Representa una línea o ítem dentro de una factura.
 */
public class DetalleFactura {

    private long idDetalle;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal porcentajeImpuesto;
    private BigDecimal descuento;
    private Producto producto;

    public DetalleFactura() {
        this.cantidad = BigDecimal.ZERO;
        this.precioUnitario = BigDecimal.ZERO;
        this.porcentajeImpuesto = new BigDecimal("12.00");
        this.descuento = BigDecimal.ZERO;
    }

    public DetalleFactura(long idDetalle, Producto producto, BigDecimal cantidad, 
                          BigDecimal precioUnitario, BigDecimal porcentajeImpuesto, BigDecimal descuento) {
        this.idDetalle = idDetalle;
        this.producto = producto;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : (producto != null ? producto.getPrecioVenta() : BigDecimal.ZERO);
        this.porcentajeImpuesto = porcentajeImpuesto != null ? porcentajeImpuesto : new BigDecimal("12.00");
        this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
    }

    /**
     * Calcula el subtotal del detalle: (cantidad * precioUnitario) - descuento.
     *
     * @return Subtotal monetario redondeado a 2 decimales
     */
    public BigDecimal calcularSubtotal() {
        if (cantidad == null || precioUnitario == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal bruto = cantidad.multiply(precioUnitario);
        BigDecimal desc = (descuento != null) ? descuento : BigDecimal.ZERO;
        BigDecimal resultado = bruto.subtract(desc);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return resultado.setScale(2, RoundingMode.HALF_UP);
    }

    public long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
    }

    public BigDecimal getPorcentajeImpuesto() {
        return porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(BigDecimal porcentajeImpuesto) {
        this.porcentajeImpuesto = porcentajeImpuesto != null ? porcentajeImpuesto : new BigDecimal("12.00");
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento != null ? descuento : BigDecimal.ZERO;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null && (this.precioUnitario == null || this.precioUnitario.compareTo(BigDecimal.ZERO) == 0)) {
            this.precioUnitario = producto.getPrecioVenta();
        }
    }
}
