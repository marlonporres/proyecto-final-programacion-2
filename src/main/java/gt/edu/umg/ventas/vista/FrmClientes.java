package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.util.Dialogos;
import gt.edu.umg.ventas.controlador.ClienteController;
import gt.edu.umg.ventas.modelo.Cliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FrmClientes extends JInternalFrame {

    private final ClienteController controller;

    private JTextField txtNit;
    private JTextField txtNombre;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    // [CAMBIO 1:N TELÉFONOS]: Modelo y componente JList para mostrar y gestionar múltiples teléfonos del cliente en la interfaz.
    private DefaultListModel<String> modeloTelefonos;
    private JList<String> lstTelefonos;
    private JTable tblClientes;
    private DefaultTableModel modeloTabla;
    private long idClienteSeleccionado = 0;

    public FrmClientes() {
        super("Catálogo de Clientes", true, true, true, true);
        this.controller = new ClienteController();

        setSize(820, 560);
        setMinimumSize(new Dimension(700, 440));
        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Formulario de entrada
        JPanel pnlNorte = new JPanel(new GridBagLayout());
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
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
        pnlNorte.add(new JLabel("Correo:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.6;
        txtCorreo = new JTextField(20);
        pnlNorte.add(txtCorreo, gbc);

        // [CAMBIO 1:N TELÉFONOS]: Sección visual con campo de texto, botones "+ Añadir Teléfono" / "Quitar" y lista JList en JScrollPane.
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pnlNorte.add(new JLabel("Teléfonos:"), gbc);

        JPanel pnlTelefonos = new JPanel(new BorderLayout(6, 4));
        JPanel pnlInputTel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        txtTelefono = new JTextField(12);
        pnlInputTel.add(txtTelefono);

        JButton btnAgregarTel = new JButton("+ Añadir Teléfono");
        btnAgregarTel.addActionListener(e -> agregarTelefonoLista());
        pnlInputTel.add(btnAgregarTel);

        JButton btnQuitarTel = new JButton("Quitar");
        btnQuitarTel.addActionListener(e -> quitarTelefonoLista());
        pnlInputTel.add(btnQuitarTel);

        pnlTelefonos.add(pnlInputTel, BorderLayout.NORTH);

        modeloTelefonos = new DefaultListModel<>();
        lstTelefonos = new JList<>(modeloTelefonos);
        lstTelefonos.setVisibleRowCount(3);
        lstTelefonos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTelefonos = new JScrollPane(lstTelefonos);
        scrollTelefonos.setPreferredSize(new Dimension(260, 64));
        pnlTelefonos.add(scrollTelefonos, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0;
        pnlNorte.add(pnlTelefonos, gbc);

        // Botones de acción
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnLimpiar = new JButton("📝 Nuevo / Limpiar");
        btnLimpiar.addActionListener(e -> limpiarCampos());
        pnlBotones.add(btnLimpiar);

        JButton btnGuardar = new JButton("💾 Guardar");
        btnGuardar.addActionListener(e -> guardarCliente());
        pnlBotones.add(btnGuardar);

        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.addActionListener(e -> cargarDatos());
        pnlBotones.add(btnRefrescar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        pnlNorte.add(pnlBotones, gbc);

        add(pnlNorte, BorderLayout.NORTH);

        // Panel Central: Tabla
        String[] columnas = {"ID", "NIT", "Nombre", "Dirección", "Teléfonos", "Correo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblClientes = new JTable(modeloTabla);
        tblClientes.setRowHeight(24);
        tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblClientes.getSelectedRow() != -1) {
                cargarClienteSeleccionado();
            }
        });

        add(new JScrollPane(tblClientes), BorderLayout.CENTER);
    }

    // [CAMBIO 1:N TELÉFONOS]: Agrega el número escrito en txtTelefono al modeloTelefonos validando longitud y evitando duplicados.
    private void agregarTelefonoLista() {
        String tel = txtTelefono.getText().trim();
        if (tel.isEmpty()) {
            return;
        }
        if (tel.length() > 20) {
            Dialogos.showMessageDialog(this, "El teléfono no puede exceder 20 caracteres.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!modeloTelefonos.contains(tel)) {
            modeloTelefonos.addElement(tel);
        }
        txtTelefono.setText("");
        txtTelefono.requestFocusInWindow();
    }

    // [CAMBIO 1:N TELÉFONOS]: Quita el teléfono seleccionado actualmente en el JList.
    private void quitarTelefonoLista() {
        int idx = lstTelefonos.getSelectedIndex();
        if (idx >= 0) {
            modeloTelefonos.remove(idx);
        }
    }

    private void limpiarCampos() {
        idClienteSeleccionado = 0;
        txtNit.setText("");
        txtNombre.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        // [CAMBIO 1:N TELÉFONOS]: Limpia la lista visual de teléfonos al reiniciar el formulario.
        modeloTelefonos.clear();
        tblClientes.clearSelection();
    }

    // [CAMBIO 1:N TELÉFONOS]: Al seleccionar un cliente en la JTable, carga todos sus teléfonos en modeloTelefonos para poder editarlos.
    private void cargarClienteSeleccionado() {
        int fila = tblClientes.getSelectedRow();
        if (fila >= 0) {
            long id = ((Number) modeloTabla.getValueAt(fila, 0)).longValue();
            try {
                Cliente c = controller.buscarPorId(id);
                if (c != null) {
                    idClienteSeleccionado = c.getId();
                    txtNit.setText(c.getNit());
                    txtNombre.setText(c.getNombre());
                    txtDireccion.setText(c.getDireccion() != null ? c.getDireccion() : "");
                    txtTelefono.setText("");
                    txtCorreo.setText(c.getCorreo() != null ? c.getCorreo() : "");
                    modeloTelefonos.clear();
                    for (String tel : c.getTelefonos()) {
                        modeloTelefonos.addElement(tel);
                    }
                }
            } catch (Exception ex) {
                Dialogos.showMessageDialog(this, "Error al cargar datos del cliente: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void cargarClientes() {
        cargarDatos();
    }

    public void cargarDatos() {
        try {
            List<Cliente> lista = controller.listar();
            modeloTabla.setRowCount(0);
            for (Cliente c : lista) {
                modeloTabla.addRow(new Object[]{
                        c.getId(),
                        c.getNit(),
                        c.getNombre(),
                        c.getDireccion(),
                        // [CAMBIO 1:N TELÉFONOS]: Muestra todos los teléfonos concatenados con coma en la columna "Teléfonos".
                        c.getTelefonosTexto(),
                        c.getCorreo()
                });
            }
        } catch (Exception ex) {
            Dialogos.showMessageDialog(this, "Error al cargar clientes desde SQL Server: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarCliente() {
        String nit = txtNit.getText().trim();
        String nombre = txtNombre.getText().trim();
        if (nit.isEmpty() || nombre.isEmpty()) {
            Dialogos.showMessageDialog(this, "El NIT y el Nombre son obligatorios.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // [CAMBIO 1:N TELÉFONOS]: Si el usuario dejó un teléfono escrito en txtTelefono sin presionar "+ Añadir Teléfono", se incorpora automáticamente.
        String telPendiente = txtTelefono.getText().trim();
        if (!telPendiente.isEmpty()) {
            if (telPendiente.length() > 20) {
                Dialogos.showMessageDialog(this, "El teléfono no puede exceder 20 caracteres.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!modeloTelefonos.contains(telPendiente)) {
                modeloTelefonos.addElement(telPendiente);
            }
            txtTelefono.setText("");
        }

        // [CAMBIO 1:N TELÉFONOS]: Construye la lista de teléfonos desde modeloTelefonos y la asigna a c.setTelefonos(...).
        List<String> telefonos = new ArrayList<>();
        for (int i = 0; i < modeloTelefonos.size(); i++) {
            telefonos.add(modeloTelefonos.getElementAt(i));
        }

        try {
            Cliente c = new Cliente();
            c.setId(idClienteSeleccionado);
            c.setNit(nit);
            c.setNombre(nombre);
            c.setDireccion(txtDireccion.getText().trim());
            c.setCorreo(txtCorreo.getText().trim());
            c.setTelefonos(telefonos);

            if (idClienteSeleccionado > 0) {
                controller.actualizar(c);
                Dialogos.showMessageDialog(this, "Cliente actualizado exitosamente.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                controller.guardar(c);
                Dialogos.showMessageDialog(this, "Cliente guardado exitosamente con ID: " + c.getId(),
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

            limpiarCampos();
            cargarDatos();
        } catch (Exception ex) {
            Dialogos.showMessageDialog(this, "Error al guardar cliente: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
