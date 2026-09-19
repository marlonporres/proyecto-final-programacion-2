package gt.edu.umg.facturacion.modelo;

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

    public Cliente() {
    }

    public Cliente(long idCliente, String nit, String nombre, String direccion, String telefono, String correo) {
        this.idCliente = idCliente;
        this.nit = nit;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
