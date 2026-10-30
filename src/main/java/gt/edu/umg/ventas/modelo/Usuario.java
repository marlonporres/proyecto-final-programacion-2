package gt.edu.umg.ventas.modelo;

/**
 * Representa al usuario que registra o atiende las operaciones de facturación.
 */
public class Usuario {

    private long idUsuario;
    private String nombre;
    private String nombreUsuario;
    private String rol;
    private boolean activo;

    public Usuario() {
        this.activo = true;
    }

    public Usuario(long idUsuario, String nombre, String nombreUsuario, String rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
        this.activo = activo;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombre + " (" + nombreUsuario + ")";
    }
}
