package gt.edu.umg.ventas.modelo;

import java.math.BigDecimal;

/**
 * Representa un producto del catálogo de ventas.
 * El stock no pertenece directamente a Producto, sino a ExistenciaInventario por Bodega.
 */
public class Producto {

    private long idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioVenta;
    private boolean activo;
    private Categoria categoria;

    public Producto() {
        this.precioVenta = BigDecimal.ZERO;
        this.activo = true;
    }

    public Producto(long idProducto, String codigo, String nombre, String descripcion, 
                    BigDecimal precioVenta, boolean activo, Categoria categoria) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioVenta = precioVenta != null ? precioVenta : BigDecimal.ZERO;
        this.activo = activo;
        this.categoria = categoria;
    }

    public void actualizarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser nulo ni negativo.");
        }
        this.precioVenta = precio;
    }

    public long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(long idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        actualizarPrecio(precioVenta);
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
