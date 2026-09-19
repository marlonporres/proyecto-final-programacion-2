package gt.edu.umg.facturacion.dao;

import gt.edu.umg.facturacion.modelo.Usuario;
import java.util.List;

/**
 * Interfaz de acceso a datos para la entidad Usuario.
 */
public interface UsuarioDAO {

    Usuario buscarPorId(long idUsuario);

    Usuario buscarPorNombreUsuario(String nombreUsuario);

    List<Usuario> listar();
}
