package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.vista.FrmContenedorPadre;
import gt.edu.umg.ventas.vista.PantallaFacturacion;

import javax.swing.*;

/**
 * Controlador principal del contenedor MDI.
 */
public class ContenedorPadreController {

    private final FrmContenedorPadre vistaPadre;

    public ContenedorPadreController(FrmContenedorPadre vistaPadre) {
        this.vistaPadre = vistaPadre;
        enlazarEventos();
    }

    private void enlazarEventos() {
        if (vistaPadre.getItemFacturacion() != null) {
            vistaPadre.getItemFacturacion().addActionListener(e -> abrirVentanaFacturacion());
        }
    }

    public void abrirVentanaFacturacion() {
        for (JInternalFrame frame : vistaPadre.getDesktopPane().getAllFrames()) {
            if (frame instanceof PantallaFacturacion) {
                try {
                    frame.setIcon(false);
                    frame.setSelected(true);
                    frame.toFront();
                } catch (Exception ignored) {}
                return;
            }
        }

        PantallaFacturacion pantalla = new PantallaFacturacion();
        new FacturaController(pantalla);
        vistaPadre.getDesktopPane().add(pantalla);
        pantalla.setVisible(true);
        try {
            pantalla.setSelected(true);
        } catch (Exception ignored) {}
    }

}
