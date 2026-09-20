package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmInventario extends JInternalFrame {
    public FrmInventario() {
        super("Inventario", true, true, true, true);
        setSize(600, 400);
        JPanel pnlNorte = new JPanel(new FlowLayout());
        pnlNorte.add(new JLabel("Filtro Bodega:")); pnlNorte.add(new JComboBox<>(new String[]{"Bodega Central"}));
        pnlNorte.add(new JLabel("Producto:")); pnlNorte.add(new JTextField(10));
        pnlNorte.add(new JButton("Buscar"));
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"Producto", "Bodega", "Actual", "Reservada", "Disponible"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}
