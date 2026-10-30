package gt.edu.umg.ventas.vista;
import gt.edu.umg.ventas.util.Dialogos;

import gt.edu.umg.ventas.controlador.MantenimientoCatalogoController;
import gt.edu.umg.ventas.modelo.*;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

final class EditorCatalogo {
    private EditorCatalogo() { }
    private static JPanel campos(Object... campos) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        for (int i = 0; i < campos.length; i += 2) { panel.add(new JLabel((String) campos[i])); panel.add((Component) campos[i + 1]); }
        return panel;
    }
    static boolean categoria(Component padre, long id) {
        MantenimientoCatalogoController ctrl = new MantenimientoCatalogoController();
        Categoria c = id > 0 ? ctrl.categoria(id) : new Categoria();
        JTextField nombre = new JTextField(c.getNombre(), 25), descripcion = new JTextField(c.getDescripcion());
        JCheckBox activa = new JCheckBox("Activa", id == 0 || c.isActiva());
        JPanel panel = campos("Nombre", nombre, "Descripción", descripcion, "Estado", activa);
        while (Dialogos.showConfirmDialog(padre, panel, "Categoría", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                c.setNombre(nombre.getText()); c.setDescripcion(descripcion.getText()); c.setActiva(activa.isSelected());
                ctrl.guardar(c); return true;
            } catch (Exception e) { Dialogos.showMessageDialog(padre, e.getMessage(), "No se pudo guardar", JOptionPane.ERROR_MESSAGE); }
        }
        return false;
    }
    static boolean producto(Component padre, long id) {
        MantenimientoCatalogoController ctrl = new MantenimientoCatalogoController();
        Producto p = id > 0 ? ctrl.producto(id) : new Producto();
        JTextField codigo = new JTextField(p.getCodigo(), 25), nombre = new JTextField(p.getNombre());
        JTextField descripcion = new JTextField(p.getDescripcion());
        JTextField precio = new JTextField(p.getPrecioVenta() == null ? "0.00" : p.getPrecioVenta().toPlainString());
        JCheckBox activo = new JCheckBox("Activo", id == 0 || p.isActivo());
        JComboBox<Categoria> categoria = new JComboBox<>(ctrl.categorias().toArray(Categoria[]::new));
        if (p.getCategoria() != null) for (int i = 0; i < categoria.getItemCount(); i++) {
            if (categoria.getItemAt(i).getIdCategoria() == p.getCategoria().getIdCategoria()) categoria.setSelectedIndex(i);
        }
        JPanel panel = campos("Código", codigo, "Nombre", nombre, "Descripción", descripcion, "Precio (Q)", precio,
                "Categoría", categoria, "Estado", activo);
        while (Dialogos.showConfirmDialog(padre, panel, "Producto", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                p.setCodigo(codigo.getText()); p.setNombre(nombre.getText()); p.setDescripcion(descripcion.getText());
                p.setPrecioVenta(new BigDecimal(precio.getText().trim())); p.setCategoria((Categoria) categoria.getSelectedItem()); p.setActivo(activo.isSelected());
                ctrl.guardar(p); return true;
            } catch (Exception e) { Dialogos.showMessageDialog(padre, e.getMessage(), "No se pudo guardar", JOptionPane.ERROR_MESSAGE); }
        }
        return false;
    }
}
