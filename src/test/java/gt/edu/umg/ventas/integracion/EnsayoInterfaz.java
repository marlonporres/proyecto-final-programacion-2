package gt.edu.umg.ventas.integracion;

import gt.edu.umg.ventas.controlador.*;
import gt.edu.umg.ventas.dao.*;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.servicio.*;
import gt.edu.umg.ventas.util.*;
import gt.edu.umg.ventas.vista.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/** Ensayo in-process: eventos reales de menú y botones, JDBC real, diálogos sustituidos únicamente en el test. */
final class EnsayoInterfaz {
    static void ejecutar(Cliente cliente, Bodega bodega, List<Producto> productos) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FrmContenedorPadre padre = null;
            String[] orden = {null}, factura = {null};
            List<Object> mensajes = new ArrayList<>();
            try {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
                SesionUsuario.setUsuarioActivo(null);
                Dialogos.configurar(new Dialogos.Interaccion() {
                    public void mensaje(Object mensaje,int tipo) {
                        assertNotEquals(JOptionPane.ERROR_MESSAGE,tipo,String.valueOf(mensaje));
                        assertNotEquals(JOptionPane.WARNING_MESSAGE,tipo,String.valueOf(mensaje));
                        mensajes.add(mensaje);
                    }
                    public int confirmar(Object mensaje) { return JOptionPane.YES_OPTION; }
                    public Object entrada(Object mensaje,Object[] opciones,Object inicial) {
                        if (opciones==null) return factura[0];
                        for (Object opcion : opciones) if (opcion.toString().contains(orden[0])) return opcion;
                        fail("La orden del ensayo no aparece en la selección de factura."); return null;
                    }
                });
                padre = contenedor();
                // Mantenimientos y navegación accesibles desde los menús.
                abrir(padre,"Productos",FrmProductos.class).dispose();
                abrir(padre,"Categorias",FrmCategorias.class).dispose();
                abrir(padre,"Mantenimiento Bodegas",FrmBodegas.class).dispose();
                FrmClientes clientes = abrir(padre,"Clientes",FrmClientes.class);
                JTable tablaClientes = campo(clientes,"tblClientes");
                for (int i=0;i<tablaClientes.getRowCount();i++) if (((Number)tablaClientes.getValueAt(i,0)).longValue()==cliente.getIdCliente())
                    tablaClientes.setRowSelectionInterval(i,i);
                ((JTextField)campo(clientes,"txtNombre")).setText("Cliente IT ensayo interfaz");
                boton(clientes,"Guardar").doClick();
                assertEquals("Cliente IT ensayo interfaz",new ClienteDAOImpl().buscarPorId(cliente.getIdCliente()).getNombre());
                clientes.dispose();
                FrmInventario inventario = abrir(padre,"Inventario (Existencias)",FrmInventario.class);
                ((JTextField)campo(inventario,"txtFiltroProducto")).setText(productos.get(0).getCodigo().substring(0,39));
                boton(inventario,"Buscar").doClick();
                verificarStockVisible(inventario,20);

                FrmOrdenVenta venta = abrir(padre,"Nueva Orden de Venta",FrmOrdenVenta.class);
                seleccionar(campo(venta,"cmbCliente"),String.valueOf(cliente.getIdCliente()),cliente);
                for (Producto producto : productos) {
                    seleccionar(campo(venta,"cmbProducto"),producto.getCodigo(),producto);
                    ((JTextField)campo(venta,"txtCantidad")).setText("5");
                    boton(venta,"Agregar Detalle").doClick();
                }
                boton(venta,"Guardar Orden").doClick();
                OrdenVenta guardada = new OrdenVentaDAOImpl().obtenerTodos().stream()
                        .filter(o -> o.getCliente().getIdCliente()==cliente.getIdCliente()).findFirst().orElseThrow();
                orden[0]=guardada.getNumeroOrden();
                assertEquals(new BigDecimal("112.00"),guardada.getTotal());
                verificarStockVisible(inventario,20);
                venta.dispose();
                FrmDespacho despacho = abrir(padre,"Despachos",FrmDespacho.class);
                seleccionar(campo(despacho,"ordenes"),orden[0],null);
                seleccionar(campo(despacho,"bodegas"),bodega.getNombre(),bodega);
                assertFalse(boton(despacho,"Confirmar despacho").isEnabled());
                boton(despacho,"Generar orden de despacho").doClick();
                Despacho generado = new DespachoDAOImpl().obtenerPorOrden(guardada.getId());
                assertEquals(EstadoDespacho.PENDIENTE,generado.getEstado());
                assertTrue(generado.getDetalles().stream().allMatch(d -> d.getCantidadDespachada()==0));
                verificarStockVisible(inventario,20);
                imagen(despacho,"despacho-pendiente.png");
                despacho.dispose();
                // Recuperación del despacho guardado al reabrir el módulo.
                despacho = abrir(padre,"Despachos",FrmDespacho.class);
                seleccionar(campo(despacho,"ordenes"),orden[0],null);
                assertFalse(boton(despacho,"Generar orden de despacho").isEnabled());
                assertTrue(boton(despacho,"Confirmar despacho").isEnabled());
                boton(despacho,"Confirmar despacho").doClick();
                verificarStockVisible(inventario,15);
                assertEquals(EstadoDespacho.CONFIRMADO,new DespachoDAOImpl().obtenerPorOrden(guardada.getId()).getEstado());
                PantallaFacturacion pantalla = abrir(padre,"Facturación de órdenes despachadas",PantallaFacturacion.class);
                pantalla.getBtnCargarOrden().doClick();
                assertEquals(2,pantalla.getTblDetalle().getRowCount());
                pantalla.getBtnEmitir().doClick();
                Factura emitida = new FacturaDAOImpl().buscarPorOrden(guardada.getId());
                assertNotNull(emitida); factura[0]=emitida.getNumero();
                assertEquals(new BigDecimal("112.00"),emitida.calcularTotal());
                verificarStockVisible(inventario,15);
                imagen(pantalla,"factura-ensayo.png");
                FrmConsultaOrdenesVenta consulta = abrir(padre,"Consultar Ordenes de Venta",FrmConsultaOrdenesVenta.class);
                boton(consulta,"Buscar").doClick();
                JTable tabla = campo(consulta,"table"); int fila=-1;
                for (int i=0;i<tabla.getRowCount();i++) if (orden[0].equals(tabla.getValueAt(i,0))) fila=i;
                assertTrue(fila>=0); assertEquals(factura[0],tabla.getValueAt(fila,8));
                tabla.setRowSelectionInterval(fila,fila); boton(consulta,"Ver operación").doClick();
                assertTrue(mensajes.stream().anyMatch(JScrollPane.class::isInstance));
                // Nuevo contenedor y nuevo controlador de factura: sin caché del anterior.
                padre.dispose(); SesionUsuario.setUsuarioActivo(null); padre=contenedor();
                pantalla = abrir(padre,"Facturación de órdenes despachadas",PantallaFacturacion.class);
                pantalla.getBtnConsultar().doClick();
                assertEquals(2,pantalla.getTblDetalle().getRowCount());
                assertEquals(productos.get(0).getCodigo(),pantalla.getTblDetalle().getValueAt(0,0));
                assertFalse(pantalla.getBtnEmitir().isEnabled());
            } catch (Exception e) { throw new RuntimeException(e); }
            finally { if (padre!=null) padre.dispose(); Dialogos.configurar(null); SesionUsuario.setUsuarioActivo(null); }
        });
    }
    private static FrmContenedorPadre contenedor() {
        FrmContenedorPadre p=new FrmContenedorPadre(); new ContenedorPadreController(p); return p;
    }
    private static <T extends JInternalFrame> T abrir(FrmContenedorPadre p,String texto,Class<T> tipo) {
        JMenuItem item=null;
        for (int i=0;i<p.getJMenuBar().getMenuCount();i++) {
            JMenu menu=p.getJMenuBar().getMenu(i);
            for (int j=0;j<menu.getItemCount();j++) if (menu.getItem(j)!=null && texto.equals(menu.getItem(j).getText())) item=menu.getItem(j);
        }
        assertNotNull(item,"Menú "+texto); item.doClick();
        for (JInternalFrame frame:p.getDesktopPane().getAllFrames()) if (tipo.isInstance(frame)) return tipo.cast(frame);
        throw new AssertionError("El menú no abrió "+tipo.getSimpleName());
    }
    @SuppressWarnings("unchecked") private static <T> T campo(Object objeto,String nombre) throws Exception {
        Field f=objeto.getClass().getDeclaredField(nombre); f.setAccessible(true); return (T)f.get(objeto);
    }
    private static JButton boton(Container contenedor,String texto) {
        for (Component c:contenedor.getComponents()) {
            if (c instanceof JButton b && b.getText().endsWith(texto)) return b;
            if (c instanceof Container sub) { JButton b=buscarBoton(sub,texto); if (b!=null) return b; }
        }
        throw new AssertionError("Botón no encontrado: "+texto);
    }
    private static JButton buscarBoton(Container contenedor,String texto) {
        for (Component c:contenedor.getComponents()) {
            if (c instanceof JButton b && b.getText().endsWith(texto)) return b;
            if (c instanceof Container sub) { JButton b=buscarBoton(sub,texto); if (b!=null) return b; }
        } return null;
    }
    private static void seleccionar(JComboBox<?> combo,String texto,Object entidad) {
        for (int i=0;i<combo.getItemCount();i++) {
            Object item=combo.getItemAt(i);
            boolean igual = item.toString().contains(texto);
            if (entidad instanceof Cliente c && item instanceof Cliente x) igual=c.getIdCliente()==x.getIdCliente();
            if (entidad instanceof Producto p && item instanceof Producto x) igual=p.getIdProducto()==x.getIdProducto();
            if (entidad instanceof Bodega b && item instanceof Bodega x) igual=b.getId().equals(x.getId());
            if (igual) { combo.setSelectedIndex(i); return; }
        } throw new AssertionError("Selección no encontrada: "+texto);
    }
    private static void verificarStockVisible(FrmInventario vista,int esperado) throws Exception {
        JTable tabla=campo(vista,"tblInventario"); assertEquals(2,tabla.getRowCount());
        for (int i=0;i<2;i++) assertEquals(esperado,((Number)tabla.getValueAt(i,5)).intValue());
    }
    private static void imagen(JInternalFrame vista,String nombre) throws Exception {
        Container contenido=vista.getContentPane(); contenido.setSize(920,620); layout(contenido);
        BufferedImage imagen=new BufferedImage(920,620,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=imagen.createGraphics(); contenido.printAll(g); g.dispose();
        Files.createDirectories(Path.of("target","ui-qa"));
        ImageIO.write(imagen,"png",Path.of("target","ui-qa",nombre).toFile());
    }
    private static void layout(Container c) {
        c.doLayout(); for (Component hijo:c.getComponents()) if (hijo instanceof Container sub) layout(sub);
    }
}
