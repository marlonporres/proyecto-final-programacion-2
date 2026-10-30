package gt.edu.umg.ventas.controlador;

import gt.edu.umg.ventas.dao.ClienteDAO;
import gt.edu.umg.ventas.dao.ClienteDAOImpl;
import gt.edu.umg.ventas.dao.ProductoDAO;
import gt.edu.umg.ventas.dao.ProductoDAOImpl;
import gt.edu.umg.ventas.modelo.Cliente;
import gt.edu.umg.ventas.modelo.Producto;
import gt.edu.umg.ventas.util.FormatoMoneda;
import gt.edu.umg.ventas.vista.FrmFacturaFiltro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para el cuadro de búsqueda y filtrado de Clientes y Productos.
 */
public class FacturaFiltroController {

    private final FrmFacturaFiltro vista;
    private final String tipo; // "CLIENTE" o "PRODUCTO"
    private final ClienteDAO clienteDAO;
    private final ProductoDAO productoDAO;

    private List<Cliente> clientesEncontrados = new ArrayList<>();
    private List<Producto> productosEncontrados = new ArrayList<>();
    private Object seleccion = null;

    public FacturaFiltroController(FrmFacturaFiltro vista, String tipo) {
        this.vista = vista;
        this.tipo = tipo;
        this.clienteDAO = new ClienteDAOImpl();
        this.productoDAO = new ProductoDAOImpl();

        configurarTabla();
        enlazarEventos();
        buscar();
    }

    private void configurarTabla() {
        DefaultTableModel modelo = vista.getModeloTabla();
        modelo.setRowCount(0);
        if ("CLIENTE".equals(tipo)) {
            modelo.setColumnIdentifiers(new Object[]{"ID", "NIT", "Nombre", "Dirección", "Teléfono"});
        } else {
            modelo.setColumnIdentifiers(new Object[]{"ID", "Código", "Nombre", "Precio", "Stock"});
        }
    }

    private void enlazarEventos() {
        vista.getBtnBuscar().addActionListener(e -> buscar());
        vista.getTxtFiltro().addActionListener(e -> buscar());
        vista.getBtnSeleccionar().addActionListener(e -> seleccionar());
    }

    private void buscar() {
        String texto = vista.getTxtFiltro().getText().trim();
        DefaultTableModel modelo = vista.getModeloTabla();
        modelo.setRowCount(0);

        try {
            if ("CLIENTE".equals(tipo)) {
                clientesEncontrados = clienteDAO.buscarPorTexto(texto);
                for (Cliente c : clientesEncontrados) {
                    modelo.addRow(new Object[]{
                            c.getIdCliente(),
                            c.getNit(),
                            c.getNombre(),
                            c.getDireccion(),
                            c.getTelefono()
                    });
                }
            } else {
                productosEncontrados = productoDAO.buscarPorTexto(texto);
                for (Producto p : productosEncontrados) {
                    modelo.addRow(new Object[]{
                            p.getIdProducto(),
                            p.getCodigo(),
                            p.getNombre(),
                            FormatoMoneda.formatear(p.getPrecioVenta()),
                            "-"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vista, "Error al consultar datos: " + e.getMessage(), 
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionar() {
        int fila = vista.getTblResultados().getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione un registro de la lista.", 
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("CLIENTE".equals(tipo)) {
            if (fila < clientesEncontrados.size()) {
                this.seleccion = clientesEncontrados.get(fila);
            }
        } else {
            if (fila < productosEncontrados.size()) {
                this.seleccion = productosEncontrados.get(fila);
            }
        }

        vista.dispose();
    }

    public Object getSeleccion() {
        return seleccion;
    }
}
