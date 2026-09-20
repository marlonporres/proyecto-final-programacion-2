package gt.edu.umg.ventas.modelo;
public class Bodega {
    private Integer id;
    private String nombre;
    private String ubicacion;
    private boolean activa;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
    
    @Override
    public String toString() { return nombre; }
}
