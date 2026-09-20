package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.ConsultaOrdenVentaController;
import gt.edu.umg.ventas.modelo.OrdenVentaResumen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class FrmConsultaOrdenesVenta extends JInternalFrame {
    private JSpinner spinDesde;
    private JSpinner spinHasta;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnBuscar;
    private JButton btnLimpiar;
    
    private ConsultaOrdenVentaController controller;

    public FrmConsultaOrdenesVenta() {
        super("Consulta de Ordenes de Venta", true, true, true, true);
        controller = new ConsultaOrdenVentaController();
        
        setSize(850, 450);
        
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        spinDesde = new JSpinner(new SpinnerDateModel());
        spinHasta = new JSpinner(new SpinnerDateModel());
        
        JSpinner.DateEditor deDesde = new JSpinner.DateEditor(spinDesde, "dd/MM/yyyy");
        spinDesde.setEditor(deDesde);
        JSpinner.DateEditor deHasta = new JSpinner.DateEditor(spinHasta, "dd/MM/yyyy");
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

        String[] columnas = {"No. Orden", "Fecha", "Cliente", "Estado", "Total", "Despacho", "Bodega"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);

        add(panelFiltros, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void buscar() {
        Date dDesde = (Date) spinDesde.getValue();
        Date dHasta = (Date) spinHasta.getValue();
        
        LocalDateTime desde = dDesde.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime hasta = dHasta.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        try {
            List<OrdenVentaResumen> resultados = controller.buscar(desde, hasta);
            tableModel.setRowCount(0);
            for (OrdenVentaResumen r : resultados) {
                tableModel.addRow(new Object[]{
                    r.getNumeroOrden(),
                    r.getFecha().toLocalDate().toString(),
                    r.getNombreCliente(),
                    r.getEstado().name(),
                    r.getEstadoDespacho() != null && !r.getEstadoDespacho().isBlank() ? r.getEstadoDespacho() : "Pendiente",
                    r.getNombreBodega() != null && !r.getNombreBodega().isBlank() ? r.getNombreBodega() : "-"
                });
            }
            if(resultados.isEmpty()){
                JOptionPane.showMessageDialog(this, "No se encontraron resultados en las fechas dadas.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de validacion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        spinDesde.setValue(new Date());
        spinHasta.setValue(new Date());
        tableModel.setRowCount(0);
    }
}
