package gt.edu.umg.ventas.vista;
import gt.edu.umg.ventas.controlador.DespachoController;
import gt.edu.umg.ventas.modelo.*;
import gt.edu.umg.ventas.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FrmDespacho extends JInternalFrame {
    private final DespachoController controller = new DespachoController();
    private final JComboBox<OrdenVentaItem> ordenes = new JComboBox<>();
    private final JComboBox<Bodega> bodegas = new JComboBox<>();
    private final JLabel estado = new JLabel("Seleccione una orden.");
    private final JButton generar = new JButton("Generar orden de despacho");
    private final JButton confirmar = new JButton("Confirmar despacho");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Código","Producto","Solicitado","Despachado"},0) {
        @Override public boolean isCellEditable(int fila,int columna) { return false; }
    };
    private OrdenVenta seleccion;
    private Despacho despacho;
    private record OrdenVentaItem(OrdenVenta orden) {
        @Override public String toString() {
            return orden == null ? "-- Seleccione una orden pendiente --"
                    : orden.getNumeroOrden()+" | "+orden.getCliente().getNombre();
        }
    }
    public FrmDespacho() {
        super("Generación y confirmación de despacho",true,true,true,true);
        setSize(920,560); setMinimumSize(new Dimension(800,480));
        setLayout(new BorderLayout(10,10));
        JPanel norte = new JPanel(new GridLayout(0,1,6,6));
        norte.setBorder(BorderFactory.createTitledBorder("Despacho completo desde una bodega"));
        norte.add(new JLabel("Orden de venta pendiente:")); norte.add(ordenes);
        norte.add(new JLabel("Bodega de salida (se fija al generar):")); norte.add(bodegas); norte.add(estado);
        add(norte,BorderLayout.NORTH);
        JTable tabla = new JTable(modelo); tabla.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(tabla); scroll.setColumnHeaderView(tabla.getTableHeader());
        add(scroll,BorderLayout.CENTER);
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refrescar = new JButton("Refrescar");
        sur.add(refrescar); sur.add(generar); sur.add(confirmar); add(sur,BorderLayout.SOUTH);
        ordenes.addActionListener(e -> cargarSeleccion());
        bodegas.addActionListener(e -> actualizarBotones());
        refrescar.addActionListener(e -> cargarDatosIniciales());
        generar.addActionListener(e -> generar());
        confirmar.addActionListener(e -> confirmar());
        cargarDatosIniciales();
    }
    public void cargarDatosIniciales() {
        Integer anterior = seleccion == null ? null : seleccion.getId();
        try {
            bodegas.removeAllItems();
            for (Bodega b : controller.obtenerBodegas()) bodegas.addItem(b);
            ordenes.removeAllItems(); ordenes.addItem(new OrdenVentaItem(null));
            for (OrdenVenta o : controller.obtenerOrdenesPendientes()) ordenes.addItem(new OrdenVentaItem(o));
            for (int i=1;i<ordenes.getItemCount();i++)
                if (ordenes.getItemAt(i).orden().getId().equals(anterior)) ordenes.setSelectedIndex(i);
            cargarSeleccion();
        } catch (Exception e) { error(e); }
    }
    private void cargarSeleccion() {
        OrdenVentaItem item = (OrdenVentaItem) ordenes.getSelectedItem();
        seleccion = null; despacho = null; modelo.setRowCount(0); bodegas.setEnabled(true);
        if (item == null || item.orden()==null) { estado.setText("Seleccione una orden."); actualizarBotones(); return; }
        try {
            seleccion = controller.obtenerOrdenConDetalles(item.orden().getId());
            despacho = controller.obtenerPorOrden(seleccion.getId());
            if (despacho != null) {
                estado.setText(despacho.getNumeroDespacho()+" | Estado: "+despacho.getEstado());
                boolean encontrada = false;
                for (int i=0;i<bodegas.getItemCount();i++) if (bodegas.getItemAt(i).getId().equals(despacho.getBodega().getId())) {
                    bodegas.setSelectedIndex(i); encontrada=true; break;
                }
                if (!encontrada) { bodegas.addItem(despacho.getBodega()); bodegas.setSelectedItem(despacho.getBodega()); }
                bodegas.setEnabled(false);
                for (DetalleDespacho d : despacho.getDetalles())
                    modelo.addRow(new Object[]{d.getProducto().getCodigo(),d.getProducto().getNombre(),d.getCantidadSolicitada(),d.getCantidadDespachada()});
            } else {
                estado.setText("Orden guardada | Aún no tiene despacho. Generarlo no rebaja stock.");
                for (DetalleOrdenVenta d : seleccion.getDetalles())
                    modelo.addRow(new Object[]{d.getProducto().getCodigo(),d.getProducto().getNombre(),d.getCantidad(),0});
            }
        } catch (Exception e) { error(e); }
        actualizarBotones();
    }
    private void actualizarBotones() {
        generar.setEnabled(seleccion!=null && despacho==null && bodegas.getSelectedItem()!=null);
        confirmar.setEnabled(despacho!=null && despacho.getEstado()==EstadoDespacho.PENDIENTE);
    }
    private void generar() {
        if (seleccion==null || despacho!=null) return;
        try {
            List<DetalleDespacho> detalles = new ArrayList<>();
            for (DetalleOrdenVenta od : seleccion.getDetalles()) {
                DetalleDespacho d = new DetalleDespacho(); d.setProducto(od.getProducto());
                d.setCantidadSolicitada(od.getCantidad()); d.setCantidadDespachada(0); detalles.add(d);
            }
            Despacho nuevo = controller.generarDespacho(seleccion,(Bodega)bodegas.getSelectedItem(),detalles);
            Dialogos.showMessageDialog(this,"Despacho "+nuevo.getNumeroDespacho()+" guardado como PENDIENTE.\nEl inventario no cambió.");
            cargarSeleccion(); CambiosVentas.notificar(this);
        } catch (Exception e) { error(e); }
    }
    private void confirmar() {
        if (despacho==null) return;
        if (Dialogos.showConfirmDialog(this,"¿Confirmar la entrega completa? En este momento se rebajará el inventario.",
                "Confirmar despacho",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        try {
            Despacho confirmado = controller.confirmarDespacho(despacho.getId());
            Dialogos.showMessageDialog(this,"Despacho "+confirmado.getNumeroDespacho()+" CONFIRMADO.\nInventario rebajado y orden COMPLETADA.");
            cargarDatosIniciales(); CambiosVentas.notificar(this);
        } catch (Exception e) { error(e); }
    }
    private void error(Exception e) {
        Dialogos.showMessageDialog(this,e.getMessage(),"Operación no completada",JOptionPane.ERROR_MESSAGE);
    }
}
