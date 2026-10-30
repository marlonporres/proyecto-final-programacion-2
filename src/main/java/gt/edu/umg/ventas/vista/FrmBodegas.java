package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.util.Dialogos;
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
    private int idSeleccionado = 0;

    public FrmBodegas() {
        super("Mantenimiento Bodegas", true, true, true, true);
        controller = new BodegaController();
        setSize(550, 420);

        JPanel pnlNorte = new JPanel(new GridLayout(4, 2, 6, 4));
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

        JPanel pnlBotonesIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JButton btnNuevo = new JButton("Nuevo / Limpiar");
        btnNuevo.addActionListener(e -> limpiarFormulario());
        pnlBotonesIzq.add(btnNuevo);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(e -> guardar());
        pnlBotonesIzq.add(btnGuardar);
        pnlNorte.add(pnlBotonesIzq);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarDatos());
        pnlNorte.add(btnRefrescar);

        add(pnlNorte, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Ubicacion", "Activa"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() != -1) {
                seleccionarBodega();
            }
        });
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        cargarDatos();
    }

    private void seleccionarBodega() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            int id = ((Number) modelo.getValueAt(fila, 0)).intValue();
            try {
                Bodega b = controller.obtenerPorId(id);
                if (b != null) {
                    idSeleccionado = b.getId();
                    txtNombre.setText(b.getNombre() != null ? b.getNombre() : "");
                    txtUbicacion.setText(b.getUbicacion() != null ? b.getUbicacion() : "");
                    chkActiva.setSelected(b.isActiva());
                }
            } catch (Exception ex) {
                Dialogos.showMessageDialog(this, ex.getMessage(), "Error al cargar bodega", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarFormulario() {
        idSeleccionado = 0;
        txtNombre.setText("");
        txtUbicacion.setText("");
        chkActiva.setSelected(true);
        tabla.clearSelection();
    }

    private void guardar() {
        try {
            if (idSeleccionado > 0) {
                controller.actualizar(idSeleccionado, txtNombre.getText(), txtUbicacion.getText(), chkActiva.isSelected());
            } else {
                controller.crear(txtNombre.getText(), txtUbicacion.getText(), chkActiva.isSelected());
            }
            limpiarFormulario();
            cargarDatos();
            gt.edu.umg.ventas.util.CambiosVentas.notificar(this);
        } catch (Exception e) {
            Dialogos.showMessageDialog(this, e.getMessage(), "Bodega no guardada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatos() {
        try {
            List<Bodega> lista = controller.listar();
            modelo.setRowCount(0);
            for (Bodega b : lista) {
                modelo.addRow(new Object[]{b.getId(), b.getNombre(), b.getUbicacion(), b.isActiva()});
            }
        } catch (Exception e) {
            Dialogos.showMessageDialog(this, e.getMessage(), "Error de consulta", JOptionPane.ERROR_MESSAGE);
        }
    }
}
