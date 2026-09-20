package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmClientes extends JInternalFrame {
    public FrmClientes() {
        super("Clientes", true, true, true, true);
        setSize(600, 400);
        JPanel pnlNorte = new JPanel(new GridLayout(3, 2));
        pnlNorte.add(new JLabel("NIT:")); pnlNorte.add(new JTextField());
        pnlNorte.add(new JLabel("Nombre:")); pnlNorte.add(new JTextField());
        pnlNorte.add(new JButton("Guardar")); pnlNorte.add(new JButton("Refrescar"));
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"ID", "NIT", "Nombre", "Direccion"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}
