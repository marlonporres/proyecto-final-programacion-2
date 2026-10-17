package gt.edu.umg.ventas.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa al cliente al cual se le puede emitir una factura.
 */
public class Cliente {

    private long idCliente;
    private String nit;
    private String nombre;
    private String direccion;
    private String telefono;
    private String correo;
    // [CAMBIO 1:N TELÉFONOS]: Lista para almacenar múltiples números de teléfono asociados al cliente.
    private List<String> telefonos = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(long idCliente, String nit, String nombre, String direccion, String telefono, String correo) {
        this.idCliente = idCliente;
        this.nit = nit;
        this.nombre = nombre;
        this.direccion = direccion;
        this.correo = correo;
        // [CAMBIO 1:N TELÉFONOS]: Sincroniza el teléfono inicial con la lista de teléfonos.
        setTelefono(telefono);
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public long getId() {
        return idCliente;
    }

    public void setId(long id) {
        this.idCliente = id;
    }

    public void setId(int id) {
        this.idCliente = id;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    // [CAMBIO 1:N TELÉFONOS]: Retorna el teléfono principal (el primero de la lista) para mantener compatibilidad.
    public String getTelefono() {
        if (telefonos != null && !telefonos.isEmpty()) {
            return telefonos.get(0);
        }
        return telefono;
    }

    // [CAMBIO 1:N TELÉFONOS]: Sincroniza el atributo individual con la primera posición de la lista de teléfonos.
    public void setTelefono(String telefono) {
        this.telefono = telefono;
        if (this.telefonos == null) {
            this.telefonos = new ArrayList<>();
        }
        if (telefono == null || telefono.isBlank()) {
            this.telefonos.clear();
        } else {
            String limpio = telefono.trim();
            if (this.telefonos.isEmpty()) {
                this.telefonos.add(limpio);
            } else {
                this.telefonos.set(0, limpio);
            }
        }
    }

    // [CAMBIO 1:N TELÉFONOS]: Obtiene la lista completa de teléfonos del cliente.
    public List<String> getTelefonos() {
        if ((telefonos == null || telefonos.isEmpty()) && telefono != null && !telefono.isBlank()) {
            List<String> respaldo = new ArrayList<>();
            respaldo.add(telefono.trim());
            return respaldo;
        }
        return telefonos != null ? telefonos : new ArrayList<>();
    }

    // [CAMBIO 1:N TELÉFONOS]: Reemplaza la lista de teléfonos descartando vacíos y duplicados, y actualiza el principal.
    public void setTelefonos(List<String> telefonos) {
        this.telefonos = new ArrayList<>();
        if (telefonos != null) {
            for (String t : telefonos) {
                if (t != null && !t.isBlank()) {
                    String limpio = t.trim();
                    if (!this.telefonos.contains(limpio)) {
                        this.telefonos.add(limpio);
                    }
                }
            }
        }
        this.telefono = this.telefonos.isEmpty() ? "" : this.telefonos.get(0);
    }

    // [CAMBIO 1:N TELÉFONOS]: Permite añadir un nuevo número de teléfono a la lista del cliente sin duplicarlo.
    public void agregarTelefono(String numero) {
        if (numero != null && !numero.isBlank()) {
            String limpio = numero.trim();
            if (this.telefonos == null) {
                this.telefonos = new ArrayList<>();
            }
            if (!this.telefonos.contains(limpio)) {
                this.telefonos.add(limpio);
            }
            if (this.telefono == null || this.telefono.isBlank()) {
                this.telefono = this.telefonos.get(0);
            }
        }
    }

    // [CAMBIO 1:N TELÉFONOS]: Formatea todos los teléfonos separados por coma para mostrarlos en la tabla de la vista.
    public String getTelefonosTexto() {
        if (telefonos != null && !telefonos.isEmpty()) {
            return String.join(", ", telefonos);
        }
        return telefono != null ? telefono : "";
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public String toString() {
        return nombre + " (" + nit + ")";
    }
}
