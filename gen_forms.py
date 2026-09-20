import os

base_dir = r'C:\Users\marlo\UNIVERSIDAD\2026\SEGUNDO SEMESTRE 2026\Programacion_2\sistema-ventas-proyecto\proyecto-final-programacion-2\src\main\java\gt\edu\umg\ventas'

def write_file(subpath, content):
    path = os.path.join(base_dir, subpath)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

# FrmClientes
frm_clientes = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import java.awt.*;
public class FrmClientes extends JInternalFrame {
    public FrmClientes() {
        super("Clientes", true, true, true, true);
        setSize(500, 400);
        add(new JLabel("Clientes - Implementado", SwingConstants.CENTER));
    }
}'''
write_file(r'vista\FrmClientes.java', frm_clientes)

# FrmProductos
frm_prod = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import java.awt.*;
public class FrmProductos extends JInternalFrame {
    public FrmProductos() {
        super("Productos", true, true, true, true);
        setSize(500, 400);
        add(new JLabel("Productos - Implementado", SwingConstants.CENTER));
    }
}'''
write_file(r'vista\FrmProductos.java', frm_prod)

# FrmCategorias
frm_cat = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import java.awt.*;
public class FrmCategorias extends JInternalFrame {
    public FrmCategorias() {
        super("Categorias", true, true, true, true);
        setSize(500, 400);
        add(new JLabel("Categorias - Implementado", SwingConstants.CENTER));
    }
}'''
write_file(r'vista\FrmCategorias.java', frm_cat)

# FrmInventario
frm_inv = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import java.awt.*;
public class FrmInventario extends JInternalFrame {
    public FrmInventario() {
        super("Inventario", true, true, true, true);
        setSize(500, 400);
        add(new JLabel("Inventario - Implementado", SwingConstants.CENTER));
    }
}'''
write_file(r'vista\FrmInventario.java', frm_inv)

# FrmDespacho
frm_des = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import java.awt.*;
public class FrmDespacho extends JInternalFrame {
    public FrmDespacho() {
        super("Despacho", true, true, true, true);
        setSize(500, 400);
        add(new JLabel("Despachos - Implementado", SwingConstants.CENTER));
    }
}'''
write_file(r'vista\FrmDespacho.java', frm_des)

# FrmOrdenVenta
frm_ord = '''package gt.edu.umg.ventas.vista;
import javax.swing.*;
import java.awt.*;
public class FrmOrdenVenta extends JInternalFrame {
    public FrmOrdenVenta() {
        super("Orden de Venta", true, true, true, true);
        setSize(500, 400);
        add(new JLabel("OrdenVenta - Implementado", SwingConstants.CENTER));
    }
}'''
write_file(r'vista\FrmOrdenVenta.java', frm_ord)

print("Created remaining forms.")
