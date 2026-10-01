package gt.edu.umg.ventas.vista;
import gt.edu.umg.ventas.util.Dialogos;

import gt.edu.umg.ventas.controlador.CategoriaController;
import gt.edu.umg.ventas.modelo.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmCategorias extends JInternalFrame {

    private final CategoriaController controller;
    private JTable tblCategorias;
    private DefaultTableModel modeloTabla;

    public FrmCategorias() {
        super("Catálogo de Categorías", true, true, true, true);
        this.controller = new CategoriaController();

        setSize(650, 420);
        setMinimumSize(new Dimension(550, 350));
        initComponents();
        cargarCategorias();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel pnlNorte = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> cargarCategorias());
        pnlNorte.add(btnRefrescar);
        JButton nuevo = new JButton("Nueva"), editar = new JButton("Editar");
        nuevo.addActionListener(e -> editarCategoria(false));
        editar.addActionListener(e -> editarCategoria(true));
        pnlNorte.add(nuevo); pnlNorte.add(editar);
        add(pnlNorte, BorderLayout.NORTH);

        String[] columnas = {"ID", "Nombre", "Descripción", "Activa"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCategorias = new JTable(modeloTabla);
        tblCategorias.setRowHeight(24);
        add(new JScrollPane(tblCategorias), BorderLayout.CENTER);
    }

    public void cargarCategorias() {
        try {
            List<Categoria> lista = controller.listar();
            modeloTabla.setRowCount(0);
            for (Categoria c : lista) {
                modeloTabla.addRow(new Object[]{
                        c.getIdCategoria(),
                        c.getNombre(),
                        c.getDescripcion(),
                        c.isActiva() ? "SÍ" : "NO"
                });
            }
        } catch (Exception ex) {
            Dialogos.showMessageDialog(this, "Error al cargar categorías: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void editarCategoria(boolean editar) {
        int fila = tblCategorias.getSelectedRow();
        if (editar && fila < 0) { Dialogos.showMessageDialog(this, "Seleccione una categoría."); return; }
        try {
            long id = editar ? ((Number) modeloTabla.getValueAt(tblCategorias.convertRowIndexToModel(fila), 0)).longValue() : 0;
            if (EditorCatalogo.categoria(this, id)) cargarCategorias();
        } catch (Exception e) { Dialogos.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
}
