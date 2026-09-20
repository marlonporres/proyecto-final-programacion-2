package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.BodegaController;
import gt.edu.umg.ventas.modelo.Bodega;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmBodegas extends JInternalFrame {
    private BodegaController controller;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField txtNombre;
    private JTextField txtUbicacion;
    private JCheckBox chkActiva;

    public FrmBodegas() {
        super("Mantenimiento Bodegas", true, true, true, true);
        controller = new BodegaController();
        setSize(500, 400);
        
        JPanel pnlNorte = new JPanel(new GridLayout(4, 2));
        pnlNorte.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        pnlNorte.add(txtNombre);
        pnlNorte.add(new JLabel("Ubicacion:"));
        txtUbicacion = new JTextField();
        pnlNorte.add(txtUbicacion);
        pnlNorte.add(new JLabel("Activa:"));
        chkActiva = new JCheckBox();
        chkActiva.setSelected(true);
        pnlNorte.add(chkActiva);
        
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardar());
        pnlNorte.add(btnGuardar);
        
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarDatos());
        pnlNorte.add(btnRefrescar);
        
        add(pnlNorte, BorderLayout.NORTH);
        
        modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Ubicacion", "Activa"}, 0);
        tabla = new JTable(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        cargarDatos();
    }
    
    private void guardar() {
        controller.crear(txtNombre.getText(), txtUbicacion.getText(), chkActiva.isSelected());
        txtNombre.setText("");
        txtUbicacion.setText("");
        cargarDatos();
    }
    
    private void cargarDatos() {
        modelo.setRowCount(0);
        List<Bodega> lista = controller.listar();
        for (Bodega b : lista) {
            modelo.addRow(new Object[]{b.getId(), b.getNombre(), b.getUbicacion(), b.isActiva()});
        }
    }
}
