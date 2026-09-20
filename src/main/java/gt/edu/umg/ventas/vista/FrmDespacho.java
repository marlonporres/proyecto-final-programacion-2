package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmDespacho extends JInternalFrame {
    public FrmDespacho() {
        super("Despacho", true, true, true, true);
        setSize(700, 500);
        JPanel pnlNorte = new JPanel(new GridLayout(3, 2));
        pnlNorte.add(new JLabel("Orden Pendiente ID:")); pnlNorte.add(new JTextField());
        pnlNorte.add(new JLabel("Bodega:")); pnlNorte.add(new JComboBox<>(new String[]{"Bodega Central"}));
        pnlNorte.add(new JButton("Cargar Orden")); pnlNorte.add(new JButton("Confirmar Despacho"));
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"Producto", "Solicitado", "Despachado"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}
