package gt.edu.umg.ventas.vista;

import gt.edu.umg.ventas.util.Dialogos;
import gt.edu.umg.ventas.controlador.ClienteController;
import gt.edu.umg.ventas.modelo.Cliente;
import gt.edu.umg.ventas.modelo.Telefono;
import gt.edu.umg.ventas.util.PaisTelefono;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FrmClientes extends JInternalFrame {

    private final ClienteController controller;

    private JTextField txtNit;
    private JTextField txtNombre;
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    // [CAMBIO 1:N TELÉFONOS]: Selector de tipo de teléfono (Personal, Trabajo, Casa u Otro) exigido en el documento oficial.
    private JComboBox<String> cmbTipoTelefono;
    // [VALIDACIÓN POR PAÍS]: País del teléfono (define prefijo y reglas) y texto con la regla vigente.
    private JComboBox<PaisTelefono> cmbPaisTelefono;
    private JLabel lblReglaTelefono;
    private JTextField txtCorreo;
    // [CAMBIO 1:N TELÉFONOS]: Modelo y componente JList de objetos Telefono (número y tipo) para gestionar múltiples teléfonos en la vista.
    private DefaultListModel<Telefono> modeloTelefonos;
    private JList<Telefono> lstTelefonos;
    private JTable tblClientes;
    private DefaultTableModel modeloTabla;
    private long idClienteSeleccionado = 0;

    public FrmClientes() {
        super("Catálogo de Clientes", true, true, true, true);
        this.controller = new ClienteController();

        setSize(920, 590);
        setMinimumSize(new Dimension(800, 460));
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

        // [CAMBIO 1:N TELÉFONOS]: Sección visual para ingresar Número + Tipo (Personal, Trabajo, Casa, Otro) y administrarlos en el JList.
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pnlNorte.add(new JLabel("Teléfonos:"), gbc);

        JPanel pnlTelefonos = new JPanel(new BorderLayout(6, 4));
        JPanel pnlInputTel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        // [VALIDACIÓN POR PAÍS]: Selector de país (Guatemala por defecto). Un número escrito con prefijo (+504 ...) usa el país del prefijo.
        pnlInputTel.add(new JLabel("País:"));
        cmbPaisTelefono = new JComboBox<>(PaisTelefono.values());
        cmbPaisTelefono.setSelectedItem(PaisTelefono.GUATEMALA);
        cmbPaisTelefono.addActionListener(e -> actualizarReglaTelefono());
        pnlInputTel.add(cmbPaisTelefono);

        pnlInputTel.add(new JLabel("Número:"));
        txtTelefono = new JTextField(11);
        // [VALIDACIÓN POR PAÍS]: Solo permite dígitos, espacios, +, -, paréntesis y un máximo de 20 caracteres.
        ((AbstractDocument) txtTelefono.getDocument()).setDocumentFilter(new DocumentFilter() {
            private boolean permitido(String texto) {
                return texto.matches("[0-9 +()\\-]*");
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String texto, AttributeSet attr) throws BadLocationException {
                if (texto != null && permitido(texto) && fb.getDocument().getLength() + texto.length() <= 20) {
                    super.insertString(fb, offset, texto, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String texto, AttributeSet attr) throws BadLocationException {
                if (texto == null || (permitido(texto) && fb.getDocument().getLength() - length + texto.length() <= 20)) {
                    super.replace(fb, offset, length, texto, attr);
                }
            }
        });
        pnlInputTel.add(txtTelefono);

        pnlInputTel.add(new JLabel("Tipo:"));
        cmbTipoTelefono = new JComboBox<>(new String[]{"Personal", "Trabajo", "Casa", "Otro"});
        pnlInputTel.add(cmbTipoTelefono);

        JButton btnAgregarTel = new JButton("+ Añadir Teléfono");
        btnAgregarTel.addActionListener(e -> agregarTelefonoLista());
        pnlInputTel.add(btnAgregarTel);

        JButton btnQuitarTel = new JButton("Quitar");
        btnQuitarTel.addActionListener(e -> quitarTelefonoLista());
        pnlInputTel.add(btnQuitarTel);

        lblReglaTelefono = new JLabel();
        lblReglaTelefono.setFont(lblReglaTelefono.getFont().deriveFont(Font.ITALIC, 11f));
        JPanel pnlEntradaTelefono = new JPanel(new BorderLayout());
        pnlEntradaTelefono.add(pnlInputTel, BorderLayout.NORTH);
        pnlEntradaTelefono.add(lblReglaTelefono, BorderLayout.CENTER);
        actualizarReglaTelefono();
        pnlTelefonos.add(pnlEntradaTelefono, BorderLayout.NORTH);

        modeloTelefonos = new DefaultListModel<>();
        lstTelefonos = new JList<>(modeloTelefonos);
        lstTelefonos.setVisibleRowCount(3);
        lstTelefonos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTelefonos = new JScrollPane(lstTelefonos);
        scrollTelefonos.setPreferredSize(new Dimension(320, 64));
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
        String[] columnas = {"ID", "NIT", "Nombre", "Dirección", "Teléfonos (Número y Tipo)", "Correo"};
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

    // [CAMBIO 1:N TELÉFONOS]: Crea un objeto Telefono con su número y su tipo (Personal, Trabajo, Casa u Otro) y lo agrega al JList.
    private void agregarTelefonoLista() {
        String tel = txtTelefono.getText().trim();
        if (tel.isEmpty()) {
            return;
        }
        // [VALIDACIÓN POR PAÍS]: Valida según el país elegido (o el del prefijo) y arma el número final.
        try {
            tel = PaisTelefono.normalizar(tel, (PaisTelefono) cmbPaisTelefono.getSelectedItem());
        } catch (IllegalArgumentException ex) {
            Dialogos.showMessageDialog(this, ex.getMessage(), "Teléfono inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tipo = (String) cmbTipoTelefono.getSelectedItem();
        agregarOActualizarEnModelo(tel, tipo);
        txtTelefono.setText("");
        cmbTipoTelefono.setSelectedIndex(0);
        txtTelefono.requestFocusInWindow();
    }

    // [VALIDACIÓN POR PAÍS]: Muestra debajo del campo la regla del país seleccionado.
    private void actualizarReglaTelefono() {
        PaisTelefono pais = (PaisTelefono) cmbPaisTelefono.getSelectedItem();
        if (pais != null) {
            lblReglaTelefono.setText("  Regla " + pais.getNombre() + " (+" + pais.getCodigo() + "): " + pais.getRegla());
        }
    }

    // [CAMBIO 1:N TELÉFONOS]: Evita duplicar el mismo número en el JList; si ya existe actualiza su tipo, si no existe lo añade.
    private void agregarOActualizarEnModelo(String numero, String tipo) {
        for (int i = 0; i < modeloTelefonos.size(); i++) {
            Telefono actual = modeloTelefonos.getElementAt(i);
            if (actual.getNumero().equalsIgnoreCase(numero)) {
                actual.setTipo(tipo);
                modeloTelefonos.set(i, actual);
                return;
            }
        }
        modeloTelefonos.addElement(new Telefono(numero, tipo));
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
        cmbTipoTelefono.setSelectedIndex(0);
        cmbPaisTelefono.setSelectedItem(PaisTelefono.GUATEMALA);
        txtCorreo.setText("");
        // [CAMBIO 1:N TELÉFONOS]: Limpia la lista visual de teléfonos al reiniciar el formulario.
        modeloTelefonos.clear();
        tblClientes.clearSelection();
    }

    // [CAMBIO 1:N TELÉFONOS]: Al seleccionar un cliente en la tabla, consulta sus teléfonos persistidos (número y tipo) y los muestra en el JList.
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
                    cmbTipoTelefono.setSelectedIndex(0);
                    cmbPaisTelefono.setSelectedItem(PaisTelefono.GUATEMALA);
                    txtCorreo.setText(c.getCorreo() != null ? c.getCorreo() : "");
                    modeloTelefonos.clear();
                    for (Telefono tel : c.getListaTelefonos()) {
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
                        // [CAMBIO 1:N TELÉFONOS]: Muestra todos los teléfonos con su número y su tipo en la tabla.
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

        // [CAMBIO 1:N TELÉFONOS]: Si el usuario dejó un teléfono escrito en txtTelefono sin presionar "+ Añadir Teléfono", lo agrega con su tipo seleccionado.
        String telPendiente = txtTelefono.getText().trim();
        if (!telPendiente.isEmpty()) {
            // [VALIDACIÓN POR PAÍS]: El teléfono pendiente también se valida según el país elegido.
            try {
                telPendiente = PaisTelefono.normalizar(telPendiente, (PaisTelefono) cmbPaisTelefono.getSelectedItem());
            } catch (IllegalArgumentException ex) {
                Dialogos.showMessageDialog(this, ex.getMessage(), "Teléfono inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String tipoPendiente = (String) cmbTipoTelefono.getSelectedItem();
            agregarOActualizarEnModelo(telPendiente, tipoPendiente);
            txtTelefono.setText("");
            cmbTipoTelefono.setSelectedIndex(0);
        }

        // [CAMBIO 1:N TELÉFONOS]: Construye la colección List<Telefono> desde el JList y la envía al controlador.
        List<Telefono> telefonos = new ArrayList<>();
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
            c.setListaTelefonos(telefonos);

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
