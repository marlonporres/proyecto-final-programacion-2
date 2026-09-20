package gt.edu.umg.ventas.vista;

import javax.swing.*;
import java.awt.*;

public class FrmContenedorPadre extends JFrame {
    private JDesktopPane desktopPane;
    private JMenuItem itemFacturacion = new JMenuItem();
    private JMenuItem itemClientes = new JMenuItem();
    private JMenuItem itemProductos = new JMenuItem();

    public FrmContenedorPadre() {
        super("Sistema de Ventas - Programacion II UMG");
        initComponents();
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(30, 30, 30));
        setContentPane(desktopPane);

        JMenuBar menuBar = new JMenuBar();
        itemFacturacion = new JMenuItem("Facturacion");
        itemClientes = new JMenuItem("Clientes");
        itemProductos = new JMenuItem("Productos");

        // Menu Ventas
        JMenu menuVentas = new JMenu("VENTAS");
        JMenuItem itemNuevaOrden = new JMenuItem("Nueva Orden de Venta");
        JMenuItem itemConsultarOrdenes = new JMenuItem("Consultar Ordenes de Venta");
        
        itemNuevaOrden.addActionListener(e -> agregarVentana(new FrmOrdenVenta()));
        itemConsultarOrdenes.addActionListener(e -> agregarVentana(new FrmConsultaOrdenesVenta()));
        
        menuVentas.add(itemNuevaOrden);
        menuVentas.add(itemConsultarOrdenes);
        menuBar.add(menuVentas);

        // Menu Inventario
        JMenu menuInventario = new JMenu("INVENTARIO");
        JMenuItem itemExistencias = new JMenuItem("Inventario (Existencias)");
        JMenuItem itemBodegas = new JMenuItem("Mantenimiento Bodegas");
        JMenuItem itemDespachos = new JMenuItem("Despachos");
        
        itemExistencias.addActionListener(e -> agregarVentana(new FrmInventario()));
        itemBodegas.addActionListener(e -> agregarVentana(new FrmBodegas()));
        itemDespachos.addActionListener(e -> agregarVentana(new FrmDespacho()));
        
        menuInventario.add(itemExistencias);
        menuInventario.add(itemBodegas);
        menuInventario.add(itemDespachos);
        menuBar.add(menuInventario);

        // Menu Catalogos
        JMenu menuCatalogos = new JMenu("CATALOGOS");
        
        
        JMenuItem itemCategorias = new JMenuItem("Categorias");
        
        itemClientes.addActionListener(e -> agregarVentana(new FrmClientes()));
        itemProductos.addActionListener(e -> agregarVentana(new FrmProductos()));
        itemCategorias.addActionListener(e -> agregarVentana(new FrmCategorias()));
        
        menuCatalogos.add(itemClientes);
        menuCatalogos.add(itemProductos);
        menuCatalogos.add(itemCategorias);
        menuBar.add(menuCatalogos);

        setJMenuBar(menuBar);
    }

    private void agregarVentana(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }
    public JDesktopPane getDesktopPane() { return desktopPane; }
    public JMenuItem getItemFacturacion() { return itemFacturacion; }
    public JMenuItem getItemClientes() { return itemClientes; }
    public JMenuItem getItemProductos() { return itemProductos; }
}
