package gt.edu.umg.ventas.modelo;

public class InventarioResumen {
    private String codigo;
    private String producto;
    private String bodega;
    private int existenciaActual;
    private int existenciaReservada;
    private int disponible;

    public InventarioResumen() {
    }

    public InventarioResumen(String codigo, String producto, String bodega, 
                             int existenciaActual, int existenciaReservada, int disponible) {
        this.codigo = codigo;
        this.producto = producto;
        this.bodega = bodega;
        this.existenciaActual = existenciaActual;
        this.existenciaReservada = existenciaReservada;
        this.disponible = disponible;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getBodega() {
        return bodega;
    }

    public void setBodega(String bodega) {
        this.bodega = bodega;
    }

    public int getExistenciaActual() {
        return existenciaActual;
    }

    public void setExistenciaActual(int existenciaActual) {
        this.existenciaActual = existenciaActual;
    }

    public int getExistenciaReservada() {
        return existenciaReservada;
    }

    public void setExistenciaReservada(int existenciaReservada) {
        this.existenciaReservada = existenciaReservada;
    }

    public int getDisponible() {
        return disponible;
    }

    public void setDisponible(int disponible) {
        this.disponible = disponible;
    }
}
