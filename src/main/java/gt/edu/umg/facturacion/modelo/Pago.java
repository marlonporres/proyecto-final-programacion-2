package gt.edu.umg.facturacion.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa un pago registrado a favor de una factura.
 */
public class Pago {

    private long idPago;
    private LocalDateTime fechaHora;
    private BigDecimal monto;
    private MetodoPago metodo;
    private String referencia;

    public Pago() {
        this.fechaHora = LocalDateTime.now();
        this.monto = BigDecimal.ZERO;
        this.metodo = MetodoPago.EFECTIVO;
    }

    public Pago(long idPago, LocalDateTime fechaHora, BigDecimal monto, MetodoPago metodo, String referencia) {
        this.idPago = idPago;
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.metodo = metodo != null ? metodo : MetodoPago.EFECTIVO;
        this.referencia = referencia;
    }

    public long getIdPago() {
        return idPago;
    }

    public void setIdPago(long idPago) {
        this.idPago = idPago;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto del pago no puede ser negativo.");
        }
        this.monto = monto != null ? monto : BigDecimal.ZERO;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    @Override
    public String toString() {
        return metodo + " - Q " + monto + (referencia != null && !referencia.isBlank() ? " (" + referencia + ")" : "");
    }
}
