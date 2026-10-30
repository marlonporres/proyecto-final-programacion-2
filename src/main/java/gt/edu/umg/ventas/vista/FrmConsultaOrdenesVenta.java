package gt.edu.umg.ventas.vista;
import gt.edu.umg.ventas.util.Dialogos;

import gt.edu.umg.ventas.controlador.ConsultaOrdenVentaController;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.util.Objects;

public class FrmConsultaOrdenesVenta extends JInternalFrame {
    private JSpinner spinDesde;
    private JSpinner spinHasta;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnBuscar;
    private JButton btnLimpiar;
    
    private ConsultaOrdenVentaController controller;
    private List<OrdenVentaResumen> resultados = List.of();

    public FrmConsultaOrdenesVenta() {
        this(new ConsultaOrdenVentaController());
    }

    public FrmConsultaOrdenesVenta(ConsultaOrdenVentaController controller) {
        super("Consulta de Ordenes de Venta", true, true, true, true);
        this.controller = Objects.requireNonNull(controller);
        
        setSize(850, 450);
        
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        spinDesde = new JSpinner(new SpinnerDateModel());
        spinHasta = new JSpinner(new SpinnerDateModel());
        
        JSpinner.DateEditor deDesde = new JSpinner.DateEditor(spinDesde, "dd/MM/yyyy");
        deDesde.getFormat().setLenient(false);
        spinDesde.setEditor(deDesde);
        JSpinner.DateEditor deHasta = new JSpinner.DateEditor(spinHasta, "dd/MM/yyyy");
        deHasta.getFormat().setLenient(false);
        spinHasta.setEditor(deHasta);
        
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscar());

        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiar());

        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(spinDesde);
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(spinHasta);
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnLimpiar);
        JButton detalle = new JButton("Ver operación");
        detalle.addActionListener(e -> mostrarDetalle());
        panelFiltros.add(detalle);

        String[] columnas = {"No. Orden", "Fecha", "Cliente", "Estado", "Total", "No. Despacho", "Despacho", "Bodega", "Factura", "Estado factura"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i)
                .setPreferredWidth(i == 0 || i == 5 || i == 8 ? 300 : 130);

        add(panelFiltros, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void buscar() {
        resultados = List.of();
        tableModel.setRowCount(0);
        try {
            // Incluye la fecha escrita, aunque el usuario aún no haya salido del campo.
            spinDesde.commitEdit();
            spinHasta.commitEdit();
            Date dDesde = (Date) spinDesde.getValue();
            Date dHasta = (Date) spinHasta.getValue();
            LocalDateTime desde = dDesde.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime hasta = dHasta.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            resultados = controller.buscar(desde.toLocalDate().atStartOfDay(), hasta.toLocalDate().atStartOfDay());
            tableModel.setRowCount(0);
            for (OrdenVentaResumen r : resultados) {
                tableModel.addRow(new Object[]{
                    r.getNumeroOrden(),
                    r.getFecha().toLocalDate().toString(),
                    r.getNombreCliente(),
                    r.getEstado().name(),
                    r.getTotal(),
                    r.getNumeroDespacho(),
                    r.getEstadoDespacho() != null && !r.getEstadoDespacho().isBlank() ? r.getEstadoDespacho() : "Pendiente",
                    r.getNombreBodega() != null && !r.getNombreBodega().isBlank() ? r.getNombreBodega() : "-",
                    r.getNumeroFactura(), r.getEstadoFactura()
                });
            }
            if(resultados.isEmpty()){
                Dialogos.showMessageDialog(this, "No se encontraron resultados en las fechas dadas.");
            }
        } catch (ParseException ex) {
            Dialogos.showMessageDialog(this, "Ingrese fechas válidas en formato dd/MM/aaaa.",
                    "Fechas inválidas", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            Dialogos.showMessageDialog(this, ex.getMessage(), "Error de validacion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        spinDesde.setValue(new Date());
        spinHasta.setValue(new Date());
        tableModel.setRowCount(0);
        resultados = List.of();
    }

    private void mostrarDetalle() {
        int fila = table.getSelectedRow();
        if (fila < 0) { Dialogos.showMessageDialog(this, "Seleccione una operación."); return; }
        try {
            int id = resultados.get(table.convertRowIndexToModel(fila)).getId();
            DefaultTableModel datos = new DefaultTableModel(new String[]{"Orden", "Producto", "Solicitado", "Despachado",
                    "Precio", "Bodega", "Despacho", "Movimiento", "Salida", "Factura", "Total factura"}, 0) {
                @Override public boolean isCellEditable(int fila, int columna) { return false; }
            };
            for (var linea : controller.consultarDetalle(id)) datos.addRow(new Object[]{linea.numeroOrden(), linea.codigoProducto(),
                    linea.cantidadSolicitada(), linea.cantidadDespachada(), linea.precioUnitario(), linea.bodega(),
                    linea.numeroDespacho(), linea.tipoMovimiento(), linea.cantidadSalida(), linea.numeroFactura(), linea.totalFactura()});
            JTable detalle = new JTable(datos);
            detalle.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < detalle.getColumnCount(); i++) detalle.getColumnModel().getColumn(i).setPreferredWidth(140);
            JScrollPane panel = new JScrollPane(detalle);
            panel.setPreferredSize(new Dimension(1000, 300));
            Dialogos.showMessageDialog(this, panel, "Detalle de la operación", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { Dialogos.showMessageDialog(this, e.getMessage(), "Error de consulta", JOptionPane.ERROR_MESSAGE); }
    }
}
