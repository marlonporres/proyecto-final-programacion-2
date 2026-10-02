package gt.edu.umg.ventas.util;
import javax.swing.*;
import java.awt.Component;

/** Interacción desacoplada para ensayar eventos Swing sin bloquear en diálogos modales. */
public final class Dialogos {
    private Dialogos() { }
    public interface Interaccion {
        void mensaje(Object mensaje, int tipo);
        int confirmar(Object mensaje);
        Object entrada(Object mensaje, Object[] opciones, Object inicial);
    }
    private static final ThreadLocal<Interaccion> interaccion = new ThreadLocal<>();
    public static void configurar(Interaccion valor) { if (valor==null) interaccion.remove(); else interaccion.set(valor); }
    public static void showMessageDialog(Component padre,Object mensaje) {
        showMessageDialog(padre,mensaje,"Información",JOptionPane.INFORMATION_MESSAGE);
    }
    public static void showMessageDialog(Component padre,Object mensaje,String titulo,int tipo) {
        if (interaccion.get()!=null) interaccion.get().mensaje(mensaje,tipo);
        else JOptionPane.showMessageDialog(padre,mensaje,titulo,tipo);
    }
    public static int showConfirmDialog(Component padre,Object mensaje,String titulo,int opciones) {
        return interaccion.get()!=null ? interaccion.get().confirmar(mensaje)
                : JOptionPane.showConfirmDialog(padre,mensaje,titulo,opciones);
    }
    public static String showInputDialog(Component padre,Object mensaje) {
        Object valor = interaccion.get()!=null ? interaccion.get().entrada(mensaje,null,null)
                : JOptionPane.showInputDialog(padre,mensaje);
        return valor==null ? null : valor.toString();
    }
    public static Object showInputDialog(Component padre,Object mensaje,String titulo,int tipo,Icon icono,Object[] opciones,Object inicial) {
        return interaccion.get()!=null ? interaccion.get().entrada(mensaje,opciones,inicial)
                : JOptionPane.showInputDialog(padre,mensaje,titulo,tipo,icono,opciones,inicial);
    }
}
