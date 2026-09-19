package gt.edu.umg.facturacion.modelo;

/**
 * Representa la categoría a la que pertenece un producto.
 */
public class Categoria {

    private long idCategoria;
    private String nombre;
    private String descripcion;
    private boolean activa;

    public Categoria() {
        this.activa = true;
    }

    public Categoria(long idCategoria, String nombre, String descripcion, boolean activa) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activa = activa;
    }

    public long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(long idCategoria) {
        this.idCategoria = idCategoria;
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

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
