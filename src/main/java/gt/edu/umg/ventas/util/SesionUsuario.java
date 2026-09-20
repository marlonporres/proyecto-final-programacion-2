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
            try {
                UsuarioDAO dao = new UsuarioDAOImpl();
                usuarioActivo = dao.buscarPorNombreUsuario("admin");
            } catch (Exception e) {
                // Fallback académico si la base no responde en pruebas aisladas
                usuarioActivo = new Usuario(1L, "Administrador del Sistema", "admin", "ADMINISTRADOR", true);
            }
        }
        return usuarioActivo;
    }

    public static synchronized void setUsuarioActivo(Usuario usuario) {
        usuarioActivo = usuario;
    }
}
