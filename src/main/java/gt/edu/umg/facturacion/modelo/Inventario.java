package gt.edu.umg.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa el control de inventario de un producto.
 */
public class Inventario {

    private long idInventario;
    private BigDecimal existencia;
    private BigDecimal stockMinimo;
    private LocalDateTime actualizadoEn;

    public Inventario() {
        this.existencia = BigDecimal.ZERO;
        this.stockMinimo = BigDecimal.ZERO;
        this.actualizadoEn = LocalDateTime.now();
    }

    public Inventario(long idInventario, BigDecimal existencia, BigDecimal stockMinimo, LocalDateTime actualizadoEn) {
        this.idInventario = idInventario;
        this.existencia = existencia != null ? existencia : BigDecimal.ZERO;
        this.stockMinimo = stockMinimo != null ? stockMinimo : BigDecimal.ZERO;
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : LocalDateTime.now();
    }

    /**
     * Verifica si existe disponibilidad suficiente para la cantidad solicitada.
     *
     * @param cantidad Cantidad a verificar
     * @return true si la existencia es mayor o igual a la cantidad solicitada
     */
    public boolean hayDisponibilidad(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return this.existencia.compareTo(cantidad) >= 0;
    }

    /**
     * Descuenta la cantidad especificada de la existencia.
     * La existencia no puede quedar negativa.
     *
     * @param cantidad Cantidad a descontar
     * @throws IllegalArgumentException si la cantidad es nula o menor o igual a cero
     * @throws IllegalStateException si no hay suficiente existencia
     */
    public void descontar(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
        }
        if (!hayDisponibilidad(cantidad)) {
            throw new IllegalStateException("Inventario insuficiente. Existencia actual: " 
                    + this.existencia + ", solicitada: " + cantidad);
        }
        this.existencia = this.existencia.subtract(cantidad);
        this.actualizadoEn = LocalDateTime.now();
    }

    /**
     * Repone la cantidad especificada al inventario.
     *
     * @param cantidad Cantidad a reponer
     * @throws IllegalArgumentException si la cantidad es nula o menor o igual a cero
     */
    public void reponer(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a reponer debe ser mayor a cero.");
        }
        this.existencia = this.existencia.add(cantidad);
        this.actualizadoEn = LocalDateTime.now();
    }

    public long getIdInventario() {
        return idInventario;
    }

    public void setIdInventario(long idInventario) {
        this.idInventario = idInventario;
    }

    public BigDecimal getExistencia() {
        return existencia;
    }

    public void setExistencia(BigDecimal existencia) {
        if (existencia != null && existencia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }
        this.existencia = existencia != null ? existencia : BigDecimal.ZERO;
        this.actualizadoEn = LocalDateTime.now();
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo != null ? stockMinimo : BigDecimal.ZERO;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
