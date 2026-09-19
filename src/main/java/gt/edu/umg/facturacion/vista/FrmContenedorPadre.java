package gt.edu.umg.facturacion.vista;

import javax.swing.*;
import java.awt.*;

/**
 * Contenedor principal MDI (Multiple Document Interface) del sistema de facturación.
 */
public class FrmContenedorPadre extends JFrame {

    private JDesktopPane desktopPane;
    private JMenuBar menuBar;
    private JMenu menuVentas;
    private JMenuItem itemFacturacion;
    private JMenu menuCatalogos;
    private JMenuItem itemClientes;
    private JMenuItem itemProductos;
    private JMenu menuArchivo;
    private JMenuItem itemSalir;

    public FrmContenedorPadre() {
        super("Sistema de Facturación - Programación II UMG");
        initComponents();
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initComponents() {
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(30, 30, 30));
        setContentPane(desktopPane);

        menuBar = new JMenuBar();

        // Menú Ventas y Facturación
        menuVentas = new JMenu("Ventas");
        itemFacturacion = new JMenuItem("Módulo de Facturación");
        menuVentas.add(itemFacturacion);
        menuBar.add(menuVentas);

        // Menú Catálogos
        menuCatalogos = new JMenu("Catálogos");
        itemClientes = new JMenuItem("Consultar Clientes");
        itemProductos = new JMenuItem("Consultar Productos");
        menuCatalogos.add(itemClientes);
        menuCatalogos.add(itemProductos);
        menuBar.add(menuCatalogos);

        // Menú Archivo
        menuArchivo = new JMenu("Archivo");
        itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);
        menuBar.add(menuArchivo);

        setJMenuBar(menuBar);
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    public JMenuItem getItemFacturacion() {
        return itemFacturacion;
    }

    public JMenuItem getItemClientes() {
        return itemClientes;
    }

    public JMenuItem getItemProductos() {
        return itemProductos;
    }

    public JMenuItem getItemSalir() {
        return itemSalir;
    }
}
