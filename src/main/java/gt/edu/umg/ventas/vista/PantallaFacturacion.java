package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.FacturaController;
import gt.edu.umg.ventas.modelo.DetalleFactura;
import gt.edu.umg.ventas.modelo.EstadoFactura;
import gt.edu.umg.ventas.modelo.Factura;
import gt.edu.umg.ventas.modelo.MetodoPago;
import gt.edu.umg.ventas.util.FormatoMoneda;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Vista de facturación (PantallaFacturacion / Boundary) según el diagrama UML.
 * Diseñada para operar dentro de un contenedor MDI con Java Swing y FlatLaf.
 */
public class PantallaFacturacion extends JInternalFrame {

    private FacturaController controller;

    // Componentes Cabecera
    private JLabel lblNumero;
    private JLabel lblEstado;
    private JLabel lblFecha;
    private JTextField txtNit;
    private JTextField txtClienteNombre;
    private JTextField txtDireccion;
    private JButton btnBuscarCliente;

    // Componentes Detalle Producto
    private JTextField txtCodigoProd;
    private JTextField txtNombreProd;
    private JTextField txtPrecioProd;
    private JTextField txtStockProd;
    private JTextField txtCantidadProd;
    private JTextField txtDescuentoProd;
    private JButton btnBuscarProd;
    private JButton btnAgregarProd;
    private JButton btnEliminarProd;

    // Tabla de Detalles
    private JTable tblDetalle;
    private DefaultTableModel modeloTabla;

    // Totales
    private JLabel lblSubtotal;
    private JLabel lblImpuesto;
    private JLabel lblTotal;
    private JLabel lblPagado;
    private JLabel lblSaldo;

    // Pagos
    private JComboBox<MetodoPago> cmbMetodoPago;
    private JTextField txtMontoPago;
    private JTextField txtReferenciaPago;
    private JButton btnRegistrarPago;

    // Botones de acción principales
    private JButton btnNuevaFactura;
    private JButton btnEmitir;
    private JButton btnConsultar;
    private JButton btnAnular;

    public PantallaFacturacion() {
        super("Módulo de Facturación y Ventas", true, true, true, true);
        initComponents();
        setSize(920, 680);
        setMinimumSize(new Dimension(850, 600));
    }

    public void setController(FacturaController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(new EmptyBorder(10, 10, 5, 10));

        // 1. Barra de información de Factura (Número, Estado, Fecha)
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelInfo.setBorder(BorderFactory.createTitledBorder("Información del Documento"));

        lblNumero = new JLabel("Factura: [NUEVA]");
        lblNumero.setFont(lblNumero.getFont().deriveFont(Font.BOLD, 14f));

        lblEstado = new JLabel("Estado: BORRADOR");
        lblEstado.setFont(lblEstado.getFont().deriveFont(Font.BOLD, 13f));
        lblEstado.setForeground(new Color(60, 179, 113));

        lblFecha = new JLabel("Fecha: Hoy");

        panelInfo.add(lblNumero);
        panelInfo.add(lblEstado);
        panelInfo.add(lblFecha);

        // 2. Datos del Cliente
        JPanel panelCliente = new JPanel(new GridBagLayout());
        panelCliente.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelCliente.add(new JLabel("NIT:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3;
        txtNit = new JTextField(10);
        panelCliente.add(txtNit, gbc);

        gbc.gridx = 2; gbc.weightx = 0.0;
        btnBuscarCliente = new JButton("🔍 Buscar");
        panelCliente.add(btnBuscarCliente, gbc);

        gbc.gridx = 3; gbc.weightx = 0.0;
        panelCliente.add(new JLabel("Nombre / Razón:"), gbc);
        gbc.gridx = 4; gbc.weightx = 0.7;
        txtClienteNombre = new JTextField(20);
        panelCliente.add(txtClienteNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panelCliente.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 4; gbc.weightx = 1.0;
        txtDireccion = new JTextField(30);
        panelCliente.add(txtDireccion, gbc);

        panelSuperior.add(panelInfo, BorderLayout.NORTH);
        panelSuperior.add(panelCliente, BorderLayout.SOUTH);
        add(panelSuperior, BorderLayout.NORTH);

        // 3. Panel Central: Captura de Productos + Tabla de Detalles
        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));
        panelCentro.setBorder(new EmptyBorder(0, 10, 5, 10));

        // Panel selección de producto
        JPanel panelProducto = new JPanel(new GridBagLayout());
        panelProducto.setBorder(BorderFactory.createTitledBorder("Selección y Captura de Productos"));
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(4, 4, 4, 4);
        gbcP.fill = GridBagConstraints.HORIZONTAL;

        gbcP.gridx = 0; gbcP.gridy = 0;
        panelProducto.add(new JLabel("Código:"), gbcP);
        gbcP.gridx = 1; gbcP.weightx = 0.2;
        txtCodigoProd = new JTextField(8);
        panelProducto.add(txtCodigoProd, gbcP);

        gbcP.gridx = 2; gbcP.weightx = 0.0;
        btnBuscarProd = new JButton("🔍");
        panelProducto.add(btnBuscarProd, gbcP);

        gbcP.gridx = 3; gbcP.weightx = 0.0;
        panelProducto.add(new JLabel("Descripción:"), gbcP);
        gbcP.gridx = 4; gbcP.weightx = 0.5;
        txtNombreProd = new JTextField(16);
        txtNombreProd.setEditable(false);
        panelProducto.add(txtNombreProd, gbcP);

        gbcP.gridx = 5; gbcP.weightx = 0.0;
        panelProducto.add(new JLabel("Precio:"), gbcP);
        gbcP.gridx = 6; gbcP.weightx = 0.2;
        txtPrecioProd = new JTextField(6);
        txtPrecioProd.setEditable(false);
        panelProducto.add(txtPrecioProd, gbcP);

        gbcP.gridx = 7; gbcP.weightx = 0.0;
        panelProducto.add(new JLabel("Stock:"), gbcP);
        gbcP.gridx = 8; gbcP.weightx = 0.15;
        txtStockProd = new JTextField(5);
        txtStockProd.setEditable(false);
        panelProducto.add(txtStockProd, gbcP);

        // Fila 2 de captura: Cantidad, Descuento y botones Agregar/Eliminar
        gbcP.gridx = 0; gbcP.gridy = 1; gbcP.weightx = 0.0;
        panelProducto.add(new JLabel("Cantidad:"), gbcP);
        gbcP.gridx = 1; gbcP.weightx = 0.2;
        txtCantidadProd = new JTextField("1", 5);
        panelProducto.add(txtCantidadProd, gbcP);

        gbcP.gridx = 2; gbcP.weightx = 0.0;
        panelProducto.add(new JLabel("Desc. (Q):"), gbcP);
        gbcP.gridx = 3; gbcP.weightx = 0.2;
        txtDescuentoProd = new JTextField("0.00", 6);
        panelProducto.add(txtDescuentoProd, gbcP);

        gbcP.gridx = 4; gbcP.gridwidth = 2; gbcP.weightx = 0.3;
        btnAgregarProd = new JButton("➕ Agregar Producto");
        btnAgregarProd.setFont(btnAgregarProd.getFont().deriveFont(Font.BOLD));
        panelProducto.add(btnAgregarProd, gbcP);

        gbcP.gridx = 6; gbcP.gridwidth = 3; gbcP.weightx = 0.3;
        btnEliminarProd = new JButton("➖ Quitar Selección");
        panelProducto.add(btnEliminarProd, gbcP);

        panelCentro.add(panelProducto, BorderLayout.NORTH);

        // Tabla de detalles
        modeloTabla = new DefaultTableModel(
                new Object[]{"Código", "Descripción", "Cantidad", "Precio Unit.", "Descuento", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDetalle = new JTable(modeloTabla);
        tblDetalle.setRowHeight(24);
        tblDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer derechaRenderer = new DefaultTableCellRenderer();
        derechaRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblDetalle.getColumnModel().getColumn(2).setCellRenderer(derechaRenderer);
        tblDetalle.getColumnModel().getColumn(3).setCellRenderer(derechaRenderer);
        tblDetalle.getColumnModel().getColumn(4).setCellRenderer(derechaRenderer);
        tblDetalle.getColumnModel().getColumn(5).setCellRenderer(derechaRenderer);

        JScrollPane scrollTabla = new JScrollPane(tblDetalle);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);

        // 4. Panel Inferior: Totales, Pagos y Botones de Acción
        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setBorder(new EmptyBorder(5, 10, 10, 10));

        // Panel de Pagos
        JPanel panelPago = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelPago.setBorder(BorderFactory.createTitledBorder("Registro de Pago"));
        cmbMetodoPago = new JComboBox<>(MetodoPago.values());
        txtMontoPago = new JTextField(8);
        txtReferenciaPago = new JTextField(10);
        btnRegistrarPago = new JButton("💳 Registrar Pago");

        panelPago.add(new JLabel("Método:"));
        panelPago.add(cmbMetodoPago);
        panelPago.add(new JLabel("Monto:"));
        panelPago.add(txtMontoPago);
        panelPago.add(new JLabel("Ref:"));
        panelPago.add(txtReferenciaPago);
        panelPago.add(btnRegistrarPago);

        // Panel de Totales
        JPanel panelTotales = new JPanel(new GridLayout(5, 1, 2, 4));
        panelTotales.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Resumen de Totales"),
                new EmptyBorder(5, 15, 5, 15)));

        lblSubtotal = new JLabel("Subtotal: Q 0.00", SwingConstants.RIGHT);
        lblImpuesto = new JLabel("IVA (12%): Q 0.00", SwingConstants.RIGHT);
        lblTotal = new JLabel("Total: Q 0.00", SwingConstants.RIGHT);
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 15f));
        lblPagado = new JLabel("Pagado: Q 0.00", SwingConstants.RIGHT);
        lblSaldo = new JLabel("Saldo: Q 0.00", SwingConstants.RIGHT);
        lblSaldo.setFont(lblSaldo.getFont().deriveFont(Font.BOLD, 13f));

        panelTotales.add(lblSubtotal);
        panelTotales.add(lblImpuesto);
        panelTotales.add(lblTotal);
        panelTotales.add(lblPagado);
        panelTotales.add(lblSaldo);

        // Panel de Botones de Acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnNuevaFactura = new JButton("📄 Nueva Factura");
        btnEmitir = new JButton("🚀 Emitir Factura");
        btnEmitir.setFont(btnEmitir.getFont().deriveFont(Font.BOLD, 13f));
        btnConsultar = new JButton("🔎 Consultar Factura");
        btnAnular = new JButton("❌ Anular Factura");

        panelBotones.add(btnNuevaFactura);
        panelBotones.add(btnConsultar);
        panelBotones.add(btnAnular);
        panelBotones.add(btnEmitir);

        JPanel panelSurInterno = new JPanel(new BorderLayout());
        panelSurInterno.add(panelPago, BorderLayout.WEST);
        panelSurInterno.add(panelTotales, BorderLayout.EAST);

        panelInferior.add(panelSurInterno, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        add(panelInferior, BorderLayout.SOUTH);
    }

    /**
     * Implementación del método UML: solicita la emisión de la factura al controlador.
     */
    public void solicitarEmision() {
        if (controller != null) {
            controller.solicitarEmision();
        }
    }

    /**
     * Implementación del método UML: presenta una factura completa en la vista.
     *
     * @param factura Factura a mostrar
     */
    public void mostrarFactura(Factura factura) {
        if (factura == null) {
            limpiarFormulario();
            return;
        }

        lblNumero.setText("Factura: " + (factura.getNumero() != null ? factura.getNumero() : "[NUEVA]"));
        lblEstado.setText("Estado: " + factura.getEstado().name());
        if (factura.getEstado() == EstadoFactura.EMITIDA) {
            lblEstado.setForeground(new Color(30, 144, 255));
        } else if (factura.getEstado() == EstadoFactura.PAGADA) {
            lblEstado.setForeground(new Color(46, 139, 87));
        } else if (factura.getEstado() == EstadoFactura.ANULADA) {
            lblEstado.setForeground(new Color(220, 20, 60));
        } else {
            lblEstado.setForeground(new Color(218, 165, 32));
        }

        if (factura.getFechaHora() != null) {
            lblFecha.setText("Fecha: " + factura.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        if (factura.getCliente() != null) {
            txtNit.setText(factura.getCliente().getNit());
            txtClienteNombre.setText(factura.getCliente().getNombre());
            txtDireccion.setText(factura.getCliente().getDireccion());
        } else {
            txtNit.setText("CF");
            txtClienteNombre.setText("Consumidor Final");
            txtDireccion.setText("Ciudad");
        }

        modeloTabla.setRowCount(0);
        for (DetalleFactura d : factura.getDetalles()) {
            modeloTabla.addRow(new Object[]{
                    d.getProducto().getCodigo(),
                    d.getProducto().getNombre(),
                    d.getCantidad().toString(),
                    FormatoMoneda.formatear(d.getPrecioUnitario()),
                    FormatoMoneda.formatear(d.getDescuento()),
                    FormatoMoneda.formatear(d.calcularSubtotal())
            });
        }

        BigDecimal sub = factura.calcularSubtotal();
        BigDecimal imp = factura.calcularImpuesto();
        BigDecimal tot = factura.calcularTotal();
        BigDecimal pag = factura.obtenerTotalPagado();
        BigDecimal sal = tot.subtract(pag);
        if (sal.compareTo(BigDecimal.ZERO) < 0) {
            sal = BigDecimal.ZERO;
        }

        lblSubtotal.setText("Subtotal: " + FormatoMoneda.formatear(sub));
        lblImpuesto.setText("IVA (12%): " + FormatoMoneda.formatear(imp));
        lblTotal.setText("Total: " + FormatoMoneda.formatear(tot));
        lblPagado.setText("Pagado: " + FormatoMoneda.formatear(pag));
        lblSaldo.setText("Saldo: " + FormatoMoneda.formatear(sal));

        // Configurar montos por defecto para pago
        txtMontoPago.setText(sal.compareTo(BigDecimal.ZERO) > 0 ? sal.toString() : "0.00");
    }

    public void limpiarFormulario() {
        lblNumero.setText("Factura: [NUEVA]");
        lblEstado.setText("Estado: BORRADOR");
        lblEstado.setForeground(new Color(218, 165, 32));
        lblFecha.setText("Fecha: Hoy");
        txtNit.setText("");
        txtClienteNombre.setText("");
        txtDireccion.setText("");
        limpiarCamposProducto();
        modeloTabla.setRowCount(0);
        lblSubtotal.setText("Subtotal: Q 0.00");
        lblImpuesto.setText("IVA (12%): Q 0.00");
        lblTotal.setText("Total: Q 0.00");
        lblPagado.setText("Pagado: Q 0.00");
        lblSaldo.setText("Saldo: Q 0.00");
        txtMontoPago.setText("");
        txtReferenciaPago.setText("");
    }

    public void limpiarCamposProducto() {
        txtCodigoProd.setText("");
        txtNombreProd.setText("");
        txtPrecioProd.setText("");
        txtStockProd.setText("");
        txtCantidadProd.setText("1");
        txtDescuentoProd.setText("0.00");
        txtCodigoProd.requestFocus();
    }

    // --- GETTERS DE COMPONENTES ---

    public JTextField getTxtNit() { return txtNit; }
    public JTextField getTxtClienteNombre() { return txtClienteNombre; }
    public JTextField getTxtDireccion() { return txtDireccion; }
    public JButton getBtnBuscarCliente() { return btnBuscarCliente; }

    public JTextField getTxtCodigoProd() { return txtCodigoProd; }
    public JTextField getTxtNombreProd() { return txtNombreProd; }
    public JTextField getTxtPrecioProd() { return txtPrecioProd; }
    public JTextField getTxtStockProd() { return txtStockProd; }
    public JTextField getTxtCantidadProd() { return txtCantidadProd; }
    public JTextField getTxtDescuentoProd() { return txtDescuentoProd; }
    public JButton getBtnBuscarProd() { return btnBuscarProd; }
    public JButton getBtnAgregarProd() { return btnAgregarProd; }
    public JButton getBtnEliminarProd() { return btnEliminarProd; }

    public JTable getTblDetalle() { return tblDetalle; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }

    public JComboBox<MetodoPago> getCmbMetodoPago() { return cmbMetodoPago; }
    public JTextField getTxtMontoPago() { return txtMontoPago; }
    public JTextField getTxtReferenciaPago() { return txtReferenciaPago; }
    public JButton getBtnRegistrarPago() { return btnRegistrarPago; }

    public JButton getBtnNuevaFactura() { return btnNuevaFactura; }
    public JButton getBtnEmitir() { return btnEmitir; }
    public JButton getBtnConsultar() { return btnConsultar; }
    public JButton getBtnAnular() { return btnAnular; }
}
