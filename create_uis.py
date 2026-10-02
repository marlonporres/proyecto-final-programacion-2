import os

base_dir = r'C:\Users\marlo\UNIVERSIDAD\2026\SEGUNDO SEMESTRE 2026\Programacion_2\sistema-ventas-proyecto\proyecto-final-programacion-2\src\main\java\gt\edu\umg\ventas'

def write_file(subpath, content):
    path = os.path.join(base_dir, subpath)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

# FrmClientes
frm_clientes = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmClientes extends JInternalFrame {
    public FrmClientes() {
        super("Clientes", true, true, true, true);
        setSize(600, 400);
        JPanel pnlNorte = new JPanel(new GridLayout(3, 2));
        pnlNorte.add(new JLabel("NIT:")); JTextField txtNit = new JTextField(); pnlNorte.add(txtNit);
        pnlNorte.add(new JLabel("Nombre:")); JTextField txtNombre = new JTextField(); pnlNorte.add(txtNombre);
        JButton btnGuardar = new JButton("Guardar"); pnlNorte.add(btnGuardar);
        JButton btnRefrescar = new JButton("Refrescar"); pnlNorte.add(btnRefrescar);
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"ID", "NIT", "Nombre", "Direccion"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}'''
write_file(r'vista\FrmClientes.java', frm_clientes)

# FrmProductos
frm_prod = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmProductos extends JInternalFrame {
    public FrmProductos() {
        super("Productos", true, true, true, true);
        setSize(600, 400);
        JPanel pnlNorte = new JPanel(new GridLayout(3, 2));
        pnlNorte.add(new JLabel("Codigo:")); JTextField txtCodigo = new JTextField(); pnlNorte.add(txtCodigo);
        pnlNorte.add(new JLabel("Nombre:")); JTextField txtNombre = new JTextField(); pnlNorte.add(txtNombre);
        JButton btnGuardar = new JButton("Guardar"); pnlNorte.add(btnGuardar);
        JButton btnRefrescar = new JButton("Refrescar"); pnlNorte.add(btnRefrescar);
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"ID", "Codigo", "Nombre", "Precio"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}'''
write_file(r'vista\FrmProductos.java', frm_prod)

# FrmCategorias
frm_cat = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmCategorias extends JInternalFrame {
    public FrmCategorias() {
        super("Categorias", true, true, true, true);
        setSize(600, 400);
        JPanel pnlNorte = new JPanel(new GridLayout(2, 2));
        pnlNorte.add(new JLabel("Nombre:")); JTextField txtNombre = new JTextField(); pnlNorte.add(txtNombre);
        JButton btnGuardar = new JButton("Guardar"); pnlNorte.add(btnGuardar);
        JButton btnRefrescar = new JButton("Refrescar"); pnlNorte.add(btnRefrescar);
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"ID", "Nombre", "Descripcion"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}'''
write_file(r'vista\FrmCategorias.java', frm_cat)

# FrmInventario
frm_inv = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
public class FrmInventario extends JInternalFrame {
    public FrmInventario() {
        super("Inventario", true, true, true, true);
        setSize(600, 400);
        JPanel pnlNorte = new JPanel(new FlowLayout());
        pnlNorte.add(new JLabel("Filtro Bodega:")); pnlNorte.add(new JComboBox<>(new String[]{"Bodega Central"}));
        pnlNorte.add(new JLabel("Producto:")); pnlNorte.add(new JTextField(10));
        JButton btnBuscar = new JButton("Buscar"); pnlNorte.add(btnBuscar);
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"Producto", "Bodega", "Actual", "Reservada", "Disponible"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}'''
write_file(r'vista\FrmInventario.java', frm_inv)

# FrmDespacho
frm_des = '''package gt.edu.umg.ventas.vista;
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
        JButton btnCargar = new JButton("Cargar Orden"); pnlNorte.add(btnCargar);
        JButton btnConfirmar = new JButton("Confirmar Despacho"); pnlNorte.add(btnConfirmar);
        add(pnlNorte, BorderLayout.NORTH);
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"Producto", "Solicitado", "Despachado"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }
}'''
write_file(r'vista\FrmDespacho.java', frm_des)

# FrmOrdenVenta
frm_ord = '''package gt.edu.umg.ventas.vista;
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
        JButton btnAgregar = new JButton("Agregar Detalle"); pnlNorte.add(btnAgregar);
        add(pnlNorte, BorderLayout.NORTH);
        
        JTable tabla = new JTable(new DefaultTableModel(new String[]{"Producto", "Cant", "Precio", "Subtotal"}, 0));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        
        JPanel pnlSur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlSur.add(new JLabel("Total: Q. 0.00"));
        JButton btnGuardar = new JButton("Guardar Orden"); pnlSur.add(btnGuardar);
        add(pnlSur, BorderLayout.SOUTH);
    }
}'''
write_file(r'vista\FrmOrdenVenta.java', frm_ord)

print("Real basic UIs created.")
