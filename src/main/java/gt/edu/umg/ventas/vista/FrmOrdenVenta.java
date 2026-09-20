package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmOrdenVenta extends JInternalFrame {
    public FrmOrdenVenta() {
        super("Nueva Orden de Venta", true, true, true, true);
        setSize(800, 500);
        JPanel pnlNorte = new JPanel(new GridLayout(3, 4));
        pnlNorte.add(new JLabel("Cliente:")); pnlNorte.add(new JTextField());
        pnlNorte.add(new JLabel("Producto:")); pnlNorte.add(new JTextField());
        pnlNorte.add(new JLabel("Cantidad:")); pnlNorte.add(new JTextField());
        pnlNorte.add(new JButton("Agregar Detalle"));
        add(pnlNorte, BorderLayout.NORTH);
        
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"Producto", "Cant", "Precio", "Subtotal"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSur.add(new JLabel("Total: Q. 0.00"));
        pnlSur.add(new JButton("Guardar Orden"));
        add(pnlSur, BorderLayout.SOUTH);
    }
}
