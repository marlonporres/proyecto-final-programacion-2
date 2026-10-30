package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import java.util.List;

public class MantenimientoCatalogoController {
    private final ProductoDAO productos = new ProductoDAOImpl();
    private final CategoriaDAO categorias = new CategoriaDAOImpl();
    public Producto producto(long id) { return productos.buscarPorId(id); }
    public Categoria categoria(long id) { return categorias.buscarPorId(id); }
    public List<Categoria> categorias() { return categorias.listar(); }
    public void guardar(Producto p) { if (p.getIdProducto() > 0) productos.actualizar(p); else productos.guardar(p); }
    public void guardar(Categoria c) { if (c.getIdCategoria() > 0) categorias.actualizar(c); else categorias.guardar(c); }
}
