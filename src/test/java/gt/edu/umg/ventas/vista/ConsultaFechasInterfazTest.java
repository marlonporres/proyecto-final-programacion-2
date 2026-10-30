package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.ConsultaOrdenVentaController;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.util.Dialogos;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConsultaFechasInterfazTest {
    static final class ConsultaPrueba extends ConsultaOrdenVentaController {
        LocalDateTime desde, hasta;
        boolean fallar;
        @Override public List<OrdenVentaResumen> buscar(LocalDateTime desde, LocalDateTime hasta) {
            this.desde = desde; this.hasta = hasta;
            if (fallar) throw new IllegalStateException("Conexión no disponible");
            OrdenVentaResumen fila = new OrdenVentaResumen();
            fila.setNumeroOrden("OV-PRUEBA"); fila.setFecha(desde); fila.setNombreCliente("Cliente");
            fila.setEstado(EstadoOrdenVenta.PENDIENTE); fila.setTotal(new BigDecimal("112.00"));
            return List.of(fila);
        }
    }

    @Test void consultaLasFechasEscritasSinSalirDelCampo() throws Exception {
        enSwing((vista, ctrl, mensajes) -> {
            escribir(vista, "spinDesde", "02/10/2026"); escribir(vista, "spinHasta", "03/10/2026");
            vista.buscar();
            assertEquals(LocalDateTime.of(2026,10,2,0,0), ctrl.desde);
            assertEquals(LocalDateTime.of(2026,10,3,0,0), ctrl.hasta);
            assertEquals(1, ((JTable)campo(vista, "table")).getRowCount());
            assertTrue(mensajes.isEmpty());
        });
    }

    @Test void rechazaDiaInexistenteSinConsultarLaBase() throws Exception {
        enSwing((vista, ctrl, mensajes) -> {
            escribir(vista, "spinDesde", "31/02/2026"); vista.buscar();
            assertNull(ctrl.desde);
            assertEquals(List.of(JOptionPane.WARNING_MESSAGE), mensajes);
            assertEquals(0, ((JTable)campo(vista, "table")).getRowCount());
        });
    }

    @Test void errorDeConsultaNoDejaResultadosAnteriores() throws Exception {
        enSwing((vista, ctrl, mensajes) -> {
            vista.buscar(); assertEquals(1, ((JTable)campo(vista, "table")).getRowCount());
            ctrl.fallar = true; vista.buscar();
            assertEquals(0, ((JTable)campo(vista, "table")).getRowCount());
            assertEquals(List.of(), campo(vista, "resultados"));
            assertEquals(List.of(JOptionPane.ERROR_MESSAGE), mensajes);
        });
    }

    private interface Caso { void ejecutar(FrmConsultaOrdenesVenta vista, ConsultaPrueba ctrl, List<Integer> mensajes) throws Exception; }
    private void enSwing(Caso caso) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ConsultaPrueba ctrl = new ConsultaPrueba();
            List<Integer> mensajes = new ArrayList<>();
            Dialogos.configurar(new Dialogos.Interaccion() {
                public void mensaje(Object mensaje, int tipo) { mensajes.add(tipo); }
                public int confirmar(Object mensaje) { return JOptionPane.CANCEL_OPTION; }
                public Object entrada(Object mensaje, Object[] opciones, Object inicial) { return null; }
            });
            FrmConsultaOrdenesVenta vista = new FrmConsultaOrdenesVenta(ctrl);
            try { caso.ejecutar(vista, ctrl, mensajes); }
            catch (Exception e) { throw new RuntimeException(e); }
            finally { vista.dispose(); Dialogos.configurar(null); }
        });
    }

    private static Object campo(Object objeto, String nombre) throws Exception {
        Field field = objeto.getClass().getDeclaredField(nombre); field.setAccessible(true); return field.get(objeto);
    }
    private static void escribir(FrmConsultaOrdenesVenta vista, String nombre, String texto) throws Exception {
        JSpinner spinner = (JSpinner)campo(vista, nombre);
        ((JSpinner.DateEditor)spinner.getEditor()).getTextField().setText(texto);
    }
}
