package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.DespachoController;
import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.Despacho;
import gt.edu.umg.ventas.modelo.DetalleDespacho;
import gt.edu.umg.ventas.modelo.DetalleOrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVenta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FrmDespacho extends JInternalFrame {

    private final DespachoController controller;
    private final List<DetalleDespacho> listaDetallesDespacho;

    private JComboBox<OrdenVentaItem> cmbOrdenesPendientes;
    private JComboBox<Bodega> cmbBodega;
    private JTable tblDetalles;
    private DefaultTableModel modeloTabla;
    private JLabel lblEstadoOrden;

    // Clase auxiliar para desplegar de forma amigable la orden en el JComboBox
    private static class OrdenVentaItem {
        private final OrdenVenta orden;

        public OrdenVentaItem(OrdenVenta orden) {
            this.orden = orden;
        }

        public OrdenVenta getOrden() {
            return orden;
        }

        @Override
        public String toString() {
            if (orden == null) return "-- Seleccione una Orden Pendiente --";
            String cliente = orden.getCliente() != null ? orden.getCliente().getNombre() : "Sin Cliente";
            return orden.getNumeroOrden() + " | " + cliente + " | Q " + orden.getTotal();
        }
    }

    public FrmDespacho() {
        super("Módulo de Despacho de Mercadería", true, true, true, true);
        this.controller = new DespachoController();
        this.listaDetallesDespacho = new ArrayList<>();

        setSize(850, 520);
        setMinimumSize(new Dimension(750, 450));
        initComponents();
        cargarDatosIniciales();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Selección de Orden y Bodega
        JPanel pnlNorte = new JPanel(new GridBagLayout());
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Gestión de Despacho"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Selección de Orden Pendiente
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Orden Pendiente:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cmbOrdenesPendientes = new JComboBox<>();
        cmbOrdenesPendientes.addActionListener(e -> onOrdenSeleccionada());
        pnlNorte.add(cmbOrdenesPendientes, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> cargarDatosIniciales());
        pnlNorte.add(btnRefrescar, gbc);

        // Fila 1: Bodega y Estado
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Bodega de Salida:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        cmbBodega = new JComboBox<>();
        pnlNorte.add(cmbBodega, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        lblEstadoOrden = new JLabel("Estado: -");
        lblEstadoOrden.setFont(lblEstadoOrden.getFont().deriveFont(Font.BOLD));
        pnlNorte.add(lblEstadoOrden, gbc);

        add(pnlNorte, BorderLayout.NORTH);

        // Panel Central: Tabla de Detalles (Producto, Solicitado, Despachado)
        String[] columnas = {"ID Producto", "Código", "Producto", "Cantidad Solicitada", "Cantidad a Despachar"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo la columna 'Cantidad a Despachar' es editable
                return column == 4;
            }
        };
        tblDetalles = new JTable(modeloTabla);
        tblDetalles.setRowHeight(24);
        add(new JScrollPane(tblDetalles), BorderLayout.CENTER);

        // Panel Inferior: Botón Confirmar Despacho
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnConfirmar = new JButton("📦 Confirmar Despacho");
        btnConfirmar.setFont(btnConfirmar.getFont().deriveFont(Font.BOLD, 13f));
        btnConfirmar.addActionListener(e -> confirmarDespacho());
        pnlSur.add(btnConfirmar);

        add(pnlSur, BorderLayout.SOUTH);
    }

    public void cargarDatosIniciales() {
        try {
            // Cargar Bodegas
            cmbBodega.removeAllItems();
            List<Bodega> bodegas = controller.obtenerBodegas();
            for (Bodega b : bodegas) {
                cmbBodega.addItem(b);
            }

            // Cargar Órdenes Pendientes
            cmbOrdenesPendientes.removeAllItems();
            cmbOrdenesPendientes.addItem(new OrdenVentaItem(null)); // Placeholder
            List<OrdenVenta> pendientes = controller.obtenerOrdenesPendientes();
            for (OrdenVenta ov : pendientes) {
                cmbOrdenesPendientes.addItem(new OrdenVentaItem(ov));
            }

            modeloTabla.setRowCount(0);
            listaDetallesDespacho.clear();
            lblEstadoOrden.setText("Estado: -");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar órdenes pendientes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onOrdenSeleccionada() {
        OrdenVentaItem item = (OrdenVentaItem) cmbOrdenesPendientes.getSelectedItem();
        if (item == null || item.getOrden() == null) {
            modeloTabla.setRowCount(0);
            listaDetallesDespacho.clear();
            lblEstadoOrden.setText("Estado: -");
            return;
        }

        try {
            OrdenVenta ordenCompleta = controller.obtenerOrdenConDetalles(item.getOrden().getId());
            if (ordenCompleta == null) {
                return;
            }

            lblEstadoOrden.setText("Estado: " + ordenCompleta.getEstado());
            modeloTabla.setRowCount(0);
            listaDetallesDespacho.clear();

            for (DetalleOrdenVenta det : ordenCompleta.getDetalles()) {
                DetalleDespacho dd = new DetalleDespacho();
                dd.setProducto(det.getProducto());
                dd.setCantidadSolicitada(det.getCantidad());
                dd.setCantidadDespachada(det.getCantidad()); // Default: despachar todo lo solicitado

                listaDetallesDespacho.add(dd);

                modeloTabla.addRow(new Object[]{
                        det.getProducto().getIdProducto(),
                        det.getProducto().getCodigo(),
                        det.getProducto().getNombre(),
                        det.getCantidad(),
                        det.getCantidad()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar detalles de la orden: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmarDespacho() {
        OrdenVentaItem item = (OrdenVentaItem) cmbOrdenesPendientes.getSelectedItem();
        if (item == null || item.getOrden() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden de venta pendiente.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Bodega bodega = (Bodega) cmbBodega.getSelectedItem();
        if (bodega == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una bodega de salida.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (listaDetallesDespacho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La orden no tiene productos para despachar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Actualizar cantidades despachadas desde la tabla
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            try {
                int cantDesp = Integer.parseInt(modeloTabla.getValueAt(i, 4).toString().trim());
                int cantSol = Integer.parseInt(modeloTabla.getValueAt(i, 3).toString().trim());
                if (cantDesp <= 0) {
                    JOptionPane.showMessageDialog(this, "La cantidad a despachar en la fila " + (i + 1) + " debe ser mayor a cero.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (cantDesp > cantSol) {
                    JOptionPane.showMessageDialog(this, "La cantidad a despachar en la fila " + (i + 1) + " supera la solicitada.",
                            "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                listaDetallesDespacho.get(i).setCantidadDespachada(cantDesp);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cantidad inválida en la fila " + (i + 1), "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            Despacho despacho = controller.confirmarDespacho(item.getOrden(), bodega, listaDetallesDespacho);
            JOptionPane.showMessageDialog(this,
                    "Despacho " + despacho.getNumeroDespacho() + " CONFIRMADO exitosamente.\n"
                    + "Inventario descontado de " + bodega.getNombre() + ".\n"
                    + "Orden " + item.getOrden().getNumeroOrden() + " actualizada a COMPLETADA.",
                    "Despacho Exitoso", JOptionPane.INFORMATION_MESSAGE);

            cargarDatosIniciales();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al confirmar despacho: " + ex.getMessage(),
                    "Fallo en Despacho (Rollback)", JOptionPane.ERROR_MESSAGE);
        }
    }
}
