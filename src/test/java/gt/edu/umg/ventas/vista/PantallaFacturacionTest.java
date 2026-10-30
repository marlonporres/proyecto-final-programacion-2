package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.modelo.*;
import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PantallaFacturacionTest {
    @Test void controlesReflejanEstadoYOrigenInmutable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PantallaFacturacion vista = new PantallaFacturacion();
            vista.mostrarFactura(null);
            assertFalse(vista.getBtnEmitir().isEnabled());
            assertFalse(vista.getBtnRegistrarPago().isEnabled());
            assertFalse(vista.getTxtNit().isEditable());
            assertFalse(vista.getTxtClienteNombre().isEditable());
            assertFalse(vista.getBtnBuscarCliente().isVisible());
            Factura f = new Factura(); f.setNumero("FAC-00000000-0000-0000-0000-000000000001");
            f.setCliente(new Cliente(1, "CF", "Cliente de prueba", "Guatemala", "", ""));
            OrdenVenta o = new OrdenVenta(); o.setNumeroOrden("OV-1"); f.setOrden(o);
            Producto p = new Producto(); p.setCodigo("TEC-001"); p.setNombre("Producto"); p.setPrecioVenta(new BigDecimal("95.00"));
            f.agregarDetalle(p, BigDecimal.valueOf(5)); vista.mostrarFactura(f);
            assertTrue(vista.getBtnEmitir().isEnabled()); assertFalse(vista.getBtnAnular().isEnabled());
            f.setEstado(EstadoFactura.EMITIDA); vista.mostrarFactura(f);
            assertFalse(vista.getBtnEmitir().isEnabled()); assertTrue(vista.getBtnRegistrarPago().isEnabled());
            f.setEstado(EstadoFactura.ANULADA); vista.mostrarFactura(f);
            assertFalse(vista.getBtnRegistrarPago().isEnabled()); assertFalse(vista.getBtnAnular().isEnabled());
            vista.dispose();
        });
    }

    @Test void renderizaDocumentoSinConexionSql() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                PantallaFacturacion vista = new PantallaFacturacion();
                Factura f = new Factura(); f.setNumero("FAC-00000000-0000-0000-0000-000000000001");
                f.setCliente(new Cliente(1, "1234567-8", "Cliente de demostración", "Guatemala", "", ""));
                f.setEstado(EstadoFactura.EMITIDA);
                Producto p = new Producto(); p.setCodigo("TEC-001"); p.setNombre("Mouse inalámbrico");
                f.agregarDetalleDirecto(new DetalleFactura(0, p, BigDecimal.valueOf(5), new BigDecimal("95.00"), new BigDecimal("12.00"), BigDecimal.ZERO));
                vista.mostrarFactura(f); vista.setVisible(true);
                vista.getContentPane().setSize(900, 620);
                validar(vista.getContentPane());
                assertTrue(vista.getTblDetalle().getTableHeader().getHeight() > 0);
                BufferedImage imagen = new BufferedImage(900, 620, BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = imagen.createGraphics(); vista.getContentPane().printAll(g); g.dispose();
                Files.createDirectories(Path.of("target", "ui-qa"));
                ImageIO.write(imagen, "png", Path.of("target", "ui-qa", "facturacion.png").toFile());
                vista.dispose();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
    private void validar(java.awt.Container componente) {
        componente.doLayout();
        for (java.awt.Component hijo : componente.getComponents()) if (hijo instanceof java.awt.Container contenedor) validar(contenedor);
    }
}
