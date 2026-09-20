package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.OrdenVentaController;
import gt.edu.umg.ventas.modelo.Cliente;
import gt.edu.umg.ventas.modelo.DetalleOrdenVenta;
import gt.edu.umg.ventas.modelo.OrdenVenta;
import gt.edu.umg.ventas.modelo.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class FrmOrdenVenta extends JInternalFrame {

    private final OrdenVentaController controller;
    private final List<DetalleOrdenVenta> listaDetalles;

    private JComboBox<Cliente> cmbCliente;
    private JComboBox<Producto> cmbProducto;
    private JTextField txtCantidad;
    private JTextField txtObservaciones;
    private JTable tblDetalles;
    private DefaultTableModel modeloTabla;

    private JLabel lblSubtotalValor;
    private JLabel lblImpuestoValor;
    private JLabel lblTotalValor;

    public FrmOrdenVenta() {
        super("Nueva Orden de Venta", true, true, true, true);
        this.controller = new OrdenVentaController();
        this.listaDetalles = new ArrayList<>();

        setSize(850, 560);
        setMinimumSize(new Dimension(750, 480));
        initComponents();
        cargarCatalogos();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Selección de Cliente, Producto y Cantidad
        JPanel pnlNorte = new JPanel(new GridBagLayout());
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Datos de la Orden de Venta"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Cliente
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.gridwidth = 3;
        cmbCliente = new JComboBox<>();
        pnlNorte.add(cmbCliente, gbc);

        // Fila 1: Producto, Cantidad y Botón Agregar
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Producto:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.6;
        cmbProducto = new JComboBox<>();
        pnlNorte.add(cmbProducto, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.2;
        txtCantidad = new JTextField("1", 6);
        pnlNorte.add(txtCantidad, gbc);

        gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0.0;
        JButton btnAgregar = new JButton("➕ Agregar Detalle");
        btnAgregar.addActionListener(e -> agregarDetalle());
        pnlNorte.add(btnAgregar, gbc);

        // Fila 2: Observaciones
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 4;
        txtObservaciones = new JTextField();
        pnlNorte.add(txtObservaciones, gbc);

        add(pnlNorte, BorderLayout.NORTH);

        // Panel Central: Tabla de Detalles
        String[] columnas = {"Código", "Producto", "Cantidad", "Precio Unitario (Q)", "Subtotal (Q)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblDetalles = new JTable(modeloTabla);
        tblDetalles.setRowHeight(24);
        add(new JScrollPane(tblDetalles), BorderLayout.CENTER);

        // Panel Inferior: Totales y Acciones
        JPanel pnlSur = new JPanel(new BorderLayout(10, 10));
        pnlSur.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        // Subpanel Izquierdo: Botón Quitar Detalle
        JPanel pnlAccionesTabla = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnQuitar = new JButton("🗑 Quitar Seleccionado");
        btnQuitar.addActionListener(e -> quitarDetalle());
        pnlAccionesTabla.add(btnQuitar);
        pnlSur.add(pnlAccionesTabla, BorderLayout.WEST);

        // Subpanel Derecho: Totales y Guardar
        JPanel pnlTotales = new JPanel(new GridBagLayout());
        GridBagConstraints gbcT = new GridBagConstraints();
        gbcT.insets = new Insets(2, 10, 2, 10);
        gbcT.anchor = GridBagConstraints.EAST;

        gbcT.gridx = 0; gbcT.gridy = 0;
        pnlTotales.add(new JLabel("Subtotal:"), gbcT);
        gbcT.gridx = 1; gbcT.gridy = 0;
        lblSubtotalValor = new JLabel("Q 0.00");
        lblSubtotalValor.setFont(lblSubtotalValor.getFont().deriveFont(Font.BOLD));
        pnlTotales.add(lblSubtotalValor, gbcT);

        gbcT.gridx = 0; gbcT.gridy = 1;
        pnlTotales.add(new JLabel("IVA (12%):"), gbcT);
        gbcT.gridx = 1; gbcT.gridy = 1;
        lblImpuestoValor = new JLabel("Q 0.00");
        lblImpuestoValor.setFont(lblImpuestoValor.getFont().deriveFont(Font.BOLD));
        pnlTotales.add(lblImpuestoValor, gbcT);

        gbcT.gridx = 0; gbcT.gridy = 2;
        pnlTotales.add(new JLabel("Total:"), gbcT);
        gbcT.gridx = 1; gbcT.gridy = 2;
        lblTotalValor = new JLabel("Q 0.00");
        lblTotalValor.setFont(lblTotalValor.getFont().deriveFont(Font.BOLD, 14f));
        lblTotalValor.setForeground(new Color(0, 120, 215));
        pnlTotales.add(lblTotalValor, gbcT);

        gbcT.gridx = 0; gbcT.gridy = 3; gbcT.gridwidth = 2;
        JButton btnGuardar = new JButton("💾 Guardar Orden");
        btnGuardar.setFont(btnGuardar.getFont().deriveFont(Font.BOLD, 13f));
        btnGuardar.addActionListener(e -> guardarOrden());
        pnlTotales.add(btnGuardar, gbcT);

        pnlSur.add(pnlTotales, BorderLayout.EAST);
        add(pnlSur, BorderLayout.SOUTH);
    }

    private void cargarCatalogos() {
        try {
            cmbCliente.removeAllItems();
            List<Cliente> clientes = controller.obtenerClientes();
            for (Cliente c : clientes) {
                cmbCliente.addItem(c);
            }

            cmbProducto.removeAllItems();
            List<Producto> productos = controller.obtenerProductosActivos();
            for (Producto p : productos) {
                cmbProducto.addItem(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar catálogos desde la base de datos: " + e.getMessage(),
                    "Error de Carga", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarDetalle() {
        Producto prod = (Producto) cmbProducto.getSelectedItem();
        if (prod == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero mayor a cero.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar si el producto ya está en los detalles agregados
        for (DetalleOrdenVenta d : listaDetalles) {
            if (d.getProducto().getIdProducto() == prod.getIdProducto()) {
                JOptionPane.showMessageDialog(this, "El producto ya se encuentra en la orden. Modifique la línea existente si desea cambiar la cantidad.",
                        "Producto Repetido", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        BigDecimal precio = prod.getPrecioVenta();
        BigDecimal subtotal = precio.multiply(new BigDecimal(cantidad)).setScale(2, RoundingMode.HALF_UP);

        DetalleOrdenVenta det = new DetalleOrdenVenta();
        det.setProducto(prod);
        det.setCantidad(cantidad);
        det.setPrecioUnitario(precio);
        det.setSubtotal(subtotal);

        listaDetalles.add(det);
        modeloTabla.addRow(new Object[]{
                prod.getCodigo(),
                prod.getNombre(),
                cantidad,
                precio.toString(),
                subtotal.toString()
        });

        recalcularTotales();
        txtCantidad.setText("1");
    }

    private void quitarDetalle() {
        int fila = tblDetalles.getSelectedRow();
        if (fila >= 0) {
            listaDetalles.remove(fila);
            modeloTabla.removeRow(fila);
            recalcularTotales();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para quitar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void recalcularTotales() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleOrdenVenta d : listaDetalles) {
            subtotal = subtotal.add(d.getSubtotal());
        }
        BigDecimal impuesto = subtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(impuesto).setScale(2, RoundingMode.HALF_UP);

        lblSubtotalValor.setText("Q " + subtotal);
        lblImpuestoValor.setText("Q " + impuesto);
        lblTotalValor.setText("Q " + total);
    }

    private void guardarOrden() {
        Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (listaDetalles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto a la orden.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            OrdenVenta ordenGuardada = controller.guardarOrden(cliente, listaDetalles, txtObservaciones.getText().trim());
            JOptionPane.showMessageDialog(this,
                    "Orden " + ordenGuardada.getNumeroOrden() + " creada correctamente\nID de Orden: " + ordenGuardada.getId(),
                    "Orden de Venta Guardada", JOptionPane.INFORMATION_MESSAGE);
            limpiarFormulario();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar orden: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        listaDetalles.clear();
        modeloTabla.setRowCount(0);
        txtObservaciones.setText("");
        txtCantidad.setText("1");
        recalcularTotales();
    }
}
