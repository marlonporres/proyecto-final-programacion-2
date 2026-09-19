package gt.edu.umg.facturacion.vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Ventana modal / buscador reutilizable para Clientes y Productos.
 */
public class FrmFacturaFiltro extends JDialog {

    private JTextField txtFiltro;
    private JButton btnBuscar;
    private JTable tblResultados;
    private DefaultTableModel modeloTabla;
    private JButton btnSeleccionar;
    private JButton btnCancelar;

    public FrmFacturaFiltro(Frame parent, String titulo) {
        super(parent, titulo, true);
        initComponents();
        setSize(650, 420);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));

        // Panel Superior: Búsqueda
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelTop.add(new JLabel("Buscar:"));
        txtFiltro = new JTextField(25);
        panelTop.add(txtFiltro);
        btnBuscar = new JButton("🔍 Buscar");
        panelTop.add(btnBuscar);
        add(panelTop, BorderLayout.NORTH);

        // Tabla Central
        modeloTabla = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblResultados = new JTable(modeloTabla);
        tblResultados.setRowHeight(22);
        tblResultados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tblResultados);
        add(scroll, BorderLayout.CENTER);

        // Panel Inferior: Botones
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnSeleccionar = new JButton("✔ Seleccionar");
        btnCancelar = new JButton("Cancelar");
        panelBottom.add(btnSeleccionar);
        panelBottom.add(btnCancelar);
        add(panelBottom, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
    }

    public JTextField getTxtFiltro() { return txtFiltro; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JTable getTblResultados() { return tblResultados; }
    public DefaultTableModel getModeloTabla() { return modeloTabla; }
    public JButton getBtnSeleccionar() { return btnSeleccionar; }
    public JButton getBtnCancelar() { return btnCancelar; }
}
