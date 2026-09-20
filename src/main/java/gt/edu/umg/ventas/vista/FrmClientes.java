package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.controlador.ClienteController;
import gt.edu.umg.ventas.modelo.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmClientes extends JInternalFrame {

    private final ClienteController controller;

    private JTextField txtNit;
    private JTextField txtNombre;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTable tblClientes;
    private DefaultTableModel modeloTabla;

    public FrmClientes() {
        super("Catálogo de Clientes", true, true, true, true);
        this.controller = new ClienteController();

        setSize(780, 500);
        setMinimumSize(new Dimension(650, 400));
        initComponents();
        cargarClientes();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Formulario de entrada
        JPanel pnlNorte = new JPanel(new GridBagLayout());
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        pnlNorte.add(new JLabel("NIT:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.4;
        txtNit = new JTextField(12);
        pnlNorte.add(txtNit, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.6;
        txtNombre = new JTextField(20);
        pnlNorte.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.4;
        txtDireccion = new JTextField(20);
        pnlNorte.add(txtDireccion, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.6;
        txtTelefono = new JTextField(12);
        pnlNorte.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlNorte.add(new JLabel("Correo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.4;
        txtCorreo = new JTextField(20);
        pnlNorte.add(txtCorreo, gbc);

        // Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton btnLimpiar = new JButton("📝 Nuevo / Limpiar");
        btnLimpiar.addActionListener(e -> limpiarCampos());
        pnlBotones.add(btnLimpiar);

        JButton btnGuardar = new JButton("💾 Guardar");
        btnGuardar.addActionListener(e -> guardarCliente());
        pnlBotones.add(btnGuardar);

        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> cargarClientes());
        pnlBotones.add(btnRefrescar);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        pnlNorte.add(pnlBotones, gbc);

        add(pnlNorte, BorderLayout.NORTH);

        // Panel Central: Tabla
        String[] columnas = {"ID", "NIT", "Nombre", "Dirección", "Teléfono", "Correo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblClientes = new JTable(modeloTabla);
        tblClientes.setRowHeight(24);
        tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblClientes.getSelectedRow() != -1) {
                seleccionarCliente();
            }
        });

        add(new JScrollPane(tblClientes), BorderLayout.CENTER);
    }

    private long idClienteSeleccionado = 0;

    private void limpiarCampos() {
        idClienteSeleccionado = 0;
        txtNit.setText("");
        txtNombre.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        tblClientes.clearSelection();
    }

    private void seleccionarCliente() {
        int fila = tblClientes.getSelectedRow();
        if (fila >= 0) {
            long id = (long) modeloTabla.getValueAt(fila, 0);
            try {
                Cliente c = controller.buscarPorId(id);
                if (c != null) {
                    idClienteSeleccionado = c.getIdCliente();
                    txtNit.setText(c.getNit());
                    txtNombre.setText(c.getNombre());
                    txtDireccion.setText(c.getDireccion() != null ? c.getDireccion() : "");
                    txtTelefono.setText(c.getTelefono() != null ? c.getTelefono() : "");
                    txtCorreo.setText(c.getCorreo() != null ? c.getCorreo() : "");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar datos del cliente: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void cargarClientes() {
        try {
            List<Cliente> lista = controller.listar();
            modeloTabla.setRowCount(0);
            for (Cliente c : lista) {
                modeloTabla.addRow(new Object[]{
                        c.getIdCliente(),
                        c.getNit(),
                        c.getNombre(),
                        c.getDireccion(),
                        c.getTelefono(),
                        c.getCorreo()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes desde SQL Server: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarCliente() {
        String nit = txtNit.getText().trim();
        String nombre = txtNombre.getText().trim();
        if (nit.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El NIT y el Nombre son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Cliente c = new Cliente();
            c.setIdCliente(idClienteSeleccionado);
            c.setNit(nit);
            c.setNombre(nombre);
            c.setDireccion(txtDireccion.getText().trim());
            c.setTelefono(txtTelefono.getText().trim());
            c.setCorreo(txtCorreo.getText().trim());

            if (idClienteSeleccionado > 0) {
                controller.actualizar(c);
                JOptionPane.showMessageDialog(this, "Cliente actualizado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                controller.guardar(c);
                JOptionPane.showMessageDialog(this, "Cliente guardado exitosamente con ID: " + c.getIdCliente(),
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

            limpiarCampos();
            cargarClientes();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar cliente: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
