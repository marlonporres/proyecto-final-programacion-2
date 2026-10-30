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
    // [CAMBIO 1:N TELÉFONOS]: Colección 1:N de objetos Telefono (cada uno con idTelefono, idCliente, numero y tipo).
    private List<Telefono> listaTelefonos = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(long idCliente, String nit, String nombre, String direccion, String telefono, String correo) {
        this.idCliente = idCliente;
        this.nit = nit;
        this.nombre = nombre;
        this.direccion = direccion;
        this.correo = correo;
        // [CAMBIO 1:N TELÉFONOS]: Sincroniza el teléfono inicial con la colección de objetos Telefono.
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

    // [CAMBIO 1:N TELÉFONOS]: Retorna el número del primer teléfono de la colección para compatibilidad con el sistema.
    public String getTelefono() {
        if (listaTelefonos != null && !listaTelefonos.isEmpty()) {
            return listaTelefonos.get(0).getNumero();
        }
        return telefono;
    }

    // [CAMBIO 1:N TELÉFONOS]: Sincroniza el atributo individual con la primera posición de la lista de objetos Telefono.
    public void setTelefono(String telefono) {
        this.telefono = telefono;
        if (this.listaTelefonos == null) {
            this.listaTelefonos = new ArrayList<>();
        }
        if (telefono == null || telefono.isBlank()) {
            this.listaTelefonos.clear();
        } else {
            String limpio = telefono.trim();
            if (this.listaTelefonos.isEmpty()) {
                this.listaTelefonos.add(new Telefono(0, this.idCliente, limpio, "Personal"));
            } else {
                this.listaTelefonos.get(0).setNumero(limpio);
            }
        }
    }

    // [CAMBIO 1:N TELÉFONOS]: Devuelve la colección completa de objetos Telefono (con número y tipo).
    public List<Telefono> getListaTelefonos() {
        if ((listaTelefonos == null || listaTelefonos.isEmpty()) && telefono != null && !telefono.isBlank()) {
            List<Telefono> respaldo = new ArrayList<>();
            respaldo.add(new Telefono(0, this.idCliente, telefono.trim(), "Personal"));
            return respaldo;
        }
        return listaTelefonos != null ? listaTelefonos : new ArrayList<>();
    }

    // [CAMBIO 1:N TELÉFONOS]: Asigna la colección de objetos Telefono (número y tipo) evitando números vacíos o duplicados.
    public void setListaTelefonos(List<Telefono> telefonos) {
        this.listaTelefonos = new ArrayList<>();
        if (telefonos != null) {
            for (Telefono t : telefonos) {
                if (t != null && t.getNumero() != null && !t.getNumero().isBlank()) {
                    agregarTelefono(t.getNumero(), t.getTipo());
                }
            }
        }
        this.telefono = this.listaTelefonos.isEmpty() ? "" : this.listaTelefonos.get(0).getNumero();
    }

    // [CAMBIO 1:N TELÉFONOS]: Agrega un teléfono con su número y su tipo (Personal, Trabajo, Casa u Otro).
    public void agregarTelefono(String numero, String tipo) {
        if (numero != null && !numero.isBlank()) {
            String limpio = numero.trim();
            if (this.listaTelefonos == null) {
                this.listaTelefonos = new ArrayList<>();
            }
            for (Telefono existente : this.listaTelefonos) {
                if (existente.getNumero().equalsIgnoreCase(limpio)) {
                    existente.setTipo(tipo);
                    return;
                }
            }
            this.listaTelefonos.add(new Telefono(0, this.idCliente, limpio, tipo));
            if (this.telefono == null || this.telefono.isBlank()) {
                this.telefono = this.listaTelefonos.get(0).getNumero();
            }
        }
    }

    // [CAMBIO 1:N TELÉFONOS]: Sobrecarga que agrega un objeto Telefono directamente a la colección del cliente.
    public void agregarTelefono(Telefono tel) {
        if (tel != null) {
            agregarTelefono(tel.getNumero(), tel.getTipo());
        }
    }

    // [CAMBIO 1:N TELÉFONOS]: Sobrecarga compatible que agrega un número asignándole por defecto el tipo "Personal".
    public void agregarTelefono(String numero) {
        agregarTelefono(numero, "Personal");
    }

    // [CAMBIO 1:N TELÉFONOS]: Compatibilidad para obtener únicamente la lista de cadenas de números telefónicos.
    public List<String> getTelefonos() {
        List<String> numeros = new ArrayList<>();
        for (Telefono t : getListaTelefonos()) {
            numeros.add(t.getNumero());
        }
        return numeros;
    }

    // [CAMBIO 1:N TELÉFONOS]: Compatibilidad para asignar una lista de Strings (convierte cada uno en Telefono).
    public void setTelefonos(List<String> telefonos) {
        this.listaTelefonos = new ArrayList<>();
        if (telefonos != null) {
            for (String s : telefonos) {
                if (s != null && !s.isBlank()) {
                    agregarTelefono(s.trim(), "Personal");
                }
            }
        }
        this.telefono = this.listaTelefonos.isEmpty() ? "" : this.listaTelefonos.get(0).getNumero();
    }

    // [CAMBIO 1:N TELÉFONOS]: Formatea todos los teléfonos con su número y tipo, ej: "5555-1234 (Personal), 2222-9999 (Trabajo)".
    public String getTelefonosTexto() {
        List<Telefono> actual = getListaTelefonos();
        if (!actual.isEmpty()) {
            List<String> textos = new ArrayList<>();
            for (Telefono t : actual) {
                textos.add(t.toString());
            }
            return String.join(", ", textos);
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
