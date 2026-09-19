package gt.edu.umg.facturacion.controlador;

import gt.edu.umg.facturacion.dao.ClienteDAO;
import gt.edu.umg.facturacion.dao.ClienteDAOImpl;
import gt.edu.umg.facturacion.dao.ProductoDAO;
import gt.edu.umg.facturacion.dao.ProductoDAOImpl;
import gt.edu.umg.facturacion.modelo.Cliente;
import gt.edu.umg.facturacion.modelo.DetalleFactura;
import gt.edu.umg.facturacion.modelo.EstadoFactura;
import gt.edu.umg.facturacion.modelo.Factura;
import gt.edu.umg.facturacion.modelo.MetodoPago;
import gt.edu.umg.facturacion.modelo.Pago;
import gt.edu.umg.facturacion.modelo.Producto;
import gt.edu.umg.facturacion.modelo.Usuario;
import gt.edu.umg.facturacion.servicio.FacturaService;
import gt.edu.umg.facturacion.util.FormatoMoneda;
import gt.edu.umg.facturacion.vista.FrmFacturaFiltro;
import gt.edu.umg.facturacion.vista.PantallaFacturacion;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Controlador de Facturación según el diagrama UML.
 * Orquesta la interacción entre PantallaFacturacion y FacturaService,
 * sin colocar lógica de negocio pesada ni SQL dentro de la vista ni del controlador.
 */
public class FacturaController {

    private final PantallaFacturacion vista;
    private final FacturaService servicio;
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;

    private Factura facturaActual;
    private Producto productoSeleccionado;
    private Usuario usuarioSesion;

    public FacturaController(PantallaFacturacion vista) {
        this(vista, new FacturaService());
    }

    public FacturaController(PantallaFacturacion vista, FacturaService servicio) {
        this.vista = vista;
        this.servicio = servicio != null ? servicio : new FacturaService();
        this.productoDAO = new ProductoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();

        // Usuario predeterminado del sistema (Cajero / Administrador)
        this.usuarioSesion = new Usuario(1L, "Marlon Porres", "admin", "ADMINISTRADOR", true);

        this.vista.setController(this);
        enlazarEventos();
        iniciarNuevaFactura();
    }

    private void enlazarEventos() {
        vista.getBtnBuscarCliente().addActionListener(e -> abrirBuscadorCliente());
        vista.getTxtNit().addActionListener(e -> buscarClientePorNit());

        vista.getBtnBuscarProd().addActionListener(e -> abrirBuscadorProducto());
        vista.getTxtCodigoProd().addActionListener(e -> buscarProductoPorCodigo());

        vista.getBtnAgregarProd().addActionListener(e -> agregarProducto());
        vista.getBtnEliminarProd().addActionListener(e -> eliminarProducto());

        vista.getBtnNuevaFactura().addActionListener(e -> iniciarNuevaFactura());
        vista.getBtnEmitir().addActionListener(e -> solicitarEmision());
        vista.getBtnConsultar().addActionListener(e -> consultarInteractivo());
        vista.getBtnAnular().addActionListener(e -> anularFacturaActual());
        vista.getBtnRegistrarPago().addActionListener(e -> registrarPago());
    }

    /**
     * Inicia una nueva factura en estado BORRADOR.
     */
    public void iniciarNuevaFactura() {
        Cliente clienteDefecto = new Cliente(1L, "CF", "Consumidor Final", "Ciudad", "00000000", "cf@sistema.gt");
        crear(clienteDefecto, usuarioSesion);
    }

    /**
     * Implementación del método UML: crear(datos).
     *
     * @param cliente Cliente asociado
     * @param usuario Usuario responsable
     */
    public void crear(Cliente cliente, Usuario usuario) {
        try {
            this.facturaActual = servicio.crear(cliente, usuario);
            this.productoSeleccionado = null;
            vista.mostrarFactura(facturaActual);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al crear nueva factura: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Implementación del método UML: emitir(idFactura).
     *
     * @param idFactura Identificador de la factura a emitir
     */
    public void emitir(long idFactura) {
        try {
            this.facturaActual = servicio.emitir(idFactura);
            vista.mostrarFactura(facturaActual);
            JOptionPane.showMessageDialog(vista, 
                    "¡Factura " + facturaActual.getNumero() + " emitida con éxito!\nInventario descontado correctamente.", 
                    "Factura Emitida", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "No se pudo emitir la factura: " + e.getMessage(), 
                    "Error de emisión", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Implementación del método UML: consultar(numero).
     *
     * @param numero Número de factura a consultar
     */
    public void consultar(String numero) {
        if (numero == null || numero.isBlank()) {
            JOptionPane.showMessageDialog(vista, "Ingrese un número de factura válido.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Factura f = servicio.consultarPorNumero(numero.trim());
            if (f != null) {
                this.facturaActual = f;
                vista.mostrarFactura(f);
            } else {
                JOptionPane.showMessageDialog(vista, "No se encontró ninguna factura con el número: " + numero, 
                        "No encontrado", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al consultar factura: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Flujo invocado al hacer clic en 'Emitir Factura'.
     */
    public void solicitarEmision() {
        if (facturaActual == null) {
            JOptionPane.showMessageDialog(vista, "No hay ninguna factura activa.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (facturaActual.getDetalles().isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Debe agregar al menos un producto antes de emitir la factura.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Actualizar datos del cliente desde la pantalla si fueron modificados
        capturarClienteDesdePantalla();

        int confirmacion = JOptionPane.showConfirmDialog(vista,
                "¿Está seguro de emitir la factura " + facturaActual.getNumero() + "?\n"
                        + "Total: " + FormatoMoneda.formatear(facturaActual.calcularTotal()),
                "Confirmar Emisión", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            emitir(facturaActual.getIdFactura());
        }
    }

    private void agregarProducto() {
        if (facturaActual == null || facturaActual.getEstado() != EstadoFactura.BORRADOR) {
            JOptionPane.showMessageDialog(vista, "Solo se pueden agregar productos a una factura en estado BORRADOR.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (productoSeleccionado == null) {
            buscarProductoPorCodigo();
            if (productoSeleccionado == null) {
                JOptionPane.showMessageDialog(vista, "Seleccione o busque un producto primero.", 
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            BigDecimal cantidad = FormatoMoneda.parsear(vista.getTxtCantidadProd().getText());
            BigDecimal descuento = FormatoMoneda.parsear(vista.getTxtDescuentoProd().getText());

            if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(vista, "La cantidad debe ser mayor a cero.", 
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Agregar a la factura mediante el servicio
            servicio.agregarProducto(facturaActual.getIdFactura(), productoSeleccionado, cantidad);

            // Ajustar descuento si se ingresó
            if (descuento.compareTo(BigDecimal.ZERO) > 0) {
                int lastIdx = facturaActual.getDetalles().size() - 1;
                facturaActual.getDetalles().get(lastIdx).setDescuento(descuento);
            }

            vista.mostrarFactura(facturaActual);
            vista.limpiarCamposProducto();
            productoSeleccionado = null;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vista, "Cantidad o descuento inválido.", 
                    "Error de formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, e.getMessage(), 
                    "Validación de Negocio", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarProducto() {
        if (facturaActual == null || facturaActual.getEstado() != EstadoFactura.BORRADOR) {
            JOptionPane.showMessageDialog(vista, "Solo se pueden modificar facturas en estado BORRADOR.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int fila = vista.getTblDetalle().getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione un producto de la tabla para eliminarlo.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (fila < facturaActual.getDetalles().size()) {
            DetalleFactura detalle = facturaActual.getDetalles().get(fila);
            facturaActual.eliminarDetalle(detalle);
            vista.mostrarFactura(facturaActual);
        }
    }

    private void registrarPago() {
        if (facturaActual == null) {
            JOptionPane.showMessageDialog(vista, "No hay ninguna factura activa.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (facturaActual.getEstado() == EstadoFactura.BORRADOR) {
            JOptionPane.showMessageDialog(vista, "Debe emitir la factura antes de registrar pagos formales.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (facturaActual.getEstado() == EstadoFactura.PAGADA) {
            JOptionPane.showMessageDialog(vista, "Esta factura ya se encuentra totalmente PAGADA.", 
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (facturaActual.getEstado() == EstadoFactura.ANULADA) {
            JOptionPane.showMessageDialog(vista, "No se pueden registrar pagos a una factura ANULADA.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal monto = FormatoMoneda.parsear(vista.getTxtMontoPago().getText());
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(vista, "El monto del pago debe ser mayor a cero.", 
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            MetodoPago metodo = (MetodoPago) vista.getCmbMetodoPago().getSelectedItem();
            String ref = vista.getTxtReferenciaPago().getText().trim();

            Pago pago = new Pago(0L, LocalDateTime.now(), monto, metodo, ref);
            this.facturaActual = servicio.registrarPago(facturaActual.getIdFactura(), pago);
            vista.mostrarFactura(facturaActual);

            if (facturaActual.getEstado() == EstadoFactura.PAGADA) {
                JOptionPane.showMessageDialog(vista, "¡Factura PAGADA en su totalidad!", 
                        "Pago Completo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vista, "Pago de " + FormatoMoneda.formatear(monto) + " registrado con éxito.", 
                        "Pago Registrado", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al registrar pago: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void anularFacturaActual() {
        if (facturaActual == null) {
            JOptionPane.showMessageDialog(vista, "No hay ninguna factura seleccionada.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (facturaActual.getEstado() == EstadoFactura.ANULADA) {
            JOptionPane.showMessageDialog(vista, "La factura ya está ANULADA.", 
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(vista, 
                "¿Está seguro de anular la factura " + facturaActual.getNumero() + "?\n"
                        + "Si la factura estaba emitida, se repondrá el inventario automáticamente.", 
                "Confirmar Anulación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                this.facturaActual = servicio.anular(facturaActual.getIdFactura());
                vista.mostrarFactura(facturaActual);
                JOptionPane.showMessageDialog(vista, "Factura anulada e inventario repuesto exitosamente.", 
                        "Factura Anulada", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(vista, "Error al anular: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void consultarInteractivo() {
        String num = JOptionPane.showInputDialog(vista, "Ingrese el número de factura a consultar (ej: FAC-1):", 
                "Consultar Factura", JOptionPane.QUESTION_MESSAGE);
        if (num != null && !num.isBlank()) {
            consultar(num.trim());
        }
    }

    private void abrirBuscadorCliente() {
        Window parentWindow = SwingUtilities.getWindowAncestor(vista);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        FrmFacturaFiltro filtro = new FrmFacturaFiltro(parentFrame, "Buscar Cliente");
        FacturaFiltroController ctrl = new FacturaFiltroController(filtro, "CLIENTE");
        filtro.setVisible(true);

        if (ctrl.getSeleccion() instanceof Cliente) {
            Cliente c = (Cliente) ctrl.getSeleccion();
            asignarCliente(c);
        }
    }

    private void buscarClientePorNit() {
        String nit = vista.getTxtNit().getText().trim();
        if (nit.isEmpty()) return;

        try {
            Cliente c = clienteDAO.buscarPorNit(nit);
            if (c != null) {
                asignarCliente(c);
            } else {
                int crear = JOptionPane.showConfirmDialog(vista, 
                        "Cliente con NIT '" + nit + "' no encontrado. ¿Desea registrarlo?", 
                        "Cliente no existe", JOptionPane.YES_NO_OPTION);
                if (crear == JOptionPane.YES_OPTION) {
                    Cliente nuevo = new Cliente(0L, nit, vista.getTxtClienteNombre().getText().trim(), 
                            vista.getTxtDireccion().getText().trim(), "", "");
                    clienteDAO.guardar(nuevo);
                    asignarCliente(nuevo);
                }
            }
        } catch (Exception e) {
            System.err.println("Advertencia al buscar cliente por NIT: " + e.getMessage());
        }
    }

    private void asignarCliente(Cliente c) {
        if (facturaActual != null) {
            facturaActual.setCliente(c);
        }
        vista.getTxtNit().setText(c.getNit());
        vista.getTxtClienteNombre().setText(c.getNombre());
        vista.getTxtDireccion().setText(c.getDireccion() != null ? c.getDireccion() : "");
    }

    private void capturarClienteDesdePantalla() {
        String nit = vista.getTxtNit().getText().trim();
        String nom = vista.getTxtClienteNombre().getText().trim();
        String dir = vista.getTxtDireccion().getText().trim();

        if (nit.isEmpty()) nit = "CF";
        if (nom.isEmpty()) nom = "Consumidor Final";

        if (facturaActual.getCliente() == null) {
            facturaActual.setCliente(new Cliente(0L, nit, nom, dir, "", ""));
        } else {
            facturaActual.getCliente().setNit(nit);
            facturaActual.getCliente().setNombre(nom);
            facturaActual.getCliente().setDireccion(dir);
        }
    }

    private void abrirBuscadorProducto() {
        Window parentWindow = SwingUtilities.getWindowAncestor(vista);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        FrmFacturaFiltro filtro = new FrmFacturaFiltro(parentFrame, "Buscar Producto");
        FacturaFiltroController ctrl = new FacturaFiltroController(filtro, "PRODUCTO");
        filtro.setVisible(true);

        if (ctrl.getSeleccion() instanceof Producto) {
            cargarProducto((Producto) ctrl.getSeleccion());
        }
    }

    private void buscarProductoPorCodigo() {
        String cod = vista.getTxtCodigoProd().getText().trim();
        if (cod.isEmpty()) return;

        try {
            Producto p = productoDAO.buscarPorCodigo(cod);
            if (p != null) {
                cargarProducto(p);
            } else {
                JOptionPane.showMessageDialog(vista, "Producto con código '" + cod + "' no encontrado.", 
                        "No encontrado", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Advertencia al buscar producto: " + e.getMessage());
        }
    }

    private void cargarProducto(Producto p) {
        this.productoSeleccionado = p;
        vista.getTxtCodigoProd().setText(p.getCodigo());
        vista.getTxtNombreProd().setText(p.getNombre());
        vista.getTxtPrecioProd().setText(FormatoMoneda.formatear(p.getPrecioVenta()));
        vista.getTxtStockProd().setText(p.getInventario() != null ? p.getInventario().getExistencia().toString() : "0");
        vista.getTxtCantidadProd().setText("1");
        vista.getTxtCantidadProd().requestFocus();
    }

    public Factura getFacturaActual() {
        return facturaActual;
    }

    public Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public void setUsuarioSesion(Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
    }
}
