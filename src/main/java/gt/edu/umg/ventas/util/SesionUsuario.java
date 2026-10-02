package gt.edu.umg.ventas.util;

import gt.edu.umg.ventas.dao.UsuarioDAO;
import gt.edu.umg.ventas.dao.UsuarioDAOImpl;
import gt.edu.umg.ventas.modelo.Usuario;

/**
 * Gestión centralizada y académica de la sesión de usuario activa.
 * Evita quemar 'id_usuario = 1' en múltiples clases.
 */
public class SesionUsuario {

    private static Usuario usuarioActivo;

    public static synchronized Usuario getUsuarioActivo() {
        if (usuarioActivo == null) {
            UsuarioDAO dao = new UsuarioDAOImpl();
            usuarioActivo = dao.buscarPorNombreUsuario("admin");
            if (!usuarioActivo.isActivo()) throw new IllegalStateException("El usuario admin está inactivo.");
        }
        return usuarioActivo;
    }

    public static synchronized void setUsuarioActivo(Usuario usuario) {
        usuarioActivo = usuario;
    }
}
