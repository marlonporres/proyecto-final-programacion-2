package gt.edu.umg.ventas.util;

import javax.swing.*;
import gt.edu.umg.ventas.vista.*;

/** Actualiza las consultas abiertas después de una operación confirmada. */
public final class CambiosVentas {
    private CambiosVentas() { }
    public static void notificar(JInternalFrame origen) {
        JDesktopPane escritorio = origen.getDesktopPane();
        if (escritorio == null) return;
        for (JInternalFrame frame : escritorio.getAllFrames()) {
            if (frame == origen) continue;
            if (frame instanceof FrmInventario inventario) inventario.refrescarInventario();
            if (frame instanceof FrmDespacho despacho) despacho.cargarDatosIniciales();
            if (frame instanceof FrmConsultaOrdenesVenta consulta) consulta.buscar();
        }
    }
}
