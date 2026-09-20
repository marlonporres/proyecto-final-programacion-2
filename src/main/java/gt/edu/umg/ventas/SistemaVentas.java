package gt.edu.umg.ventas;

import com.formdev.flatlaf.FlatDarkLaf;
import gt.edu.umg.ventas.controlador.ContenedorPadreController;
import gt.edu.umg.ventas.vista.FrmContenedorPadre;

import javax.swing.*;

/**
 * Clase principal de inicio del Sistema de Facturación.
 * Configura el Look and Feel moderno (FlatDarkLaf) e inicia el contenedor MDI.
 */
public class SistemaVentas {

    public static void main(String[] args) {
        // 1. Inyectar Look & Feel moderno FlatDarkLaf
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo cargar FlatDarkLaf: " + ex.getMessage());
        }

        // 2. Iniciar la interfaz gráfica de forma segura en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            FrmContenedorPadre contenedor = new FrmContenedorPadre();
            ContenedorPadreController control = new ContenedorPadreController(contenedor);
            contenedor.setVisible(true);

            // Abrir automáticamente la ventana de facturación al inicio
            control.abrirVentanaFacturacion();
        });
    }
}
