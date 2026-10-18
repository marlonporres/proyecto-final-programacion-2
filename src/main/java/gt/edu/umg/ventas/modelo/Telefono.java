package gt.edu.umg.ventas.modelo;

import java.util.Objects;

/**
 * // [CAMBIO 1:N TELÉFONOS]: Clase nueva del modelo que representa cada teléfono asociado a un Cliente (Relación 1:N).
 * Almacena idTelefono, idCliente, numero y tipo (Personal, Trabajo, Casa u Otro) según el documento oficial.
 */
public class Telefono {

    // [CAMBIO 1:N TELÉFONOS]: Atributos solicitados en el modelo esperado del documento oficial.
    private long idTelefono;
    private long idCliente;
    private String numero;
    private String tipo; // Valores: Personal, Trabajo, Casa, Otro

    public Telefono() {
        this.tipo = "Personal";
    }

    public Telefono(String numero, String tipo) {
        this.numero = numero != null ? numero.trim() : "";
        this.tipo = normalizarTipo(tipo);
    }

    public Telefono(long idTelefono, long idCliente, String numero, String tipo) {
        this.idTelefono = idTelefono;
        this.idCliente = idCliente;
        this.numero = numero != null ? numero.trim() : "";
        this.tipo = normalizarTipo(tipo);
    }

    // [CAMBIO 1:N TELÉFONOS]: Garantiza que el tipo sea siempre uno de los valores válidos (por defecto "Personal").
    public static String normalizarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "Personal";
        }
        String limpio = tipo.trim();
        if (limpio.equalsIgnoreCase("Personal")) return "Personal";
        if (limpio.equalsIgnoreCase("Trabajo")) return "Trabajo";
        if (limpio.equalsIgnoreCase("Casa")) return "Casa";
        if (limpio.equalsIgnoreCase("Otro")) return "Otro";
        return limpio;
    }

    public long getIdTelefono() {
        return idTelefono;
    }

    public void setIdTelefono(long idTelefono) {
        this.idTelefono = idTelefono;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero != null ? numero.trim() : "";
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = normalizarTipo(tipo);
    }

    // [CAMBIO 1:N TELÉFONOS]: Representación legible "número (tipo)" usada en el JList de la vista y en la tabla.
    @Override
    public String toString() {
        return numero + " (" + (tipo != null && !tipo.isBlank() ? tipo : "Personal") + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Telefono that)) return false;
        return Objects.equals(numero, that.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }
}
