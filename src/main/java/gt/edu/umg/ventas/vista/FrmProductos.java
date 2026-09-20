package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.ProductoController;
import gt.edu.umg.ventas.modelo.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmProductos extends JInternalFrame {

    private final ProductoController controller;

    private JTextField txtBuscar;
    private JTable tblProductos;
    private DefaultTableModel modeloTabla;

    public FrmProductos() {
        super("Catálogo de Productos", true, true, true, true);
        this.controller = new ProductoController();

        setSize(800, 500);
        setMinimumSize(new Dimension(700, 400));
        initComponents();
        cargarProductos("");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Búsqueda y acciones
        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Búsqueda de Productos"));

        pnlNorte.add(new JLabel("Buscar (Código / Nombre):"));
        txtBuscar = new JTextField(20);
        pnlNorte.add(txtBuscar);

        JButton btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.addActionListener(e -> cargarProductos(txtBuscar.getText().trim()));
        pnlNorte.add(btnBuscar);

        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarProductos("");
        });
        pnlNorte.add(btnRefrescar);

        add(pnlNorte, BorderLayout.NORTH);

        // Panel Central: Tabla
        String[] columnas = {"ID", "Código", "Nombre", "Descripción", "Precio Venta (Q)", "Categoría", "Activo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblProductos = new JTable(modeloTabla);
        tblProductos.setRowHeight(24);
        add(new JScrollPane(tblProductos), BorderLayout.CENTER);
    }

    public void cargarProductos(String criterio) {
        try {
            List<Producto> lista;
            if (criterio == null || criterio.isBlank()) {
                lista = controller.listar();
            } else {
                lista = controller.buscar(criterio);
            }

            modeloTabla.setRowCount(0);
            for (Producto p : lista) {
                String catNombre = p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin Categoría";
                modeloTabla.addRow(new Object[]{
                        p.getIdProducto(),
                        p.getCodigo(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getPrecioVenta().toString(),
                        catNombre,
                        p.isActivo() ? "SÍ" : "NO"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar productos desde SQL Server: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
