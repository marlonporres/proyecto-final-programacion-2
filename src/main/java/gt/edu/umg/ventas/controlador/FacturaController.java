package gt.edu.umg.ventas.controlador;
import gt.edu.umg.ventas.util.Dialogos;

import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.servicio.FacturaService;
import gt.edu.umg.ventas.util.*;
import gt.edu.umg.ventas.vista.PantallaFacturacion;
import javax.swing.*;
import java.time.LocalDateTime;

/** Factura únicamente las órdenes despachadas; el origen no es editable. */
public class FacturaController {
    private final PantallaFacturacion vista;
    private final FacturaService servicio;
    private Factura facturaActual;

    public FacturaController(PantallaFacturacion vista) { this(vista, new FacturaService()); }
    public FacturaController(PantallaFacturacion vista, FacturaService servicio) {
        this.vista = vista;
        this.servicio = servicio;
        vista.setController(this);
        vista.getBtnCargarOrden().addActionListener(e -> cargarOrdenInteractivo());
        vista.getBtnNuevaFactura().addActionListener(e -> { facturaActual = null; vista.mostrarFactura(null); });
        vista.getBtnEmitir().addActionListener(e -> solicitarEmision());
        vista.getBtnConsultar().addActionListener(e -> {
            String numero = Dialogos.showInputDialog(vista, "Número de factura:");
            if (numero != null && !numero.isBlank()) consultar(numero);
        });
        vista.getBtnRegistrarPago().addActionListener(e -> registrarPago());
        vista.getBtnAnular().addActionListener(e -> anular());
        vista.mostrarFactura(null);
    }

    private record OpcionOrden(OrdenVenta orden) {
        @Override public String toString() {
            return orden.getNumeroOrden() + " | " + orden.getCliente().getNombre() + " | " + FormatoMoneda.formatear(orden.getTotal());
        }
    }

    private void cargarOrdenInteractivo() {
        try {
            OpcionOrden[] opciones = servicio.obtenerOrdenesFacturables().stream().map(OpcionOrden::new).toArray(OpcionOrden[]::new);
            if (opciones.length == 0) { mensaje("No hay órdenes despachadas pendientes de facturar."); return; }
            OpcionOrden seleccion = (OpcionOrden) Dialogos.showInputDialog(vista, "Seleccione una orden despachada:",
                    "Facturar orden", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (seleccion != null) cargarDesdeOrden(seleccion.orden());
        } catch (Exception e) { error(e); }
    }

    public void cargarDesdeOrden(OrdenVenta orden) {
        try {
            facturaActual = servicio.crearDesdeOrden(orden.getId(), SesionUsuario.getUsuarioActivo());
            vista.mostrarFactura(facturaActual);
        } catch (Exception e) { error(e); }
    }

    public void solicitarEmision() {
        if (facturaActual == null) { mensaje("Seleccione una orden despachada."); return; }
        if (Dialogos.showConfirmDialog(vista, "¿Emitir la factura por " + FormatoMoneda.formatear(facturaActual.calcularTotal()) + "?",
                "Emitir factura", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                facturaActual = servicio.emitir(facturaActual.getIdFactura());
                vista.mostrarFactura(facturaActual);
                CambiosVentas.notificar(vista);
                mensaje("Factura guardada: " + facturaActual.getNumero());
            } catch (Exception e) { error(e); }
        }
    }

    public void consultar(String numero) {
        try { facturaActual = servicio.consultarPorNumero(numero); vista.mostrarFactura(facturaActual); }
        catch (Exception e) { error(e); }
    }

    private void registrarPago() {
        if (facturaActual == null) return;
        try {
            Pago pago = new Pago(0, LocalDateTime.now(), FormatoMoneda.parsear(vista.getTxtMontoPago().getText()),
                    (MetodoPago) vista.getCmbMetodoPago().getSelectedItem(), vista.getTxtReferenciaPago().getText().trim());
            facturaActual = servicio.registrarPago(facturaActual.getIdFactura(), pago);
            vista.mostrarFactura(facturaActual);
            CambiosVentas.notificar(vista);
            mensaje("Pago guardado.");
        } catch (Exception e) { error(e); }
    }

    private void anular() {
        if (facturaActual == null) return;
        if (Dialogos.showConfirmDialog(vista, "¿Anular el documento comercial? La mercadería ya fue despachada.",
                "Anular factura", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            facturaActual = servicio.anular(facturaActual.getIdFactura());
            vista.mostrarFactura(facturaActual);
            CambiosVentas.notificar(vista);
        } catch (Exception e) { error(e); }
    }

    private void mensaje(String texto) { Dialogos.showMessageDialog(vista, texto); }
    private void error(Exception e) { Dialogos.showMessageDialog(vista, e.getMessage(), "Operación no completada", JOptionPane.ERROR_MESSAGE); }
    public Factura getFacturaActual() { return facturaActual; }
}
