package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.ConsultaFacturaController;
import gt.edu.umg.ventas.modelo.FacturaResumen;
import gt.edu.umg.ventas.servicio.ConsultaFacturaService.LineaFactura;
import gt.edu.umg.ventas.util.Dialogos;
import gt.edu.umg.ventas.util.FormatoMoneda;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/** Consulta de facturas emitidas: filtros por fechas, cliente, No. de orden y estados. */
public class FrmConsultaFacturas extends JInternalFrame {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ConsultaFacturaController controller = new ConsultaFacturaController();

    private final JCheckBox chkFechas = new JCheckBox("Rango de fechas", true);
    private final JSpinner spinDesde = new JSpinner(new SpinnerDateModel());
    private final JSpinner spinHasta = new JSpinner(new SpinnerDateModel());
    private final JTextField txtCliente = new JTextField(18);
    private final JTextField txtOrden = new JTextField(14);
    private final JComboBox<String> cmbEstadoFactura =
            new JComboBox<>(new String[]{"TODOS", "EMITIDA", "PAGADA", "ANULADA"});
    private final JComboBox<String> cmbEstadoOrden =
            new JComboBox<>(new String[]{"TODOS", "PENDIENTE", "COMPLETADA", "CANCELADA"});
    private final JLabel lblResumen = new JLabel("Facturas: 0");
    private DefaultTableModel tableModel;
    private JTable table;
    private List<FacturaResumen> resultados = List.of();

    public FrmConsultaFacturas() {
        super("Consulta de Facturas Emitidas", true, true, true, true);
        setSize(1100, 520);
        configurarFechas();

        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        // Fila 1: fechas y cliente
        g.gridy = 0;
        g.gridx = 0; filtros.add(chkFechas, g);
        g.gridx = 1; filtros.add(new JLabel("Desde:"), g);
        g.gridx = 2; filtros.add(spinDesde, g);
        g.gridx = 3; filtros.add(new JLabel("Hasta:"), g);
        g.gridx = 4; filtros.add(spinHasta, g);
        g.gridx = 5; filtros.add(new JLabel("Cliente (NIT o nombre):"), g);
        g.gridx = 6; filtros.add(txtCliente, g);

        // Fila 2: orden, estados y botones
        g.gridy = 1;
        g.gridx = 1; filtros.add(new JLabel("No. Orden:"), g);
        g.gridx = 2; filtros.add(txtOrden, g);
        g.gridx = 3; filtros.add(new JLabel("Estado orden:"), g);
        g.gridx = 4; filtros.add(cmbEstadoOrden, g);
        g.gridx = 5; filtros.add(new JLabel("Estado factura:"), g);
        g.gridx = 6; filtros.add(cmbEstadoFactura, g);

        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnDetalle = new JButton("Ver detalle");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        botones.add(btnBuscar);
        botones.add(btnLimpiar);
        botones.add(btnDetalle);
        g.gridy = 2; g.gridx = 0; g.gridwidth = 7;
        filtros.add(botones, g);

        String[] columnas = {"No. Factura", "Fecha", "NIT", "Cliente", "No. Orden",
                "Estado orden", "Estado factura", "Total", "Pagado", "Saldo"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        int[] anchos = {300, 120, 100, 200, 150, 110, 110, 100, 100, 100};
        for (int i = 0; i < anchos.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c = 7; c <= 9; c++) table.getColumnModel().getColumn(c).setCellRenderer(derecha);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pie.add(lblResumen);

        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);

        chkFechas.addActionListener(e -> {
            spinDesde.setEnabled(chkFechas.isSelected());
            spinHasta.setEnabled(chkFechas.isSelected());
        });
        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnDetalle.addActionListener(e -> mostrarDetalle());
        txtCliente.addActionListener(e -> buscar());
        txtOrden.addActionListener(e -> buscar());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) mostrarDetalle();
            }
        });

        // Por defecto: último mes, para no traer toda la historia.
        spinDesde.setValue(java.sql.Date.valueOf(LocalDate.now().minusMonths(1)));
        spinHasta.setValue(new Date());
    }

    private void configurarFechas() {
        for (JSpinner s : new JSpinner[]{spinDesde, spinHasta}) {
            JSpinner.DateEditor de = new JSpinner.DateEditor(s, "dd/MM/yyyy");
            de.getFormat().setLenient(false);
            s.setEditor(de);
        }
    }

    private static LocalDate aLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void buscar() {
        try {
            LocalDate desde = null;
            LocalDate hasta = null;
            if (chkFechas.isSelected()) {
                spinDesde.commitEdit();
                spinHasta.commitEdit();
                desde = aLocalDate((Date) spinDesde.getValue());
                hasta = aLocalDate((Date) spinHasta.getValue());
            }
            resultados = controller.buscar(desde, hasta, txtCliente.getText(), txtOrden.getText(),
                    (String) cmbEstadoFactura.getSelectedItem(), (String) cmbEstadoOrden.getSelectedItem());
            llenarTabla();
            if (resultados.isEmpty()) {
                Dialogos.showMessageDialog(this, "No se encontraron facturas con esos filtros.");
            }
        } catch (ParseException ex) {
            Dialogos.showMessageDialog(this, "Ingrese fechas válidas en formato dd/MM/aaaa.",
                    "Fechas inválidas", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            Dialogos.showMessageDialog(this, ex.getMessage(), "Filtros inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            Dialogos.showMessageDialog(this, ex.getMessage(), "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void llenarTabla() {
        tableModel.setRowCount(0);
        BigDecimal totalFacturado = BigDecimal.ZERO;
        BigDecimal totalSaldo = BigDecimal.ZERO;
        for (FacturaResumen r : resultados) {
            tableModel.addRow(new Object[]{
                    r.numeroFactura(), r.fecha().format(FECHA_HORA), r.nit(), r.nombreCliente(),
                    r.numeroOrden(), r.estadoOrden(), r.estadoFactura(),
                    FormatoMoneda.formatear(r.total()), FormatoMoneda.formatear(r.pagado()),
                    FormatoMoneda.formatear(r.saldo())});
            if (!"ANULADA".equals(r.estadoFactura())) {
                totalFacturado = totalFacturado.add(r.total());
                totalSaldo = totalSaldo.add(r.saldo());
            }
        }
        lblResumen.setText("Facturas: " + resultados.size()
                + "   |   Total facturado (sin anuladas): " + FormatoMoneda.formatear(totalFacturado)
                + "   |   Saldo pendiente: " + FormatoMoneda.formatear(totalSaldo));
    }

    private void limpiar() {
        chkFechas.setSelected(true);
        spinDesde.setEnabled(true);
        spinHasta.setEnabled(true);
        spinDesde.setValue(java.sql.Date.valueOf(LocalDate.now().minusMonths(1)));
        spinHasta.setValue(new Date());
        txtCliente.setText("");
        txtOrden.setText("");
        cmbEstadoFactura.setSelectedIndex(0);
        cmbEstadoOrden.setSelectedIndex(0);
        resultados = List.of();
        tableModel.setRowCount(0);
        lblResumen.setText("Facturas: 0");
    }

    private void mostrarDetalle() {
        int fila = table.getSelectedRow();
        if (fila < 0) {
            Dialogos.showMessageDialog(this, "Seleccione una factura de la lista.");
            return;
        }
        try {
            FacturaResumen f = resultados.get(table.convertRowIndexToModel(fila));
            DefaultTableModel datos = new DefaultTableModel(
                    new String[]{"Código", "Producto", "Cantidad", "Precio unitario", "Subtotal"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (LineaFactura l : controller.detalle(f.idFactura())) {
                datos.addRow(new Object[]{l.codigo(), l.producto(), l.cantidad().toBigInteger(),
                        FormatoMoneda.formatear(l.precioUnitario()), FormatoMoneda.formatear(l.subtotal())});
            }
            JTable detalle = new JTable(datos);
            detalle.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            int[] anchos = {100, 280, 80, 120, 120};
            for (int i = 0; i < anchos.length; i++) detalle.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
            JScrollPane panel = new JScrollPane(detalle);
            panel.setPreferredSize(new Dimension(740, 240));
            Dialogos.showMessageDialog(this, panel, "Detalle de la factura " + f.numeroFactura()
                    + "  (Total " + FormatoMoneda.formatear(f.total()) + ")", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            Dialogos.showMessageDialog(this, e.getMessage(), "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }
}
