package gt.edu.umg.ventas.vista;
import gt.edu.umg.ventas.util.Dialogos;

import gt.edu.umg.ventas.controlador.InventarioController;
import gt.edu.umg.ventas.modelo.Bodega;
import gt.edu.umg.ventas.modelo.InventarioResumen;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import gt.edu.umg.ventas.modelo.Producto;

public class FrmInventario extends JInternalFrame {

    private final InventarioController controller;
    private JComboBox<BodegaItem> cmbBodega;
    private JTextField txtFiltroProducto;
    private JTable tblInventario;
    private DefaultTableModel modeloTabla;

    private static class BodegaItem {
        private final Bodega bodega;

        public BodegaItem(Bodega bodega) {
            this.bodega = bodega;
        }

        public Bodega getBodega() {
            return bodega;
        }

        @Override
        public String toString() {
            return bodega == null ? "-- Todas las Bodegas --" : bodega.getNombre();
        }
    }

    public FrmInventario() {
        super("Consulta de Existencias de Inventario", true, true, true, true);
        this.controller = new InventarioController();

        setSize(850, 480);
        setMinimumSize(new Dimension(750, 400));
        initComponents();
        cargarBodegas();
        consultarInventario();
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override public void internalFrameActivated(InternalFrameEvent e) { refrescarInventario(); }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Filtros
        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Filtros de Búsqueda"));

        pnlNorte.add(new JLabel("Bodega:"));
        cmbBodega = new JComboBox<>();
        pnlNorte.add(cmbBodega);

        pnlNorte.add(new JLabel("Producto:"));
        txtFiltroProducto = new JTextField(15);
        pnlNorte.add(txtFiltroProducto);

        JButton btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.addActionListener(e -> consultarInventario());
        pnlNorte.add(btnBuscar);

        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> {
            txtFiltroProducto.setText("");
            cmbBodega.setSelectedIndex(0);
            refrescarInventario();
        });
        pnlNorte.add(btnRefrescar);
        JButton entrada = new JButton("Registrar entrada");
        entrada.addActionListener(e -> registrarEntrada());
        pnlNorte.add(entrada);

        add(pnlNorte, BorderLayout.NORTH);

        // Panel Central: Tabla con columnas exactas requeridas
        String[] columnas = {"Código", "Producto", "Bodega", "Existencia actual", "Existencia reservada", "Disponible"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblInventario = new JTable(modeloTabla);
        tblInventario.setRowHeight(24);
        add(new JScrollPane(tblInventario), BorderLayout.CENTER);
    }

    private void cargarBodegas() {
        BodegaItem anterior = (BodegaItem) cmbBodega.getSelectedItem();
        Integer idAnterior = anterior == null || anterior.getBodega() == null ? null : anterior.getBodega().getId();
        try {
            cmbBodega.removeAllItems();
            cmbBodega.addItem(new BodegaItem(null)); // Opción Todas
            List<Bodega> bodegas = controller.obtenerBodegas();
            for (Bodega b : bodegas) {
                cmbBodega.addItem(new BodegaItem(b));
                if (b.getId().equals(idAnterior)) cmbBodega.setSelectedIndex(cmbBodega.getItemCount() - 1);
            }
        } catch (Exception e) {
            Dialogos.showMessageDialog(this, "Error al cargar bodegas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refrescarInventario() {
        cargarBodegas();
        consultarInventario();
    }

    public void consultarInventario() {
        try {
            BodegaItem item = (BodegaItem) cmbBodega.getSelectedItem();
            Integer idBodega = (item != null && item.getBodega() != null) ? item.getBodega().getId() : null;
            String filtro = txtFiltroProducto.getText().trim();

            List<InventarioResumen> lista = controller.listarExistencias(idBodega, filtro);
            modeloTabla.setRowCount(0);

            for (InventarioResumen ir : lista) {
                modeloTabla.addRow(new Object[]{
                        ir.getCodigo(),
                        ir.getProducto(),
                        ir.getBodega(),
                        ir.getExistenciaActual(),
                        ir.getExistenciaReservada(),
                        ir.getDisponible()
                });
            }
        } catch (Exception ex) {
            modeloTabla.setRowCount(0);
            Dialogos.showMessageDialog(this, "Error al consultar inventario: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void registrarEntrada() {
        try {
            JComboBox<Producto> productos = new JComboBox<>(controller.obtenerProductosActivos().toArray(Producto[]::new));
            JComboBox<Bodega> bodegas = new JComboBox<>(controller.obtenerBodegasActivas().toArray(Bodega[]::new));
            JTextField cantidad = new JTextField("1"), referencia = new JTextField();
            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
            panel.add(new JLabel("Producto")); panel.add(productos); panel.add(new JLabel("Bodega")); panel.add(bodegas);
            panel.add(new JLabel("Cantidad entera")); panel.add(cantidad); panel.add(new JLabel("Referencia")); panel.add(referencia);
            if (Dialogos.showConfirmDialog(this, panel, "Entrada de inventario", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            controller.registrarEntrada(
                    (Producto) productos.getSelectedItem(), (Bodega) bodegas.getSelectedItem(),
                    Integer.parseInt(cantidad.getText().trim()), referencia.getText().trim());
            consultarInventario();
            gt.edu.umg.ventas.util.CambiosVentas.notificar(this);
        } catch (Exception e) { Dialogos.showMessageDialog(this, e.getMessage(), "Entrada no completada", JOptionPane.ERROR_MESSAGE); }
    }
}
